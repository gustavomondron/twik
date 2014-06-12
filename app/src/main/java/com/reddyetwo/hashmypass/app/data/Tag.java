package com.reddyetwo.hashmypass.app.data;

public class Tag {

    public final static long NO_ID = -1;

    private long mId = NO_ID;
    private long mProfileId;
    private String mSite;
    private String mName;
    private int mPasswordLength;
    private PasswordType mPasswordType;

    public Tag() {
    }

    public Tag(long id, long profileId, String site, String name,
               int passwordLength, PasswordType passwordType) {
        mId = id;
        mProfileId = profileId;
        mSite = site;
        mName = name;
        mPasswordLength = passwordLength;
        mPasswordType = passwordType;
    }

    public long getId() {
        return mId;
    }

    public void setId(long id) {
        mId = id;
    }

    public long getProfileId() {
        return mProfileId;
    }

    public void setProfileId(long profileId) {
        mProfileId = profileId;
    }

    public String getSite() {
        return mSite;
    }

    public void setSite(String site) {
        mSite = site;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public int getPasswordLength() {
        return mPasswordLength;
    }

    public void setPasswordLength(int passwordLength) {
        mPasswordLength = passwordLength;
    }

    public PasswordType getPasswordType() {
        return mPasswordType;
    }

    public void setPasswordType(PasswordType passwordType) {
        mPasswordType = passwordType;
    }
}
