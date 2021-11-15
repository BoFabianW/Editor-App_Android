package com.editor.editor;

import android.annotation.SuppressLint;
import android.os.Build;
import android.view.View;
import android.widget.CompoundButton;
import androidx.annotation.RequiresApi;

public class MainActivityListener implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    MainActivity mainActivity;

    public MainActivityListener(MainActivity mainActivity) {

        this.mainActivity = mainActivity;
    }

    @SuppressLint("NonConstantResourceId")
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onClick(View v) {

        /*
         * Befehl 'go.dismiss' schließt immer den aktuell geöffneten Dialog.
         */
        switch (v.getId()) {

            case R.id.btnNeu:
                mainActivity.neuDialog();
                break;

            case R.id.btnSpeichern:
                mainActivity.speichernDialog();
                break;

            case R.id.btnOeffnen:
                mainActivity.oeffnenDialog();
                break;

            case R.id.btnDelete:
                mainActivity.dialogFileDelete();
                break;

            case R.id.btnJa:
                mainActivity.neuesDokument();
                break;

            case R.id.btnNein:
                mainActivity.go.dismiss();
                break;

            case R.id.btnDeleteJa:
                mainActivity.deleteFile();
                mainActivity.go.dismiss();
                break;

            case R.id.btnDeleteNein:
                mainActivity.go.dismiss();
                break;

            case R.id.btnSuchen:
                mainActivity.suchen(mainActivity.editTextEingabe.getText().toString(), mainActivity.editTextSuchen.getText().toString(), mainActivity.caseSensitive);
                mainActivity.go.dismiss();
                break;

            case R.id.btnErsetzen:
                mainActivity.allesErsetzen(mainActivity.caseSensitive);
                break;

            case R.id.btnSuchenSchliessen:
                mainActivity.go.dismiss();
                break;

            case R.id.btnSchliessen:
                mainActivity.go.dismiss();
                break;

            case R.id.btnClose:
                mainActivity.suchenBeenden();
                break;

            case R.id.btnVor:
                mainActivity.suchenVor();
                break;

            case R.id.btnZurueck:
                mainActivity.suchenZurueck();
                break;

            case R.id.btnSaveFile:
                mainActivity.speichernDialog();
                mainActivity.go.dismiss();
                break;

            case R.id.btnNoSaveFile:
                mainActivity.go.dismiss();
                try {
                    // Kleine Pause vor dem Beenden der App.
                    Thread.sleep(400);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mainActivity.finish();
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        mainActivity.caseSensitive = mainActivity.switchCase.isChecked();
    }
}
