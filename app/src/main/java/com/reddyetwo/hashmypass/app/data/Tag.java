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

/**
 * POJO class for tags
 */
public class Tag implements Parcelable {

    public static final long NO_ID = -1;
    private long mId = NO_ID;
    public static final Parcelable.Creator<Tag> CREATOR = new Parcelable.Creator<Tag>() {
        public Tag createFromParcel(Parcel in) {
            return new Tag(in);
        }

        public Tag[] newArray(int size) {
            return new Tag[size];
        }
    };
    private final long mProfileId;
    private int mHashCounter = 0;
    private String mSite;
    private String mName;
    private int mPasswordLength;
    private PasswordType mPasswordType;

    /**
     * Constructor
     *
     * @param tag tag to copy
     */
    public Tag(Tag tag) {
        this(tag.getId(), tag.getProfileId(), tag.getHashCounter(), tag.getSite(), tag.getName(),
                tag.getPasswordLength(), tag.getPasswordType());
    }

    /**
     * Constructor
     *
     * @param id             tag ID
     * @param profileId      profile ID
     * @param hashCounter    number of times the tag has been used
     * @param site           site identifier
     * @param name           tag name
     * @param passwordLength password length
     * @param passwordType   password type
     */
    public Tag(long id, long profileId, int hashCounter, String site, String name,
               int passwordLength, PasswordType passwordType) {
        mId = id;
        mProfileId = profileId;
        mHashCounter = hashCounter;
        mSite = site;
        mName = name;
        mPasswordLength = passwordLength;
        mPasswordType = passwordType;
    }

    /**
     * Constructor
     *
     * @param in Parcelable object to get data from
     */
    private Tag(Parcel in) {
        mId = in.readLong();
        mProfileId = in.readLong();
        mHashCounter = in.readInt();
        mSite = in.readString();
        mName = in.readString();
        mPasswordLength = in.readInt();
        mPasswordType = PasswordType.values()[in.readInt()];
    }

    /**
     * Get the tag ID
     *
     * @return
     */
    public long getId() {
        return mId;
    }

    /**
     * Set the tag ID
     *
     * @param id
     */
    public void setId(long id) {
        mId = id;
    }

    /**
     * Get the profile ID
     *
     * @return
     */
    public long getProfileId() {
        return mProfileId;
    }

    /**
     * Get the number of times the tag has been used
     *
     * @return
     */
    public int getHashCounter() {
        return mHashCounter;
    }

    /**
     * Set the number of times the tag has been used
     *
     * @param hashCounter the number of times
     */
    public void setHashCounter(int hashCounter) {
        mHashCounter = hashCounter;
    }

    /**
     * Get the site associated with this tag
     *
     * @return
     */
    public String getSite() {
        return mSite;
    }

    /**
     * Set the site associated with this tag
     *
     * @param site the site identifier
     */
    public void setSite(String site) {
        mSite = site;
    }

    /**
     * Get the tag name
     *
     * @return
     */
    public String getName() {
        return mName;
    }

    /**
     * Set the tag name
     *
     * @param name
     */
    public void setName(String name) {
        mName = name;
    }

    /**
     * Get the password length
     *
     * @return
     */
    public int getPasswordLength() {
        return mPasswordLength;
    }

    /**
     * Set the password length
     *
     * @param passwordLength the password length
     */
    public void setPasswordLength(int passwordLength) {
        mPasswordLength = passwordLength;
    }

    /**
     * Get the password type
     *
     * @return
     */
    public PasswordType getPasswordType() {
        return mPasswordType;
    }

    /**
     * Set the password type
     *
     * @param passwordType
     */
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
}
