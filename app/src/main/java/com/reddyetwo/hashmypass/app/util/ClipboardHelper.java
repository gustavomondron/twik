package com.reddyetwo.hashmypass.app.util;


import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.widget.Toast;

import com.reddyetwo.hashmypass.app.R;

public class ClipboardHelper {

    public static final String CLIPBOARD_LABEL_PASSWORD = "password";

    public static void copyToClipboard(Context context, String label,
                                       String string, int toastMessageId) {
        ClipboardManager clipboard = (ClipboardManager) context
                .getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(label, string);
        clipboard.setPrimaryClip(clip);

        if (toastMessageId > 0) {
            Toast.makeText(context, toastMessageId, Toast.LENGTH_LONG).show();
        }
    }
}
