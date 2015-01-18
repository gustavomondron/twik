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

import android.os.Parcel;
import android.os.Parcelable;

public class Tag implements Parcelable {

    public final static long NO_ID = -1;

    private long mId = NO_ID;
    private long mProfileId;
    private int mHashCounter = 0;
    private String mSite;
    private String mName;
    private int mPasswordLength;
    private PasswordType mPasswordType;

    public Tag(Tag tag) {
        this(tag.getId(), tag.getProfileId(), tag.getHashCounter(),
                tag.getSite(), tag.getName(), tag.getPasswordLength(),
                tag.getPasswordType());
    }

    public Tag(long id, long profileId, int hashCounter, String site,
               String name, int passwordLength, PasswordType passwordType) {
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(mId);
        dest.writeLong(mProfileId);
        dest.writeInt(mHashCounter);
        dest.writeString(mSite);
        dest.writeString(mName);
        dest.writeInt(mPasswordLength);
        dest.writeInt(mPasswordType.ordinal());
    }

    public static final Parcelable.Creator<Tag> CREATOR =
            new Parcelable.Creator<Tag>() {
                public Tag createFromParcel(Parcel in) {
                    return new Tag(in);
                }

                public Tag[] newArray(int size) {
                    return new Tag[size];
                }
            };

    private Tag(Parcel in) {
        mId = in.readLong();
        mProfileId = in.readLong();
        mHashCounter = in.readInt();
        mSite = in.readString();
        mName = in.readString();
        mPasswordLength = in.readInt();
        mPasswordType = PasswordType.values()[in.readInt()];
    }
}
