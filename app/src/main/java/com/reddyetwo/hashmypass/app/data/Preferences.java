package com.reddyetwo.hashmypass.app.data;

import android.content.Context;
import android.content.SharedPreferences;

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

    public static boolean getCopyToClipboard(Context context) {
        SharedPreferences preferences =
                context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(context.getString(R.string
                .settings_key_copy_to_clipboard), context.getResources()
                .getBoolean(R.bool.settings_default_copy_to_clipboard));
    }
}
