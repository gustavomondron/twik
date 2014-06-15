package com.reddyetwo.hashmypass.app;

import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class TutorialSplashFragment extends Fragment {

    private ViewGroup mRootView;
    private ImageView mImageView;

    public TutorialSplashFragment() {
        super();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = (ViewGroup) inflater
                .inflate(R.layout.fragment_tutorial_splash_screen, container,
                        false);

        mImageView = (ImageView) mRootView.findViewById(R.id
                .tutorial_content_image);
        return mRootView;
    }
}
