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
