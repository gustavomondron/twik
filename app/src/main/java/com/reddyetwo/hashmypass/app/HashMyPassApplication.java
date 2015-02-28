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

import android.app.Application;
import android.content.Context;

import com.reddyetwo.hashmypass.app.data.Preferences;

/**
 * Class extending {@link android.app.Application} which contains data which can be accessed
 * from any application Activity
 */
public class HashMyPassApplication extends Application {

    public static final String LOG_TAG = "TWIK";
    private static char[] mCachedMasterKey = new char[]{};
    private static boolean mTutorialDismissed = false;

    /**
     * Get the tutorial dismissed flag.
     *
     * @return true if the tutorial has been dismissed, false otherwise.
     */
    public static boolean getTutorialDismissed() {
        return mTutorialDismissed;
    }

    /**
     * Set the tutorial dismissed flag. Once the tutorial has been dismissed, it is never shown again.
     *
     * @param tutorialDismissed the flag value
     */
    public static void setTutorialDismissed(boolean tutorialDismissed) {
        mTutorialDismissed = tutorialDismissed;
    }

    /**
     * Get the cached master key
     *
     * @param context the {@link android.content.Context} instance
     * @return the cached master key
     */
    public static char[] getCachedMasterKey(Context context) {
        if (Preferences.getRememberMasterKeyMins(context) == 0) {
            mCachedMasterKey = new char[]{};
        }
        return mCachedMasterKey;
    }

    /**
     * Wipe the cached master key
     */
    public static void wipeCachedMasterKey() {
        /* Rewrite the char array so we don't wait for garbage collection
         to destroy it */
        for (int i = 0; i < mCachedMasterKey.length; i++) {
            mCachedMasterKey[i] = ' ';
        }
        mCachedMasterKey = new char[]{};
    }

    /**
     * Save master key to cache if enabled by user
     *
     * @param masterKey the master key
     */
    public static void cacheMasterKey(Context context, char[] masterKey) {
        if (Preferences.getRememberMasterKeyMins(context) > 0) {
            mCachedMasterKey = masterKey;
        }
    }
}
