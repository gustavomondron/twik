package com.reddyetwo.hashmypass.app;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.reddyetwo.hashmypass.app.data.Preferences;
import com.reddyetwo.hashmypass.app.data.Profile;
import com.reddyetwo.hashmypass.app.data.ProfileSettings;
import com.reddyetwo.hashmypass.app.data.Tag;
import com.reddyetwo.hashmypass.app.data.TagSettings;
import com.reddyetwo.hashmypass.app.hash.PasswordHasher;
import com.reddyetwo.hashmypass.app.util.ButtonsEnableTextWatcher;
import com.reddyetwo.hashmypass.app.util.ClipboardHelper;
import com.reddyetwo.hashmypass.app.util.Constants;
import com.reddyetwo.hashmypass.app.util.HelpToastOnLongPressClickListener;
import com.reddyetwo.hashmypass.app.util.MasterKeyAlarmManager;
import com.reddyetwo.hashmypass.app.util.MasterKeyWatcher;
import com.reddyetwo.hashmypass.app.util.TagAutocomplete;

import java.util.List;


public class MainActivity extends Activity {

    // Constants
    private static final int ID_ADD_PROFILE = -1;

    // State keys
    private static final String STATE_SELECTED_PROFILE_ID = "profile_id";
    private static final String STATE_ORIENTATION_HAS_CHANGED =
            "orientation_has_changed";

    // State vars
    private long mSelectedProfileId = -1;
    private boolean mOrientationHasChanged;

    private AutoCompleteTextView mTagEditText;
    private EditText mMasterKeyEditText;
    private TextView mHashedPasswordTextView;
    private TextView mHashedPasswordOldTextView;
    private ButtonsEnableTextWatcher mButtonsEnableTextWatcher;
    private OrientationEventListener mOrientationEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        }

        final TextView digestTextView =
                (TextView) findViewById(R.id.digest_text);

        mTagEditText = (AutoCompleteTextView) findViewById(R.id.tag_text);

        mMasterKeyEditText = (EditText) findViewById(R.id.master_key_text);
        mMasterKeyEditText
                .addTextChangedListener(new MasterKeyWatcher(digestTextView));

        ImageButton tagSettingsButton =
                (ImageButton) findViewById(R.id.tag_settings);
        tagSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mTagEditText.getText().length() > 0) {
                    showTagSettingsDialog();
                }
            }
        });
        tagSettingsButton.setOnLongClickListener(
                new HelpToastOnLongPressClickListener());

        Button hashButton = (Button) findViewById(R.id.hash_button);
        hashButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calculatePasswordHash();

                /* Update last used profile preference */
                SharedPreferences preferences =
                        getSharedPreferences(Preferences.PREFS_NAME,
                                MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putLong(Preferences.PREFS_KEY_LAST_PROFILE,
                        mSelectedProfileId);
                editor.commit();

                /* Automatically copy password to clipboard if the preference
                 is selected
                  */
                if (Preferences.getCopyToClipboard(getApplicationContext())) {
                    ClipboardHelper.copyToClipboard(getApplicationContext(),
                            ClipboardHelper.CLIPBOARD_LABEL_PASSWORD,
                            mHashedPasswordTextView.getText().toString(),
                            R.string.copied_to_clipboard);
                }
            }
        });

        mHashedPasswordTextView = (TextView) findViewById(R.id.hashed_password);
        mHashedPasswordTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mHashedPasswordTextView.getText().length() > 0) {
                    ClipboardHelper.copyToClipboard(getApplicationContext(),
                            ClipboardHelper.CLIPBOARD_LABEL_PASSWORD,
                            mHashedPasswordTextView.getText().toString(),
                            R.string.copied_to_clipboard);
                }
            }
        });

        mHashedPasswordOldTextView =
                (TextView) findViewById(R.id.hashed_password_old);

        Typeface tf =
                Typeface.createFromAsset(getAssets(), Constants.FONT_MONOSPACE);
        mHashedPasswordTextView.setTypeface(tf);
        mHashedPasswordOldTextView.setTypeface(tf);
        digestTextView.setTypeface(tf);

        mButtonsEnableTextWatcher =
                new ButtonsEnableTextWatcher(mTagEditText,
                        mMasterKeyEditText, tagSettingsButton, hashButton);
        mTagEditText.addTextChangedListener(mButtonsEnableTextWatcher);
        mMasterKeyEditText.addTextChangedListener(mButtonsEnableTextWatcher);

        /* Detect orientation changes.
        In the case of an orientation change, we do not select the last used
        profile but the currently selected profile.
         */
        mOrientationEventListener = new OrientationEventListener(this,
                SensorManager.SENSOR_DELAY_NORMAL) {

            @Override
            public void onOrientationChanged(int orientation) {
                mOrientationHasChanged = true;
            }
        };
        mOrientationEventListener.enable();

        // Select the last profile used for hashing a password
        SharedPreferences preferences =
                getSharedPreferences(Preferences.PREFS_NAME, MODE_PRIVATE);
        mSelectedProfileId =
                preferences.getLong(Preferences.PREFS_KEY_LAST_PROFILE, -1);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_ORIENTATION_HAS_CHANGED,
                mOrientationHasChanged);
        if (mSelectedProfileId != ID_ADD_PROFILE) {
            outState.putLong(STATE_SELECTED_PROFILE_ID, mSelectedProfileId);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        /* When the activity has been destroyed because of screen rotation,
        we should restore the profile that the user had selected
         */
        if (savedInstanceState
                .getBoolean(STATE_ORIENTATION_HAS_CHANGED, false)) {
            mSelectedProfileId =
                    savedInstanceState.getLong(STATE_SELECTED_PROFILE_ID, -1);
        }

        // Reset orientation changed flag
        mOrientationHasChanged = false;
    }

    @Override
    protected void onResume() {
        super.onResume();

        MasterKeyAlarmManager.cancelAlarm(this);
        String cachedMasterKey = HashMyPassApplication.getCachedMasterKey();
        if (cachedMasterKey != null) {
            mMasterKeyEditText.setText(cachedMasterKey);
            /* If we have removed the master key, remove also the hashed key */
            if (cachedMasterKey.length() == 0) {
                mHashedPasswordTextView.setText("");
            } else {
                /* Else... populate the tag and the hashed password because
                the activity could be destroyed
                 */
                mTagEditText.setText(HashMyPassApplication.getCachedTag());
                mHashedPasswordTextView.setText(
                        HashMyPassApplication.getCachedHashedPassword());
            }
        }
        HashMyPassApplication.setCachedMasterKey("");
        HashMyPassApplication.setCachedTag("");
        HashMyPassApplication.setCachedHashedPassword("");

        /* We have to re-populate the spinner because a new profile may have
        been added or an existing profile may have been edited or deleted.
         */
        populateActionBarSpinner();
        TagAutocomplete
                .populateTagAutocompleteTextView(this, mSelectedProfileId,
                        mTagEditText);
        mButtonsEnableTextWatcher.updateHashButtonEnabled();

    }

    @Override
    protected void onStop() {
        super.onStop();

        /* Check if we have to remember the master key:
        (a) Remove text from master key edit text in the case that "Remember
        master key" preference is set to never.
        (b) In other case, store the master key in the application class and set
         an alarm to remove it when the alarm is triggered. */
        int masterKeyMins = Preferences.getRememberMasterKeyMins(this);
        if (masterKeyMins == 0) {
            mMasterKeyEditText.setText("");
            mTagEditText.setText("");
            mHashedPasswordTextView.setText("");
        } else {
            HashMyPassApplication.setCachedMasterKey(
                    mMasterKeyEditText.getText().toString());
            HashMyPassApplication
                    .setCachedTag(mTagEditText.getText().toString());
            HashMyPassApplication.setCachedHashedPassword(
                    mHashedPasswordTextView.getText().toString());
            MasterKeyAlarmManager.setAlarm(this, masterKeyMins);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mOrientationEventListener.disable();
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
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        if (requestCode == AddProfileActivity.REQUEST_ADD_PROFILE &&
                resultCode == RESULT_OK) {
            mSelectedProfileId =
                    data.getLongExtra(AddProfileActivity.RESULT_KEY_PROFILE_ID,
                            0);
        }

    }

    private class ProfileAdapter extends ArrayAdapter<Profile> {
        private List<Profile> mProfiles;
        private static final int mResource =
                R.layout.actionbar_simple_spinner_dropdown_item;

        public ProfileAdapter(Context context, List<Profile> objects) {
            super(context, mResource, objects);
            mProfiles = objects;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView =
                        getLayoutInflater().inflate(mResource, parent, false);
            }

            ((TextView) convertView).setText(mProfiles.get(position).getName());
            return convertView;
        }

        @Override
        public View getDropDownView(int position, View convertView,
                                    ViewGroup parent) {
            return getView(position, convertView, parent);
        }
    }

    /* Shows a number picker dialog for choosing the password length */
    private void showTagSettingsDialog() {
        TagSettingsDialogFragment dialogFragment =
                new TagSettingsDialogFragment();
        dialogFragment.setProfileId(mSelectedProfileId);
        dialogFragment.setTag(mTagEditText.getText().toString());

        dialogFragment.show(getFragmentManager(), "tagSettings");
    }

    private void calculatePasswordHash() {
        String tagName = mTagEditText.getText().toString().trim();
        String masterKey = mMasterKeyEditText.getText().toString();

        if (tagName.length() > 0 && masterKey.length() > 0) {
            // Calculate the hashed password
            Tag tag = TagSettings.getTag(this, mSelectedProfileId, tagName);
            Profile profile =
                    ProfileSettings.getProfile(this, mSelectedProfileId);

            // Calculate hashed password
            String hashedPassword = PasswordHasher
                    .hashPassword(tagName, masterKey, profile.getPrivateKey(),
                            tag.getPasswordLength(), tag.getPasswordType());

            String hashedPasswordOld =
                    mHashedPasswordTextView.getText().toString();

            // Update the TextView
            mHashedPasswordTextView.setText(hashedPassword);

            // Animate password text views if different
            if (!hashedPassword.equals(hashedPasswordOld)) {
                // Load "password in" animation
                Animation animationIn = AnimationUtils
                        .loadAnimation(this, R.anim.hashed_password_in);

                // Load "password out" animation for backup view
                Animation animationOut = AnimationUtils
                        .loadAnimation(this, R.anim.hashed_password_out);
                animationOut.setAnimationListener(
                        new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                // Hide backup view once animation ends
                                mHashedPasswordOldTextView
                                        .setVisibility(View.INVISIBLE);
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {

                            }
                        }
                );

                // Update backup view and make visible
                mHashedPasswordOldTextView.setText(hashedPasswordOld);
                mHashedPasswordOldTextView.setVisibility(View.VISIBLE);

                // Animate
                mHashedPasswordTextView.startAnimation(animationIn);
                mHashedPasswordOldTextView.startAnimation(animationOut);
            }

            /* If the tag is not already stored in the database,
            save the current settings and update the AutoCompleteTextView */
            if (tag.getId() == Tag.NO_ID) {
                TagSettings.insertTag(this, tag);
                TagAutocomplete.populateTagAutocompleteTextView(this,
                        mSelectedProfileId, mTagEditText);
            }
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
                                        AddProfileActivity.REQUEST_ADD_PROFILE);
                            } else {
                                mSelectedProfileId = selectedProfile;
                                // Update TagAutoCompleteTextView
                                TagAutocomplete.populateTagAutocompleteTextView(
                                        MainActivity.this, mSelectedProfileId,
                                        mTagEditText);
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

}