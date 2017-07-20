package com.lzc.myapplication;

import android.app.Application;
import android.content.Context;

/**
 * Created by Administrator on 2017/7/19 0019.
 * $(EMAIL)
 */

public class MyApplication extends Application {

    private static Context context;
    @Override
    public void onCreate() {
        super.onCreate();
        context=this;
    }

    public static Context getAppContext(){
        return context;
    }
}
