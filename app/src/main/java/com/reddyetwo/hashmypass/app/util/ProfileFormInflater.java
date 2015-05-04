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
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.reddyetwo.hashmypass.app.R;
import com.reddyetwo.hashmypass.app.data.PasswordType;

/**
 * Inflater for 'Add profile' or 'Edit profile' forms spinners
 */
public class ProfileFormInflater {

    private ProfileFormInflater() {

    }

    /**
     * Populate a password length spinner
     *
     * @param context        the {@link android.content.Context} instance
     * @param spinner        the {@link android.widget.Spinner} instance
     * @param passwordLength the password length
     */
    public static void populatePasswordLengthSpinner(Context context, Spinner spinner,
                                                     int passwordLength) {
        ArrayAdapter<String> passwordLengthAdapter =
                new ArrayAdapter<>(context, android.R.layout.simple_spinner_item,
                        new String[]{String.valueOf(passwordLength)});
        passwordLengthAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(passwordLengthAdapter);
    }

    /**
     * Populate a password type spinner
     *
     * @param context      the {@link android.content.Context} instance
     * @param spinner      the {@link android.widget.Spinner} instance
     * @param passwordType the password type
     */
    public static void populatePasswordTypeSpinner(Context context, Spinner spinner,
                                                   PasswordType passwordType) {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter
                .createFromResource(context, R.array.password_types_array,
                        android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(passwordType.ordinal());
    }
}
