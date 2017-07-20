package com.lzc.basedzxinglibrary.tool;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.lzc.basedzxinglibrary.BuildConfig;


/**
 * 日志管理类
 * 方便打包时忘记去除Log日志
 * Created by Administrator on 2017/6/2 0002.
 */

public class LogUtil {
    private static final String DEBUG_TAG="leqi";
    private static Toast mToast;
    public static void v(String log){
        if (BuildConfig.DEBUG){
            Log.v(DEBUG_TAG,log);
        }
    }

    public static void d(String log){
        if (BuildConfig.DEBUG){
            Log.d(DEBUG_TAG,log);
        }
    }

    public static void i(String log){
        if (BuildConfig.DEBUG){
            Log.i(DEBUG_TAG,log);
        }
    }

    public static void w(String log){
        if (BuildConfig.DEBUG){
            Log.w(DEBUG_TAG,log);
        }
    }

    public static void e(String log){
        if (BuildConfig.DEBUG){
            Log.e(DEBUG_TAG,log);
        }
    }

    public static void toast(Context context,String text,int duration){
        if (mToast==null){
            mToast=Toast.makeText(context,text,duration);
        }else{
            mToast.setDuration(duration);
            mToast.setText(text);
        }
        mToast.show();
    }

    public static void toast(Context context,int resId,int duration){
        if (mToast==null){
            mToast=Toast.makeText(context,resId,duration);
        }else{
            mToast.setDuration(duration);
            mToast.setText(resId);
        }
        mToast.show();
    }

    /**
     * 吐司 默认时长
     * <p>
     * {@link Toast#LENGTH_SHORT}
     * @param text
     */
    public static void toast(Context context,String text){
        toast(context,text,Toast.LENGTH_SHORT);
    }

    /**
     *  吐司 默认时长
     *  <p>
     * {@link Toast#LENGTH_SHORT}
     * @param resId
     */
    public static void toast(Context context,int resId){
        toast(context,resId,Toast.LENGTH_SHORT);
    }
}
