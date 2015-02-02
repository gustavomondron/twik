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
import android.view.View;
import android.widget.TextView;

import com.reddyetwo.hashmypass.app.R;

public class TagListViewHolder extends RecyclerView.ViewHolder {

    private final TextView mFaviconTextView;
    private final TextView mTagNameTextView;

    public TagListViewHolder(View itemView) {
        super(itemView);
        mFaviconTextView = (TextView) itemView.findViewById(R.id.tag_favicon);
        mTagNameTextView = (TextView) itemView.findViewById(R.id.tag_name);
    }

    public TextView getFaviconTextView() {
        return mFaviconTextView;
    }

    public TextView getTagNameTextView() {
        return mTagNameTextView;
    }
}
