package com.reddyetwo.hashmypass.app.data;

import android.graphics.Bitmap;

public class Favicon {

    public static final long NO_ID = -1;

    private long mId;
    private String mSite;
    private Bitmap mIcon;

    public Favicon(long id, String site, Bitmap icon) {
        mId = id;
        mSite = site;
        mIcon = icon;
    }

    public long getId() {
        return mId;
    }

    public String getSite() {
        return mSite;
    }

    public Bitmap getIcon() {
        return mIcon;
    }
}
