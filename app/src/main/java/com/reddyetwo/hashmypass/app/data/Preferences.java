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


package com.reddyetwo.hashmypass.app.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.reddyetwo.hashmypass.app.R;

public class Preferences {

    public static final String PREFS_NAME = "MyPreferences";
    public static final String PREFS_KEY_LAST_PROFILE = "LastProfile";
    public static final String PREFS_KEY_TUTORIAL_PAGE = "tutorialPage";
    public static final String PREFS_KEY_TAG_ORDER = "tagOrder";

    private Preferences() {

    }

    public static int getRememberMasterKeyMins(Context context) {
        SharedPreferences preferences =
                context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return Integer.decode(preferences
                .getString(context.getString(R.string.settings_key_remember_master_key),
                        context.getString(R.string.settings_default_remember_master_key)));
    }

    public static boolean getCopyToClipboard(Context context) {
        SharedPreferences preferences =
                context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(context.getString(R.string.settings_key_copy_to_clipboard),
                context.getResources().getBoolean(R.bool.settings_default_copy_to_clipboard));
    }

    /**
     * Get the tag order preference
     * @param context the context
     * @return the tag order
     */
    public static int getTagOrder(Context context) {
        SharedPreferences preferences =
                context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getInt(PREFS_KEY_TAG_ORDER,
                context.getResources().getInteger(R.integer.settings_default_tag_order));
    }

    /**
     * Set the tag order preference
     * @param context the context
     * @param tagOrder the tag order
     */
    public static void setTagOrder(Context context, int tagOrder) {
        SharedPreferences preferences =
                context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(PREFS_KEY_TAG_ORDER, tagOrder);
        editor.apply();
    }
}
