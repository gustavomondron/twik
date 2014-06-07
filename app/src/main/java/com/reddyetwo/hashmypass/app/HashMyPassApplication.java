package com.reddyetwo.hashmypass.app;

import android.app.Application;

public class HashMyPassApplication extends Application {

    private static HashMyPassApplication mInstance;

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
}
