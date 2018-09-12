package com.example.android.books.data;


import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.example.android.books.data.BookContract.BookEntry;

public class BooksProvider extends ContentProvider {
    /**
     * Tag for the log messages
     */
    public static final String LOG_TAG = BooksProvider.class.getSimpleName();
    private static final int BOOKS = 1;
    private static final int BOOKS_ID = 2;
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(BookContract.CONTENT_AUTHORITY, BookContract.PATH_BOOKS, BOOKS);
        sUriMatcher.addURI(BookContract.CONTENT_AUTHORITY, BookContract.PATH_BOOKS + "/#", BOOKS_ID);
    }

    // database helper object
    private BooksDbHelper mDbHelper;

    @Override
    public boolean onCreate() {

        // Initialize database helper object
        mDbHelper = new BooksDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {

        // Get readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // the cursor variable will hold the result of the query
        Cursor cursor;

        // find the and match the query to one of the sUriMatcher codes
        int match = sUriMatcher.match(uri);

        // tells what to do in case the sUriMatcher finds a match or not
        switch (match) {

            // if the sUriMatcher matches the BOOKS path, then query the whole table
            case BOOKS:
                cursor = database.query(BookEntry.TABLE_NAME, projection,
                        null, null, null, null, null);
                break;

            // if the sUriMatcher matches the BOOKS_ID path, then extract a row id
            // and query only that row
            case BOOKS_ID:
                selection = BookEntry._ID + "/=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(BookEntry.TABLE_NAME, projection,
                        selection, selectionArgs, null, null, null);
                break;
            // if there is no match to either sUriMatcher then throw an error
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        // return the results of the query
        return cursor;
    }

    /**
     * Insert new data into the provider with the given ContentValues.
     */
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                return insertBook(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /**
     * Insert a book into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    private Uri insertBook(Uri uri, ContentValues values) {

        // Check that the title is not null
        String title = values.getAsString(BookEntry.COLUMN_PRODUCT_NAME);
        if (title == null) {
            throw new IllegalArgumentException("Title is a required field");
        }

        // Check that the supplier name is not null
        String supplier = values.getAsString(BookEntry.COLUMN_SUPPLIER_NAME);
        if (supplier == null) {
            throw new IllegalArgumentException("Supplier name is a required field");
        }

        SQLiteDatabase database = mDbHelper.getWritableDatabase();


        // Insert the new book with the given values
        long id = database.insert(BookEntry.TABLE_NAME, null, values);
        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        // Once we know the ID of the new row in the table,
        // return the new URI with the ID appended to the end of it
        return ContentUris.withAppendedId(uri, id);
    }

    /**
     * Updates the data at the given selection and selection arguments, with the new ContentValues.
     */
    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            // if the sUriMatcher finds BOOKS value
            case BOOKS:
                return updateBooks(uri, contentValues, selection, selectionArgs);
            // if the sUriMatcher finds BOOKS_ID, then extract the line ID so it can be appended to the uri
            case BOOKS_ID:
                selection = BookEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateBooks(uri, contentValues, selection, selectionArgs);
            // if neither BOOKS nor BOOKS_ID value is matched, then throw exception
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    private int updateBooks(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        if (values.containsKey(BookEntry.COLUMN_PRODUCT_NAME)) {
            String title = values.getAsString(BookEntry.COLUMN_PRODUCT_NAME);
            if (title == null) {
                throw new IllegalArgumentException("Title is a required field");
            }
        }
        if (values.containsKey(BookEntry.COLUMN_SUPPLIER_NAME)) {
            String supplier = values.getAsString(BookEntry.COLUMN_SUPPLIER_NAME);
            if (supplier == null) {
                throw new IllegalArgumentException("Supplier Name is a required field");
            }
        }
        if (values.size() == 0) {
            return 0;
        }
        // get a writable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // return the uri with value pairs, selection and selection args
        return database.update(BookEntry.TABLE_NAME, values, selection, selectionArgs);
    }

    /**
     * Delete the data at the given selection and selection arguments.
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    /**
     * Returns the MIME type of data for the content URI.
     */
    @Override
    public String getType(Uri uri) {
        return null;
    }
}

