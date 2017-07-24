package com.lzc.myapplication.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.lzc.myapplication.R;
import com.lzc.myapplication.tool.LogUtil;

/**
 * Created by Administrator on 2017/7/24 0024.
 * $(EMAIL)
 */

public class ProgressView extends View {
    private float[] pos;
    private float[] tan;
    private int currentLength = 0;
    private Paint mPaint;
    private int mWidth, mHeight;
    private Matrix matrix;//图片控制
    private Bitmap mBitmap;

    public ProgressView(Context context) {
        this(context, null);
    }

    public ProgressView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();

    }

    private void init() {

        BitmapFactory.Options options=new BitmapFactory.Options();
        options.inSampleSize=4;
        mBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.arrow,options);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.BLUE);
        mPaint.setStrokeWidth(10);
        mPaint.setStyle(Paint.Style.STROKE);

        matrix = new Matrix();

        pos = new float[2];
        tan = new float[2];
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;
        LogUtil.d("onSizeChanged");
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.translate(mWidth / 2, mHeight / 2);
        Path path = new Path();
        path.addCircle(0, 0, 300, Path.Direction.CW);
        PathMeasure pathMeasure = new PathMeasure(path, false);

        float length = pathMeasure.getLength();
        currentLength += 2;
        if (currentLength >= length) {
            currentLength = 0;
        }
        LogUtil.d("degree");
        boolean posTan = pathMeasure.getPosTan(currentLength, pos, tan);
        if (posTan) {
            matrix.reset();
            double degree = Math.atan2(tan[1], tan[0]) * 180 / Math.PI;//计算角度

//            Log.d("degree ",""+degree+" x: "+pos[0]+" y: "+pos[1]);
            matrix.postRotate((float) degree, mBitmap.getWidth() / 2, mBitmap.getHeight() / 2);//按照中点旋转
            matrix.postTranslate(pos[0] - mBitmap.getWidth() / 2, pos[1] - mBitmap.getHeight() / 2);

            canvas.drawPath(path, mPaint);
            canvas.drawBitmap(mBitmap, matrix, mPaint);
            invalidate();
        }
    }
}
