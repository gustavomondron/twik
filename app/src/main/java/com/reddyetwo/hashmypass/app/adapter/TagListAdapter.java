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
import android.support.annotation.LayoutRes;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.reddyetwo.hashmypass.app.R;
import com.reddyetwo.hashmypass.app.data.Tag;
import com.reddyetwo.hashmypass.app.data.TagSettings;
import com.reddyetwo.hashmypass.app.util.FaviconLoader;

import java.util.List;

/**
 * Adapter for the tag list shown in {@link com.reddyetwo.hashmypass.app.MainActivity}
 */
public class TagListAdapter extends RecyclerView.Adapter<TagListViewHolder> {

    /**
     * Layout of each item in the list of tags
     */
    @LayoutRes
    private static final int ITEM_RESOURCE = R.layout.tag_list_item;

    /**
     * Application context
     */
    private final Context mContext;

    /**
     * Listener that reacts to tag click events
     */
    private final OnTagClickedListener mTagClickedListener;

    /**
     * ID of the selected profile
     */
    private long mProfileId;

    /**
     * List of tags
     */
    private List<Tag> mTags;

    /**
     * Order of the tags (e.g., by name, by usage)
     */
    private int mTagOrder;

    /**
     * Constructor
     *
     * @param context            the application context
     * @param profileId          the profile ID
     * @param tagOrder           the tag order
     * @param tagClickedListener listener for tag clicked event
     * @param objects            list of tags
     */
    public TagListAdapter(Context context, long profileId, int tagOrder,
                          OnTagClickedListener tagClickedListener, List<Tag> objects) {
        super();
        mContext = context;
        mProfileId = profileId;
        mTagOrder = tagOrder;
        mTagClickedListener = tagClickedListener;
        mTags = objects;
    }

    /**
     * Set the selected profile ID
     *
     * @param profileId the profile ID
     */
    public void setProfileId(long profileId) {
        mProfileId = profileId;
    }

    /**
     * Set the selected tag order
     *
     * @param tagOrder the tag order
     */
    public void setTagOrder(int tagOrder) {
        mTagOrder = tagOrder;
    }

    @Override
    public TagListViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        // Create a view that can represent a tag in the list
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(ITEM_RESOURCE, viewGroup, false);
        return new TagListViewHolder(v);
    }

    @Override
    public void onBindViewHolder(TagListViewHolder tagListViewHolder, int i) {
        final Tag tag = mTags.get(i);

        // Set tag favicon
        FaviconLoader.setAsBackground(mContext, tagListViewHolder.getFaviconTextView(), tag);

        // Set tag name
        tagListViewHolder.getTagNameTextView().setText(tag.getName());

        // Set tag click listener
        tagListViewHolder.itemView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Increase hash counter. Note that this may affect the order of the tags in the
                // case that they are ordered by usage.
                tag.setHashCounter(tag.getHashCounter() + 1);
                TagSettings.updateTag(mContext, tag);
                if (mTagOrder == TagSettings.ORDER_BY_HASH_COUNTER) {
                    update(tag);
                }

                // Notify to the tag click listener
                mTagClickedListener.onTagClicked(new Tag(tag));
            }
        });

        // Add listener for tag long click event, which notifies to the tag click listener.
        tagListViewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mTagClickedListener.onTagLongClicked(tag);
                return false;
            }
        });
    }

    /**
     * Add a tag to the list
     *
     * @param tag      the tag
     * @param position the position in the list
     */
    public void add(Tag tag, int position) {
        mTags.add(position, tag);
        notifyItemInserted(position);
    }

    /**
     * Remove a tag from the list
     *
     * @param tag the tag to remove
     */
    public void remove(Tag tag) {
        int position = mTags.indexOf(tag);
        mTags.remove(position);
        notifyItemRemoved(position);
    }

    /**
     * Updates the list adding or modifying a tag, updating its position according the the sort criterion.
     *
     * @param tag the tag to add or modify
     */
    public void update(Tag tag) {
        // It is necessary to compare the previous position in the list of the tag with the new
        // position, updating the list if it has changed.
        int oldPosition = 0;
        int newPosition = TagSettings.getTagPosition(mContext, tag.getId(), mProfileId, mTagOrder,
                TagSettings.LIMIT_UNBOUNDED);

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
            mTags.remove(oldPosition);
            mTags.add(newPosition, tag);
            notifyItemRemoved(oldPosition);
            notifyItemInserted(newPosition);
        } else {
            // Update the tag because its name (and therefore its favicon) may have changed.
            mTags.set(oldPosition, tag);
            notifyItemChanged(oldPosition);
        }
    }

    /**
     * Get the list of tags
     *
     * @return the @{@link java.util.List} of tags
     */
    public List<Tag> getTags() {
        return mTags;
    }

    /**
     * Set the list of tags
     *
     * @param tags the list of tags
     */
    public void setTags(List<Tag> tags) {
        mTags = tags;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mTags.size();
    }

    /**
     * Interface which can be implemented to handler tag clicked events
     */
    public interface OnTagClickedListener {

        /**
         * Method called when a tag is clicked
         *
         * @param tag the {@link com.reddyetwo.hashmypass.app.data.Tag} instance
         */
        public void onTagClicked(final Tag tag);

        /**
         * Method called when a tag is long-clicked
         *
         * @param tag the the {@link com.reddyetwo.hashmypass.app.data.Tag} instance
         */
        public void onTagLongClicked(final Tag tag);
    }
}
