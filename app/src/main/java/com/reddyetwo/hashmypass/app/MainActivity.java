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

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.reddyetwo.hashmypass.app.data.Favicon;
import com.reddyetwo.hashmypass.app.data.FaviconSettings;
import com.reddyetwo.hashmypass.app.data.Preferences;
import com.reddyetwo.hashmypass.app.data.Profile;
import com.reddyetwo.hashmypass.app.data.ProfileSettings;
import com.reddyetwo.hashmypass.app.data.Tag;
import com.reddyetwo.hashmypass.app.data.TagSettings;
import com.reddyetwo.hashmypass.app.util.Fab;
import com.reddyetwo.hashmypass.app.util.FaviconLoader;
import com.reddyetwo.hashmypass.app.util.MasterKeyAlarmManager;

import java.util.List;


public class MainActivity extends Activity
        implements AddDefaultProfileDialog.OnProfileAddedListener,
        GeneratePasswordDialogFragment.GeneratePasswordDialogListener {

    // Constants
    private static final int ID_ADD_PROFILE = -1;
    private static final int REQUEST_ADD_PROFILE = 1;
    private static final int REQUEST_CREATE_DEFAULT_PROFILE = 2;

    // State keys
    private static final String STATE_SELECTED_PROFILE_ID = "profile_id";
    private static final String STATE_ORIENTATION_HAS_CHANGED =
            "orientation_has_changed";

    // State vars
    private long mSelectedProfileId = -1;
    private OrientationEventListener mOrientationEventListener;
    private boolean mOrientationHasChanged;

    private RecyclerView mTagRecyclerView;
    private AddDefaultProfileDialog mAddDefaultProfileDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        }

        // Select the last profile used for hashing a password
        mSelectedProfileId = getLastProfile();

        mTagRecyclerView = (RecyclerView) findViewById(R.id.tag_list);
        mTagRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mTagRecyclerView.setItemAnimator(new DefaultItemAnimator());

        Fab mFab = (Fab) findViewById(R.id.fabbutton);
        mFab.setFabColor(getResources().getColor(R.color.hashmypass_main));
        mFab.setFabDrawable(
                getResources().getDrawable(R.drawable.ic_action_add));
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Profile profile = ProfileSettings
                        .getProfile(MainActivity.this, mSelectedProfileId);
                Tag tag = new Tag(Tag.NO_ID, mSelectedProfileId, 1, null, "",
                        profile.getPasswordLength(), profile.getPasswordType());
                showGeneratePasswordDialog(tag);
            }
        });

        /* Detect orientation changes.
        In the case of an orientation change, we do not select the last used
        profile but the currently selected profile.
         */
        mOrientationHasChanged = false;
        mOrientationEventListener = new OrientationEventListener(this,
                SensorManager.SENSOR_DELAY_NORMAL) {

            @Override
            public void onOrientationChanged(int orientation) {
                mOrientationHasChanged = true;
            }
        };
        mOrientationEventListener.enable();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // If we have just dismissed the application, close the application
        // because the user wants to exit
        if (HashMyPassApplication.getTutorialDismissed()) {
            HashMyPassApplication.setTutorialDismissed(false);
            finish();
            return;
        }

        if (!showTutorial()) {
            populateActionBarSpinner();
            populateTagList();

            /* Cancel the master key alarm to clear cache */
            MasterKeyAlarmManager.cancelAlarm(this);
        }
    }

    private void populateTagList() {
        List<Tag> tags = TagSettings.getProfileTags(this, mSelectedProfileId,
                TagSettings.ORDER_BY_HASH_COUNTER, TagSettings.LIMIT_UNBOUNDED);
        mTagRecyclerView.setAdapter(new TagAdapter(tags));
        if (tags.size() == 0) {
            mTagRecyclerView.setVisibility(View.GONE);
            findViewById(R.id.list_empty).setVisibility(View.VISIBLE);
        } else {
            mTagRecyclerView.setVisibility(View.VISIBLE);
            findViewById(R.id.list_empty).setVisibility(View.GONE);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        /* Dismiss add default profile dialog in the case that it is shown,
        to prevent opening multiple dialogs simultaneously.
         */
        if (mAddDefaultProfileDialog != null) {
            mAddDefaultProfileDialog.dismiss();
            mAddDefaultProfileDialog = null;
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        cacheMasterKey();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mOrientationEventListener.disable();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Activity state
        outState.putBoolean(STATE_ORIENTATION_HAS_CHANGED,
                mOrientationHasChanged);
        outState.putLong(STATE_SELECTED_PROFILE_ID, mSelectedProfileId);

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
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_edit_profile) {
            Intent intent = new Intent(this, EditProfileActivity.class);
            intent.putExtra(EditProfileActivity.EXTRA_PROFILE_ID,
                    mSelectedProfileId);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_help) {
            Intent intent = new Intent(this, TutorialActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_about) {
            AboutDialog.showAbout(this);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        switch (requestCode) {
            case REQUEST_ADD_PROFILE:
                switch (resultCode) {
                    case RESULT_OK:
                        mSelectedProfileId = data.getLongExtra(
                                AddProfileActivity.RESULT_KEY_PROFILE_ID, 0);
                        break;
                    default:
                }
                break;
            case REQUEST_CREATE_DEFAULT_PROFILE:
                switch (resultCode) {
                    case RESULT_CANCELED:
                        finish();
                        return;
                    default:
                }
                break;
            default:
        }
    }

    private void populateActionBarSpinner() {
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            final List<Profile> profiles = ProfileSettings.getList(this);
            Profile addProfile = new Profile();
            addProfile.setId(ID_ADD_PROFILE);
            addProfile.setName(getString(R.string.action_add_profile));
            profiles.add(addProfile);

            ProfileAdapter adapter = new ProfileAdapter(this, profiles);
            actionBar.setListNavigationCallbacks(adapter,
                    new ActionBar.OnNavigationListener() {
                        @Override
                        public boolean onNavigationItemSelected(
                                int itemPosition, long itemId) {
                            long selectedProfile =
                                    profiles.get(itemPosition).getId();
                            if (selectedProfile == ID_ADD_PROFILE) {
                                Intent intent = new Intent(MainActivity.this,
                                        AddProfileActivity.class);
                                startActivityForResult(intent,
                                        REQUEST_ADD_PROFILE);
                            } else {
                                mSelectedProfileId = selectedProfile;
                                populateTagList();
                            }
                            return false;
                        }
                    });

            /* If we had previously selected a profile before pausing the
            activity and it still exists, select it in the spinner. */
            int position = 0;

            /* If mSelectedProfileId == ID_ADD_PROFILE is because we have
            never hashed any password and there is no "last profile used for
            hashing" */
            if (mSelectedProfileId != ID_ADD_PROFILE) {
                for (Profile p : profiles) {
                    if (p.getId() == mSelectedProfileId) {
                        break;
                    }
                    position++;
                }
            }

            /* It may happen that the last profiled used for hashing no longer
            exists */
            position = position % profiles.size();
            actionBar.setSelectedNavigationItem(position);
        }
    }

    @Override
    public void onProfileAdded() {
        // Refresh the action bar spinner
        populateActionBarSpinner();
        populateTagList();
    }

    @Override
    public void onCanceled() {
        // You didn't add the profile! Nothing to do here!
        finish();
    }

    private void updateLastProfile() {
                /* Update last used profile preference */
        SharedPreferences preferences =
                getSharedPreferences(Preferences.PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong(Preferences.PREFS_KEY_LAST_PROFILE, mSelectedProfileId);
        editor.apply();
    }

    private long getLastProfile() {
        // Select the last profile used for hashing a password
        SharedPreferences preferences =
                getSharedPreferences(Preferences.PREFS_NAME, MODE_PRIVATE);
        return preferences.getLong(Preferences.PREFS_KEY_LAST_PROFILE, -1);
    }

    private boolean showTutorial() {
        // Check if tutorial should be shown
        if (ProfileSettings.getList(this).size() == 0) {
            Intent intent = new Intent(this, TutorialActivity.class);
            startActivity(intent);
            return true;
        } else {
            return false;
        }
    }

    private void cacheMasterKey() {
        int masterKeyMins = Preferences.getRememberMasterKeyMins(this);
        if (masterKeyMins > 0) {
            MasterKeyAlarmManager.setAlarm(this, masterKeyMins);
        }
    }

    @Override
    public void onDialogDismiss(Tag tag) {
        if (tag != null && tag.getId() == Tag.NO_ID &&
                tag.getName().length() > 0) {
            // It is a new tag
            TagSettings.insertTag(this, tag);
            populateTagList();

            // Update last used profile
            updateLastProfile();
        } else if (tag != null && tag.getName().length() > 0) {
            // Save the tag and update the list because the name of a tag or
            // its order in the list can have been modified
            TagSettings.updateTag(this, tag);
            populateTagList();
        }

    }

    private class TagAdapter extends RecyclerView.Adapter<TagListViewHolder> {

        private List<Tag> mTags;
        private static final int mResource = R.layout.tag_list_item;

        public TagAdapter(List<Tag> objects) {
            super();
            mTags = objects;
        }

        @Override
        public TagListViewHolder onCreateViewHolder(ViewGroup viewGroup,
                                                    int i) {
            View v = LayoutInflater.from(viewGroup.getContext())
                    .inflate(mResource, viewGroup, false);
            return new TagListViewHolder(v);
        }

        @Override
        public void onBindViewHolder(TagListViewHolder tagListViewHolder,
                                     int i) {
            final Tag tag = mTags.get(i);
            FaviconLoader.setAsBackground(getApplicationContext(),
                    tagListViewHolder.mFaviconTextView, tag);
            tagListViewHolder.mTagNameTextView.setText(tag.getName());
            tagListViewHolder.itemView
                    .setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // Increase hash counter
                            tag.setHashCounter(tag.getHashCounter() + 1);
                            TagSettings.updateTag(MainActivity.this, tag);

                            // Update last used profile
                            updateLastProfile();

                            // Show dialog
                            showGeneratePasswordDialog(tag);
                        }
                    });
            tagListViewHolder.itemView
                    .setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            AlertDialog.Builder builder =
                                    new AlertDialog.Builder(MainActivity.this);
                            builder.setMessage(
                                    getString(R.string.confirm_delete_tag,
                                            tag.getName()));
                            builder.setPositiveButton(R.string.action_delete,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(
                                                DialogInterface dialog,
                                                int which) {
                                            deleteTag(tag);
                                        }
                                    });
                            builder.setNegativeButton(android.R.string.cancel,
                                    null);
                            builder.create().show();
                            return false;
                        }
                    });
        }

        @Override
        public int getItemCount() {
            return mTags.size();
        }
    }

    private void deleteTag(Tag tag) {
        if (TagSettings.deleteTag(this, tag)) {
            // Update tags list
            populateTagList();

            // Check if we should delete favicon
            String site = tag.getSite();
            if (site != null) {
                Favicon favicon = FaviconSettings.getFavicon(this, site);
                if (favicon != null && !TagSettings.siteHasTags(this, site)) {
                    FaviconSettings.deleteFavicon(this, favicon);
                }

            }
        } else {
            // Error!
            Toast.makeText(this, R.string.error, Toast.LENGTH_LONG).show();
        }
    }


    private void showGeneratePasswordDialog(Tag tag) {
        GeneratePasswordDialogFragment dialog =
                new GeneratePasswordDialogFragment();
        dialog.setProfileId(mSelectedProfileId);
        dialog.setTag(tag);
        dialog.setDialogOkListener(this);
        dialog.show(getFragmentManager(), "generatePassword");
    }

    public class TagListViewHolder extends RecyclerView.ViewHolder {

        public TextView mFaviconTextView;
        public TextView mTagNameTextView;

        public TagListViewHolder(View itemView) {
            super(itemView);
            mFaviconTextView =
                    (TextView) itemView.findViewById(R.id.tag_favicon);
            mTagNameTextView = (TextView) itemView.findViewById(R.id.tag_name);
        }
    }
}