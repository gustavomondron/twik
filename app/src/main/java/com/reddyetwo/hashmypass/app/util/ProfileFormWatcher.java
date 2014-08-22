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

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;

import com.reddyetwo.hashmypass.app.R;
import com.reddyetwo.hashmypass.app.data.Profile;
import com.reddyetwo.hashmypass.app.data.ProfileSettings;

public class ProfileFormWatcher implements TextWatcher {

    private Context mContext;
    private long mProfileId;
    private EditText mNameEditText;
    private EditText mPrivateKeyEditText;
    private Button mSaveButton;

    public ProfileFormWatcher(Context context, long profileId,
                              EditText nameEditText,
                              EditText privateKeyEditText, Button saveButton) {
        mContext = context;
        mProfileId = profileId;
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

        if (!nameSet || !privateKeySet) {
            mSaveButton.setEnabled(false);
        } else if (nameSet) {
            long storedProfileId = ProfileSettings
                    .getProfileId(mContext, mNameEditText.getText().toString());
            boolean repeated = storedProfileId != Profile.NO_ID &&
                    storedProfileId != mProfileId;
            mSaveButton.setEnabled(!repeated);
            if (repeated) {
                mNameEditText.setError(
                        mContext.getString(R.string.error_profile_exists));
            } else {
                mNameEditText.setError(null);
            }
        }
    }


}

