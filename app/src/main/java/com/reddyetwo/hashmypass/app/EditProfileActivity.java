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
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
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
import com.reddyetwo.hashmypass.app.view.MaterialColorPalette;

/**
 * Activity which allows editing or adding a profile
 */
public class EditProfileActivity extends AppCompatActivity {

    /**
     * Key for Profile ID extra received in the startActivity {@link android.content.Intent}
     */
    public static final String EXTRA_PROFILE_ID = "profile_id";

    /**
     * Activity result key which contains the ID of the added profile
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
     * Password length {@link android.widget.Spinner}
     */
    private Spinner mPasswordLengthSpinner;

    /**
     * Password type {@link android.widget.Spinner}
     */
    private Spinner mPasswordTypeSpinner;

    /**
     * {@link android.widget.Button} for saving the profile changes
     */
    private Button mSaveButton;

    /**
     * {@link android.widget.Button} for discarding changes
     */
    private Button mDiscardButton;

    /**
     * {@link com.reddyetwo.hashmypass.app.view.MaterialColorPalette} to pick a profile color
     */
    private MaterialColorPalette mColorPalette;

    /**
     * Profile ID
     */
    private long mProfileId;

    /**
     * Name of the profile (before being modified in the form)
     */
    private String mOriginalName;

    /**
     * Password length selected when the activity is shown
     */
    private int mInitialPasswordLength;

    /**
     * Selected color index in the {@link com.reddyetwo.hashmypass.app.view.MaterialColorPalette} instance
     */
    private int mColor;

    /**
     * The {@link com.reddyetwo.hashmypass.app.data.Profile} instance to operate on
     */
    private Profile mProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_edit_profile);

        if (!loadProfileData()) {
            return;
        }

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
    public boolean onCreateOptionsMenu(Menu menu) {
        if (mProfileId != Profile.NO_ID) {
            getMenuInflater().inflate(R.menu.edit_profile, menu);
            return true;
        } else {
            return super.onCreateOptionsMenu(menu);
        }
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
                            if (ProfileSettings.deleteProfile(EditProfileActivity
                                    .this, mProfileId)) {
                                NavUtils.navigateUpFromSameTask(EditProfileActivity.this);
                            } else {
                                Log.e(TwikApplication.LOG_TAG, "Error deleting profile");
                            }
                        }
                    });
            builder.setNegativeButton(android.R.string.cancel, null);
            builder.create().show();

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Load the profile data.
     *
     * @return true if data has been successfully loaded, false otherwise
     */
    private boolean loadProfileData() {
        mProfileId = getIntent().getLongExtra(EXTRA_PROFILE_ID, Profile.NO_ID);

        if (mProfileId != Profile.NO_ID) {
            // Load profile from database
            mProfile = ProfileSettings.getProfile(this, mProfileId);
            if (mProfile == null) {
                // Profile not found
                NavUtils.navigateUpFromSameTask(EditProfileActivity.this);
                return false;
            }
        } else {
            mProfile = new Profile();
            setTitle(R.string.action_add_profile);
        }

        mColor = mProfile.getColorIndex();
        mOriginalName = mProfile.getName();
        return true;
    }

    private void initializeView() {
        addToolbar();

        mPasswordTypeSpinner = (Spinner) findViewById(R.id.password_type_spinner);
        mPasswordLengthSpinner = (Spinner) findViewById(R.id.password_length_spinner);
        mDiscardButton = (Button) findViewById(R.id.discard_button);
        mSaveButton = (Button) findViewById(R.id.save_button);
        mNameEditText = (EditText) findViewById(R.id.profile_name_text);
        mColorPalette = (MaterialColorPalette) findViewById(R.id.profile_color_palette);


        mPrivateKeyEditText = (EditText) findViewById(R.id.private_key_text);
        // Use a monospaced typeface for private key which allows distinguishing 0 from O.
        Typeface monospacedTypeface =
                Typeface.createFromAsset(getAssets(), Constants.FONT_MONOSPACE);
        mPrivateKeyEditText.setTypeface(monospacedTypeface);

        addPasswordLengthTouchListener();
        addColorPaletteSelectedListener();
        addSaveButtonClickListener();
        addDiscardButtonClickListener();
        addFormChangedListener();
    }

    private void initializeSettings(Bundle savedInstanceState) {
        mInitialPasswordLength =
                savedInstanceState != null ? savedInstanceState.getInt(KEY_PASSWORD_LENGTH) :
                        mProfile.getPasswordLength();
    }

    private void populateView() {
        mNameEditText.setText(mOriginalName);
        mPrivateKeyEditText.setText(mProfile.getPrivateKey());
        ProfileFormInflater.populatePasswordLengthSpinner(this, mPasswordLengthSpinner,
                mInitialPasswordLength);
        ProfileFormInflater.populatePasswordTypeSpinner(this, mPasswordTypeSpinner,
                mProfile.getPasswordType());
        mColorPalette.setSelectedPosition(mColor);
        mColorPalette.scrollToPosition(mColor);
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
                new ProfileFormWatcher(getApplicationContext(), mProfileId, mNameEditText,
                        mPrivateKeyEditText, mSaveButton);
        mNameEditText.addTextChangedListener(profileFormWatcher);
        mPrivateKeyEditText.addTextChangedListener(profileFormWatcher);
    }

    private void addSaveButtonClickListener() {
        mSaveButton.setOnClickListener(new SaveButtonClickListener());
    }

    private void addDiscardButtonClickListener() {
        mDiscardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_CANCELED);
                NavUtils.navigateUpFromSameTask(EditProfileActivity.this);
            }
        });
    }

    /**
     * Adds the toolbar to the activity. The toolbar is configured so it shows the navigation
     * control and the subtitle.
     */
    private void addToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setSubtitle(mProfile.getName());
        }
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
                ProfileFormInflater.populatePasswordLengthSpinner(EditProfileActivity.this,
                        mPasswordLengthSpinner, length);
            }
        });

        dialogFragment.show(getFragmentManager(), "passwordLength");
    }

    private class SaveButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            // Update profile in the database
            Profile profile = new Profile(mProfileId, mNameEditText.getText().toString(),
                    mPrivateKeyEditText.getText().toString(),
                    Integer.decode((String) mPasswordLengthSpinner.getSelectedItem()),
                    PasswordType.values()[mPasswordTypeSpinner.getSelectedItemPosition()], mColor);
            if (mProfileId == Profile.NO_ID) {
                long profileId = ProfileSettings.insertProfile(EditProfileActivity.this, profile);
                if (profileId == -1) {
                    setResult(RESULT_CANCELED);
                } else {
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra(RESULT_KEY_PROFILE_ID, profileId);
                    setResult(RESULT_OK, resultIntent);
                }
            } else if (!ProfileSettings.updateProfile(EditProfileActivity.this, profile)) {
                Log.e(TwikApplication.LOG_TAG, "Error updating profile");
                Toast.makeText(EditProfileActivity.this, R.string.error, Toast.LENGTH_LONG).show();
            }

            // Go to the parent activity
            NavUtils.navigateUpFromSameTask(EditProfileActivity.this);
        }
    }
}
