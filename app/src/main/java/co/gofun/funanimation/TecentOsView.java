package co.gofun.funanimation;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Xfermode;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import static android.R.attr.width;


public class TecentOsView extends SurfaceView implements SurfaceHolder.Callback {

    private static final String TAG = "TecentOsView";


    private final Object mSurfaceLock = new Object();
    private DrawThread mThread;
    private Paint mLinePaint;
    private Path mLinePath1;
    private Path mLinePath2;
    private Path mLinePath3;

    /**
     * 绘图交叉模式。放在成员变量避免每次重复创建。
     */
    private final Xfermode clipXfermode = new PorterDuffXfermode(PorterDuff.Mode.SRC_IN);

    private final Xfermode clearXfermode = new PorterDuffXfermode(PorterDuff.Mode.CLEAR);

    private final Xfermode srcXfermode = new PorterDuffXfermode(PorterDuff.Mode.SRC);

    /**
     * 振幅
     * amplitude
     */
    private float amplitude ;

    /**
     * 角速度
     */
    private float w = 0.5f;
    /**
     * 相位
     */
    private float offsetX = 0;
    /**
     * y轴方向位移
     */
    private double offsetY = 0;
    private Paint clearScreenPaint = new Paint();


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


        mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLinePaint.setColor(Color.RED);
        mLinePath1 = new Path();
        mLinePath2 = new Path();
        mLinePath3 = new Path();

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

            long startAt = System.currentTimeMillis();
            while (true) {
                synchronized (mSurfaceLock) {
//                    offsetX = 5;
                   /* if (offsetX == 360) {
                        offsetX = 0;
                    }*/

                    if (!mIsRun) {
                        return;
                    }
                    Canvas canvas = mHolder.lockCanvas();
                    if (canvas != null) {
                        doDraw(canvas, System.currentTimeMillis() - startAt);  //这里做真正绘制的事情
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

    private void doDraw(Canvas canvas, long millisPassed) {

//        drawSin(canvas);
        amplitude= getWidth() >> 3;


        offsetX = millisPassed / 500f;
        drawSinLine(canvas);
//        drawSinLine(canvas, true);
    }

    private void drawSinLine(Canvas canvas) {
        initArray();
        drawSinLine(canvas, w, amplitude);
    }

    /**
     * 极值和交叉点
     * 波峰和两条路径交叉点的记录，包括起点和终点，用于绘制渐变。
     * 通过日志可知其数量范围为7~9个，故这里size取9。
     * <p>
     * 每个元素都是一个float[2]，用于保存xy值
     */
    private final float[][] crestAndCrossPints = new float[9][];

    {//直接分配内存
        for (int i = 0; i < 9; i++) {
            crestAndCrossPints[i] = new float[2];
        }
    }


    /**
     * @param canvas
     * @param w
     * @param amplitude
     */
    private void drawSinLine(Canvas canvas, float w, float amplitude) {
//        Log.i(TAG, "Start:" + System.currentTimeMillis() % 10000 + "ms");
        float x, y;


        clearScreenPaint.setXfermode(clearXfermode);
        canvas.drawPaint(clearScreenPaint);
        clearScreenPaint.setXfermode(srcXfermode);

//        canvas.drawColor(Color.WHITE);

//        mPaint.setColor(Color.BLACK);
//        mPaint.setColor(Color.YELLOW);
        mLinePaint.setColor(0xBF158072);
        mLinePaint.setStyle(Paint.Style.STROKE);
        mLinePaint.setStrokeWidth(5);
//        mLinePath1.reset();
//        mLinePath2.reset();
//        mLinePath3.reset();
//
        mLinePath1.rewind();
        mLinePath2.rewind();
        mLinePath3.rewind();


        float preX = 0;
        float preY = 0;
        float absPreY = 0;
        float curY = 0f;
        float absCurY = 0;
        float nextY = (float) MathUtil.getY(mapX[0], offsetX, amplitude);
        float absNextY = 0;
        float centerY = getHeight() / 2;

        //上一个筛选出的点是波峰还是交错点
        boolean lastIsCrest = false;
        int crestAndCrossCount = 0;

        mLinePath1.moveTo(0, centerY);
        mLinePath2.moveTo(0, centerY);
        mLinePath3.moveTo(0, centerY);

        for (int i = 0; i <= SAMPLE_COUNT; i++) {
            x = sampleX[i];
            preY = curY;
            curY = nextY;
            nextY = i == SAMPLE_COUNT ? 0 : (float) MathUtil.getY(mapX[i + 1], offsetX, amplitude);
//            y = (float) MathUtil.getY(mapX[i], offsetX, amplitude);
//            y = (float) (centerY - MathUtil.calcValue(mapX[i], 0.5f));

//            Log.i(TAG,"y:"+nextY);
/*
            if (i == 0) {
                //x=0的时候，即左上角的点，移动画笔于此
                mLinePath1.moveTo(0, centerY);
                mLinePath2.moveTo(0, centerY);
                mLinePath3.moveTo(0, centerY);
            }*/
            mLinePath1.lineTo(x, centerY + curY);
            mLinePath2.lineTo(x, centerY - curY);
            mLinePath3.lineTo(x, centerY + curY / 5);//振幅为1/5


            absPreY = Math.abs(preY);
            absCurY = Math.abs(curY);
            absNextY = Math.abs(nextY);


            if (i == 0 || i == SAMPLE_COUNT || lastIsCrest && absCurY < absPreY && absCurY < absNextY) {
                crestAndCrossPints[crestAndCrossCount][0] = x;
                crestAndCrossPints[crestAndCrossCount][1] = 0;
                crestAndCrossCount++;
                lastIsCrest = false;
            } else if (!lastIsCrest && absCurY > absPreY && absCurY > absNextY) {
                crestAndCrossPints[crestAndCrossCount][0] = x;
                crestAndCrossPints[crestAndCrossCount][1] = curY;
                crestAndCrossCount++;
                lastIsCrest = true;
            }
        }

        mLinePath1.lineTo(width, centerY);
        mLinePath2.lineTo(width, centerY);
        mLinePath3.lineTo(width, centerY);

        //记录layer
        int saveCount = canvas.saveLayer(0, 0, getWidth(), getHeight(), null, Canvas.ALL_SAVE_FLAG);  //相关知识点： http://blog.csdn.net/cquwentao/article/details/51423371

        mLinePaint.setStyle(Paint.Style.FILL);
        mLinePaint.setStrokeWidth(1);
        canvas.drawPath(mLinePath1, mLinePaint);
        canvas.drawPath(mLinePath2, mLinePaint);
//        canvas.drawPath(mLinePath3, mLinePaint);

        //绘制渐变
        mLinePaint.setStyle(Paint.Style.FILL);
        mLinePaint.setColor(Color.BLACK);
        mLinePaint.setXfermode(clipXfermode);

        float startX, crestY, endX;
        for (int i = 2; i < crestAndCrossCount; i += 2) {
            startX = crestAndCrossPints[i - 2][0];
            endX = crestAndCrossPints[i][0];
            crestY = crestAndCrossPints[i - 1][1];
            mLinePaint.setShader(new LinearGradient(0,
                    centerY + crestY,
                    0,
                    centerY - crestY,
                    regionStartColor,
                    regionEndColor,
                    Shader.TileMode.CLAMP));

            rectF.set(startX, centerY + crestY, endX, centerY - crestY);
            canvas.drawRect(rectF, mLinePaint);
        }

        //清理一下
        mLinePaint.setShader(null);
        mLinePaint.setXfermode(null);

        //叠加layer，因为使用了SRC_IN的模式所以只会保留波形渐变重合的地方
        canvas.restoreToCount(saveCount);

        //绘制上弦线
        mLinePaint.setStrokeWidth(3);
        mLinePaint.setStyle(Paint.Style.STROKE);

        mLinePaint.setColor(regionStartColor);
        canvas.drawPath(mLinePath1, mLinePaint);

        //绘制下弦线
        mLinePaint.setColor(regionEndColor);
        canvas.drawPath(mLinePath2, mLinePaint);

        //绘制中间线
        mLinePaint.setColor(regionCenterColor);
        canvas.drawPath(mLinePath3, mLinePaint);

//        Log.i(TAG, "End:" + System.currentTimeMillis() % 10000 + "ms");
    }

    private int regionStartColor = 0x6482e6c9;
    private int regionCenterColor = 0x64ffffff;
    private int regionEndColor = 0xf00a2140;
    /**
     * 用于处理矩形的rectF
     */
    private final RectF rectF = new RectF();

    /**
     * 在[-2,2]范围内x所有的取值
     */
    private float[] mapX;
    /**
     * map映射在canvas上x的取值
     */
    private float[] sampleX;

    /**
     * 采样的点
     */
    private int SAMPLE_COUNT = 64;


    private void initArray() {
        if (sampleX == null || mapX == null) {
            sampleX = new float[SAMPLE_COUNT + 1];
            mapX = new float[SAMPLE_COUNT + 1];
            float gap = (float) getWidth() / SAMPLE_COUNT;
            float x;
            for (int i = 0; i <= SAMPLE_COUNT; i++) {
                x = i * gap;
                sampleX[i] = x;
                Log.i(TAG, "X:" + x);
                mapX[i] = (x / (float) getWidth()) * 4 - 2;
            }
        }
    }


}
