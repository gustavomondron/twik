package com.reddyetwo.hashmypass.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

/**
 * About Dialog
 */
public class AboutDialog extends DialogFragment {

    private static final String VERSION_UNAVAILABLE = "N/A";

    public static void showAbout(Activity activity) {

        FragmentManager fm = activity.getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        Fragment prev = fm.findFragmentByTag("dialog_about");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        new AboutDialog().show(ft,"dialog_about");
    }

    public AboutDialog() {
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // Build the about body view and append the link to see OSS licenses
        //SpannableStringBuilder aboutBody = new SpannableStringBuilder();
        //aboutBody.append(Html.fromHtml(getString(R.string.about_body, versionName)));

        LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        View rootView = layoutInflater.inflate(R.layout.dialog_about, null);
        TextView nameView = (TextView) rootView.findViewById(R.id.app_name);
        nameView.setText(Html.fromHtml(getString(R.string.app_name)));

        TextView aboutBodyView = (TextView) rootView.findViewById(R.id.about_body);
        aboutBodyView.setText(Html.fromHtml(getString(R.string.about_body)));
        aboutBodyView.setMovementMethod(new LinkMovementMethod());

        return new AlertDialog.Builder(getActivity())
                //.setTitle(R.string.title_about)
                .setView(rootView)
                .setPositiveButton(R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dialog.dismiss();
                            }
                        }
                )
                .create();
    }
}