/*
 * Copyright 2014 Red Dye No. 2
 *
 * This file is part of Hash My pass.
 *
 * Hash my pass is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Hash my pass is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Hash my pass.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.reddyetwo.hashmypass.app;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.reddyetwo.hashmypass.app.data.Profile;

import java.util.List;

/**
 * Adapter for showing {@link Profile} instances in a dropdown navigation view.
 */
class ProfileAdapter extends ArrayAdapter<Profile> {

    private List<Profile> mProfiles;
    private static final int mResource =
            R.layout.actionbar_simple_spinner_dropdown_item;

    public ProfileAdapter(Context context, List<Profile> objects) {
        super(context, mResource, objects);
        mProfiles = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(mResource, parent, false);
        }

        ((TextView) convertView).setText(mProfiles.get(position).getName());

        if (position == getCount() - 1) {
            // Set to italic if this is the last option in the dropdown
            ((TextView) convertView).setTypeface(null, Typeface.ITALIC);
        } else {
            // Explicitly set to normal, in case convertView was in italics
            ((TextView) convertView).setTypeface(null, Typeface.NORMAL);
        }

        return convertView;
    }

    @Override
    public View getDropDownView(int position, View convertView,
                                ViewGroup parent) {
        return getView(position, convertView, parent);
    }

}
