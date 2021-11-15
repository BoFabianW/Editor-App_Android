package com.editor.editor;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import java.io.IOException;

public class ImageScan extends AppCompatActivity implements View.OnClickListener {

    MainActivity mainActivity;

    public Intent imageDialog;
    public ImageView imageScan;
    public EditText txtImageScan;
    public TextView txtInfo;
    public Bitmap image;
    public Button btnScan;

    public final int KEY_OPEN_IMAGE = 4004;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_scan);

        // Blendet beim Start die Tastatur aus.
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        imageScan       = (ImageView) findViewById(R.id.imgScan);
        txtImageScan    = (EditText)  findViewById(R.id.txtImageScan);
        txtInfo         = (TextView)  findViewById(R.id.txtInfo);
        btnScan         = (Button)    findViewById(R.id.btnScan);

        // Listener setzen.
        imageScan.setOnClickListener(this);
        btnScan.setOnClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.image_scan_options, menu);
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {

            case R.id.item_ok_image:
                scanImageTextSavePreferences();
                break;

            case R.id.item_load_image:
                loadImage();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Wird automatisch nach dem Schließen einer Dialog-Activity (Öffnen, Speichern, Kamera usw.) ausgeführt
     * und gibt die empfangenen Daten zurück.
     * @param requestCode gibt den übergebenen RequestCode beim Aufruf des Dialogs zurück.
     * @param resultCode gibt die vom Benutzer gedrückte Taste zurück (OK, Cancel usw.)
     * @param data enthält die empfangenen Daten.
     * Der requestCode wird verwendet um den geöffneten Dialog zu identifizieren.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if (data == null) return;

                if (resultCode == RESULT_OK && requestCode == KEY_OPEN_IMAGE) {

                    /* 'get.data' gibt eine URI zurück.
                     * imageScan(ImageView) erhält neues Image.
                     */
                    imageScan.setImageURI(data.getData());

                    // Button deaktivieren um einen Scan ohne geladenes Bild zu vermeiden.
                    btnScan.setEnabled(true);
                    imageScan.setVisibility(View.VISIBLE);

                    try {
                        // 'image' enthält das geladene Bild.
                        image = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Öffnen-Dialog zum laden des Bildes.
     */
    public void loadImage() {

        imageScan.setVisibility(View.INVISIBLE);
        // Button zum scannen wieder deaktivieren.
        btnScan.setEnabled(false);

        // Text über Imaage auf Standard  zurücksetzen.
        txtInfo.setText(R.string.dialog_scan_text);
        txtInfo.setTextColor(Color.BLACK);

        // Image unsichtbar.
        imageScan.setVisibility(View.INVISIBLE);

        // Öffnen-Dialog starten.
        imageDialog = new Intent(Intent.ACTION_GET_CONTENT);
        imageDialog.setType("image/*");
        startActivityForResult(imageDialog, KEY_OPEN_IMAGE);
    }

    /**
     * Innere Klasse - Rotiert die Bitmap 'image' um 90 Grad nach rechts.
     */
    public class ScaleImage implements Runnable {

        @Override
        public void run() {

            Handler scale = new Handler(Looper.getMainLooper());

            scale.post(() -> {

                try {

                    imageScan.setScaleType(ImageView.ScaleType.MATRIX);

                    // Die Matrix-Klasse wird zum Transformieren von Koordinaten verwendet.
                    Matrix matrix = new Matrix();

                    matrix.postRotate(90);

                    image = Bitmap.createBitmap(image, 0, 0, image.getWidth(), image.getHeight(), matrix, true);

                    imageScan.setImageBitmap(image);

                    imageScan.setScaleType(ImageView.ScaleType.FIT_CENTER);

                } catch (Exception e) {
                    Toast.makeText(mainActivity, "Es ist ein Fehler aufgetreten. Bitte neu scannen.", Toast.LENGTH_LONG).show();
                }

                // Button zum Scannen wieder aktivieren.
                btnScan.setEnabled(true);
                // Textfarbe des Buttons wieder auf blau setzen.
                btnScan.setTextColor(Color.rgb(46,131,184));
            });
        }
    }

    /**
     * Speichern der gescannten Daten in SharedPreferences.
     */
    public void scanImageTextSavePreferences() {

        // Der SharedPreferences-Variable Speicher- und Leseort zuweisen.
        MainActivity.prefScan = getSharedPreferences("textScan", 0);
        // Erstellen des Editors zum Schreiben der Daten.
        SharedPreferences.Editor editor = MainActivity.prefScan.edit();
        // Daten in den Editor-Puffer schreiben.
        editor.putString("Text", txtImageScan.getText().toString());
        // Daten endgültig schreiben.
        editor.apply();

        // Laden der MainActivity
        Intent start = new Intent(this, MainActivity.class);
        startActivity(start);

        // Beenden der aktiven Activity.
        finish();
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {

        switch ((v.getId())) {

            case R.id.btnScan:
                changeElementsForScan();
                // Neues Objekt der 'RunScanImage-Klasse' erstellen.
                RunScanImage run = new RunScanImage();
                new Thread(run).start();
                break;

            case R.id.imgScan:
                // Button deaktivieren um das Ausführen eines Scans wärend der Scalierung zu vermeiden.
                btnScan.setEnabled(false);
                // Textfarbe des Buttons auf Rot setzen.
                btnScan.setTextColor(Color.RED);
                ScaleImage scale = new ScaleImage();
                new Thread(scale).start();
                break;
        }
    }

    @SuppressLint("SetTextI18n")
    public void changeElementsForScan() {

        if (txtInfo.getText().toString().equals("Bild antippen um es zu drehen.") || txtInfo.getText().toString().equals("Tap the image to rotate it.")) {

            txtInfo.setText(R.string.dialog_scan_start);
            txtInfo.setTextColor(Color.RED);
            btnScan.setEnabled(false);
            imageScan.setEnabled(false);

        } else {

            txtInfo.setText(R.string.dialog_scan_text);
            txtInfo.setTextColor(MainActivity.txtAktuelleDatei.getTextColors());
            btnScan.setEnabled(true);
            imageScan.setEnabled(true);
        }
    }

    /**
     * Innere Klasse zum Auslesen von Text über eine Bild-Datei.
     * Um ein einfrieren der Activity zu vermeiden, wird diese Klasse in einem eigenen Thread ausgeführt.
     */
    public class RunScanImage implements Runnable {

        Handler changeElements = new Handler(Looper.getMainLooper());
        StringBuilder addScanText = new StringBuilder();

        @Override
        public void run() {

            try {

                TextRecognizer textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();

                // Erstellt ein neues Frame mit einer Bitmap.
                Frame frame = new Frame.Builder().setBitmap(image).build();

                SparseArray<TextBlock> items = textRecognizer.detect(frame);

                for (int i = 0 ; i < items.size() ; i++) {

                    TextBlock neuItem = items.valueAt(i);

                    addScanText.append(neuItem.getValue());
                    addScanText.append("\n");
                }

            } catch (Exception ignored) {}

            changeElements.post(() -> {
                changeElementsForScan();
                txtImageScan.setText(addScanText.toString());
            });
        }
    }
}