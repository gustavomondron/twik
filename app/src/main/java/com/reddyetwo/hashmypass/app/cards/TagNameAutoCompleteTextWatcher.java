package com.reddyetwo.hashmypass.app.cards;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;

import com.reddyetwo.hashmypass.app.data.Tag;
import com.reddyetwo.hashmypass.app.data.TagSettings;

import java.util.ArrayList;
import java.util.List;

public class TagNameAutoCompleteTextWatcher implements TextWatcher {

    public interface OnTagNameChangedListener {

        public void onNameChanged(String name, boolean match);
    }

    private Context mContext;
    private OnTagNameChangedListener mNameMatchListener;
    private long mProfileId;
    private List<String> mTags;

    public TagNameAutoCompleteTextWatcher(Context context, long profileId,
                                          OnTagNameChangedListener nameMatchListener) {
        super();
        mContext = context;
        mProfileId = profileId;
        mNameMatchListener = nameMatchListener;
        updateProfileTags();
    }

    public void updateProfileTags() {
        mTags = new ArrayList<String>();
        List<Tag> tags = TagSettings.getProfileTags(mContext, mProfileId);
        for (Tag tag : tags) {
            mTags.add(tag.getName());
        }
    }

    public void addTag(String tagName) {
        mTags.add(tagName);
    }

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
        String name = s.toString();
        mNameMatchListener.onNameChanged(name, mTags.contains(name));
    }
}
