package com.lzc.basedzxinglibrary;

/**
 * Created by Administrator on 2017/7/25 0025.
 * 844497109@qq.com
 */

public class SpUtil {


    private static SpUtil mInstance;
    private SpUtil(){

    }
    public static SpUtil getInstance(){
        if (mInstance==null){
            synchronized (SpUtil.class){
                if (mInstance==null){
                    mInstance=new SpUtil();
                }
            }
        }
        return mInstance;
    }

    public static void getLightMode(){

    }


}
