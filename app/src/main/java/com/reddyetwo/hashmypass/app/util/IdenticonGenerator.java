/*
 * Copyright 2014 Red Dye No. 2
 * Copyright 2014 David Hamp-Gonsalves
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

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.TypedValue;

import com.reddyetwo.hashmypass.app.hash.PasswordHasher;

public class IdenticonGenerator {
    private static final int IDENTICON_HEIGHT = 5;
    private static final int IDENTICON_WIDTH = 5;

    private IdenticonGenerator() {

    }

    public static Bitmap generate(Context context, char[] input) {

        byte[] hash = PasswordHasher.calculateDigest(input);

        Bitmap identicon = Bitmap.createBitmap(IDENTICON_WIDTH, IDENTICON_HEIGHT, Config.ARGB_8888);

        // get byte values as unsigned ints
        int r = hash[0] & 255;
        int g = hash[1] & 255;
        int b = hash[2] & 255;

        int background = Color.parseColor("#00f0f0f0");
        int foreground = Color.argb(255, r, g, b);

        for (int x = 0; x < IDENTICON_WIDTH; x++) {

            //make identicon horizontally symmetrical
            int i = x < 3 ? x : 4 - x;
            int pixelColor;
            for (int y = 0; y < IDENTICON_HEIGHT; y++) {

                if ((hash[i] >> y & 1) == 1) {
                    pixelColor = foreground;
                } else {
                    pixelColor = background;
                }

                identicon.setPixel(x, y, pixelColor);
            }
        }

        // scale image by 2 to add border
        Resources res = context.getResources();
        int size = (int) TypedValue
                .applyDimension(TypedValue.COMPLEX_UNIT_DIP, 32, res.getDisplayMetrics());
        Bitmap bmpWithBorder = Bitmap.createBitmap(size, size, identicon.getConfig());
        Canvas canvas = new Canvas(bmpWithBorder);
        canvas.drawColor(background);
        identicon = Bitmap.createScaledBitmap(identicon, size - 2, size - 2, false);
        canvas.drawBitmap(identicon, 1, 1, null);

        return bmpWithBorder;
    }
}