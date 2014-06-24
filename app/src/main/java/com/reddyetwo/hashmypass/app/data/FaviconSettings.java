package com.reddyetwo.hashmypass.app.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class FaviconSettings {

    private static final String FILE_NAME = "favicon-%d.png";

    public static Favicon getFavicon(Context context, String site) {
        DataOpenHelper helper = new DataOpenHelper(context);
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.query(DataOpenHelper.FAVICONS_TABLE_NAME,
                new String[]{DataOpenHelper.COLUMN_ID},
                DataOpenHelper.COLUMN_FAVICONS_SITE + "= ?", new String[]{site},
                null, null, null);

        Favicon favicon = null;
        if (cursor.moveToFirst()) {
            long id = cursor.getLong(
                    cursor.getColumnIndex(DataOpenHelper.COLUMN_ID));
            try {
                String filename = String.format(FILE_NAME, id);
                FileInputStream fis = context.openFileInput(filename);
                Bitmap icon = BitmapFactory.decodeStream(fis);
                favicon = new Favicon(id, site, icon);
            } catch (FileNotFoundException e) {
                // Favicon not found in storage
            }
        }

        db.close();
        return favicon;
    }

    public static long insertFavicon(Context context, Favicon favicon) {
        DataOpenHelper helper = new DataOpenHelper(context);
        SQLiteDatabase db = helper.getWritableDatabase();
        long id = -1;
        ContentValues values = new ContentValues();
        values.put(DataOpenHelper.COLUMN_FAVICONS_SITE, favicon.getSite());
        try {
            db.beginTransaction();
            id = db.insertOrThrow(DataOpenHelper.FAVICONS_TABLE_NAME, null,
                    values);
            String filename = String.format(FILE_NAME, id);
            FileOutputStream fos =
                    context.openFileOutput(filename, Context.MODE_PRIVATE);
            boolean stored = favicon.getIcon()
                    .compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
            if (stored) {
                db.setTransactionSuccessful();
            } else {
                id = -1;
            }
        } catch (Exception e) {
            Log.d("TEST", "Error: " + e.getMessage());
            //} catch (FileNotFoundException e) {
            //   id = -1;
        } finally {
            db.endTransaction();
            db.close();
        }

        return id;
    }

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
            deleted = context.deleteFile(
                    String.format(FILE_NAME, favicon.getId()));
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
