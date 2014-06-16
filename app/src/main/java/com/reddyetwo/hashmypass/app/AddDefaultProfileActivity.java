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

package com.reddyetwo.hashmypass.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.reddyetwo.hashmypass.app.data.PasswordLength;
import com.reddyetwo.hashmypass.app.data.PasswordType;
import com.reddyetwo.hashmypass.app.data.Profile;
import com.reddyetwo.hashmypass.app.data.ProfileSettings;
import com.reddyetwo.hashmypass.app.util.RandomPrivateKeyGenerator;

public class AddDefaultProfileActivity extends Activity {

    private EditText mPrivateKeyText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_default_profile);

        final Button readyButton = (Button) findViewById(R.id.ready_button);
        readyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Profile profile = new Profile(-1,
                        getString(R.string.profile_default_name),
                        mPrivateKeyText.getText().toString(),
                        PasswordLength.DEFAULT,
                        PasswordType.ALPHANUMERIC_AND_SPECIAL_CHARS);
                ProfileSettings
                        .insertProfile(AddDefaultProfileActivity.this, profile);
                Intent intent = new Intent(AddDefaultProfileActivity.this,
                        MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        mPrivateKeyText = (EditText) findViewById(R.id.private_key_text);
        mPrivateKeyText.addTextChangedListener(new TextWatcher() {
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
                readyButton.setEnabled(
                        mPrivateKeyText.getText().toString().length() > 0);
            }
        });

        mPrivateKeyText.setText(RandomPrivateKeyGenerator.generate());
    }
}
