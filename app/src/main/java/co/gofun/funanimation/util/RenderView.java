package co.gofun.funanimation.util;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

/**
 * 多线程更新SurfaceView
 * 可复用
 * Created by Arthor on 2017/7/30.
 */

public abstract class RenderView extends SurfaceView implements Callback {

    private Object mSurfaceLock = new Object();
    private DrawThread mThread;

    public RenderView(Context context) {
        super(context);
        init();
    }

    public RenderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RenderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public RenderView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();

    }

    private void init() {

        getHolder().addCallback(this);
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        mThread = new DrawThread(holder);
        mThread.setRun(true);
        mThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

        synchronized (mSurfaceLock) {
            mThread.setRun(false);
        }
    }

    class DrawThread extends Thread {

        /**
         * 16毫秒刷新一次，因为屏幕每秒刷新60次
         */
        private static final long SLEEP_TIME = 16;
        private SurfaceHolder mSurfaceHolder;
        private boolean mIsRun;

        public DrawThread(SurfaceHolder mSurfaceHolder) {
            this.mSurfaceHolder = mSurfaceHolder;
        }

        @Override
        public void run() {

            long startTime = System.currentTimeMillis();
            while (true) {

                synchronized (mSurfaceLock) {
                    if (!mIsRun) {
                        return;
                    }
                    try {

                        Canvas canvas = mSurfaceHolder.lockCanvas();

                        if (canvas != null) {

                            //执行draw操作
                            doDraw(canvas, System.currentTimeMillis() - startTime);
                        }
                        mSurfaceHolder.unlockCanvasAndPost(canvas);
                        Thread.sleep(SLEEP_TIME);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } finally {
                    }
                }

            }
        }

        public void setRun(boolean isRun) {
            this.mIsRun = isRun;
        }

    }

    /**
     * 子类重写这个方法
     *
     * @param canvas
     * @param elapseTime 线程已经执行的时间
     */
    protected abstract void doDraw(Canvas canvas, long elapseTime);
}
