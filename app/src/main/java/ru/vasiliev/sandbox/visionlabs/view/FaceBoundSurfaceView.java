package ru.vasiliev.sandbox.visionlabs.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.ColorInt;
import android.util.AttributeSet;
import android.view.SurfaceView;

public class FaceBoundSurfaceView extends SurfaceView {

    private Paint mPaint = null;

    private RectF faceRect = new RectF();

    private int rectColor = Color.GREEN;

    public FaceBoundSurfaceView(Context context) {
        this(context, null);
    }

    public FaceBoundSurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FaceBoundSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setWillNotDraw(false);

        configurePaint();
    }

    public void setFaceRect(int left, int top, int right, int bottom) {
        faceRect.set(left, top, right, bottom);
    }

    public void setFaceRectColor(@ColorInt int color) {
        rectColor = color;
    }

    @Override
    protected void onDraw(Canvas canvas) {

        super.onDraw(canvas);

        if (faceRect == null) {
            return;
        }

        mPaint.setColor(rectColor);

        canvas.drawRoundRect(faceRect, 20, 20, mPaint);
    }

    protected void configurePaint() {
        mPaint = new Paint();

        mPaint.setStyle(Paint.Style.FILL);

        mPaint.setStrokeWidth(5);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setAntiAlias(true);
    }
}
