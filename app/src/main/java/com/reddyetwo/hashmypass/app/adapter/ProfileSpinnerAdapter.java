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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.reddyetwo.hashmypass.app.R;
import com.reddyetwo.hashmypass.app.data.Profile;

import java.util.List;

public class ProfileSpinnerAdapter implements SpinnerAdapter {

    private static final String TAG_TOOLBAR_SPINNER_ITEM_DROPDOWN = "SpinnerDropdown";
    private static final String TAG_TOOLBAR_SPINNER_ITEM_ACTIONBAR = "SpinnerNotDropdown";

    private final Context mThemedContext;
    private final List<Profile> mProfiles;

    public ProfileSpinnerAdapter(Context themedContext, List<Profile> profiles) {
        mThemedContext = themedContext;
        mProfiles = profiles;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        TextView profileNameTextView;
        if (convertView == null || convertView.getTag() == null ||
                !convertView.getTag().equals(TAG_TOOLBAR_SPINNER_ITEM_DROPDOWN)) {
            profileNameTextView = (TextView) LayoutInflater.from(mThemedContext)
                    .inflate(R.layout.toolbar_spinner_item_dropdown, parent, false);
            profileNameTextView.setTag(TAG_TOOLBAR_SPINNER_ITEM_DROPDOWN);
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
                !convertView.getTag().equals(TAG_TOOLBAR_SPINNER_ITEM_ACTIONBAR)) {
            profileNameTextView = (TextView) LayoutInflater.from(mThemedContext)
                    .inflate(R.layout.toolbar_spinner_item_actionbar, parent, false);
            profileNameTextView.setTag(TAG_TOOLBAR_SPINNER_ITEM_ACTIONBAR);
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
