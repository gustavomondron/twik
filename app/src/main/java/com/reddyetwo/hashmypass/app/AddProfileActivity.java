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
import android.graphics.Typeface;
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

import com.reddyetwo.hashmypass.app.data.PasswordType;
import com.reddyetwo.hashmypass.app.data.Profile;
import com.reddyetwo.hashmypass.app.data.ProfileSettings;
import com.reddyetwo.hashmypass.app.dialog.PasswordLengthDialogFragment;
import com.reddyetwo.hashmypass.app.util.Constants;
import com.reddyetwo.hashmypass.app.util.KeyboardManager;
import com.reddyetwo.hashmypass.app.util.ProfileFormInflater;
import com.reddyetwo.hashmypass.app.util.ProfileFormWatcher;
import com.reddyetwo.hashmypass.app.util.RandomPrivateKeyGenerator;
import com.reddyetwo.hashmypass.app.view.MaterialColorPalette;

/**
 * Activity which allows adding a new profile
 */
public class AddProfileActivity extends ActionBarActivity {

    /**
     * Activity result key
     */
    public static final String RESULT_KEY_PROFILE_ID = "profile_id";

    /**
     * Key for saving/getting password length to/from saved instance state bundle
     */
    private static final String KEY_PASSWORD_LENGTH = "password_length";

    /**
     * Profile name {@link android.widget.EditText}
     */
    private EditText mNameEditText;

    /**
     * Private key {@link android.widget.EditText}
     */
    private EditText mPrivateKeyEditText;

    /**
     * Password type {@link android.widget.Spinner}
     */
    private Spinner mPasswordTypeSpinner;

    /**
     * Password length {@link android.widget.Spinner}
     */
    private Spinner mPasswordLengthSpinner;

    /**
     * {@link android.widget.Button} for adding profile
     */
    private Button mAddButton;

    /**
     * {@link android.widget.Button} for discarding changes
     */
    private Button mDiscardButton;

    /**
     * {@link com.reddyetwo.hashmypass.app.view.MaterialColorPalette} to pick a profile color
     */
    private MaterialColorPalette mColorPalette;

    /**
     * Password length selected when the activity is shown
     */
    private int mInitialPasswordLength;

    /**
     * Selected color index in the {@link com.reddyetwo.hashmypass.app.view.MaterialColorPalette} instance
     */
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

        initializeView();
        initializeSettings(savedInstanceState);
        populateView();
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

    private void initializeView() {
        mNameEditText = (EditText) findViewById(R.id.profile_name_text);

        // Use a monospaced typeface for private key which allows distinguishing 0 from O.
        mPrivateKeyEditText = (EditText) findViewById(R.id.private_key_text);
        Typeface monospacedTypeface =
                Typeface.createFromAsset(getAssets(), Constants.FONT_MONOSPACE);
        mPrivateKeyEditText.setTypeface(monospacedTypeface);

        mPasswordLengthSpinner = (Spinner) findViewById(R.id.password_length_spinner);
        mPasswordTypeSpinner = (Spinner) findViewById(R.id.password_type_spinner);
        mAddButton = (Button) findViewById(R.id.add_button);
        mDiscardButton = (Button) findViewById(R.id.discard_button);
        mColorPalette = (MaterialColorPalette) findViewById(R.id.profile_color_palette);

        addPasswordLengthTouchListener();
        addAddButtonClickListener();
        addDiscardButtonClickListener();
        addColorPaletteSelectedListener();
        addFormChangedListener();
    }

    private void initializeSettings(Bundle savedInstanceState) {
        mInitialPasswordLength =
                savedInstanceState != null ? savedInstanceState.getInt(KEY_PASSWORD_LENGTH) :
                        Constants.DEFAULT_PASSWORD_LENGTH;

    }

    private void populateView() {
        ProfileFormInflater.populatePasswordLengthSpinner(this, mPasswordLengthSpinner,
                mInitialPasswordLength);

        ProfileFormInflater.populatePasswordTypeSpinner(this, mPasswordTypeSpinner,
                PasswordType.ALPHANUMERIC_AND_SPECIAL_CHARS);

        mPrivateKeyEditText.setText(RandomPrivateKeyGenerator.generate());
    }

    private void addPasswordLengthTouchListener() {
        mPasswordLengthSpinner.setOnTouchListener(
                new MovementTouchListener(this, new MovementTouchListener.OnPressedListener() {
                    @Override
                    public void onPressed() {
                        showPasswordLengthDialog();
                    }
                }));
    }

    private void addAddButtonClickListener() {
        mAddButton.setOnClickListener(new AddProfileButtonOnClickListener());
    }

    private void addDiscardButtonClickListener() {
        mDiscardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_CANCELED);

                // Navigate to previous activity
                NavUtils.navigateUpFromSameTask(AddProfileActivity.this);
            }
        });

    }

    private void addColorPaletteSelectedListener() {
        mColorPalette
                .setOnColorSelectedListener(new MaterialColorPalette.OnColorSelectedListener() {
                    @Override
                    public void onColorSelected(int color) {
                        mColor = color;
                    }
                });
    }

    private void addFormChangedListener() {
        ProfileFormWatcher profileFormWatcher =
                new ProfileFormWatcher(getApplicationContext(), Profile.NO_ID, mNameEditText,
                        mPrivateKeyEditText, mAddButton);
        mNameEditText.addTextChangedListener(profileFormWatcher);
        mPrivateKeyEditText.addTextChangedListener(profileFormWatcher);
    }

    /**
     * Shows a number picker dialog for choosing the password length
     */
    private void showPasswordLengthDialog() {
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
                    PasswordType.values()[mPasswordTypeSpinner.getSelectedItemPosition()], mColor);
            long profileId = ProfileSettings.insertProfile(AddProfileActivity.this, profile);
            if (profileId == -1) {
                // Error!
                Toast.makeText(AddProfileActivity.this, R.string.error, Toast.LENGTH_LONG).show();
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