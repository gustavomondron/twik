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

package com.reddyetwo.hashmypass.app.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.NumberPicker;

import com.reddyetwo.hashmypass.app.R;
import com.reddyetwo.hashmypass.app.util.Constants;

/**
 * Dialog fragment with a number picker for selecting password length.
 */
public class PasswordLengthDialogFragment extends DialogFragment {

    private int mPasswordLength;
    private OnSelectedListener mListener;

    /**
     * Set the password length
     *
     * @param passwordLength the password length
     */
    public void setPasswordLength(int passwordLength) {
        this.mPasswordLength = passwordLength;
    }

    /**
     * Set the listener for password length selected events
     *
     * @param listener the {@link com.reddyetwo.hashmypass.app.dialog.PasswordLengthDialogFragment.OnSelectedListener} instance
     */
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
        final NumberPicker picker = (NumberPicker) view.findViewById(R.id.numberPicker);
        picker.setMinValue(Constants.MIN_PASSWORD_LENGTH);
        picker.setMaxValue(Constants.MAX_PASSWORD_LENGTH);
        picker.setValue(mPasswordLength);

        // Set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(view)
                // Add action buttons
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onPasswordLengthSelected(picker.getValue());
                        PasswordLengthDialogFragment.this.getDialog().cancel();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        PasswordLengthDialogFragment.this.getDialog().cancel();
                    }
                });

        return builder.create();
    }

    /**
     * Interface that can be implemented to listen to password length selected events.
     */
    public interface OnSelectedListener {

        /**
         * Method called when a password length has been selected
         *
         * @param length the password length
         */
        void onPasswordLengthSelected(int length);

    }
}
