package com.reddyetwo.hashmypass.app.cards;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import com.reddyetwo.hashmypass.app.R;
import com.reddyetwo.hashmypass.app.data.Profile;
import com.reddyetwo.hashmypass.app.data.ProfileSettings;
import com.reddyetwo.hashmypass.app.data.Tag;
import com.reddyetwo.hashmypass.app.data.TagSettings;

import it.gmariotti.cardslib.library.internal.CardHeader;

public class TagCardHeader extends CardHeader {

    //private ImageView mFavIconImageView;
    private TextView mNameTextView;
    private AutoCompleteTextView mNameAutoComplete;
    private TagNameAutoCompleteTextWatcher mNameAutocompleteTextWatcher;
    private OnTagChangedListener mTagChangedListener;

    private long mProfileId;
    Tag mTag;

    public TagCardHeader(Context context, int innerLayout, long profileId,
                         OnTagChangedListener tagChangedListener) {
        super(context, innerLayout);
        mProfileId = profileId;
        mTagChangedListener = tagChangedListener;
    }

    public void setTag(Tag tag) {
        mTag = tag;
        mNameTextView.setText(tag.getName());
    }

    public void setProfileId(long profileId) {
        mProfileId = profileId;
        Profile profile = ProfileSettings.getProfile(getContext(), profileId);
        mTag = new Tag(Tag.NO_ID, profileId, null, null,
                profile.getPasswordLength(), profile.getPasswordType());
        mNameAutocompleteTextWatcher.updateProfileTags();
    }

    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {
        //mFavIconImageView = (ImageView) parent.findViewById(R.id.tag_favicon);
        mNameTextView = (TextView) parent.findViewById(R.id.tag_name);
        mNameAutoComplete = (AutoCompleteTextView) parent
                .findViewById(R.id.tag_name_autocomplete);
        mNameAutocompleteTextWatcher =
                new TagNameAutoCompleteTextWatcher(getContext(), mProfileId,
                        mNameMatchListener);
        mNameAutoComplete.addTextChangedListener(mNameAutocompleteTextWatcher);
    }

    public void toggleOverflow() {
        getCustomOverflowAnimation().doAnimation(getParentCard(),
                mInnerView.findViewById(R.id.card_header_button_overflow));
    }

    public void toggleAutocomplete() {
        if (mNameAutoComplete.getVisibility() == View.GONE) {
            // Setup autocomplete
            mNameAutoComplete.setText(mNameTextView.getText().toString());
            mNameTextView.setVisibility(View.GONE);
            mNameAutoComplete.setVisibility(View.VISIBLE);

            // Request focus and show soft keyboard
            mNameAutoComplete.requestFocus();
            InputMethodManager imm = (InputMethodManager) getContext()
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(mNameAutoComplete,
                    InputMethodManager.SHOW_IMPLICIT);

            // Select all the tag

        } else {
            mNameTextView.setVisibility(View.VISIBLE);
            mNameAutoComplete.setVisibility(View.GONE);
        }
    }

    public Tag getTag() {
        return mTag;
    }

    public void tagStored() {
        mNameAutocompleteTextWatcher.addTag(mTag.getName());
    }

    public boolean autoCompleteIsShown() {
        return mNameAutoComplete.getVisibility() == View.VISIBLE;
    }

    private TagNameAutoCompleteTextWatcher.OnTagNameChangedListener
            mNameMatchListener =
            new TagNameAutoCompleteTextWatcher.OnTagNameChangedListener() {

                @Override
                public void onNameChanged(String name, boolean match) {
                    // Keep the name text view in sync
                    mNameTextView.setText(name);

                    if (match) {
                        mTag = TagSettings
                                .getTag(getContext(), mProfileId, name);
                    } else if (mTag.getId() != Tag.NO_ID) {
                        // Replace the current (existing and saved) tag for a
                        // new one
                        Profile profile = ProfileSettings
                                .getProfile(getContext(), mProfileId);
                        mTag = new Tag(Tag.NO_ID, mProfileId, null, name,
                                profile.getPasswordLength(),
                                profile.getPasswordType());
                    } else {
                        mTag.setName(name);
                    }

                    // Notify the listener
                    mTagChangedListener.onTagChanged(mTag);
                }
            };

    public interface OnTagChangedListener {
        public void onTagChanged(Tag tag);
    }
}
