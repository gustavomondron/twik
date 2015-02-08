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
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.reddyetwo.hashmypass.app.adapter.ProfileSpinnerAdapter;
import com.reddyetwo.hashmypass.app.data.Favicon;
import com.reddyetwo.hashmypass.app.data.FaviconSettings;
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
import com.reddyetwo.hashmypass.app.util.HelpToastOnLongPressClickListener;
import com.reddyetwo.hashmypass.app.util.MasterKeyAlarmManager;
import com.reddyetwo.hashmypass.app.util.SecurePassword;
import com.reddyetwo.hashmypass.app.util.TagAutocomplete;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BrowserIntegrationActivity extends Activity
        implements TagSettingsDialogFragment.OnTagSettingsSavedListener,
                   IdenticonGenerationTask.OnIconGeneratedListener {

    /**
     * URL Pattern for parsing site name
     */
    private static final Pattern SITE_PATTERN =
            Pattern.compile("^.*?([\\w\\d\\-]+)\\.((co|com|net|org|ac)\\.)?\\w+$");

    /**
     * Constant for empty string label
     */
    private static final String EMPTY_STRING = "";

    /**
     * Identicon generation task
     */
    private IdenticonGenerationTask mTask;

    /**
     * Tag name EditText component
     */
    private AutoCompleteTextView mTagEditText;

    /**
     * Master key EditText component
     */
    private EditText mMasterKeyEditText;

    /**
     * Profile Spinner component
     */
    private Spinner mProfileSpinner;

    /**
     * Site name
     */
    private String mSite;

    /**
     * Site favicon
     */
    private Favicon mFavicon;

    /**
     * Generated password TextView component
     */
    private TextView mPasswordTextView;

    /**
     * Identicon ImageView component
     */
    private ImageView mIdenticonImageView;

    /**
     * Tag settings button component
     */
    private ImageButton mTagSettingsImageButton;

    /**
     * Ok Button component
     */
    private Button mOkButton;

    /**
     * Profile ID used
     */
    private long mProfileId = -1;

    /**
     * Tag used
     */
    private Tag mTag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Hide title bar and show layout
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_browser_integration);

        initializeView();

        // Get site from URL
        try {
            mSite = getSite(getHost());
        } catch (IllegalArgumentException e) {
            finish();
        }

        // Load site favicon
        loadFavicon();

        // Get tag for this site
        updateTag();

        // Restore remembered master key
        mMasterKeyEditText.setText(HashMyPassApplication.getCachedMasterKey(this), 0,
                HashMyPassApplication.getCachedMasterKey(this).length);
    }

    private void initializeView() {
        mTagEditText = (AutoCompleteTextView) findViewById(R.id.tag_text);
        mMasterKeyEditText = (EditText) findViewById(R.id.master_key_text);
        mIdenticonImageView = (ImageView) findViewById(R.id.identicon);
        mTagSettingsImageButton = (ImageButton) findViewById(R.id.tag_settings);
        mOkButton = (Button) findViewById(R.id.hash_button);
        initializePasswordTextView();
        initializeProfileSpinner();

        // Add listeners
        addPasswordClickedListener();
        addProfileSelectedListener();
        addTagSettingsClickedListener();
        addOkButtonClickedListener();
        addTextChangedListeners();
    }

    private void initializePasswordTextView() {
        mPasswordTextView = (TextView) findViewById(R.id.website_password);
        Typeface tf = Typeface.createFromAsset(getAssets(), Constants.FONT_MONOSPACE);
        mPasswordTextView.setTypeface(tf);
    }

    private void initializeProfileSpinner() {
        mProfileSpinner = (Spinner) findViewById(R.id.profile_spinner);
        if (!ProfileSettings.getList(this).isEmpty()) {
            populateProfileSpinner();
        } else {
            // No profiles, no hash!
            Toast.makeText(this, R.string.error_no_profiles, Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void addPasswordClickedListener() {
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
    }

    private void addProfileSelectedListener() {
        mProfileSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mProfileId = ((Profile) mProfileSpinner.getSelectedItem()).getId();
                updateTag();
                TagAutocomplete.populateTagAutocompleteTextView(BrowserIntegrationActivity.this,
                        mProfileId, mTagEditText);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Nothing to do
            }
        });
    }

    private void addTagSettingsClickedListener() {
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
    }

    private void addOkButtonClickedListener() {
        mOkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stop();
            }
        });
    }

    private void addTextChangedListeners() {
        PasswordTextWatcher watcher = new PasswordTextWatcher();
        mTagEditText.addTextChangedListener(watcher);
        mMasterKeyEditText.addTextChangedListener(watcher);
    }

    /**
     * Load site favicon
     */
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

    private void stop() {
        if (mFavicon != null && mFavicon.getId() == Favicon.NO_ID) {
            // Save the favicon in the storage
            FaviconSettings.insertFavicon(this, mFavicon);
        }

        saveTag();

        // Update last used profile preference
        Preferences.setLastProfile(this, mProfileId);

        // Cache master key
        HashMyPassApplication
                .cacheMasterKey(this, SecurePassword.getPassword(mMasterKeyEditText.getText()));

        // Copy password to clipboard
        if (Preferences.getCopyToClipboard(this) && mPasswordTextView.length() > 0) {
            ClipboardHelper.copyToClipboard(this, ClipboardHelper.CLIPBOARD_LABEL_PASSWORD,
                    mPasswordTextView.getText().toString(), R.string.copied_to_clipboard);
        }

        // Close the dialog activity
        finish();
    }

    private void saveTag() {
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
    }

    private void updateTag() {
        if (mProfileId > -1) {
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

    /**
     * Extract site name from host
     *
     * @return the site name, or null if not found
     * @throws java.lang.IllegalArgumentException if host is null
     */
    private String getSite(String host) throws IllegalArgumentException {
        if (host == null) {
            throw new IllegalArgumentException("Host is null");
        }
        Matcher siteExtractor = SITE_PATTERN.matcher(host);
        String site = null;
        if (siteExtractor.matches()) {
            site = siteExtractor.group(1);
        }
        return site;
    }

    /**
     * Get host from intent data
     *
     * @return the host, or null if data not found in the intent
     */
    private String getHost() {
        String host = null;
        Intent intent = getIntent();
        if (intent != null && Intent.ACTION_SEND.equals(intent.getAction())) {
            String intentText = intent.getStringExtra(Intent.EXTRA_TEXT);
            if (intentText != null) {
                host = Uri.parse(intentText).getHost();
            }
        }
        return host;
    }

    private void populateProfileSpinner() {
        List<Profile> profileList = ProfileSettings.getList(this);
        mProfileSpinner.setAdapter(new ProfileSpinnerAdapter(this, profileList));

        // Get the last used profile
        long lastProfileId = Preferences.getLastProfile(this);
        if (lastProfileId != -1) {
            int position = profileList.indexOf(new Profile(lastProfileId));
            if (position != -1) {
                mProfileSpinner.setSelection(position);
            }
        }
    }

    private void updatePassword() {
        if (mProfileId != 0 && mTagEditText.length() > 0 &&
                mMasterKeyEditText.length() > 0) {
            Profile profile = ProfileSettings.getProfile(this, mProfileId);
            mTag.setName(mTagEditText.getText().toString());
            String password = PasswordHasher.hashTagWithKeys(mTag.getName(),
                    SecurePassword.getPassword(mMasterKeyEditText.getText()),
                    profile.getPrivateKey(), mTag.getPasswordLength(), mTag.getPasswordType());
            mPasswordTextView.setText(password);
        } else {
            mPasswordTextView.setText(EMPTY_STRING);
        }
    }

    private class PasswordTextWatcher implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // Do nothing
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // Do nothing
        }

        @Override
        public void afterTextChanged(Editable s) {
            updatePassword();
            generateIdenticon();

            mOkButton.setEnabled(mTagEditText.length() > 0 && mMasterKeyEditText.length() > 0);


            // Update tag settings button visibility
            if (mTagEditText.getText().length() > 0) {
                mTagSettingsImageButton.setVisibility(View.VISIBLE);
                mTagSettingsImageButton.setEnabled(true);
            } else {
                mTagSettingsImageButton.setVisibility(View.INVISIBLE);
                mTagSettingsImageButton.setEnabled(false);
            }
        }

        private void generateIdenticon() {
            if (mTask != null && mTask.getStatus() == AsyncTask.Status.RUNNING) {
                mTask.cancel(true);
            }

            mTask = new IdenticonGenerationTask(BrowserIntegrationActivity.this,
                    BrowserIntegrationActivity.this);
            mTask.execute(SecurePassword.getPassword(mMasterKeyEditText.getText()));
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

}
