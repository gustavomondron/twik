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

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.reddyetwo.hashmypass.app.data.PasswordType;
import com.reddyetwo.hashmypass.app.hash.PasswordHasher;
import com.reddyetwo.hashmypass.app.util.Constants;

import java.util.Random;

public class TutorialIntroFragment extends Fragment {

    private ImageView mIcMasterKeyView;
    private TextView mWebsiteTextView;
    private TextView mWebsitePasswordView;

    private AnimatorSet mAnimatorSet;

    private Random mRandom;

    private final static String[] WEBSITES =
            {"amazon", "google", "ebay", "bing", "yahoo", "reddit", "paypal",
                    "spotify", "facebook", "twitter", "flickr", "steam",
                    "feedly", "foursquare", "apple", "xda-developers",
                    "bugzilla", "ssh", "wopr", "skynet"};
    private final static char[] MASTER_KEY = {'m', 'a', 's', 't', 'e', 'r'};

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView =
                (ViewGroup) inflater.inflate(R.layout.fragment_tutorial_intro, container, false);
        mIcMasterKeyView =
                (ImageView) rootView.findViewById(R.id.ic_master_key);
        mWebsiteTextView = (TextView) rootView.findViewById(R.id.website_text);
        mWebsitePasswordView =
                (TextView) rootView.findViewById(R.id.website_password);
        Typeface tf = Typeface.createFromAsset(getActivity().getAssets(),
                Constants.FONT_MONOSPACE);
        mWebsitePasswordView.setTypeface(tf);

        mRandom = new Random();

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        startAnimation();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopAnimation();
    }

    private void startAnimation() {
        // Load and set up individual animators
        AnimatorSet websiteAnimator = (AnimatorSet) AnimatorInflater
                .loadAnimator(getActivity(), R.animator.intro_website);
        websiteAnimator.setTarget(mWebsiteTextView);
        AnimatorSet masterKeyAnimator = (AnimatorSet) AnimatorInflater
                .loadAnimator(getActivity(), R.animator.intro_master_key);
        masterKeyAnimator.setTarget(mIcMasterKeyView);
        AnimatorSet passwordAnimator = (AnimatorSet) AnimatorInflater
                .loadAnimator(getActivity(), R.animator.intro_password);
        passwordAnimator.setTarget(mWebsitePasswordView);

        // Prepare set with all animators, set up repeating and random data
        mAnimatorSet = new AnimatorSet();
        mAnimatorSet.playTogether(websiteAnimator, masterKeyAnimator,
                passwordAnimator);
        mAnimatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                generateRandomData();
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (mAnimatorSet != null) {
                    mAnimatorSet.start();
                }
            }
        });

        mAnimatorSet.start();
    }

    private void stopAnimation() {
        mAnimatorSet.cancel();
        mAnimatorSet = null;
    }

    private void generateRandomData() {
        String website = WEBSITES[mRandom.nextInt(WEBSITES.length)];
        mWebsiteTextView.setText(website);
        String password = PasswordHasher
                .hashPassword(website, MASTER_KEY, "private", 8,
                        PasswordType.ALPHANUMERIC_AND_SPECIAL_CHARS);
        mWebsitePasswordView.setText(password);
    }

}
