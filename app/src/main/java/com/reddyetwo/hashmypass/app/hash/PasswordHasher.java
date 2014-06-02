package com.reddyetwo.hashmypass.app.hash;

import android.util.Base64;

import com.reddyetwo.hashmypass.app.data.PasswordType;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class PasswordHasher {

    /**
     * Keyed-hash message authentication code (HMAC) used
     */
    private static final String HMAC_SHA1 = "HmacSHA1";

    /**
     * A pattern that matches numerical digits
     */
    private static final Pattern NUM_PATTERN = Pattern.compile("[^0-9]");

    /**
     * Matches all special characters
     */
    private static final Pattern SPECIAL_PATTERN =
            Pattern.compile("[^a-z0-9]", Pattern.CASE_INSENSITIVE);

    private static String _hashPassword(String tag, String key, int length,
                                        PasswordType type) {

        Mac hmac = null;
        try {
            hmac = Mac.getInstance(HMAC_SHA1);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }

        /* First, we have to calculate the hashing key as a result of hashing
         the tag and the private key */
        SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(), HMAC_SHA1);
        try {
            hmac.init(keySpec);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
            return null;
        }

        String hash = Base64.encodeToString(hmac.doFinal(tag.getBytes()),
                Base64.NO_PADDING | Base64.NO_WRAP);

        int sum = 0;
        for (int i = 0; i < hash.length(); i++)
            sum += hash.charAt(i);

        /* Parse password to match the request type */
        if (type == PasswordType.NUMERIC) {
            hash = convertToDigits(hash, sum, length);
        } else {
           /* We force digits, punctuation characters and mixed case in order to
        provide compatibility with the Chrome extension */
            hash = injectCharacter(hash, 0, 4, sum, length, '0', 10);
            if (type == PasswordType.ALPHANUMERIC_AND_SPECIAL_CHARS) {
                hash = injectCharacter(hash, 1, 4, sum, length, '!', 15);
            }
            hash = injectCharacter(hash, 2, 4, sum, length, 'A', 26);
            hash = injectCharacter(hash, 3, 4, sum, length, 'a', 26);

            /* Remove special chars if needed */
            if (type == PasswordType.ALPHANUMERIC) {
                hash = removeSpecialCharacters(hash, sum, length);
            }
        }

        /* Trim the password to match the requested length */
        return hash.substring(0, length);
    }

    public static String hashPassword(String tag, String masterKey,
                                      String privateKey, int length,
                                      PasswordType passwordType) {
        /* First, hash the tag with the private key (in the case that it is
        used) */
        if (privateKey != null) {
            tag = _hashPassword(privateKey, tag, 24,
                    PasswordType.ALPHANUMERIC_AND_SPECIAL_CHARS);
        }

        return _hashPassword(tag, masterKey, length, passwordType);
    }

    /**
     * Converts the input string to a digits-only representation.
     */
    private static String convertToDigits(String sInput, int seed, int lenOut) {
        StringBuilder s = new StringBuilder(lenOut);
        int i = 0;
        while (i < lenOut) {
            Matcher m = NUM_PATTERN.matcher(sInput.substring(i));
            if (!m.find()) break;
            int matchPos = m.start();
            if (matchPos > 0) {
                s.append(sInput.substring(i, i + matchPos));
            }
            s.append((char) ((seed + sInput.charAt(i)) % 10 + 48));
            i += (matchPos + 1);
        }
        if (i < sInput.length()) {
            s.append(sInput.substring(i));
        }
        return s.toString();
    }

    /**
     * Replace special characters by digits and numbers.
     */
    private static String removeSpecialCharacters(String sInput, int seed,
                                                  int lenOut) {
        StringBuilder s = new StringBuilder(lenOut);
        int i = 0;
        while (i < lenOut) {
            Matcher m = SPECIAL_PATTERN.matcher(sInput.substring(i));
            if (!m.find()) break;
            int matchPos = m.start();
            if (matchPos > 0) s.append(sInput.substring(i, i + matchPos));
            s.append((char) ((seed + i) % 26 + 65));
            i += (matchPos + 1);
        }
        if (i < sInput.length()) s.append(sInput.substring(i));
        return s.toString();
    }

    /**
     * Inject a character chosen from a range of character codes into a block at the front of a
     * string if one of those characters is not already present.
     */
    private static String injectCharacter(String sInput, int offset,
                                          int reserved, int seed, int lenOut,
                                          char cStart, int cNum) {
        int pos0 = seed % lenOut;
        int pos = (pos0 + offset) % lenOut;
        // Check if a qualified character is already present
        // Write the loop so that the reserved block is ignored.
        for (int i = 0; i < lenOut - reserved; i++) {
            int i2 = (pos0 + reserved + i) % lenOut;
            char c = sInput.charAt(i2);
            if (c >= cStart && c < cStart + cNum)
                return sInput; // Already present - nothing to do
        }

        StringBuilder result = new StringBuilder();
        if (pos > 0) {
            result.append(sInput.substring(0, pos));
        }
        result.append((char) (((seed + sInput.charAt(pos)) % cNum) + cStart));
        if (pos + 1 < sInput.length()) {
            result.append(sInput.substring(pos + 1, sInput.length()));
        }
        return result.toString();
    }
}
