package com.szakal.customseekbar;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import android.os.Handler;



public class CustomSB extends View {


    private Paint mPaint;
    private Rect mShape;
    private GradientDrawable mDrawableShape;
    private int mScreenHeight, mScreenWidth;
    private Path mPath, mPathRect;
    private PathMeasure mPathMeasure;
    private float mPathLength;
    private Matrix mMatrix;
    private float mStep;   //distance each step
    private float mDistance;  //distance moved
    private float[] pos, tan;

    private Bitmap mBitmap;
    private int mBitmapHeight, mBitmapWidth;
    private int mBitmapX, mBitmapY;
    private int prevX;
    private boolean isMovable = false;

    private Handler handler;
    private long mTime;
    private boolean isRunning;


    private int timeLabelColor;
    private int timeTextSize;


    public CustomSB(Context context) {

        super(context);
        init();
    }

    public CustomSB(Context context, AttributeSet attrs) {

        super(context, attrs);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CustomSB, 0, 0);

        try {
            timeLabelColor = typedArray.getColor(R.styleable.CustomSB_timeLabelColor, Color.BLACK);
            timeTextSize = typedArray.getInteger(R.styleable.CustomSB_timeTextSize, 50);
        } finally {
            typedArray.recycle();
        }
        init();
    }

    private void init() {

        mPaint = new Paint();

        mDrawableShape = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, new int[] { 0xFF0000FF, 0xFF00FF00, 0xFFFF0000 });
        mDrawableShape.setShape(GradientDrawable.RECTANGLE);

        mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.thumb);
        mBitmapHeight = mBitmap.getHeight();
        mBitmapWidth = mBitmap.getWidth();


        mStep = 10;
        mDistance = 0;
        pos = new float[2];
        tan = new float[2];
        mMatrix = new Matrix();
        mPath = new Path();

        mPathRect = new Path();
        handler = new Handler();
        mTime = 0;
        isRunning = false;






    }

    @Override
    protected void onSizeChanged(int xNew, int yNew, int xOld, int yOld){
        super.onSizeChanged(xNew, yNew, xOld, yOld);

        mShape = new Rect(mScreenWidth/10, (int)(mScreenHeight * 3/8),(int)(mScreenWidth * 0.9)  ,(int)(mScreenHeight * 5/8));
        mBitmapX = mShape.left;
        mBitmapY = mShape.top - mBitmapHeight;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        mScreenWidth = MeasureSpec.getSize(widthMeasureSpec);
        mScreenHeight = MeasureSpec.getSize(heightMeasureSpec);

        this.setMeasuredDimension(mScreenWidth, mScreenHeight);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {

        int positionX = (int) motionEvent.getX();
        int positionY = (int) motionEvent.getY();
        mDistance = 0;

        switch (motionEvent.getAction()) {

            case MotionEvent.ACTION_DOWN: {

                if ((positionX >= mBitmapX) && (positionX <= (mBitmapX + mBitmapWidth)) && (positionY >= mBitmapY) && (positionY <= (mBitmapY + mBitmapHeight)))  {

                    Log.e(getRootView().toString(), "");

                    isMovable = true;
                    prevX = positionX;
                }
                break;
            }

            case  MotionEvent.ACTION_MOVE: {

                if (isMovable) {

                    positionX = (int)motionEvent.getX();
                    int deltaX = positionX - prevX;

                    if ((deltaX != 0) && (positionX >= (mShape.left + mBitmapWidth)) && (positionX <= mShape.right) ) {

                        mBitmapX = positionX - mBitmapWidth/2;

                        if (!isRunning) {
                            isRunning = true;
                            handler.post(runnable);
                            mTime = 0;
                        }
                        else {
                            mTime = 0;
                        }
                    }
                }
                break;
            }

            case MotionEvent.ACTION_UP: {

                isMovable = false;

                int leftDifference = Math.abs(mBitmapX + mBitmapWidth/2 - mShape.left);
                int rightDifference = Math.abs(mBitmapX + mBitmapWidth/2 - mShape.right);
                int centerDifference = Math.abs(mBitmapX + mBitmapWidth/2 - mShape.centerX());
                int minArray[] = new int[] { leftDifference, centerDifference, rightDifference };

                int minIndex = 0;
                int min = minArray[0];

                for (int i = 1; i < 3; i++) {

                    if (minArray[i] < min) {

                        minIndex = i;
                        min = minArray[i];
                    }
                }
                mPath.reset();
                mPath.moveTo(mBitmapX + mBitmapWidth, mBitmapY + mBitmapHeight);

                switch (minIndex) {

                    case(0): {
                        mPath.lineTo(mShape.left + mBitmapWidth, mShape.top);
                        break;
                    }

                    case(1): {
                        mPath.lineTo(mShape.centerX() - mBitmapWidth, mShape.top);
                        break;
                    }

                    case(2): {
                        mPath.lineTo(mShape.right - mBitmapWidth, mShape.top);
                        break;
                    }
                }
                break;
            }
        }
        mPath.close();
        mPathMeasure = new PathMeasure(mPath, false);
        mPathLength = mPathMeasure.getLength()/2;
        mStep = 1;
        mDistance = 0;
        invalidate();
        return true;
    }


    @Override
    protected void onDraw(Canvas canvas) {

        mDrawableShape.setBounds(mShape);

        float r = 10;
        mDrawableShape.setCornerRadius(r);
        mDrawableShape.setGradientType(GradientDrawable.LINEAR_GRADIENT);
        mDrawableShape.draw(canvas);

        drawProgressText("0%", canvas, mShape.left + mShape.left - mPaint.measureText("0%") / 2, mShape.bottom + mShape.left / 2);
        drawProgressText("50%", canvas, mShape.exactCenterX() - mPaint.measureText("50%") / 2, mShape.bottom + mShape.left / 2);
        drawProgressText("100%", canvas, mShape.right - mPaint.measureText("100%") / 2, mShape.bottom + mShape.left / 2);


        if (mDistance < mPathLength) {

            mMatrix.reset();
            mPathMeasure.getPosTan(mDistance, pos, tan);
            mMatrix.postTranslate(pos[0] - mBitmapWidth, pos[1] - mBitmapHeight);
            canvas.drawBitmap(mBitmap, mMatrix, mPaint);
            mBitmapX = (int)pos[0] - mBitmapWidth;
            drawProgressBar(canvas);
            drawTimeText(mTime + " s.", canvas, mShape);
            mDistance += mStep;
            mPath.reset();
            invalidate();
        } else {
            canvas.drawBitmap(mBitmap, mBitmapX, mBitmapY, mPaint);
            drawProgressBar(canvas);
            drawTimeText(mTime + " s.", canvas, mShape);
        }
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {

            invalidate();
            mTime++;
            postDelayed(runnable, 1000);
        }
    };


    private void drawProgressBar(Canvas canvas) {

        mPathRect.reset();
        //1
        mPathRect.moveTo(mShape.left, mShape.top);
        //2
        mPathRect.lineTo(mBitmapX + mShape.left, mShape.top);
        //3
        mPathRect.lineTo(mBitmapX + mShape.left * 2,  mShape.top + (mShape.bottom - mShape.top) / 2);
        //4
        mPathRect.lineTo(mBitmapX + mShape.left,  mShape.bottom);
        //5
        mPathRect.lineTo(mShape.left,  mShape.bottom);
        //1
        mPathRect.moveTo(mShape.left, mShape.top);
        mPathRect.close();
        mPaint.setColor(Color.CYAN);

        canvas.drawPath(mPathRect, mPaint);
    }

    private void drawProgressText(String text, Canvas canvas, float x, float y) {

        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setTextSize(20);
        mPaint.setColor(Color.BLACK);
        canvas.drawText(text, x, y, mPaint);
    }

    private void drawTimeText(String text, Canvas canvas, Rect r) {

        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setTextSize(timeTextSize);
        mPaint.setColor(timeLabelColor);
        float width = mPaint.measureText(text);
        float textSize = mPaint.getTextSize();
        //canvas.drawText(text, r.exactCenterX() - (width/2), r.exactCenterY()+ textSize/3, mPaint);
        canvas.drawText(text, mScreenWidth / 2 - (width / 2), mScreenHeight / 4, mPaint);
    }

    public void setTimeLabelColor (int color) {

        timeLabelColor = color;
        invalidate();
    }

    public void setTimeTextSize (int size) {

        timeTextSize = size;
        invalidate();
        requestLayout();
    }


}
