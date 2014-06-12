package com.reddyetwo.hashmypass.app.util;

import android.content.Context;
import android.database.Cursor;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import com.reddyetwo.hashmypass.app.data.DataOpenHelper;
import com.reddyetwo.hashmypass.app.data.Tag;
import com.reddyetwo.hashmypass.app.data.TagSettings;

import java.util.ArrayList;
import java.util.List;

public class TagAutocomplete {

    public static void populateTagAutocompleteTextView(Context context,
                                                       long profileId,
                                                       AutoCompleteTextView tagTextView) {

        List<Tag> tags = TagSettings.getProfileTags(context, profileId);
        List<String> names = new ArrayList<String>();
        for (Tag tag : tags) {
            names.add(tag.getName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,
                android.R.layout.simple_list_item_1, names);

        tagTextView.setAdapter(adapter);
    }
}
