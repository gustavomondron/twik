package com.reddyetwo.hashmypass.app;

import android.app.Application;

public class HashMyPassApplication extends Application {

    private static HashMyPassApplication mInstance;

    private static String mCachedTag;
    private static String mCachedHashedPassword;
    private static String mCachedMasterKey;

    public HashMyPassApplication getInstance() {
        return mInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
    }

    public static String getCachedMasterKey() {
        return mCachedMasterKey;
    }

    public static void setCachedMasterKey(String masterKey) {
        mCachedMasterKey = masterKey;
    }

    public static String getCachedTag() {
        return mCachedTag;
    }

    public static void setCachedTag(String cachedTag) {
        mCachedTag = cachedTag;
    }

    public static String getCachedHashedPassword() {
        return mCachedHashedPassword;
    }

    public static void setCachedHashedPassword(String cachedHashedPassword) {
        mCachedHashedPassword = cachedHashedPassword;
    }
}
