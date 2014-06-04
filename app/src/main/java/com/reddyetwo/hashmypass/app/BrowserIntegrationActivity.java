package com.reddyetwo.hashmypass.app;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.reddyetwo.hashmypass.app.data.DataOpenHelper;
import com.reddyetwo.hashmypass.app.data.PasswordType;
import com.reddyetwo.hashmypass.app.data.ProfileSettings;
import com.reddyetwo.hashmypass.app.data.SiteSettings;
import com.reddyetwo.hashmypass.app.data.TagSettings;
import com.reddyetwo.hashmypass.app.hash.PasswordHasher;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BrowserIntegrationActivity extends Activity {

    private static final Pattern SITE_PATTERN = Pattern.compile(
            "^.*?([\\w\\d\\-]+)\\.((co|com|net|org|ac)\\.)?\\w+$");

    private EditText mTagEditText;
    private Spinner mProfileSpinner;
    private String mSite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browser_integration);

        /* Extract site from URI */
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
            /* We shouldn't be here */
            finish();
        }

        mTagEditText = (EditText) findViewById(R.id.browser_tag);

        /* Populate profile spinner */
        DataOpenHelper helper = new DataOpenHelper(this);
        SQLiteDatabase db = helper.getWritableDatabase();

        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(DataOpenHelper.PROFILES_TABLE_NAME);
        Cursor cursor = queryBuilder.query(db,
                new String[]{DataOpenHelper.COLUMN_ID,
                        DataOpenHelper.COLUMN_PROFILES_NAME}, null, null, null,
                null, null
        );

        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
                android.R.layout.simple_spinner_item, cursor,
                new String[]{DataOpenHelper.COLUMN_PROFILES_NAME},
                new int[]{android.R.id.text1}, 0);
        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        mProfileSpinner = (Spinner) findViewById(R.id.browser_profile);
        mProfileSpinner.setAdapter(adapter);
        mProfileSpinner.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view,
                                               int position, long id) {
                        updateTagText();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });

        db.close();

        /* Tag settings button */
        ImageButton tagSettingsButton =
                (ImageButton) findViewById(R.id.browser_tag_settings);
        tagSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTagSettingsDialog();
            }
        });

        /* Cancel button finishes dialog activity */
        Button cancelButton = (Button) findViewById(R.id.browser_cancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        /* Hash button calculated the hashed password,
        copies it to the clipboard and finishes the dialog activity
         */
        Button hashButton = (Button) findViewById(R.id.browser_hash);
        hashButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calculatePasswordHash();
                finish();
            }
        });

        /* Update the tag according to the site */
        updateTagText();
    }

    private void updateTagText() {
        long profileID = mProfileSpinner.getSelectedItemId();
        String siteTag = SiteSettings.getSiteTag(this, profileID, mSite);
        if (siteTag == null) {
            // There is no previous association, use the site as tag
            siteTag = mSite;
        }
        mTagEditText.setText(siteTag);
    }

    private void calculatePasswordHash() {
        String tag = mTagEditText.getText().toString().trim();
        EditText masterKeyEditText =
                (EditText) findViewById(R.id.browser_master_key);
        String masterKey = masterKeyEditText.getText().toString();
        long profileID = mProfileSpinner.getSelectedItemId();

        // TODO Show warning if tag or master key are empty
        if (tag.length() > 0 && masterKey.length() > 0) {
            /* Calculate the hashed password */
            ContentValues tagSettings =
                    TagSettings.getTagSettings(this, profileID, tag);
            ContentValues profileSettings =
                    ProfileSettings.getProfileSettings(this, profileID);
            String privateKey = profileSettings
                    .getAsString(DataOpenHelper.COLUMN_PROFILES_PRIVATE_KEY);
            int passwordLength = tagSettings
                    .getAsInteger(DataOpenHelper.COLUMN_TAGS_PASSWORD_LENGTH);
            PasswordType passwordType = PasswordType.values()[tagSettings
                    .getAsInteger(DataOpenHelper.COLUMN_TAGS_PASSWORD_TYPE)];
            String hashedPassword = PasswordHasher
                    .hashPassword(tag, masterKey, privateKey, passwordLength,
                            passwordType);

            /* Copy the hashed password to the clipboard */
            ClipboardManager clipboard =
                    (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            ClipData clip =
                    ClipData.newPlainText("hashed_password", hashedPassword);
            clipboard.setPrimaryClip(clip);

            Toast.makeText(this, R.string.copied_to_clipboard,
                    Toast.LENGTH_LONG).show();

            /* If the tag is not already stored in the database,
            save the current settings */
            if (!tagSettings.containsKey(DataOpenHelper.COLUMN_ID)) {
                TagSettings
                        .insertTagSettings(this, tag, profileID, passwordLength,
                                passwordType);
            }

            /* Update the site-tag association */
            SiteSettings.updateSiteTag(this, profileID, mSite, tag);
        }
    }

    /* Shows a number picker dialog for choosing the password length */
    private void showTagSettingsDialog() {
        TagSettingsDialogFragment dialogFragment =
                new TagSettingsDialogFragment();
        dialogFragment.setProfileId(mProfileSpinner.getSelectedItemId());
        dialogFragment.setTag(mTagEditText.getText().toString());
        dialogFragment.show(getFragmentManager(), "tagSettings");
    }
}
