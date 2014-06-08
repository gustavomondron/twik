package com.reddyetwo.hashmypass.app.util;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;

public class HashButtonEnableTextWatcher implements TextWatcher {

    private AutoCompleteTextView mTagEditText;
    private EditText mMasterKeyEditText;
    private Button mHashButton;

    public HashButtonEnableTextWatcher(AutoCompleteTextView tagEditText,
                                       EditText masterKeyEditText,
                                       Button hashButton) {
        mTagEditText = tagEditText;
        mMasterKeyEditText = masterKeyEditText;
        mHashButton = hashButton;
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
        updateHashButtonEnabled();
    }

    public void updateHashButtonEnabled() {
        boolean tagSet = mTagEditText.getText().toString().trim().length() > 0;
        boolean masterKeySet =
                mMasterKeyEditText.getText().toString().length() > 0;
        mHashButton.setEnabled(tagSet && masterKeySet);
    }


}

