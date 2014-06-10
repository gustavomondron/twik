package com.reddyetwo.hashmypass.app.util;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;

public class ProfileFormWatcher implements TextWatcher {

    private EditText mNameEditText;
    private EditText mPrivateKeyEditText;
    private Button mSaveButton;

    public ProfileFormWatcher(EditText nameEditText,
                              EditText privateKeyEditText, Button saveButton) {
        mNameEditText = nameEditText;
        mPrivateKeyEditText = privateKeyEditText;
        mSaveButton = saveButton;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count,
                                  int after) {
        // Do nothing
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before,
                              int count) {
        // Do nothing
    }

    @Override
    public void afterTextChanged(Editable s) {
        updateSaveButtonEnabled();
    }

    public void updateSaveButtonEnabled() {
        boolean nameSet =
                mNameEditText.getText().toString().trim().length() > 0;
        boolean privateKeySet =
                mPrivateKeyEditText.getText().toString().length() > 0;
        mSaveButton.setEnabled(nameSet && privateKeySet);
    }


}

