package com.reddyetwo.hashmypass.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.reddyetwo.hashmypass.app.data.Preferences;
import com.reddyetwo.hashmypass.app.data.Profile;
import com.reddyetwo.hashmypass.app.data.ProfileSettings;
import com.reddyetwo.hashmypass.app.data.Tag;
import com.reddyetwo.hashmypass.app.data.TagSettings;
import com.reddyetwo.hashmypass.app.hash.PasswordHasher;
import com.reddyetwo.hashmypass.app.util.ClipboardHelper;
import com.reddyetwo.hashmypass.app.util.Constants;
import com.reddyetwo.hashmypass.app.util.ButtonsEnableTextWatcher;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_LEFT_ICON);
        setContentView(R.layout.activity_browser_integration);
        setFeatureDrawableResource(Window.FEATURE_LEFT_ICON,
                R.drawable.ic_launcher);

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

        mTagEditText = (AutoCompleteTextView) findViewById(R.id.tag_text);

        TextView digestTextView = (TextView) findViewById(R.id.digest_text);

        mMasterKeyEditText = (EditText) findViewById(R.id.master_key_text);
        mMasterKeyEditText
                .addTextChangedListener(new MasterKeyWatcher(digestTextView));

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
                // Close the dialog activity
                finish();
            }
        });

        /* Update the tag according to the site */
        updateTagText();

        /* Set monospace font for key digest */
        Typeface tf =
                Typeface.createFromAsset(getAssets(), Constants.FONT_MONOSPACE);
        digestTextView.setTypeface(tf);

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
            Tag tag = TagSettings.getTag(this, profileId, tagName);

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
            if (tag.getId() == Tag.NO_ID) {
                TagSettings.insertTag(this, tag);
            }

            // Update the site-tag association
            tag.setSite(mSite);
            TagSettings.updateTag(this, tag);

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
