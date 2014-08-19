package com.reddyetwo.hashmypass.app;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.reddyetwo.hashmypass.app.util.RandomPrivateKeyGenerator;

public class TutorialSetupFragment extends Fragment {

    private ViewGroup mRootView;
    private EditText mPrivateKeyText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = (ViewGroup) inflater
                .inflate(R.layout.fragment_tutorial_setup, container,
                        false);
        mPrivateKeyText = (EditText) mRootView.findViewById(R.id
                .private_key_text);
        mPrivateKeyText.setText(RandomPrivateKeyGenerator.generate());
        mPrivateKeyText.addTextChangedListener(new TextWatcher() {
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
                boolean enableButton = mPrivateKeyText.getText().toString()
                        .length() > 0;
            }
        });
        return mRootView;
    }

}
