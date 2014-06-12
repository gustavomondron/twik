package com.reddyetwo.hashmypass.app;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Spinner;

import com.reddyetwo.hashmypass.app.data.PasswordType;
import com.reddyetwo.hashmypass.app.data.Tag;
import com.reddyetwo.hashmypass.app.data.TagSettings;
import com.reddyetwo.hashmypass.app.util.ProfileFormInflater;

/**
 * Dialog fragment for editing tag settings.
 */
public class TagSettingsDialogFragment extends DialogFragment {

    private static final String KEY_PROFILE_ID = "profile_id";
    private static final String KEY_TAG_ID = "tag_id";
    private static final String KEY_PASSWORD_LENGTH = "password_length";
    private static final String KEY_PASSWORD_TYPE = "password_type";

    // Dialog state
    private long mProfileId;
    private long mTagId;
    private String mTagName;

    // UI widgets
    private Spinner mPasswordLengthSpinner;
    private Spinner mPasswordTypeSpinner;

    /**
     * Sets the tag whose settings are to be edited with this dialog. Note that
     * this "tag" is not the same as {@link #getTag()}, which belongs to the
     * {@link android.app.Fragment} class.
     */
    public void setTag(String tag) {
        this.mTagName = tag;
    }

    /**
     * Sets the profile id associate with the settings that are to be edited
     * with this dialog.
     */
    public void setProfileId(long mProfileId) {
        this.mProfileId = mProfileId;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.tag_settings));

        // Inflate the layout
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_tag_settings, null);

        int passwordLength;
        int passwordType;
        if (savedInstanceState != null) {
            // Restore the state (e.g. when the screen is rotated)
            mProfileId = savedInstanceState.getLong(KEY_PROFILE_ID);
            mTagId = savedInstanceState.getLong(KEY_TAG_ID);
            passwordLength = savedInstanceState.getInt(KEY_PASSWORD_LENGTH);
            passwordType = savedInstanceState.getInt(KEY_PASSWORD_TYPE);
        } else {
            // No previous state, get data from storage
            Tag tag = TagSettings.getTag(getActivity(), mProfileId, mTagName);
            mTagId = tag.getId();
            passwordLength = tag.getPasswordLength();
            passwordType = tag.getPasswordType().ordinal();
        }

        mPasswordLengthSpinner =
                (Spinner) view.findViewById(R.id.tag_settings_password_length);
        ProfileFormInflater.populatePasswordLengthSpinner(getActivity(),
                mPasswordLengthSpinner, passwordLength);
        mPasswordLengthSpinner.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    showPasswordLengthDialog();
                    return true;
                }

                return false;
            }
        });

        mPasswordTypeSpinner =
                (Spinner) view.findViewById(R.id.tag_settings_password_type);
        ProfileFormInflater.populatePasswordTypeSpinner(getActivity(),
                mPasswordTypeSpinner, PasswordType.values()[passwordType]);


        // Set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(view)
                // Add action buttons
                .setPositiveButton(R.string.save,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                saveTagSettings();
                                TagSettingsDialogFragment.this.getDialog()
                                        .cancel();
                            }
                        }
                ).setNegativeButton(R.string.discard,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        TagSettingsDialogFragment.this.getDialog().cancel();
                    }
                }
        );

        return builder.create();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(KEY_PROFILE_ID, mProfileId);
        outState.putLong(KEY_TAG_ID, mTagId);
        outState.putInt(KEY_PASSWORD_LENGTH, Integer.parseInt(
                        (String) mPasswordLengthSpinner.getSelectedItem())
        );
        outState.putInt(KEY_PASSWORD_TYPE,
                mPasswordTypeSpinner.getSelectedItemPosition());
    }

    /* Shows a number picker dialog for choosing the password length */
    private void showPasswordLengthDialog() {
        PasswordLengthDialogFragment dialogFragment =
                new PasswordLengthDialogFragment();
        dialogFragment.setPasswordLength(Integer.parseInt(
                (String) mPasswordLengthSpinner.getSelectedItem()));
        dialogFragment.setOnSelectedListener(
                new PasswordLengthDialogFragment.OnSelectedListener() {
                    @Override
                    public void onPasswordLengthSelected(int length) {
                        ProfileFormInflater
                                .populatePasswordLengthSpinner(getActivity(),
                                        mPasswordLengthSpinner, length);
                    }
                }
        );

        dialogFragment.show(getFragmentManager(), "passwordLength");
    }

    private void saveTagSettings() {
        int passwordLength = Integer.parseInt(
                (String) mPasswordLengthSpinner.getSelectedItem());
        PasswordType passwordType = PasswordType.values()[mPasswordTypeSpinner
                .getSelectedItemPosition()];

        Tag tag = new Tag(mTagId, mProfileId, null, mTagName, passwordLength,
                passwordType);
        if (mTagId == Tag.NO_ID) {
            // New tag
            TagSettings.insertTag(getActivity(), tag);
        } else {
            TagSettings.updateTag(getActivity(), tag);
        }
    }

}
