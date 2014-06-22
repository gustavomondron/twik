package com.reddyetwo.hashmypass.app.cards;


import com.reddyetwo.hashmypass.app.data.Tag;

public interface OnTagSelectedListener {

    public void onTagSelected(Tag tag, boolean fromList);
    public void onTagSettingsChanged(Tag tag);
}
