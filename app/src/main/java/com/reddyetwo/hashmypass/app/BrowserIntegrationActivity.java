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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.reddyetwo.hashmypass.app.data.Favicon;
import com.reddyetwo.hashmypass.app.data.FaviconSettings;
import com.reddyetwo.hashmypass.app.data.Preferences;
import com.reddyetwo.hashmypass.app.data.Profile;
import com.reddyetwo.hashmypass.app.data.ProfileSettings;
import com.reddyetwo.hashmypass.app.data.Tag;
import com.reddyetwo.hashmypass.app.data.TagSettings;
import com.reddyetwo.hashmypass.app.hash.PasswordHasher;
import com.reddyetwo.hashmypass.app.util.ClipboardHelper;
import com.reddyetwo.hashmypass.app.util.Constants;
import com.reddyetwo.hashmypass.app.util.FaviconLoader;
import com.reddyetwo.hashmypass.app.util.HelpToastOnLongPressClickListener;
import com.reddyetwo.hashmypass.app.util.MasterKeyAlarmManager;
import com.reddyetwo.hashmypass.app.util.SecurePassword;
import com.reddyetwo.hashmypass.app.util.TagAutocomplete;

import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BrowserIntegrationActivity extends Activity
        implements TagSettingsDialogFragment.OnTagSettingsSavedListener,
                   IdenticonGenerationTask.OnIconGeneratedListener {

    private static final Pattern SITE_PATTERN =
            Pattern.compile("^.*?([\\w\\d\\-]+)\\.((co|com|net|org|ac)\\.)?\\w+$");

    private IdenticonGenerationTask mTask;
    private AutoCompleteTextView mTagEditText;
    private EditText mMasterKeyEditText;
    private Spinner mProfileSpinner;
    private String mSite;
    private Favicon mFavicon;
    private TextView mPasswordTextView;
    private ImageView mIdenticonImageView;
    private ImageButton mTagSettingsImageButton;
    private Button mOkButton;

    private long mProfileId;
    private Tag mTag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_browser_integration);

        mSite = getSite();

        loadFavicon();

        mPasswordTextView = (TextView) findViewById(R.id.website_password);
        Typeface tf = Typeface.createFromAsset(getAssets(), Constants.FONT_MONOSPACE);
        mPasswordTextView.setTypeface(tf);
        mPasswordTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPasswordTextView.length() > 0) {
                    ClipboardHelper.copyToClipboard(BrowserIntegrationActivity.this,
                            ClipboardHelper.CLIPBOARD_LABEL_PASSWORD,
                            mPasswordTextView.getText().toString(), R.string.copied_to_clipboard);
                }
            }
        });

        mTagEditText = (AutoCompleteTextView) findViewById(R.id.tag_text);
        mMasterKeyEditText = (EditText) findViewById(R.id.master_key_text);
        mIdenticonImageView = (ImageView) findViewById(R.id.identicon);

        // Populate profile spinner
        mProfileSpinner = (Spinner) findViewById(R.id.profile_spinner);
        if (!populateProfileSpinner()) {
            // No profiles, no hash!
            Toast.makeText(this, R.string.error_no_profiles, Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        mProfileSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position,
                                               long id) {
                        mProfileId = ((Profile) mProfileSpinner.getSelectedItem()).getId();
                        updateTag();
                        TagAutocomplete
                                .populateTagAutocompleteTextView(BrowserIntegrationActivity.this,
                                        mProfileId, mTagEditText);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        // Nothing to do
                    }
                });

        mTagSettingsImageButton = (ImageButton) findViewById(R.id.tag_settings);
        mTagSettingsImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mTagEditText.getText().length() > 0) {
                    TagSettingsDialogFragment settingsDialog = new TagSettingsDialogFragment();
                    settingsDialog.setProfileId(mProfileId);
                    settingsDialog.setTag(mTag);
                    settingsDialog.setTagSettingsSavedListener(BrowserIntegrationActivity.this);
                    settingsDialog.show(getFragmentManager(), "tagSettings");
                }
            }
        });
        mTagSettingsImageButton.setOnLongClickListener(new HelpToastOnLongPressClickListener());


        mOkButton = (Button) findViewById(R.id.hash_button);
        mOkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeDialog();
            }
        });


        /* Add text watchers */
        PasswordTextWatcher watcher = new PasswordTextWatcher();
        mTagEditText.addTextChangedListener(watcher);
        mMasterKeyEditText.addTextChangedListener(watcher);

        /* Update the tag according to the site */
        updateTag();
        mMasterKeyEditText.setText(HashMyPassApplication.getCachedMasterKey(), 0,
                HashMyPassApplication.getCachedMasterKey().length);
    }

    private void loadFavicon() {
        final ImageView faviconImageView = (ImageView) findViewById(R.id.tag_favicon);
        final ProgressBar faviconProgressBar = (ProgressBar) findViewById(R.id.favicon_progress);
        faviconProgressBar.setIndeterminate(true);
        final Intent intent = getIntent();

        // Check if we have a favicon stored for this site
        mFavicon = FaviconSettings.getFavicon(this, mSite);
        if (mFavicon != null) {
            // Loaded from storage
            faviconProgressBar.setVisibility(View.GONE);
            faviconImageView.setImageBitmap(mFavicon.getIcon());
            faviconImageView.setVisibility(View.VISIBLE);
        } else {
            // Load favicon from website
            faviconProgressBar.setVisibility(View.VISIBLE);
            faviconImageView.setVisibility(View.GONE);
            FaviconLoader faviconLoader = new FaviconLoader(this);
            faviconLoader.load(intent.getStringExtra(Intent.EXTRA_TEXT),
                    new FaviconLoader.OnFaviconLoaded() {
                        @Override
                        public void onFaviconLoaded(BitmapDrawable icon) {
                            faviconImageView.setImageDrawable(icon);
                            faviconProgressBar.setVisibility(View.GONE);
                            faviconImageView.setVisibility(View.VISIBLE);
                            mFavicon = new Favicon(Favicon.NO_ID, mSite, icon.getBitmap());
                        }
                    });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        /* Cancel the alarm and restore the cached master key */
        MasterKeyAlarmManager.cancelAlarm(this);

        TagAutocomplete.populateTagAutocompleteTextView(this,
                ((Profile) mProfileSpinner.getSelectedItem()).getId(), mTagEditText);
    }

    private void closeDialog() {
        if (mFavicon != null) {
            if (mFavicon.getId() == Favicon.NO_ID) {
                // Save the favicon in the storage
                FaviconSettings.insertFavicon(this, mFavicon);
            }
        }

        // Increase hash counter
        mTag.setHashCounter(mTag.getHashCounter() + 1);

        // Current site tag
        Tag siteTag = TagSettings.getSiteTag(this, mProfileId, mSite);

        if (siteTag != null && siteTag.getId() != mTag.getId()) {
            /* I understand I should remove the previous tag because it
            is no longer necessary, but we should add its hash counter
            because we are generating the password for the same website */
            mTag.setHashCounter(mTag.getHashCounter() + siteTag.getHashCounter());
            TagSettings.deleteTag(this, siteTag);
        }

        /* If the tag is not already stored in the database,
        save the current settings */
        mTag.setSite(mSite);

        // We have to check whether we are overwriting an existing tag
        Tag storedTag = TagSettings.getTag(this, mProfileId, mTag.getName());
        if (storedTag.getId() == Tag.NO_ID) {
            // Not overwriting
            if (mTag.getId() == Tag.NO_ID) {
                TagSettings.insertTag(this, mTag);
            } else {
                TagSettings.updateTag(this, mTag);
            }
        } else {
            /* Overwrite the current tag with the new site and the updated
            tag settings and hash counter */

            if (mTag.getId() != Tag.NO_ID) {
                // Unlink the current tag from this site
                Tag oldTag = TagSettings.getTag(this, mTag.getId());
                oldTag.setSite(null);
                TagSettings.updateTag(this, oldTag);
            }

            // Update the tag
            mTag.setHashCounter(storedTag.getHashCounter() + 1);
            mTag.setId(storedTag.getId());
            TagSettings.updateTag(this, mTag);
        }

        // Update last used profile preference
        SharedPreferences preferences = getSharedPreferences(Preferences.PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong(Preferences.PREFS_KEY_LAST_PROFILE, mProfileId);
        editor.apply();

        // Cache master key
        int masterKeyMins = Preferences.getRememberMasterKeyMins(BrowserIntegrationActivity.this);
        if (masterKeyMins > 0) {
            HashMyPassApplication
                    .setCachedMasterKey(SecurePassword.getPassword(mMasterKeyEditText.getText()));

            MasterKeyAlarmManager.setAlarm(this, masterKeyMins);
        }

        // Copy password to clipboard
        if (Preferences.getCopyToClipboard(this) && mPasswordTextView.length() > 0) {
            ClipboardHelper.copyToClipboard(this, ClipboardHelper.CLIPBOARD_LABEL_PASSWORD,
                    mPasswordTextView.getText().toString(), R.string.copied_to_clipboard);
        }

        // Close the dialog activity
        finish();
    }

    private void updateTag() {
        if (mProfileId > 0) {
            mTag = TagSettings.getSiteTag(this, mProfileId, mSite);
            String siteTagName;
            if (mTag == null) {
                // There is no previous association, use the site as tag.
                siteTagName = mSite;
                Profile profile = ProfileSettings.getProfile(this, mProfileId);
                mTag = new Tag(Tag.NO_ID, mProfileId, 0, mSite, mSite, profile.getPasswordLength(),
                        profile.getPasswordType());
            } else {
                siteTagName = mTag.getName();
            }
            mTagEditText.setText(siteTagName);
        }
    }

    @Override
    public void onTagSettingsSaved(Tag tag) {
        mTag = tag;
        updatePassword();
    }

    private String getSite() {
        // Extract site from URI
        String site = null;
        Intent intent = getIntent();
        if (intent != null && Intent.ACTION_SEND.equals(intent.getAction())) {
            Uri uri = Uri.parse(intent.getStringExtra(Intent.EXTRA_TEXT));
            String host = uri.getHost();

            if (host == null) {
                // Invalid URI
                finish();
                return null;
            }

            Matcher siteExtractor = SITE_PATTERN.matcher(host);
            if (!siteExtractor.matches()) {
                Log.e(HashMyPassApplication.LOG_TAG, "Site pattern does not match");
                finish();
            }

            site = siteExtractor.group(1);
        } else {
            // We shouldn't be here
            finish();
        }

        return site;
    }

    private boolean populateProfileSpinner() {
        List<Profile> profileList = ProfileSettings.getList(this);
        boolean availableProfiles = !profileList.isEmpty();

        // Check that we have added at least one profile
        if (availableProfiles) {
            mProfileSpinner.setAdapter(new ProfileAdapter(this, profileList));

            // Get the last used profile
            SharedPreferences preferences =
                    getSharedPreferences(Preferences.PREFS_NAME, MODE_PRIVATE);
            long lastProfileId = preferences.getLong(Preferences.PREFS_KEY_LAST_PROFILE, -1);
            if (lastProfileId != -1) {
                int position = 0;
                boolean found = false;
                Iterator<Profile> profileIterator = profileList.iterator();
                while (!found && profileIterator.hasNext()) {
                    found = profileIterator.next().getId() == lastProfileId;
                    position++;
                }
                if (found) {
                    position--;
                }
                position = position % profileList.size();
                mProfileSpinner.setSelection(position);
            }
        }

        return availableProfiles;
    }

    private class ProfileAdapter extends ArrayAdapter<Profile> {

        private final List<Profile> mProfiles;
        private static final int RESOURCE = android.R.layout.simple_spinner_dropdown_item;

        public ProfileAdapter(Context context, List<Profile> objects) {
            super(context, RESOURCE, objects);
            mProfiles = objects;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(RESOURCE, parent, false);
            }
            ((TextView) convertView).setText(mProfiles.get(position).getName());

            return convertView;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(RESOURCE, parent, false);
            }

            ((TextView) convertView).setText(mProfiles.get(position).getName());
            return convertView;
        }
    }

    private void updatePassword() {
        if (mProfileId != 0 && mTagEditText.length() > 0 &&
                mMasterKeyEditText.length() > 0) {
            Profile profile = ProfileSettings.getProfile(this, mProfileId);
            mTag.setName(mTagEditText.getText().toString());
            String password = PasswordHasher.hashPassword(mTag.getName(),
                    SecurePassword.getPassword(mMasterKeyEditText.getText()),
                    profile.getPrivateKey(), mTag.getPasswordLength(), mTag.getPasswordType());
            mPasswordTextView.setText(password);
        } else {
            mPasswordTextView.setText("");
        }
    }

    private class PasswordTextWatcher implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // Do nothing because it is not necessary
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // Do nothing because it is not necessary
        }

        @Override
        public void afterTextChanged(Editable s) {
            // Update generated password
            updatePassword();

            // Update OK button enabled state
            mOkButton.setEnabled(mTagEditText.length() > 0 && mMasterKeyEditText.length() > 0);

            // Update identicon
            mIdenticonImageView.setVisibility(View.INVISIBLE);
            if (mTask != null && mTask.getStatus() == AsyncTask.Status.RUNNING) {
                mTask.cancel(true);
            }
            if (mMasterKeyEditText.length() > 0) {
                mTask = new IdenticonGenerationTask(BrowserIntegrationActivity.this,
                        BrowserIntegrationActivity.this);
                mTask.execute(SecurePassword.getPassword(mMasterKeyEditText.getText()));
            }

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
}
