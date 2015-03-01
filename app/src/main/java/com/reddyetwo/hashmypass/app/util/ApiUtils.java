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

package com.reddyetwo.hashmypass.app.util;

import android.annotation.TargetApi;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

/**
 * Utility methods for API-dependent functionality
 */
public class ApiUtils {

    private ApiUtils() {
    }

    /**
     * Check if API is equal or higher than LOLLIPOP
     *
     * @return true if API is equal or higher than LOLLIPOP, false otherwise
     */
    public static boolean hasLollipopApi() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    /**
     * Check if API is equal or higher than JELLY BEAN
     *
     * @return true if API is equal or higher than JELLY BEAN, false otherwise
     */
    public static boolean hasJellyBeanApi() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
    }


    /**
     * Draws system bar using primary dark color
     *
     * @param window The activity window
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void colorizeSystemBar(Window window) {
        if (hasLollipopApi()) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        }
    }


    /**
     * Enable drawing behind status bar
     *
     * @param window The activity window
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public static void drawBehindStatusBar(Window window) {
        if (hasJellyBeanApi()) {
            window.getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
    }
}
