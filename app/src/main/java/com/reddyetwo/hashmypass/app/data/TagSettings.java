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

public class TagSettings {

    public static final int ORDER_BY_NAME = 1;
    public static final int ORDER_BY_HASH_COUNTER = 2;
    public static final int LIMIT_UNBOUNDED = -1;

    /**
     * Gets tag settings from database
     *
     * @param context
     * @param profileID
     * @param name
     * @return the tag settings, or a tag with NO_ID and the profile default
     * settings if not found
     */
    public static Tag getTag(Context context, long profileID, String name) {
        DataOpenHelper helper = new DataOpenHelper(context);
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.query(DataOpenHelper.TAGS_TABLE_NAME,
                new String[]{DataOpenHelper.COLUMN_ID,
                        DataOpenHelper.COLUMN_TAGS_SITE,
                        DataOpenHelper.COLUMN_TAGS_HASH_COUNTER,
                        DataOpenHelper.COLUMN_TAGS_PASSWORD_LENGTH,
                        DataOpenHelper.COLUMN_TAGS_PASSWORD_TYPE},
                DataOpenHelper.COLUMN_TAGS_PROFILE_ID + "=" +
                        profileID + " AND " + DataOpenHelper.COLUMN_TAGS_NAME +
                        " = ?", new String[]{name}, null, null, null);

        Tag tag = null;
        if (cursor.moveToFirst()) {
            // Specific tag settings found
            tag = new Tag(cursor
                    .getLong(cursor.getColumnIndex(DataOpenHelper.COLUMN_ID)),
                    profileID, cursor.getInt(cursor.getColumnIndex(
                    DataOpenHelper.COLUMN_TAGS_HASH_COUNTER)), cursor.getString(
                    cursor.getColumnIndex(DataOpenHelper.COLUMN_TAGS_SITE)),
                    name, cursor.getInt(cursor.getColumnIndex(
                            DataOpenHelper.COLUMN_TAGS_PASSWORD_LENGTH)),
                    PasswordType.values()[cursor.getInt(cursor.getColumnIndex(
                            DataOpenHelper.COLUMN_TAGS_PASSWORD_TYPE))]);
        } else {
            // Tag settings not found, use profile settings
            Profile profile = ProfileSettings.getProfile(context, profileID);
            if (profile != null) {
                tag = new Tag(Tag.NO_ID, profileID, 0, null, name,
                        profile.getPasswordLength(), profile.getPasswordType());
            }
        }

        db.close();
        return tag;
    }

    /**
     * Gets tag settings from database
     *
     * @param context
     * @param tagId   the tag identifier
     * @return the tag settings, or null if not found
     */
    public static Tag getTag(Context context, long tagId) {
        DataOpenHelper helper = new DataOpenHelper(context);
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.query(DataOpenHelper.TAGS_TABLE_NAME,
                new String[]{DataOpenHelper.COLUMN_TAGS_PROFILE_ID,
                        DataOpenHelper.COLUMN_TAGS_NAME,
                        DataOpenHelper.COLUMN_TAGS_HASH_COUNTER,
                        DataOpenHelper.COLUMN_TAGS_SITE,
                        DataOpenHelper.COLUMN_TAGS_PASSWORD_LENGTH,
                        DataOpenHelper.COLUMN_TAGS_PASSWORD_TYPE},
                DataOpenHelper.COLUMN_ID + "=" +
                        tagId, null, null, null, null);

        Tag tag = null;
        if (cursor.moveToFirst()) {
            // Specific tag settings found
            tag = new Tag(tagId, cursor.getLong(cursor.getColumnIndex(
                    DataOpenHelper.COLUMN_TAGS_PROFILE_ID)), cursor.getInt(
                    cursor.getColumnIndex(
                            DataOpenHelper.COLUMN_TAGS_HASH_COUNTER)),
                    cursor.getString(cursor.getColumnIndex(
                            DataOpenHelper.COLUMN_TAGS_SITE)), cursor.getString(
                    cursor.getColumnIndex(DataOpenHelper.COLUMN_TAGS_NAME)),
                    cursor.getInt(cursor.getColumnIndex(
                                    DataOpenHelper.COLUMN_TAGS_PASSWORD_LENGTH)),
                    PasswordType.values()[cursor.getInt(cursor.getColumnIndex(
                            DataOpenHelper.COLUMN_TAGS_PASSWORD_TYPE))]);
        }
        return tag;
    }

    /**
     * Inserts a tag in the database
     *
     * @param context
     * @param tag
     * @return the ID of the new tag, or -1 if an error occurred
     */
    public static long insertTag(Context context, Tag tag) {
        DataOpenHelper helper = new DataOpenHelper(context);
        SQLiteDatabase db = helper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DataOpenHelper.COLUMN_TAGS_NAME, tag.getName());
        values.put(DataOpenHelper.COLUMN_TAGS_PROFILE_ID, tag.getProfileId());
        values.put(DataOpenHelper.COLUMN_TAGS_HASH_COUNTER,
                tag.getHashCounter());
        values.put(DataOpenHelper.COLUMN_TAGS_SITE, tag.getSite());
        values.put(DataOpenHelper.COLUMN_TAGS_PASSWORD_LENGTH,
                tag.getPasswordLength());
        values.put(DataOpenHelper.COLUMN_TAGS_PASSWORD_TYPE,
                tag.getPasswordType().ordinal());

        long id =
                db.insertOrThrow(DataOpenHelper.TAGS_TABLE_NAME, null, values);

        db.close();
        return id;
    }

    /**
     * Updates a tag in the database
     *
     * @param context
     * @param tag
     * @return true if success, false in case of error
     */
    public static boolean updateTag(Context context, Tag tag) {
        DataOpenHelper helper = new DataOpenHelper(context);
        SQLiteDatabase db = helper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DataOpenHelper.COLUMN_TAGS_NAME, tag.getName());
        values.put(DataOpenHelper.COLUMN_TAGS_PASSWORD_LENGTH,
                tag.getPasswordLength());
        values.put(DataOpenHelper.COLUMN_TAGS_PASSWORD_TYPE,
                tag.getPasswordType().ordinal());
        values.put(DataOpenHelper.COLUMN_TAGS_HASH_COUNTER,
                tag.getHashCounter());
        values.put(DataOpenHelper.COLUMN_TAGS_SITE, tag.getSite());

        boolean updated = db.update(DataOpenHelper.TAGS_TABLE_NAME, values,
                DataOpenHelper.COLUMN_ID + " = ?",
                new String[]{Long.toString(tag.getId())}) > 0;

        db.close();
        return updated;
    }

    public static boolean deleteTag(Context context, Tag tag) {
        DataOpenHelper helper = new DataOpenHelper(context);
        SQLiteDatabase db = helper.getWritableDatabase();
        boolean deleted = db.delete(DataOpenHelper.TAGS_TABLE_NAME,
                "_id=" + tag.getId() + " AND " +
                        DataOpenHelper.COLUMN_TAGS_PROFILE_ID +
                        "=" + tag.getProfileId(), null) > 0;

        db.close();
        return deleted;
    }

    /**
     * Returns an ordered list of tags of a profile
     *
     * @param context
     * @param profileId
     * @param orderBy
     * @return
     */
    public static List<Tag> getProfileTags(Context context, long profileId,
                                           int orderBy, int limit) {
        DataOpenHelper helper = new DataOpenHelper(context);
        SQLiteDatabase db = helper.getReadableDatabase();

        String orderClause = "";
        switch (orderBy) {
            case ORDER_BY_HASH_COUNTER:
                orderClause = DataOpenHelper.COLUMN_TAGS_HASH_COUNTER + " " +
                        "DESC, ";
            case ORDER_BY_NAME:
            default:
                orderClause += DataOpenHelper.COLUMN_TAGS_NAME + " COLLATE " +
                        "NOCASE";
        }
        String limitClause = null;
        if (limit != LIMIT_UNBOUNDED) {
            limitClause = Integer.toString(limit);
        }

        Cursor cursor = db.query(DataOpenHelper.TAGS_TABLE_NAME,
                new String[]{DataOpenHelper.COLUMN_ID,
                        DataOpenHelper.COLUMN_TAGS_HASH_COUNTER,
                        DataOpenHelper.COLUMN_TAGS_SITE,
                        DataOpenHelper.COLUMN_TAGS_NAME,
                        DataOpenHelper.COLUMN_TAGS_PASSWORD_LENGTH,
                        DataOpenHelper.COLUMN_PROFILES_PASSWORD_TYPE},
                DataOpenHelper.COLUMN_TAGS_PROFILE_ID + "= ?",
                new String[]{Long.toString(profileId)}, null, null, orderClause,
                limitClause);

        List<Tag> tagList = new ArrayList<Tag>();
        if (cursor.moveToFirst()) {
            do {
                long tagId = cursor.getLong(
                        cursor.getColumnIndex(DataOpenHelper.COLUMN_ID));
                int hashCounter = cursor.getInt(cursor.getColumnIndex(
                        DataOpenHelper.COLUMN_TAGS_HASH_COUNTER));
                String site = cursor.getString(
                        cursor.getColumnIndex(DataOpenHelper.COLUMN_TAGS_SITE));
                String name = cursor.getString(
                        cursor.getColumnIndex(DataOpenHelper.COLUMN_TAGS_NAME));
                int passwordLength = cursor.getInt(cursor.getColumnIndex(
                        DataOpenHelper.COLUMN_TAGS_PASSWORD_LENGTH));
                PasswordType passwordType = PasswordType.values()[cursor
                        .getInt(cursor.getColumnIndex(
                                DataOpenHelper.COLUMN_TAGS_PASSWORD_TYPE))];
                tagList.add(new Tag(tagId, profileId, hashCounter, site, name,
                                passwordLength, passwordType));
            } while (cursor.moveToNext());
        }

        db.close();
        return tagList;
    }

    public static Tag getSiteTag(Context context, long profileId, String site) {
        DataOpenHelper helper = new DataOpenHelper(context);
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.query(DataOpenHelper.TAGS_TABLE_NAME,
                new String[]{DataOpenHelper.COLUMN_TAGS_NAME},
                DataOpenHelper.COLUMN_TAGS_PROFILE_ID + "=" + profileId + " " +
                        "AND " + DataOpenHelper.COLUMN_TAGS_SITE + " = ?",
                new String[]{site}, null, null, null);

        Tag tag = null;
        if (cursor.moveToFirst()) {
            String tagName = cursor.getString(
                    cursor.getColumnIndex(DataOpenHelper.COLUMN_TAGS_NAME));
            tag = getTag(context, profileId, tagName);
        }

        db.close();
        return tag;
    }

    public static boolean siteHasTags(Context context, String site) {
        DataOpenHelper helper = new DataOpenHelper(context);
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.query(DataOpenHelper.TAGS_TABLE_NAME,
                new String[]{DataOpenHelper.COLUMN_ID},
                DataOpenHelper.COLUMN_TAGS_SITE + " = ?", new String[]{site},
                null, null, null);
        return cursor.getCount() > 0;
    }
}
