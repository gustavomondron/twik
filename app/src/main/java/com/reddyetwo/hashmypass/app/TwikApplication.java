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

import com.reddyetwo.hashmypass.app.data.Preferences;
import com.squareup.leakcanary.LeakCanary;

import java.util.Arrays;

/**
 * Class extending {@link android.app.Application} which contains data which can be accessed
 * from any application Activity
 */
public class TwikApplication extends Application {

    public static final String LOG_TAG = "TWIK";
    private char[] mCachedMasterKey;
    private boolean mTutorialDismissed = false;
    private static TwikApplication mInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        LeakCanary.install(this);
    }

    /**
     * Return the Singleton instance of the {@link android.app.Application}
     * @return the Singleton instance of the {@link android.app.Application}
     */
    public static TwikApplication getInstance() {
        return mInstance;
    }

    /**
     * Get the tutorial dismissed flag.
     *
     * @return true if the tutorial has been dismissed, false otherwise.
     */
    public boolean getTutorialDismissed() {
        return mTutorialDismissed;
    }

    /**
     * Set the tutorial dismissed flag. Once the tutorial has been dismissed, it is never shown again.
     *
     * @param tutorialDismissed the flag value
     */
    public void setTutorialDismissed(boolean tutorialDismissed) {
        mTutorialDismissed = tutorialDismissed;
    }

    /**
     * Get the cached master key
     *
     * @return the cached master key
     */
    public char[] getCachedMasterKey() {
        if (mCachedMasterKey == null || Preferences.getRememberMasterKeyMins(this) == 0) {
            mCachedMasterKey = new char[]{};
        }
        return mCachedMasterKey;
    }

    /**
     * Wipe the cached master key
     */
    public void wipeCachedMasterKey() {
        if (mCachedMasterKey != null) {
            Arrays.fill(mCachedMasterKey, ' ');
            mCachedMasterKey = null;
        }
    }

    /**
     * Save master key to cache if enabled by user
     *
     * @param masterKey the master key
     */
    public void cacheMasterKey(char[] masterKey) {
        if (Preferences.getRememberMasterKeyMins(this) > 0) {
            mCachedMasterKey = Arrays.copyOf(masterKey, masterKey.length);
        }
    }
}
