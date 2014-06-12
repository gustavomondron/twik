package com.reddyetwo.hashmypass.app.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class ProfileSettings {

    public static Profile getProfile(Context context, long profileId) {
        DataOpenHelper helper = new DataOpenHelper(context);
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.query(DataOpenHelper.PROFILES_TABLE_NAME,
                new String[]{DataOpenHelper.COLUMN_PROFILES_NAME,
                        DataOpenHelper.COLUMN_PROFILES_PRIVATE_KEY,
                        DataOpenHelper.COLUMN_PROFILES_PASSWORD_LENGTH,
                        DataOpenHelper.COLUMN_PROFILES_PASSWORD_TYPE},
                DataOpenHelper.COLUMN_ID + "=" + profileId, null, null, null,
                null
        );

        Profile profile = null;
        if (cursor.moveToFirst()) {
            // Populate profile
            String name = cursor.getString(
                    cursor.getColumnIndex(DataOpenHelper.COLUMN_PROFILES_NAME));
            String privateKey = cursor.getString(cursor.getColumnIndex(
                    DataOpenHelper.COLUMN_PROFILES_PRIVATE_KEY));
            int passwordLength = cursor.getInt(cursor.getColumnIndex(
                    DataOpenHelper.COLUMN_PROFILES_PASSWORD_LENGTH));
            PasswordType passwordType = PasswordType.values()[cursor
                    .getInt(cursor.getColumnIndex(
                            DataOpenHelper.COLUMN_PROFILES_PASSWORD_TYPE))];
            profile = new Profile(profileId, name, privateKey, passwordLength,
                    passwordType);
        }

        db.close();

        return profile;
    }

    /**
     * Inserts a profile in the database
     *
     * @param context
     * @param profile
     * @return the ID of the inserted row, or -1 if an error occurred
     */
    public static long insertProfile(Context context, Profile profile) {
        DataOpenHelper helper = new DataOpenHelper(context);
        SQLiteDatabase db = helper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DataOpenHelper.COLUMN_PROFILES_NAME, profile.getName());
        values.put(DataOpenHelper.COLUMN_PROFILES_PRIVATE_KEY,
                profile.getPrivateKey());
        values.put(DataOpenHelper.COLUMN_PROFILES_PASSWORD_LENGTH,
                profile.getPasswordLength());
        values.put(DataOpenHelper.COLUMN_PROFILES_PASSWORD_TYPE,
                profile.getPasswordType().ordinal());

        long id = db.insertOrThrow(DataOpenHelper.PROFILES_TABLE_NAME, null,
                values);

        db.close();
        return id;
    }

    /**
     * Updates a profile in the database
     *
     * @param context
     * @param profile
     * @return true in case of success, false if an error occurred
     */
    public static boolean updateProfile(Context context, Profile profile) {
        DataOpenHelper helper = new DataOpenHelper(context);
        SQLiteDatabase db = helper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DataOpenHelper.COLUMN_PROFILES_NAME, profile.getName());
        values.put(DataOpenHelper.COLUMN_PROFILES_PRIVATE_KEY,
                profile.getPrivateKey());
        values.put(DataOpenHelper.COLUMN_PROFILES_PASSWORD_LENGTH,
                profile.getPasswordLength());
        values.put(DataOpenHelper.COLUMN_PROFILES_PASSWORD_TYPE,
                profile.getPasswordType().ordinal());

        boolean updated = db.update(DataOpenHelper.PROFILES_TABLE_NAME, values,
                DataOpenHelper.COLUMN_ID + " = ?",
                new String[]{Long.toString(profile.getId())}) > 0;

        db.close();
        return updated;
    }

    /**
     * Deletes a profile in the database
     *
     * @param context
     * @param profileId
     * @return true in case of success, false if no profile was deleted
     */
    public static boolean deleteProfile(Context context, long profileId) {
        DataOpenHelper helper = new DataOpenHelper(context);
        SQLiteDatabase db = helper.getWritableDatabase();
        boolean deleted = db.delete(DataOpenHelper.PROFILES_TABLE_NAME,
                "_id=" + profileId, null) > 0;

        db.close();
        return deleted;
    }

    /**
     * Finds the position of a profile in a cursor
     *
     * @param profileId
     * @param c
     * @return the index where the profile is stored, or -1 if not found
     */
    public static int indexOf(long profileId, Cursor c) {
        int position = -1;
        if (profileId != -1 && c.moveToFirst()) {
            position = 0;
            int cursorLength = c.getCount();
            while (position < cursorLength &&
                    c.getLong(c.getColumnIndex(DataOpenHelper.COLUMN_ID)) !=
                            profileId) {
                position++;
                c.moveToNext();
            }
            if (position == cursorLength) {
                /* Profile not found */
                position = -1;
            }
        }
        return position;
    }

    public static List<Profile> getList(Context context) {
        DataOpenHelper helper = new DataOpenHelper(context);
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.query(DataOpenHelper.PROFILES_TABLE_NAME,
                new String[]{DataOpenHelper.COLUMN_ID,
                        DataOpenHelper.COLUMN_PROFILES_NAME,
                        DataOpenHelper.COLUMN_PROFILES_PRIVATE_KEY,
                        DataOpenHelper.COLUMN_PROFILES_PASSWORD_LENGTH,
                        DataOpenHelper.COLUMN_PROFILES_PASSWORD_TYPE}, null,
                null, null, null, null
        );

        List<Profile> list = new ArrayList<Profile>();
        if (cursor.moveToFirst()) {
            do {
                long id = cursor.getLong(
                        cursor.getColumnIndex(DataOpenHelper.COLUMN_ID));
                String name = cursor.getString(cursor.getColumnIndex(
                        DataOpenHelper.COLUMN_PROFILES_NAME));
                String privateKey = cursor.getString(cursor.getColumnIndex(
                        DataOpenHelper.COLUMN_PROFILES_PRIVATE_KEY));
                int passwordLength = cursor.getInt(cursor.getColumnIndex(
                        DataOpenHelper.COLUMN_PROFILES_PASSWORD_LENGTH));
                PasswordType passwordType = PasswordType.values()[cursor
                        .getInt(cursor.getColumnIndex(
                                DataOpenHelper.COLUMN_PROFILES_PASSWORD_TYPE))];
                list.add(new Profile(id, name, privateKey, passwordLength,
                        passwordType));
            } while (cursor.moveToNext());
        }

        db.close();
        return list;
    }

}
