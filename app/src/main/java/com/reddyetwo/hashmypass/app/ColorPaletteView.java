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

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

public class ColorPaletteView extends View {

    /* Sizes (dip) */
    private static final int SIZE_BORDER_WIDTH = 2;
    private static final int SIZE_RECTANGLE_WIDTH = 41;
    private static final int SIZE_SPACING = 16;
    private static final int SIZE_PADDING = 5;

    private int mRectangleSize;
    private int mSpacing;
    private int mBorderWidth;
    private int mPadding;
    private int mAccentColor;
    private int[] mColors;
    private int mIndex = -1;
    private Paint[] mFillPaint;
    private Paint mAccentPaint;
    private GestureDetector mDetector;
    private OnColorSelectedListener mColorSelectedListener = null;
    private boolean mColorSelected = false;

    public ColorPaletteView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.getTheme()
                .obtainStyledAttributes(attrs, R.styleable.ColorPaletteView, 0,
                        0);

        int paletteResId =
                a.getResourceId(R.styleable.ColorPaletteView_colors, 0);
        if (paletteResId != 0) {
            mColors = getResources().getIntArray(paletteResId);
        }
        mIndex = a.getInt(R.styleable.ColorPaletteView_index, -1);
        mAccentColor = a.getColor(R.styleable.ColorPaletteView_accent_color, 0);
        a.recycle();
        init();
    }

    private float dpToPx(int dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                getResources().getDisplayMetrics());
    }

    /**
     * Calculates the X position that should be set in the horizontal scroll
     * view containing the palette, according to the selected item.
     *
     * @return the X coordinate
     */
    public int getSelectedColorScrollX() {
        int position = 0;
        if (mIndex > 0) {
            int itemCenterX = (mRectangleSize + 2 * mPadding +
                    mSpacing) * mIndex + mPadding +
                    mRectangleSize / 2;
            int paletteWidth = getPaletteWidth();
            int parentWidth = ((View) getParent()).getWidth();
            // Check if we can center on the item
            if (itemCenterX >= parentWidth / 2 &&
                    (paletteWidth - itemCenterX) >= parentWidth / 2) {
                // We can center on the item
                position = itemCenterX - parentWidth / 2;
            } else if (itemCenterX < parentWidth / 2) {
                /* We are at the beginning of the palette */
                position = 0;
            } else {
                /* We are at the end of the palette */
                position = paletteWidth - parentWidth;
            }
        }

        return position;
    }

    public int[] getColors() {
        return mColors;
    }

    public void setColors(int[] colors) {
        mColors = colors;
        init();
        invalidate();
        requestLayout();
    }

    public int getSelectedColorIndex() {
        return mIndex;
    }

    public void setSelectedColorIndex(int index) {
        mIndex = index;
    }

    public void setAccentColor(int accentColor) {
        mAccentColor = accentColor;
        init();
        invalidate();
    }

    public int getAccentColor() {
        return mAccentColor;
    }

    public void setOnColorSelectedListener(OnColorSelectedListener listener) {
        mColorSelectedListener = listener;
    }

    private void init() {
        /* Translate sizes from dp to px */
        mBorderWidth = (int) dpToPx(SIZE_BORDER_WIDTH);
        mRectangleSize = (int) dpToPx(SIZE_RECTANGLE_WIDTH);
        mSpacing = (int) dpToPx(SIZE_SPACING);
        mPadding = (int) dpToPx(SIZE_PADDING);

        mFillPaint = new Paint[mColors.length];
        for (int i = 0; i < mColors.length; i++) {
            mFillPaint[i] = new Paint(Paint.ANTI_ALIAS_FLAG);
            mFillPaint[i].setColor(mColors[i]);
            mFillPaint[i].setStyle(Paint.Style.FILL);
        }
        mAccentPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mAccentPaint.setColor(mAccentColor);
        mAccentPaint.setStyle(Paint.Style.STROKE);
        mAccentPaint.setStrokeWidth(mBorderWidth);

        mDetector = new GestureDetector(getContext(), new mGestureListener());
        mDetector.setIsLongpressEnabled(false);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (int i = 0; i < mFillPaint.length; i++) {
            /* Draw all colors */
            canvas.drawRect(
                    (mRectangleSize + 2 * mPadding + mSpacing) * i + mPadding,
                    mPadding,
                    (mRectangleSize + 2 * mPadding + mSpacing) * i + mPadding +
                            mRectangleSize, mPadding + mRectangleSize,
                    mFillPaint[i]);
        }

        /* Remark selected color */
        if (mIndex >= 0 && mIndex < mColors.length) {
            canvas.drawRect(mBorderWidth / 2 + (mRectangleSize + 2 * mPadding +
                            mSpacing) * mIndex, mBorderWidth / 2,
                    (mRectangleSize + 2 * mPadding + mSpacing) * mIndex +
                            mRectangleSize +
                            2 * mPadding - mBorderWidth / 2,
                    2 * mPadding + mRectangleSize - mBorderWidth / 2,
                    mAccentPaint);
        }
    }

    private int getPaletteWidth() {
        return (mRectangleSize + 2 * mPadding) * mColors.length +
                mSpacing * (mColors.length - 1) + mBorderWidth;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(getPaletteWidth(),
                mRectangleSize + mPadding * 2 + mBorderWidth);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean result = mDetector.onTouchEvent(event);
        if (result && mColorSelected) {
            mIndex = (int) (event.getX() / (mRectangleSize + 2 * mPadding +
                    mSpacing));
            if (mColorSelectedListener != null) {
                invalidate();
                mColorSelectedListener.onColorSelected(this, mIndex);
            }
        }
        return result;
    }

    public interface OnColorSelectedListener {
        void onColorSelected(ColorPaletteView source, int color);
    }

    private class mGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDown(MotionEvent e) {
            mColorSelected = false;
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            mColorSelected = true;
            return true;
        }
    }
}
