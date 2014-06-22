package com.reddyetwo.hashmypass.app.cards;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.reddyetwo.hashmypass.app.R;

import java.util.ArrayList;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.overflowanimation.TwoCardOverlayAnimation;

/**
 * Extends TwoCardOverlayAnimation class to allow specifying a listener to
 * know when the new view has been added.
 */
public abstract class NewTwoCardOverlayAnimation
        extends TwoCardOverlayAnimation {

    public NewTwoCardOverlayAnimation(Context context, Card card) {
        super(context, card);
    }

    @Override
    public void doAnimation(Card card, View imageOverflow) {
        super.doAnimation(card, imageOverflow);
    }

    @Override
    protected void doOverFirstAnimation(final Card card,
                                        CardInfoToAnimate infoAnimation,
                                        View imageOverflow) {

        if (infoAnimation == null) return;

        final ViewGroup mInternalLayoutOverlay =
                (ViewGroup) card.getCardView().findViewById(R.id.card_overlap);

        //Checks
        if (mInternalLayoutOverlay == null) {
            Log.e(TAG, "Overlap layout not found!");
            return;
        }
        if (infoAnimation.getLayoutsIdToAdd() == null) {
            Log.e(TAG, "You have to specify layouts to add!");
            return;
        }

        //Views to remove
        View[] viewsOut = getOutViews(card, infoAnimation);

        //Get the layout to add
        final int layoutIdIn = infoAnimation.getLayoutsIdToAdd()[0];

        AnimatorSet animAlpha = new AnimatorSet();
        if (viewsOut != null && layoutIdIn > 0) {

            ArrayList<Animator> animators = new ArrayList<Animator>();

            for (final View viewOut : viewsOut) {
                if (viewOut != null) {
                    ObjectAnimator anim =
                            ObjectAnimator.ofFloat(viewOut, "alpha", 1f, 0f);
                    anim.setDuration(getAnimationDuration());
                    anim.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            viewOut.setVisibility(View.GONE);
                        }
                    });
                    animators.add(anim);
                }
            }
            animAlpha.playTogether(animators);
        }


        animAlpha.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);

                LayoutInflater inflater = (LayoutInflater) mContext
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View viewIn =
                        inflater.inflate(layoutIdIn, mInternalLayoutOverlay,
                                false);
                if (viewIn != null) {

                    if (card.getCardView() != null &&
                            card.getCardView().getInternalMainCardLayout() !=
                                    null &&
                            card.getCardView().getInternalHeaderLayout() !=
                                    null &&
                            card.getCardView().getInternalHeaderLayout()
                                    .getFrameButton() != null) {
                        int h1 = card.getCardView().getInternalMainCardLayout()
                                .getMeasuredHeight();
                        int h2 = card.getCardView().getInternalHeaderLayout()
                                .getFrameButton().getMeasuredHeight();
                        viewIn.setMinimumHeight(h1 - h2);
                    }
                    mInternalLayoutOverlay.addView(viewIn);

                    viewIn.setAlpha(0);
                    viewIn.setVisibility(View.VISIBLE);

                    viewIn.animate().alpha(1f)
                            .setDuration(getAnimationDuration())
                            .setListener(null);
                    onOverflowShown(viewIn);
                }
            }
        });

        animAlpha.start();
    }

    @Override
    protected void doOverOtherAnimation(final Card card,
                                        CardInfoToAnimate infoAnimation,
                                        View imageOverflow) {

        //Checks
        if (infoAnimation == null) return;

        final ViewGroup mInternalLayoutOverlay =
                (ViewGroup) card.getCardView().findViewById(R.id.card_overlap);
        if (mInternalLayoutOverlay == null) {
            Log.e(TAG, "Overlap layout not found!");
            return;
        }
        if (infoAnimation.getLayoutsIdToAdd() == null) {
            Log.e(TAG, "You have to specify layouts to add!");
            return;
        }

        final View[] viewsLastOut = getOutViews(card, infoAnimation);
        final View viewLastIn = mInternalLayoutOverlay.getChildAt(0);

        if (viewLastIn != null) {
            viewLastIn.animate().alpha(0f).setDuration(getAnimationDuration())
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            viewLastIn.setVisibility(View.GONE);

                            for (final View viewOut : viewsLastOut) {
                                if (viewOut != null) {
                                    viewOut.setVisibility(View.VISIBLE);
                                }
                            }

                            mInternalLayoutOverlay.removeView(viewLastIn);

                            for (final View viewOut : viewsLastOut) {
                                if (viewOut != null) {
                                    viewOut.animate().alpha(1f).setDuration(
                                            getAnimationDuration());
                                }
                            }

                            onOverflowHidden();
                        }
                    });
        }
    }

    abstract void onOverflowShown(View v);

    abstract void onOverflowHidden();
}
