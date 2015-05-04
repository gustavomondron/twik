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

package com.reddyetwo.hashmypass.app.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * Class to get/add/delete/update {@link com.reddyetwo.hashmypass.app.data.Tag} from storage
 */
public class TagSettings {

    public static final int ORDER_BY_HASH_COUNTER = 0;
    public static final int ORDER_BY_NAME = 1;

    public static final int LIMIT_UNBOUNDED = -1;

    private TagSettings() {

    }

    /**
     * Get tag settings from database
     *
     * @param context   The application context
     * @param profileID The profile ID
     * @param name      The tag name
     * @return the tag settings, or a tag with NO_ID and the profile default
     * settings if not found
     */
    public static Tag getTag(Context context, long profileID, String name) {
        DataOpenHelper helper = new DataOpenHelper(context);
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.query(DataOpenHelper.TAGS_TABLE_NAME,
                new String[]{DataOpenHelper.COLUMN_ID, DataOpenHelper.COLUMN_TAGS_SITE,
                        DataOpenHelper.COLUMN_TAGS_HASH_COUNTER,
                        DataOpenHelper.COLUMN_TAGS_PASSWORD_LENGTH,
                        DataOpenHelper.COLUMN_TAGS_PASSWORD_TYPE},
                DataOpenHelper.COLUMN_TAGS_PROFILE_ID + "=" +
                        profileID + " AND " + DataOpenHelper.COLUMN_TAGS_NAME +
                        " = ?", new String[]{name}, null, null, null);

        Tag tag = null;
        if (cursor.moveToFirst()) {
            // Specific tag settings found
            tag = new Tag(cursor.getLong(cursor.getColumnIndex(DataOpenHelper.COLUMN_ID)),
                    profileID,
                    cursor.getInt(cursor.getColumnIndex(DataOpenHelper.COLUMN_TAGS_HASH_COUNTER)),
                    cursor.getString(cursor.getColumnIndex(DataOpenHelper.COLUMN_TAGS_SITE)), name,
                    cursor.getInt(
                            cursor.getColumnIndex(DataOpenHelper.COLUMN_TAGS_PASSWORD_LENGTH)),
                    PasswordType.values()[cursor.getInt(cursor
                            .getColumnIndex(DataOpenHelper.COLUMN_TAGS_PASSWORD_TYPE))]);
        } else {
            // Tag settings not found, use profile settings
            Profile profile = ProfileSettings.getProfile(context, profileID);
            if (profile != null) {
                tag = new Tag(Tag.NO_ID, profileID, 0, null, name, profile.getPasswordLength(),
                        profile.getPasswordType());
            }
        }

        cursor.close();
        db.close();
        return tag;
    }

    /**
     * Get tag settings from database
     *
     * @param context The application context
     * @param tagId   the tag ID
     * @return the tag settings, or null if not found
     */
    public static Tag getTag(Context context, long tagId) {
        DataOpenHelper helper = new DataOpenHelper(context);
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.query(DataOpenHelper.TAGS_TABLE_NAME,
                new String[]{DataOpenHelper.COLUMN_TAGS_PROFILE_ID, DataOpenHelper.COLUMN_TAGS_NAME,
                        DataOpenHelper.COLUMN_TAGS_HASH_COUNTER, DataOpenHelper.COLUMN_TAGS_SITE,
                        DataOpenHelper.COLUMN_TAGS_PASSWORD_LENGTH,
                        DataOpenHelper.COLUMN_TAGS_PASSWORD_TYPE}, DataOpenHelper.COLUMN_ID + "=" +
                        tagId, null, null, null, null);

        Tag tag = null;
        if (cursor.moveToFirst()) {
            // Specific tag settings found
            tag = new Tag(tagId,
                    cursor.getLong(cursor.getColumnIndex(DataOpenHelper.COLUMN_TAGS_PROFILE_ID)),
                    cursor.getInt(cursor.getColumnIndex(DataOpenHelper.COLUMN_TAGS_HASH_COUNTER)),
                    cursor.getString(cursor.getColumnIndex(DataOpenHelper.COLUMN_TAGS_SITE)),
                    cursor.getString(cursor.getColumnIndex(DataOpenHelper.COLUMN_TAGS_NAME)),
                    cursor.getInt(
                            cursor.getColumnIndex(DataOpenHelper.COLUMN_TAGS_PASSWORD_LENGTH)),
                    PasswordType.values()[cursor.getInt(cursor
                            .getColumnIndex(DataOpenHelper.COLUMN_TAGS_PASSWORD_TYPE))]);
        }

        cursor.close();
        db.close();
        return tag;
    }

    /**
     * Insert a tag in the database
     *
     * @param context The application context
     * @param tag     The tag
     * @return the ID of the new tag, or -1 if an error occurred
     */
    public static long insertTag(Context context, Tag tag) {
        DataOpenHelper helper = new DataOpenHelper(context);
        SQLiteDatabase db = helper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DataOpenHelper.COLUMN_TAGS_NAME, tag.getName());
        values.put(DataOpenHelper.COLUMN_TAGS_PROFILE_ID, tag.getProfileId());
        values.put(DataOpenHelper.COLUMN_TAGS_HASH_COUNTER, tag.getHashCounter());
        values.put(DataOpenHelper.COLUMN_TAGS_SITE, tag.getSite());
        values.put(DataOpenHelper.COLUMN_TAGS_PASSWORD_LENGTH, tag.getPasswordLength());
        values.put(DataOpenHelper.COLUMN_TAGS_PASSWORD_TYPE, tag.getPasswordType().ordinal());

        long id = db.insertOrThrow(DataOpenHelper.TAGS_TABLE_NAME, null, values);

        db.close();
        return id;
    }

    /**
     * Update a tag in the database
     *
     * @param context The application context
     * @param tag     The tag
     * @return true if success, false in case of error
     */
    public static boolean updateTag(Context context, Tag tag) {
        DataOpenHelper helper = new DataOpenHelper(context);
        SQLiteDatabase db = helper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DataOpenHelper.COLUMN_TAGS_NAME, tag.getName());
        values.put(DataOpenHelper.COLUMN_TAGS_PASSWORD_LENGTH, tag.getPasswordLength());
        values.put(DataOpenHelper.COLUMN_TAGS_PASSWORD_TYPE, tag.getPasswordType().ordinal());
        values.put(DataOpenHelper.COLUMN_TAGS_HASH_COUNTER, tag.getHashCounter());
        values.put(DataOpenHelper.COLUMN_TAGS_SITE, tag.getSite());

        boolean updated =
                db.update(DataOpenHelper.TAGS_TABLE_NAME, values, DataOpenHelper.COLUMN_ID + " = ?",
                        new String[]{Long.toString(tag.getId())}) > 0;

        db.close();
        return updated;
    }

    /**
     * Delete a tag in the database
     *
     * @param context the {@link android.content.Context} instance
     * @param tag     the tag
     * @return true if success, false in case of error
     */
    public static boolean deleteTag(Context context, Tag tag) {
        DataOpenHelper helper = new DataOpenHelper(context);
        SQLiteDatabase db = helper.getWritableDatabase();
        boolean deleted = db.delete(DataOpenHelper.TAGS_TABLE_NAME, "_id=" + tag.getId() + " AND " +
                DataOpenHelper.COLUMN_TAGS_PROFILE_ID +
                "=" + tag.getProfileId(), null) > 0;

        db.close();
        return deleted;
    }

    /**
     * Return an ordered list of tags of a profile
     *
     * @param context   The application context
     * @param profileId The profile ID
     * @param orderBy   The profiles ordering
     * @param limit     The maximum number of results
     * @return The list of tags
     */
    @SuppressWarnings("SameParameterValue")
    public static List<Tag> getProfileTags(Context context, long profileId, int orderBy,
                                           int limit) {
        DataOpenHelper helper = new DataOpenHelper(context);
        SQLiteDatabase db = helper.getReadableDatabase();

        String orderClause = "";
        switch (orderBy) {
            case ORDER_BY_HASH_COUNTER:
                orderClause = DataOpenHelper.COLUMN_TAGS_HASH_COUNTER + " " +
                        "DESC";
                break;
            case ORDER_BY_NAME:
                orderClause = DataOpenHelper.COLUMN_TAGS_NAME + " COLLATE " +
                        "NOCASE";
                break;
            default:
        }
        String limitClause = null;
        if (limit != LIMIT_UNBOUNDED) {
            limitClause = Integer.toString(limit);
        }

        Cursor cursor = db.query(DataOpenHelper.TAGS_TABLE_NAME,
                new String[]{DataOpenHelper.COLUMN_ID, DataOpenHelper.COLUMN_TAGS_HASH_COUNTER,
                        DataOpenHelper.COLUMN_TAGS_SITE, DataOpenHelper.COLUMN_TAGS_NAME,
                        DataOpenHelper.COLUMN_TAGS_PASSWORD_LENGTH,
                        DataOpenHelper.COLUMN_PROFILES_PASSWORD_TYPE},
                DataOpenHelper.COLUMN_TAGS_PROFILE_ID + "= ?",
                new String[]{Long.toString(profileId)}, null, null, orderClause, limitClause);

        List<Tag> tagList = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                long tagId = cursor.getLong(cursor.getColumnIndex(DataOpenHelper.COLUMN_ID));
                int hashCounter = cursor.getInt(
                        cursor.getColumnIndex(DataOpenHelper.COLUMN_TAGS_HASH_COUNTER));
                String site =
                        cursor.getString(cursor.getColumnIndex(DataOpenHelper.COLUMN_TAGS_SITE));
                String name =
                        cursor.getString(cursor.getColumnIndex(DataOpenHelper.COLUMN_TAGS_NAME));
                int passwordLength = cursor.getInt(
                        cursor.getColumnIndex(DataOpenHelper.COLUMN_TAGS_PASSWORD_LENGTH));
                PasswordType passwordType = PasswordType.values()[cursor
                        .getInt(cursor.getColumnIndex(DataOpenHelper.COLUMN_TAGS_PASSWORD_TYPE))];
                tagList.add(new Tag(tagId, profileId, hashCounter, site, name, passwordLength,
                        passwordType));
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return tagList;
    }

    /**
     * Get the tag for a site
     *
     * @param context   the the {@link android.content.Context} instance
     * @param profileId the profile ID
     * @param site      the site identifier
     * @return the tag
     */
    public static Tag getSiteTag(Context context, long profileId, String site) {
        DataOpenHelper helper = new DataOpenHelper(context);
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.query(DataOpenHelper.TAGS_TABLE_NAME,
                new String[]{DataOpenHelper.COLUMN_TAGS_NAME},
                DataOpenHelper.COLUMN_TAGS_PROFILE_ID + "=" + profileId + " " +
                        "AND " + DataOpenHelper.COLUMN_TAGS_SITE + " = ?", new String[]{site}, null,
                null, null);

        Tag tag = null;
        if (cursor.moveToFirst()) {
            String tagName =
                    cursor.getString(cursor.getColumnIndex(DataOpenHelper.COLUMN_TAGS_NAME));
            tag = getTag(context, profileId, tagName);
        }

        cursor.close();
        db.close();
        return tag;
    }

    /**
     * Return true if a site has tags
     *
     * @param context the {@link android.content.Context} instance
     * @param site    the site identifier
     * @return true if a site has tags
     */
    public static boolean siteHasTags(Context context, String site) {
        DataOpenHelper helper = new DataOpenHelper(context);
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor =
                db.query(DataOpenHelper.TAGS_TABLE_NAME, new String[]{DataOpenHelper.COLUMN_ID},
                        DataOpenHelper.COLUMN_TAGS_SITE + " = ?", new String[]{site}, null, null,
                        null);
        boolean hasTags = cursor.getCount() > 0;

        cursor.close();
        db.close();
        return hasTags;
    }

    /**
     * Return the position of a tag according to a sort criterion
     *
     * @param context   the {@link android.content.Context} instance
     * @param tagId     the tag ID
     * @param profileId the profile ID
     * @param orderBy   the order criterion
     * @param limit     the The maximum number of results
     * @return the position of the tag
     */
    @SuppressWarnings("SameParameterValue")
    public static int getTagPosition(Context context, long tagId, long profileId, int orderBy,
                                     int limit) {
        int position = 0;
        List<Tag> tags = getProfileTags(context, profileId, orderBy, limit);
        int size = tags.size();
        while (position < size && tags.get(position).getId() != tagId) {
            position++;
        }

        return position;
    }
}