package com.reddyetwo.hashmypass.app.data;

public class Profile {

    public final static long NO_ID = -1;

    private long mId = NO_ID;
    private String mName;
    private String mPrivateKey;
    private int mPasswordLength;
    private PasswordType mPasswordType;

    public Profile() {
    }

    public Profile(long id, String name, String privateKey, int passwordLength,
                   PasswordType passwordType) {
        mId = id;
        mName = name;
        mPrivateKey = privateKey;
        mPasswordLength = passwordLength;
        mPasswordType = passwordType;
    }

    public long getId() {
        return mId;
    }

    public void setId(long id) {
        mId = id;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getPrivateKey() {
        return mPrivateKey;
    }

    public void setPrivateKey(String privateKey) {
        mPrivateKey = privateKey;
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
