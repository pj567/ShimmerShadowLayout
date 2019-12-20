package com.pj.tv.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.Build;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.BounceInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;

import com.pj.tv.R;

import java.util.ArrayList;
import java.util.List;

import static android.graphics.Canvas.ALL_SAVE_FLAG;

/**
 * 结合compile 'me.jessyan:autosize:1.1.2'适配使用
 *
 * @author acer
 */
public class ShimmerShadowLayout extends FrameLayout implements View.OnFocusChangeListener {
    protected long mAnimDuration = 300;
    protected long mShimmerDelay = mAnimDuration + 100;
    private int mShimmerColor = 0x66FFFFFF;
    private boolean mIsShimmerAnim = true;
    private boolean mIsBounceInterpolator = true;
    private boolean mBringToFront = true;
    private boolean mIsParent = false;
    private float mScale = 1.05f;
    private LinearGradient mShimmerLinearGradient;
    private Matrix mShimmerGradientMatrix;
    private Paint mShimmerPaint;
    private Path mShimmerPath;
    protected RectF mFrameRectF;
    private float mShimmerTranslate = 0;
    private boolean mShimmerAnimating = false;
    private ViewTreeObserver.OnPreDrawListener startAnimationPreDrawListener;
    private AnimatorSet mAnimatorSet;
    private BreatheShadowView shadowView;
    private float mRadius = 0;
    private float mShadowWidth = pt2px(10);
    private float mBorderWidth = pt2px(10);
    private int mBorderColor = Color.WHITE;
    private int mShadowColor = Color.WHITE;
    private int mBreatheDuration = 4000;
    private boolean mIsBreathe = true;
    private boolean mIsShadow = true;
    private boolean mIsBorder = false;

    private float mTopLeftRadius;
    private float mTopRightRadius;
    private float mBottomLeftRadius;
    private float mBottomRightRadius;

    private boolean mIsDrawRound;
    private RectF mRefreshRectF;

    private boolean mIsDrawn;

    public ShimmerShadowLayout(Context context) {
        this(context, null);
    }

    public ShimmerShadowLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ShimmerShadowLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        setWillNotDraw(false);
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ShimmerShadowLayout, 0, 0);
        try {
            mAnimDuration = a.getInteger(R.styleable.ShimmerShadowLayout_mAnimDuration, 300);
            mShimmerColor = a.getColor(R.styleable.ShimmerShadowLayout_mShimmerColor, 0x66FFFFFF);
            mIsShimmerAnim = a.getBoolean(R.styleable.ShimmerShadowLayout_mIsShimmerAnim, true);
            mBringToFront = a.getBoolean(R.styleable.ShimmerShadowLayout_mBringToFront, false);
            mIsBounceInterpolator = a.getBoolean(R.styleable.ShimmerShadowLayout_mIsBounceInterpolator, true);
            mIsParent = a.getBoolean(R.styleable.ShimmerShadowLayout_mIsParent, false);
            mScale = a.getFloat(R.styleable.ShimmerShadowLayout_mScale, 1.05f);
            mShimmerDelay = mAnimDuration + 100;

            mShadowWidth = a.getDimension(R.styleable.ShimmerShadowLayout_mShadowWidth, pt2px(10));
            mBorderWidth = a.getDimension(R.styleable.ShimmerShadowLayout_mBorderWidth, pt2px(2));
            mShadowColor = a.getColor(R.styleable.ShimmerShadowLayout_mShadowColor, Color.WHITE);
            mBorderColor = a.getColor(R.styleable.ShimmerShadowLayout_mBorderColor, Color.WHITE);
            mBreatheDuration = a.getInteger(R.styleable.ShimmerShadowLayout_mBreatheDuration, 4000);
            mIsBreathe = a.getBoolean(R.styleable.ShimmerShadowLayout_mIsBreathe, true);
            mIsShadow = a.getBoolean(R.styleable.ShimmerShadowLayout_mIsShadow, true);
            mIsBorder = a.getBoolean(R.styleable.ShimmerShadowLayout_mIsBorder, true);

            mRadius = a.getDimension(R.styleable.ShimmerShadowLayout_mRadius, 0);
            mTopLeftRadius = a.getDimension(R.styleable.ShimmerShadowLayout_mTopLeftRadius, mRadius);
            mTopRightRadius = a.getDimension(R.styleable.ShimmerShadowLayout_mTopRightRadius, mRadius);
            mBottomLeftRadius = a.getDimension(R.styleable.ShimmerShadowLayout_mBottomLeftRadius, mRadius);
            mBottomRightRadius = a.getDimension(R.styleable.ShimmerShadowLayout_mBottomRightRadius, mRadius);
        } finally {
            a.recycle();
        }
        if (!mIsParent) {
            setOnFocusChangeListener(this);
        }
        //关闭硬件加速
//        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        mShimmerPaint = new Paint();
        mShimmerPath = new Path();
        mShimmerGradientMatrix = new Matrix();
        mFrameRectF = new RectF();
        mIsDrawRound = mTopLeftRadius != 0 || mTopRightRadius != 0 || mBottomLeftRadius != 0 || mBottomRightRadius != 0;

    }

    @Override
    public boolean isInEditMode() {
        return true;
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldw, int oldh) {
        super.onSizeChanged(width, height, oldw, oldh);
        if (shadowView == null) {
            shadowView = new BreatheShadowView(getContext());
            shadowView.setShadowWidth(mShadowWidth);
            shadowView.setBorderWidth(mBorderWidth);
            shadowView.setShadowColor(mShadowColor);
            shadowView.setBorderColor(mBorderColor);
            shadowView.setBreatheDuration(mBreatheDuration);
            shadowView.setBreathe(mIsBreathe);
            shadowView.setBorder(mIsBorder);
            shadowView.setShadow(mIsShadow);
            shadowView.setStartDelay(mShimmerDelay);
            shadowView.setTopLeftRadius(mTopLeftRadius);
            shadowView.setTopRightRadius(mTopRightRadius);
            shadowView.setBottomLeftRadius(mBottomLeftRadius);
            shadowView.setBottomRightRadius(mBottomRightRadius);
            LayoutParams layoutParams = new LayoutParams(width, height);
            addView(shadowView, layoutParams);
        }
        mFrameRectF.set(getPaddingLeft() + mShadowWidth + mBorderWidth / 2, getPaddingTop() + mShadowWidth + mBorderWidth / 2,
                width - getPaddingRight() - mShadowWidth - mBorderWidth / 2, height - getPaddingBottom() - mShadowWidth - mBorderWidth / 2);
        Path path = new Path();
        final float[] shimmerRadius = new float[]{
                mTopLeftRadius, mTopLeftRadius,
                mTopRightRadius, mTopRightRadius,
                mBottomRightRadius, mBottomRightRadius,
                mBottomLeftRadius, mBottomLeftRadius};
        if (mTopLeftRadius != 0 || mTopRightRadius != 0 || mBottomLeftRadius != 0 || mBottomRightRadius != 0) {
            path.addRoundRect(mFrameRectF, shimmerRadius, Path.Direction.CW);
        } else {
            path.addRoundRect(mFrameRectF, 0, 0, Path.Direction.CW);
        }
        mShimmerPath = path;
        if ((height != oldw || height != oldh) && mIsDrawRound) {
            mRefreshRectF = new RectF(getPaddingLeft(), getPaddingTop(), width - getPaddingRight(), height - getPaddingBottom());
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        if (mIsDrawn || !mIsDrawRound) {
            super.dispatchDraw(canvas);
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                canvas.saveLayer(mRefreshRectF, null);
            } else {
                canvas.saveLayer(mRefreshRectF, null, ALL_SAVE_FLAG);
            }
            super.dispatchDraw(canvas);
            canvas.restore();
        }
        onDrawShimmer(canvas);
    }

    @Override
    public void draw(Canvas canvas) {
        if (!mIsDrawRound) {
            super.draw(canvas);
        } else {
            mIsDrawn = true;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                canvas.saveLayer(mRefreshRectF, null);
            } else {
                canvas.saveLayer(mRefreshRectF, null, ALL_SAVE_FLAG);
            }
            super.draw(canvas);
            canvas.restore();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        stopAnimation();
        super.onDetachedFromWindow();
    }

    /**
     * 绘制闪光
     *
     * @param canvas
     */
    protected void onDrawShimmer(Canvas canvas) {
        if (mShimmerAnimating) {
            canvas.save();
            float shimmerTranslateX = mFrameRectF.width() * mShimmerTranslate;
            float shimmerTranslateY = mFrameRectF.height() * mShimmerTranslate;
            mShimmerGradientMatrix.setTranslate(shimmerTranslateX, shimmerTranslateY);
            mShimmerLinearGradient.setLocalMatrix(mShimmerGradientMatrix);
            canvas.drawPath(mShimmerPath, mShimmerPaint);
            canvas.restore();
        }
    }

    private void setShimmerAnimating(boolean shimmerAnimating) {
        mShimmerAnimating = shimmerAnimating;
        if (mShimmerAnimating) {
            mShimmerLinearGradient = new LinearGradient(
                    0, 0, mFrameRectF.width(), mFrameRectF.height(),
                    new int[]{0x00FFFFFF, reduceColorAlphaValueToZero(mShimmerColor), mShimmerColor, reduceColorAlphaValueToZero(mShimmerColor), 0x00FFFFFF},
                    new float[]{0f, 0.2f, 0.5f, 0.8f, 1f}, Shader.TileMode.CLAMP);
            mShimmerPaint.setShader(mShimmerLinearGradient);
        }
    }

    private int reduceColorAlphaValueToZero(int actualColor) {
        return Color.argb(0x1A, Color.red(actualColor), Color.green(actualColor), Color.blue(actualColor));
    }

    public void startAnimation() {
        if (getWidth() == 0) {
            startAnimationPreDrawListener = new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    getViewTreeObserver().removeOnPreDrawListener(this);
                    startAnimation();
                    return true;
                }
            };
            getViewTreeObserver().addOnPreDrawListener(startAnimationPreDrawListener);
            return;
        }
        if (null != mAnimatorSet) {
            mAnimatorSet.cancel();
        }
        if (shadowView != null) {
            shadowView.start();
        }
        createAnimatorSet(true);
        mAnimatorSet.start();
        setSelected(true);
    }

    public void stopAnimation() {
        if (startAnimationPreDrawListener != null) {
            getViewTreeObserver().removeOnPreDrawListener(startAnimationPreDrawListener);
        }
        if (null != mAnimatorSet) {
            mAnimatorSet.cancel();
        }
        createAnimatorSet(false);
        if (shadowView != null) {
            shadowView.stop();
        }
        mAnimatorSet.start();
        setSelected(false);
    }

    private void createAnimatorSet(boolean isStart) {
        final List<Animator> together = new ArrayList<>();
        if (isStart) {
            together.add(getScaleXAnimator(mScale));
            together.add(getScaleYAnimator(mScale));
        } else {
            together.add(getScaleXAnimator(1.0f));
            together.add(getScaleYAnimator(1.0f));
        }
        final List<Animator> sequentially = new ArrayList<>();
        if (mIsShimmerAnim && isStart) {
            sequentially.add(getShimmerAnimator());
        }
        mAnimatorSet = new AnimatorSet();
        mAnimatorSet.playTogether(together);
        mAnimatorSet.playSequentially(sequentially);
    }

    private ObjectAnimator getScaleXAnimator(float scale) {
        ObjectAnimator scaleXObjectAnimator = ObjectAnimator.ofFloat(this, "scaleX", scale).setDuration(mAnimDuration);
        if (mIsBounceInterpolator) {
            scaleXObjectAnimator.setInterpolator(new BounceInterpolator());
        }
        return scaleXObjectAnimator;
    }

    private ObjectAnimator getScaleYAnimator(float scale) {
        ObjectAnimator scaleYObjectAnimator = ObjectAnimator.ofFloat(this, "scaleY", scale).setDuration(mAnimDuration);
        if (mIsBounceInterpolator) {
            scaleYObjectAnimator.setInterpolator(new BounceInterpolator());
        }
        return scaleYObjectAnimator;
    }

    private ObjectAnimator getShimmerAnimator() {
        ObjectAnimator mShimmerAnimator = ObjectAnimator.ofFloat(this, "shimmerTranslate", -1f, 1f);
        mShimmerAnimator.setInterpolator(new DecelerateInterpolator(1));
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int max = getWidth() >= getHeight() ? getWidth() : getHeight();
        int duration = max > screenWidth / 3 ? screenWidth / 3 : max;
        mShimmerAnimator.setDuration(duration * 3);
        mShimmerAnimator.setStartDelay(mShimmerDelay);
        mShimmerAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                setShimmerAnimating(true);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                setShimmerAnimating(false);
            }
        });
        return mShimmerAnimator;
    }


    protected void setShimmerTranslate(float shimmerTranslate) {
        if (mIsShimmerAnim && mShimmerTranslate != shimmerTranslate) {
            mShimmerTranslate = shimmerTranslate;
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    protected float getShimmerTranslate() {
        return mShimmerTranslate;
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
            if (mBringToFront) {
                v.bringToFront();
            }
            v.setSelected(true);
            startAnimation();
        } else {
            v.setSelected(false);
            stopAnimation();
        }
    }

    private float pt2px(float value) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PT, value, getResources().getDisplayMetrics()) + 0.5f;
    }
}
