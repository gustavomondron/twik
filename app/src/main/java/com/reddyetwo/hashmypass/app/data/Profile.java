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
