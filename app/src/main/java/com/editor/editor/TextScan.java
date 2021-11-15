package com.editor.editor;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import java.io.IOException;

public class TextScan extends AppCompatActivity {

    SurfaceView cameraView;
    EditText txtTextScan;
    CameraSource cameraSource;

    final int REQUEST_CAMERA_PERSMISSION_ID = 5005;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {

            case REQUEST_CAMERA_PERSMISSION_ID: {

                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    try {
                        cameraSource.start(cameraView.getHolder());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.text_scan);

        // Blendet beim Start die Tastatur aus.
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        cameraView = (SurfaceView) findViewById(R.id.surfaceView);
        txtTextScan = (EditText) findViewById(R.id.txtTextScan);

        TextRecognizer textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();

        if (textRecognizer.isOperational()) {

            cameraSource = new CameraSource.Builder(getApplicationContext(), textRecognizer)
                    .setFacing(CameraSource.CAMERA_FACING_BACK)
                    .setRequestedPreviewSize(800, 600)
                    .setAutoFocusEnabled(true)
                    .build();

            cameraView.getHolder().addCallback(new SurfaceHolder.Callback() {

                @Override
                public void surfaceCreated(@NonNull SurfaceHolder holder) {

                    camStart();
                }

                @Override
                public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {}

                @Override
                public void surfaceDestroyed(@NonNull SurfaceHolder holder) {

                    cameraSource.stop();
                }
            });

            textRecognizer.setProcessor(new Detector.Processor<TextBlock>() {

                @Override
                public void release() {}

                @Override
                public void receiveDetections(@NonNull Detector.Detections<TextBlock> detections) {

                    final SparseArray<TextBlock> items = detections.getDetectedItems();

                    if (items.size() != 0) {
                        txtTextScan.post(new Runnable() {
                            @Override
                            public void run() {
                                StringBuilder stringBuilder = new StringBuilder();

                                for (int i = 0; i < items.size(); i++) {

                                    TextBlock item = items.valueAt(i);

                                    stringBuilder.append(item.getValue());
                                    stringBuilder.append("\n");
                                }
                                txtTextScan.setText(stringBuilder.toString());
                            }
                        });
                    }
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.cam_options, menu);
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {

            case R.id.item_cam_start:
                camStart();
                break;

            case R.id.item_cam_stop:
                cameraSource.stop();
                break;

            case R.id.item_ok:
                scanTextSavePreferences();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        // Activity beenden.
        finish();
        super.onPause();
    }

    @Override
    protected void onStop() {
        // Activity beenden.
        finish();
        super.onStop();
    }

    /**
     * Überprüfen ob Benutzer die Erlaubnis zur Nutzung der Kamera erteilt hat.
     */
    public void camStart() {

        try {
            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(TextScan.this,
                        new String[]{Manifest.permission.CAMERA},
                        REQUEST_CAMERA_PERSMISSION_ID);
                return;
            }

            cameraSource.start(cameraView.getHolder());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Speichern der gescannten Daten in SharedPreferences.
     */
    public void scanTextSavePreferences() {

        // Der SharedPreferences-Variable Speicher- und Leseort zuweisen.
        MainActivity.prefScan = getSharedPreferences("textScan", 0);
        // Erstellen des Editors zu schreiben der Daten.
        SharedPreferences.Editor editor = MainActivity.prefScan.edit();
        // Daten in den Editor-Puffer schreiben.
        editor.putString("Text", txtTextScan.getText().toString());
        // Daten endgültig schreiben.
        editor.apply();

        // Laden der MainActivity
        Intent start = new Intent(this, MainActivity.class);
        startActivity(start);
    }
}