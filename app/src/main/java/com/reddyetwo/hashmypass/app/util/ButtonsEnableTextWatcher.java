package com.reddyetwo.hashmypass.app.util;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

public class ButtonsEnableTextWatcher implements TextWatcher {

    private AutoCompleteTextView mTagEditText;
    private EditText mMasterKeyEditText;
    private ImageButton mTagSettingsButton;
    private Button mHashButton;

    public ButtonsEnableTextWatcher(AutoCompleteTextView tagEditText,
                                    EditText masterKeyEditText,
                                    ImageButton tagSettingsButton,
                                    Button hashButton) {
        mTagEditText = tagEditText;
        mMasterKeyEditText = masterKeyEditText;
        mTagSettingsButton = tagSettingsButton;
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
        updateTagSettingsButtonEnabled();
        updateHashButtonEnabled();
    }

    public void updateHashButtonEnabled() {
        boolean tagSet = mTagEditText.getText().toString().trim().length() > 0;
        boolean masterKeySet =
                mMasterKeyEditText.getText().toString().length() > 0;
        mHashButton.setEnabled(tagSet && masterKeySet);
    }

    public void updateTagSettingsButtonEnabled() {
        boolean tagSet = mTagEditText.getText().toString().trim().length() > 0;
        mTagSettingsButton.setEnabled(tagSet);
        if (tagSet) {
            mTagSettingsButton.setVisibility(View.VISIBLE);
        } else {
            mTagSettingsButton.setVisibility(View.INVISIBLE);
        }
    }


}

