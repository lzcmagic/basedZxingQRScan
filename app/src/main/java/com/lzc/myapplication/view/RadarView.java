package com.lzc.myapplication.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.lzc.myapplication.tool.LogUtil;
import com.lzc.myapplication.tool.ScreenUtil;

/**
 * Created by Administrator on 2017/7/19 0019.
 * $(EMAIL)
 */

public class RadarView extends View {

    private Paint mPaint1;//外网
    private Paint mPaint2;//外网
    private Paint mPaint3;//外网
    private Paint mPaint4;//外网
    private Paint mPaint5;//外网
    private int mWidth;//宽
    private Path mPath;
    private Path mPath1;
    private int mHeight;//高
    private int mRadius;

    private float posibility1 = 50.4f;//能力1
    private float posibility2 = 70.8f;
    private float posibility3 = 90f;
    private float posibility4 = 56.4f;
    private float posibility5 = 87.4f;
    private float posibility6 = 30f;

    private Context context;

    public RadarView(Context context) {
        this(context, null);
    }

    public RadarView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context=context;
        init();
    }

    private void init() {

        mPaint1 = new Paint();
        mPaint1.setAntiAlias(true);
        mPaint1.setColor(0xFF123456);
        mPaint1.setStyle(Paint.Style.STROKE);

        mPaint2 = new Paint();
        mPaint2.setAntiAlias(true);
        mPaint2.setColor(0x8000ff00);
        mPaint2.setStyle(Paint.Style.FILL);

        mPaint3 = new Paint();
        mPaint3.setAntiAlias(true);
        mPaint3.setColor(0x8000ff00);
        mPaint3.setStyle(Paint.Style.FILL);
        mPaint3.setTextSize(ScreenUtil.dp2px(context,16));

        mPaint4 = new Paint();
        mPaint4.setAntiAlias(true);
        mPaint4.setColor(0xFF123456);
        mPaint4.setStyle(Paint.Style.STROKE);

        mPaint5 = new Paint();
        mPaint5.setAntiAlias(true);
        mPaint5.setColor(0xFF123456);
        mPaint5.setStyle(Paint.Style.STROKE);

        //
        mPath = new Path();

        mPath1 = new Path();
        mRadius = 200;

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;
    }

    int totalLine = 5;

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.translate(mWidth / 2, mHeight / 2);

        canvas.scale(1, -1);//y轴
        canvas.drawCircle(0, 0, mRadius, mPaint1);
        int eachWidth = mRadius / totalLine;

        LogUtil.d("eachWidth: " + eachWidth);
        for (int i = 1; i < totalLine + 1; i++) {
            mPath.reset();
            mPath.moveTo((float) (eachWidth * i * Math.cos(Math.PI / 3)),
                    (float) (eachWidth * i * Math.sin(Math.PI / 3)));
            mPath.lineTo(-(eachWidth * i / 2), (float) (eachWidth * i * Math.sin(Math.PI / 3)));
            mPath.lineTo(-eachWidth * i, 0);
            mPath.lineTo(-(eachWidth * i / 2), -(float) (eachWidth * i * Math.sin(Math.PI / 3)));
            mPath.lineTo(eachWidth * i / 2, -(float) (eachWidth * i * Math.sin(Math.PI / 3)));
            mPath.lineTo(eachWidth * i, 0);
            mPath.close();
            canvas.drawPath(mPath, mPaint1);
        }
        canvas.drawLine(-(float) (Math.cos(Math.PI / 3) * mRadius),
                -(float) (Math.sin(Math.PI / 3) * mRadius),
                (float) (Math.cos(Math.PI / 3) * mRadius),
                (float) (Math.sin(Math.PI / 3) * mRadius),
                mPaint1);
        canvas.drawLine((float) (Math.cos(Math.PI / 3) * mRadius),
                -(float) (Math.sin(Math.PI / 3) * mRadius),
                -(float) (Math.cos(Math.PI / 3) * mRadius),
                (float) (Math.sin(Math.PI / 3) * mRadius),
                mPaint1);
        canvas.drawLine(-mRadius, 0,
                mRadius,0,
                mPaint1);

        canvas.scale(1,-1);
        canvas.drawText("打野能力",(float) (Math.cos(Math.PI / 3) * mRadius),
                (float) (Math.sin(Math.PI / 3) * mRadius+20),mPaint3);

        canvas.scale(1,-1);
        mPath.reset();
        mPath.rewind();
        mPath.moveTo((float) (Math.cos(Math.PI / 3) * (mRadius / 100.0) * posibility1),
                (float) (Math.sin(Math.PI / 3) * (mRadius / 100.0) * posibility1));
        mPath.lineTo(-(float) (Math.cos(Math.PI / 3) * (mRadius / 100.0) * posibility2),
                (float) (Math.sin(Math.PI / 3) * (mRadius / 100.0) * posibility2));
        mPath.lineTo(-(float)((mRadius / 100.0) * posibility3), 0);
        mPath.lineTo(-(float) (Math.cos(Math.PI / 3) * (mRadius / 100.0) * posibility4),
                -(float) (Math.sin(Math.PI / 3) * (mRadius / 100.0) * posibility4));
        mPath.lineTo((float) (Math.cos(Math.PI / 3) * (mRadius / 100.0) * posibility5),
                -(float) (Math.sin(Math.PI / 3) * (mRadius / 100.0) * posibility5));
        mPath.lineTo(posibility6, 0);
        mPath.close();
        canvas.drawPath(mPath, mPaint2);
    }
}
