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

public class HashMyPassApplication extends Application {

    private static char[] mCachedMasterKey = new char[]{};
    private static boolean mTutorialDismissed = false;
    public static final String LOG_TAG = "TWIK";

    public static void setTutorialDismissed(boolean tutorialDismissed) {
        mTutorialDismissed = tutorialDismissed;
    }

    public static boolean getTutorialDismissed() {
        return mTutorialDismissed;
    }

    public static char[] getCachedMasterKey() {
        return mCachedMasterKey;
    }

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
     * @param masterKey the master key
     */
    public static void cacheMasterKey(Context context, char[] masterKey) {
        if (Preferences.getRememberMasterKeyMins(context) > 0) {
            mCachedMasterKey = masterKey;
        }
    }
}
