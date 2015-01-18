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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

class DataOpenHelper extends SQLiteOpenHelper {

    // TODO Typo in the db name... we should fix it... or maybe it's too late
    private static final String DATABASE_NAME = "hassmypass.db";
    private static final int DATABASE_VERSION = 4;

    public static final String COLUMN_ID = "_id";

    /* Table "profiles" */
    public static final String PROFILES_TABLE_NAME = "profiles";
    public static final String COLUMN_PROFILES_NAME = "name";
    public static final String COLUMN_PROFILES_PRIVATE_KEY = "private_key";
    public static final String COLUMN_PROFILES_PASSWORD_LENGTH =
            "password_length";
    public static final String COLUMN_PROFILES_PASSWORD_TYPE = "password_type";
    public static final String COLUMN_PROFILES_COLOR_INDEX = "color_index";
    private static final String PROFILES_TABLE_CREATE =
            "CREATE TABLE " + PROFILES_TABLE_NAME + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY, " +
                    COLUMN_PROFILES_NAME + " TEXT, " +
                    COLUMN_PROFILES_PRIVATE_KEY + " TEXT, " +
                    COLUMN_PROFILES_PASSWORD_LENGTH + " INTEGER, " +
                    COLUMN_PROFILES_PASSWORD_TYPE + " INTEGER, " +
                    COLUMN_PROFILES_COLOR_INDEX + " INTEGER NOT NULL DEFAULT " +
                    "0" +
                    ");";

    /* Table "tags" */
    public static final String TAGS_TABLE_NAME = "tags";
    public static final String COLUMN_TAGS_NAME = "name";
    public static final String COLUMN_TAGS_PROFILE_ID = "profile_id";
    public static final String COLUMN_TAGS_SITE = "site";
    public static final String COLUMN_TAGS_HASH_COUNTER = "hash_counter";
    public static final String COLUMN_TAGS_PASSWORD_LENGTH = "password_length";
    public static final String COLUMN_TAGS_PASSWORD_TYPE = "password_type";
    private static final String TAGS_TABLE_CREATE =
            "CREATE TABLE " + TAGS_TABLE_NAME + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY, " +
                    COLUMN_TAGS_NAME + " TEXT, " +
                    COLUMN_TAGS_PROFILE_ID + " INTEGER, " +
                    COLUMN_TAGS_HASH_COUNTER + " INTEGER NOT NULL DEFAULT 0, " +
                    COLUMN_TAGS_SITE + " TEXT, " +
                    COLUMN_TAGS_PASSWORD_LENGTH + " INTEGER, " +
                    COLUMN_TAGS_PASSWORD_TYPE + " INTEGER, " +
                    "FOREIGN KEY(" + COLUMN_TAGS_PROFILE_ID + ") REFERENCES " +
                    PROFILES_TABLE_NAME + "(id)" +
                    "UNIQUE (" + COLUMN_TAGS_NAME + "," +
                    "" + COLUMN_TAGS_PROFILE_ID + ")," +
                    "UNIQUE (" + COLUMN_TAGS_PROFILE_ID + "," +
                    "" + COLUMN_TAGS_SITE + ")" +
                    ");";
    private static final String TAGS_TABLE_ADD_HASH_COUNTER_COLUMN = "" +
            "ALTER TABLE " + TAGS_TABLE_NAME + " ADD COLUMN " +
            COLUMN_TAGS_HASH_COUNTER + " INTEGER NOT NULL DEFAULT 0;";

    private static final String PROFILES_TABLE_ADD_COLOR_INDEX_COLUMN = "" +
            "ALTER TABLE " + PROFILES_TABLE_NAME + " ADD COLUMN " +
            COLUMN_PROFILES_COLOR_INDEX + " INTEGER NOT NULL DEFAULT 0;";

    /* Table "favicons" */
    public static final String FAVICONS_TABLE_NAME = "favicons";
    public static final String COLUMN_FAVICONS_SITE = "site";
    private static final String FAVICONS_TABLE_CREATE = "CREATE TABLE " +
            FAVICONS_TABLE_NAME + " (" + COLUMN_ID + " INTEGER PRIMARY KEY, " +
            COLUMN_FAVICONS_SITE + " TEXT, " +
            "UNIQUE (" + COLUMN_FAVICONS_SITE + "));";

    public DataOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(PROFILES_TABLE_CREATE);
        db.execSQL(TAGS_TABLE_CREATE);
        db.execSQL(FAVICONS_TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        switch (oldVersion) {
            case 1:
                db.execSQL(FAVICONS_TABLE_CREATE);
            case 2:
                db.execSQL(TAGS_TABLE_ADD_HASH_COUNTER_COLUMN);
            case 3:
                db.execSQL(PROFILES_TABLE_ADD_COLOR_INDEX_COLUMN);
        }
    }
}
