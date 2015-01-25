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
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.reddyetwo.hashmypass.app.R;
import com.reddyetwo.hashmypass.app.data.Tag;
import com.reddyetwo.hashmypass.app.data.TagSettings;
import com.reddyetwo.hashmypass.app.util.FaviconLoader;

import java.util.List;

public class TagListAdapter extends RecyclerView.Adapter<TagListViewHolder> {

    private static final int ITEM_RESOURCE = R.layout.tag_list_item;
    private long mProfileId;
    private List<Tag> mTags;
    private final Context mContext;
    private final OnTagClickedListener mTagClickedListener;

    public TagListAdapter(Context context, long profileId, OnTagClickedListener tagClickedListener,
                          List<Tag> objects) {
        super();
        mContext = context;
        mProfileId = profileId;
        mTagClickedListener = tagClickedListener;
        mTags = objects;
    }

    public interface OnTagClickedListener {
        public void onTagClicked(final Tag tag);

        public void onTagLongClicked(final Tag tag);

    }

    public void setProfileId(long profileId) {
        mProfileId = profileId;
    }

    public void setTags(List<Tag> tags) {
        mTags = tags;
        notifyDataSetChanged();
    }

    @Override
    public TagListViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(ITEM_RESOURCE, viewGroup, false);
        return new TagListViewHolder(v);
    }

    @Override
    public void onBindViewHolder(TagListViewHolder tagListViewHolder, int i) {
        final Tag tag = mTags.get(i);
        // Set tag favicon
        FaviconLoader.setAsBackground(mContext, tagListViewHolder.mFaviconTextView, tag);

        // Set tag name
        tagListViewHolder.mTagNameTextView.setText(tag.getName());

        // Set tag click listener
        tagListViewHolder.itemView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Increase hash counter. This may affect tag list.
                tag.setHashCounter(tag.getHashCounter() + 1);
                TagSettings.updateTag(mContext, tag);
                update(tag);

                // Update last used profile
                mTagClickedListener.onTagClicked(new Tag(tag));
            }
        });

        // Set tag long click listener
        tagListViewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mTagClickedListener.onTagLongClicked(tag);
                return false;
            }
        });
    }

    public void add(Tag tag, int position) {
        mTags.add(position, tag);
        notifyItemInserted(position);
    }

    public void remove(Tag tag) {
        int position = mTags.indexOf(tag);
        mTags.remove(position);
        notifyItemRemoved(position);
    }

    public void update(Tag tag) {
        int oldPosition = 0;
        int newPosition = TagSettings.getTagPosition(mContext, tag.getId(), mProfileId,
                TagSettings.ORDER_BY_HASH_COUNTER, TagSettings.LIMIT_UNBOUNDED);

        while (oldPosition < mTags.size() && mTags.get(oldPosition).getId() != tag.getId()) {
            oldPosition++;
        }
        if (oldPosition >= mTags.size()) {
            // The tag is not in the list because it's new and its custom settings were saved during its creation.
            mTags.add(newPosition, tag);
            notifyItemInserted(newPosition);
        }

        if (oldPosition != newPosition) {
            // Animate the change of position of this tag.
            // The tag is removed from its current position and added to the new one.
            mTags.remove(tag);
            mTags.add(newPosition, tag);
            notifyItemRemoved(oldPosition);
            notifyItemInserted(newPosition);
        } else {
            // Update the tag because the favicon and the name may have changed.
            mTags.set(oldPosition, tag);
            notifyItemChanged(oldPosition);
        }
    }

    public List<Tag> getTags() {
        return mTags;
    }

    @Override
    public int getItemCount() {
        return mTags.size();
    }
}
