/*
 * Copyright 2014 Red Dye No. 2
 * Copyright (C) 2011-2013 TG Byte Software GmbH
 * Copyright (C) 2009-2011 Thilo-Alexander Ginkel.
 * Copyright (C) 2010-2014 Eric Woodruff
 * Copyright (C) 2006-2010 Steve Cooper
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

package com.reddyetwo.hashmypass.app.hash;

import android.util.Base64;
import android.util.Log;

import com.reddyetwo.hashmypass.app.HashMyPassApplication;
import com.reddyetwo.hashmypass.app.data.PasswordType;
import com.reddyetwo.hashmypass.app.util.SecurePassword;

import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class PasswordHasher {

    /**
     * Keyed-hash message authentication code (HMAC) used
     */
    private static final String HMAC_SHA1 = "HmacSHA1";

    /**
     * Message digest used to calculate key digest
     */
    private static final String DIGEST_MD5 = "MD5";

    private PasswordHasher() {
        
    }

    private static String _hashPassword(String tag, char[] key, int length, PasswordType type) {

        Mac hmac;
        try {
            hmac = Mac.getInstance(HMAC_SHA1);
        } catch (NoSuchAlgorithmException e) {
            return null;
        }

        /* First, we have to calculate the hashing key as a result of hashing
         the tag and the private key */
        SecretKeySpec keySpec = new SecretKeySpec(SecurePassword.toBytes(key), HMAC_SHA1);
        try {
            hmac.init(keySpec);
        } catch (InvalidKeyException e) {
            Log.e(HashMyPassApplication.LOG_TAG, "Invalid secret key");
            return null;
        }

        String hash = Base64.encodeToString(hmac.doFinal(tag.getBytes()),
                Base64.NO_PADDING | Base64.NO_WRAP);

        int sum = 0;
        for (int i = 0; i < hash.length(); i++) {
            sum += hash.charAt(i);
        }

        /* Parse password to match the request type */
        if (type == PasswordType.NUMERIC) {
            hash = convertToDigits(hash, sum, length);
        } else {
            /* We force digits, punctuation characters and mixed case in
            order to provide compatibility with the Chrome extension */
            // Force digits
            hash = injectCharacter(hash, 0, 4, sum, length, '0', 10);
            if (type == PasswordType.ALPHANUMERIC_AND_SPECIAL_CHARS) {
                // Force special chars
                hash = injectCharacter(hash, 1, 4, sum, length, '!', 15);
            }
            // Force mixed case
            hash = injectCharacter(hash, 2, 4, sum, length, 'A', 26);
            hash = injectCharacter(hash, 3, 4, sum, length, 'a', 26);

            // Remove special chars if needed
            if (type == PasswordType.ALPHANUMERIC) {
                hash = removeSpecialCharacters(hash, sum, length);
            }
        }

        /* Trim the password to match the requested length */
        return hash.substring(0, length);
    }

    public static String hashPassword(String tag, char[] masterKey, String privateKey, int length,
                                      PasswordType passwordType) {
        /* First, hash the tag with the private key (in the case that it is
        used) */
        if (privateKey != null) {
            tag = _hashPassword(privateKey, tag.toCharArray(), 24,
                    PasswordType.ALPHANUMERIC_AND_SPECIAL_CHARS);
        }

        // Then, hash the result with the master key
        return _hashPassword(tag, masterKey, length, passwordType);
    }

    /**
     * Calculates the digest of an input string
     *
     * @param input the input string
     * @return the digest of the input string
     */
    public static byte[] calculateDigest(char[] input) {
        MessageDigest messageDigest;
        byte[] result;

        try {
            messageDigest = MessageDigest.getInstance(DIGEST_MD5);
            result = messageDigest.digest(SecurePassword.toBytes(input));
        } catch (NoSuchAlgorithmException e) {
            result = null;
        }

        return result;
    }

    /**
     * Convert input string to digits-only
     *
     * @param input  input string
     * @param seed   seed for pseudo-randomizing the position and injected character
     * @param length length of head of string that will eventually survive truncation
     * @return the digits-only string
     */
    private static String convertToDigits(String input, int seed, int length) {
        char[] inputChars = input.toCharArray();
        int pivot = 0; // Pivot for next char-to-digit conversion
        for (int i = 0; i < length; i++) {
            if (!Character.isDigit(inputChars[i])) {
                inputChars[i] = (char) ((seed + inputChars[pivot]) % 10 + '0');
                pivot = i + 1;
            }
        }

        return String.valueOf(inputChars);
    }

    /**
     * Replace special chars with alphanumeric chars
     *
     * @param input  input string
     * @param seed   seed for pseudo-randomizing the position and injected
     *               character
     * @param length length of head of string that will eventually survive
     *               truncation
     * @return the string with special chars replaced
     */
    private static String removeSpecialCharacters(String input, int seed, int length) {
        char[] inputChars = input.toCharArray();
        int pivot = 0;
        for (int i = 0; i < length; i++) {
            if (!Character.isLetterOrDigit(inputChars[i])) {
                inputChars[i] = (char) ((seed + pivot) % 26 + 'A');
                pivot = i + 1;
            }
        }

        return String.valueOf(inputChars);
    }

    /**
     * Inject a character chosen from a range of character codes into a block
     * at the front of a string if one of those characters is not already
     * present
     *
     * @param input    input string
     * @param offset   offset for position of injected character
     * @param reserved number of offsets reserved for special chars
     * @param seed     seed for pseudo-randomizing the position and
     *                 injected character
     * @param length   length of head of string that will eventually
     *                 survive truncation
     * @param cStart   character code for first valid injected character
     * @param cNum     number of valid character codes starting from cStart
     * @return the string with the injected chars
     */
    @SuppressWarnings("SameParameterValue")
    private static String injectCharacter(String input, int offset, int reserved, int seed,
                                          int length, char cStart, int cNum) {
        int pos0 = seed % length;
        int pos = (pos0 + offset) % length;
        // Check if a qualified character is already present.
        // Write the loop so that the reserved block is ignored.
        for (int i = 0; i < length - reserved; i++) {
            int i2 = (pos0 + reserved + i) % length;
            char c = input.charAt(i2);
            if (c >= cStart && c < cStart + cNum) {
                return input; // Already present - nothing to do
            }
        }

        String head = pos > 0 ? input.substring(0, pos) : "";
        char inject = (char) (((seed + input.charAt(pos)) % cNum) + cStart);
        String tail = (pos + 1 < input.length()) ? input.substring(pos + 1) : "";

        return head + inject + tail;
    }
}
