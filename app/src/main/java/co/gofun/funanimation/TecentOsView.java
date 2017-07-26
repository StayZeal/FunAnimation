package co.gofun.funanimation;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;


public class TecentOsView extends SurfaceView implements SurfaceHolder.Callback {

    private static final String TAG = "TecentOsView";


    private final Object mSurfaceLock = new Object();
    private DrawThread mThread;
    private Paint mPaint;
    private Path mPath;

    /**
     * 振幅
     * amplitude
     */
    private int amplitude = 20;

    /**
     * 角速度
     */
    private float w = 0.5f;
    /**
     * 相位
     */
    private int offsetX = 0;
    /**
     * y轴方向位移
     */
    private double offsetY = 0;

    public TecentOsView(Context context) {
        super(context);
        init();
    }

    public TecentOsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TecentOsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public TecentOsView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {


        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.RED);
        mPath = new Path();

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

        //这里可以获取SurfaceView的宽高等信息
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

        synchronized (mSurfaceLock) {  //这里需要加锁，否则doDraw中有可能会crash
            mThread.setRun(false);
        }

    }

    private class DrawThread extends Thread {
        private static final long SLEEP_TIME = 4;
        private SurfaceHolder mHolder;
        private boolean mIsRun = false;

        public DrawThread(SurfaceHolder holder) {
            super(TAG);
            mHolder = holder;
        }

        @Override
        public void run() {
            while (true) {
                synchronized (mSurfaceLock) {
                   /* offsetX += 5;
                    if (offsetX == 360) {
                        offsetX = 0;
                    }*/

                    if (!mIsRun) {
                        return;
                    }
                    Canvas canvas = mHolder.lockCanvas();
                    if (canvas != null) {
                        doDraw(canvas);  //这里做真正绘制的事情
                        mHolder.unlockCanvasAndPost(canvas);
                    }
                }
                try {
                    Thread.sleep(SLEEP_TIME);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        public void setRun(boolean isRun) {
            this.mIsRun = isRun;
        }
    }

    private void doDraw(Canvas canvas) {

//        drawSin(canvas);
        drawSinLine(canvas);
//        drawSinLine(canvas, true);
    }

    private void drawSinLine(Canvas canvas) {
        drawSinLine(canvas, w, amplitude);
    }



    /**
     * @param canvas
     * @param w
     * @param amplitude
     */
    private void drawSinLine(Canvas canvas, float w, float amplitude) {
        Log.i(TAG, "Start:" + System.currentTimeMillis() % 10000 + "ms");
        int x = 0, y = 500;
        canvas.drawColor(Color.WHITE);
        mPaint.setColor(Color.BLACK);
//        mPaint.setColor(Color.YELLOW);
//        mPaint.setColor(Color.BLUE);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(2);
        mPath.reset();

        int preX = 0;
        int preY = 0;
        int centerY = getHeight() / 2;

        for (int i = 0; i < getWidth(); i++) {
            x = i;
            y = centerY - (int) (amplitude * Math.sin((i * w + offsetX) * Math.PI / 180) + offsetY);

            preX = x;
            preY = y;
            if (i == 0) {
                //x=0的时候，即左上角的点，移动画笔于此
                mPath.moveTo(x, y);
            }
            //用每个x求得每个y，用quadTo方法连接成一条贝塞尔曲线
            mPath.quadTo(preX, preY, x, y);
        }

//        mPath.lineTo(getWidth(), getHeight());
//        mPath.lineTo(0, getHeight());
//        mPath.close();
        canvas.drawPath(mPath, mPaint);


        Log.i(TAG, "End:" + System.currentTimeMillis() % 10000 + "ms");
    }


}
