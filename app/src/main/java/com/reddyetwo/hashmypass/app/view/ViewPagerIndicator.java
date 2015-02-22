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

public class ViewPagerIndicator extends View {

    private static final float DEFAULT_ALPHA = 0.5f;
    private static final float DEFAULT_CURRENT_POSITION_ALPHA = 0.8f;
    private static final int ALPHA_MAX = 255;

    private final float mRadius;
    private final float mSpacing;
    private int mPosition;
    private final int mAlpha;
    private final int mCurrentPositionAlpha;
    private int mNumberOfItems;
    private final Paint mPaint = new Paint();
    private ViewPager.OnPageChangeListener mPageChangeListener;

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

    public void setViewPager(ViewPager viewPager) {
        mNumberOfItems = viewPager.getAdapter().getCount();
        viewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                setPosition(position);
                if (mPageChangeListener != null) {
                    mPageChangeListener.onPageSelected(position);
                }
            }

            @Override
            public void onPageScrolled(int position, float positionOffset,
                                       int positionOffsetPixels) {
                if (mPageChangeListener != null) {
                    mPageChangeListener
                            .onPageScrolled(position, positionOffset, positionOffsetPixels);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (mPageChangeListener != null) {
                    mPageChangeListener.onPageScrollStateChanged(state);
                }
            }
        });
    }

    public void setOnPageChangeListener(ViewPager.OnPageChangeListener pageChangeListener) {
        mPageChangeListener = pageChangeListener;
    }

    void setPosition(int position) {
        mPosition = position;
        invalidate();
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int measuredWidth = (int) (mSpacing * (mNumberOfItems - 1) + mRadius * 2 * mNumberOfItems);
        int measuredHeight = (int) (mRadius * 2);
        setMeasuredDimension(measuredWidth, measuredHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float y = mRadius;
        for (int i = 0; i < mNumberOfItems; i++) {
            float x = i * mSpacing + (1 + 2 * i) * mRadius;
            int alpha = i == mPosition ? mCurrentPositionAlpha : mAlpha;
            mPaint.setAlpha(alpha);
            canvas.drawCircle(x, y, mRadius, mPaint);
        }
    }
}
