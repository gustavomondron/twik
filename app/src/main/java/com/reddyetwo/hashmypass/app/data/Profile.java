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

public class Profile {

    public static final long NO_ID = -1;

    private long mId = NO_ID;
    private String mName;
    private String mPrivateKey;
    private int mPasswordLength;
    private int mColorIndex;
    private PasswordType mPasswordType;

    public Profile() {
    }

    @Override
    public boolean equals(Object o) {
        boolean equals = false;
        if (o instanceof Profile) {
            equals = ((Profile) o).getId() == mId;
        }
        return equals;
    }

    public Profile(long id, String name, String privateKey, int passwordLength,
                   PasswordType passwordType, int colorIndex) {
        mId = id;
        mName = name;
        mPrivateKey = privateKey;
        mPasswordLength = passwordLength;
        mPasswordType = passwordType;
        mColorIndex = colorIndex;
    }

    public long getId() {
        return mId;
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

    public int getPasswordLength() {
        return mPasswordLength;
    }

    public PasswordType getPasswordType() {
        return mPasswordType;
    }

    public int getColorIndex() {
        return mColorIndex;
    }
}
