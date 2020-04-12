package com.example.mapapplication;

import android.app.Dialog;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

public class AppInfoDialog extends AppCompatDialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder//.setTitle("TITLE")
                .setMessage("Check the weather anywhere in the world!\n\n" +
                        "You can:\n" +
                        "- browse through the map\n" +
                        "- go to your current location\n" +
                        "- go to home location\n" +
                        "- set your home location from settings")/*.se   tPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        })*/;
        return builder.create();
    }
}