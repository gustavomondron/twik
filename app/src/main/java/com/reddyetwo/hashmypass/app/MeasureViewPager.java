package com.reddyetwo.hashmypass.app;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;

public class MeasureViewPager extends ViewPager {

    private OnMeasureListener mOnMeasureListener;

    public MeasureViewPager(Context context) {
        super(context);
    }

    public MeasureViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setmOnMeasureListener(OnMeasureListener onMeasureListener) {
        mOnMeasureListener = onMeasureListener;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mOnMeasureListener.onMeasure(MeasureSpec.getSize(widthMeasureSpec),
                MeasureSpec.getSize(heightMeasureSpec));
    }

    public interface OnMeasureListener {
        public void onMeasure(int width, int height);
    }
}
