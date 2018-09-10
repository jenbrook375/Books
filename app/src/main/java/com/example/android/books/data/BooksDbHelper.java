package com.example.android.books.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class BooksDbHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "inventory.db";

    public BooksDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {

        // called when database is created
        String SQL_CREATE_BOOKS_TABLE = "CREATE TABLE " + BookContract.BookEntry.TABLE_NAME + "("
                + BookContract.BookEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + BookContract.BookEntry.COLUMN_PRODUCT_NAME + " TEXT NOT NULL, "
                + BookContract.BookEntry.COLUMN_PRODUCT_TYPE + " INTEGER, "
                + BookContract.BookEntry.COLUMN_PRICE + " REAL, "
                + BookContract.BookEntry.COLUMN_QUANTITY + " INTEGER, "
                + BookContract.BookEntry.COLUMN_SUPPLIER_NAME + " TEXT, "
                + BookContract.BookEntry.COLUMN_SUPPLIER_PHONE_NUMBER + " TEXT);";

        // executes creation of database
        db.execSQL(SQL_CREATE_BOOKS_TABLE);
    }

    // Called when database is upgraded
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
