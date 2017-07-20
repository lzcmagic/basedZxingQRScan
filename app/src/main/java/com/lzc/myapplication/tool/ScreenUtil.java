package com.lzc.myapplication.tool;

import android.content.Context;
import android.util.TypedValue;

/**
 * Created by lzc on 2017/5/27 0027.
 */

public class ScreenUtil {
    public static int dp2px(Context context, int dpValue) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, context.getResources().getDisplayMetrics());
    }

    public static int sp2dp(Context context, int value) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, value, context.getResources().getDisplayMetrics());
    }

    public static int px2dip(Context context,float pxValue) {

        final float scale = context.getResources().getDisplayMetrics().density;

        return (int) (pxValue / scale +0.5f);

    }
}
