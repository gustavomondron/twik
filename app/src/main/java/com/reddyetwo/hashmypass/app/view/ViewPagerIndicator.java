/*
 * Copyright 2015 Red Dye No. 2
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

package com.reddyetwo.hashmypass.app.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;

import com.reddyetwo.hashmypass.app.R;

/**
 * Indicator of current page in a {@link android.support.v4.view.ViewPager}
 */
public class ViewPagerIndicator extends View {

    private static final float DEFAULT_ALPHA = 0.5f;
    private static final float DEFAULT_CURRENT_POSITION_ALPHA = 0.8f;
    private static final int ALPHA_MAX = 255;
    private static final int RADIUS_TO_DIAMETER_RATIO = 2;
    private static final int HALF_RATIO = 2;

    private final float mRadius;
    private final float mSpacing;
    private final int mAlpha;
    private final int mCurrentPositionAlpha;
    private final Paint mPaint = new Paint();
    private int mPosition;
    private int mNumberOfItems;
    private int mWidthOffset = 0;
    private int mHeightOffset = 0;
    private ViewPager.OnPageChangeListener mPageChangeListener;

    /**
     * Constructor
     *
     * @param context the {@link android.content.Context} instance
     * @param attrs   the view attributes
     */
    public ViewPagerIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray array = context.getTheme()
                .obtainStyledAttributes(attrs, R.styleable.ViewPagerIndicator, 0, 0);
        mRadius = array.getDimensionPixelSize(R.styleable.ViewPagerIndicator_radius, 0);
        mSpacing = array.getDimensionPixelSize(R.styleable.ViewPagerIndicator_spacing, 0);
        mAlpha = (int) (array.getFloat(R.styleable.ViewPagerIndicator_alpha, DEFAULT_ALPHA) *
                ALPHA_MAX);
        mCurrentPositionAlpha = (int) (array
                .getFloat(R.styleable.ViewPagerIndicator_current_position_alpha,
                        DEFAULT_CURRENT_POSITION_ALPHA) * ALPHA_MAX);

        // Initialize paint
        mPaint.setAntiAlias(true);
        mPaint.setColor(getResources().getColor(android.R.color.white));

    }

    /**
     * Set the view pager
     *
     * @param viewPager the {@link android.support.v4.view.ViewPager} instance
     */
    public void setViewPager(ViewPager viewPager) {
        mNumberOfItems = viewPager.getAdapter().getCount();
        viewPager.setOnPageChangeListener(new IndicatorSimpleOnPageChangeListener());
    }

    /**
     * Set the {@link android.support.v4.view.ViewPager.OnPageChangeListener} listener
     *
     * @param pageChangeListener the {@link android.support.v4.view.ViewPager.OnPageChangeListener} instance
     */
    public void setOnPageChangeListener(ViewPager.OnPageChangeListener pageChangeListener) {
        mPageChangeListener = pageChangeListener;
    }

    private void setPosition(int position) {
        mPosition = position;
        invalidate();
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int desiredWidth = (int) (mSpacing * (mNumberOfItems - 1) +
                mRadius * RADIUS_TO_DIAMETER_RATIO * mNumberOfItems);
        int desiredHeight = (int) (mRadius * RADIUS_TO_DIAMETER_RATIO);

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int measuredWidth;
        int measuredHeight;

        // Measure width
        if (widthMode == MeasureSpec.EXACTLY) {
            // layout_width has been specified. Make the view this size.
            measuredWidth = widthSize;
            // Calculate offset to draw the indicator centered in the view
            if (measuredWidth > desiredWidth) {
                mWidthOffset = (measuredWidth - desiredWidth) / HALF_RATIO;
            }
        } else if (widthMode == MeasureSpec.AT_MOST) {
            // layout_width is set to match_parent or wrap_content.
            measuredWidth = Math.min(desiredWidth, widthSize);
        } else {
            // layout_width is not specified
            measuredWidth = desiredWidth;
        }

        // Measure height
        if (heightMode == MeasureSpec.EXACTLY) {
            measuredHeight = heightSize;
            if (measuredHeight > desiredHeight) {
                mHeightOffset = (measuredHeight - desiredHeight) / HALF_RATIO;
            }
        } else if (heightMode == MeasureSpec.AT_MOST) {
            measuredHeight = Math.min(desiredHeight, heightSize);
        } else {
            measuredHeight = desiredHeight;
        }

        setMeasuredDimension(measuredWidth, measuredHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float y = mHeightOffset + mRadius;
        for (int i = 0; i < mNumberOfItems; i++) {
            float x = mWidthOffset + i * mSpacing + (1 + RADIUS_TO_DIAMETER_RATIO * i) * mRadius;
            int alpha = i == mPosition ? mCurrentPositionAlpha : mAlpha;
            mPaint.setAlpha(alpha);
            canvas.drawCircle(x, y, mRadius, mPaint);
        }
    }

    private class IndicatorSimpleOnPageChangeListener extends ViewPager.SimpleOnPageChangeListener {

        @Override
        public void onPageSelected(int position) {
            setPosition(position);
            if (mPageChangeListener != null) {
                mPageChangeListener.onPageSelected(position);
            }
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            if (mPageChangeListener != null) {
                mPageChangeListener.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            if (mPageChangeListener != null) {
                mPageChangeListener.onPageScrollStateChanged(state);
            }
        }
    }
}
