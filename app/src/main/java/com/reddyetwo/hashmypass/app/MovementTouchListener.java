package com.reddyetwo.hashmypass.app;

import android.content.Context;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

public class MovementTouchListener implements View.OnTouchListener {

    private static final int STATE_PRESSING = 1;
    private static final int STATE_RELEASED = 2;

    private static final int MOVEMENT_THRESHOLD_DP = 15; // 15 dp ~ 3 mm

    private int mState = STATE_RELEASED;
    private OnPressedListener mOnPressedListener;
    private float mMovementThresholdPx;
    private float mX;
    private float mY;

    public MovementTouchListener(Context context,
                                 OnPressedListener onPressedListener) {
        mOnPressedListener = onPressedListener;
        mMovementThresholdPx = TypedValue
                .applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                        MOVEMENT_THRESHOLD_DP,
                        context.getResources().getDisplayMetrics());

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        switch (mState) {
            case STATE_RELEASED:
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mState = STATE_PRESSING;
                        mX = event.getX();
                        mY = event.getY();
                        break;
                    default:
                }
                break;
            case STATE_PRESSING:
                switch (event.getAction()) {
                    case MotionEvent.ACTION_UP:
                        mState = STATE_RELEASED;
                        mOnPressedListener.onPressed();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (Math.abs(event.getX() - mX) >
                                mMovementThresholdPx ||
                                Math.abs(event.getY() - mY) >
                                        mMovementThresholdPx) {
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
}