package com.reddyetwo.hashmypass.app.cards;

import android.content.Context;
import android.util.AttributeSet;

import it.gmariotti.cardslib.library.view.CardView;

/**
 * New implementation of CardView which allows adding and removing click
 * listeners dynamically. In the original implementation,
 * listeners can only be added or removed before CardView#setCard method is
 * executed.
 */
public class NewCardView extends CardView {

    public NewCardView(Context context) {
        super(context);
    }

    public NewCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NewCardView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void updateListeners() {
        setupListeners();
    }
}
