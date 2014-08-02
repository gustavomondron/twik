package com.reddyetwo.hashmypass.app;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class GeneratePasswordDialogFragment extends DialogFragment {

    private TextView mFaviconTextView;
    private EditText mTagEditText;
    private EditText mMasterKeyEditText;
    private TextView mPasswordTextView;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Inflate the layout
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_generate_password, null);
        builder.setView(view);
        builder.setNeutralButton("Ok, I got it!", new DialogInterface
                .OnClickListener
                () {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        return builder.create();


    }
}
