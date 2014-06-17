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

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.TextView;

import com.reddyetwo.hashmypass.app.hash.PasswordHasher;

public class MasterKeyWatcher implements TextWatcher {

    private TextView mDigestTextView;

    public MasterKeyWatcher(TextView digestTextView) {
        mDigestTextView = digestTextView;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count,
                                  int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before,
                              int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        if (s.toString().length() == 0) {
            mDigestTextView.setText("");
        } else {
            mDigestTextView
                    .setText(PasswordHasher.calculateKeyDigest(s.toString()));
        }
    }
}
