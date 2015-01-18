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

package com.reddyetwo.hashmypass.app.util;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

public class Fab extends View {
    private final Context _context;
    private Paint mButtonPaint;
    private Paint mDrawablePaint;
    private Bitmap mBitmap;
    private int mFabColor;
    private int mScreenHeight;
    private float currentY;
    private boolean mHidden = false;

    public Fab(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        _context = context;
        init(Color.WHITE);
    }

    @SuppressLint("NewApi")
    public Fab(Context context) {
        super(context);
        _context = context;
        init(Color.WHITE);
    }

    public void setFabColor(int fabColor) {
        init(fabColor);
    }

    public int getFabColor() {
        return mFabColor;
    }

    public void setFabDrawable(Drawable fabDrawable) {
        mBitmap = ((BitmapDrawable) fabDrawable).getBitmap();
        invalidate();
    }


    void init(int fabColor) {
        mFabColor = fabColor;
        setWillNotDraw(false);
        this.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        mButtonPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mButtonPaint.setColor(fabColor);
        mButtonPaint.setStyle(Paint.Style.FILL);
        mButtonPaint
                .setShadowLayer(10.0f, 0.0f, 3.5f, Color.argb(100, 0, 0, 0));
        mDrawablePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        invalidate();

        WindowManager mWindowManager = (WindowManager) _context
                .getSystemService(Context.WINDOW_SERVICE);
        Display display = mWindowManager.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        mScreenHeight = size.y;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        setClickable(true);
        canvas.drawCircle(getWidth() / 2, getHeight() / 2,
                (float) (getWidth() / 2.6), mButtonPaint);
        canvas.drawBitmap(mBitmap, (getWidth() - mBitmap.getWidth()) / 2,
                (getHeight() - mBitmap.getHeight()) / 2, mDrawablePaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            setAlpha(1.0f);
        } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
            setAlpha(0.6f);
        }
        return super.onTouchEvent(event);
    }

    public int dpToPx(int dp) {
        DisplayMetrics displayMetrics =
                getContext().getResources().getDisplayMetrics();
        return Math.round(dp *
                (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public void hideFab() {
        if (!mHidden) {
            currentY = getY();
            ObjectAnimator mHideAnimation =
                    ObjectAnimator.ofFloat(this, "Y", mScreenHeight);
            mHideAnimation.setInterpolator(new AccelerateInterpolator());
            mHideAnimation.start();
            mHidden = true;
        }
    }

    public void showFab() {
        if (mHidden) {
            ObjectAnimator mShowAnimation =
                    ObjectAnimator.ofFloat(this, "Y", currentY);
            mShowAnimation.setInterpolator(new DecelerateInterpolator());
            mShowAnimation.start();
            mHidden = false;
        }
    }

}