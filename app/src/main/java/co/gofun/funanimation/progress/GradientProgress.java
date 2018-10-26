package co.gofun.funanimation.progress;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateInterpolator;

public class GradientProgress extends View {

    private static final String TAG = "GradientProgress";
    private Paint mPaint;
    private LinearGradient mLeftLinearGradient;
    private LinearGradient mRightLinearGradient;
    private int mBlueEnd;
    private int mWhiteStart;
    private float mWidth;
    private float mHeight;

    public static final int BIGGER_TIME = 1000;
    public static final int SMALLER_TIME = 500;
    private float mStartPosition = 1 / 4f;// 从四分之一的位置开始
    private float mEndPosition = 3 / 4f;//结束在四分之三的位置

    private int[] mColors = new int[]{Color.WHITE, Color.CYAN, Color.BLUE};
    private float[] mColorsWeight = new float[]{0f, 0.5f, 1f};
    private ValueAnimator mValueAnimatorBigger;
    private ValueAnimator mValueAnimatorSmaller;
    private AnimatorSet mAnimatorSet;


    public GradientProgress(Context context) {
        super(context);
        init();
    }

    public GradientProgress(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GradientProgress(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public GradientProgress(
            Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        mPaint = new Paint();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mWidth = getWidth();
        mHeight = getHeight();
        mLeftLinearGradient = new LinearGradient(mWhiteStart, 0, mBlueEnd, 0,
                mColors, mColorsWeight, Shader.TileMode.CLAMP);
        mRightLinearGradient = new LinearGradient(mWidth - mWhiteStart, 0,
                mWidth - mBlueEnd, 0, mColors, mColorsWeight, Shader.TileMode.CLAMP);
        drawProgress(canvas);


    }

    private void drawProgress(Canvas canvas) {
        mPaint.setShader(mLeftLinearGradient);
        if ((mBlueEnd - mHeight / 2f) < (mWidth - mHeight) / 2f) {
            canvas.drawRect(0, 0, mBlueEnd - mHeight / 2f, mHeight, mPaint);
            canvas.drawCircle(mBlueEnd - mHeight / 2f, mHeight / 2f,
                    mHeight / 2f, mPaint);
        } else {
            canvas.drawRect(0, 0, mBlueEnd, mHeight, mPaint);
        }

        mPaint.setShader(mRightLinearGradient);
        if ((mBlueEnd - mHeight / 2f) < (mWidth - mHeight) / 2f) {
            canvas.drawCircle(mWidth - mBlueEnd + mHeight / 2f, mHeight / 2f,
                    mHeight / 2f, mPaint);
            canvas.drawRect(mWidth - mBlueEnd + mHeight / 2f, 0, mWidth, mHeight, mPaint);
        } else {
            canvas.drawRect(mWidth - mBlueEnd, 0, mWidth, mHeight, mPaint);
        }
    }


    /**
     * @param progress 0-100
     */
    public void setProgress(int progress) {
        if (progress <= 50) {
            updateEnd(progress / 50f);
        } else {
            updateStart((progress - 50) % 50 / 50f);
        }
    }

    public void reset() {
        mWhiteStart = mBlueEnd = 0;
    }

    /**
     * @param position 0-1
     */
    public GradientProgress startPosition(float position) {
        mStartPosition = position;
        return this;
    }

    public GradientProgress endPosition(float endPosition) {
        this.mEndPosition = endPosition;
        return this;
    }

    public GradientProgress color(int s, int m, int e) {
        mColors = new int[]{s, m, e};
        return this;
    }

    private void updateEnd(float progress) {
        final float width = getWidth() / 2f;
        //LogUtil.i(TAG, "updateEnd->mWidth:" + width);
        if (mBlueEnd <= getWidth() / 2)
            mBlueEnd = (int) ((mStartPosition + progress) * width);
        if (mBlueEnd >= getWidth() / 2)//去除上面的float误差
            mBlueEnd = getWidth() / 2;

        //LogUtil.i(TAG, "updateEnd->mBlueEnd: " + mBlueEnd);
        invalidate();
    }

    private void updateStart(float progress) {
        final float width = getWidth() / 2f;
        if (mWhiteStart < (mEndPosition * width))
            mWhiteStart = (int) (progress * width);
        invalidate();
    }


    public void start() {
        reset();
        //final float mWidth = getWidth();
        final float width = getWidth() / 2;
        //LogUtil.i(TAG, "start->mWidth:" + width);
        if (mValueAnimatorBigger == null) {
            mValueAnimatorBigger = ValueAnimator.ofInt(0, (int) width);
            mValueAnimatorBigger.setDuration(BIGGER_TIME);
            mValueAnimatorBigger.setInterpolator(new AccelerateInterpolator());
            mValueAnimatorBigger.addUpdateListener(valueAnimator ->
                    updateEnd((int) valueAnimator.getAnimatedValue() / width));
        }


        if (mValueAnimatorSmaller == null) {
            mValueAnimatorSmaller = ValueAnimator.ofInt(1, (int) width);
            mValueAnimatorSmaller.setDuration(SMALLER_TIME);
            mValueAnimatorSmaller.addUpdateListener(animation ->
                    updateStart((int) animation.getAnimatedValue() / width));

        }
        if (mAnimatorSet == null) {
            mAnimatorSet = new AnimatorSet();
            mAnimatorSet.playSequentially(mValueAnimatorBigger, mValueAnimatorSmaller);

            mAnimatorSet.addListener(mAnimatorListener);

        } else {
            mAnimatorSet.cancel();
        }
        mAnimatorSet.start();
    }

    private Animator.AnimatorListener mAnimatorListener = new Animator.AnimatorListener() {
        @Override
        public void onAnimationStart(Animator animation) {

        }

        @Override
        public void onAnimationEnd(Animator animation) {
            start();
        }

        @Override
        public void onAnimationCancel(Animator animation) {

        }

        @Override
        public void onAnimationRepeat(Animator animation) {

        }
    };

    public void stop() {
        if (mAnimatorSet != null) {
            mAnimatorSet.removeListener(mAnimatorListener);
            mAnimatorSet.cancel();
            mAnimatorSet.addListener(mAnimatorListener);
        }
    }

}
