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
