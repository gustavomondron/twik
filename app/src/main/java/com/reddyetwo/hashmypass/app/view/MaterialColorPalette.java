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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.reddyetwo.hashmypass.app.R;

public class MaterialColorPalette extends RecyclerView {

    private int[] mNormalColorList = new int[]{};
    private int[] mPressedColorList = new int[]{};
    private int[] mRippleColorList = new int[]{};
    private int mSelectedPosition = 0;
    private OnColorSelectedListener mColorSelectedListener;

    public MaterialColorPalette(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray array = context.getTheme()
                .obtainStyledAttributes(attrs, R.styleable.MaterialColorPalette, 0, 0);
        int normalColorsResId =
                array.getResourceId(R.styleable.MaterialColorPalette_color_palette_normal, 0);
        int pressedColorsResId =
                array.getResourceId(R.styleable.MaterialColorPalette_color_palette_pressed, 0);
        int rippleColorsResId =
                array.getResourceId(R.styleable.MaterialColorPalette_color_palette_ripple, 0);

        if (normalColorsResId != 0) {
            mNormalColorList = getResources().getIntArray(normalColorsResId);
        }

        if (pressedColorsResId != 0) {
            mPressedColorList = getResources().getIntArray(pressedColorsResId);
        } else {
            mPressedColorList = mNormalColorList;
        }

        if (rippleColorsResId != 0) {
            mRippleColorList = getResources().getIntArray(rippleColorsResId);
        } else {
            mRippleColorList = mNormalColorList;
        }

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        setLayoutManager(layoutManager);
        setAdapter(new ColorPaletteAdapter());

    }

    public void setSelectedPosition(int position) {
        mSelectedPosition = position;
        ((ColorPaletteAdapter) getAdapter()).updateButtons(position);
    }

    public void setOnColorSelectedListener(OnColorSelectedListener listener) {
        mColorSelectedListener = listener;
    }


    private class ColorPaletteViewHolder extends RecyclerView.ViewHolder {

        private final MaterialColorPaletteButton mButton;

        public ColorPaletteViewHolder(View itemView) {
            super(itemView);
            mButton = (MaterialColorPaletteButton) itemView
                    .findViewById(R.id.material_color_palette_button);
        }

        public MaterialColorPaletteButton getButton() {
            return mButton;
        }
    }

    private class ColorPaletteAdapter extends Adapter<ColorPaletteViewHolder> {

        @Override
        public ColorPaletteViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.material_color_palette_item, parent, false);
            return new ColorPaletteViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ColorPaletteViewHolder holder, final int position) {
            final MaterialColorPaletteButton button = holder.getButton();
            button.setColor(mNormalColorList[position], mPressedColorList[position],
                    mRippleColorList[position]);
            button.setSelected(mSelectedPosition == position);
            button.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    button.setSelected(true);
                    mSelectedPosition = position;
                    updateButtons(position);
                    if (mColorSelectedListener != null) {
                        mColorSelectedListener.onColorSelected(position);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return mNormalColorList.length;
        }

        public void updateButtons(int selectedPosition) {
            for (int i = 0; i < selectedPosition; i++) {
                notifyItemChanged(i);
            }
            for (int i = selectedPosition + 1; i < mNormalColorList.length; i++) {
                notifyItemChanged(i);
            }
        }
    }

    public interface OnColorSelectedListener {
        void onColorSelected(int index);
    }


}
