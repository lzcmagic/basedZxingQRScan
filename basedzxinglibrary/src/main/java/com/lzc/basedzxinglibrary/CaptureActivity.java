/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lzc.basedzxinglibrary;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import com.google.zxing.client.result.ResultParser;
import com.google.zxing.common.HybridBinarizer;
import com.lzc.basedzxinglibrary.camera.CameraManager;
import com.lzc.basedzxinglibrary.decode.BitmapLuminanceSource;
import com.lzc.basedzxinglibrary.decode.DecodeFormatManager;
import com.lzc.basedzxinglibrary.sensor.BeepManager;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Vector;

/**
 * This activity opens the camera and does the actual scanning on a background thread. It draws a
 * viewfinder to help the user place the barcode correctly, shows feedback as the image processing
 * is happening, and then overlays the results when a scan is successful.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 * @author Sean Owen
 */
public final class CaptureActivity extends AppCompatActivity implements SurfaceHolder.Callback {

    private static final String TAG = CaptureActivity.class.getSimpleName();

    private CameraManager cameraManager;
    private CaptureActivityHandler handler;
    private ViewfinderView viewfinderView;
    private TextView statusView;
    private View resultView;
    private Result lastResult;
    private boolean hasSurface;
    private IntentSource source;
    private Collection<BarcodeFormat> decodeFormats;
    private String characterSet;
    private BeepManager beepManager;
    private int ScreenOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;

    ViewfinderView getViewfinderView() {
        return viewfinderView;
    }

    public Handler getHandler() {
        return handler;
    }

    public CameraManager getCameraManager() {
        return cameraManager;
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.capture);

        hasSurface = false;
        beepManager = new BeepManager(this);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
    }

    @SuppressWarnings("WrongConstant")
    @Override
    protected void onResume() {
        super.onResume();

        Log.d(TAG, "Capture-onResume");
        // historyManager must be initialized here to update the history preference

        // CameraManager must be initialized here, not in onCreate(). This is necessary because we don't
        // want to open the camera driver and measure the screen size if we're going to show the help on
        // first launch. That led to bugs where the scanning rectangle was the wrong size and partially
        // off screen.
        cameraManager = new CameraManager(getApplication());

        viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);
        viewfinderView.setCameraManager(cameraManager);

        resultView = findViewById(R.id.result_view);
        statusView = (TextView) findViewById(R.id.status_view);

        handler = null;
        lastResult = null;

        int requestedOrientation = getRequestedOrientation();
        Log.d(TAG, "requestedOrientation: " + requestedOrientation);
        resetStatusView();


        beepManager.updatePrefs();


        source = IntentSource.NONE;
        decodeFormats = null;
        characterSet = null;

        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        if (hasSurface) {
            // The activity was paused but not stopped, so the surface still exists. Therefore
            // surfaceCreated() won't be called, so init the camera here.
            initCamera(surfaceHolder);
        } else {
            // Install the callback and wait for surfaceCreated() to init the camera.
            surfaceHolder.addCallback(this);
        }
    }

    public void gallery(View v) {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        startActivityForResult(intent, 33);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 33 && RESULT_OK == resultCode) {
            Uri uri = data.getData();

            if (null == uri) {
                return;
            }
            final String scheme = uri.getScheme();
            String dataInfo = null;
            if (scheme == null)
                dataInfo = uri.getPath();
            else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
                dataInfo = uri.getPath();
            } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
                Cursor cursor = getContentResolver().query(uri, new String[]{MediaStore.Images.ImageColumns.DATA}, null, null, null);
                if (null != cursor) {
                    if (cursor.moveToFirst()) {
                        int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                        if (index > -1) {
                            dataInfo = cursor.getString(index);
                        }
                    }
                    cursor.close();
                }
            }
            Log.d("lzc", "dataInfo: " + dataInfo);
            File file = new File(dataInfo);

            Bitmap mBitmap = BitmapFactory.decodeFile(file.getPath());
            MultiFormatReader multiFormatReader = new MultiFormatReader();
            // 解码的参数
            Hashtable<DecodeHintType, Object> hints =
                    new Hashtable<DecodeHintType, Object>(2);
            // 可以解析的编码类型
            Vector<BarcodeFormat> decodeFormats = new Vector<BarcodeFormat>();
            if (decodeFormats == null || decodeFormats.isEmpty()) {
                decodeFormats = new Vector<BarcodeFormat>();

                // 这里设置可扫描的类型，我这里选择了都支持
                decodeFormats.addAll(DecodeFormatManager.ONE_D_FORMATS);
                decodeFormats.addAll(DecodeFormatManager.QR_CODE_FORMATS);
                decodeFormats.addAll(DecodeFormatManager.DATA_MATRIX_FORMATS);
            }
            hints.put(DecodeHintType.POSSIBLE_FORMATS, decodeFormats);
            // 设置继续的字符编码格式为UTF8
            // hints.put(DecodeHintType.CHARACTER_SET, "UTF8");
            // 设置解析配置参数
            multiFormatReader.setHints(hints);

            // 开始对图像资源解码
            Result rawResult = null;
            try {
                rawResult = multiFormatReader.decodeWithState(
                        new BinaryBitmap(
                                new HybridBinarizer(
                                        new BitmapLuminanceSource(mBitmap))));

            } catch (Exception e) {
                e.printStackTrace();
            }

            if (rawResult != null) {
                Log.d("lzc","resultText: "+ rawResult.getText());
            } else {
                Log.d("lzc","resultText: 解析失败");
            }
        }
    }



    @Override
    protected void onPause() {
        if (handler != null) {
            handler.quitSynchronously();
            handler = null;
        }
        beepManager.close();
        cameraManager.closeDriver();
        //historyManager = null; // Keep for onActivityResult
        if (!hasSurface) {
            SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
            SurfaceHolder surfaceHolder = surfaceView.getHolder();
            surfaceHolder.removeCallback(this);
        }
        super.onPause();
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                if (source == IntentSource.NATIVE_APP_INTENT) {
                    setResult(RESULT_CANCELED);
                    finish();
                    return true;
                }
                if ((source == IntentSource.NONE || source == IntentSource.ZXING_LINK) && lastResult != null) {
                    restartPreviewAfterDelay(0L);
                    return true;
                }
                break;
            case KeyEvent.KEYCODE_FOCUS:
            case KeyEvent.KEYCODE_CAMERA:
                // Handle these events so they don't launch the Camera app
                return true;
            // Use volume up/down to turn on light
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                cameraManager.setTorch(false);
                return true;
            case KeyEvent.KEYCODE_VOLUME_UP:
                cameraManager.setTorch(true);
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }



    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (holder == null) {
            Log.e(TAG, "*** WARNING *** surfaceCreated() gave us a null surface!");
        }
        if (!hasSurface) {
            hasSurface = true;
            initCamera(holder);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        hasSurface = false;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    /**
     * A valid barcode has been found, so give an indication of success and show the results.
     *
     * @param rawResult   The contents of the barcode.
     * @param scaleFactor amount by which thumbnail was scaled
     * @param barcode     A greyscale bitmap of the camera data which was decoded.
     */
    public void handleDecode(Result rawResult, Bitmap barcode, float scaleFactor) {
        //1. barcode 被识别的条码图片
        //2. rawResult.getBarcodeFormat().toString() 被识别条码的类型
        //3. ResultParser.parseResult(rawResult).getType()  识别内容的类型
        //4. rawResult.getTimestamp() 识别的时间
        //5. ResultParser.parseResult(rawResult).getDisplayResult().replace("\r","");
        //   被识别二维码中的内容

        Log.d("lzc","type: "+rawResult.getBarcodeFormat().toString());
        Log.d("lzc","type1: "+ ResultParser.parseResult(rawResult).getType().toString());
        Log.d("lzc","content: "+ResultParser.parseResult(rawResult).getDisplayResult().replace("\r","").toString());

        lastResult = rawResult;


        boolean fromLiveScan = barcode != null;
        if (fromLiveScan) {
            // Then not from history, so beep/vibrate and we have an image to draw on
            beepManager.playVibrate();
            //绘制 识别点
            drawResultPoints(barcode, scaleFactor, rawResult);
        }
//        handleDecodeInternally(rawResult,  barcode);
    }

    /**
     * Superimpose a line for 1D or dots for 2D to highlight the key features of the barcode.
     *
     * @param barcode     A bitmap of the captured image.
     * @param scaleFactor amount by which thumbnail was scaled
     * @param rawResult   The decoded results which contains the points to draw.
     */
    private void drawResultPoints(Bitmap barcode, float scaleFactor, Result rawResult) {
        ResultPoint[] points = rawResult.getResultPoints();
        Log.d("lzc", "points: " + points.length);
        if (points != null && points.length > 0) {
            Canvas canvas = new Canvas(barcode);
            Paint paint = new Paint();
            paint.setColor(getResources().getColor(R.color.result_points));
            if (points.length == 2) {
                paint.setStrokeWidth(4.0f);
                drawLine(canvas, paint, points[0], points[1], scaleFactor);
            } else if (points.length == 4 &&
                    (rawResult.getBarcodeFormat() == BarcodeFormat.UPC_A ||
                            rawResult.getBarcodeFormat() == BarcodeFormat.EAN_13)) {
                // Hacky special case -- draw two lines, for the barcode and metadata
                drawLine(canvas, paint, points[0], points[1], scaleFactor);
                drawLine(canvas, paint, points[2], points[3], scaleFactor);
            } else {
                paint.setStrokeWidth(10.0f);
                for (ResultPoint point : points) {
                    if (point != null) {
                        canvas.drawPoint(scaleFactor * point.getX(), scaleFactor * point.getY(), paint);
                    }
                }
            }
        }
    }

    private static void drawLine(Canvas canvas, Paint paint, ResultPoint a, ResultPoint b, float scaleFactor) {
        if (a != null && b != null) {
            canvas.drawLine(scaleFactor * a.getX(),
                    scaleFactor * a.getY(),
                    scaleFactor * b.getX(),
                    scaleFactor * b.getY(),
                    paint);
        }
    }

    // Put up our own UI for how to handle the decoded contents.
//    private void handleDecodeInternally(Result rawResult,  Bitmap barcode) {
//
//
//
//        statusView.setVisibility(View.GONE);
//        viewfinderView.setVisibility(View.GONE);
//        resultView.setVisibility(View.VISIBLE);
//
//        ImageView barcodeImageView = (ImageView) findViewById(R.id.barcode_image_view);
//        if (barcode == null) {
//            barcodeImageView.setImageBitmap(BitmapFactory.decodeResource(getResources(),
//                    R.drawable.launcher_icon));
//        } else {
//            barcodeImageView.setImageBitmap(barcode);
//        }
//
//        TextView formatTextView = (TextView) findViewById(R.id.format_text_view);
//        formatTextView.setText(rawResult.getBarcodeFormat().toString());
//
//        TextView typeTextView = (TextView) findViewById(R.id.type_text_view);
//        ParsedResultType type = ResultParser.parseResult(rawResult).getType();
//
//        typeTextView.setText(type.toString());
//
//        DateFormat formatter = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
//        TextView timeTextView = (TextView) findViewById(R.id.time_text_view);
//        timeTextView.setText(formatter.format(rawResult.getTimestamp()));
//
//
//        TextView metaTextView = (TextView) findViewById(R.id.meta_text_view);
//        View metaTextViewLabel = findViewById(R.id.meta_text_view_label);
//        metaTextView.setVisibility(View.GONE);
//        metaTextViewLabel.setVisibility(View.GONE);
//        Map<ResultMetadataType, Object> metadata = rawResult.getResultMetadata();
//        if (metadata != null) {
//            StringBuilder metadataText = new StringBuilder(20);
//            for (Map.Entry<ResultMetadataType, Object> entry : metadata.entrySet()) {
//                if (DISPLAYABLE_METADATA_TYPES.contains(entry.getKey())) {
//                    metadataText.append(entry.getValue()).append('\n');
//                }
//            }
//            if (metadataText.length() > 0) {
//                metadataText.setLength(metadataText.length() - 1);
//                metaTextView.setText(metadataText);
//                metaTextView.setVisibility(View.VISIBLE);
//                metaTextViewLabel.setVisibility(View.VISIBLE);
//            }
//        }
//
//       ParsedResult parsedResult= ResultParser.parseResult(rawResult);
//        String contents = parsedResult.getDisplayResult().replace("\r", "");
//        CharSequence displayContents = contents;
//        TextView contentsTextView = (TextView) findViewById(R.id.contents_text_view);
//        contentsTextView.setText(displayContents);
////        int scaledSize = Math.max(22, 32 - displayContents.length() / 4);
////        contentsTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, scaledSize);
//
//        TextView supplementTextView = (TextView) findViewById(R.id.contents_supplement_text_view);
//        supplementTextView.setText("");
//        supplementTextView.setOnClickListener(null);
//
////        int buttonCount = resultHandler.getButtonCount();
//        ViewGroup buttonView = (ViewGroup) findViewById(R.id.result_button_view);
//        buttonView.requestFocus();
//
//    }


//    private void sendReplyMessage(int id, Object arg, long delayMS) {
//        if (handler != null) {
//            Message message = Message.obtain(handler, id, arg);
//            if (delayMS > 0L) {
//                handler.sendMessageDelayed(message, delayMS);
//            } else {
//                handler.sendMessage(message);
//            }
//        }
//    }


    private void initCamera(SurfaceHolder surfaceHolder) {
        if (surfaceHolder == null) {
            throw new IllegalStateException("No SurfaceHolder provided");
        }
        if (cameraManager.isOpen()) {
            Log.w(TAG, "initCamera() while already open -- late SurfaceView callback?");
            return;
        }
        try {
            cameraManager.openDriver(surfaceHolder);
            // Creating the handler starts the preview, which can also throw a RuntimeException.
            if (handler == null) {
                handler = new CaptureActivityHandler(this,
                        decodeFormats,
                        null,
                        characterSet,
                        cameraManager,
                        ScreenOrientation);
            }
//            decodeOrStoreSavedBitmap(null, null);
        } catch (IOException ioe) {
            Log.w(TAG, ioe);
            displayFrameworkBugMessageAndExit();
        } catch (RuntimeException e) {
            // Barcode Scanner has seen crashes in the wild of this variety:
            // java.?lang.?RuntimeException: Fail to connect to camera service
            Log.w(TAG, "Unexpected error initializing camera", e);
            displayFrameworkBugMessageAndExit();
        }
    }

    private void displayFrameworkBugMessageAndExit() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.app_name));
        builder.setMessage(getString(R.string.msg_camera_framework_bug));
        builder.setPositiveButton(R.string.button_ok, new FinishListener(this));
        builder.setOnCancelListener(new FinishListener(this));
        builder.show();
    }

    public void restartPreviewAfterDelay(long delayMS) {
        if (handler != null) {
            handler.sendEmptyMessageDelayed(R.id.restart_preview, delayMS);
        }
        resetStatusView();
    }

    private void resetStatusView() {
        resultView.setVisibility(View.GONE);
        statusView.setText(R.string.msg_default_status);
        statusView.setVisibility(View.VISIBLE);
        viewfinderView.setVisibility(View.VISIBLE);
        lastResult = null;
    }

    public void drawViewfinder() {
        viewfinderView.drawViewfinder();
    }
}
