package com.reddyetwo.hashmypass.app.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class ProfileSettings {

    public static ContentValues getProfileSettings(Context context,
                                                   long profileID) {
        DataOpenHelper helper = new DataOpenHelper(context);
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.query(DataOpenHelper.PROFILES_TABLE_NAME,
                new String[]{DataOpenHelper.COLUMN_PROFILES_NAME,
                        DataOpenHelper.COLUMN_PROFILES_PRIVATE_KEY,
                        DataOpenHelper.COLUMN_PROFILES_PASSWORD_LENGTH,
                        DataOpenHelper.COLUMN_PROFILES_PASSWORD_TYPE},
                DataOpenHelper.COLUMN_ID + "=" + profileID, null, null, null,
                null
        );

        cursor.moveToFirst();
        /* TODO Check that cursor contains data */

        /* Add values to ContentValues object */
        ContentValues values = new ContentValues();
        values.put(DataOpenHelper.COLUMN_PROFILES_NAME, cursor.getString(
                        cursor.getColumnIndex(
                                DataOpenHelper.COLUMN_PROFILES_NAME)
                )
        );
        values.put(DataOpenHelper.COLUMN_PROFILES_PRIVATE_KEY, cursor.getString(
                        cursor.getColumnIndex(
                                DataOpenHelper.COLUMN_PROFILES_PRIVATE_KEY)
                )
        );
        values.put(DataOpenHelper.COLUMN_PROFILES_PASSWORD_LENGTH,
                cursor.getInt(cursor.getColumnIndex(
                        DataOpenHelper.COLUMN_PROFILES_PASSWORD_LENGTH))
        );
        values.put(DataOpenHelper.COLUMN_PROFILES_PASSWORD_TYPE, cursor.getInt(
                        cursor.getColumnIndex(
                                DataOpenHelper.COLUMN_PROFILES_PASSWORD_TYPE)
                )
        );

        db.close();
        return values;
    }

    /**
     * Inserts a profile in the database
     *
     * @param context
     * @param name
     * @param privateKey
     * @param passwordLength
     * @param passwordType
     * @return the ID of the inserted row, or -1 if an error occurred
     */
    public static long insertProfileSettings(Context context, String name,
                                             String privateKey,
                                             int passwordLength,
                                             PasswordType passwordType) {
        DataOpenHelper helper = new DataOpenHelper(context);
        SQLiteDatabase db = helper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DataOpenHelper.COLUMN_PROFILES_NAME, name);
        values.put(DataOpenHelper.COLUMN_PROFILES_PRIVATE_KEY, privateKey);
        values.put(DataOpenHelper.COLUMN_PROFILES_PASSWORD_LENGTH,
                passwordLength);
        values.put(DataOpenHelper.COLUMN_PROFILES_PASSWORD_TYPE,
                passwordType.ordinal());

        return db.insertOrThrow(DataOpenHelper.PROFILES_TABLE_NAME, null,
                values);
    }

    public static void updateProfileSettings(Context context, long profileID,
                                             String name, String privateKey,
                                             int passwordLength,
                                             PasswordType passwordType) {
        DataOpenHelper helper = new DataOpenHelper(context);
        SQLiteDatabase db = helper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DataOpenHelper.COLUMN_PROFILES_NAME, name);
        values.put(DataOpenHelper.COLUMN_PROFILES_PRIVATE_KEY, privateKey);
        values.put(DataOpenHelper.COLUMN_PROFILES_PASSWORD_LENGTH,
                passwordLength);
        values.put(DataOpenHelper.COLUMN_PROFILES_PASSWORD_TYPE,
                passwordType.ordinal());

        db.update(DataOpenHelper.PROFILES_TABLE_NAME, values,
                DataOpenHelper.COLUMN_ID + " = ?",
                new String[]{Long.toString(profileID)});
    }

}
