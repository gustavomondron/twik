package com.reddyetwo.hashmypass.app.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.ContactsContract;
import android.util.Log;

public class TagSettings {

    public static ContentValues getTagSettings(Context context, long profileID,
                                               String tag) {
        DataOpenHelper helper = new DataOpenHelper(context);
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.query(DataOpenHelper.TAGS_TABLE_NAME,
                new String[] {DataOpenHelper.COLUMN_TAGS_PASSWORD_LENGTH,
                        DataOpenHelper.COLUMN_TAGS_PASSWORD_TYPE},
                DataOpenHelper.COLUMN_TAGS_PROFILE_ID + "=" +
                        profileID + " AND " + DataOpenHelper.COLUMN_TAGS_NAME
                        + "=\"" + tag + "\"", null, null, null, null);

        ContentValues values = new ContentValues();
        if (cursor.moveToFirst()) {
            /* Tag settings found */
            values.put(DataOpenHelper.COLUMN_TAGS_PASSWORD_LENGTH,
                    cursor.getInt(cursor.getColumnIndex(DataOpenHelper
                            .COLUMN_TAGS_PASSWORD_LENGTH)));
            values.put(DataOpenHelper.COLUMN_TAGS_PASSWORD_TYPE,
                    cursor.getInt(cursor.getColumnIndex(DataOpenHelper
                            .COLUMN_TAGS_PASSWORD_TYPE)));
        }
        if (!cursor.moveToFirst()) {
            /* Tag settings not found, use profile settings */
            cursor = db.query(DataOpenHelper.PROFILES_TABLE_NAME,
                    new String[] { DataOpenHelper
                            .COLUMN_PROFILES_PASSWORD_LENGTH,
                            DataOpenHelper.COLUMN_PROFILES_PASSWORD_TYPE},
                    DataOpenHelper.COLUMN_ID + "=" + profileID,
                    null, null, null, null);
            cursor.moveToFirst();
            /* TODO Check that cursor contains data */
            values.put(DataOpenHelper.COLUMN_PROFILES_PASSWORD_LENGTH,
                    cursor.getInt(cursor.getColumnIndex(DataOpenHelper
                            .COLUMN_PROFILES_PASSWORD_LENGTH)));
            values.put(DataOpenHelper.COLUMN_PROFILES_PASSWORD_TYPE,
                    cursor.getInt(cursor.getColumnIndex(DataOpenHelper
                            .COLUMN_PROFILES_PASSWORD_TYPE)));
        }

        db.close();
        return values;
    }
}
