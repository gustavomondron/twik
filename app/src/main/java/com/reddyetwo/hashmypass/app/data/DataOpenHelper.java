package com.reddyetwo.hashmypass.app.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DataOpenHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "hassmypass.db";
    private static final int DATABASE_VERSION = 1;

    public static final String COLUMN_ID = "_id";

    /* Table "profiles" */
    public static final String PROFILES_TABLE_NAME = "profiles";
    public static final String COLUMN_PROFILES_NAME = "name";
    public static final String COLUMN_PROFILES_PRIVATE_KEY = "private_key";
    public static final String COLUMN_PROFILES_PASSWORD_LENGTH =
            "password_length";
    public static final String COLUMN_PROFILES_PASSWORD_TYPE = "password_type";
    private static final String PROFILES_TABLE_CREATE =
            "CREATE TABLE " + PROFILES_TABLE_NAME + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY, " +
                    COLUMN_PROFILES_NAME + " TEXT, " +
                    COLUMN_PROFILES_PRIVATE_KEY + " TEXT, " +
                    COLUMN_PROFILES_PASSWORD_LENGTH + " INTEGER, " +
                    COLUMN_PROFILES_PASSWORD_TYPE + " INTEGER" +
                    ");";

    /* Table "tags" */
    public static final String TAGS_TABLE_NAME = "tags";
    public static final String COLUMN_TAGS_NAME = "name";
    public static final String COLUMN_TAGS_PROFILE_ID = "profile_id";
    public static final String COLUMN_TAGS_SITE = "site";
    public static final String COLUMN_TAGS_PASSWORD_LENGTH = "password_length";
    public static final String COLUMN_TAGS_PASSWORD_TYPE = "password_type";
    private static final String TAGS_TABLE_CREATE =
            "CREATE TABLE " + TAGS_TABLE_NAME + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY, " +
                    COLUMN_TAGS_NAME + " TEXT, " +
                    COLUMN_TAGS_PROFILE_ID + " INTEGER, " +
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

    public DataOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(PROFILES_TABLE_CREATE);
        db.execSQL(TAGS_TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
