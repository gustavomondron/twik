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
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

class MovementTouchListener implements View.OnTouchListener {

    private static final int STATE_PRESSING = 1;
    private static final int STATE_RELEASED = 2;
    private int mState = STATE_RELEASED;
    // Threshold ~= 3 mm
    private static final int MOVEMENT_THRESHOLD_DP = 15;
    private final OnPressedListener mOnPressedListener;
    private final float mMovementThresholdPx;
    private float mX;
    private float mY;

    public MovementTouchListener(Context context, OnPressedListener onPressedListener) {
        mOnPressedListener = onPressedListener;
        mMovementThresholdPx = TypedValue
                .applyDimension(TypedValue.COMPLEX_UNIT_DIP, MOVEMENT_THRESHOLD_DP,
                        context.getResources().getDisplayMetrics());

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (mState == STATE_RELEASED && event.getAction() == MotionEvent.ACTION_DOWN) {
            mState = STATE_PRESSING;
            mX = event.getX();
            mY = event.getY();
        } else if (mState == STATE_PRESSING) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_UP:
                    mState = STATE_RELEASED;
                    mOnPressedListener.onPressed();
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (movementThresholdReached(event.getX(), event.getY())) {
                        mState = STATE_RELEASED;
                    }
                    break;
                default:
                    mState = STATE_RELEASED;
            }
        }
        return true;
    }

    public interface OnPressedListener {
        public void onPressed();
    }

    private boolean movementThresholdReached(float x, float y) {
        return Math.abs(x - mX) > mMovementThresholdPx ||
                Math.abs(y - mY) > mMovementThresholdPx;
    }
}
