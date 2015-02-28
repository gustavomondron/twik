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

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.support.annotation.StringRes;
import android.widget.Toast;

/**
 * Helper class to copy strings to the clipboard
 */
@SuppressWarnings("SameParameterValue")
public class ClipboardHelper {

    public static final String CLIPBOARD_LABEL_PASSWORD = "password";

    private ClipboardHelper() {

    }

    /**
     * Copy a string to the clipboard
     *
     * @param context        the {@link android.content.Context} instance
     * @param label          the label
     * @param string         the {@link java.lang.String} to copy
     * @param toastMessageId the {@link android.support.annotation.StringRes} of the message shown in the toast confirmation, or 0 if toast should not be shown
     */
    public static void copyToClipboard(Context context, String label, String string,
                                       @StringRes int toastMessageId) {
        ClipboardManager clipboard =
                (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(label, string);
        clipboard.setPrimaryClip(clip);

        if (toastMessageId > 0) {
            Toast.makeText(context, toastMessageId, Toast.LENGTH_LONG).show();
        }
    }
}
