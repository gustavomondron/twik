package com.reddyetwo.hashmypass.app;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;

import com.reddyetwo.hashmypass.app.data.Tag;
import com.reddyetwo.hashmypass.app.util.Constants;
import com.reddyetwo.hashmypass.app.util.FaviconLoader;

public class GeneratePasswordDialogFragment extends DialogFragment {

    private long mProfileId;
    private Tag mTag;

    private TextView mFaviconTextView;
    private AutoCompleteTextView mTagEditAutoCompleteTextView;
    private EditText mMasterKeyEditText;
    private TextView mPasswordTextView;

    public void setProfileId(long profileId) {
        mProfileId = profileId;
    }

    public void setTag(Tag tag) {
        mTag = tag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Inflate the layout
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_generate_password, null);
        builder.setView(view);
        builder.setNeutralButton(android.R.string.ok,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }
        );

        mFaviconTextView = (TextView) view.findViewById(R.id.tag_favicon);
        FaviconLoader.setAsBackground(getActivity(), mFaviconTextView, mTag);

        mTagEditAutoCompleteTextView =
                (AutoCompleteTextView) view.findViewById(R.id.tag_text);
        mTagEditAutoCompleteTextView.setText(mTag.getName());

        mMasterKeyEditText = (EditText) view.findViewById(R.id.master_key_text);
        mMasterKeyEditText.requestFocus();

        mPasswordTextView = (TextView) view.findViewById(R.id.website_password);
        Typeface tf = Typeface.createFromAsset(getActivity().getAssets(),
                Constants.FONT_MONOSPACE);
        mPasswordTextView.setTypeface(tf);


        return builder.create();
    }


}
