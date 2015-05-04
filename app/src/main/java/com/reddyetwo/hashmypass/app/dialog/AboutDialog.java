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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;

import com.reddyetwo.hashmypass.app.R;
import com.reddyetwo.hashmypass.app.util.PackageUtils;

/**
 * About Dialog
 */
public class AboutDialog extends DialogFragment {

    /**
     * Tag to identify the {@link android.app.DialogFragment}
     */
    private static final String FRAGMENT_DIALOG_TAG = "dialog_about";

    /**
     * Constructor
     */
    public AboutDialog() {
    }

    /**
     * Show the dialog
     *
     * @param activity the {@link android.app.Activity} instance
     */
    public static void showAbout(Activity activity) {
        // Ensure that the dialog is not shown several times simultaneously
        FragmentManager fm = activity.getFragmentManager();
        if (fm.findFragmentByTag(FRAGMENT_DIALOG_TAG) == null) {
            new AboutDialog().show(fm, FRAGMENT_DIALOG_TAG);
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        View rootView = View.inflate(getActivity(), R.layout.dialog_about, null);
        TextView nameView = (TextView) rootView.findViewById(R.id.app_name);
        nameView.setText(getString(R.string.app_name));

        String bodyText = String.format(getResources().getString(R.string.about_body),
                PackageUtils.getVersionName(getActivity()));
        TextView aboutBodyView = (TextView) rootView.findViewById(R.id.about_body);
        aboutBodyView.setText(Html.fromHtml(bodyText));
        aboutBodyView.setMovementMethod(new LinkMovementMethod());

        return new AlertDialog.Builder(getActivity()).setView(rootView)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                    }
                }).create();
    }
}