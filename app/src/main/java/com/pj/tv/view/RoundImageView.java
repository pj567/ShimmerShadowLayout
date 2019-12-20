package com.pj.tv.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.pj.tv.R;

import static android.graphics.Canvas.ALL_SAVE_FLAG;

/**
 * user by pj567
 * date on 2019/12/19.
 */

public class RoundImageView extends ImageView {
    private float mTopLeftRadius;
    private float mTopRightRadius;
    private float mBottomLeftRadius;
    private float mBottomRightRadius;

    private boolean mIsDrawRound;
    private Path mRoundPath;
    private Paint mRoundPaint;
    private RectF mRoundRectF;

    private boolean mIsDrawn;

    public RoundImageView(Context context) {
        this(context, null);
    }

    public RoundImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RoundImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RoundImageView);
            float radius = a.getDimension(R.styleable.RoundImageView_radius, 0);
            mTopLeftRadius = a.getDimension(R.styleable.RoundImageView_topLeftRadius, radius);
            mTopRightRadius = a.getDimension(R.styleable.RoundImageView_topRightRadius, radius);
            mBottomLeftRadius = a.getDimension(R.styleable.RoundImageView_bottomLeftRadius, radius);
            mBottomRightRadius = a.getDimension(R.styleable.RoundImageView_bottomRightRadius, radius);
            a.recycle();
        }
        mIsDrawRound = mTopLeftRadius != 0 || mTopRightRadius != 0 || mBottomLeftRadius != 0 || mBottomRightRadius != 0;

        mRoundPaint = new Paint();
        mRoundPaint.setAntiAlias(true);
        // 取两层绘制交集。显示下层。
        mRoundPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));

    }

    @Override
    public boolean isInEditMode() {
        return true;
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldw, int oldh) {
        super.onSizeChanged(width, height, oldw, oldh);
        if ((width != oldw || height != oldh) && mIsDrawRound) {
            final Path path = new Path();
            mRoundRectF = new RectF(getPaddingLeft(), getPaddingTop(), width - getPaddingRight(), height - getPaddingBottom());
            final float[] radius = new float[]{
                    mTopLeftRadius, mTopLeftRadius,
                    mTopRightRadius, mTopRightRadius,
                    mBottomRightRadius, mBottomRightRadius,
                    mBottomLeftRadius, mBottomLeftRadius};
            path.addRoundRect(mRoundRectF, radius, Path.Direction.CW);
            mRoundPath = path;
        }
    }

    @Override
    public void draw(Canvas canvas) {
        if (!mIsDrawRound) {
            super.draw(canvas);
        } else {
            mIsDrawn = true;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                canvas.saveLayer(mRoundRectF, null);
            } else {
                canvas.saveLayer(mRoundRectF, null, ALL_SAVE_FLAG);
            }
            super.draw(canvas);
            canvas.drawPath(mRoundPath, mRoundPaint);
            canvas.restore();
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        if (mIsDrawn || !mIsDrawRound) {
            super.dispatchDraw(canvas);
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                canvas.saveLayer(mRoundRectF, null);
            } else {
                canvas.saveLayer(mRoundRectF, null, ALL_SAVE_FLAG);
            }
            super.dispatchDraw(canvas);
            canvas.drawPath(mRoundPath, mRoundPaint);
            canvas.restore();
        }
    }
}
