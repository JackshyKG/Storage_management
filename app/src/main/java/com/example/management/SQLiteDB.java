package com.example.management;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class SQLiteDB extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "mainDB";

    // Documents - TABLE_DOC
    public static final String TABLE_DOC = "documents";
    public static final String KEY_ID = "_id";// id for both tables (TABLE_DOC & TABLE_ITEM)
    public static final String KEY_NUMBER = "number";// number of document
    public static final String KEY_DATE = "date";
    public static final String KEY_TIME = "time";
    public static final String KEY_ITEM_ID = "item";// item id
    public static final String KEY_COUNT = "count";

    // Items - TABLE_ITEM
    public static final String TABLE_ITEM = "items";
    public static final String ITEM_NAME = "item";



    public SQLiteDB(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("create table " + TABLE_DOC + "(" + KEY_ID + " integer primary key,"
                + KEY_NUMBER + " integer," + KEY_DATE + " integer," + KEY_TIME + " integer," + KEY_ITEM_ID + " integer,"
                + KEY_COUNT + " integer" + ")");

        db.execSQL("create table " + TABLE_ITEM + "(" + KEY_ID + " integer primary key,"
                + ITEM_NAME + " text" + ")");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists " + TABLE_DOC);
        db.execSQL("drop table if exists " + TABLE_ITEM);
        onCreate(db);
    }

}
