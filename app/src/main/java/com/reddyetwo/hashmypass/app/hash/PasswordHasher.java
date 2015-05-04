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

import com.reddyetwo.hashmypass.app.TwikApplication;
import com.reddyetwo.hashmypass.app.data.PasswordType;
import com.reddyetwo.hashmypass.app.util.SecurePassword;

import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Generator of hashed passwords
 */
public class PasswordHasher {

    /**
     * Keyed-hash message authentication code (HMAC) used
     */
    private static final String HMAC_SHA1 = "HmacSHA1";

    /**
     * Message digest used to calculate key digest
     */
    private static final String DIGEST_MD5 = "MD5";

    private static final int INJECT_RESERVED = 4;
    private static final int INJECT_NUMBER_OF_NUMERIC_CHARS = 10;
    private static final int INJECT_NUMBER_OF_SPECIAL_CHARS = 15;
    private static final int INJECT_NUMBER_OF_ALPHA_CHARS = 26;
    private static final int INJECT_OFFSET_NUMERIC = 0;
    private static final int INJECT_OFFSET_SPECIAL = 1;
    private static final int INJECT_OFFSET_ALPHA_UPPERCASE = 2;
    private static final int INJECT_OFFSET_ALPHA_LOWERCASE = 3;
    private static final int INTERMEDIATE_HASH_SIZE = 24;

    private PasswordHasher() {

    }

    private static String hashKey(String tag, char[] key, int length, PasswordType type) {

        Mac hmac;
        try {
            hmac = Mac.getInstance(HMAC_SHA1);
        } catch (NoSuchAlgorithmException e) {
            Log.e(TwikApplication.LOG_TAG, "HMAC error: " + e);
            return null;
        }

        /* First, we have to calculate the hashing key as a result of hashing
         the tag and the private key */
        SecretKeySpec keySpec = new SecretKeySpec(SecurePassword.toBytes(key), HMAC_SHA1);
        try {
            hmac.init(keySpec);
        } catch (InvalidKeyException e) {
            Log.e(TwikApplication.LOG_TAG, "Invalid secret key: " + e);
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
            hash = injectCharacter(hash, INJECT_OFFSET_NUMERIC, INJECT_RESERVED, sum, length, '0',
                    INJECT_NUMBER_OF_NUMERIC_CHARS);
            if (type == PasswordType.ALPHANUMERIC_AND_SPECIAL_CHARS) {
                // Force special chars
                hash = injectCharacter(hash, INJECT_OFFSET_SPECIAL, INJECT_RESERVED, sum, length,
                        '!', INJECT_NUMBER_OF_SPECIAL_CHARS);
            }
            // Force mixed case
            hash = injectCharacter(hash, INJECT_OFFSET_ALPHA_UPPERCASE, INJECT_RESERVED, sum,
                    length, 'A', INJECT_NUMBER_OF_ALPHA_CHARS);
            hash = injectCharacter(hash, INJECT_OFFSET_ALPHA_LOWERCASE, INJECT_RESERVED, sum,
                    length, 'a', INJECT_NUMBER_OF_ALPHA_CHARS);

            // Remove special chars if needed
            if (type == PasswordType.ALPHANUMERIC) {
                hash = removeSpecialCharacters(hash, sum, length);
            }
        }

        /* Trim the password to match the requested length */
        return hash.substring(0, length);
    }

    /**
     * Generate a hashed password using a tag, a master key and a private key
     *
     * @param tag          the tag name
     * @param masterKey    the master key
     * @param privateKey   the private key
     * @param length       the password length
     * @param passwordType the password type
     * @return the hashed password
     */
    public static String hashTagWithKeys(String tag, char[] masterKey, String privateKey,
                                         int length, PasswordType passwordType) {
        // First, hash the tag with the private key (in the case that it is used)
        String tagToHash = privateKey == null ? tag :
                hashKey(privateKey, tag.toCharArray(), INTERMEDIATE_HASH_SIZE,
                        PasswordType.ALPHANUMERIC_AND_SPECIAL_CHARS);

        // Then, hash the result with the master key
        return hashKey(tagToHash, masterKey, length, passwordType);
    }

    /**
     * Calculate the digest of an input string
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
            Log.e(TwikApplication.LOG_TAG, "Could not find DIGEST MD5 algorithm: " + e);
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
        // Pivot for next char-to-digit conversion
        int pivot = 0;
        for (int i = 0; i < length; i++) {
            if (!Character.isDigit(inputChars[i])) {
                inputChars[i] =
                        (char) ((seed + inputChars[pivot]) % INJECT_NUMBER_OF_NUMERIC_CHARS + '0');
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
                inputChars[i] = (char) ((seed + pivot) % INJECT_NUMBER_OF_ALPHA_CHARS + 'A');
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
                // Already present - nothing to do
                return input;
            }
        }

        String head = pos > 0 ? input.substring(0, pos) : "";
        char inject = (char) (((seed + input.charAt(pos)) % cNum) + cStart);
        String tail = (pos + 1 < input.length()) ? input.substring(pos + 1) : "";

        return head + inject + tail;
    }
}
