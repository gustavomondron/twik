package com.reddyetwo.hashmypass.app.util;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.TextView;

import com.reddyetwo.hashmypass.app.hash.PasswordHasher;

public class MasterKeyWatcher implements TextWatcher {

    private TextView mDigestTextView;

    public MasterKeyWatcher(TextView digestTextView) {
        mDigestTextView = digestTextView;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count,
                                  int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before,
                              int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        if (s.toString().length() == 0) {
            mDigestTextView.setText("");
        } else {
            mDigestTextView
                    .setText(PasswordHasher.calculateKeyDigest(s.toString()));
        }
    }
}
