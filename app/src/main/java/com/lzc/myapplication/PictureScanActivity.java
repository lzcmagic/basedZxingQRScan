package com.lzc.myapplication;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * Created by Administrator on 2017/7/21 0021.
 * $(EMAIL)
 */

public class PictureScanActivity extends AppCompatActivity {
    TextView textView;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        textView=new TextView(this);
//        ViewGroup.LayoutParams layoutParams = textView.getLayoutParams();
//        layoutParams.height= 800;
//        layoutParams.width=600;
//        textView.setLayoutParams(layoutParams);
        textView.setText("scantext");
        setContentView(textView);
    }

    @Override
    protected void onStart() {
        super.onStart();
        new MyAsyncTask().execute("http://www.jcodecraeer.com/a/chengxusheji/java/2012/0610/240.html");
    }

    private class MyAsyncTask extends AsyncTask<String,Integer,List<String>>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected List<String> doInBackground(String... params) {
            getWebResource(params[0]);
            return null;
        }

        @Override
        protected void onPostExecute(List<String> strings) {
            super.onPostExecute(strings);
        }
    }

    private void getWebResource(String string){
        try {
            URL url=new URL(string);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            BufferedInputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
            ByteArrayOutputStream baos=new ByteArrayOutputStream();
            BufferedOutputStream bos=new BufferedOutputStream(baos);
            StringBuffer sb=new StringBuffer();
            byte[] buffer=new byte[1024];
            int length=-1;
            int totalSize=0;
            while ((length=inputStream.read(buffer,0,buffer.length)) !=-1){
                totalSize+=length;
                bos.write(buffer,0,length);
            }
                sb.append(new String(baos.toByteArray(),"utf-8"));
            Log.d("scan","scan: "+totalSize+"\n"+"sb: "+sb.toString());
            inputStream.close();
            urlConnection.disconnect();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {

        }

    }
}
