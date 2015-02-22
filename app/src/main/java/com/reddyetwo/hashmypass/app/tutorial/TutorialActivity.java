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

package com.reddyetwo.hashmypass.app.tutorial;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Layout;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.reddyetwo.hashmypass.app.HashMyPassApplication;
import com.reddyetwo.hashmypass.app.R;
import com.reddyetwo.hashmypass.app.data.PasswordLength;
import com.reddyetwo.hashmypass.app.data.PasswordType;
import com.reddyetwo.hashmypass.app.data.Preferences;
import com.reddyetwo.hashmypass.app.data.Profile;
import com.reddyetwo.hashmypass.app.data.ProfileSettings;
import com.reddyetwo.hashmypass.app.util.ApiUtils;
import com.reddyetwo.hashmypass.app.view.ViewPagerIndicator;

public class TutorialActivity extends FragmentActivity
        implements TutorialSetupFragment.PrivateKeyChangedListener {

    /**
     * Color for created profile
     */
    private static final int DEFAULT_PROFILE_COLOR = 0;

    /**
     * Button for skipping page
     */
    private Button mSkipButton;

    /**
     * Button for going to the next page
     */
    private Button mNextButton;

    /**
     * Pager
     */
    private ViewPager mPager;

    /**
     * Private key selected by the user
     */
    private String mPrivateKey;

    /**
     * Background color for each page
     */
    @ColorRes
    private int[] mBackgroundColors;

    /**
     * Panel containing a tutorial page
     */
    private LinearLayout mTutorialPanel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);

        initializeView();
        initializeSettings();
    }

    @Override
    public void onBackPressed() {
        if (mPager.getCurrentItem() == 0) {
            HashMyPassApplication.setTutorialDismissed(true);
            super.onBackPressed();
        } else {
            mPager.setCurrentItem(mPager.getCurrentItem() - 1);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Preferences.setTutorialPage(this, mPager.getCurrentItem());
    }

    private void initializeView() {
        ApiUtils.drawBehindStatusBar(getWindow());

        mTutorialPanel = (LinearLayout) findViewById(R.id.tutorial_panel);

        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setAdapter(new TutorialPagerAdapter(getSupportFragmentManager()));

        ViewPagerIndicator viewPagerIndicator =
                (ViewPagerIndicator) findViewById(R.id.pager_indicator);
        viewPagerIndicator.setViewPager(mPager);
        viewPagerIndicator.setOnPageChangeListener(new TutorialOnPageChangeListener());

        mSkipButton = (Button) findViewById(R.id.skip_button);
        mNextButton = (Button) findViewById(R.id.next_button);

        // Add listener
        addNextButtonClickedListener();
        addSkipButtonClickedListener();
    }

    private void initializeSettings() {
        mBackgroundColors = getResources().getIntArray(R.array.color_tutorial);

        // Restore the current page
        int initialPosition = Preferences.getTutorialPage(this);
        mPager.setCurrentItem(initialPosition);
        mTutorialPanel.setBackgroundColor(mBackgroundColors[initialPosition]);
    }

    private void addNextButtonClickedListener() {
        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPager.getCurrentItem() == TutorialPagerAdapter.NUMBER_OF_PAGES - 1) {
                    // Finish tutorial and start Twik
                    Profile profile =
                            new Profile(Profile.NO_ID, getString(R.string.profile_default_name),
                                    mPrivateKey, PasswordLength.DEFAULT,
                                    PasswordType.ALPHANUMERIC_AND_SPECIAL_CHARS,
                                    DEFAULT_PROFILE_COLOR);
                    ProfileSettings.insertProfile(TutorialActivity.this, profile);
                    HashMyPassApplication.setTutorialDismissed(false);
                    finish();
                } else {
                    // Go to next page
                    mPager.setCurrentItem(mPager.getCurrentItem() + 1);
                }
            }
        });
    }

    private void addSkipButtonClickedListener() {
        mSkipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Go to last page
                mPager.setCurrentItem(TutorialPagerAdapter.NUMBER_OF_PAGES - 1);
            }
        });
    }

    @Override
    public void onPrivateKeyChanged(String privateKey) {
        mPrivateKey = privateKey;
    }

    private class TutorialPagerAdapter extends FragmentStatePagerAdapter {

        public static final int NUMBER_OF_PAGES = 3;
        private static final int PAGE_SPLASH = 0;
        private static final int PAGE_INTRO = 1;
        private static final int PAGE_SETUP = 2;

        public TutorialPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == PAGE_SPLASH) {
                return new TutorialSplashFragment();
            } else if (position == PAGE_INTRO) {
                return new TutorialIntroFragment();
            } else if (position == PAGE_SETUP) {
                TutorialSetupFragment fragment = new TutorialSetupFragment();
                fragment.setPrivateKeyChangedListener(TutorialActivity.this);
                return fragment;
            } else {
                return null;
            }
        }

        @Override
        public int getCount() {
            return NUMBER_OF_PAGES;
        }
    }

    private class TutorialOnPageChangeListener implements ViewPager.OnPageChangeListener {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            if ((position + 1) < TutorialPagerAdapter.NUMBER_OF_PAGES) {
                mTutorialPanel.setBackgroundColor((int) new ArgbEvaluator()
                        .evaluate(positionOffset, mBackgroundColors[position],
                                mBackgroundColors[position + 1]));
            }
        }

        @Override
        public void onPageSelected(int position) {
            if (position == mPager.getAdapter().getCount() - 1) {
                mSkipButton.setVisibility(View.INVISIBLE);
                mNextButton.setText(R.string.start);
            } else {
                mSkipButton.setVisibility(View.VISIBLE);
                mNextButton.setText(R.string.next);
            }

            mTutorialPanel.setBackgroundColor(mBackgroundColors[position]);
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            // Do nothing
        }
    }
}
