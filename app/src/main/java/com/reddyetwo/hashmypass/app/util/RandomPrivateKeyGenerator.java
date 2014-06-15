package com.reddyetwo.hashmypass.app.util;

import java.security.SecureRandom;

public class RandomPrivateKeyGenerator {

    private static final int[] SUBGROUPS_LENGTH = {8, 4, 4, 4, 12};
    private static final char SUBGROUP_SEPARATOR = '-';
    private static final String ALLOWED_CHARS =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    public static String generate() {
        SecureRandom sr = new SecureRandom();
        String key = "";
        int allowedCharsLength = ALLOWED_CHARS.length();

        for (int i = 0; i < SUBGROUPS_LENGTH.length; i++) {
            for (int j = 0; j < SUBGROUPS_LENGTH[i]; j++) {
                key = key +
                        ALLOWED_CHARS.charAt(sr.nextInt(allowedCharsLength));
            }
            if (i < SUBGROUPS_LENGTH.length - 1) {
                key += SUBGROUP_SEPARATOR;
            }
        }

        return key;
    }
}
