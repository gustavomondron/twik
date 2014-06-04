package com.reddyetwo.hashmypass.app;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.reddyetwo.hashmypass.app.data.DataOpenHelper;
import com.reddyetwo.hashmypass.app.data.PasswordType;
import com.reddyetwo.hashmypass.app.data.ProfileSettings;
import com.reddyetwo.hashmypass.app.data.TagSettings;
import com.reddyetwo.hashmypass.app.hash.PasswordHasher;


public class MainActivity extends Activity {

    private final static int ID_ADD_PROFILE = -1;
    private long mSelectedProfileID;
    private EditText mTagEditText;
    private TextView mHashedPasswordTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DataOpenHelper helper = new DataOpenHelper(this);
        SQLiteDatabase db = helper.getWritableDatabase();

        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(DataOpenHelper.PROFILES_TABLE_NAME);
        Cursor cursor = queryBuilder.query(db,
                new String[]{DataOpenHelper.COLUMN_ID,
                        DataOpenHelper.COLUMN_PROFILES_NAME}, null, null, null,
                null, null
        );

        MatrixCursor extras = new MatrixCursor(
                new String[]{DataOpenHelper.COLUMN_ID,
                        DataOpenHelper.COLUMN_PROFILES_NAME}
        );
        extras.addRow(new String[]{Integer.toString(ID_ADD_PROFILE),
                getResources().getString(R.string.action_add_profile)});
        MergeCursor mergeCursor = new MergeCursor(new Cursor[]{cursor, extras});

        ProfileAdapter adapter = new ProfileAdapter(this, mergeCursor, 0);

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        actionBar.setListNavigationCallbacks(adapter,
                new ActionBar.OnNavigationListener() {
                    @Override
                    public boolean onNavigationItemSelected(int itemPosition,
                                                            long itemId) {
                        mSelectedProfileID = itemId;
                        if (itemId == ID_ADD_PROFILE) {
                            Intent intent = new Intent(getBaseContext(),
                                    AddProfileActivity.class);
                            startActivity(intent);
                        }

                        return false;
                    }
                }
        );

        mTagEditText = (EditText) findViewById(R.id.main_tag);

        ImageButton tagSettingsButton =
                (ImageButton) findViewById(R.id.main_tag_settings);
        tagSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTagSettingsDialog();
            }
        });

        Button hashButton = (Button) findViewById(R.id.main_hash);
        hashButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calculatePasswordHash();
            }
        });

        mHashedPasswordTextView =
                (TextView) findViewById(R.id.main_hashed_password);
        ImageButton clipboardButton =
                (ImageButton) findViewById(R.id.main_clipboard);
        clipboardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard =
                        (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("hashed_password",
                        mHashedPasswordTextView.getText().toString());
                clipboard.setPrimaryClip(clip);

                Toast.makeText(MainActivity.this, R.string.copied_to_clipboard,
                        Toast.LENGTH_LONG).show();

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.action_edit_profile) {
            Intent intent =
                    new Intent(getBaseContext(), EditProfileActivity.class);
            intent.putExtra(EditProfileActivity.EXTRA_PROFILE_ID,
                    mSelectedProfileID);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class ProfileAdapter extends CursorAdapter {

        private ProfileAdapter(Context context, Cursor c, int flags) {
            super(context, c, flags);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            return inflater.inflate(android.R.layout.simple_spinner_dropdown_item,
                    parent, false);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            String profileName = cursor.getString(
                    cursor.getColumnIndex(DataOpenHelper.COLUMN_PROFILES_NAME));
            ((TextView) view).setText(profileName);
        }
    }

    /* Shows a number picker dialog for choosing the password length */
    private void showTagSettingsDialog() {
        TagSettingsDialogFragment dialogFragment =
                new TagSettingsDialogFragment();
        dialogFragment.setProfileId(mSelectedProfileID);
        dialogFragment.setTag(mTagEditText.getText().toString());

        dialogFragment.show(getFragmentManager(), "tagSettings");
    }

    private void calculatePasswordHash() {
        String tag = mTagEditText.getText().toString().trim();
        EditText masterKeyEditText =
                (EditText) findViewById(R.id.main_master_key);
        String masterKey = masterKeyEditText.getText().toString();

        // TODO Show warning if tag or master key are empty
        if (tag.length() > 0 && masterKey.length() > 0) {
            /* Calculate the hashed password */
            ContentValues tagSettings =
                    TagSettings.getTagSettings(this, mSelectedProfileID, tag);
            ContentValues profileSettings = ProfileSettings
                    .getProfileSettings(this, mSelectedProfileID);
            String privateKey = profileSettings
                    .getAsString(DataOpenHelper.COLUMN_PROFILES_PRIVATE_KEY);
            int passwordLength = tagSettings
                    .getAsInteger(DataOpenHelper.COLUMN_TAGS_PASSWORD_LENGTH);
            PasswordType passwordType = PasswordType.values()[tagSettings
                    .getAsInteger(DataOpenHelper.COLUMN_TAGS_PASSWORD_TYPE)];
            String hashedPassword = PasswordHasher
                    .hashPassword(tag, masterKey, privateKey, passwordLength,
                            passwordType);

            /* Update the TextView */
            mHashedPasswordTextView.setText(hashedPassword);

            /* If the tag is not already stored in the database,
            save the current settings */
            if (!tagSettings.containsKey(DataOpenHelper.COLUMN_ID)) {
                TagSettings.insertTagSettings(this, tag, mSelectedProfileID,
                        passwordLength, passwordType);
            }
        }
    }

}