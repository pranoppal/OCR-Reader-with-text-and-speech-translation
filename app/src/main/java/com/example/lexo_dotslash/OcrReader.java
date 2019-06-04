package com.example.lexo_dotslash;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import com.google.android.gms.vision.CameraSource;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;
import java.io.IOException;
import java.util.Locale;

import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.google.android.gms.vision.Detector;
import android.speech.tts.TextToSpeech;
import android.widget.Toast;


public class OcrReader extends AppCompatActivity {

    private CameraSource mCameraSource;
    private SurfaceView mCameraView;
    private TextView mTextView;
    private static String str;
    private static final String TAG = "OcrCaptureActivity";
    private Button speechbutton;
    private static final int requestPermissionID = 101;
    TextToSpeech t1;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.reader_layout);

        mCameraView = findViewById(R.id.surfaceView);
        mTextView = findViewById(R.id.text_view);
        speechbutton=findViewById(R.id.speech_button);
        startCameraSource();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode != requestPermissionID) {
            Log.d(TAG, "Got unexpected permission result: " + requestCode);
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            try {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                mCameraSource.start(mCameraView.getHolder());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void startCameraSource() {

        final TextRecognizer textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();

        if (!textRecognizer.isOperational()) {
            Log.w(TAG, "Detector dependencies not loaded yet");
        } else {

            mCameraSource = new CameraSource.Builder(getApplicationContext(), textRecognizer)
                    .setFacing(CameraSource.CAMERA_FACING_BACK)
                    .setRequestedPreviewSize(1280, 1024)
                    .setAutoFocusEnabled(true)
                    .setRequestedFps(2.0f)
                    .build();

            /**
             * Add call back to SurfaceView and check if camera permission is granted.
             * If permission is granted we can start our cameraSource and pass it to surfaceView
             */
            mCameraView.getHolder().addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(SurfaceHolder holder) {
                    try {

                        if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

                            ActivityCompat.requestPermissions(OcrReader.this,
                                    new String[]{Manifest.permission.CAMERA},
                                    requestPermissionID);
                            return;
                        }
                        mCameraSource.start(mCameraView.getHolder());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                }

                @Override
                public void surfaceDestroyed(SurfaceHolder holder) {
                    mCameraSource.stop();
                }
            });

            textRecognizer.setProcessor(new Detector.Processor<TextBlock>() {
                @Override
                public void release() {
                }


                @Override
                public void receiveDetections(Detector.Detections<TextBlock> detections) {
                    final SparseArray<TextBlock> items = detections.getDetectedItems();
                    if (items.size() != 0) {

                        mTextView.post(new Runnable() {
                            @Override
                            public void run() {
                                StringBuilder stringBuilder = new StringBuilder();
                                for (int i = 0; i < items.size(); i++) {
                                    TextBlock item = items.valueAt(i);
                                    stringBuilder.append(item.getValue());
                                    stringBuilder.append("\n");
                                }
                                str = stringBuilder.toString();
                                translateAsyncTask task = new translateAsyncTask();
                                task.execute(str);

                            }
                        });
                    }
                }
            });
        }
    }

    private class translateAsyncTask extends AsyncTask<String, Void, String> {
        private static final String API_KEY = BuildConfig.ApiKey;

        @Override
        protected String doInBackground(String... strs) {
            TranslateOptions options = TranslateOptions.newBuilder()
                    .setApiKey(API_KEY)
                    .build();
            Translate translate = options.getService();
            final Translation translation =
                    translate.translate(strs[0],
                            Translate.TranslateOption.targetLanguage("en"));

            return translation.getTranslatedText();
        }
        @Override
        protected void onPostExecute(String str) {
            mTextView.setText(str);

            t1 = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status) {
                    if(status != TextToSpeech.ERROR) {
                        t1.setLanguage(Locale.UK);
                    }
                }
            });

            speechbutton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String toSpeak = mTextView.getText().toString();
                    Toast.makeText(getApplicationContext(), toSpeak,Toast.LENGTH_SHORT).show();
                    t1.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
                }
            });
        }
    }
}
