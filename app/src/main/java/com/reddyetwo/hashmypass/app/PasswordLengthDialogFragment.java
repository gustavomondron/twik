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

package com.reddyetwo.hashmypass.app;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.NumberPicker;

import com.reddyetwo.hashmypass.app.data.PasswordLength;

/**
 * Dialog fragment with a number picker for selecting password length.
 */
public class PasswordLengthDialogFragment extends DialogFragment {

    public interface OnSelectedListener {

        public void onPasswordLengthSelected(int length);

    }

    private int mPasswordLength;
    private OnSelectedListener mListener;

    public void setPasswordLength(int passwordLength) {
        this.mPasswordLength = passwordLength;
    }

    public void setOnSelectedListener(OnSelectedListener listener) {
        mListener = listener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (mListener == null) {
            dismiss();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.password_length));

        // Inflate the layout
        View view = View.inflate(getActivity(), R.layout.dialog_number_picker, null);

        // Set up number picker
        final NumberPicker picker =
                (NumberPicker) view.findViewById(R.id.numberPicker);
        picker.setMinValue(PasswordLength.MIN_LENGTH);
        picker.setMaxValue(PasswordLength.MAX_LENGTH);
        picker.setValue(mPasswordLength);

        // Set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(view)
                // Add action buttons
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                mListener.onPasswordLengthSelected(
                                        picker.getValue());
                                PasswordLengthDialogFragment.this.getDialog()
                                        .cancel();
                            }
                        }
                ).setNegativeButton(android.R.string.cancel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        PasswordLengthDialogFragment.this.getDialog().cancel();
                    }
                }
        );

        return builder.create();
    }
}
