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

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.DataSetObserver;
import android.graphics.Typeface;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
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


public class MainActivity extends ActionBarActivity
        implements GeneratePasswordDialogFragment.GeneratePasswordDialogListener {

    // Constants
    private static final int ID_ADD_PROFILE = -1;
    private static final int REQUEST_ADD_PROFILE = 1;
    private static final int REQUEST_CREATE_DEFAULT_PROFILE = 2;
    private static final String FRAGMENT_GENERATE_PASSWORD = "generatePassword";
    private static final int LIST_EMPTY = 1;
    private static final int LIST_CONTAINS_ITEMS = 2;
    private static final int LIST_UNDEFINED = 3;
    private static final String TAG_TOOLBAR_SPINNER_ITEM_DROPDOWN = "SpinnerDropdown";
    private static final String TAG_TOOLBAR_SPINNER_ITEM_ACTIONBAR = "SpinnerNotDropdown";

    // State keys
    private static final String STATE_SELECTED_PROFILE_ID = "profile_id";
    private static final String STATE_ORIENTATION_HAS_CHANGED = "orientation_has_changed";

    // State vars
    private long mSelectedProfileId = -1;
    private OrientationEventListener mOrientationEventListener;
    private boolean mOrientationHasChanged;

    private int[] mColors;
    private RecyclerView mTagRecyclerView;
    private LinearLayout mEmptyListLayout;
    private TagAdapter mAdapter;
    private Fab mFab;
    private Toolbar mToolbar;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Use primary dark color for the status bar
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        // Add toolbar to the activity
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // Select the last profile used for hashing a password
        mSelectedProfileId = getLastProfile();

        mTagRecyclerView = (RecyclerView) findViewById(R.id.tag_list);
        mTagRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mTagRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mEmptyListLayout = (LinearLayout) findViewById(R.id.list_empty);

        mColors = getResources().getIntArray(R.array.favicon_background_colors);
        mFab = (Fab) findViewById(R.id.fabbutton);
        mFab.setFabDrawable(getResources().getDrawable(R.drawable.ic_action_add));
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Profile profile = ProfileSettings.getProfile(MainActivity.this, mSelectedProfileId);
                Tag tag = new Tag(Tag.NO_ID, mSelectedProfileId, 1, null, "",
                        profile.getPasswordLength(), profile.getPasswordType());
                showGeneratePasswordDialog(tag);
            }
        });

        /* Detect orientation changes.
        In the case of an orientation change, we do not select the last used
        profile but the currently selected profile.
         */
        if (savedInstanceState != null &&
                savedInstanceState.getBoolean(STATE_ORIENTATION_HAS_CHANGED)) {
            mSelectedProfileId = savedInstanceState.getLong(STATE_SELECTED_PROFILE_ID);
        }
        mOrientationHasChanged = false;
        mOrientationEventListener =
                new OrientationEventListener(this, SensorManager.SENSOR_DELAY_NORMAL) {

                    @Override
                    public void onOrientationChanged(int orientation) {
                        mOrientationHasChanged = true;
                    }
                };
        mOrientationEventListener.enable();

        /* If the password generation dialog fragment is shown and the
        screen, we have to restore the listener */
        if (savedInstanceState != null) {
            GeneratePasswordDialogFragment fragment =
                    (GeneratePasswordDialogFragment) getFragmentManager()
                            .findFragmentByTag(FRAGMENT_GENERATE_PASSWORD);
            if (fragment != null) {
                fragment.setDialogOkListener(this);
            }
        }

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
            populateToolBarSpinner();

            /* Cancel the master key alarm to clear cache */
            MasterKeyAlarmManager.cancelAlarm(this);
        }

        // Update FAB color
        if (mSelectedProfileId != Profile.NO_ID) {
            Profile profile = ProfileSettings.getProfile(this, mSelectedProfileId);
            /* Warning: the profile could have been removed. In that case,
            we'll select the first profile in the list */
            if (profile == null) {
                profile = ProfileSettings.getList(this).get(0);
                mSelectedProfileId = profile.getId();
            }
            setFabColor(mColors[profile.getColorIndex()]);
        }

        /* Update tag list (may have been changed from web browser) */
        populateTagList();
    }

    private int getTagPosition(long id) {
        List<Tag> tags = TagSettings
                .getProfileTags(this, mSelectedProfileId, TagSettings.ORDER_BY_HASH_COUNTER,
                        TagSettings.LIMIT_UNBOUNDED);
        int position = 0;
        int size = tags.size();
        while (position < size && tags.get(position).getId() != id) {
            position++;
        }
        return position;
    }

    /**
     * Updates the visibility of the tag list
     *
     * @param listContainsItems indicates whether the list contains items
     * @param prevState         previous state of the list
     * @param tags              list of tags, used when prevState =
     *                          LIST_UNDEFINED or LIST_CONTAINS_ITEMS. Null
     *                          if list items should not be modified.
     */
    private void updateTagListVisibility(boolean listContainsItems, int prevState,
                                         final List<Tag> tags) {
        AnimatorSet invisibleAnimator =
                (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.invisible);
        final AnimatorSet visibleAnimator =
                (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.visible);
        AnimatorSet animator;

        if (listContainsItems) {
            switch (prevState) {
                case LIST_UNDEFINED:
                    if (tags != null) {
                        mAdapter = new TagAdapter(tags);
                        mTagRecyclerView.setAdapter(mAdapter);
                        visibleAnimator.setTarget(mTagRecyclerView);
                        visibleAnimator.start();
                    }
                    break;
                case LIST_EMPTY:
                    if (tags != null) {
                        mAdapter = new TagAdapter(tags);
                        mTagRecyclerView.setAdapter(mAdapter);
                    }
                    invisibleAnimator.setTarget(mEmptyListLayout);
                    visibleAnimator.setTarget(mTagRecyclerView);
                    animator = new AnimatorSet();
                    animator.playTogether(visibleAnimator, invisibleAnimator);
                    animator.start();
                    break;
                case LIST_CONTAINS_ITEMS:
                    if (tags != null) {
                        invisibleAnimator.setTarget(mTagRecyclerView);
                        invisibleAnimator.setDuration(150);
                        visibleAnimator.setTarget(mTagRecyclerView);
                        visibleAnimator.setDuration(150);
                        invisibleAnimator.addListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animator animation) {
                                mAdapter = new TagAdapter(tags);
                                mTagRecyclerView.setAdapter(mAdapter);
                                visibleAnimator.start();
                            }

                            @Override
                            public void onAnimationCancel(Animator animation) {

                            }

                            @Override
                            public void onAnimationRepeat(Animator animation) {

                            }
                        });
                        invisibleAnimator.start();
                    }
                    break;
            }
        } else {
            if (tags != null) {
                mAdapter = new TagAdapter(tags);
                mTagRecyclerView.setAdapter(mAdapter);
            }

            switch (prevState) {
                case LIST_UNDEFINED:
                    visibleAnimator.setTarget(mEmptyListLayout);
                    visibleAnimator.start();
                    break;
                case LIST_CONTAINS_ITEMS:
                    invisibleAnimator.setTarget(mTagRecyclerView);
                    visibleAnimator.setTarget(mEmptyListLayout);
                    animator = new AnimatorSet();
                    animator.playTogether(invisibleAnimator, visibleAnimator);
                    animator.start();
                    break;
            }
        }
    }

    private void populateTagList() {
        // Determine previous state
        int prevState;
        if (mAdapter == null) {
            prevState = LIST_UNDEFINED;
        } else if (mAdapter.getItemCount() == 0) {
            prevState = LIST_EMPTY;
        } else {
            prevState = LIST_CONTAINS_ITEMS;
        }

        List<Tag> tags = TagSettings
                .getProfileTags(this, mSelectedProfileId, TagSettings.ORDER_BY_HASH_COUNTER,
                        TagSettings.LIMIT_UNBOUNDED);
        updateTagListVisibility(tags.size() > 0, prevState, tags);
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
        outState.putBoolean(STATE_ORIENTATION_HAS_CHANGED, mOrientationHasChanged);
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
            intent.putExtra(EditProfileActivity.EXTRA_PROFILE_ID, mSelectedProfileId);
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ADD_PROFILE:
                switch (resultCode) {
                    case RESULT_OK:
                        mSelectedProfileId =
                                data.getLongExtra(AddProfileActivity.RESULT_KEY_PROFILE_ID, 0);
                        setFabColor(mColors[ProfileSettings.getProfile(this, mSelectedProfileId)
                                .getColorIndex()]);
                        populateTagList();
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

    private void setFabColor(int color) {
        int colorFrom = mFab.getFabColor();
        ValueAnimator colorAnimation =
                ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, color);
        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mFab.setFabColor((Integer) animation.getAnimatedValue());
            }
        });
        colorAnimation.start();
    }

    private void populateToolBarSpinner() {
        if (mToolbar != null) {
            final List<Profile> profiles = ProfileSettings.getList(this);
            Profile addProfile = new Profile();
            addProfile.setId(ID_ADD_PROFILE);
            addProfile.setName(getString(R.string.action_add_profile));
            profiles.add(addProfile);
            ProfileSpinnerAdapter spinnerAdapter = new ProfileSpinnerAdapter(profiles);

            Spinner spinner = (Spinner) findViewById(R.id.spinner_nav);
            spinner.setAdapter(spinnerAdapter);
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position,
                                           long id) {
                    if (id == ID_ADD_PROFILE) {
                        Intent intent = new Intent(MainActivity.this, AddProfileActivity.class);
                        startActivityForResult(intent, REQUEST_ADD_PROFILE);
                    } else if (id != mSelectedProfileId) {
                        mSelectedProfileId = id;
                        populateTagList();
                        /* Animate fab color transition,
                        but delay it a bit to let the tag list
                        populate without affecting the animation
                         */
                        mFab.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                setFabColor(mColors[ProfileSettings
                                        .getProfile(MainActivity.this, mSelectedProfileId)
                                        .getColorIndex()]);
                            }
                        }, 100);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

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

            /* It may happen that the last profiled used for hashing no longer exists */
            position = position % profiles.size();
            spinner.setSelection(position);
        }
    }

    private void updateLastProfile() {
                /* Update last used profile preference */
        SharedPreferences preferences = getSharedPreferences(Preferences.PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong(Preferences.PREFS_KEY_LAST_PROFILE, mSelectedProfileId);
        editor.apply();
    }

    private long getLastProfile() {
        // Select the last profile used for hashing a password
        SharedPreferences preferences = getSharedPreferences(Preferences.PREFS_NAME, MODE_PRIVATE);
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
            tag.setId(TagSettings.insertTag(this, tag));
            int position = getTagPosition(tag.getId());
            mAdapter.add(tag, position);

            // Update last used profile
            updateLastProfile();
        } else if (tag != null && tag.getName().length() > 0) {
            /* Save the tag and update the list because the name of a tag can
             alter its position in the list
              */
            TagSettings.updateTag(this, tag);
            mAdapter.update(tag);
        }
    }

    private class TagAdapter extends RecyclerView.Adapter<TagListViewHolder> {

        private final List<Tag> mTags;
        private static final int mResource = R.layout.tag_list_item;

        public TagAdapter(List<Tag> objects) {
            super();
            mTags = objects;
        }

        @Override
        public TagListViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View v = LayoutInflater.from(viewGroup.getContext())
                    .inflate(mResource, viewGroup, false);
            return new TagListViewHolder(v);
        }

        @Override
        public void onBindViewHolder(TagListViewHolder tagListViewHolder, int i) {
            final Tag tag = mTags.get(i);
            FaviconLoader
                    .setAsBackground(getApplicationContext(), tagListViewHolder.mFaviconTextView,
                            tag);
            tagListViewHolder.mTagNameTextView.setText(tag.getName());
            tagListViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Increase hash counter. This may affect tag list.
                    tag.setHashCounter(tag.getHashCounter() + 1);
                    TagSettings.updateTag(MainActivity.this, tag);
                    mAdapter.update(tag);

                    // Update last used profile
                    updateLastProfile();

                    // Show dialog
                    showGeneratePasswordDialog(new Tag(tag));
                }
            });
            tagListViewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setMessage(getString(R.string.confirm_delete_tag, tag.getName()));
                    builder.setPositiveButton(R.string.action_delete,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    deleteTag(tag);
                                }
                            });
                    builder.setNegativeButton(android.R.string.cancel, null);
                    builder.create().show();
                    return false;
                }
            });
        }

        public void add(Tag tag, int position) {
            mTags.add(position, tag);
            if (mTags.size() == 1) {
                updateTagListVisibility(true, LIST_EMPTY, null);
            }
            notifyItemInserted(position);
        }

        public void remove(Tag tag) {
            int position = mTags.indexOf(tag);
            mTags.remove(position);
            notifyItemRemoved(position);
            if (mTags.size() == 0) {
                updateTagListVisibility(false, LIST_CONTAINS_ITEMS, null);
            }
        }

        public void update(Tag tag) {
            int oldPosition = 0;
            while (oldPosition < mTags.size() && mTags.get(oldPosition).getId() != tag.getId()) {
                oldPosition++;
            }
            if (oldPosition >= mTags.size()) {
                /* The tag is stored in the database but not in the list,
                because it's a new tag but its customs settings were saved
                during its creation.
                 */
                int newPosition = getTagPosition(tag.getId());
                mTags.add(newPosition, tag);
                notifyItemInserted(newPosition);
            }
            int newPosition = getTagPosition(tag.getId());
            if (oldPosition != newPosition) {
                mTags.remove(tag);
                mTags.add(newPosition, tag);
                notifyItemRemoved(oldPosition);
                notifyItemInserted(newPosition);
            } else {
                mTags.set(oldPosition, tag);
                notifyItemChanged(oldPosition);
            }
        }

        @Override
        public int getItemCount() {
            return mTags.size();
        }
    }

    private void deleteTag(Tag tag) {
        if (TagSettings.deleteTag(this, tag)) {

            // Update tags list
            mAdapter.remove(tag);

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
        GeneratePasswordDialogFragment dialog = new GeneratePasswordDialogFragment();
        dialog.setProfileId(mSelectedProfileId);
        dialog.setTag(tag);
        dialog.setDialogOkListener(this);
        dialog.show(getFragmentManager(), FRAGMENT_GENERATE_PASSWORD);
    }

    public class TagListViewHolder extends RecyclerView.ViewHolder {

        public final TextView mFaviconTextView;
        public final TextView mTagNameTextView;

        public TagListViewHolder(View itemView) {
            super(itemView);
            mFaviconTextView = (TextView) itemView.findViewById(R.id.tag_favicon);
            mTagNameTextView = (TextView) itemView.findViewById(R.id.tag_name);
        }
    }

    private class ProfileSpinnerAdapter implements SpinnerAdapter {

        private final List<Profile> mProfiles;

        public ProfileSpinnerAdapter(List<Profile> profiles) {
            mProfiles = profiles;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            if (convertView == null || convertView.getTag() == null ||
                    !convertView.getTag().equals(TAG_TOOLBAR_SPINNER_ITEM_DROPDOWN)) {
                convertView = LayoutInflater.from(getSupportActionBar().getThemedContext())
                        .inflate(R.layout.toolbar_spinner_item_dropdown, parent, false);
                convertView.setTag(TAG_TOOLBAR_SPINNER_ITEM_DROPDOWN);
            }

            TextView profileNameTextView = (TextView) convertView.findViewById(R.id.profile_name);
            profileNameTextView.setText(mProfiles.get(position).getName());

            if (mProfiles.get(position).getId() == ID_ADD_PROFILE) {
                // Set to italic if this is the last option in the dropdown
                profileNameTextView.setTypeface(null, Typeface.ITALIC);
            } else {
                // Explicitly set to normal, in case convertView was in italics
                profileNameTextView.setTypeface(null, Typeface.NORMAL);
            }

            return profileNameTextView;
        }

        @Override
        public void registerDataSetObserver(DataSetObserver observer) {

        }

        @Override
        public void unregisterDataSetObserver(DataSetObserver observer) {

        }

        @Override
        public int getCount() {
            return mProfiles.size();
        }

        @Override
        public Object getItem(int position) {
            return mProfiles.get(position);
        }

        @Override
        public long getItemId(int position) {
            return mProfiles.get(position).getId();
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null || convertView.getTag() == null ||
                    !convertView.getTag().equals(TAG_TOOLBAR_SPINNER_ITEM_ACTIONBAR)) {
                convertView = LayoutInflater.from(getSupportActionBar().getThemedContext())
                        .inflate(R.layout.toolbar_spinner_item_actionbar, parent, false);
                convertView.setTag(TAG_TOOLBAR_SPINNER_ITEM_ACTIONBAR);
            }

            TextView profileNameTextView = (TextView) convertView.findViewById(R.id.profile_name);
            profileNameTextView.setText(mProfiles.get(position).getName());
            return profileNameTextView;
        }

        @Override
        public int getItemViewType(int position) {
            return IGNORE_ITEM_VIEW_TYPE;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public boolean isEmpty() {
            return mProfiles.size() == 0;
        }
    }
}