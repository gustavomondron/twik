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

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.reddyetwo.hashmypass.app.data.PasswordLength;
import com.reddyetwo.hashmypass.app.data.PasswordType;
import com.reddyetwo.hashmypass.app.data.Preferences;
import com.reddyetwo.hashmypass.app.data.Profile;
import com.reddyetwo.hashmypass.app.data.ProfileSettings;

public class TutorialActivity extends FragmentActivity {

    private Button mSkipButton;
    private Button mNextButton;
    private Button mStartButton;
    private MeasureViewPager mPager;
    private TutorialSetupFragment.PrivateKeyManager mPrivateKeyManager;
    private String mPrivateKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);
        setFinishOnTouchOutside(false);

        TutorialPagerAdapter pagerAdapter =
                new TutorialPagerAdapter(getSupportFragmentManager());

        mPager = (MeasureViewPager) findViewById(R.id.pager);
        mPager.setmOnMeasureListener(pagerAdapter);
        mPager.setOnPageChangeListener(pagerAdapter);
        mPager.setAdapter(pagerAdapter);

        mSkipButton = (Button) findViewById(R.id.skip_button);
        mNextButton = (Button) findViewById(R.id.next_button);
        mStartButton = (Button) findViewById(R.id.start_button);

        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPager.setCurrentItem(mPager.getCurrentItem() + 1);
            }
        });

        mSkipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Go to last page
                mPager.setCurrentItem(2);
            }
        });

        mStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Save profile and show main activity
                Profile profile = new Profile(-1,
                        getString(R.string.profile_default_name), mPrivateKey,
                        PasswordLength.DEFAULT,
                        PasswordType.ALPHANUMERIC_AND_SPECIAL_CHARS, 4);
                ProfileSettings.insertProfile(TutorialActivity.this, profile);
                HashMyPassApplication.setTutorialDismissed(false);
                finish();
            }
        });

        // Restore the current page
        SharedPreferences preferences =
                getSharedPreferences(Preferences.PREFS_NAME, MODE_PRIVATE);
        int position =
                preferences.getInt(Preferences.PREFS_KEY_TUTORIAL_PAGE, 0);
        mPager.setCurrentItem(position);

        mPrivateKeyManager = new TutorialSetupFragment.PrivateKeyManager() {
            @Override
            public void setPrivateKey(String privateKeyManager) {
                mPrivateKey = privateKeyManager;
                mStartButton.setEnabled(mPrivateKey.length() > 0);
            }
        };
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
        saveTutorialPage(mPager.getCurrentItem());
    }

    private class TutorialPagerAdapter extends FragmentStatePagerAdapter
            implements MeasureViewPager.OnMeasureListener,
            ViewPager.OnPageChangeListener {

        private double mIndicatorBaseWidth;
        private final View mProgressPrevView;
        private final View mProgressCurrentView;
        private final View mProgressNextView;

        public TutorialPagerAdapter(FragmentManager fm) {
            super(fm);
            mProgressPrevView = findViewById(R.id.pager_progress_prev);
            mProgressCurrentView = findViewById(R.id.pager_progress_current);
            mProgressNextView = findViewById(R.id.pager_progress_next);
        }

        private void updateIndicators(int position) {
            ViewGroup.LayoutParams currentViewParams =
                    mProgressCurrentView.getLayoutParams();
            currentViewParams.width = (int) Math.ceil(mIndicatorBaseWidth);
            mProgressCurrentView.setLayoutParams(currentViewParams);

            ViewGroup.LayoutParams prevViewParams =
                    mProgressPrevView.getLayoutParams();
            prevViewParams.width =
                    (int) Math.ceil(mIndicatorBaseWidth * position);
            mProgressPrevView.setLayoutParams(prevViewParams);

            ViewGroup.LayoutParams nextViewParams =
                    mProgressNextView.getLayoutParams();
            nextViewParams.width = (int) Math
                    .ceil(mIndicatorBaseWidth * (getCount() - 1 - position));
            mProgressNextView.setLayoutParams(nextViewParams);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                return new TutorialSplashFragment();
            } else if (position == 1) {
                return new TutorialIntroFragment();
            } else if (position == 2) {
                TutorialSetupFragment fragment = new TutorialSetupFragment();
                fragment.setPrivateKeyManager(mPrivateKeyManager);
                return fragment;
            } else {
                return null;
            }
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public void onMeasure(int width, int height) {
            mIndicatorBaseWidth = (double) width / (getCount());
            updateIndicators(mPager.getCurrentItem());
        }

        @Override
        public void onPageScrolled(int position, float positionOffset,
                                   int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            updateIndicators(position);
            if (position == getCount() - 1) {
                mSkipButton.setVisibility(View.GONE);
                mNextButton.setVisibility(View.GONE);
                mStartButton.setVisibility(View.VISIBLE);
            } else {
                mSkipButton.setVisibility(View.VISIBLE);
                mNextButton.setVisibility(View.VISIBLE);
                mStartButton.setVisibility(View.GONE);
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    }

    private void saveTutorialPage(int page) {
        SharedPreferences preferences =
                getSharedPreferences(Preferences.PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(Preferences.PREFS_KEY_TUTORIAL_PAGE, page);
        editor.apply();
    }
}
