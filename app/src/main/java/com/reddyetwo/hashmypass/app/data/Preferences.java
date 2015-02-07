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

import com.reddyetwo.hashmypass.app.R;

public class Preferences {

    // Shared preferences name
    public static final String PREFS_NAME = "MyPreferences";

    // Preferences keys
    private static final String PREFS_KEY_LAST_PROFILE = "LastProfile";
    private static final String PREFS_KEY_TUTORIAL_PAGE = "tutorialPage";
    private static final String PREFS_KEY_TAG_ORDER = "tagOrder";

    private Preferences() {
    }

    /**
     * Get the time the master key is remembered for
     *
     * @param context the context
     * @return the time
     */
    public static int getRememberMasterKeyMins(Context context) {
        return Integer.decode(context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString(context.getString(R.string.settings_key_remember_master_key),
                        context.getString(R.string.settings_default_remember_master_key)));
    }

    /**
     * Get the preference which enables/disables copying generated passwords to clipboard
     *
     * @param context the context
     * @return the preference value
     */
    public static boolean getCopyToClipboard(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getBoolean(context.getString(R.string.settings_key_copy_to_clipboard),
                        context.getResources()
                                .getBoolean(R.bool.settings_default_copy_to_clipboard));
    }

    /**
     * Get the last shown tutorial page
     *
     * @param context the context
     * @return the page number
     */
    public static int getTutorialPage(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getInt(PREFS_KEY_TUTORIAL_PAGE, 0);
    }

    /**
     * Set the last shown tutorial page
     *
     * @param context the context
     * @param tutorialPage the tutorial page
     */
    public static void setTutorialPage(Context context, int tutorialPage) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
                .putInt(PREFS_KEY_TUTORIAL_PAGE, tutorialPage).apply();
    }

    /**
     * Get the tag order preference
     *
     * @param context the context
     * @return the tag order
     */
    public static int getTagOrder(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getInt(PREFS_KEY_TAG_ORDER,
                        context.getResources().getInteger(R.integer.settings_default_tag_order));
    }

    /**
     * Set the tag order preference
     *
     * @param context  the context
     * @param tagOrder the tag order
     */
    public static void setTagOrder(Context context, int tagOrder) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
                .putInt(PREFS_KEY_TAG_ORDER, tagOrder).apply();
    }

    /**
     * Get the last used profile ID
     *
     * @param context the context
     * @return the last used profile ID or -1 if not defined
     */
    public static long getLastProfile(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getLong(PREFS_KEY_LAST_PROFILE, -1);
    }

    /**
     * Set the last used profile ID
     *
     * @param context the context
     * @param profileId the profile ID
     */
    public static void setLastProfile(Context context, long profileId) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
                .putLong(PREFS_KEY_LAST_PROFILE, profileId).apply();
    }
}
