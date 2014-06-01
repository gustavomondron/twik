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
import com.reddyetwo.hashmypass.app.data.TagSettings;

/**
 * Dialog fragment for editing tag settings.
 */
public class TagSettingsDialogFragment extends DialogFragment {

    private long mProfileId;
    private String mTag;

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

        // Inflate the layout
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_tag_settings, null);

        // Fill form and set button actions
        ContentValues tagValues =
                TagSettings.getTagSettings(getActivity(), mProfileId, mTag);

        Spinner passwordLengthSpinner =
                (Spinner) view.findViewById(R.id.tag_settings_password_length);
        ArrayAdapter<String> passwordLengthAdapter =
                new ArrayAdapter<String>(getActivity(),
                        android.R.layout.simple_spinner_item, new String[]{
                        tagValues.getAsString(
                                DataOpenHelper.COLUMN_TAGS_PASSWORD_LENGTH)}
                );
        passwordLengthAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        passwordLengthSpinner.setAdapter(passwordLengthAdapter);

        /* Open number picker dialog when the password length spinner is
           touched */
        passwordLengthSpinner.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    showPasswordLengthDialog();
                    return true;
                }

                return false;
            }
        });

        /* Populate password type spinner */
        Spinner passwordTypeSpinner =
                (Spinner) view.findViewById(R.id.tag_settings_password_type);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter
                .createFromResource(getActivity(), R.array.password_types_array,
                        android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        passwordTypeSpinner.setAdapter(adapter);
        passwordTypeSpinner.setSelection(tagValues
                .getAsInteger(DataOpenHelper.COLUMN_TAGS_PASSWORD_TYPE));

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

    private void showPasswordLengthDialog() {
        // TODO
    }

    private void saveTagSettings() {
        // TODO
    }

}
