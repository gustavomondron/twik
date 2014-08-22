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

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.reddyetwo.hashmypass.app.data.PasswordType;
import com.reddyetwo.hashmypass.app.data.Profile;
import com.reddyetwo.hashmypass.app.data.ProfileSettings;
import com.reddyetwo.hashmypass.app.util.ProfileFormInflater;
import com.reddyetwo.hashmypass.app.util.ProfileFormWatcher;

public class EditProfileActivity extends Activity {

    public static final String EXTRA_PROFILE_ID = "profile_id";

    // State bundle keys
    private static final String KEY_PASSWORD_LENGTH = "password_length";

    // UI Widgets
    private EditText mNameEditText;
    private EditText mPrivateKeyEditText;
    private Spinner mPasswordLengthSpinner;
    private Spinner mPasswordTypeSpinner;

    // Activity status
    private long mProfileId;
    private String mOriginalName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_edit_profile);
        mProfileId = getIntent().getLongExtra(EXTRA_PROFILE_ID, -1);

        // Load profile from database
        Profile profile = ProfileSettings.getProfile(this, mProfileId);
        if (profile == null) {
            // Profile not found
            NavUtils.navigateUpFromSameTask(EditProfileActivity.this);
            return;
        }

        // Setup action bar
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
            getActionBar().setSubtitle(profile.getName());
        }

        mPasswordTypeSpinner =
                (Spinner) findViewById(R.id.password_type_spinner);
        Button discardButton = (Button) findViewById(R.id.discard_button);
        Button saveButton = (Button) findViewById(R.id.save_button);

        // Populate UI widgets
        mOriginalName = profile.getName();
        mNameEditText = (EditText) findViewById(R.id.profile_name_text);
        mNameEditText.setText(mOriginalName);

        mPrivateKeyEditText = (EditText) findViewById(R.id.private_key_text);
        mPrivateKeyEditText.setText(profile.getPrivateKey());

        // Populating password length spinner is a bit more tricky
        // We have to restore its value from savedInstanceState...
        mPasswordLengthSpinner =
                (Spinner) findViewById(R.id.password_length_spinner);
        int passwordLength;
        if (savedInstanceState != null) {
            passwordLength = savedInstanceState.getInt(KEY_PASSWORD_LENGTH);
        } else {
            passwordLength = profile.getPasswordLength();
        }
        ProfileFormInflater
                .populatePasswordLengthSpinner(this, mPasswordLengthSpinner,
                        passwordLength);
        // Show number picker dialog when the spinner is touched
        mPasswordLengthSpinner.setOnTouchListener(
                new MovementTouchListener(this,
                        new MovementTouchListener.OnPressedListener() {
                            @Override
                            public void onPressed() {
                                showDialog();
                            }
                        }));

        mPasswordTypeSpinner =
                (Spinner) findViewById(R.id.password_type_spinner);
        ProfileFormInflater
                .populatePasswordTypeSpinner(this, mPasswordTypeSpinner,
                        profile.getPasswordType());

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Update profile in the database
                Profile profile = new Profile(mProfileId,
                        mNameEditText.getText().toString(),
                        mPrivateKeyEditText.getText().toString(),
                        Integer.decode((String) mPasswordLengthSpinner
                                .getSelectedItem()),
                        PasswordType.values()[mPasswordTypeSpinner
                                .getSelectedItemPosition()]);
                ProfileSettings
                        .updateProfile(EditProfileActivity.this, profile);

                // Go to the parent activity
                NavUtils.navigateUpFromSameTask(EditProfileActivity.this);
            }
        });

        discardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavUtils.navigateUpFromSameTask(EditProfileActivity.this);
            }
        });

        // Add form watcher for enabling/disabling Save button
        ProfileFormWatcher profileFormWatcher =
                new ProfileFormWatcher(getApplicationContext(), mProfileId,
                        mNameEditText, mPrivateKeyEditText, saveButton);
        mNameEditText.addTextChangedListener(profileFormWatcher);
        mPrivateKeyEditText.addTextChangedListener(profileFormWatcher);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_PASSWORD_LENGTH, Integer.parseInt(
                        (String) mPasswordLengthSpinner.getSelectedItem()));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.edit_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
            return true;
        } else if (id == R.id.action_delete) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.confirm_delete, mOriginalName));
            builder.setMessage(R.string.warning_lose_settings);
            builder.setPositiveButton(R.string.action_delete,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ProfileSettings.deleteProfile(EditProfileActivity
                                    .this, mProfileId);
                            NavUtils.navigateUpFromSameTask(
                                    EditProfileActivity.this);
                        }
                    });
            builder.setNegativeButton(android.R.string.cancel, null);
            builder.create().show();

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /* Shows a number picker dialog for choosing the password length */
    private void showDialog() {
        PasswordLengthDialogFragment dialogFragment =
                new PasswordLengthDialogFragment();
        dialogFragment.setPasswordLength(Integer.parseInt(
                (String) mPasswordLengthSpinner.getSelectedItem()));
        dialogFragment.setOnSelectedListener(
                new PasswordLengthDialogFragment.OnSelectedListener() {
                    @Override
                    public void onPasswordLengthSelected(int length) {
                        ProfileFormInflater.populatePasswordLengthSpinner(
                                EditProfileActivity.this,
                                mPasswordLengthSpinner, length);
                    }
                });

        dialogFragment.show(getFragmentManager(), "passwordLength");
    }

}
