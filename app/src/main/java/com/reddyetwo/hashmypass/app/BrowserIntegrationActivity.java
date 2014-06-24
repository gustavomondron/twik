/*
 * Copyright 2014 Red Dye No. 2
 *
 * This file is part of Hash My pass.
 *
 * Hash my pass is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Hash my pass is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Hash my pass.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.reddyetwo.hashmypass.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
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
import com.reddyetwo.hashmypass.app.util.ButtonsEnableTextWatcher;
import com.reddyetwo.hashmypass.app.util.ClipboardHelper;
import com.reddyetwo.hashmypass.app.util.FaviconLoader;
import com.reddyetwo.hashmypass.app.util.HelpToastOnLongPressClickListener;
import com.reddyetwo.hashmypass.app.util.MasterKeyAlarmManager;
import com.reddyetwo.hashmypass.app.util.MasterKeyWatcher;
import com.reddyetwo.hashmypass.app.util.TagAutocomplete;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BrowserIntegrationActivity extends Activity {

    private static final Pattern SITE_PATTERN = Pattern.compile(
            "^.*?([\\w\\d\\-]+)\\.((co|com|net|org|ac)\\.)?\\w+$");

    private AutoCompleteTextView mTagEditText;
    private EditText mMasterKeyEditText;
    private Spinner mProfileSpinner;
    private String mSite;
    private ButtonsEnableTextWatcher mButtonsEnableTextWatcher;
    private Favicon mFavicon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_LEFT_ICON);
        setContentView(R.layout.activity_browser_integration);
        setFeatureDrawableResource(Window.FEATURE_LEFT_ICON,
                R.drawable.ic_launcher);

        final ImageView faviconImageView =
                (ImageView) findViewById(R.id.tag_favicon);
        final ProgressBar faviconProgressBar =
                (ProgressBar) findViewById(R.id.favicon_progress);
        faviconProgressBar.setIndeterminate(true);

        // Extract site from URI
        Intent intent = getIntent();
        if (intent != null && Intent.ACTION_SEND.equals(intent.getAction())) {
            Uri uri = Uri.parse(intent.getStringExtra(Intent.EXTRA_TEXT));
            String host = uri.getHost();

            Matcher siteExtractor = SITE_PATTERN.matcher(host);
            if (!siteExtractor.matches()) {
                // TODO Show error
                finish();
            }

            mSite = siteExtractor.group(1);
        } else {
            // We shouldn't be here
            finish();
        }

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
                            mFavicon = new Favicon(Favicon.NO_ID, mSite,
                                    icon.getBitmap());
                        }
                    }
            );
        }

        mTagEditText = (AutoCompleteTextView) findViewById(R.id.tag_text);

        ImageView identiconImageView = (ImageView) findViewById(R.id.identicon);

        mMasterKeyEditText = (EditText) findViewById(R.id.master_key_text);
        mMasterKeyEditText.addTextChangedListener(
                new MasterKeyWatcher(this, identiconImageView));

        // Populate profile spinner
        mProfileSpinner = (Spinner) findViewById(R.id.profile_spinner);
        if (!populateProfileSpinner()) {
            // No profiles, no hash!
            Toast.makeText(this, R.string.error_no_profiles, Toast.LENGTH_LONG)
                    .show();
            finish();
            return;
        }

        mProfileSpinner.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view,
                                               int position, long id) {
                        updateTagText();
                        TagAutocomplete.populateTagAutocompleteTextView(
                                BrowserIntegrationActivity.this,
                                ((Profile) mProfileSpinner.getSelectedItem())
                                        .getId(), mTagEditText
                        );

                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                }
        );

        ImageButton tagSettingsButton =
                (ImageButton) findViewById(R.id.tag_settings);
        tagSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mTagEditText.getText().length() > 0) {
                    showTagSettingsDialog();
                }
            }
        });
        tagSettingsButton.setOnLongClickListener(
                new HelpToastOnLongPressClickListener());

        Button cancelButton = (Button) findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Button hashButton = (Button) findViewById(R.id.hash_button);
        hashButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calculatePasswordHash();
                if (mFavicon != null) {
                    if (mFavicon.getId() == Favicon.NO_ID) {
                        // Save the favicon in the storage
                        FaviconSettings
                                .insertFavicon(BrowserIntegrationActivity.this,
                                        mFavicon);
                    }
                }
                // Close the dialog activity
                finish();
            }
        });

        /* Update the tag according to the site */
        updateTagText();

        /* Set hash button enable watcher */
        mButtonsEnableTextWatcher =
                new ButtonsEnableTextWatcher(mTagEditText, mMasterKeyEditText,
                        tagSettingsButton, hashButton);
        mTagEditText.addTextChangedListener(mButtonsEnableTextWatcher);
        mMasterKeyEditText.addTextChangedListener(mButtonsEnableTextWatcher);
    }

    @Override
    protected void onResume() {
        super.onResume();

        /* Cancel the alarm and restore the cached master key */
        MasterKeyAlarmManager.cancelAlarm(this);
        mMasterKeyEditText.setText(HashMyPassApplication.getCachedMasterKey());

        TagAutocomplete.populateTagAutocompleteTextView(this,
                ((Profile) mProfileSpinner.getSelectedItem()).getId(),
                mTagEditText);

        mButtonsEnableTextWatcher.updateHashButtonEnabled();
    }

    @Override
    protected void onStop() {
        super.onStop();
        int masterKeyMins = Preferences.getRememberMasterKeyMins(this);
        if (masterKeyMins > 0) {
            HashMyPassApplication.setCachedMasterKey(
                    mMasterKeyEditText.getText().toString());
            MasterKeyAlarmManager.setAlarm(this, masterKeyMins);
        }
    }

    private void updateTagText() {
        long profileId = ((Profile) mProfileSpinner.getSelectedItem()).getId();
        Tag tag = TagSettings.getSiteTag(this, profileId, mSite);
        String siteTagName;
        if (tag == null) {
            // There is no previous association, use the site as tag.
            siteTagName = mSite;
        } else {
            siteTagName = tag.getName();
        }
        mTagEditText.setText(siteTagName);
    }

    private void calculatePasswordHash() {
        String tagName = mTagEditText.getText().toString().trim();
        String masterKey = mMasterKeyEditText.getText().toString();

        if (tagName.length() > 0 && masterKey.length() > 0) {
            // Get the private key
            long profileId =
                    ((Profile) mProfileSpinner.getSelectedItem()).getId();
            Profile profile = ProfileSettings.getProfile(this, profileId);

            // Current tag
            Tag tag = TagSettings.getTag(this, profileId, tagName);

            // Increase hash counter
            tag.setHashCounter(tag.getHashCounter() + 1);

            // Current site tag
            Tag siteTag = TagSettings.getSiteTag(this, profileId, mSite);

            if (siteTag != null && !siteTag.getName().equals(tag.getName())) {
                /* I understand I should remove the previous tag because it
                is no longer necessary, but we should add its hash counter
                because we are generating the password for the same website */
                tag.setHashCounter(
                        tag.getHashCounter() + siteTag.getHashCounter());
                TagSettings.deleteTag(this, siteTag);
            }

            // Calculate hashed password
            String hashedPassword = PasswordHasher
                    .hashPassword(tagName, masterKey, profile.getPrivateKey(),
                            tag.getPasswordLength(), tag.getPasswordType());

            // Copy hashed password to clipboard
            ClipboardHelper.copyToClipboard(getApplicationContext(),
                    ClipboardHelper.CLIPBOARD_LABEL_PASSWORD, hashedPassword,
                    R.string.copied_to_clipboard);

            /* If the tag is not already stored in the database,
            save the current settings */
            tag.setSite(mSite);
            if (tag.getId() == Tag.NO_ID) {
                TagSettings.insertTag(this, tag);
            } else {
                // Update the site-tag association
                TagSettings.updateTag(this, tag);
            }

            // Update last used profile preference
            SharedPreferences preferences =
                    getSharedPreferences(Preferences.PREFS_NAME, MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putLong(Preferences.PREFS_KEY_LAST_PROFILE, profileId);
            editor.commit();

        }
    }

    // Shows a number picker dialog for choosing the password length
    private void showTagSettingsDialog() {
        TagSettingsDialogFragment dialogFragment =
                new TagSettingsDialogFragment();
        dialogFragment.setProfileId(
                ((Profile) mProfileSpinner.getSelectedItem()).getId());
        dialogFragment.setTag(mTagEditText.getText().toString());
        dialogFragment.show(getFragmentManager(), "tagSettings");
    }

    private boolean populateProfileSpinner() {
        List<Profile> profileList = ProfileSettings.getList(this);
        boolean availableProfiles = profileList.size() > 0;

        // Check that we have added at least one profile
        if (availableProfiles) {
            mProfileSpinner.setAdapter(new ProfileAdapter(this, profileList));

            // Get the last used profile
            SharedPreferences preferences =
                    getSharedPreferences(Preferences.PREFS_NAME, MODE_PRIVATE);
            long lastProfileId =
                    preferences.getLong(Preferences.PREFS_KEY_LAST_PROFILE, -1);
            if (lastProfileId != -1) {
                int position = 0;
                for (Profile f : profileList) {
                    if (f.getId() == lastProfileId) {
                        break;
                    }
                    position++;
                }
                position = position % profileList.size();
                mProfileSpinner.setSelection(position);
            }
        }

        return availableProfiles;
    }

    private class ProfileAdapter extends ArrayAdapter<Profile> {

        private List<Profile> mProfiles;
        private static final int mResource =
                android.R.layout.simple_spinner_dropdown_item;

        public ProfileAdapter(Context context, List<Profile> objects) {
            super(context, mResource, objects);
            mProfiles = objects;
        }

        @Override
        public View getDropDownView(int position, View convertView,
                                    ViewGroup parent) {
            if (convertView == null) {
                convertView =
                        getLayoutInflater().inflate(mResource, parent, false);
            }
            ((TextView) convertView).setText(mProfiles.get(position).getName());

            return convertView;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView =
                        getLayoutInflater().inflate(mResource, parent, false);
            }

            ((TextView) convertView).setText(mProfiles.get(position).getName());
            return convertView;
        }
    }
}
