package com.reddyetwo.hashmypass.app;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.reddyetwo.hashmypass.app.data.DataOpenHelper;
import com.reddyetwo.hashmypass.app.data.PasswordType;
import com.reddyetwo.hashmypass.app.data.TagSettings;

/**
 * Dialog fragment for editing tag settings.
 */
public class TagSettingsDialogFragment extends DialogFragment {

    private static final String KEY_PROFILE_ID = "profile_id";
    private static final String KEY_TAG = "tag";
    private static final String KEY_TAG_ID = "tag_id";
    private static final String KEY_PASSWORD_LENGTH = "password_length";
    private static final String KEY_PASSWORD_TYPE = "password_type";

    // Dialog state
    private long mProfileId;
    private long mTagId;
    private String mTag;
    private int mPasswordLength;
    private int mPasswordType;

    // UI widgets
    private Spinner mPasswordLengthSpinner;
    private Spinner mPasswordTypeSpinner;

    /**
     * Sets the tag whose settings are to be edited with this dialog. Note that
     * this "tag" is not the same as {@link #getTag()}, which belongs to the
     * {@link android.app.Fragment} class.
     */
    public void setTag(String tag) {
        this.mTag = tag;
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

        if (savedInstanceState != null) {
            // Restore the state (e.g. when the screen is rotated)
            mProfileId = savedInstanceState.getLong(KEY_PROFILE_ID);
            mTag = savedInstanceState.getString(KEY_TAG);
            mTagId = savedInstanceState.getLong(KEY_TAG_ID);
            mPasswordLength = savedInstanceState.getInt(KEY_PASSWORD_LENGTH);
            mPasswordType = savedInstanceState.getInt(KEY_PASSWORD_TYPE);
        } else {
            // No previous state, get data from storage
            ContentValues tagValues =
                    TagSettings.getTagSettings(getActivity(), mProfileId, mTag);
            Integer tagId = tagValues.getAsInteger(DataOpenHelper.COLUMN_ID);
            mTagId = tagId != null ? tagId : -1; // -1 => no previous data
            mPasswordLength = tagValues
                    .getAsInteger(DataOpenHelper.COLUMN_TAGS_PASSWORD_LENGTH);
            mPasswordType = tagValues
                    .getAsInteger(DataOpenHelper.COLUMN_TAGS_PASSWORD_TYPE);
        }

        // Inflate the layout
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_tag_settings, null);

        // Get spinner widgets
        mPasswordLengthSpinner =
                (Spinner) view.findViewById(R.id.tag_settings_password_length);
        mPasswordTypeSpinner =
                (Spinner) view.findViewById(R.id.tag_settings_password_type);

        /* Open number picker dialog when the password length spinner is
           touched */
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

        populatePasswordLengthSpinner(mPasswordLength);

        /* Populate password type spinner */
        Spinner passwordTypeSpinner =
                (Spinner) view.findViewById(R.id.tag_settings_password_type);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter
                .createFromResource(getActivity(), R.array.password_types_array,
                        android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        passwordTypeSpinner.setAdapter(adapter);
        passwordTypeSpinner.setSelection(mPasswordType);

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
        outState.putString(KEY_TAG, mTag);
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
                        populatePasswordLengthSpinner(length);
                    }
                }
        );

        dialogFragment.show(getFragmentManager(), "passwordLength");
    }

    private void populatePasswordLengthSpinner(int length) {
        ArrayAdapter<String> passwordLengthAdapter =
                new ArrayAdapter<String>(getActivity(),
                        android.R.layout.simple_spinner_item,
                        new String[]{String.valueOf(length)});
        passwordLengthAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        mPasswordLengthSpinner.setAdapter(passwordLengthAdapter);
    }

    private void saveTagSettings() {
        int passwordLength = Integer.parseInt(
                (String) mPasswordLengthSpinner.getSelectedItem());
        PasswordType passwordType = PasswordType.values()[mPasswordTypeSpinner
                .getSelectedItemPosition()];

        if (mTagId == -1) {
            TagSettings.insertTagSettings(getActivity(), mTag, mProfileId,
                    passwordLength, passwordType);
        } else {
            TagSettings.updateTagSettings(getActivity(), mTagId, passwordLength,
                    passwordType);
        }
    }

}
