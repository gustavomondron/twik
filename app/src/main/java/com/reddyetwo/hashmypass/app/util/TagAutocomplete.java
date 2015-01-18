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


package com.reddyetwo.hashmypass.app.util;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import com.reddyetwo.hashmypass.app.data.Tag;
import com.reddyetwo.hashmypass.app.data.TagSettings;

import java.util.ArrayList;
import java.util.List;

public class TagAutocomplete {

    public static void populateTagAutocompleteTextView(Context context,
                                                       long profileId,
                                                       AutoCompleteTextView tagTextView) {

        List<Tag> tags = TagSettings.getProfileTags(context, profileId,
                TagSettings.ORDER_BY_HASH_COUNTER, TagSettings.LIMIT_UNBOUNDED);
        List<String> names = new ArrayList<>();
        for (Tag tag : tags) {
            names.add(tag.getName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(context,
                android.R.layout.simple_list_item_1, names);

        tagTextView.setAdapter(adapter);
    }
}
