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

import android.content.res.ColorStateList;
import android.support.design.widget.FloatingActionButton;

/**
 * Utility methods for FAB-related functionality.
 */
public final class FabUtils {

    private static final int[][] FAB_COLOR_STATES = {
            new int[] {},
            new int[] { android.R.attr.state_pressed},
    };

    public static void setFabColor(FloatingActionButton fab, int colorNormal, int colorPressed) {
        fab.setBackgroundTintList(new ColorStateList(FAB_COLOR_STATES, new int[] { colorNormal, colorPressed}));
    }
}
