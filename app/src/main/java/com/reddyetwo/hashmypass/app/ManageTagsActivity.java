package com.reddyetwo.hashmypass.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.reddyetwo.hashmypass.app.data.DataOpenHelper;
import com.reddyetwo.hashmypass.app.data.ProfileSettings;
import com.reddyetwo.hashmypass.app.data.TagSettings;

public class ManageTagsActivity extends Activity {

    public static final String EXTRA_PROFILE_ID = "profile_id";

    private long mProfileID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_tags);

        mProfileID = getIntent().getLongExtra(EXTRA_PROFILE_ID, -1);

        // Get profile name
        ContentValues profileSettings =
                ProfileSettings.getProfileSettings(this, mProfileID);

        // Populate action bar
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setSubtitle(profileSettings
                .getAsString(DataOpenHelper.COLUMN_PROFILES_NAME));

        // Set list adapter
        Cursor cursor = TagSettings.getTagsForProfile(this, mProfileID);
        TagAdapter adapter = new TagAdapter(this, cursor, 0);
        ((ListView) findViewById(android.R.id.list)).setAdapter(adapter);

        // Check that cursor contains data
        checkEmptyList(cursor);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class TagAdapter extends CursorAdapter {

        private TagAdapter(Context context, Cursor c, int flags) {
            super(context, c, flags);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            return inflater.inflate(R.layout.tags_list_item, parent, false);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            final long tagID = cursor.getLong(
                    cursor.getColumnIndex(DataOpenHelper.COLUMN_ID));
            final String tagName = cursor.getString(
                    cursor.getColumnIndex(DataOpenHelper.COLUMN_TAGS_NAME));
            TextView nameView = (TextView) view.findViewById(R.id.tag_name);
            ImageButton deleteButton =
                    (ImageButton) view.findViewById(R.id.delete_button);
            nameView.setText(tagName);
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder =
                            new AlertDialog.Builder(ManageTagsActivity.this);
                    builder.setTitle(
                            getString(R.string.confirm_delete_tag, tagName));
                    builder.setMessage(R.string.warning_lose_settings);
                    builder.setPositiveButton(R.string.action_delete,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    DataOpenHelper helper = new DataOpenHelper(
                                            ManageTagsActivity
                                                    .this
                                    );
                                    SQLiteDatabase db =
                                            helper.getWritableDatabase();
                                    db.delete(DataOpenHelper.TAGS_TABLE_NAME,
                                            "_id=" + tagID + " AND " +
                                                    DataOpenHelper.COLUMN_TAGS_PROFILE_ID +
                                                    "=" + mProfileID, null
                                    );
                                    /* TODO Check delete return value */

                                    /* Update list */
                                    Cursor cursor = TagSettings
                                            .getTagsForProfile(
                                                    ManageTagsActivity.this,
                                                    mProfileID);
                                    changeCursor(cursor);
                                    notifyDataSetChanged();
                                    checkEmptyList(cursor);
                                }
                            }
                    );
                    builder.setNegativeButton(android.R.string.cancel, null);
                    builder.create().show();
                }
            });

            deleteButton.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Toast.makeText(ManageTagsActivity.this,
                            R.string.action_delete, Toast.LENGTH_SHORT).show();
                    return true;
                }
            });

            nameView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showTagSettingsDialog(tagName);
                }
            });
        }
    }

    /* Shows a number picker dialog for choosing the password length */
    private void showTagSettingsDialog(String tag) {
        TagSettingsDialogFragment dialogFragment =
                new TagSettingsDialogFragment();
        dialogFragment.setProfileId(mProfileID);
        dialogFragment.setTag(tag);

        dialogFragment.show(getFragmentManager(), "tagSettings");
    }

    private void checkEmptyList(Cursor cursor) {
        View listView = findViewById(android.R.id.list);
        View emptyView = findViewById(R.id.list_empty_layout);
        if (cursor.getCount() == 0) {
            listView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            listView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }
    }
}
