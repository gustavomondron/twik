package com.reddyetwo.hashmypass.app.util;

import android.content.Context;
import android.database.Cursor;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import com.reddyetwo.hashmypass.app.data.DataOpenHelper;
import com.reddyetwo.hashmypass.app.data.TagSettings;

import java.util.ArrayList;
import java.util.List;

public class TagAutocomplete {

    public static void populateTagAutocompleteTextView(Context context,
                                                       long profileId,
                                                       AutoCompleteTextView tagTextView) {
        Cursor cursor = TagSettings.getTagsForProfile(context, profileId);
        if (!cursor.moveToFirst()) {
            return;
        }

        int column = cursor.getColumnIndex(DataOpenHelper.COLUMN_TAGS_NAME);

        List<String> tags = new ArrayList<String>(cursor.getCount());
        tags.add(cursor.getString(column));
        while (cursor.moveToNext()) {
            tags.add(cursor.getString(column));
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,
                android.R.layout.simple_list_item_1,
                tags.toArray(new String[tags.size()]));

        tagTextView.setAdapter(adapter);
    }
}
