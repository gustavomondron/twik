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
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.reddyetwo.hashmypass.app.cards.MasterKeyCard;
import com.reddyetwo.hashmypass.app.cards.NewCardView;
import com.reddyetwo.hashmypass.app.cards.OnMasterKeyChangedListener;
import com.reddyetwo.hashmypass.app.cards.OnTagSelectedListener;
import com.reddyetwo.hashmypass.app.cards.SelectedTagCard;
import com.reddyetwo.hashmypass.app.cards.TagCardHeader;
import com.reddyetwo.hashmypass.app.cards.TagListCard;
import com.reddyetwo.hashmypass.app.data.PasswordType;
import com.reddyetwo.hashmypass.app.data.Preferences;
import com.reddyetwo.hashmypass.app.data.Profile;
import com.reddyetwo.hashmypass.app.data.ProfileSettings;
import com.reddyetwo.hashmypass.app.data.Tag;
import com.reddyetwo.hashmypass.app.data.TagSettings;
import com.reddyetwo.hashmypass.app.hash.PasswordHasher;
import com.reddyetwo.hashmypass.app.util.Fab;
import com.reddyetwo.hashmypass.app.util.FaviconLoader;
import com.reddyetwo.hashmypass.app.util.MasterKeyAlarmManager;

import java.util.List;


public class MainActivity extends Activity
        implements AddDefaultProfileDialog.OnProfileAddedListener,
        OnTagSelectedListener, OnMasterKeyChangedListener {

    // Constants
    private static final int ID_ADD_PROFILE = -1;
    private static final int REQUEST_ADD_PROFILE = 1;
    private static final int REQUEST_CREATE_DEFAULT_PROFILE = 2;
    private static final int TAG_LIST_MAX_SIZE = 25;

    // State keys
    private static final String STATE_SELECTED_PROFILE_ID = "profile_id";
    private static final String STATE_ORIENTATION_HAS_CHANGED =
            "orientation_has_changed";
    private static final String STATE_CARD_AUTOCOMPLETE_IS_SHOWN =
            "autocomplete_is_shown";
    private static final String STATE_CARD_TAG_SETTINGS_ARE_SHOWN =
            "settings_are_shown";
    private static final String STATE_CARD_HASHED_PASSWORD_IS_SHOWN =
            "hashed_password_is_shown";
    private static final String STATE_CARD_TAG_ID = "tag_id";
    private static final String STATE_CARD_TAG_NAME = "tag_name";
    private static final String STATE_CARD_PASSWORD_LENGTH = "password_length";
    private static final String STATE_CARD_PASSWORD_TYPE = "password_type";
    private static final String STATE_CARD_MASTER_KEY_IS_SHOWN =
            "master_key_is_shown";

    // State vars
    private long mSelectedProfileId = -1;
    private OrientationEventListener mOrientationEventListener;
    private boolean mOrientationHasChanged;
    private Tag mTagToRestore;

    // Cards
    private SelectedTagCard mSelectedTagCard;
    private MasterKeyCard mMasterKeyCard;
    private TagListCard mTagListCard;

    private ScrollView mBaseView;

    private Tag mTag;
    private String mMasterKey;

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

        ListView newTagList = (ListView) findViewById(R.id.new_tag_list);
        newTagList.setAdapter(new TagAdapter(this, TagSettings
                .getProfileTags(this, mSelectedProfileId,
                        TagSettings.ORDER_BY_HASH_COUNTER,
                        TagSettings.LIMIT_UNBOUNDED)));

        Fab mFab = (Fab)findViewById(R.id.fabbutton);
        mFab.setFabColor(getResources().getColor(R.color.hashmypass_main));
        mFab.setFabDrawable(getResources().getDrawable(R.drawable
                .ic_action_add));
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });

        // Create cards
/*
        mSelectedTagCard = new SelectedTagCard(this, this, mSelectedProfileId);
        NewCardView selectedTagCardView =
                (NewCardView) findViewById(R.id.card_selected_tag);
        selectedTagCardView.setCard(mSelectedTagCard);

        mMasterKeyCard = new MasterKeyCard(this, this);
        NewCardView masterKeyCardView =
                (NewCardView) findViewById(R.id.card_master_key);
        masterKeyCardView.setCard(mMasterKeyCard);

        NewCardView listCardView =
                (NewCardView) findViewById(R.id.card_tags_list);
        mTagListCard = new TagListCard(this, this);
        mTagListCard.init();
        listCardView.setCard(mTagListCard);

        mBaseView = (ScrollView) findViewById(R.id.main_base);

        if (savedInstanceState != null) {
            restoreSelectedTagCardState(savedInstanceState);
            if (savedInstanceState.getBoolean(STATE_CARD_MASTER_KEY_IS_SHOWN)) {
                masterKeyCardView.setVisibility(View.VISIBLE);
                mMasterKey = mMasterKeyCard.getMasterKey();
            }

            if (savedInstanceState
                    .getBoolean(STATE_ORIENTATION_HAS_CHANGED, false)) {
                mSelectedProfileId = savedInstanceState
                        .getLong(STATE_SELECTED_PROFILE_ID, -1);
            }
        }
*/

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
        if (!showTutorial()) {
            restoreCachedMasterKey();
            populateActionBarSpinner();
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

/*        // Selected tag card
        TagCardHeader tagHeader =
                (TagCardHeader) mSelectedTagCard.getCardHeader();
        outState.putBoolean(STATE_CARD_AUTOCOMPLETE_IS_SHOWN,
                tagHeader.autoCompleteIsShown());
        outState.putBoolean(STATE_CARD_TAG_SETTINGS_ARE_SHOWN,
                mSelectedTagCard.tagSettingsAreShown());
        outState.putBoolean(STATE_CARD_HASHED_PASSWORD_IS_SHOWN,
                mSelectedTagCard.hashedPasswordIsShown());
        Tag tag = tagHeader.getTag();
        if (tag != null) {
            outState.putLong(STATE_CARD_TAG_ID, tag.getId());
            if (tag.getId() == Tag.NO_ID) {
                // This data is not saved in storage
                outState.putString(STATE_CARD_TAG_NAME, tag.getName());
                outState.putInt(STATE_CARD_PASSWORD_LENGTH,
                        tag.getPasswordLength());
                outState.putInt(STATE_CARD_PASSWORD_TYPE,
                        tag.getPasswordType().ordinal());
            }
        }

        // Master key card
        outState.putBoolean(STATE_CARD_MASTER_KEY_IS_SHOWN,
                mMasterKeyCard.getCardView().getVisibility() == View.VISIBLE);*/

        // Activity state
        outState.putBoolean(STATE_ORIENTATION_HAS_CHANGED,
                mOrientationHasChanged);
        outState.putLong(STATE_SELECTED_PROFILE_ID, mSelectedProfileId);

    }

    private void restoreSelectedTagCardState(Bundle savedInstanceState) {
        TagCardHeader tagHeader =
                (TagCardHeader) mSelectedTagCard.getCardHeader();

        long tagId = savedInstanceState.getLong(STATE_CARD_TAG_ID);
        if (tagId == Tag.NO_ID) {
            String tagName = savedInstanceState.getString(STATE_CARD_TAG_NAME);
            int passwordLength =
                    savedInstanceState.getInt(STATE_CARD_PASSWORD_LENGTH);
            PasswordType passwordType = PasswordType.values()[savedInstanceState
                    .getInt(STATE_CARD_PASSWORD_TYPE)];
            mTag = new Tag(Tag.NO_ID, mSelectedProfileId, 0, null, tagName,
                    passwordLength, passwordType);
        } else {
            mTag = TagSettings.getTag(this, tagId);
        }

        if (mTag != null) {
            mSelectedTagCard.setTag(mTag);
        }

        boolean autoCompleteIsShown =
                savedInstanceState.getBoolean(STATE_CARD_AUTOCOMPLETE_IS_SHOWN);
        if (autoCompleteIsShown) {
            tagHeader.toggleAutocomplete();
        }

        boolean tagSettingsAreShown = savedInstanceState
                .getBoolean(STATE_CARD_TAG_SETTINGS_ARE_SHOWN);
        if (tagSettingsAreShown) {
            tagHeader.toggleOverflow();
        }

        boolean hashedPasswordIsShown = savedInstanceState
                .getBoolean(STATE_CARD_HASHED_PASSWORD_IS_SHOWN);
        if (hashedPasswordIsShown) {
            mSelectedTagCard.showHashedPassword();
        } else {
            mSelectedTagCard.showHashAction();
        }
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
        } else if (id == R.id.action_manage_tags) {
            Intent intent = new Intent(this, ManageTagsActivity.class);
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
                                Intent intent =
                                        new Intent(MainActivity.this,
                                                AddProfileActivity.class);
                                startActivityForResult(intent,
                                        REQUEST_ADD_PROFILE);
                            } else {
                                mSelectedProfileId = selectedProfile;
                                // Update cards
/*
                                mSelectedTagCard
                                        .setProfileId(mSelectedProfileId);
                                List<Tag> tags = TagSettings
                                        .getProfileTags(MainActivity.this,
                                                mSelectedProfileId,
                                                TagSettings.ORDER_BY_HASH_COUNTER,
                                                TAG_LIST_MAX_SIZE);
                                if (tags.size() > 0) {
                                    mTagListCard.updateTags(tags);
                                    if (mTagToRestore != null) {
                                        mTag = mTagToRestore;
                                        mTagToRestore = null;
                                    } else {
                                        mTag = tags.get(0);
                                    }
                                    mSelectedTagCard.setTag(mTag);
                                } else {
                                    mTagListCard.clearTags();
                                    mSelectedTagCard.clear();
                                }
*/

                                // Recalculate hashed password
                                onMasterKeyChanged(mMasterKey);
                            }
                            return false;
                        }
                    }
            );

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
    }

    @Override
    public void onCanceled() {
        // You didn't add the profile! Nothing to do here!
        finish();
    }

    @Override
    public void onTagSelected(Tag tag, boolean fromList, View view) {
        CardView cardView = (CardView) findViewById(R.id.native_cardview);
        Animation animationIn = AnimationUtils
                .loadAnimation(MainActivity.this,
                        R.anim.hashed_password_in);
        cardView.setVisibility(View.VISIBLE);
        cardView.setX(view.getX());
        cardView.setY(view.getY());
        cardView.startAnimation(animationIn);


//        mTag = tag;
//        // Save the profile used
//        updateLastProfile();
//
//        // Add +1 to the hash counter
//        mTag.setHashCounter(mTag.getHashCounter() + 1);
//
//        if (fromList) {
//            mSelectedTagCard.setTag(tag);
//            mSelectedTagCard.selectTag();
//            updateHashedPassword();
//
//            // Save the tag to update the hash counter
//            TagSettings.updateTag(this, mTag);
//
//            // Go to the top of ScrollView to show the password
//            mBaseView.fullScroll(ScrollView.FOCUS_UP);
//
//        } else {
//            // Save the tag in the case that it has been just created
//            if (mTag.getId() == Tag.NO_ID) {
//                mTag.setId(TagSettings.insertTag(this, mTag));
//                // Update the list and the selected tag card
//                mTagListCard.addTag(mTag);
//                mSelectedTagCard.setTag(mTag);
//            }
//        }
//
//        mMasterKeyCard.getCardView().setVisibility(View.VISIBLE);
//        /* The soft keyboard should be automatically shown only if the
//        master key is not already filled.
//         */
//        if (mMasterKey == null || mMasterKey.length() == 0) {
//            mMasterKeyCard.showSoftKeyboard();
//        }
    }

    @Override
    public void onTagSettingsChanged(Tag tag) {
        updateHashedPassword();
    }

    @Override
    public void onMasterKeyChanged(String masterKey) {
        mMasterKey = masterKey;
        updateHashedPassword();
    }

    private void updateHashedPassword() {
        if (mTag != null && mMasterKey != null && mMasterKey.length() > 0) {
            String hashedPassword = PasswordHasher
                    .hashPassword(mTag.getName(), mMasterKey,
                            ProfileSettings.getProfile(this, mSelectedProfileId)
                                    .getPrivateKey(), mTag.getPasswordLength(),
                            mTag.getPasswordType()
                    );
            //mSelectedTagCard.setHashedPassword(hashedPassword);
        } else {
            //mSelectedTagCard.setHashedPassword("");
        }
    }

    private void updateLastProfile() {
                /* Update last used profile preference */
        SharedPreferences preferences =
                getSharedPreferences(Preferences.PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong(Preferences.PREFS_KEY_LAST_PROFILE, mSelectedProfileId);
        editor.commit();
    }

    private long getLastProfile() {
        // Select the last profile used for hashing a password
        SharedPreferences preferences =
                getSharedPreferences(Preferences.PREFS_NAME, MODE_PRIVATE);
        return preferences.getLong(Preferences.PREFS_KEY_LAST_PROFILE, -1);
    }

    private boolean showTutorial() {
        // Check if tutorial should be shown
        SharedPreferences preferences =
                getSharedPreferences(Preferences.PREFS_NAME, MODE_PRIVATE);
        boolean showTutorial = preferences
                .getBoolean(Preferences.PREFS_KEY_SHOW_TUTORIAL, getResources()
                        .getBoolean(R.bool.settings_default_show_tutorial));
        if (showTutorial) {
            Intent intent = new Intent(this, TutorialActivity.class);
            startActivity(intent);
            return true;
        } else if (ProfileSettings.getList(this).size() == 0) {
            // Check if a profile is already defined
            AddDefaultProfileDialog addProfileDialog =
                    new AddDefaultProfileDialog();
            addProfileDialog.setOnProfileAddedListener(this);
            addProfileDialog.show(getFragmentManager(), "addDefaultProfile");
            return true;
        } else {
            return false;
        }
    }

    private void restoreCachedMasterKey() {
        MasterKeyAlarmManager.cancelAlarm(this);
        String cachedMasterKey = HashMyPassApplication.getCachedMasterKey();
        if (cachedMasterKey != null) {
            mMasterKeyCard.setMasterKey(cachedMasterKey);
            mMasterKeyCard.getCardView().setVisibility(View.VISIBLE);
            mSelectedTagCard.showHashedPassword();

            // Restore tag if cached
            if (HashMyPassApplication.getCachedTag() != null &&
                    HashMyPassApplication.getCachedTag().length() > 0) {
                mTagToRestore = TagSettings.getTag(this, mSelectedProfileId,
                        HashMyPassApplication.getCachedTag());
            }
        }
        HashMyPassApplication.setCachedMasterKey("");
    }

    private void cacheMasterKey() {
        /* Check if we have to remember the master key:
            (a) Remove text from master key edit text in the case that "Remember
                master key" preference is set to never.
            (b) In other case, store the master key in the application class and set
                an alarm to remove it when the alarm is triggered.
        */
        int masterKeyMins = Preferences.getRememberMasterKeyMins(this);
        if (masterKeyMins == 0) {
            mMasterKeyCard.setMasterKey("");
        } else {
            HashMyPassApplication
                    .setCachedMasterKey(mMasterKeyCard.getMasterKey());
            HashMyPassApplication.setCachedTag(mTag.getName());
            MasterKeyAlarmManager.setAlarm(this, masterKeyMins);
        }
    }

    private class TagAdapter extends ArrayAdapter<Tag> {

        private List<Tag> mTags;
        private static final int mResource = R.layout.main_tag_list_item;

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
            TextView faviconTextView =
                    (TextView) convertView.findViewById(R.id.tag_favicon);
            TextView tagName =
                    (TextView) convertView.findViewById(R.id.tag_name);
            tagName.setText(tag.getName());
            FaviconLoader.setAsBackground(getContext(), faviconTextView, tag);

            tagName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    GeneratePasswordDialogFragment dialog = new
                            GeneratePasswordDialogFragment();
                    dialog.show(getFragmentManager(), "generatePassword");
                }
            });

            return convertView;
        }
    }
}