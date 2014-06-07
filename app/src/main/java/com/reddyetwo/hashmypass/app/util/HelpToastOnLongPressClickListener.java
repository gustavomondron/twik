package com.reddyetwo.hashmypass.app.util;

import android.view.View;
import android.widget.Toast;

/**
 * {@link android.view.View.OnLongClickListener} implementation that shows a
 * toast with the contents of the {@code android:contentDescription} attribute,
 * if set. Otherwise does not consume the long press.
 */
public class HelpToastOnLongPressClickListener
        implements View.OnLongClickListener {

    @Override
    public boolean onLongClick(View v) {
        CharSequence text = v.getContentDescription();

        if (text != null && text.length() > 0) {
            Toast.makeText(v.getContext(), text, Toast.LENGTH_SHORT).show();
            return true;
        } else {
            return false;
        }
    }

}
