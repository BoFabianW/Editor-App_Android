package com.editor.editor;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener, View.OnClickListener {

    public Button btnChange;
    public RadioButton rbDeutsch, rbEnglisch, rbFranz;

    public String spracheNeu;
    public String spracheAktuell;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        btnChange   = findViewById(R.id.btnChange);

        rbDeutsch   = (RadioButton) findViewById(R.id.rbDeutsch);
        rbEnglisch  = (RadioButton) findViewById(R.id.rbEnglisch);
        rbFranz     = (RadioButton) findViewById(R.id.rbFranz);

        // Listener für Button.
        btnChange.setOnClickListener(this);

        // Listener für RadioButtons.
        rbDeutsch.setOnCheckedChangeListener(this);
        rbEnglisch.setOnCheckedChangeListener(this);
        rbFranz.setOnCheckedChangeListener(this);

        // Den übergebenen Intent aus der MainActivity auslesen.
        Intent daten = getIntent();
        spracheAktuell  = daten.getStringExtra("SpracheAktuell");
        spracheNeu      = daten.getStringExtra("SpracheAktuell");

        checked();
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

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                btnChange.setEnabled(true);
                btnChange.setTextColor(Color.rgb(46,131,184));
    }

    @Override
    public void onClick(View v) {

        // Setzt die String-Variable 'spracheNeu' auf die aktuelle gewählte Sprache.
        if (rbDeutsch.isChecked())  spracheNeu = "deutsch";
        if (rbEnglisch.isChecked()) spracheNeu = "englisch";
        if (rbFranz.isChecked())    spracheNeu = "französisch";

        // Neue Spracheinstellung speichern.
        setzeSprache();

        // Deaktiviert den Button.
        btnChange.setEnabled(false);
        btnChange.setTextColor(Color.BLACK);
    }

    /**
     * Prüfen welche Sprache aktuell gewählt ist und den Radio-Options entsprechend setzen.
     */
    public void checked() {

        switch (spracheAktuell) {

            case "deutsch":
                rbDeutsch.setChecked(true);
                break;

            case "englisch":
                rbEnglisch.setChecked(true);
                break;

            case "französisch":
                rbFranz.setChecked(true);
                break;
        }

        // Deaktiviert den Button.
        btnChange.setEnabled(false);
        btnChange.setTextColor(Color.BLACK);
    }

    /**
     * Speichert die aktuelle Sprache als String in die SharedPreferences.
     * Auf SharedPreferences kann immer über den Aufruf der Klasse zugegriffen werden.
     */
    public void setzeSprache() {

        // Der SharedPreferences-Variable Speicher- und Leseort zuweisen.
        MainActivity.prefSprache = getSharedPreferences("Sprache", 0);
        // Erstellen des Editors zu schreiben der Daten.
        SharedPreferences.Editor editor = MainActivity.prefSprache.edit();
        // Daten in den Editor-Puffer schreiben.
        editor.putString("Sprache", spracheNeu);
        // Daten endgültig schreiben.
        editor.apply();
    }
}

