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

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.reddyetwo.hashmypass.app.data.PasswordLength;
import com.reddyetwo.hashmypass.app.data.PasswordType;
import com.reddyetwo.hashmypass.app.data.Profile;
import com.reddyetwo.hashmypass.app.data.ProfileSettings;
import com.reddyetwo.hashmypass.app.util.RandomPrivateKeyGenerator;

public class AddDefaultProfileDialog extends DialogFragment {

    private EditText mPrivateKeyText;
    private OnProfileAddedListener mOnProfileAddedListener;

    public interface OnProfileAddedListener {

        public void onProfileAdded();
        public void onCanceled();
    }

    public void setOnProfileAddedListener(OnProfileAddedListener listener) {
        mOnProfileAddedListener = listener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (mOnProfileAddedListener == null) {
            dismiss();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.add_first_profile);
        builder.setCancelable(false);

        // Inflate the layout
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view =
                inflater.inflate(R.layout.dialog_add_default_profile, null);
        builder.setView(view);


        builder.setPositiveButton(R.string.ready,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Profile profile = new Profile(-1,
                                getString(R.string.profile_default_name),
                                mPrivateKeyText.getText().toString(),
                                PasswordLength.DEFAULT,
                                PasswordType.ALPHANUMERIC_AND_SPECIAL_CHARS);
                        ProfileSettings.insertProfile(getActivity(), profile);
                        mOnProfileAddedListener.onProfileAdded();
                        dismiss();
                    }
                }
        );

        final AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);

        mPrivateKeyText = (EditText) view.findViewById(R.id.private_key_text);
        mPrivateKeyText.setText(RandomPrivateKeyGenerator.generate());
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

                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(
                        mPrivateKeyText.getText().toString().length() > 0);
            }
        });

        return dialog;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        mOnProfileAddedListener.onCanceled();
    }
}
