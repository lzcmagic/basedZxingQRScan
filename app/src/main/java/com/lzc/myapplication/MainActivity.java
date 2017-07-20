package com.lzc.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.lzc.basedzxinglibrary.CaptureActivity;
import com.lzc.myapplication.view.BezierView2;

public class MainActivity extends AppCompatActivity {
    BezierView2 bezierView2;
    public static final int SCAN_CODE = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        bezierView2 = (BezierView2) findViewById(R.id.bezier2);

    }

    public void switchPoint(View v){
        if (v.getId()==R.id.point1){
            bezierView2.setPoint(BezierView2.POINT_ONE);
        }else if (v.getId()==R.id.point2){
            bezierView2.setPoint(BezierView2.POINT_TWO);
        }
    }

    public void scan(View view){
        Intent intent = new Intent(MainActivity.this, CaptureActivity.class);
        startActivityForResult(intent, SCAN_CODE);
    }
}
