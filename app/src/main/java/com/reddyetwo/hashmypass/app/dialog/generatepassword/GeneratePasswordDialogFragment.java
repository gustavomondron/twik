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

package com.reddyetwo.hashmypass.app.dialog.generatepassword;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.reddyetwo.hashmypass.app.IdenticonGenerationTask;
import com.reddyetwo.hashmypass.app.R;
import com.reddyetwo.hashmypass.app.TwikApplication;
import com.reddyetwo.hashmypass.app.data.Preferences;
import com.reddyetwo.hashmypass.app.data.Profile;
import com.reddyetwo.hashmypass.app.data.ProfileSettings;
import com.reddyetwo.hashmypass.app.data.Tag;
import com.reddyetwo.hashmypass.app.data.TagSettings;
import com.reddyetwo.hashmypass.app.dialog.TagSettingsDialogFragment;
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
    private GeneratePasswordDialogWatcher mGeneratePasswordDialogWatcher;

    private AlertDialog mAlertDialog;
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

        mAlertDialog = builder.create();
        mAlertDialog.setCanceledOnTouchOutside(false);
        return mAlertDialog;
    }

    @Override
    public void onResume() {
        super.onResume();
        populateView();
        updateViewState();
        manageKeyboardVisibility();
    }

    /**
     * Shows the soft keyboard and set the focus to the appropriate TextView in the case that
     * the tag name or the master password are empty.
     */
    private void manageKeyboardVisibility() {
        // Manage keyboard status
        if (mTagEditText.length() == 0) {
            KeyboardManager.show(mAlertDialog.getWindow(), mTagEditText);
        } else if (mMasterKeyEditText.length() == 0) {
            KeyboardManager.show(mAlertDialog.getWindow(), mMasterKeyEditText);
        }
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
        updateGeneratedPassword();
    }

    private void updateGeneratedPassword() {
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
     * Set the {@link GeneratePasswordDialogFragment.GeneratePasswordDialogListener},
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
            ((AlertDialog) getDialog()).getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
        }

        // Manage focus
        if (mTag.getId() != Tag.NO_ID) {
            // Tag name already populated
            mMasterKeyEditText.requestFocus();
        } else {
            mTagEditText.requestFocus();
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
        mGeneratePasswordDialogWatcher = new GeneratePasswordDialogWatcher(buildDialogChangedListener());
        mTagEditText.addTextChangedListener(mGeneratePasswordDialogWatcher);
        mMasterKeyEditText.addTextChangedListener(mGeneratePasswordDialogWatcher);
    }

    private GeneratePasswordDialogWatcher.OnDialogChangedListener buildDialogChangedListener() {
        return new GeneratePasswordDialogWatcher.OnDialogChangedListener() {
            @Override
            public void onDialogChanged() {
                updateDialog();
            }
        };
    }

    private void updateDialog() {
        updateOkButtonStatus();
        updateTagSettingsButtonStatus();
        updateTagErrorStatus();
        updateTagName();
        updateFavicon();
        updateIdenticon();
        updateGeneratedPassword();
        cacheMasterKey();
    }

    private void updateOkButtonStatus() {
        getOkButton().setEnabled(tagIsValid());
    }

    private Button getOkButton() {
        return mAlertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
    }

    private boolean tagIsValid() {
        String tagName = getTagName();
        return !tagName.isEmpty() && !tagNameExists(tagName);
    }

    private String getTagName() {
        return mTagEditText.getText().toString();
    }

    private boolean tagNameExists(String tagName) {
        Tag storedTag = getTag(tagName);
        return storedTag.getId() != Tag.NO_ID && storedTag.getId() != mTag.getId();
    }

    private Tag getTag(String tagName) {
        return TagSettings.getTag(getActivity(), mProfileId, tagName);
    }

    private void updateTagSettingsButtonStatus() {
        if (tagIsValid()) {
            enableTagSettingsButton();
        } else {
            disableTagSettingsButton();
        }
    }

    private void enableTagSettingsButton() {
        mTagSettingsImageButton.setVisibility(View.VISIBLE);
        mTagSettingsImageButton.setEnabled(true);
    }

    private void disableTagSettingsButton() {
        mTagSettingsImageButton.setVisibility(View.INVISIBLE);
        mTagSettingsImageButton.setEnabled(false);
    }

    private void updateTagErrorStatus() {
        String error = null;
        if (tagNameExists(getTagName())) {
            error = getString(R.string.error_tag_exists);
        }
        mTagEditText.setError(error);
    }

    private void cacheMasterKey() {
        TwikApplication.getInstance()
                .cacheMasterKey(SecurePassword.getPassword(mMasterKeyEditText.getText()));
    }

    private void updateTagName() {
        mTag.setName(mTagEditText.getText().toString());
    }

    private void updateFavicon() {
        if (mTag.getSite() == null) {
            FaviconLoader.setAsBackground(getActivity(), mFaviconTextView, mTag);
        }
    }

    private void updateIdenticon() {
        if (mTask != null && mTask.getStatus() == AsyncTask.Status.RUNNING) {
            mTask.cancel(true);
        }

        mTask = new IdenticonGenerationTask(getActivity(), this);
        mTask.execute(SecurePassword.getPassword(mMasterKeyEditText.getText()));
    }

    private void removeTextChangedListeners() {
        mTagEditText.removeTextChangedListener(mGeneratePasswordDialogWatcher);
        mMasterKeyEditText.removeTextChangedListener(mGeneratePasswordDialogWatcher);
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
        void onDialogDismiss(Tag tag);
    }

    private class DialogButtonClickedListener implements DialogInterface.OnClickListener {

        @Override
        public void onClick(android.content.DialogInterface dialog, int which) {
            copyPasswordToClipboardIfEnabled();
            KeyboardManager.hide(getActivity(), mTagEditText);
            mListener.onDialogDismiss(mTag);
        }
    }

    private void copyPasswordToClipboardIfEnabled() {
        if (Preferences.getCopyToClipboard(getActivity()) && mPasswordTextView.length() > 0) {
            ClipboardHelper
                    .copyToClipboard(getActivity(), ClipboardHelper.CLIPBOARD_LABEL_PASSWORD,
                            mPasswordTextView.getText().toString(),
                            R.string.copied_to_clipboard);
        }
    }

}
