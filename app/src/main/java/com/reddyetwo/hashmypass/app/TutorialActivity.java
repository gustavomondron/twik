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

import com.reddyetwo.hashmypass.app.data.Preferences;

public class TutorialActivity extends FragmentActivity {

    private Button mSkipButton;
    private Button mNextButton;
    private Button mStartButton;
    private MeasureViewPager mPager;

    // bottom center center bottom bottom
    private static final Integer[] imageAlignment =
            new Integer[]{TutorialContentFragment.ALIGN_BOTTOM,
                    TutorialContentFragment.ALIGN_CENTER,
                    TutorialContentFragment.ALIGN_CENTER,
                    TutorialContentFragment.ALIGN_BOTTOM,
                    TutorialContentFragment.ALIGN_BOTTOM,
                    TutorialContentFragment.ALIGN_BOTTOM};

    private static final Integer[] imageResIds =
            new Integer[]{R.drawable.tutorial_main_activity, R.drawable.shield,
                    R.drawable.lock, R.drawable.tutorial_profiles,
                    R.drawable.tutorial_tag_settings,
                    R.drawable.tutorial_browser_dialog};

    private static final Integer[] headerResIds =
            new Integer[]{R.string.tutorial_one_password_header,
                    R.string.tutorial_not_a_password_store_header,
                    R.string.tutorial_private_key_header,
                    R.string.tutorial_profiles_header,
                    R.string.tutorial_tag_settings_header,
                    R.string.tutorial_browser_dialog_header};

    private static final Integer[] contentResIds =
            new Integer[]{R.string.tutorial_one_password_content,
                    R.string.tutorial_not_a_password_store_content,
                    R.string.tutorial_private_key_content,
                    R.string.tutorial_profiles_content,
                    R.string.tutorial_tag_settings_content,
                    R.string.tutorial_browser_dialog_content};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);

        TutorialPagerAdapter pagerAdapter =
                new TutorialPagerAdapter(getSupportFragmentManager(),
                        imageAlignment, imageResIds, headerResIds,
                        contentResIds);

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

        TutorialFinishedListener tutorialFinishedListener =
                new TutorialFinishedListener();
        mSkipButton.setOnClickListener(tutorialFinishedListener);
        mStartButton.setOnClickListener(tutorialFinishedListener);
    }

    @Override
    public void onBackPressed() {
        if (mPager.getCurrentItem() == 0) {
            disableTutorial();
            super.onBackPressed();
        } else {
            mPager.setCurrentItem(mPager.getCurrentItem() - 1);
        }
    }

    private class TutorialPagerAdapter extends FragmentStatePagerAdapter
            implements MeasureViewPager.OnMeasureListener,
            ViewPager.OnPageChangeListener {

        private double mIndicatorBaseWidth;
        private View mProgressPrevView;
        private View mProgressCurrentView;
        private View mProgressNextView;
        private Integer[] mImageAlignment;
        private Integer[] mImageResIds;
        private Integer[] mHeaderResIds;
        private Integer[] mContentResIds;

        public TutorialPagerAdapter(FragmentManager fm,
                                    Integer[] imageAlignment,
                                    Integer[] imageResIds,
                                    Integer[] headerResIds,
                                    Integer[] contentResIds) {
            super(fm);
            mProgressPrevView = findViewById(R.id.pager_progress_prev);
            mProgressCurrentView = findViewById(R.id.pager_progress_current);
            mProgressNextView = findViewById(R.id.pager_progress_next);
            mImageAlignment = imageAlignment;
            mImageResIds = imageResIds;
            mHeaderResIds = headerResIds;
            mContentResIds = contentResIds;
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
            nextViewParams.width = (int) Math.ceil(mIndicatorBaseWidth *
                    (mImageAlignment.length - position));
            mProgressNextView.setLayoutParams(nextViewParams);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                return new TutorialSplashFragment();
            } else {
                return TutorialContentFragment
                        .newInstance(mImageAlignment[position - 1],
                                mImageResIds[position - 1],
                                mHeaderResIds[position - 1],
                                mContentResIds[position - 1]);
            }
        }

        @Override
        public int getCount() {
            return mImageAlignment.length + 1;
        }

        @Override
        public void onMeasure(int width, int height) {
            mIndicatorBaseWidth = (double) width / (mImageAlignment.length + 1);
            updateIndicators(0);
        }

        @Override
        public void onPageScrolled(int position, float positionOffset,
                                   int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            updateIndicators(position);
            if (position == mImageAlignment.length) {
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

    private void disableTutorial() {
        SharedPreferences preferences =
                getSharedPreferences(Preferences.PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(Preferences.PREFS_KEY_SHOW_TUTORIAL, false);
        editor.commit();
    }

    private class TutorialFinishedListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            disableTutorial();
            finish();
        }
    }
}
