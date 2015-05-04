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

package com.reddyetwo.hashmypass.app.util;

import android.text.Editable;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * A class which can be used to manage and store passwords in a secure way, preventing the usage of immutable objectsA
 */
public class SecurePassword {

    private SecurePassword() {

    }

    /**
     * Get the password stored in an {@link android.text.Editable} object
     *
     * @param s the {@link android.text.Editable} instance
     * @return the password
     */
    public static char[] getPassword(Editable s) {
        int length = s.length();
        char[] password = new char[length];
        for (int i = 0; i < length; i++) {
            password[i] = s.charAt(i);
        }
        return password;
    }

    /**
     * Convert a password stored as a char array to a byte array
     *
     * @param chars the password as a char array
     * @return the password as a byte array
     */
    public static byte[] toBytes(char[] chars) {
        CharBuffer charBuffer = CharBuffer.wrap(chars);
        ByteBuffer byteBuffer = Charset.forName("UTF-8").encode(charBuffer);
        byte[] bytes =
                Arrays.copyOfRange(byteBuffer.array(), byteBuffer.position(), byteBuffer.limit());

        // Clear sensitive data
        Arrays.fill(charBuffer.array(), '\u0000');
        Arrays.fill(byteBuffer.array(), (byte) 0);
        return bytes;
    }
}
