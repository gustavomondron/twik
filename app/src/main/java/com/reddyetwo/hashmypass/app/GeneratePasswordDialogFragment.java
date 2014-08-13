package com.reddyetwo.hashmypass.app;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.reddyetwo.hashmypass.app.data.Preferences;
import com.reddyetwo.hashmypass.app.data.Profile;
import com.reddyetwo.hashmypass.app.data.ProfileSettings;
import com.reddyetwo.hashmypass.app.data.Tag;
import com.reddyetwo.hashmypass.app.data.TagSettings;
import com.reddyetwo.hashmypass.app.hash.PasswordHasher;
import com.reddyetwo.hashmypass.app.util.Constants;
import com.reddyetwo.hashmypass.app.util.FaviconLoader;

public class GeneratePasswordDialogFragment extends DialogFragment
        implements TagSettingsDialogFragment.OnTagSettingsSavedListener {

    private long mProfileId;
    private Tag mTag;
    private boolean mCacheMasterKey;

    private TextView mFaviconTextView;
    private AutoCompleteTextView mTagEditAutoCompleteTextView;
    private EditText mMasterKeyEditText;
    private TextView mPasswordTextView;
    private ImageView mTagSettingsImageView;

    public void setProfileId(long profileId) {
        mProfileId = profileId;
    }

    public void setTag(Tag tag) {
        mTag = tag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Inflate the layout
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_generate_password, null);
        builder.setView(view);
        builder.setNeutralButton(android.R.string.ok,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

        mFaviconTextView = (TextView) view.findViewById(R.id.tag_favicon);
        FaviconLoader.setAsBackground(getActivity(), mFaviconTextView, mTag);

        mTagEditAutoCompleteTextView =
                (AutoCompleteTextView) view.findViewById(R.id.tag_text);

        mMasterKeyEditText = (EditText) view.findViewById(R.id.master_key_text);
        mMasterKeyEditText.requestFocus();

        mPasswordTextView = (TextView) view.findViewById(R.id.website_password);
        Typeface tf = Typeface.createFromAsset(getActivity().getAssets(),
                Constants.FONT_MONOSPACE);
        mPasswordTextView.setTypeface(tf);

        mTagSettingsImageView =
                (ImageView) view.findViewById(R.id.tag_settings);
        mTagSettingsImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TagSettingsDialogFragment settingsDialog =
                        new TagSettingsDialogFragment();
                settingsDialog.setProfileId(mProfileId);
                settingsDialog.setTag(mTag);
                settingsDialog.setTagSettingsSavedListener(
                        GeneratePasswordDialogFragment.this);
                settingsDialog.show(getFragmentManager(), "tagSettings");
            }
        });

        PasswordTextWatcher watcher = new PasswordTextWatcher();
        mTagEditAutoCompleteTextView.addTextChangedListener(watcher);
        mMasterKeyEditText.addTextChangedListener(watcher);

        // Populate fields
        mTagEditAutoCompleteTextView.setText(mTag.getName());

        mCacheMasterKey =
                Preferences.getRememberMasterKeyMins(getActivity()) > 0;
        if (mCacheMasterKey) {
            mMasterKeyEditText.setText(HashMyPassApplication.getCachedMasterKey());
        }

        return builder.create();
    }

    private void updatePassword() {
        Profile profile = ProfileSettings.getProfile(getActivity(), mProfileId);
        String password = PasswordHasher.hashPassword(mTag.getName(),
                mMasterKeyEditText.getText().toString(),
                profile.getPrivateKey(), mTag.getPasswordLength(),
                mTag.getPasswordType());
        mPasswordTextView.setText(password);
    }

    @Override
    public void onTagSettingsSaved(Tag tag) {
        mTag = tag;
        updatePassword();
    }

    private class PasswordTextWatcher implements TextWatcher {

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
            // Store master key in the case that it is cached
            if (mCacheMasterKey) {
                HashMyPassApplication.setCachedMasterKey(
                        mMasterKeyEditText.getText().toString());
            }

            // Update password
            if (mTagEditAutoCompleteTextView.getText().length() > 0 &&
                    mMasterKeyEditText.getText().length() > 0) {
                updatePassword();
            } else {
                mPasswordTextView.setText("");
            }

            // Update tag settings icon visibility
            if (mTagEditAutoCompleteTextView.getText().length() > 0) {
                mTagSettingsImageView.setVisibility(View.VISIBLE);
                mTagSettingsImageView.setEnabled(true);
            } else {
                mTagSettingsImageView.setVisibility(View.INVISIBLE);
                mTagSettingsImageView.setEnabled(false);
            }
        }
    }


}
