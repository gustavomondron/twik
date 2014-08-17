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

import android.app.Application;

public class HashMyPassApplication extends Application {

    private static char[] mCachedMasterKey = new char[]{};

    public static char[] getCachedMasterKey() {
        return mCachedMasterKey;
    }

    public static void setCachedMasterKey(char[] masterKey) {
        mCachedMasterKey = masterKey;
    }

    public static void wipeCachedMasterKey() {
        /* Rewrite the char array so we don't wait for garbage collection
         to destroy it */
        for (int i = 0; i < mCachedMasterKey.length; i++) {
            mCachedMasterKey[i] = ' ';
        }
        mCachedMasterKey = new char[]{};
    }
}
