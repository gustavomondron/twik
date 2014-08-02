package com.reddyetwo.hashmypass.app.cards;


import android.view.View;

import com.reddyetwo.hashmypass.app.data.Tag;

public interface OnTagSelectedListener {

    public void onTagSelected(Tag tag, boolean fromList, View view);
    public void onTagSettingsChanged(Tag tag);
}
