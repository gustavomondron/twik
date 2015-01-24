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

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.reddyetwo.hashmypass.app.data.PasswordLength;
import com.reddyetwo.hashmypass.app.data.PasswordType;
import com.reddyetwo.hashmypass.app.data.Profile;
import com.reddyetwo.hashmypass.app.data.ProfileSettings;
import com.reddyetwo.hashmypass.app.util.KeyboardManager;
import com.reddyetwo.hashmypass.app.util.ProfileFormInflater;
import com.reddyetwo.hashmypass.app.util.ProfileFormWatcher;
import com.reddyetwo.hashmypass.app.util.RandomPrivateKeyGenerator;
import com.reddyetwo.hashmypass.app.views.MaterialColorPalette;

public class AddProfileActivity extends ActionBarActivity {

    // Result codes
    public static final String RESULT_KEY_PROFILE_ID = "profile_id";

    // State bundle keys
    private static final String KEY_PASSWORD_LENGTH = "password_length";

    // UI Widgets
    private EditText mNameEditText;
    private EditText mPrivateKeyEditText;
    private Spinner mPasswordTypeSpinner;
    private Spinner mPasswordLengthSpinner;

    private int mColor = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_profile);

        // Add and setup toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        /* Get UI widgets */
        mNameEditText = (EditText) findViewById(R.id.profile_name_text);
        mPrivateKeyEditText = (EditText) findViewById(R.id.private_key_text);


        // Populating password length spinner is a bit more tricky
        // We have to restore its value from savedInstanceState...
        mPasswordLengthSpinner = (Spinner) findViewById(R.id.password_length_spinner);
        int passwordLength;
        if (savedInstanceState != null) {
            passwordLength = savedInstanceState.getInt(KEY_PASSWORD_LENGTH);
        } else {
            passwordLength = PasswordLength.DEFAULT;
        }
        ProfileFormInflater
                .populatePasswordLengthSpinner(this, mPasswordLengthSpinner, passwordLength);
        // Show number picker dialog when the spinner is touched
        mPasswordLengthSpinner.setOnTouchListener(
                new MovementTouchListener(this, new MovementTouchListener.OnPressedListener() {
                    @Override
                    public void onPressed() {
                        showDialog();
                    }
                }));

        mPasswordTypeSpinner = (Spinner) findViewById(R.id.password_type_spinner);
        ProfileFormInflater.populatePasswordTypeSpinner(this, mPasswordTypeSpinner,
                PasswordType.ALPHANUMERIC_AND_SPECIAL_CHARS);

        Button addButton = (Button) findViewById(R.id.add_button);
        addButton.setOnClickListener(new AddProfileButtonOnClickListener());

        Button discardButton = (Button) findViewById(R.id.discard_button);
        discardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_CANCELED);

                // Navigate to previous activity
                NavUtils.navigateUpFromSameTask(AddProfileActivity.this);
            }
        });

        // Add form watcher for enabling/disabling Add button
        ProfileFormWatcher profileFormWatcher =
                new ProfileFormWatcher(getApplicationContext(), Profile.NO_ID, mNameEditText,
                        mPrivateKeyEditText, addButton);
        mNameEditText.addTextChangedListener(profileFormWatcher);
        mPrivateKeyEditText.addTextChangedListener(profileFormWatcher);
        mPrivateKeyEditText.setText(RandomPrivateKeyGenerator.generate());

        MaterialColorPalette colorPalette =
                (MaterialColorPalette) findViewById(R.id.profile_color_palette);
        colorPalette.setOnColorSelectedListener(new MaterialColorPalette.OnColorSelectedListener() {
            @Override
            public void onColorSelected(int color) {
                mColor = color;
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_PASSWORD_LENGTH,
                Integer.parseInt((String) mPasswordLengthSpinner.getSelectedItem()));
    }

    @Override
    protected void onPause() {
        super.onPause();
        KeyboardManager.hide(this, getCurrentFocus());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            setResult(RESULT_CANCELED);
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /* Shows a number picker dialog for choosing the password length */
    private void showDialog() {
        PasswordLengthDialogFragment dialogFragment = new PasswordLengthDialogFragment();
        dialogFragment.setPasswordLength(
                Integer.parseInt((String) mPasswordLengthSpinner.getSelectedItem()));
        dialogFragment.setOnSelectedListener(new PasswordLengthDialogFragment.OnSelectedListener() {
            @Override
            public void onPasswordLengthSelected(int length) {
                ProfileFormInflater.populatePasswordLengthSpinner(AddProfileActivity.this,
                        mPasswordLengthSpinner, length);
            }
        });

        dialogFragment.show(getFragmentManager(), "passwordLength");
    }

    private class AddProfileButtonOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Profile profile = new Profile(Profile.NO_ID, mNameEditText.getText().toString(),
                    mPrivateKeyEditText.getText().toString(),
                    Integer.decode((String) mPasswordLengthSpinner.getSelectedItem()),
                    PasswordType.values()[mPasswordTypeSpinner.getSelectedItemPosition()],
                    mColor);
            long profileId = ProfileSettings.insertProfile(AddProfileActivity.this, profile);
            if (profileId == -1) {
                // Error!
                Toast.makeText(AddProfileActivity.this, R.string.error, Toast.LENGTH_LONG)
                        .show();
                setResult(RESULT_CANCELED);
            } else {
                Intent resultIntent = new Intent();
                resultIntent.putExtra(RESULT_KEY_PROFILE_ID, profileId);
                setResult(RESULT_OK, resultIntent);
            }

            // Navigate to previous activity
            NavUtils.navigateUpFromSameTask(AddProfileActivity.this);
        }
    }
}