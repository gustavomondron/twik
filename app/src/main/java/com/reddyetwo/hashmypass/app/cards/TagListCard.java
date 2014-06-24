package com.reddyetwo.hashmypass.app.cards;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.reddyetwo.hashmypass.app.R;
import com.reddyetwo.hashmypass.app.data.Favicon;
import com.reddyetwo.hashmypass.app.data.FaviconSettings;
import com.reddyetwo.hashmypass.app.data.Tag;
import com.reddyetwo.hashmypass.app.util.FaviconLoader;

import java.util.ArrayList;
import java.util.List;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardHeader;
import it.gmariotti.cardslib.library.prototypes.CardWithList;
import it.gmariotti.cardslib.library.prototypes.LinearListView;

public class TagListCard extends CardWithList {

    private List<Tag> mTags;
    private OnTagSelectedListener mSelectedTagListener;

    public TagListCard(Context context,
                       OnTagSelectedListener selectedTagListener) {
        super(context);
        mSelectedTagListener = selectedTagListener;
    }


    public TagListCard(Context context,
                       OnTagSelectedListener selectedTagListener,
                       List<Tag> tags) {
        this(context, selectedTagListener);
        mTags = tags;

    }

    @Override
    protected CardHeader initCardHeader() {
        CardHeader header = new CardHeader(getContext());
        header.setTitle(getContext().getString(R.string.tags_list));
        return header;
    }

    @Override
    protected void initCard() {
        setEmptyViewViewStubLayoutId(R.layout.card_tag_list_empty);
    }

    public void updateTags(List<Tag> tags) {
        getLinearListAdapter().clear();
        List<TagObject> list = new ArrayList<TagObject>();
        for (Tag tag : tags) {
            list.add(new TagObject(this, tag));
        }
        getLinearListAdapter().addAll(list);
    }

    public void clearTags() {
        getLinearListAdapter().clear();
    }

    public void addTag(Tag tag) {
        getLinearListAdapter().add(new TagObject(this, tag));
    }

    @Override
    protected List<ListObject> initChildren() {
        List<ListObject> objects = new ArrayList<ListObject>();
        if (mTags != null) {
            for (Tag tag : mTags) {
                objects.add(new TagObject(this, tag));
            }
        }
        return objects;
    }

    @Override
    public int getChildLayoutId() {
        return R.layout.card_tag_list_inner_main;
    }

    @Override
    public View setupChildView(int i, ListObject listObject, View view,
                               ViewGroup viewGroup) {
        TextView faviconTextView =
                (TextView) view.findViewById(R.id.tag_favicon);
        TextView tagName = (TextView) view.findViewById(R.id.tag_name);
        Tag tag = ((TagObject) listObject).getTag();
        tagName.setText(tag.getName());
        FaviconLoader.setAsBackground(getContext(), faviconTextView, tag);
        return view;
    }

    private class TagObject extends DefaultListObject {
        private Tag mTag;

        public TagObject(Card parentCard, Tag tag) {
            super(parentCard);
            mTag = tag;
            init();
        }

        public Tag getTag() {
            return mTag;
        }

        private void init() {
            setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(LinearListView linearListView,
                                        View view, int i,
                                        ListObject listObject) {
                    mSelectedTagListener
                            .onTagSelected(((TagObject) listObject).getTag(),
                                    true);
                }
            });
        }

    }
}
