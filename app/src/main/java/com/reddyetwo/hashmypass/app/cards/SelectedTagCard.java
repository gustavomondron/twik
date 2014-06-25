package com.reddyetwo.hashmypass.app.cards;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;

import com.reddyetwo.hashmypass.app.PasswordLengthDialogFragment;
import com.reddyetwo.hashmypass.app.R;
import com.reddyetwo.hashmypass.app.data.PasswordType;
import com.reddyetwo.hashmypass.app.data.Tag;
import com.reddyetwo.hashmypass.app.data.TagSettings;
import com.reddyetwo.hashmypass.app.util.ClipboardHelper;
import com.reddyetwo.hashmypass.app.util.Constants;
import com.reddyetwo.hashmypass.app.util.ProfileFormInflater;

import it.gmariotti.cardslib.library.internal.Card;

public class SelectedTagCard extends Card {

    private long mProfileId;
    private TextView mHashActionTextView;
    private TextView mHashedPasswordTextView;
    private Spinner mPasswordLengthSpinner;
    private OnTagSelectedListener mSelectedTagListener;
    private SelectedTagAnimation mCardAnimation;
    private OnSelectedTagCardContentClickListener mContentClickListener;
    private boolean mHashActionEnabled = true;

    private TagCardHeader mHeader;

    public SelectedTagCard(Context context,
                           OnTagSelectedListener selectedTagListener,
                           long profileId) {
        super(context, R.layout.card_tag_inner_main);
        mSelectedTagListener = selectedTagListener;
        mProfileId = profileId;
        init();
    }

    /**
     * Set a new tag
     *
     * @param tag the tag
     */
    public void setTag(Tag tag) {
        mHeader.setTag(tag);
    }

    /**
     * Mark the current tag as selected (no autocomplete, no settings)
     */
    public void selectTag() {
        showHashedPassword();

        // Hide tag settings
        if (tagSettingsAreShown()) {
            mHeader.toggleOverflow();
        }

        // Hide autocomplete
        if (mHeader.autoCompleteIsShown()) {
            mHeader.toggleAutocomplete();
        }
    }

    /**
     * Set the profile ID
     *
     * @param profileId the profile identifier
     */
    public void setProfileId(long profileId) {
        mProfileId = profileId;
        mHeader.setProfileId(profileId);

        // Hide tag settings
        if (tagSettingsAreShown()) {
            mHeader.toggleOverflow();
        }
    }

    /**
     * Set the hashed password
     *
     * @param hashedPassword the hashed password
     */
    public void setHashedPassword(String hashedPassword) {
        mHashedPasswordTextView.setText(hashedPassword);
        if (hashedPassword.length() == 0) {
            mHeader.hideDivider();
        } else {
            mHeader.showDivider();
        }
    }

    /**
     * De-select the card. Autocomplete is shown and hash action.
     */
    public void clear() {
        if (!mHeader.autoCompleteIsShown()) {
            mHeader.toggleAutocomplete();
        }
        showHashAction();
        mHeader.showDivider();
    }

    /**
     * Hide hash action and show hashed password
     */
    public void showHashedPassword() {
        mHashActionTextView.setVisibility(View.GONE);
        mHashedPasswordTextView.setVisibility(View.VISIBLE);
        if (mHashedPasswordTextView.getText().length() == 0) {
            mHeader.hideDivider();
        }
    }

    /**
     * Show hash action and hide hashed password
     */
    public void showHashAction() {
        mHashedPasswordTextView.setVisibility(View.GONE);
        mHashActionTextView.setVisibility(View.VISIBLE);
        mHeader.showDivider();
    }

    public boolean hashedPasswordIsShown() {
        return mHashedPasswordTextView.getVisibility() == View.VISIBLE;
    }

    private void enableHashAction() {
        addPartialOnClickListener(Card.CLICK_LISTENER_CONTENT_VIEW,
                mContentClickListener);
        mHashActionTextView.setTextColor(
                getContext().getResources().getColor(R.color.card_action));
        mHashActionEnabled = true;
        ((NewCardView)getCardView()).updateListeners();
    }

    private void disableHashAction() {
        removePartialOnClickListener(Card.CLICK_LISTENER_CONTENT_VIEW);
        mHashActionTextView.setTextColor(getContext().getResources()
                .getColor(R.color.button_text_disabled));
        mHashActionEnabled = false;
        ((NewCardView)getCardView()).updateListeners();
    }

    private void init() {

        //Add header
        mHeader =
                new TagCardHeader(getContext(), R.layout.card_tag_inner_header,
                        mProfileId, new TagCardHeader.OnTagChangedListener() {
                    @Override
                    public void onTagChanged(Tag tag) {
                        if (tag.getName().length() == 0) {
                            disableHashAction();
                        } else if (!mHashActionEnabled) {
                            enableHashAction();
                        }
                    }
                }
                );
        mCardAnimation = new SelectedTagAnimation(getContext(), this);
        mHeader.setCustomOverflowAnimation(mCardAnimation);
        addCardHeader(mHeader);

        // Set content clickListener
        mContentClickListener = new OnSelectedTagCardContentClickListener();
        addPartialOnClickListener(Card.CLICK_LISTENER_CONTENT_VIEW,
                mContentClickListener);

        // Set header clickListener
        addPartialOnClickListener(Card.CLICK_LISTENER_HEADER_VIEW,
                new OnSelectedTagCardHeaderClickListener());
    }

    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {
        mHashActionTextView = (TextView) view.findViewById(R.id.hash);
        mHashedPasswordTextView =
                (TextView) view.findViewById(R.id.hashed_password);
        Typeface tf = Typeface.createFromAsset(getContext().getAssets(),
                Constants.FONT_MONOSPACE);
        mHashedPasswordTextView.setTypeface(tf);
    }

    /* Shows a number picker dialog for choosing the password length */
    private void showPasswordLengthDialog() {
        PasswordLengthDialogFragment dialogFragment =
                new PasswordLengthDialogFragment();
        dialogFragment.setPasswordLength(Integer.parseInt(
                (String) mPasswordLengthSpinner.getSelectedItem()));
        dialogFragment.setOnSelectedListener(
                new PasswordLengthDialogFragment.OnSelectedListener() {
                    @Override
                    public void onPasswordLengthSelected(int length) {
                        ProfileFormInflater
                                .populatePasswordLengthSpinner(getContext(),
                                        mPasswordLengthSpinner, length);
                        mHeader.getTag().setPasswordLength(length);
                        // If it exists, save the changes in storage
                        if (mHeader.getTag().getId() != Tag.NO_ID) {
                            saveTag();
                        }
                    }
                }
        );

        try {
            Activity activity = (Activity) getContext();
            dialogFragment
                    .show(activity.getFragmentManager(), "passwordLength");
        } catch (ClassCastException e) {
            // TODO Fatal error
        }
    }

    /**
     * Save the current tag in the storage
     */
    private void saveTag() {
        Tag tag = mHeader.getTag();

        if (tag.getId() == Tag.NO_ID) {
            TagSettings.insertTag(getContext(), tag);
        } else {
            TagSettings.updateTag(getContext(), tag);
        }
    }

    public boolean tagSettingsAreShown() {
        return mCardAnimation.tagSettingsAreShown();
    }

    private class OnSelectedTagCardContentClickListener
            implements OnCardClickListener {
        @Override
        public void onClick(Card card, View view) {
            Tag tag = mHeader.getTag();
            if (mHashActionTextView.getVisibility() == View.VISIBLE) {
                // Hash action
                if (mHeader.autoCompleteIsShown()) {
                    // Hide the autocomplete and hide tags settings
                    selectTag();
                }

                mHeader.tagStored();
                showHashedPassword();
            } else {
                // Hashed password is shown. Copy password to clipboard.
                if (mHashedPasswordTextView.getText().length() > 0) {
                    ClipboardHelper.copyToClipboard(getContext(),
                            ClipboardHelper.CLIPBOARD_LABEL_PASSWORD,
                            mHashedPasswordTextView.getText().toString(),
                            R.string.copied_to_clipboard);
                }
            }

            // Notify: new tag hashed
            mSelectedTagListener.onTagSelected(tag, false);
        }
    }

    private class OnSelectedTagCardHeaderClickListener
            implements OnCardClickListener {

        @Override
        public void onClick(Card card, View view) {
            if (!mHeader.autoCompleteIsShown()) {
                // Show AutoCompleteTextView
                mHeader.toggleAutocomplete();

                // Hide tag settings
                if (tagSettingsAreShown()) {
                    mHeader.toggleOverflow();
                }
            }

            showHashAction();
        }
    }

    private class SelectedTagAnimation extends NewTwoCardOverlayAnimation {

        private boolean mTagSettingsAreShown = false;

        public SelectedTagAnimation(Context context, Card card) {
            super(context, card);
        }

        @Override
        protected CardInfoToAnimate setCardToAnimate(Card card) {
            return new TagCardAnimate();
        }

        private class TagCardAnimate extends TwoCardToAnimate {

            public TagCardAnimate() {
                mLayoutsIdToRemove = new int[]{R.id.card_main_content_layout,
                        R.id.card_header_divider};
            }

            @Override
            public int getLayoutIdToAdd() {
                return R.layout.card_tag_settings;
            }
        }

        @Override
        void onOverflowShown(View v) {
            mTagSettingsAreShown = true;
            final Tag tag = mHeader.getTag();

            mPasswordLengthSpinner =
                    (Spinner) v.findViewById(R.id.password_length);
            Spinner passwordTypeSpinner =
                    (Spinner) v.findViewById(R.id.password_type);

            ProfileFormInflater.populatePasswordLengthSpinner(getContext(),
                    mPasswordLengthSpinner, tag.getPasswordLength());
            mPasswordLengthSpinner
                    .setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                                showPasswordLengthDialog();
                                return true;
                            }

                            return false;
                        }
                    });

            ProfileFormInflater.populatePasswordTypeSpinner(getContext(),
                    passwordTypeSpinner, tag.getPasswordType());
            passwordTypeSpinner.setOnItemSelectedListener(
                    new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent,
                                                   View view, int position,
                                                   long id) {
                            tag.setPasswordType(
                                    PasswordType.values()[position]);
                            // If it exists, save the changes in storage
                            if (mHeader.getTag().getId() != Tag.NO_ID) {
                                saveTag();
                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    }
            );
        }

        @Override
        void onOverflowHidden() {
            mTagSettingsAreShown = false;
            // Update hashed password using the new settings
            mSelectedTagListener.onTagSettingsChanged(mHeader.getTag());
        }

        public boolean tagSettingsAreShown() {
            return mTagSettingsAreShown;
        }
    }

}
