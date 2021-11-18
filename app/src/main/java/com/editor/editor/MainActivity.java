package com.editor.editor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.documentfile.provider.DocumentFile;
import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    MainActivityListener mainActivityListener;

    @SuppressLint("StaticFieldLeak")
    public static TextView txtAktuelleDatei, txtAnzahlWort, txtAnzahlZeichen, txtChange;
    public EditText editTextEingabe, editTextSuchen, editTextErsetzen;
    public Button btnNeu, btnSpeichern, btnOeffnen, btnJa, btnNein, btnSuchen,
            btnErsetzen, btnSuchenSchliessen, btnSchliessen, btnZurueck,
            btnVor, btnClose, btnSaveFile, btnNoSaveFile, btnDelete,
            btnDeleteJa, btnDeleteNein;

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    public Switch switchCase;
    public boolean caseSensitive = true;

    public final int KEY_SAVE = 1001;
    public final int KEY_OPEN = 2002;
    public final int KEY_SPEAK = 3003;

    public Intent openDialog, saveDialog;
    public Uri saveFile = null;
    public String showFile = null;
    public String sprache;
    public String neuScanText = "";
    public float textSize = 18;
    public int indexShow;
    public String aktuellGesuchtesWort;
    public ArrayList<Integer> indexWort = new ArrayList<>();
    public ArrayList<String> result = new ArrayList<>();
    public DocumentFile documentFile;
    public AlertDialog go;
    public TextToSpeech tts;

    // SharedPreferences zum dauerhaften speichern von primitiven Datentypen.
    public SharedPreferences tempData;
    public static SharedPreferences prefSprache;
    public static SharedPreferences prefScan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextEingabe = findViewById(R.id.editTextEingabe);
        txtAktuelleDatei = findViewById(R.id.txtAktuelleDatei);
        txtChange = findViewById(R.id.txtChange);

        btnNeu = findViewById(R.id.btnNeu);
        btnSpeichern = findViewById(R.id.btnSpeichern);
        btnDelete = findViewById(R.id.btnDelete);
        btnOeffnen = findViewById(R.id.btnOeffnen);
        btnZurueck = findViewById(R.id.btnZurueck);
        btnVor = findViewById(R.id.btnVor);
        btnClose = findViewById(R.id.btnClose);

        mainActivityListener = new MainActivityListener(this);

        // Listener für Buttons zuweisen.
        btnNeu.setOnClickListener(mainActivityListener);
        btnSpeichern.setOnClickListener(mainActivityListener);
        btnDelete.setOnClickListener(mainActivityListener);
        btnOeffnen.setOnClickListener(mainActivityListener);
        btnZurueck.setOnClickListener(mainActivityListener);
        btnVor.setOnClickListener(mainActivityListener);
        btnClose.setOnClickListener(mainActivityListener);

        // Listener für EditText zuweisen.
        editTextEingabe.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                txtChange.setText("* ");
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Laden der SharedPreferences für Sprache
        prefSprache = getSharedPreferences("Sprache", MODE_PRIVATE);
        sprache = prefSprache.getString("Sprache", "deutsch");

        // Neues TextToSpeech-Objekt erstellen.
        tts = new TextToSpeech(this, status -> {

            if (status == TextToSpeech.SUCCESS) {

                // Prüfen welche Sprache eingestellt ist.
                switch (sprache) {
                    case "deutsch":
                        tts.setLanguage(Locale.GERMAN);
                        break;

                    case "englisch":
                        tts.setLanguage(Locale.ENGLISH);
                        break;

                    case "französisch":
                        tts.setLanguage(Locale.FRENCH);
                        break;
                }
                tts.setSpeechRate(0.8f);
            }
        });

        // Blendet beim Start die Tastatur aus.
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    /**
     * Bei Neustart der MainActivity 'loadPrefernecesForFile' und 'loadScan' ausführen.
     */
    @Override
    protected void onRestart() {
        loadPreferencesForFile();
        loadScan();
        super.onRestart();
    }

    /**
     * Beim stoppen der Activity 'datenSchreiben' ausführen.
     */
    @Override
    protected void onStop() {
        datenSchreiben();
        super.onStop();
    }

    @Override
    protected void onDestroy() {

        // TextToSpeech anhalten und beenden.
        tts.stop();
        tts.shutdown();

        super.onDestroy();
    }

    /**
     * Beim Betätigen der Back-Taste des Handys muss geprüft werden, ob es eine Änderung des Dokumentes gegeben hat.
     */
    @Override
    public void onBackPressed() {

        // Bei Änderung des Dokumentes den 'dialodSaveFile' aufrufen.
        if (!txtChange.getText().toString().equals("")) {
            dialogSaveFile();

        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options, menu);
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {

            case R.id.item0:
                scanStart();
                break;

            case R.id.item00:
                scanImageStart();
                break;

            case R.id.item1:
                if (btnClose.isEnabled()) {

                    Toast.makeText(this, R.string.dialog_suchen_ende, Toast.LENGTH_LONG).show();

                } else {

                    suchenDialog();
                }
                break;

            case R.id.item2:
                statistikDialog();
                break;

            case R.id.item3:
                allesMarkieren();
                break;

            case R.id.item4:
                textZoomIn();
                break;

            case R.id.item5:
                textZoomOut();
                break;

            case R.id.item6:
                settingStart();
                break;

            case R.id.item7:
                speak();
                break;

            case R.id.item8:
                aufnahmeDialog();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (data != null) {

            try {
                result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            } catch (Exception ignored) {}

            try {
                saveFile = data.getData();

                /*
                 * Übergibt dem DocumentFile-Object die Uri aus 'saveFile' und wandelt die Uri in einen Klartext-String.
                 * Dateiname wird nun sichtbar. (Im Uri-Format werden Dateinamen durch eine Zahl angezeigt!)
                 */
                documentFile = DocumentFile.fromSingleUri(this, saveFile);

                int lastIndex;

                // Der int-Variable wird der Index des letzten Vorkommen des Zeichens '/' übergeben.
                lastIndex = documentFile.getUri().toString().lastIndexOf("/");

                showFile = documentFile.getUri().toString().substring(0, lastIndex + 1) + documentFile.getName();

                // Ersetzt den 'slash' durch den 'backslash' in dem String 'showFile'.
                showFile = showFile.replace("/", "\\");

            } catch (Exception ignored) {}

            switch (requestCode) {

                case KEY_SAVE:
                    try {
                        if (resultCode == RESULT_OK) {

                            speichern();
                            break;
                        }
                    } catch (Exception ignored) {
                    }

                case KEY_OPEN:
                    try {
                        if (resultCode == RESULT_OK) {

                            oeffnen();
                            break;
                        }
                    } catch (Exception ignored) {
                    }

                case KEY_SPEAK:
                    try {
                        if (resultCode == RESULT_OK) {

                            StringBuilder textBestand = new StringBuilder();
                            textBestand.append(editTextEingabe.getText().toString());
                            textBestand.append(result.get(0));
                            textBestand.append(" ");
                            editTextEingabe.setText(textBestand);
                            break;
                        }
                    } catch (Exception e) {
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void speichernDialog() {

        saveDialog = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        saveDialog.setType("text/plain");
        startActivityForResult(saveDialog, KEY_SAVE);
    }

    /**
     * Inhalt in einer Datei speichern.
     */
    public void speichern() {

        try {

            OutputStream fos;

            fos = getContentResolver().openOutputStream(saveFile);
            fos.write(editTextEingabe.getText().toString().getBytes());
            fos.flush();
            fos.close();

            txtAktuelleDatei.setText(documentFile.getName());
            txtChange.setText("");

            btnDelete.setEnabled(true);
            btnDelete.setTextColor(Color.rgb(46, 131, 184));

            Toast.makeText(this, R.string.dialog_save_file + showFile + R.string.dialog_save_info, Toast.LENGTH_LONG).show();

            datenSchreiben();

        } catch (Exception e) {
            Toast.makeText(this, R.string.dialog_save_error, Toast.LENGTH_LONG).show();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void oeffnenDialog() {

        openDialog = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        openDialog.setType("text/plain");
        startActivityForResult(openDialog, KEY_OPEN);
    }

    /**
     * Inhalt einer Datei zeilenweise auslesen und dem StringBuilder hinzufügen.
     */
    public void oeffnen() {

        try {

            InputStream fis;

            fis = getContentResolver().openInputStream(saveFile);

            // Schreibt den Inhalt aus 'InputStraemReader' in einen Puffer (BufferedReader)
            BufferedReader r = new BufferedReader(new InputStreamReader(fis));

            String zeile;
            StringBuilder sb = new StringBuilder();

            /* Durchläuft die Schleife solange noch eine neue Zeile vohanden ist.
             * Ist keine Zeile mehr vohanden wird der Wert 'null' zurückgegeben und die Schleife wird beendet.
             */
            while ((zeile = r.readLine()) != null) {

                sb.append(zeile);
                sb.append("\n");
            }

            fis.close();

            editTextEingabe.setText(sb);
            txtAktuelleDatei.setText(documentFile.getName());
            txtChange.setText("");

            btnDelete.setEnabled(true);
            btnDelete.setTextColor(Color.rgb(46,131,184));

            datenSchreiben();

        } catch (Exception e) {
            Toast.makeText(this, R.string.dialog_open_error, Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }

        hideKeyboard();
    }

    /**
     * Überprüfen ob aktuell eine Suche läuft.
     * Gesamten Text im EditText-Feld markieren
     */
    public void allesMarkieren() {

        if (btnClose.isEnabled()) {
            Toast.makeText(this, R.string.dialog_selectall, Toast.LENGTH_LONG).show();
        } else {
            editTextEingabe.selectAll();
        }
    }

    /**
     * Aufbau des Neu-Dialogs.
     */
    public void neuDialog() {

        AlertDialog.Builder dialogNeu;

        LayoutInflater dialogNeuInflater = getLayoutInflater();

        View dialogNeuView = dialogNeuInflater.inflate(R.layout.dialog_neu, null);

        // Buttons des Dialogs 'Neu' zuweisen.
        btnJa   = (Button) dialogNeuView.findViewById(R.id.btnJa);
        btnNein = (Button) dialogNeuView.findViewById(R.id.btnNein);

        btnJa.setOnClickListener(mainActivityListener);
        btnNein.setOnClickListener(mainActivityListener);

        dialogNeu = new AlertDialog.Builder(this);
        dialogNeu.setTitle(R.string.dialog_neu_titel);
        dialogNeu.setView(dialogNeuView);

        /* Dem AlertDialog 'go' den erstellten AlertDialog 'dialogNeu' übergeben.
         * Dialog wird erstellt und angezeigt.
         * Eine globale AlertDialog-Variable wird angelegt. Zugriff von anderen Klassen möglich.
         * Erspart das Anlegen einer globalen AlertDialog-Variable für jeden einzelnen AlertDialog.
         */
        go = dialogNeu.create();
        go.show();
    }

    /**
     * Aufbau des Suchen-Dialogs.
     */
    public void suchenDialog() {

        // Tastatur ausblenden.
        hideKeyboard();

        AlertDialog.Builder dialogSuchen;

        LayoutInflater dialogSuchenInflater = getLayoutInflater();

        // View bzw. Layout für den Dialog festlegen.
        View dialogSuchenView = dialogSuchenInflater.inflate(R.layout.dialog_suchen, null);

        // Buttons des Dialogs 'Suchen' zuweisen.
        btnSuchen              = (Button) dialogSuchenView.findViewById(R.id.btnSuchen);
        btnErsetzen            = (Button) dialogSuchenView.findViewById(R.id.btnErsetzen);
        btnSuchenSchliessen    = (Button) dialogSuchenView.findViewById(R.id.btnSuchenSchliessen);

        // TextViews des Dialogs 'Suchen' zuweisen.

        editTextSuchen      = (EditText) dialogSuchenView.findViewById(R.id.editTextSuchen);
        editTextErsetzen    = (EditText) dialogSuchenView.findViewById(R.id.editTextErsetzen);

        // Switch zuweisen.
        switchCase          = (Switch) dialogSuchenView.findViewById(R.id.switchCase);

        // Listener für Buttons
        btnSuchen.setOnClickListener(mainActivityListener);
        btnErsetzen.setOnClickListener(mainActivityListener);
        btnSuchenSchliessen.setOnClickListener(mainActivityListener);

        // Listener für Switch zuweisen.
        switchCase.setOnCheckedChangeListener(mainActivityListener);

        // Listener für EditText 'Suchen'
        editTextSuchen.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (editTextSuchen.getText().length() < 1) {
                    btnSuchen.setEnabled(false);
                    btnErsetzen.setEnabled(false);
                    btnSuchen.setTextColor(Color.BLACK);
                    btnErsetzen.setTextColor(Color.BLACK);
                } else {
                    btnSuchen.setEnabled(true);
                    btnSuchen.setTextColor(Color.WHITE);

                    if (editTextErsetzen.getText().length() > 0) {
                        btnErsetzen.setEnabled(true);
                        btnErsetzen.setTextColor(Color.WHITE);
                    } else {
                        btnErsetzen.setEnabled(false);
                        btnErsetzen.setTextColor(Color.BLACK);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Listener für EditText 'Ersetzen'
        editTextErsetzen.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            /**
             * Überprüft beim Ändern des Textes die Länge des Textes und setzt die Buttons
             * 'Suchen' und 'Ersetzen' auf den entsprechenden Enabled-Wert.
             */
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (editTextErsetzen.getText().length() > 0) {

                    if (editTextSuchen.getText().length() > 0) {
                        btnErsetzen.setEnabled(true);
                        btnErsetzen.setTextColor(Color.WHITE);
                    }
                } else {
                    btnErsetzen.setEnabled(false);
                    btnErsetzen.setTextColor(Color.BLACK);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        dialogSuchen = new AlertDialog.Builder(this);
        dialogSuchen.setTitle(R.string.dialog_suchen_titel);
        dialogSuchen.setView(dialogSuchenView);

        /* Dem AlertDialog 'go' den erstellten AlertDialog 'suchenDialog' übergeben.
         * Dialog wird erstellt und angezeigt.
         * Eine globale AlertDialog-Variable wird angelegt. Zugriff von anderen Klassen möglich.
         * Erspart das Anlegen einer globalen AlertDialog-Variable für jeden einzelnen AlertDialog.
         */
        go = dialogSuchen.create();
        go.show();
    }

    /**
     * Aufbau des Statistik-Dialogs.
     */
    public void statistikDialog () {

        // Tastatur ausblenden.
        hideKeyboard();

        AlertDialog.Builder statistikDialog;
        LayoutInflater statistikDialogInflater = getLayoutInflater();
        View statistikDialogView = statistikDialogInflater.inflate(R.layout.dialog_statistik, null);

        // Button des Dialogs 'Statistik' zuweisen.
        btnSchliessen       = (Button) statistikDialogView.findViewById(R.id.btnSchliessen);

        // TextViews des Dialogs 'Statistik' zuweisen.
        txtAnzahlWort       = (TextView) statistikDialogView.findViewById(R.id.txtAnzahlWort);
        txtAnzahlZeichen    = (TextView) statistikDialogView.findViewById(R.id.txtAnzahlZeichen);

        btnSchliessen.setOnClickListener(mainActivityListener);

        statistikDialog = new AlertDialog.Builder(this);
        statistikDialog.setTitle(R.string.dialog_statistik_titel);
        statistikDialog.setView(statistikDialogView);

        /* Dem AlertDialog 'go' den erstellten AlertDialog 'statistikDialog' übergeben.
         * Dialog wird erstellt und angezeigt.
         * Eine globale AlertDialog-Variable wird angelegt. Zugriff von anderen Klassen möglich.
         * Erspart das Anlegen einer globalen AlertDialog-Variable für jeden einzelnen AlertDialog.
         */
        go = statistikDialog.create();
        go.show();

        statistikInfo();
    }

    /**
     * Ersetzt alle Wörter im Text durch den im SuchenDialog hinterlegten String.
     */
    public void allesErsetzen(boolean caseS) {

        // Text aus dem EditText-Feld der String-Variable 'alt' übergeben.
        String alt = editTextEingabe.getText().toString();
        String gesucht = editTextSuchen.getText().toString();

        // Prüfen ob die Groß- und Kleinschreibung berücksichtigt werden soll.
        if (!caseS) {
            alt = alt.toLowerCase();
            gesucht = gesucht.toLowerCase();
        }

        // Prüfen ob der gesuchte Text im EditText-Feld vorhanden ist.
        if (alt.contains(gesucht)) {

            String neu = alt.replace(gesucht, editTextErsetzen.getText().toString());
            editTextEingabe.setText(neu);
        } else {

            Toast.makeText(this, R.string.dialog_suchen_text_no, Toast.LENGTH_LONG).show();
        }

        // Setzt den Standardwert für die Berücksichtigung der Groß- und Kleinschreibung wieder her.
        caseSensitive = true;

        // Aktuell geöffneten Dialog schließen.
        go.dismiss();
    }

    /**
     * Setzt die Schriftgröße höher
     */
    public void textZoomIn() {

        if (textSize <= 40) {

            editTextEingabe.setTextSize(textSize + 2);
            editTextEingabe.refreshDrawableState();

            textSize = textSize + 2;
        }
    }

    /**
     * Setzt die Schriftgröße niedriger
     */
    public void textZoomOut() {

        if (textSize >= 20) {

            editTextEingabe.setTextSize(textSize - 2);
            editTextEingabe.refreshDrawableState();

            textSize = textSize - 2;
        }
    }

    /**
     * StatistikDialog wird mit Informationen gefüllt.
     */
    public void statistikInfo() {

        // Setzt die Länge des Textes in eine int-Variable.
        int anzahlZeichen = editTextEingabe.getText().length();

        // Wörter im Text zählen. Satzendungszeichen werden ignoriert.
        char[] text = editTextEingabe.getText().toString().toCharArray();

        StringBuilder stringChar = new StringBuilder();

        int position = 0;

        for (char c : text) {

            // Zeilenumbrüche werden als Zeichen gelesen. Aus diesem Grund wird bei jedem Zeilenumbruch im Text ein Zeichen für die Länge abgezogen.
            if (c == '\n') anzahlZeichen = anzahlZeichen - 1;
            txtAnzahlZeichen.setText(String.valueOf(anzahlZeichen));

            // Enthält ein Char eines dieser Zeichen, wird die int-Variable 'position' um 1 erhöht und die String-Variable zurück gesetzt.
            if (c == ' ' || c == '.' || c == ',' || c == ':' || c == ';' || c == '(' || c == ')' || c == '"' || c == '!' || c == '?' || c == '=' || c == '[' || c == ']' || c == '\n') {

                if (stringChar.length() > 0) {
                    position++;
                    stringChar = new StringBuilder();
                }

            } else {
                stringChar.append(c);
            }
        }

        if (stringChar.length() > 0) position++;

        if (anzahlZeichen == 0) txtAnzahlZeichen.setText("0");
        txtAnzahlWort.setText(String.valueOf(position));
    }

    /**
     Ausblenden des Keyboards
     */
    public void hideKeyboard() {

        try {
            InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(MainActivity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);

        } catch (Exception ignored) {}
    }

    /**
     * Gibt den Text aus dem EditText wieder.
     */
    public void speak() {

        // Prüfen der aktuellen SDK-Version.
        if (Build.VERSION.SDK_INT < 30) {

            if (editTextEingabe.getText().toString().trim().isEmpty()) {
                tts.speak(String.valueOf(R.string.dialog_speak_notext), TextToSpeech.QUEUE_FLUSH, null);

            } else {
                tts.speak(editTextEingabe.getText().toString(), TextToSpeech.QUEUE_FLUSH, null);
            }
        } else {
            Toast.makeText(this, R.string.dialog_speak_nosupport, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Aufbau des Aufnahme-Dialogs.
     */
    public void aufnahmeDialog() {

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

        // Prüfen welche Sprache eingestellt ist.
        switch (sprache) {
            case "deutsch":
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.GERMAN);
                break;

            case "englisch":
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.ENGLISH);
                break;

            case "französisch":
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.FRENCH);
                break;
        }

        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, R.string.dialog_speak);
        startActivityForResult(intent, KEY_SPEAK);
    }

    /**
     * Text in einem String (EditText) suchen. Startindex des gefundenen Textes wird in
     * einer ArrayList gespeichert.
     * @param textDurchsuchen nimmt des zu durchsuchenden String entgegen.
     * @param gesuchterText nimmt den zu suchenden String entgegen.
     */
    public void suchen(String textDurchsuchen, String gesuchterText, boolean caseSensitive) {

        indexShow = 0;
        aktuellGesuchtesWort = gesuchterText;

        /*
         * Prüfen ob caseSensitive 'false' ist.
         * Groß- und Kleinschreibung wird nicht berücksichtigt.
         */
        if (!caseSensitive) {

            // Strings werden in Kleinbuchstaben umgewandelt.
            textDurchsuchen = textDurchsuchen.toLowerCase();
            gesuchterText = gesuchterText.toLowerCase();
        }

        int index = 0;

        for (int i = 0 ; i < textDurchsuchen.length() ; i++) {

            int temp = textDurchsuchen.indexOf(gesuchterText, i);

            if (temp == -1) break;

            if (temp == 0) indexWort.add(temp);
            if (index != temp) indexWort.add(temp);

            index = temp;
        }

        // Ist die ArrayList größer als 0, muss geprüft werden, welche Button aktiviert werden.
        if (indexWort.size() > 0) {

            /* Überprüfen welche Button aktiviert werden.
             * Ist die Größe der ArrayList = 1, muss der Button 'Vor' nicht aktiviert werden,
             * da nur ein Suchergebnis vorliegt.
             */
            if (indexWort.size() == 1) {
                btnClose.setEnabled(true);
            } else {
                btnVor.setEnabled(true);
                btnClose.setEnabled(true);
            }

            editTextEingabe.setSelection(indexWort.get(indexShow), indexWort.get(indexShow) + aktuellGesuchtesWort.length());
            editTextEingabe.setEnabled(false);

            // Setzt den Standardwert für die Berücksichtigung der Groß- und Kleinschreibung wieder her.
            this.caseSensitive = true;

            indexShow++;

        } else {
            Toast.makeText(this, R.string.dialog_suchen_text_no, Toast.LENGTH_LONG).show();
        }
    }

    public void suchenVor() {

        editTextEingabe.setSelection(indexWort.get(indexShow), indexWort.get(indexShow) + aktuellGesuchtesWort.length());
        indexShow++;

        if (indexShow > indexWort.size() -1) {
            btnVor.setEnabled(false);
        }

        btnZurueck.setEnabled(true);
    }

    public void suchenZurueck() {

        indexShow += -2;
        editTextEingabe.setSelection(indexWort.get(indexShow), indexWort.get(indexShow) + aktuellGesuchtesWort.length());

        if (indexShow <= 0) {
            indexShow = 0;
            btnZurueck.setEnabled(false);
        }

        indexShow ++;
        btnVor.setEnabled(true);
    }
    /**
     * Deaktiviert die Buttons zum Dursuchen des Textes.
     */
    public void suchenBeenden() {

        // Deaktiviert die Buttons beim Suchen.
        btnZurueck.setEnabled(false);
        btnVor.setEnabled(false);
        btnClose.setEnabled(false);

        // Aktiviert das Textfeld.
        editTextEingabe.setEnabled(true);

        aktuellGesuchtesWort = "";

        // ArrayList wird geleert.
        indexWort.clear();
    }

    /**
     * Starten der SettingsActivity und Übergabe des Variablenwertes der Variable 'sprache' durch einen Intent.
     */
    public void settingStart() {

        // Laden der SharedPreferences für Sprache
        prefSprache = getSharedPreferences("Sprache", 0);
        sprache = prefSprache.getString("Sprache", "deutsch");

        Intent daten = new Intent(this, SettingsActivity.class);
        daten.putExtra("SpracheAktuell", sprache);
        startActivity(daten);
    }

    public void scanStart() {

        hideKeyboard();

        // Starten der Activity
        Intent cam = new Intent(this, TextScan.class);
        startActivity(cam);
    }

    public void scanImageStart() {

        hideKeyboard();

        // Starten der Activity
        Intent image = new Intent(this, ImageScan.class);
        startActivity(image);
    }

    /**
     * Speichern des EditText-Inhaltes und die Angabe der geöffneten Datei.
     */
    public void datenSchreiben() {

        // Der SharedPreferences-Variable Speicher- und Leseort zuweisen.
        tempData = getSharedPreferences("zwischenspeicher", 0);
        // Erstellen des Editors zu schreiben der Daten.
        SharedPreferences.Editor editor = tempData.edit();
        // Daten in den Editor-Puffer schreiben.
        editor.putString("Text", editTextEingabe.getText().toString());
        editor.putString("Datei", txtAktuelleDatei.getText().toString());
        editor.putString("Change", txtChange.getText().toString());
        editor.putString("FileOpen", showFile); // Neu
        // Daten endgültig schreiben.
        editor.apply();
    }

    /**
     * Laden der Preferences nach dem Scannen von Texten.
     */
    public void loadScan() {

        boolean change = true;

        // Der SharedPreferences-Variable Speicher- und Leseort zuweisen.
        prefScan = getSharedPreferences("textScan", 0);

        /*
         * Da beim setzen der String-Variable 'neuScanText' in das EditText-Element der 'onTextChange-Listener' angesprochen wird,
         * muss geprüft werden, ob es wirklich zu einer Veränderung des Textes durch die Übernahme aus dem Scan kommt und ob es vor
         * Beginn des Scans schon eine Veränderung des Textes gegeben hat.
         */
        if (tempData.getString("Change", "").equals("") && prefScan.getString("Text", "").equals("")) change = false;

        neuScanText = editTextEingabe.getText().toString() + prefScan.getString("Text", "");
        editTextEingabe.setText(neuScanText);

        // Hat es vor dem Scan keine Veränderung gegeben und wird kein Inhalt aus dem Scan übernommen, wird der Text entfernt.
        if (!change) txtChange.setText("");

        // Erstellen des Editors zu schreiben der Daten.
        SharedPreferences.Editor editor = prefScan.edit();
        // Daten in den Editor-Puffer schreiben.
        editor.putString("Text", "");
        // Daten endgültig schreiben.
        editor.apply();
    }

    /**
     * Laden der Preferences für Datei, Text und Status
     */
    public void loadPreferencesForFile() {

        tempData = getSharedPreferences("zwischenspeicher", 0);

        editTextEingabe.setText(tempData.getString("Text", ""));
        txtAktuelleDatei.setText(tempData.getString("Datei", ""));
        txtChange.setText(tempData.getString("Change", ""));
    }

    /**
     * Dialog zur Abfrage des Speichern bei Änderungen im Dokument vor dem Beenden der App.
     */
    public void dialogSaveFile() {

        AlertDialog.Builder dialogSaveFile;

        LayoutInflater dialogNeuInflater = getLayoutInflater();

        View dialogSaveFileView = dialogNeuInflater.inflate(R.layout.dialog_save_file, null);

        // Buttons zuweisen.
        btnSaveFile     = (Button) dialogSaveFileView.findViewById(R.id.btnSaveFile);
        btnNoSaveFile   = (Button) dialogSaveFileView.findViewById(R.id.btnNoSaveFile);

        // Listener zuweisen.
        btnSaveFile.setOnClickListener(mainActivityListener);
        btnNoSaveFile.setOnClickListener(mainActivityListener);

        dialogSaveFile = new AlertDialog.Builder(this);
        dialogSaveFile.setTitle(R.string.dialog_save_titel);
        dialogSaveFile.setView(dialogSaveFileView);

        /* Dem AlertDialog 'go' den erstellten AlertDialog 'dialogSaveFile' übergeben.
         * Dialog wird erstellt und angezeigt.
         * Eine globale AlertDialog-Variable wird angelegt. Zugriff von anderen Klassen möglich.
         * Erspart das Anlegen einer globalen AlertDialog-Variable für jeden einzelnen AlertDialog.
         */
        go = dialogSaveFile.create();
        go.show();
    }

    public void dialogFileDelete() {

        AlertDialog.Builder dialogFileDelete;

        LayoutInflater dialogFileDeleteInflater = getLayoutInflater();

        View dialogFileDeleteView = dialogFileDeleteInflater.inflate(R.layout.dialog_file_delete, null);

        // Buttons zuweisen.
        btnDeleteJa     = (Button) dialogFileDeleteView.findViewById(R.id.btnDeleteJa);
        btnDeleteNein   = (Button) dialogFileDeleteView.findViewById(R.id.btnDeleteNein);

        // Listener zuweisen.
        btnDeleteJa.setOnClickListener(mainActivityListener);
        btnDeleteNein.setOnClickListener(mainActivityListener);

        dialogFileDelete = new AlertDialog.Builder(this);
        dialogFileDelete.setTitle(R.string.dialog_delete_titel);
        dialogFileDelete.setView(dialogFileDeleteView);

        /* Dem AlertDialog 'go' den erstellten AlertDialog 'dialogFileDelete' übergeben.
         * Dialog wird erstellt und angezeigt.
         * Eine globale AlertDialog-Variable wird angelegt. Zugriff von anderen Klassen möglich.
         * Erspart das Anlegen einer globalen AlertDialog-Variable für jeden einzelnen AlertDialog.
         */
        go = dialogFileDelete.create();
        go.show();
    }

    public void deleteFile() {

        /* Ab SDK-Version 23 muss vor dem Zugriff auf den externen Speicher eine Berechtigungsabfrage stattfinden.
         * Der Eintrag im Manifest ist dringend erforderlich aber beeinflusst nicht die zusätzliche Abfrage.
         */
        if (android.os.Build.VERSION.SDK_INT >= 23) {

            int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

            /*
             * Prüfen ob Berechtigung erteilt wurde, ansonsten nach Berechtigung fragen.
             */
            if (permission != PackageManager.PERMISSION_GRANTED) {
                this.requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 200);

            } else {
                delete();
            }
        } else {
            delete();
        }
    }

    public void delete() {
        if (documentFile.delete()) {
            neuesDokument();
            Toast.makeText(this, R.string.dialog_delete_file, Toast.LENGTH_LONG).show();
        }
    }

    @SuppressLint("SetTextI18n")
    public void neuesDokument() {

        editTextEingabe.setText("");
        txtAktuelleDatei.setText("");
        txtChange.setText("");
        btnDelete.setEnabled(false);
        btnDelete.setTextColor(Color.rgb(198,198,198));
        saveFile = null;
        datenSchreiben();
        go.dismiss();
    }
}