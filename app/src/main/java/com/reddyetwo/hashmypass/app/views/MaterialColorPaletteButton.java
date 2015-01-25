/*
 * Copyright 2015 Red Dye No. 2
 * Copyright 2014 Oleksander Melnykov
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


package com.reddyetwo.hashmypass.app.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Outline;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.support.annotation.ColorRes;
import android.support.annotation.DimenRes;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.ImageButton;

import com.reddyetwo.hashmypass.app.R;
import com.reddyetwo.hashmypass.app.util.ApiUtils;

public class MaterialColorPaletteButton extends ImageButton {

    private int mColorNormal;
    private int mColorPressed;
    private int mColorRipple;
    private int mShadowSize;
    private boolean mMarginsSet;

    public MaterialColorPaletteButton(Context context) {
        this(context, null);
    }

    public MaterialColorPaletteButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MaterialColorPaletteButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    @Override
    public void setSelected(boolean selected) {
        super.setSelected(selected);
        if (selected) {
            int mImageResId = R.drawable.ic_beenhere_white_18dp;
            setImageResource(mImageResId);
        } else {
            setImageResource(android.R.color.transparent);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int size = getDimension(R.dimen.fab_size_mini);
        if (!ApiUtils.hasLollipopApi()) {
            size += mShadowSize * 2;
            setMarginsWithoutShadow();
        }
        setMeasuredDimension(size, size);
    }

    private void init() {
        mColorNormal = getColor(R.color.material_blue_500);
        mColorPressed = getColor(R.color.material_blue_600);
        mColorRipple = getColor(android.R.color.white);
        mShadowSize = getDimension(R.dimen.fab_shadow_size);
        updateBackground();
    }

    private void updateBackground() {
        StateListDrawable drawable = new StateListDrawable();
        drawable.addState(new int[]{android.R.attr.state_pressed}, createDrawable(mColorPressed));
        drawable.addState(new int[]{}, createDrawable(mColorNormal));
        setBackgroundCompat(drawable);
    }

    private Drawable createDrawable(int color) {
        OvalShape ovalShape = new OvalShape();
        ShapeDrawable shapeDrawable = new ShapeDrawable(ovalShape);
        shapeDrawable.getPaint().setColor(color);

        if (!ApiUtils.hasLollipopApi()) {
            Drawable shadowDrawable = getResources().getDrawable(R.drawable.shadow_mini);
            LayerDrawable layerDrawable =
                    new LayerDrawable(new Drawable[]{shadowDrawable, shapeDrawable});
            layerDrawable.setLayerInset(1, mShadowSize, mShadowSize, mShadowSize, mShadowSize);
            return layerDrawable;
        } else {
            return shapeDrawable;
        }
    }

    private int getColor(@ColorRes int id) {
        return getResources().getColor(id);
    }

    private int getDimension(@DimenRes int id) {
        return getResources().getDimensionPixelSize(id);
    }

    private void setMarginsWithoutShadow() {
        if (!mMarginsSet && getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams layoutParams =
                    (ViewGroup.MarginLayoutParams) getLayoutParams();
            int leftMargin = layoutParams.leftMargin - mShadowSize;
            int topMargin = layoutParams.topMargin - mShadowSize;
            int rightMargin = layoutParams.rightMargin - mShadowSize;
            int bottomMargin = layoutParams.bottomMargin - mShadowSize;
            layoutParams.setMargins(leftMargin, topMargin, rightMargin, bottomMargin);

            requestLayout();
            mMarginsSet = true;
        }
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    private void setBackgroundCompat(Drawable drawable) {
        if (ApiUtils.hasLollipopApi()) {
            float elevation = getElevation() > 0.0f ? getElevation() :
                    getDimension(R.dimen.fab_elevation_lollipop);
            setElevation(elevation);
            RippleDrawable rippleDrawable =
                    new RippleDrawable(new ColorStateList(new int[][]{{}}, new int[]{mColorRipple}),
                            drawable, null);
            setOutlineProvider(new ViewOutlineProvider() {
                @Override
                public void getOutline(View view, Outline outline) {
                    int size = getDimension(R.dimen.fab_size_mini);
                    outline.setOval(0, 0, size, size);
                }
            });
            setClipToOutline(true);
            setBackground(rippleDrawable);
        } else if (ApiUtils.hasJellyBeanApi()) {
            setBackground(drawable);
        } else {
            setBackgroundDrawable(drawable);
        }
    }

    public void setColor(@ColorRes int colorNormal, @ColorRes int colorPressed,
                         @ColorRes int colorRipple) {
        if (mColorNormal != colorNormal || mColorPressed != colorPressed ||
                mColorRipple != colorRipple) {
            mColorNormal = colorNormal;
            mColorPressed = colorPressed;
            mColorRipple = colorRipple;
            updateBackground();
        }
    }
}
