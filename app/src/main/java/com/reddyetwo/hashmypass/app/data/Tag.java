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

package com.reddyetwo.hashmypass.app.data;

public class Tag {

    public final static long NO_ID = -1;

    private long mId = NO_ID;
    private long mProfileId;
    private int mHashCounter = 0;
    private String mSite;
    private String mName;
    private int mPasswordLength;
    private PasswordType mPasswordType;

    public Tag() {
    }

    public Tag(long id, long profileId, int hashCounter, String site,
               String name,
               int passwordLength, PasswordType passwordType) {
        mId = id;
        mProfileId = profileId;
        mHashCounter = hashCounter;
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

    public int getHashCounter() {
        return mHashCounter;
    }

    public void setHashCounter(int hashCounter) {
        mHashCounter = hashCounter;
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
