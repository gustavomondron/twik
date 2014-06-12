package com.reddyetwo.hashmypass.app.util;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.reddyetwo.hashmypass.app.R;
import com.reddyetwo.hashmypass.app.data.PasswordType;

public class ProfileFormInflater {

    public static void populatePasswordLengthSpinner(Context context,
                                                Spinner spinner,
                                               int passwordLength) {
        ArrayAdapter<String> passwordLengthAdapter =
                new ArrayAdapter<String>(context,
                        android.R.layout.simple_spinner_item,
                        new String[]{String.valueOf(passwordLength)});
        passwordLengthAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(passwordLengthAdapter);
    }

    public static void populatePasswordTypeSpinner(Context context,
                                                Spinner spinner,
                                             PasswordType passwordType) {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter
                .createFromResource(context, R.array.password_types_array,
                        android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(passwordType.ordinal());
    }
}
