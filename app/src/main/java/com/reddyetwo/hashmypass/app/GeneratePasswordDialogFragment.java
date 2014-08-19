/*
 * Copyright 2014 Red Dye No. 2
 *
 * This file is part of Hash My pass.
 *
 * Hash my pass is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Hash my pass is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Hash my pass.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.reddyetwo.hashmypass.app;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.reddyetwo.hashmypass.app.data.Preferences;
import com.reddyetwo.hashmypass.app.data.Profile;
import com.reddyetwo.hashmypass.app.data.ProfileSettings;
import com.reddyetwo.hashmypass.app.data.Tag;
import com.reddyetwo.hashmypass.app.data.TagSettings;
import com.reddyetwo.hashmypass.app.hash.PasswordHasher;
import com.reddyetwo.hashmypass.app.util.ClipboardHelper;
import com.reddyetwo.hashmypass.app.util.Constants;
import com.reddyetwo.hashmypass.app.util.FaviconLoader;
import com.reddyetwo.hashmypass.app.util.SecurePassword;

public class GeneratePasswordDialogFragment extends DialogFragment
        implements TagSettingsDialogFragment.OnTagSettingsSavedListener,
        IdenticonGenerationTask.OnIconGeneratedListener {

    private long mProfileId;
    private Tag mTag;
    private boolean mCacheMasterKey;
    private IdenticonGenerationTask mTask;
    private GeneratePasswordDialogListener mListener;

    private TextView mFaviconTextView;
    private AutoCompleteTextView mTagEditAutoCompleteTextView;
    private EditText mMasterKeyEditText;
    private TextView mPasswordTextView;
    private ImageView mTagSettingsImageView;
    private ImageView mIdenticonImageView;

    public void setProfileId(long profileId) {
        mProfileId = profileId;
    }

    public void setTag(Tag tag) {
        mTag = tag;
    }

    public void setDialogOkListener(GeneratePasswordDialogListener listener) {
        mListener = listener;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        mListener.onDialogDismiss(null);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // Prevent destroy-and-create cycle on configuration change
        // This introduces a bug: the dialog is dismissed, but at least no FC
        setRetainInstance(true);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(),
                R.style.AlertDialog_Hashmypass);

        // Inflate the layout
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_generate_password, null);
        builder.setView(view);
        builder.setNeutralButton(android.R.string.ok,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        // Copy password to clipboard
                        if (Preferences.getCopyToClipboard(getActivity()) &&
                                mPasswordTextView.length() > 0) {
                            ClipboardHelper.copyToClipboard(getActivity(),
                                    ClipboardHelper.CLIPBOARD_LABEL_PASSWORD,
                                    mPasswordTextView.getText().toString(),
                                    R.string.copied_to_clipboard);
                        }

                        // Hide keyboard
                        hideKeyboard();

                        // Call listener
                        mListener.onDialogDismiss(mTag);
                    }
                });

        mFaviconTextView = (TextView) view.findViewById(R.id.tag_favicon);
        FaviconLoader.setAsBackground(getActivity(), mFaviconTextView, mTag);

        mTagEditAutoCompleteTextView =
                (AutoCompleteTextView) view.findViewById(R.id.tag_text);

        mMasterKeyEditText = (EditText) view.findViewById(R.id.master_key_text);

        // Manage focus
        if (mTag.getId() != Tag.NO_ID) {
            // Tag name already populated
            mMasterKeyEditText.requestFocus();
        } else {
            mTagEditAutoCompleteTextView.requestFocus();
        }

        mPasswordTextView = (TextView) view.findViewById(R.id.website_password);
        Typeface tf = Typeface.createFromAsset(getActivity().getAssets(),
                Constants.FONT_MONOSPACE);
        mPasswordTextView.setTypeface(tf);
        mPasswordTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPasswordTextView.length() > 0) {
                    ClipboardHelper.copyToClipboard(getActivity(),
                            ClipboardHelper.CLIPBOARD_LABEL_PASSWORD,
                            mPasswordTextView.getText().toString(),
                            R.string.copied_to_clipboard);
                }
            }
        });

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

        mIdenticonImageView = (ImageView) view.findViewById(R.id.identicon);

        PasswordTextWatcher watcher = new PasswordTextWatcher();
        mTagEditAutoCompleteTextView.addTextChangedListener(watcher);
        mMasterKeyEditText.addTextChangedListener(watcher);

        // Populate fields
        mTagEditAutoCompleteTextView.setText(mTag.getName());

        mCacheMasterKey =
                Preferences.getRememberMasterKeyMins(getActivity()) > 0;
        if (mCacheMasterKey) {
            mMasterKeyEditText
                    .setText(HashMyPassApplication.getCachedMasterKey(), 0,
                            HashMyPassApplication.getCachedMasterKey().length);
        }

        // Manage keyboard status
        if (mMasterKeyEditText.length() == 0 ||
                mTagEditAutoCompleteTextView.length() == 0) {
            showKeyboard();
        }


        return builder.create();
    }

    @Override
    public void onStart() {
        super.onStart();

        // Disable OK button when adding a new tag
        if (mTag.getId() == Tag.NO_ID) {
            ((AlertDialog) getDialog())
                    .getButton(DialogInterface.BUTTON_NEUTRAL)
                    .setEnabled(false);
        }

    }

    private void showKeyboard() {
        InputMethodManager imm = (InputMethodManager) getActivity()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getActivity()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(
                mTagEditAutoCompleteTextView.getWindowToken(), 0);
    }

    private void updatePassword() {
        if (mTagEditAutoCompleteTextView.length() > 0 &&
                mMasterKeyEditText.length() > 0) {
            Profile profile =
                    ProfileSettings.getProfile(getActivity(), mProfileId);
            String password = PasswordHasher.hashPassword(mTag.getName(),
                    SecurePassword.getPassword(mMasterKeyEditText.getText()),
                    profile.getPrivateKey(), mTag.getPasswordLength(),
                    mTag.getPasswordType());
            mPasswordTextView.setText(password);
        } else {
            mPasswordTextView.setText("");
        }
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
            // Warning: this code is called before the dialog show() method
            // is called, and getDialog() can return a null value
            AlertDialog dialog = (AlertDialog) getDialog();

            // Check whether the tag already exists
            String tagName = mTagEditAutoCompleteTextView.getText().toString();
            if (dialog != null) {
                Button okButton =
                        dialog.getButton(DialogInterface.BUTTON_NEUTRAL);
                if (tagName.length() == 0) {
                    okButton.setEnabled(false);
                } else {
                    Tag storedTag = TagSettings
                            .getTag(getActivity(), mProfileId, tagName);
                    if (storedTag.getId() != Tag.NO_ID &&
                            storedTag.getId() != mTag.getId()) {
                        // Show error: the tag already exists
                        mTagEditAutoCompleteTextView
                                .setError(getString(R.string.error_tag_exists));
                        okButton.setEnabled(false);
                    } else {
                        mTagEditAutoCompleteTextView.setError(null);
                        okButton.setEnabled(true);
                    }
                }
            }

            // Store master key in the case that it is cached
            if (mCacheMasterKey) {
                HashMyPassApplication.setCachedMasterKey(SecurePassword
                        .getPassword(mMasterKeyEditText.getText()));
            }

            // Update favicon
            mTag.setName(mTagEditAutoCompleteTextView.getText().toString());
            if (mTag.getSite() == null) {
                FaviconLoader
                        .setAsBackground(getActivity(), mFaviconTextView, mTag);
            }

            // Update identicon
            mIdenticonImageView.setVisibility(View.INVISIBLE);
            if (mTask != null &&
                    mTask.getStatus() == AsyncTask.Status.RUNNING) {
                mTask.cancel(true);
            }
            if (mMasterKeyEditText.length() > 0) {
                mTask = new IdenticonGenerationTask(getActivity(),
                        GeneratePasswordDialogFragment.this);
                mTask.execute(SecurePassword
                        .getPassword(mMasterKeyEditText.getText()));
            }

            // Update password
            updatePassword();

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

    @Override
    public void onIconGenerated(Bitmap bitmap) {
        mIdenticonImageView.setImageBitmap(bitmap);
        mIdenticonImageView.setVisibility(View.VISIBLE);
        mTask = null;
    }

    public interface GeneratePasswordDialogListener {
        void onDialogDismiss(Tag tag);
    }

}
