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
import android.animation.AnimatorSet;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.OrientationEventListener;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.melnykov.fab.FloatingActionButton;
import com.reddyetwo.hashmypass.app.adapter.ProfileSpinnerAdapter;
import com.reddyetwo.hashmypass.app.adapter.TagListAdapter;
import com.reddyetwo.hashmypass.app.animation.Animations;
import com.reddyetwo.hashmypass.app.data.Favicon;
import com.reddyetwo.hashmypass.app.data.FaviconSettings;
import com.reddyetwo.hashmypass.app.data.Preferences;
import com.reddyetwo.hashmypass.app.data.Profile;
import com.reddyetwo.hashmypass.app.data.ProfileSettings;
import com.reddyetwo.hashmypass.app.data.Tag;
import com.reddyetwo.hashmypass.app.data.TagSettings;
import com.reddyetwo.hashmypass.app.util.ApiUtils;
import com.reddyetwo.hashmypass.app.util.MasterKeyAlarmManager;

import java.util.List;


public class MainActivity extends ActionBarActivity
        implements GeneratePasswordDialogFragment.GeneratePasswordDialogListener {

    // Constants
    private static final int REQUEST_ADD_PROFILE = 1;
    private static final int REQUEST_CREATE_DEFAULT_PROFILE = 2;
    private static final String FRAGMENT_GENERATE_PASSWORD = "generatePassword";
    private static final long LIST_ANIMATION_DURATION = 150;

    /**
     * Tag list state: List is empty
     */
    private static final int LIST_EMPTY = 1;

    /**
     * Tag list state: List contains items
     */
    private static final int LIST_CONTAINS_ITEMS = 2;

    /**
     * Tag list state: Undefined (no adapter available)
     */
    private static final int LIST_NOT_INITIALIZED = 3;


    // State keys
    private static final String STATE_SELECTED_PROFILE_ID = "profile_id";
    private static final String STATE_ORIENTATION_HAS_CHANGED = "orientation_has_changed";

    /**
     * Selected profile ID
     */
    private long mSelectedProfileId = -1;

    /**
     * Listener for clicks on tag list items
     */
    private TagListAdapter.OnTagClickedListener mTagClickedListener;

    /**
     * Screen orientation changes listener
     */
    private OrientationEventListener mOrientationEventListener;

    /**
     * Screen orientation changed flag
     */
    private boolean mOrientationHasChanged;

    /**
     * Color palette - normal state
     */
    private int[] mColorsNormal;

    /**
     * Color palette - pressed state
     */
    private int[] mColorsPressed;

    /**
     * Color palette - ripple state
     */
    private int[] mColorsRipple;

    private RecyclerView mTagRecyclerView;
    private LinearLayout mEmptyListLayout;
    private TagListAdapter mAdapter;
    private FloatingActionButton mFab;
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Use primary dark color for the status bar
        ApiUtils.colorizeSystemBar(getWindow());

        findViews();
        addToolbar();

        // Select the last profile used for hashing a password
        mSelectedProfileId = getLastProfile();

        // Get palette colors
        mColorsNormal = getResources().getIntArray(R.array.color_palette_normal);
        mColorsPressed = getResources().getIntArray(R.array.color_palette_pressed);
        mColorsRipple = getResources().getIntArray(R.array.color_palette_ripple);

        // Init UI elements
        initTagList();
        initFab();

        restoreSelectedProfile(savedInstanceState);

        // Add listeners
        addOrientationChangedListener();
        addPasswordDialogListener(savedInstanceState);
        addTagClickedListener();
    }

    /**
     * Detect orientation changes. In the case of an orientation change, the last used profile
     * is not selected, but the currently selected profile.
     */
    private void addOrientationChangedListener() {
        mOrientationHasChanged = false;
        mOrientationEventListener =
                new OrientationEventListener(this, SensorManager.SENSOR_DELAY_NORMAL) {

                    @Override
                    public void onOrientationChanged(int orientation) {
                        mOrientationHasChanged = true;
                    }
                };
        mOrientationEventListener.enable();
    }

    /**
     * If the password generation dialog was shown, we have to restore the listener.
     *
     * @param savedInstanceState The saved instance state
     */
    private void addPasswordDialogListener(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            GeneratePasswordDialogFragment fragment =
                    (GeneratePasswordDialogFragment) getFragmentManager()
                            .findFragmentByTag(FRAGMENT_GENERATE_PASSWORD);
            if (fragment != null) {
                fragment.setDialogOkListener(this);
            }
        }
    }

    private void addTagClickedListener() {
        mTagClickedListener = new TagClickListener();
    }

    private void restoreSelectedProfile(Bundle savedInstanceState) {
        if (savedInstanceState != null &&
                savedInstanceState.getBoolean(STATE_ORIENTATION_HAS_CHANGED)) {
            mSelectedProfileId = savedInstanceState.getLong(STATE_SELECTED_PROFILE_ID);
        }
    }

    private void initFab() {
        mFab.attachToRecyclerView(mTagRecyclerView);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Profile profile = ProfileSettings.getProfile(MainActivity.this, mSelectedProfileId);
                Tag tag = new Tag(Tag.NO_ID, mSelectedProfileId, 1, null, "",
                        profile.getPasswordLength(), profile.getPasswordType());
                showGeneratePasswordDialog(tag);
            }
        });
    }

    private void findViews() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mTagRecyclerView = (RecyclerView) findViewById(R.id.tag_list);
        mEmptyListLayout = (LinearLayout) findViewById(R.id.list_empty);
        mFab = (FloatingActionButton) findViewById(R.id.fab);
    }

    private void initTagList() {
        mTagRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mTagRecyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    private void addToolbar() {
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    /**
     * Check if tutorial should be shown
     *
     * @return true if tutorial should be shown, false otherwise
     */
    private boolean shouldShowTutorial() {
        return ProfileSettings.getList(this).isEmpty();
    }

    private void showTutorial() {
        Intent intent = new Intent(this, TutorialActivity.class);
        startActivity(intent);
    }

    /**
     * Update FAB color to match the currently selected profile
     */
    private void updateFabColor() {
        if (mSelectedProfileId != Profile.NO_ID) {
            Profile profile = ProfileSettings.getProfile(this, mSelectedProfileId);
            /* Warning: the profile could have been removed. In that case,
            we'll select the first profile in the list */
            if (profile == null) {
                profile = ProfileSettings.getList(this).get(0);
                mSelectedProfileId = profile.getId();
            }
            int colorIndex = profile.getColorIndex();
            setFabColor(mColorsNormal[colorIndex], mColorsPressed[colorIndex],
                    mColorsRipple[colorIndex]);
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

        if (shouldShowTutorial()) {
            showTutorial();
        } else {
            populateToolBarSpinner();

            // Cancel the master key alarm to clear cache
            MasterKeyAlarmManager.cancelAlarm(this);

            // Update tag list (may have been changed using the web browser)
            populateTagList();

            updateFabColor();
        }
    }

    private void populateTagList() {
        final List<Tag> tags = TagSettings
                .getProfileTags(this, mSelectedProfileId, TagSettings.ORDER_BY_HASH_COUNTER,
                        TagSettings.LIMIT_UNBOUNDED);
        final int stateBeforeUpdating = getTagListState();

        if (stateBeforeUpdating == LIST_NOT_INITIALIZED) {
            mAdapter = new TagListAdapter(this, mSelectedProfileId, mTagClickedListener, tags);
            mTagRecyclerView.setAdapter(mAdapter);
        } else {
            mAdapter.setProfileId(mSelectedProfileId);
        }

        if (stateBeforeUpdating == LIST_EMPTY) {
            mAdapter.setTags(tags);
        }

        // When stateBeforeUpdating == LIST_CONTAINS_ITEMS and the tag list of the current profile
        // is not empty, the user has selected a different profile and the adapter needs to be
        // updated by the animator.

        // Update tag list visibility
        updateTagListView(stateBeforeUpdating, tags);
    }

    /**
     * Get the tag position in the list of tags
     *
     * @param id the tag ID
     * @return the position of the tag in the list
     */
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
     * Update the visibility of the tag list.
     * This method is also used when a different profile has been selected.
     * When the user selects a different profile, a different tag list is generated and the adapter
     * must be updated.
     */
    private void updateTagListView(final int stateBeforeUpdating, final List<Tag> newTags) {
        if (!newTags.isEmpty()) {
            switch (stateBeforeUpdating) {
                case LIST_NOT_INITIALIZED:
                    // Make tag list visible
                    Animations.getToVisibleAnimatorSet(this, mTagRecyclerView).start();
                    break;
                case LIST_EMPTY:
                    // Hide empty view and show tag list
                    animateEmptyViewToVisibleListTransition();
                    break;
                case LIST_CONTAINS_ITEMS:
                    // Hide outdated tag list and then show tag list with updated tags
                    animateListTransition(mTagRecyclerView, mTagRecyclerView, newTags);
                    break;
                default:
            }
        } else {
            switch (stateBeforeUpdating) {
                case LIST_NOT_INITIALIZED:
                    // Show empty view
                    Animations.getToVisibleAnimatorSet(this, mEmptyListLayout).start();
                    break;
                case LIST_CONTAINS_ITEMS:
                    // Hide tag list and show empty view
                    animateListTransition(mTagRecyclerView, mEmptyListLayout, newTags);
                    break;
                default:
            }
        }
    }

    private void animateEmptyViewToVisibleListTransition() {
        AnimatorSet invisibleAnimator =
                Animations.getToInvisibleAnimatorSet(this, mEmptyListLayout);
        AnimatorSet visibleAnimator = Animations.getToVisibleAnimatorSet(this, mTagRecyclerView);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(visibleAnimator, invisibleAnimator);
        animatorSet.start();
    }

    private void animateListTransition(Object toInvisibleObject, Object toVisibleObject,
                                       List<Tag> newTags) {
        AnimatorSet invisibleAnimator = Animations
                .getToInvisibleAnimatorSet(this, toInvisibleObject, LIST_ANIMATION_DURATION);
        AnimatorSet visibleAnimator =
                Animations.getToVisibleAnimatorSet(this, toVisibleObject, LIST_ANIMATION_DURATION);
        initTagListProfileChangedAnimator(invisibleAnimator, visibleAnimator, newTags);
        invisibleAnimator.start();

    }

    private void initTagListProfileChangedAnimator(final AnimatorSet invisibleAnimator,
                                                   final AnimatorSet visibleAnimator,
                                                   final List<Tag> tags) {
        invisibleAnimator
                .addListener(new TagListProfileChangedAnimatorListener(visibleAnimator, tags));
    }

    private int getTagListState() {
        int state;
        if (mAdapter == null) {
            state = LIST_NOT_INITIALIZED;
        } else if (mAdapter.getItemCount() == 0) {
            state = LIST_EMPTY;
        } else {
            state = LIST_CONTAINS_ITEMS;
        }
        return state;
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
                if (resultCode == RESULT_OK) {
                    addProfile(data.getLongExtra(AddProfileActivity.RESULT_KEY_PROFILE_ID, 0));
                }
                break;
            case REQUEST_CREATE_DEFAULT_PROFILE:
                if (resultCode == RESULT_CANCELED) {
                    finish();
                }
                break;
            default:
        }
    }

    private void addProfile(long profileId) {
        mSelectedProfileId = profileId;
        int colorIndex = ProfileSettings.getProfile(this, mSelectedProfileId).getColorIndex();
        setFabColor(mColorsNormal[colorIndex], mColorsPressed[colorIndex],
                mColorsRipple[colorIndex]);
        populateTagList();
    }

    private void setFabColor(int colorNormal, int colorPressed, int colorRipple) {
        mFab.setColorNormal(colorNormal);
        mFab.setColorPressed(colorPressed);
        mFab.setColorRipple(colorRipple);
    }

    private void populateToolBarSpinner() {
        if (mToolbar != null) {
            final List<Profile> profiles = ProfileSettings.getList(this);
            Profile addProfile = new Profile();
            addProfile.setName(getString(R.string.action_add_profile));
            profiles.add(addProfile);
            ProfileSpinnerAdapter spinnerAdapter =
                    new ProfileSpinnerAdapter(getSupportActionBar().getThemedContext(), profiles);

            Spinner spinner = (Spinner) findViewById(R.id.spinner_nav);
            spinner.setAdapter(spinnerAdapter);
            spinner.setOnItemSelectedListener(new SpinnerItemSelectedListener());

            /* If we had previously selected a profile before pausing the
            activity and it still exists, select it in the spinner. */
            int position = mSelectedProfileId == Profile.NO_ID ? 0 :
                    profiles.indexOf(ProfileSettings.getProfile(this, mSelectedProfileId));


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
            if (mAdapter.getItemCount() == 1) {
                updateTagListView(LIST_EMPTY, mAdapter.getTags());
            }

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

    private void deleteTag(Tag tag) {
        if (TagSettings.deleteTag(this, tag)) {

            // Update tags list
            mAdapter.remove(tag);

            if (mAdapter.getItemCount() == 0) {
                updateTagListView(LIST_CONTAINS_ITEMS, mAdapter.getTags());
            }

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


    private class TagClickListener implements TagListAdapter.OnTagClickedListener {

        @Override
        public void onTagClicked(final Tag tag) {
            updateLastProfile();
            showGeneratePasswordDialog(tag);
        }

        @Override
        public void onTagLongClicked(final Tag tag) {
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
        }
    }

    private class SpinnerItemSelectedListener implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            if (id == Profile.NO_ID) {
                Intent intent = new Intent(MainActivity.this, AddProfileActivity.class);
                startActivityForResult(intent, REQUEST_ADD_PROFILE);
            } else if (id != mSelectedProfileId) {
                mSelectedProfileId = id;
                populateTagList();
                updateFabColor();
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            // Nothing to do
        }

    }

    private class TagListProfileChangedAnimatorListener implements Animator.AnimatorListener {

        private final AnimatorSet mVisibleAnimator;
        private final List<Tag> mTags;

        public TagListProfileChangedAnimatorListener(AnimatorSet visibleAnimator, List<Tag> tags) {
            mVisibleAnimator = visibleAnimator;
            mTags = tags;
        }

        @Override
        public void onAnimationStart(Animator animation) {
            // Nothing to do
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            // Update list tags
            mAdapter.setProfileId(mSelectedProfileId);
            mAdapter.setTags(mTags);
            mVisibleAnimator.start();
        }

        @Override
        public void onAnimationCancel(Animator animation) {
            // Nothing to do
        }

        @Override
        public void onAnimationRepeat(Animator animation) {
            // Nothing to do
        }
    }
}