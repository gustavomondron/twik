/*
 * Copyright 2014 Red Dye No. 2
 *
 * This file is part of Hash My pass.
 *
 * Hash my pass is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Hash my pass is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Hash my pass.  If not, see <http://www.gnu.org/licenses/>.
 */

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
