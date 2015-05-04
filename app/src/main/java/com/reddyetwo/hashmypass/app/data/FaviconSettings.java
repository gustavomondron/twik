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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.reddyetwo.hashmypass.app.TwikApplication;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/**
 * Class to get/add/delete/update {@link com.reddyetwo.hashmypass.app.data.Favicon} from storage
 */
public class FaviconSettings {

    private static final String FILE_NAME = "favicon-%d.png";
    private static final int FAVICON_QUALITY = 100;

    private FaviconSettings() {

    }

    /**
     * Get the favicon of a site
     *
     * @param context the {@link android.content.Context} instance
     * @param site    the site
     * @return the {@link com.reddyetwo.hashmypass.app.data.Favicon} instance
     */
    public static Favicon getFavicon(Context context, String site) {
        DataOpenHelper helper = new DataOpenHelper(context);
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor =
                db.query(DataOpenHelper.FAVICONS_TABLE_NAME, new String[]{DataOpenHelper.COLUMN_ID},
                        DataOpenHelper.COLUMN_FAVICONS_SITE + "= ?", new String[]{site}, null, null,
                        null);

        Favicon favicon = null;
        if (cursor.moveToFirst()) {
            long id = cursor.getLong(cursor.getColumnIndex(DataOpenHelper.COLUMN_ID));
            try {
                String filename = String.format(FILE_NAME, id);
                FileInputStream fis = context.openFileInput(filename);
                Bitmap icon = BitmapFactory.decodeStream(fis);
                favicon = new Favicon(id, site, icon);
            } catch (FileNotFoundException e) {
                // Favicon not found in storage
                Log.d(TwikApplication.LOG_TAG, "Favicon file not found: " + e);
            }
        }

        cursor.close();
        db.close();
        return favicon;
    }

    /**
     * Insert a favicon in the storage
     *
     * @param context the {@link android.content.Context} instance
     * @param favicon the {@link com.reddyetwo.hashmypass.app.data.Favicon} instance
     * @return the ID of the inserted favicon, or -1 if an error occurred
     */
    public static long insertFavicon(Context context, Favicon favicon) {
        DataOpenHelper helper = new DataOpenHelper(context);
        SQLiteDatabase db = helper.getWritableDatabase();
        long id = -1;
        ContentValues values = new ContentValues();
        values.put(DataOpenHelper.COLUMN_FAVICONS_SITE, favicon.getSite());
        try {
            db.beginTransaction();
            id = db.insertOrThrow(DataOpenHelper.FAVICONS_TABLE_NAME, null, values);
            String filename = String.format(FILE_NAME, id);
            FileOutputStream fos = context.openFileOutput(filename, Context.MODE_PRIVATE);
            boolean stored =
                    favicon.getIcon().compress(Bitmap.CompressFormat.PNG, FAVICON_QUALITY, fos);
            fos.close();
            if (stored) {
                db.setTransactionSuccessful();
            } else {
                id = -1;
            }
        } catch (Exception e) {
            Log.d(TwikApplication.LOG_TAG, "Error saving favicon: " + e);
        } finally {
            db.endTransaction();
            db.close();
        }

        return id;
    }

    /**
     * Delete a favicon
     *
     * @param context the {@link android.content.Context} instance
     * @param favicon the {@link com.reddyetwo.hashmypass.app.data.Favicon} instance
     * @return true in case of success, false if an error occurred
     */
    public static boolean deleteFavicon(Context context, Favicon favicon) {
        DataOpenHelper helper = new DataOpenHelper(context);
        SQLiteDatabase db = helper.getWritableDatabase();
        boolean deleted = false;

        // Start transaction
        try {
            db.beginTransaction();
            db.delete(DataOpenHelper.FAVICONS_TABLE_NAME,
                    DataOpenHelper.COLUMN_ID + "=" + favicon.getId(), null);

            // Delete file from storage
            deleted = context.deleteFile(String.format(FILE_NAME, favicon.getId()));
            if (deleted) {
                db.setTransactionSuccessful();
            }
        } finally {
            db.endTransaction();
            db.close();
        }

        return deleted;
    }
}
