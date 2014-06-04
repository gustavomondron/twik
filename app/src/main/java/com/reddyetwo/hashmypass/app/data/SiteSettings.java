package com.reddyetwo.hashmypass.app.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class SiteSettings {

    public static String getSiteTag(Context context, long profileID,
                                    String site) {
        DataOpenHelper helper = new DataOpenHelper(context);
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.query(DataOpenHelper.TAGS_TABLE_NAME,
                new String[]{DataOpenHelper.COLUMN_TAGS_NAME},
                DataOpenHelper.COLUMN_TAGS_PROFILE_ID + "=" + profileID + " " +
                        "AND " + DataOpenHelper.COLUMN_TAGS_SITE + "=\"" +
                        site + "\"", null, null, null, null
        );

        String tag = null;
        if (cursor.moveToFirst()) {
            tag = cursor.getString(
                    cursor.getColumnIndex(DataOpenHelper.COLUMN_TAGS_NAME));
        }

        db.close();

        return tag;
    }

    public static void updateSiteTag(Context context, long profileID,
                                     String site, String tag) {

        String currentTag = getSiteTag(context, profileID, site);

        DataOpenHelper helper = new DataOpenHelper(context);
        SQLiteDatabase db = helper.getReadableDatabase();

        if (currentTag != tag) {
            /* Remove the old association */
            ContentValues values = new ContentValues();
            values.put(DataOpenHelper.COLUMN_TAGS_SITE, (String) null);
            db.update(DataOpenHelper.TAGS_TABLE_NAME, values,
                    DataOpenHelper.COLUMN_TAGS_NAME + "=\"" + currentTag +
                            "\" AND " +
                            "" + DataOpenHelper.COLUMN_TAGS_PROFILE_ID + "=" +
                            profileID, null
            );
        }

        /* Set the new association */
        ContentValues values = new ContentValues();
        values.put(DataOpenHelper.COLUMN_TAGS_SITE, site);
        db.update(DataOpenHelper.TAGS_TABLE_NAME, values,
                DataOpenHelper.COLUMN_TAGS_NAME + "=\"" + tag + "\" AND " +
                        "" + DataOpenHelper.COLUMN_TAGS_PROFILE_ID + "=" +
                        profileID, null
        );
    }
}
