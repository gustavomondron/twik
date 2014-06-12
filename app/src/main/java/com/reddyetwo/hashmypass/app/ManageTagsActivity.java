package com.reddyetwo.hashmypass.app;

import android.app.ActionBar;
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
import android.widget.ArrayAdapter;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.reddyetwo.hashmypass.app.data.DataOpenHelper;
import com.reddyetwo.hashmypass.app.data.Profile;
import com.reddyetwo.hashmypass.app.data.ProfileSettings;
import com.reddyetwo.hashmypass.app.data.Tag;
import com.reddyetwo.hashmypass.app.data.TagSettings;

import java.util.List;

public class ManageTagsActivity extends Activity {

    public static final String EXTRA_PROFILE_ID = "profile_id";

    private long mProfileId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_tags);

        mProfileId = getIntent().getLongExtra(EXTRA_PROFILE_ID, -1);

        Profile profile = ProfileSettings.getProfile(this, mProfileId);

        // Populate action bar
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setSubtitle(profile.getName());
        }

        // Set list adapter
        List<Tag> tagList = TagSettings.getProfileTags(this, mProfileId);
        TagAdapter adapter = new TagAdapter(this, tagList);
        ((ListView) findViewById(android.R.id.list)).setAdapter(adapter);

        // Show the no-tags view or the list view
        updateLayout(tagList.size());
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

    private class TagAdapter extends ArrayAdapter<Tag> {

        private List<Tag> mTags;
        private static final int mResource = R.layout.tags_list_item;

        public TagAdapter(Context context, List<Tag> objects) {
            super(context, mResource, objects);
            mTags = objects;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView =
                        getLayoutInflater().inflate(mResource, parent, false);
            }

            final Tag tag = mTags.get(position);
            TextView nameView =
                    (TextView) convertView.findViewById(R.id.tag_name);
            ImageButton deleteButton =
                    (ImageButton) convertView.findViewById(R.id.delete_button);
            nameView.setText(tag.getName());

            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder =
                            new AlertDialog.Builder(ManageTagsActivity.this);
                    builder.setMessage(getString(R.string.confirm_delete_tag,
                            tag.getName()));
                    builder.setPositiveButton(R.string.action_delete,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    DataOpenHelper helper = new DataOpenHelper(
                                            ManageTagsActivity
                                                    .this
                                    );
                                    if (TagSettings
                                            .deleteTag(ManageTagsActivity.this,
                                                    tag)) {
                                        // Update tags list
                                        mTags = TagSettings.getProfileTags(
                                                ManageTagsActivity.this,
                                                mProfileId);
                                        clear();
                                        addAll(mTags);
                                        notifyDataSetChanged();
                                        updateLayout(mTags.size());
                                    } else {
                                        // Error!
                                        Toast.makeText(ManageTagsActivity.this,
                                                R.string.error,
                                                Toast.LENGTH_LONG).show();
                                    }
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
                    showTagSettingsDialog(tag.getName());
                }
            });

            return convertView;
        }
    }

    /* Shows a number picker dialog for choosing the password length */
    private void showTagSettingsDialog(String tagName) {
        TagSettingsDialogFragment dialogFragment =
                new TagSettingsDialogFragment();
        dialogFragment.setProfileId(mProfileId);
        dialogFragment.setTag(tagName);

        dialogFragment.show(getFragmentManager(), "tagSettings");
    }

    private void updateLayout(int tagListSize) {
        View listView = findViewById(android.R.id.list);
        View emptyView = findViewById(R.id.list_empty_layout);
        if (tagListSize == 0) {
            listView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            listView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }
    }
}
