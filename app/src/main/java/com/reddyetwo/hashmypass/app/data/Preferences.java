package com.reddyetwo.hashmypass.app.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.reddyetwo.hashmypass.app.R;

public class Preferences {

    public static final String PREFS_NAME = "MyPreferences";
    public static final String PREFS_KEY_LAST_PROFILE = "LastProfile";

    public static int getRememberMasterKeyMins(Context context) {
        SharedPreferences preferences =
                context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return Integer.decode(preferences.getString(
                context.getString(R.string.settings_key_remember_master_key),
                context.getString(
                        R.string.settings_default_remember_master_key)));
    }
}
