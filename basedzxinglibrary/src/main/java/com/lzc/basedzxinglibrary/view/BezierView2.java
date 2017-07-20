package com.lzc.basedzxinglibrary.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.lzc.basedzxinglibrary.tool.LogUtil;


/**
 * Created by Administrator on 2017/7/20 0020.
 * $(EMAIL)
 */

public class BezierView2 extends View {
    private int mWidth;
    private int mHeight;

    private PointF startPoint, endPoint, controlPoint1, controlPoint2;//起始 终点 控制点
    private Paint mPaint;
    private Paint mPaint2;
    private Paint mPaint3;
    public static final int POINT_ONE = 1;
    public static final int POINT_TWO = 2;
    private int witchPoint = 1;//默认为1

    public BezierView2(Context context) {
        this(context, null);
    }

    public BezierView2(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        //初始化曲线画笔
        mPaint = new Paint();
        mPaint.setColor(Color.RED);
        mPaint.setStyle(Paint.Style.STROKE);

        //初始化辅助线画笔

        mPaint2 = new Paint();
        mPaint2.setColor(Color.DKGRAY);
        mPaint2.setStyle(Paint.Style.STROKE);
        mPaint2.setStrokeWidth(4);

        //初始化 点
        mPaint3 = new Paint();
        mPaint3.setColor(Color.GREEN);
        mPaint3.setAntiAlias(true);
        mPaint3.setStyle(Paint.Style.STROKE);
        mPaint3.setStrokeWidth(10);

        //初始化各个点
        startPoint = new PointF();
        endPoint = new PointF();
        controlPoint1 = new PointF();
        controlPoint2 = new PointF();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;

        startPoint.set(mWidth / 2 - 200, mHeight / 2);
        endPoint.set(mWidth / 2 + 200, mHeight / 2);
        controlPoint1.set(mWidth / 2 - 200, mHeight / 2 + 200);
        controlPoint2.set(mWidth / 2 + 200, mHeight / 2 + 200);
    }

    public void setPoint(int index){
        this.witchPoint=index;
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        LogUtil.d("ex: " + event.getRawX() + " ey: " + event.getRawY() + "\n" + event.getX() + " " + event.getY());
        if (witchPoint == POINT_ONE) {
            controlPoint1.set(event.getX(), event.getY());
        } else if (witchPoint == POINT_TWO) {
            controlPoint2.set(event.getX(), event.getY());
        } else {
            controlPoint1.set(event.getX(), event.getY());
        }
        invalidate();
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
//        canvas.translate(mWidth/2,mHeight/2);
        canvas.drawPoint(startPoint.x, startPoint.y, mPaint3);
        canvas.drawPoint(endPoint.x, endPoint.y, mPaint3);
        canvas.drawPoint(controlPoint1.x, controlPoint1.y, mPaint3);
        canvas.drawPoint(controlPoint2.x, controlPoint2.y, mPaint3);

        canvas.drawLine(startPoint.x, startPoint.y, controlPoint1.x, controlPoint1.y, mPaint2);
        canvas.drawLine(controlPoint1.x, controlPoint1.y, controlPoint2.x, controlPoint2.y, mPaint2);
        canvas.drawLine(endPoint.x, endPoint.y, controlPoint2.x, controlPoint2.y, mPaint2);

        Path path = new Path();
        path.moveTo(startPoint.x, startPoint.y);
        path.cubicTo(controlPoint1.x,controlPoint1.y,controlPoint2.x,controlPoint2.y,
                endPoint.x,endPoint.y);

        canvas.drawPath(path, mPaint);
    }
}
