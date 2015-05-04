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

package com.reddyetwo.hashmypass.app.animation;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.content.Context;

import com.reddyetwo.hashmypass.app.R;

/**
 * Generator of view animations
 */
public class Animations {

    private Animations() {
    }

    /**
     * Get an animator from visible to invisible state
     *
     * @param context the {@link android.content.Context} instance
     * @param object  the target object
     * @return the {@link android.animation.AnimatorSet} instance
     */
    public static AnimatorSet getToInvisibleAnimatorSet(Context context, Object object) {
        AnimatorSet animatorSet =
                (AnimatorSet) AnimatorInflater.loadAnimator(context, R.animator.invisible);
        animatorSet.setTarget(object);
        return animatorSet;
    }

    /**
     * Get an animator from visible to invisible state
     *
     * @param context  the {@link android.content.Context} instance
     * @param object   the the target object
     * @param duration the animation duration
     * @return the {@link android.animation.AnimatorSet} instance
     */
    @SuppressWarnings("SameParameterValue")
    public static AnimatorSet getToInvisibleAnimatorSet(Context context, Object object,
                                                        long duration) {
        AnimatorSet animatorSet = getToInvisibleAnimatorSet(context, object);
        animatorSet.setDuration(duration);
        return animatorSet;
    }

    /**
     * Get an animator from invisible to visible state
     *
     * @param context the {@link android.content.Context} instance
     * @param object  the target object
     * @return the {@link android.animation.AnimatorSet} instance
     */
    public static AnimatorSet getToVisibleAnimatorSet(Context context, Object object) {
        AnimatorSet animatorSet =
                (AnimatorSet) AnimatorInflater.loadAnimator(context, R.animator.visible);
        animatorSet.setTarget(object);
        return animatorSet;
    }

    /**
     * Get an animator from invisible to visible state
     *
     * @param context  the {@link android.content.Context} instance
     * @param object   the target object
     * @param duration the animation duration
     * @return the {@link android.animation.AnimatorSet} instance
     */
    @SuppressWarnings("SameParameterValue")
    public static AnimatorSet getToVisibleAnimatorSet(Context context, Object object,
                                                      long duration) {
        AnimatorSet animatorSet = getToVisibleAnimatorSet(context, object);
        animatorSet.setDuration(duration);
        return animatorSet;
    }
}
