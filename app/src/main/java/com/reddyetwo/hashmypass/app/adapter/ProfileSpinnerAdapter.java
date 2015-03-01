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

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Typeface;
import android.support.annotation.LayoutRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.reddyetwo.hashmypass.app.data.Profile;

import java.util.List;

/**
 * Adapter for spinners containing the list of {@link com.reddyetwo.hashmypass.app.data.Profile}
 */
public class ProfileSpinnerAdapter implements SpinnerAdapter {

    private static final String TAG_VIEW_DROPDOWN = "SpinnerDropdown";
    private static final String TAG_VIEW_NOT_DROPDOWN = "SpinnerNotDropdown";

    /**
     * Themed context
     */
    private final Context mThemedContext;

    /**
     * List of profiles
     */
    private final List<Profile> mProfiles;

    /**
     * Dropdown item view
     */
    @LayoutRes
    private int mViewItemDropdown = android.R.layout.simple_spinner_dropdown_item;

    /**
     * Not-dropdown item view
     */
    @LayoutRes
    private int mViewItemNotDropdown = android.R.layout.simple_spinner_item;

    /**
     * Constructor
     *
     * @param themedContext the themed {@link android.content.Context} instance
     * @param profiles      the {@link java.util.List} of profiles
     */
    public ProfileSpinnerAdapter(Context themedContext, List<Profile> profiles) {
        mThemedContext = themedContext;
        mProfiles = profiles;

    }

    /**
     * Constructor
     *
     * @param themedContext   the themed {@link android.content.Context} instance
     * @param profiles        the {@link java.util.List} of profiles
     * @param itemDropdown    the item drop-down {@link android.support.annotation.LayoutRes}
     * @param itemNotDropdown the item not drop-down {@link android.support.annotation.LayoutRes}
     */
    @SuppressWarnings("SameParameterValue")
    public ProfileSpinnerAdapter(Context themedContext, List<Profile> profiles,
                                 @LayoutRes int itemDropdown, @LayoutRes int itemNotDropdown) {
        this(themedContext, profiles);
        mViewItemDropdown = itemDropdown;
        mViewItemNotDropdown = itemNotDropdown;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        TextView profileNameTextView;
        if (convertView == null || convertView.getTag() == null ||
                !convertView.getTag().equals(TAG_VIEW_DROPDOWN)) {
            profileNameTextView = (TextView) LayoutInflater.from(mThemedContext)
                    .inflate(mViewItemDropdown, parent, false);
            profileNameTextView.setTag(TAG_VIEW_DROPDOWN);
        } else {
            profileNameTextView = (TextView) convertView;
        }

        profileNameTextView.setText(mProfiles.get(position).getName());

        if (mProfiles.get(position).getId() == Profile.NO_ID) {
            // Set to italic if this is the last option in the dropdown
            profileNameTextView.setTypeface(null, Typeface.ITALIC);
        } else {
            // Explicitly set to normal, in case convertView was in italics
            profileNameTextView.setTypeface(null, Typeface.NORMAL);
        }

        return profileNameTextView;
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
        // Nothing to do
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
        // Nothing to do
    }

    @Override
    public int getCount() {
        return mProfiles.size();
    }

    @Override
    public Object getItem(int position) {
        return mProfiles.get(position);
    }

    @Override
    public long getItemId(int position) {
        return mProfiles.get(position).getId();
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView profileNameTextView;
        if (convertView == null || convertView.getTag() == null ||
                !convertView.getTag().equals(TAG_VIEW_NOT_DROPDOWN)) {
            profileNameTextView = (TextView) LayoutInflater.from(mThemedContext)
                    .inflate(mViewItemNotDropdown, parent, false);
            profileNameTextView.setTag(TAG_VIEW_NOT_DROPDOWN);
        } else {
            profileNameTextView = (TextView) convertView;
        }

        profileNameTextView.setText(mProfiles.get(position).getName());
        return profileNameTextView;
    }

    @Override
    public int getItemViewType(int position) {
        return IGNORE_ITEM_VIEW_TYPE;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return mProfiles.isEmpty();
    }
}
