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

import android.graphics.Bitmap;

/**
 * POJO class for favicons
 */
public class Favicon {

    public static final long NO_ID = -1;

    private final long mId;
    private final String mSite;
    private final Bitmap mIcon;

    /**
     * Constructor
     *
     * @param id   the ID
     * @param site the site
     * @param icon the icon
     */
    public Favicon(long id, String site, Bitmap icon) {
        mId = id;
        mSite = site;
        mIcon = icon;
    }

    /**
     * Get the favicon ID
     *
     * @return the favicon ID
     */
    public long getId() {
        return mId;
    }

    /**
     * Get the favicon site
     *
     * @return the favicon site
     */
    public String getSite() {
        return mSite;
    }

    /**
     * Get the icon {@link android.graphics.Bitmap}
     *
     * @return the icon
     */
    public Bitmap getIcon() {
        return mIcon;
    }
}
