package com.reddyetwo.hashmypass.app;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;

import com.reddyetwo.hashmypass.app.data.DataOpenHelper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BrowserIntegrationActivity extends Activity {

    private static final Pattern SITE_PATTERN = Pattern.compile(
            "^.*?([\\w\\d\\-]+)\\.((co|com|net|org|ac)\\.)?\\w+$");

    private EditText mTagEditText;

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
            if (siteExtractor.matches()) {
                String site = siteExtractor.group(1);
                mTagEditText = (EditText) findViewById(R.id.browser_tag);
                mTagEditText.setText(site);
            }
        }

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
        Spinner profileSpinner = (Spinner) findViewById(R.id.browser_profile);
        profileSpinner.setAdapter(adapter);

        db.close();

        /* Cancel button finishes dialog activity */
        Button cancelButton = (Button) findViewById(R.id.browser_cancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
