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
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.reddyetwo.hashmypass.app.R;
import com.reddyetwo.hashmypass.app.util.FabUtils;

/**
 * View which allows choosing a color from a color palette
 */
public class MaterialColorPalette extends RecyclerView {

    private int[] mNormalColorList = new int[]{};
    private int[] mPressedColorList = new int[]{};
    private int mSelectedPosition = 0;
    private OnColorSelectedListener mColorSelectedListener;

    /**
     * Constructor
     *
     * @param context the context
     * @param attrs   the view attributes
     */
    public MaterialColorPalette(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray array = context.getTheme()
                .obtainStyledAttributes(attrs, R.styleable.MaterialColorPalette, 0, 0);
        int normalColorsResId =
                array.getResourceId(R.styleable.MaterialColorPalette_color_palette_normal, 0);
        int pressedColorsResId =
                array.getResourceId(R.styleable.MaterialColorPalette_color_palette_pressed, 0);

        if (normalColorsResId != 0) {
            mNormalColorList = getResources().getIntArray(normalColorsResId);
        }

        if (pressedColorsResId != 0) {
            mPressedColorList = getResources().getIntArray(pressedColorsResId);
        } else {
            mPressedColorList = mNormalColorList;
        }

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        setLayoutManager(layoutManager);
        setAdapter(new ColorPaletteAdapter());
    }

    /**
     * Set the selected color in the palette
     *
     * @param position the color position
     */
    public void setSelectedPosition(int position) {
        ((ColorPaletteAdapter) getAdapter()).updateButtons(position, mSelectedPosition);
        mSelectedPosition = position;
    }

    /**
     * Set the color selected listener
     *
     * @param listener the listener
     */
    public void setOnColorSelectedListener(OnColorSelectedListener listener) {
        mColorSelectedListener = listener;
    }

    /**
     * Interface which can be implemented to listen to color selected events
     */
    public interface OnColorSelectedListener {

        /**
         * Method called when a color is selected
         *
         * @param index the color index
         */
        void onColorSelected(int index);
    }

    private class ColorPaletteViewHolder extends RecyclerView.ViewHolder {

        private final FloatingActionButton mButton;

        /**
         * Constructor
         *
         * @param itemView a color button {@link android.view.View} instance
         */
        public ColorPaletteViewHolder(View itemView) {
            super(itemView);
            mButton = (FloatingActionButton) itemView
                    .findViewById(R.id.color_button);
        }

        /**
         * Get the FloatingActionButton of this color
         *
         * @return the color button
         */
        public FloatingActionButton getButton() {
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
            final FloatingActionButton button = holder.getButton();
            FabUtils.setFabColor(button, mNormalColorList[position], mPressedColorList[position]);
            setButtonSelectedStatus(button, mSelectedPosition == position);
            button.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    setButtonSelectedStatus(button, true);
                    updateButtons(position, mSelectedPosition);
                    mSelectedPosition = position;
                    if (mColorSelectedListener != null) {
                        mColorSelectedListener.onColorSelected(position);
                    }
                }
            });
        }

        private void setButtonSelectedStatus(FloatingActionButton fab, boolean selected) {
            if (selected) {
                fab.setImageResource(R.drawable.ic_beenhere_white_18dp);
            } else {
                fab.setImageResource(android.R.color.transparent);
            }
        }

        @Override
        public int getItemCount() {
            return mNormalColorList.length;
        }

        /**
         * Updates button views, highlighting the selected color
         *
         * @param newPosition the selected color position
         * @param previousPosition the previously selected color position
         */
        public void updateButtons(int newPosition, int previousPosition) {
            notifyItemChanged(newPosition);
            notifyItemChanged(previousPosition);
        }
    }


}
