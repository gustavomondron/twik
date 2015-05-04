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


package com.reddyetwo.hashmypass.app.dialog;

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

import com.reddyetwo.hashmypass.app.TwikApplication;
import com.reddyetwo.hashmypass.app.IdenticonGenerationTask;
import com.reddyetwo.hashmypass.app.R;
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

/**
 * Dialog fragment which is used to generate and configure the password of a tag
 */
public class GeneratePasswordDialogFragment extends DialogFragment
        implements TagSettingsDialogFragment.OnTagSettingsSavedListener,
                   IdenticonGenerationTask.OnIconGeneratedListener {

    private static final String STATE_PROFILE_ID = "profileId";
    private static final String STATE_TAG = "tag";
    private static final String DIALOG_TAG_SETTINGS = "tagSettings";

    private long mProfileId;
    private Tag mTag;
    private IdenticonGenerationTask mTask;
    private GeneratePasswordDialogListener mListener;
    private PasswordTextWatcher mPasswordTextWatcher;

    private TextView mFaviconTextView;
    private EditText mTagEditText;
    private EditText mMasterKeyEditText;
    private TextView mPasswordTextView;
    private ImageButton mTagSettingsImageButton;
    private ImageView mIdenticonImageView;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Inflate the layout
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        initializeView(builder,
                View.inflate(getActivity(), R.layout.dialog_generate_password, null));

        initializeSettings(savedInstanceState);

        return builder.create();
    }

    @Override
    public void onResume() {
        super.onResume();
        populateView();
        updateViewState();
    }

    @Override
    public void onPause() {
        super.onPause();
        removeTextChangedListeners();
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
    public void onTagSettingsSaved(Tag tag) {
        mTag = tag;
        updatePassword();
    }

    /**
     * Generate key identicon
     */
    private void generateIdenticon() {
        if (mTask != null && mTask.getStatus() == AsyncTask.Status.RUNNING) {
            mTask.cancel(true);
        }

        mTask = new IdenticonGenerationTask(getActivity(), this);
        mTask.execute(SecurePassword.getPassword(mMasterKeyEditText.getText()));
    }

    @Override
    public void onIconGenerated(Bitmap bitmap) {
        if (bitmap != null) {
            mIdenticonImageView.setImageBitmap(bitmap);
            mIdenticonImageView.setVisibility(View.VISIBLE);
        } else {
            mIdenticonImageView.setVisibility(View.INVISIBLE);
        }
        mTask = null;
    }

    /**
     * Set the profile ID
     *
     * @param profileId the profile ID
     */
    public void setProfileId(long profileId) {
        mProfileId = profileId;
    }

    /**
     * Set the tag
     *
     * @param tag the tag
     */
    public void setTag(Tag tag) {
        mTag = tag;
    }

    /**
     * Set the {@link com.reddyetwo.hashmypass.app.dialog.GeneratePasswordDialogFragment.GeneratePasswordDialogListener},
     * which is notified when the OK button has been clicked
     *
     * @param listener the dialog listener
     */
    public void setDialogOkListener(GeneratePasswordDialogListener listener) {
        mListener = listener;
    }

    private void initializeSettings(Bundle savedInstanceState) {
        // Restore tag if the device configuration has changed
        if (savedInstanceState != null) {
            mProfileId = savedInstanceState.getLong(STATE_PROFILE_ID);
            mTag = savedInstanceState.getParcelable(STATE_TAG);
        }
    }

    private void initializeView(AlertDialog.Builder builder, View view) {
        builder.setView(view);

        mFaviconTextView = (TextView) view.findViewById(R.id.tag_favicon);
        FaviconLoader.setAsBackground(getActivity(), mFaviconTextView, mTag);

        mTagEditText = (EditText) view.findViewById(R.id.tag_text);
        mMasterKeyEditText = (EditText) view.findViewById(R.id.master_key_text);

        mPasswordTextView = (TextView) view.findViewById(R.id.website_password);
        Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), Constants.FONT_MONOSPACE);
        mPasswordTextView.setTypeface(tf);

        mTagSettingsImageButton = (ImageButton) view.findViewById(R.id.tag_settings);
        mIdenticonImageView = (ImageView) view.findViewById(R.id.identicon);

        addDialogButtonClickedListener(builder);
        addPasswordClickedListener();
        addTagSettingsClickedListener();
    }

    private void populateView() {
        // Populate fields
        mTagEditText.setText(mTag.getName());

        // Add text changed listeners for tag name and master key
        addTextChangedListeners();

        // Clear master key EditText and restore cached master key
        TwikApplication application = TwikApplication.getInstance();
        mMasterKeyEditText.setText(application.getCachedMasterKey(), 0,
                application.getCachedMasterKey().length);
    }

    private void updateViewState() {
        // Disable OK button when adding a new tag
        if (mTag.getId() == Tag.NO_ID) {
            ((AlertDialog) getDialog()).getButton(DialogInterface.BUTTON_NEUTRAL).setEnabled(false);
        }

        // Manage focus
        if (mTag.getId() != Tag.NO_ID) {
            // Tag name already populated
            mMasterKeyEditText.requestFocus();
        } else {
            mTagEditText.requestFocus();
        }

        // Manage keyboard status
        if (mMasterKeyEditText.length() == 0 || mTagEditText.length() == 0) {
            KeyboardManager.show(getActivity());
        }
    }

    private void addDialogButtonClickedListener(AlertDialog.Builder builder) {
        builder.setPositiveButton(android.R.string.ok, new DialogButtonClickedListener());
    }

    private void addPasswordClickedListener() {
        mPasswordTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPasswordTextView.length() > 0) {
                    ClipboardHelper.copyToClipboard(getActivity(),
                            ClipboardHelper.CLIPBOARD_LABEL_PASSWORD,
                            mPasswordTextView.getText().toString(), R.string.copied_to_clipboard);
                }
            }
        });
    }

    private void addTagSettingsClickedListener() {
        mTagSettingsImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TagSettingsDialogFragment settingsDialog = new TagSettingsDialogFragment();
                settingsDialog.setProfileId(mProfileId);
                settingsDialog.setTag(mTag);
                settingsDialog.setTagSettingsSavedListener(GeneratePasswordDialogFragment.this);
                settingsDialog.show(getFragmentManager(), DIALOG_TAG_SETTINGS);
            }
        });
    }

    private void addTextChangedListeners() {
        mPasswordTextWatcher = new PasswordTextWatcher();
        mTagEditText.addTextChangedListener(mPasswordTextWatcher);
        mMasterKeyEditText.addTextChangedListener(mPasswordTextWatcher);
    }

    private void removeTextChangedListeners() {
        mTagEditText.removeTextChangedListener(mPasswordTextWatcher);
        mMasterKeyEditText.removeTextChangedListener(mPasswordTextWatcher);
    }

    private void updatePassword() {
        if (mTagEditText.length() > 0 && mMasterKeyEditText.length() > 0) {
            Profile profile = ProfileSettings.getProfile(getActivity(), mProfileId);
            String password = PasswordHasher.hashTagWithKeys(mTag.getName(),
                    SecurePassword.getPassword(mMasterKeyEditText.getText()),
                    profile.getPrivateKey(), mTag.getPasswordLength(), mTag.getPasswordType());
            mPasswordTextView.setText(password);
        } else {
            mPasswordTextView.setText("");
        }
    }

    /**
     * Interface which can be implemented to listen to dialog dismissed events.
     */
    public interface GeneratePasswordDialogListener {

        /**
         * Method called when the dialog has been dismissed
         *
         * @param tag the {@link Tag} instance
         */
        public void onDialogDismiss(Tag tag);
    }

    private class DialogButtonClickedListener implements DialogInterface.OnClickListener {

        @Override
        public void onClick(android.content.DialogInterface dialog, int which) {

            // Copy password to clipboard
            if (Preferences.getCopyToClipboard(getActivity()) && mPasswordTextView.length() > 0) {
                ClipboardHelper
                        .copyToClipboard(getActivity(), ClipboardHelper.CLIPBOARD_LABEL_PASSWORD,
                                mPasswordTextView.getText().toString(),
                                R.string.copied_to_clipboard);
            }

            // Hide keyboard
            KeyboardManager.hide(getActivity(), mTagEditText);

            // Call listener
            mListener.onDialogDismiss(mTag);
        }
    }

    private class PasswordTextWatcher implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // Nothing to do
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
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
                Button okButton = dialog.getButton(DialogInterface.BUTTON_NEUTRAL);
                if (tagName.length() == 0) {
                    okButton.setEnabled(false);
                } else {
                    Tag storedTag = TagSettings.getTag(getActivity(), mProfileId, tagName);
                    if (storedTag.getId() != Tag.NO_ID && storedTag.getId() != mTag.getId()) {
                        // Show error: the tag already exists
                        mTagEditText.setError(getString(R.string.error_tag_exists));
                        okButton.setEnabled(false);
                    } else {
                        mTagEditText.setError(null);
                        okButton.setEnabled(true);
                    }
                }
            }

            // Cache master key
            TwikApplication.getInstance()
                    .cacheMasterKey(SecurePassword.getPassword(mMasterKeyEditText.getText()));

            // Show favicon
            mTag.setName(mTagEditText.getText().toString());
            if (mTag.getSite() == null) {
                FaviconLoader.setAsBackground(getActivity(), mFaviconTextView, mTag);
            }

            generateIdenticon();
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
}
