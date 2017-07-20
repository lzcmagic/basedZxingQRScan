package com.lzc.myapplication.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.lzc.myapplication.R;
import com.lzc.myapplication.tool.LogUtil;

/**
 * Created by Administrator on 2017/7/18 0018.
 * $(EMAIL)
 */

public class BitmapView extends View {

    private int mWidth;
    private int mHeight;
    private Bitmap mBitmap;
    private int animDuration = 1000;//anim time
    private int totalPage = 13;
    private int currentPage = 0;
    private Paint mPaint;
    private int pageSizeWidth;
    private int pageSizeHeight;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int what = msg.what;
            currentPage = what;
            postInvalidate();
        }
    };

    public BitmapView(Context context) {
        this(context, null);
    }

    public BitmapView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();

    }

    private void init() {
        mBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.checkmark);
        pageSizeWidth = mBitmap.getWidth() / totalPage;
        pageSizeHeight = mBitmap.getHeight();
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.YELLOW);
        mPaint.setStyle(Paint.Style.FILL);
        new MyThread(true).start();

    }

    private class MyThread extends Thread{
        private boolean IsStop=true;
        public MyThread(boolean isStop) {
            this.IsStop=isStop;
        }

        @Override
        public void run() {
            super.run();
            if (IsStop){
                for (int i = 0; i < totalPage; i++) {
                    mHandler.sendEmptyMessage(i);
                    try {
                        Thread.sleep(animDuration / totalPage);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction()==MotionEvent.ACTION_DOWN){
            currentPage=0;
            new MyThread(true).start();
        }
        return true;
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        LogUtil.d("aaaaa");
        mWidth = w;
        mHeight = h;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        LogUtil.d("aaaaa");
        canvas.translate(mWidth / 2, mHeight / 2);
        canvas.drawCircle(0, 0, 240, mPaint);
        Rect src = new Rect(pageSizeWidth * currentPage, 0, pageSizeWidth * (currentPage + 1), pageSizeHeight);
        Rect dst = new Rect(-200, -200, 200, 200);
        canvas.drawBitmap(mBitmap, src, dst, new Paint());
    }
}
