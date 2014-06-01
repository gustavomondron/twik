package com.reddyetwo.hashmypass.app;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.reddyetwo.hashmypass.app.data.DataOpenHelper;
import com.reddyetwo.hashmypass.app.data.TagSettings;


public class MainActivity extends Activity {

    private final static int ID_ADD_PROFILE = -1;
    private long mSelectedProfileID;
    private EditText mTagEditText;

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

        ImageButton tagSettingsButton = (ImageButton) findViewById(R.id
                .main_tag_settings);
        tagSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTagSettingsDialog();
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
            Intent intent = new Intent(getBaseContext(), EditProfileActivity.class);
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
            return inflater.inflate(android.R.layout.simple_dropdown_item_1line,
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
        final Dialog d = new Dialog(MainActivity.this);
        d.setTitle(getString(R.string.tag_settings));
        d.setContentView(R.layout.dialog_tag_settings);

        ContentValues tagValues = TagSettings.getTagSettings(this,
                mSelectedProfileID, mTagEditText.getText().toString());

        /* Populate password length spinner */
        Spinner passwordLengthSpinner = (Spinner) d.findViewById(R.id
                .tag_settings_password_length);
        ArrayAdapter<String> passwordLengthAdapter = new ArrayAdapter<String>
                (this,
                android.R.layout.simple_spinner_item,
                new String[] { tagValues.getAsString(
                        DataOpenHelper.COLUMN_TAGS_PASSWORD_LENGTH)});
        passwordLengthAdapter.setDropDownViewResource(android.R.layout
                .simple_spinner_dropdown_item);
        passwordLengthSpinner.setAdapter(passwordLengthAdapter);

        /* Open number picker dialog when the password length spinner is
           touched */
        passwordLengthSpinner.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    showNumberPickerDialog();
                    return true;
                }

                return false;
            }
        });

        /* Populate password type spinner */
        Spinner passwordTypeSpinner = (Spinner) d.findViewById(R.id
                .tag_settings_password_type);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter
                .createFromResource(this, R.array.password_types_array,
                        android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        passwordTypeSpinner.setAdapter(adapter);
        passwordTypeSpinner.setSelection(tagValues.getAsInteger
                (DataOpenHelper.COLUMN_TAGS_PASSWORD_TYPE));


        final Button discardButton = (Button) d.findViewById(R.id
                .tag_settings_discard);
        discardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                d.dismiss();
            }
        });

        d.show();
    }

    public void showNumberPickerDialog() {

    }
}