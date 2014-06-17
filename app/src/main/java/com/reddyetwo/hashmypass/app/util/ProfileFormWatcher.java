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
import android.widget.Button;
import android.widget.EditText;

public class ProfileFormWatcher implements TextWatcher {

    private EditText mNameEditText;
    private EditText mPrivateKeyEditText;
    private Button mSaveButton;

    public ProfileFormWatcher(EditText nameEditText,
                              EditText privateKeyEditText, Button saveButton) {
        mNameEditText = nameEditText;
        mPrivateKeyEditText = privateKeyEditText;
        mSaveButton = saveButton;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count,
                                  int after) {
        // Do nothing
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before,
                              int count) {
        // Do nothing
    }

    @Override
    public void afterTextChanged(Editable s) {
        updateSaveButtonEnabled();
    }

    public void updateSaveButtonEnabled() {
        boolean nameSet =
                mNameEditText.getText().toString().trim().length() > 0;
        boolean privateKeySet =
                mPrivateKeyEditText.getText().toString().length() > 0;
        mSaveButton.setEnabled(nameSet && privateKeySet);
    }


}

