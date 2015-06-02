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

package com.reddyetwo.hashmypass.app.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.melnykov.fab.FloatingActionButton;
import com.reddyetwo.hashmypass.app.R;

public class ColorPaletteAdapter extends RecyclerView.Adapter<ColorPaletteViewHolder> {

    private final int[] mNormalColorList;
    private final int[] mPressedColorList;
    private final int[] mRippleColorList;
    private int mSelectedPosition = 0;
    private OnColorSelectedListener mColorSelectedListener;

    public ColorPaletteAdapter(int[] normalColorList, int[] pressedColorList, int[] rippleColorList) {
        mNormalColorList = normalColorList;
        mPressedColorList = pressedColorList;
        mRippleColorList = rippleColorList;
    }

    public void setOnColorSelectedListener(OnColorSelectedListener listener) {
        mColorSelectedListener = listener;
    }

    public void selectItem(int position) {
        updateButtons(position, mSelectedPosition);
        mSelectedPosition = position;
    }

    @Override
    public ColorPaletteViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.material_color_palette_item, parent, false);
        return new ColorPaletteViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ColorPaletteViewHolder holder, final int position) {
        final FloatingActionButton button = holder.getButton();
        button.setColorNormal(mNormalColorList[position]);
        button.setColorPressed(mPressedColorList[position]);
        button.setColorRipple(mRippleColorList[position]);
        setButtonSelectedStatus(button, mSelectedPosition == position);
        button.setOnClickListener(new View.OnClickListener() {
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

    @Override
    public int getItemCount() {
        return mNormalColorList.length;
    }

    private void setButtonSelectedStatus(FloatingActionButton fab, boolean selected) {
        if (selected) {
            fab.setImageResource(R.drawable.ic_beenhere_white_18dp);
        } else {
            fab.setImageResource(android.R.color.transparent);
        }
    }

    /**
     * Updates button views, highlighting the selected color
     *
     * @param newPosition      the selected color position
     * @param previousPosition the previously selected color position
     */
    private void updateButtons(int newPosition, int previousPosition) {
        notifyItemChanged(newPosition);
        notifyItemChanged(previousPosition);
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

}
