/*
 * Copyright 2014 Red Dye No. 2
 *
 * This file is part of Twik.
 *
 * Twik is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Twik is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Twik.  If not, see <http://www.gnu.org/licenses/>.
 */


package com.reddyetwo.hashmypass.app;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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
import com.reddyetwo.hashmypass.app.util.KeyboardManager;
import com.reddyetwo.hashmypass.app.util.SecurePassword;

public class GeneratePasswordDialogFragment extends DialogFragment
        implements TagSettingsDialogFragment.OnTagSettingsSavedListener,
        IdenticonGenerationTask.OnIconGeneratedListener {

    private static final String STATE_PROFILE_ID = "profileId";
    private static final String STATE_TAG = "tag";

    private long mProfileId;
    private Tag mTag;
    private boolean mCacheMasterKey;
    private IdenticonGenerationTask mTask;
    private GeneratePasswordDialogListener mListener;

    private TextView mFaviconTextView;
    private EditText mTagEditText;
    private EditText mMasterKeyEditText;
    private TextView mPasswordTextView;
    private ImageButton mTagSettingsImageButton;
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
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(STATE_TAG, mTag);
        outState.putLong(STATE_PROFILE_ID, mProfileId);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Inflate the layout
        View view = View.inflate(getActivity(), R.layout.dialog_generate_password, null);
        builder.setView(view);
        builder.setPositiveButton(android.R.string.ok,
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
                        KeyboardManager.hide(getActivity(), mTagEditText);

                        // Call listener
                        mListener.onDialogDismiss(mTag);
                    }
                });

        mFaviconTextView = (TextView) view.findViewById(R.id.tag_favicon);
        FaviconLoader.setAsBackground(getActivity(), mFaviconTextView, mTag);

        mTagEditText = (EditText) view.findViewById(R.id.tag_text);

        mMasterKeyEditText = (EditText) view.findViewById(R.id.master_key_text);

        // Restore tag if the device configuration has changed
        if (savedInstanceState != null) {
            mProfileId = savedInstanceState.getLong(STATE_PROFILE_ID);
            mTag = savedInstanceState.getParcelable(STATE_TAG);
        }

        // Manage focus
        if (mTag.getId() != Tag.NO_ID) {
            // Tag name already populated
            mMasterKeyEditText.requestFocus();
        } else {
            mTagEditText.requestFocus();
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

        mTagSettingsImageButton =
                (ImageButton) view.findViewById(R.id.tag_settings);
        mTagSettingsImageButton.setOnClickListener(new View.OnClickListener() {
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
        mTagEditText.addTextChangedListener(watcher);
        mMasterKeyEditText.addTextChangedListener(watcher);

        // Populate fields
        mTagEditText.setText(mTag.getName());

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

        // Restore cached master key
        mCacheMasterKey =
                Preferences.getRememberMasterKeyMins(getActivity()) > 0;
        if (mCacheMasterKey) {
            mMasterKeyEditText
                    .setText(HashMyPassApplication.getCachedMasterKey(), 0,
                            HashMyPassApplication.getCachedMasterKey().length);

        } else {
            mMasterKeyEditText.setText("");
        }


        // Manage keyboard status
        if (mMasterKeyEditText.length() == 0 || mTagEditText.length() == 0) {
            KeyboardManager.show(getActivity());
        }
    }

    private void updatePassword() {
        if (mTagEditText.length() > 0 && mMasterKeyEditText.length() > 0) {
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
            // Nothing to do
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before,
                                  int count) {
            // Nothing to do
        }

        @Override
        public void afterTextChanged(Editable s) {
            // Warning: this code is called before the dialog show() method
            // is called, and getDialog() can return a null value
            AlertDialog dialog = (AlertDialog) getDialog();

            // Check whether the tag already exists
            String tagName = mTagEditText.getText().toString();
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
                        mTagEditText
                                .setError(getString(R.string.error_tag_exists));
                        okButton.setEnabled(false);
                    } else {
                        mTagEditText.setError(null);
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
            mTag.setName(mTagEditText.getText().toString());
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

            // Update tag settings button visibility
            if (mTagEditText.getText().length() > 0) {
                mTagSettingsImageButton.setVisibility(View.VISIBLE);
                mTagSettingsImageButton.setEnabled(true);
            } else {
                mTagSettingsImageButton.setVisibility(View.INVISIBLE);
                mTagSettingsImageButton.setEnabled(false);
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
