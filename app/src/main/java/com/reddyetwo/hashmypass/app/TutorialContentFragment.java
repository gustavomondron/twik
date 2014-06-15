package com.reddyetwo.hashmypass.app;

import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TutorialContentFragment extends Fragment {

    public static final String IMAGE_ALIGN_EXTRA = "image_position";
    public static final String IMAGE_RES_EXTRA = "image";
    public static final String HEADER_RES_EXTRA = "header";
    public static final String CONTENT_RES_EXTRA = "content";

    public static final int ALIGN_BOTTOM = 1;
    public static final int ALIGN_CENTER = 2;

    private int mImageResId;
    private int mHeaderResId;
    private int mContentResId;

    private ViewGroup mRootView;
    private ImageView mImageView;
    private TextView mHeaderTextView;
    private TextView mContentTextView;

    public TutorialContentFragment() {
        super();
    }

    public static TutorialContentFragment newInstance(int imageAlign,
                                                      int imageResId,
                                                      int headerResId,
                                                      int contentResId) {
        final TutorialContentFragment f = new TutorialContentFragment();
        final Bundle args = new Bundle();
        args.putInt(IMAGE_ALIGN_EXTRA, imageAlign);
        args.putInt(IMAGE_RES_EXTRA, imageResId);
        args.putInt(HEADER_RES_EXTRA, headerResId);
        args.putInt(CONTENT_RES_EXTRA, contentResId);
        f.setArguments(args);
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        int layout;
        switch (getArguments().getInt(IMAGE_ALIGN_EXTRA, ALIGN_BOTTOM)) {
            case ALIGN_BOTTOM:
                layout = R.layout.fragment_tutorial_screenshot_screen;
                break;
            case ALIGN_CENTER:
            default:
                layout = R.layout.fragment_tutorial_content_screen;
                break;
        }

        mImageResId = getArguments().getInt(IMAGE_RES_EXTRA);
        mHeaderResId = getArguments().getInt(HEADER_RES_EXTRA);
        mContentResId = getArguments().getInt(CONTENT_RES_EXTRA);

        mRootView =
                (ViewGroup) inflater.inflate(layout, container, false);
        mImageView =
                (ImageView) mRootView.findViewById(R.id.tutorial_content_image);
        mHeaderTextView =
                (TextView) mRootView.findViewById(R.id.tutorial_header_text);
        mContentTextView =
                (TextView) mRootView.findViewById(R.id.tutorial_content_text);
        return mRootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mImageView.setImageResource(mImageResId);
        mHeaderTextView.setText(mHeaderResId);
        mContentTextView.setText(mContentResId);
    }
}
