package com.reddyetwo.hashmypass.app.cards;

import android.content.Context;
import android.graphics.Typeface;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;

import com.reddyetwo.hashmypass.app.R;
import com.reddyetwo.hashmypass.app.util.MasterKeyWatcher;

import it.gmariotti.cardslib.library.internal.Card;

public class MasterKeyCard extends Card {

    private OnMasterKeyChangedListener mMasterKeyChangedListener;
    private EditText mMasterKeyEditText;

    public MasterKeyCard(Context context,
                         OnMasterKeyChangedListener masterKeyChangedListener) {
        super(context, R.layout.card_master_key_inner_main);
        mMasterKeyChangedListener = masterKeyChangedListener;
    }

    public void showSoftKeyboard() {
        mMasterKeyEditText.requestFocus();
        InputMethodManager imm = (InputMethodManager) getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(mMasterKeyEditText, InputMethodManager.SHOW_IMPLICIT);
    }

    public String getMasterKey() {
        return mMasterKeyEditText.getText().toString();
    }

    public void setMasterKey(String masterKey) {
        mMasterKeyEditText.setText(masterKey);
        mMasterKeyChangedListener.onMasterKeyChanged(masterKey);
    }

    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {
        super.setupInnerViewElements(parent, view);
        mMasterKeyEditText =
                (EditText) view.findViewById(R.id.master_key);
        mMasterKeyEditText.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
        mMasterKeyEditText.setTransformationMethod(new PasswordTransformationMethod());

        ImageView imIdenticon = (ImageView) view.findViewById(R.id.identicon);
        mMasterKeyEditText.addTextChangedListener(
                new MasterKeyWatcher(getContext(), imIdenticon));
        mMasterKeyEditText.addTextChangedListener(new TextWatcher() {
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
                mMasterKeyChangedListener
                        .onMasterKeyChanged(mMasterKeyEditText.getText().toString());
            }
        });
    }
}
