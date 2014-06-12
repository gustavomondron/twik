package com.reddyetwo.hashmypass.app.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class TagSettings {

    /**
     * Gets tag settings from database
     *
     * @param context
     * @param profileID
     * @param name
     * @return the tag settings, or the profile default settings if not found
     */
    public static Tag getTag(Context context, long profileID, String name) {
        DataOpenHelper helper = new DataOpenHelper(context);
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.query(DataOpenHelper.TAGS_TABLE_NAME,
                new String[]{DataOpenHelper.COLUMN_ID,
                        DataOpenHelper.COLUMN_TAGS_SITE,
                        DataOpenHelper.COLUMN_TAGS_PASSWORD_LENGTH,
                        DataOpenHelper.COLUMN_TAGS_PASSWORD_TYPE},
                DataOpenHelper.COLUMN_TAGS_PROFILE_ID + "=" +
                        profileID + " AND " + DataOpenHelper.COLUMN_TAGS_NAME +
                        "=\"" + name + "\"", null, null, null, null
        );

        Tag tag = null;
        if (cursor.moveToFirst()) {
            // Specific tag settings found
            tag = new Tag(cursor
                    .getLong(cursor.getColumnIndex(DataOpenHelper.COLUMN_ID)),
                    profileID, cursor.getString(
                    cursor.getColumnIndex(DataOpenHelper.COLUMN_TAGS_SITE)),
                    name, cursor.getInt(cursor.getColumnIndex(
                            DataOpenHelper.COLUMN_TAGS_PASSWORD_LENGTH)
            ), PasswordType.values()[cursor.getInt(cursor
                    .getColumnIndex(DataOpenHelper.COLUMN_TAGS_PASSWORD_TYPE))]
            );
        } else {
            // Tag settings not found, use profile settings
            Profile profile = ProfileSettings.getProfile(context, profileID);
            if (profile != null) {
                tag = new Tag(Tag.NO_ID, profileID, null, name,
                        profile.getPasswordLength(), profile.getPasswordType());
            }
        }

        db.close();
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
        values.put(DataOpenHelper.COLUMN_TAGS_PASSWORD_LENGTH,
                tag.getPasswordLength());
        values.put(DataOpenHelper.COLUMN_TAGS_PASSWORD_TYPE,
                tag.getPasswordType().ordinal());

        return db.insertOrThrow(DataOpenHelper.TAGS_TABLE_NAME, null, values);
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
        values.put(DataOpenHelper.COLUMN_TAGS_PASSWORD_LENGTH,
                tag.getPasswordLength());
        values.put(DataOpenHelper.COLUMN_TAGS_PASSWORD_TYPE,
                tag.getPasswordType().ordinal());

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
                        "=" + tag.getProfileId(), null
        ) > 0;

        db.close();

        return deleted;
    }

    /**
     * Returns the list of tags of a profile
     *
     * @param context
     * @param profileId
     * @return
     */
    public static List<Tag> getProfileTags(Context context, long profileId) {
        DataOpenHelper helper = new DataOpenHelper(context);
        SQLiteDatabase db = helper.getReadableDatabase();

        Cursor cursor = db.query(DataOpenHelper.TAGS_TABLE_NAME,
                new String[]{DataOpenHelper.COLUMN_ID,
                        DataOpenHelper.COLUMN_TAGS_SITE,
                        DataOpenHelper.COLUMN_TAGS_NAME,
                        DataOpenHelper.COLUMN_TAGS_PASSWORD_LENGTH,
                        DataOpenHelper.COLUMN_PROFILES_PASSWORD_TYPE},
                DataOpenHelper.COLUMN_TAGS_PROFILE_ID + "= ?",
                new String[]{Long.toString(profileId)}, null, null, null
        );

        List<Tag> tagList = new ArrayList<Tag>();
        if (cursor.moveToFirst()) {
            do {
                long tagId = cursor.getLong(
                        cursor.getColumnIndex(DataOpenHelper.COLUMN_ID));
                String site = cursor.getString(
                        cursor.getColumnIndex(DataOpenHelper.COLUMN_TAGS_SITE));
                String name = cursor.getString(
                        cursor.getColumnIndex(DataOpenHelper.COLUMN_TAGS_NAME));
                int passwordLength = cursor.getInt(cursor.getColumnIndex(
                        DataOpenHelper.COLUMN_TAGS_PASSWORD_LENGTH));
                PasswordType passwordType = PasswordType.values()[cursor
                        .getInt(cursor.getColumnIndex(
                                DataOpenHelper.COLUMN_TAGS_PASSWORD_TYPE))];
                tagList.add(
                        new Tag(tagId, profileId, site, name, passwordLength,
                                passwordType)
                );
            } while (cursor.moveToNext());
        }

        return tagList;
    }

    public static Tag getSiteTag(Context context, long profileId, String site) {
        DataOpenHelper helper = new DataOpenHelper(context);
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.query(DataOpenHelper.TAGS_TABLE_NAME,
                new String[]{DataOpenHelper.COLUMN_TAGS_NAME},
                DataOpenHelper.COLUMN_TAGS_PROFILE_ID + "=" + profileId + " " +
                        "AND " + DataOpenHelper.COLUMN_TAGS_SITE + "=\"" +
                        site + "\"", null, null, null, null
        );

        Tag tag = null;
        if (cursor.moveToFirst()) {
            String tagName = cursor.getString(
                    cursor.getColumnIndex(DataOpenHelper.COLUMN_TAGS_NAME));
            tag = getTag(context, profileId, tagName);
        }

        db.close();

        return tag;
    }
}
