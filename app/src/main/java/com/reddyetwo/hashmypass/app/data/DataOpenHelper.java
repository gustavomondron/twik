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

/**
 * Helper used to access database, as well as creating/upgrading it.
 */
class DataOpenHelper extends SQLiteOpenHelper {

    /**
     * Name of the ID column in the tables
     */
    public static final String COLUMN_ID = "_id";

    // Column names of the profiles table
    public static final String PROFILES_TABLE_NAME = "profiles";
    public static final String COLUMN_PROFILES_NAME = "name";
    public static final String COLUMN_PROFILES_PRIVATE_KEY = "private_key";
    public static final String COLUMN_PROFILES_PASSWORD_LENGTH = "password_length";
    public static final String COLUMN_PROFILES_PASSWORD_TYPE = "password_type";
    public static final String COLUMN_PROFILES_COLOR_INDEX = "color_index";

    // Column names of the tags table
    public static final String TAGS_TABLE_NAME = "tags";
    public static final String COLUMN_TAGS_NAME = "name";
    public static final String COLUMN_TAGS_PROFILE_ID = "profile_id";
    public static final String COLUMN_TAGS_SITE = "site";
    public static final String COLUMN_TAGS_HASH_COUNTER = "hash_counter";
    public static final String COLUMN_TAGS_PASSWORD_LENGTH = "password_length";
    public static final String COLUMN_TAGS_PASSWORD_TYPE = "password_type";

    // Column names of the favicons table
    public static final String FAVICONS_TABLE_NAME = "favicons";
    public static final String COLUMN_FAVICONS_SITE = "site";

    /**
     * Database name
     */
    private static final String DATABASE_NAME = "hassmypass.db";

    /**
     * Database versions
     */
    private static final int DATABASE_VERSION_CURRENT = 4;
    private static final int DATABASE_VERSION_1 = 1;
    private static final int DATABASE_VERSION_2 = 2;
    private static final int DATABASE_VERSION_3 = 3;

    // Strings used in SQL queries
    private static final String SQL_CREATE_TABLE = "CREATE TABLE ";
    private static final String SQL_ALTER_TABLE = "ALTER TABLE ";
    private static final String SQL_INTEGER = " INTEGER";
    private static final String SQL_PRIMARY_KEY = " PRIMARY KEY";
    private static final String SQL_TEXT = " TEXT";
    private static final String SQL_NOT_NULL = " NOT NULL";
    private static final String SQL_DEFAULT = " DEFAULT 0";
    private static final String SQL_FIELD_SEPARATOR = ", ";
    private static final String SQL_FOREIGN_KEY = "FOREIGN KEY ";
    private static final String SQL_REFERENCES = " REFERENCES ";
    private static final String SQL_UNIQUE = "UNIQUE ";
    private static final String SQL_ADD_COLUMN = " ADD COLUMN ";

    /**
     * SQL query for creating the profiles table
     */
    private static final String PROFILES_TABLE_CREATE =
            SQL_CREATE_TABLE + PROFILES_TABLE_NAME + " (" +
                    COLUMN_ID + SQL_INTEGER + SQL_PRIMARY_KEY + SQL_FIELD_SEPARATOR +
                    COLUMN_PROFILES_NAME + SQL_TEXT + SQL_FIELD_SEPARATOR +
                    COLUMN_PROFILES_PRIVATE_KEY + SQL_TEXT + SQL_FIELD_SEPARATOR +
                    COLUMN_PROFILES_PASSWORD_LENGTH + SQL_INTEGER + SQL_FIELD_SEPARATOR +
                    COLUMN_PROFILES_PASSWORD_TYPE + SQL_INTEGER + SQL_FIELD_SEPARATOR +
                    COLUMN_PROFILES_COLOR_INDEX + SQL_INTEGER + SQL_NOT_NULL + SQL_DEFAULT + ");";

    /**
     * SQL query for creating the tags table
     */
    private static final String TAGS_TABLE_CREATE = SQL_CREATE_TABLE + TAGS_TABLE_NAME + " (" +
            COLUMN_ID + SQL_INTEGER + SQL_PRIMARY_KEY + SQL_FIELD_SEPARATOR +
            COLUMN_TAGS_NAME + SQL_TEXT + SQL_FIELD_SEPARATOR +
            COLUMN_TAGS_PROFILE_ID + SQL_INTEGER + SQL_FIELD_SEPARATOR +
            COLUMN_TAGS_HASH_COUNTER + SQL_INTEGER + SQL_NOT_NULL + SQL_DEFAULT +
            SQL_FIELD_SEPARATOR + COLUMN_TAGS_SITE + SQL_TEXT + SQL_FIELD_SEPARATOR +
            COLUMN_TAGS_PASSWORD_LENGTH + SQL_INTEGER + SQL_FIELD_SEPARATOR +
            COLUMN_TAGS_PASSWORD_TYPE + SQL_INTEGER + SQL_FIELD_SEPARATOR +
            SQL_FOREIGN_KEY + "(" + COLUMN_TAGS_PROFILE_ID + ")" + SQL_REFERENCES +
            PROFILES_TABLE_NAME + "(id)" + SQL_UNIQUE + "(" + COLUMN_TAGS_NAME +
            SQL_FIELD_SEPARATOR + COLUMN_TAGS_PROFILE_ID + ")," +
            SQL_UNIQUE + "(" + COLUMN_TAGS_PROFILE_ID + SQL_FIELD_SEPARATOR +
            COLUMN_TAGS_SITE + ")" + ");";

    /**
     * SQL query for creating the favicons table
     */
    private static final String FAVICONS_TABLE_CREATE = SQL_CREATE_TABLE +
            FAVICONS_TABLE_NAME + " (" + COLUMN_ID + SQL_INTEGER + SQL_PRIMARY_KEY +
            SQL_FIELD_SEPARATOR + COLUMN_FAVICONS_SITE + SQL_TEXT + SQL_FIELD_SEPARATOR +
            SQL_UNIQUE + "(" + COLUMN_FAVICONS_SITE + "));";

    /**
     * SQL query for adding hash counter column to the tags table
     */
    private static final String TAGS_TABLE_ADD_HASH_COUNTER_COLUMN =
            SQL_ALTER_TABLE + TAGS_TABLE_NAME + SQL_ADD_COLUMN + COLUMN_TAGS_HASH_COUNTER +
                    SQL_INTEGER + SQL_NOT_NULL + SQL_DEFAULT;

    /**
     * SQL query for adding the color index column to the profiles table
     */
    private static final String PROFILES_TABLE_ADD_COLOR_INDEX_COLUMN = "" +
            SQL_ALTER_TABLE + PROFILES_TABLE_NAME + SQL_ADD_COLUMN +
            COLUMN_PROFILES_COLOR_INDEX + SQL_INTEGER + SQL_NOT_NULL + SQL_DEFAULT;

    /**
     * Constructor
     *
     * @param context the {@link android.content.Context} instance
     */
    public DataOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION_CURRENT);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(PROFILES_TABLE_CREATE);
        db.execSQL(TAGS_TABLE_CREATE);
        db.execSQL(FAVICONS_TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion <= DATABASE_VERSION_1) {
            db.execSQL(FAVICONS_TABLE_CREATE);
        }
        if (oldVersion <= DATABASE_VERSION_2) {
            db.execSQL(TAGS_TABLE_ADD_HASH_COUNTER_COLUMN);
        }
        if (oldVersion <= DATABASE_VERSION_3) {
            db.execSQL(PROFILES_TABLE_ADD_COLOR_INDEX_COLUMN);
        }
    }
}
