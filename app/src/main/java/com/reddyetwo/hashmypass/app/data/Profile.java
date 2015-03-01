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

/**
 * POJO class for profiles
 */
public class Profile {

    public static final long NO_ID = -1;

    private long mId = NO_ID;
    private String mName;
    private String mPrivateKey;
    private int mPasswordLength;
    private int mColorIndex;
    private PasswordType mPasswordType;

    /**
     * Constructor
     */
    public Profile() {
    }

    /**
     * Constructor
     *
     * @param id the ID
     */
    public Profile(long id) {
        mId = id;
    }

    /**
     * Constructor
     *
     * @param id             the ID
     * @param name           the name
     * @param privateKey     the private key
     * @param passwordLength the password length
     * @param passwordType   the password type
     * @param colorIndex     the color index
     */
    public Profile(long id, String name, String privateKey, int passwordLength,
                   PasswordType passwordType, int colorIndex) {
        mId = id;
        mName = name;
        mPrivateKey = privateKey;
        mPasswordLength = passwordLength;
        mPasswordType = passwordType;
        mColorIndex = colorIndex;
    }

    @Override
    public boolean equals(Object o) {
        boolean equals = false;
        if (o instanceof Profile) {
            equals = ((Profile) o).getId() == mId;
        }
        return equals;
    }

    @Override
    public int hashCode() {
        return (int) mId;
    }

    /**
     * Get the profile ID
     *
     * @return the profile ID
     */
    public long getId() {
        return mId;
    }

    /**
     * Get the profile name
     *
     * @return the profile name
     */
    public String getName() {
        return mName;
    }

    /**
     * Set the profile name
     *
     * @param name the name
     */
    public void setName(String name) {
        mName = name;
    }

    /**
     * Get the private key
     *
     * @return the private key
     */
    public String getPrivateKey() {
        return mPrivateKey;
    }

    /**
     * Get the password length
     *
     * @return the password length
     */
    public int getPasswordLength() {
        return mPasswordLength;
    }

    /**
     * Get the password type
     *
     * @return the password type
     */
    public PasswordType getPasswordType() {
        return mPasswordType;
    }

    /**
     * Get the color index
     *
     * @return the color index
     */
    public int getColorIndex() {
        return mColorIndex;
    }
}
