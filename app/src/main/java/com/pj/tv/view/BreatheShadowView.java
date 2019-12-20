package com.pj.tv.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import static android.animation.ValueAnimator.INFINITE;
import static android.animation.ValueAnimator.REVERSE;

/**
 * user by pj567
 * date on 2019/12/16.
 */

final class BreatheShadowView extends View {
    private Paint mShadowPaint;
    private Paint mBorderPaint;
    private RectF mShadowRectF;
    private Path mPath;
    private float mShadowWidth = pt2px(10);
    private float mBorderWidth = pt2px(2);
    private int mBorderColor = Color.WHITE;
    private int mShadowColor = Color.WHITE;
    private int mBreatheDuration = 4000;
    private long mStartDelay = 400;
    private AnimatorSet mAnimatorSet;
    private boolean isBreathe = true;
    private boolean isShadow = true;
    private boolean isBorder = true;
    private float mTopLeftRadius;
    private float mTopRightRadius;
    private float mBottomLeftRadius;
    private float mBottomRightRadius;

    public BreatheShadowView(Context context) {
        this(context, null);
    }

    public BreatheShadowView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BreatheShadowView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // 需禁用硬件加速
        setLayerType(LAYER_TYPE_SOFTWARE, null);
        mShadowPaint = new Paint();
        mPath = new Path();
        mShadowPaint.setMaskFilter(new BlurMaskFilter(mShadowWidth, BlurMaskFilter.Blur.OUTER));
        mShadowPaint.setStrokeWidth(pt2px(1));
        mShadowPaint.setColor(mShadowColor);

        mBorderPaint = new Paint();
        mBorderPaint.setColor(mBorderColor);
        mBorderPaint.setStrokeWidth(mBorderWidth);
        mBorderPaint.setStyle(Paint.Style.STROKE);
        mBorderPaint.setMaskFilter(new BlurMaskFilter(0.5f, BlurMaskFilter.Blur.NORMAL));
        setVisibility(GONE);
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldw, int oldh) {
        super.onSizeChanged(width, height, oldw, oldh);
        mShadowRectF = new RectF(mShadowWidth, mShadowWidth, width - mShadowWidth, height - mShadowWidth);
        final Path path = new Path();
        final float[] shadowRadius = new float[]{
                mTopLeftRadius, mTopLeftRadius,
                mTopRightRadius, mTopRightRadius,
                mBottomRightRadius, mBottomRightRadius,
                mBottomLeftRadius, mBottomLeftRadius};
        if (mTopLeftRadius != 0 || mTopRightRadius != 0 || mBottomLeftRadius != 0 || mBottomRightRadius != 0) {
            path.addRoundRect(mShadowRectF, shadowRadius, Path.Direction.CW);
        } else {
            path.addRoundRect(mShadowRectF, 0, 0, Path.Direction.CW);
        }
        mPath = path;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mShadowRectF == null) {
            return;
        }
        if (isShadow) {
            canvas.save();
            canvas.drawPath(mPath, mShadowPaint);
            canvas.restore();
        }
        if (isBorder) {
            canvas.save();
            canvas.drawPath(mPath, mBorderPaint);
            canvas.restore();
        }
    }

    private void createAnimatorSet() {
        mAnimatorSet = new AnimatorSet();
        mAnimatorSet.playSequentially(getAlphaAnimator());
    }

    private ObjectAnimator getAlphaAnimator() {
        ObjectAnimator alphaAnimator;
        alphaAnimator = ObjectAnimator.ofFloat(this, "alpha", 1f, 0.2f, 1f);
        alphaAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        alphaAnimator.setDuration(mBreatheDuration);
        alphaAnimator.setRepeatMode(REVERSE);
        alphaAnimator.setRepeatCount(INFINITE);
        alphaAnimator.setStartDelay(mStartDelay);
        alphaAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
                setAlpha(1f);
            }
        });
        return alphaAnimator;
    }

    public void start() {
        setVisibility(VISIBLE);
        if (isBreathe) {
            if (null != mAnimatorSet) {
                mAnimatorSet.cancel();
            }
            createAnimatorSet();
            mAnimatorSet.start();
        } else {
            setAlpha(1f);
        }
    }

    public void stop() {
        setVisibility(GONE);
        if (isBreathe) {
            if (null != mAnimatorSet) {
                mAnimatorSet.cancel();
            }
        }
    }

    public void setStartDelay(long mStartDelay) {
        this.mStartDelay = mStartDelay;
    }

    public void setShadowWidth(float mShadowWidth) {
        if (this.mShadowWidth != mShadowWidth) {
            this.mShadowWidth = mShadowWidth;
            if (mShadowPaint != null) {
                mShadowPaint.setMaskFilter(new BlurMaskFilter(mShadowWidth == 0 ? 0.1f : mShadowWidth, BlurMaskFilter.Blur.OUTER));
            }
        }
    }

    public void setBorderWidth(float mBorderWidth) {
        this.mBorderWidth = mBorderWidth;
        if (mBorderPaint != null) {
            mBorderPaint.setStrokeWidth(mBorderWidth);
        }
    }

    public void setTopLeftRadius(float mTopLeftRadius) {
        this.mTopLeftRadius = mTopLeftRadius;
    }

    public void setTopRightRadius(float mTopRightRadius) {
        this.mTopRightRadius = mTopRightRadius;
    }

    public void setBottomLeftRadius(float mBottomLeftRadius) {
        this.mBottomLeftRadius = mBottomLeftRadius;
    }

    public void setBottomRightRadius(float mBottomRightRadius) {
        this.mBottomRightRadius = mBottomRightRadius;
    }


    public void setShadow(boolean shadow) {
        isShadow = shadow;
    }

    public void setBorder(boolean border) {
        isBorder = border;
    }

    public void setShadowColor(int mShadowColor) {
        this.mShadowColor = mShadowColor;
        if (mShadowPaint != null) {
            mShadowPaint.setColor(mShadowColor);
        }
    }

    public void setBorderColor(int mBorderColor) {
        this.mBorderColor = mBorderColor;
        if (mBorderPaint != null) {
            mBorderPaint.setColor(mBorderColor);
        }
    }

    public void setBreatheDuration(int mBreatheDuration) {
        this.mBreatheDuration = mBreatheDuration;
    }

    public void setBreathe(boolean breathe) {
        isBreathe = breathe;
    }

    private float pt2px(float value) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PT, value, getResources().getDisplayMetrics()) + 0.5f;
    }
}
