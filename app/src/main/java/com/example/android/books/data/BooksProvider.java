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

    //Tag for the log messages
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
                selection = BookEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(BookEntry.TABLE_NAME, projection,
                        selection, selectionArgs, null, null, null);
                break;
            // if there is no match to either sUriMatcher then throw an error
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

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

        // Check that the type is not null
        String type = values.getAsString(BookEntry.COLUMN_PRODUCT_TYPE);
        if (type == null) {
            throw new IllegalArgumentException("Type is a required field");
        }
        // Check that the price is not null
        String price = values.getAsString(BookEntry.COLUMN_PRICE);
        if (price == null) {
            throw new IllegalArgumentException("Price is a required field");
        }
        // Check that the quantity is not null
        String quantity = values.getAsString(BookEntry.COLUMN_QUANTITY);
        if (quantity == null) {
            throw new IllegalArgumentException("Quantity is a required field");
        }
        // Check that the supplier name is not null
        String supplier = values.getAsString(BookEntry.COLUMN_SUPPLIER_NAME);
        if (supplier == null) {
            throw new IllegalArgumentException("Supplier name is a required field");
        }
        // Check that the supplier phone is not null
        String phone = values.getAsString(BookEntry.COLUMN_SUPPLIER_PHONE_NUMBER);
        if (phone == null) {
            throw new IllegalArgumentException("Supplier phone number is a required field");
        }

        SQLiteDatabase database = mDbHelper.getWritableDatabase();


        // Insert the new book with the given values
        long id = database.insert(BookEntry.TABLE_NAME, null, values);
        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        // notifies listeners that data has changed for the books uri
        getContext().getContentResolver().notifyChange(uri, null);

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

        // make sure that no empty fields are saved to the database
        if (values.containsKey(BookEntry.COLUMN_PRODUCT_NAME)) {
            String title = values.getAsString(BookEntry.COLUMN_PRODUCT_NAME);
            if (title == null) {
                throw new IllegalArgumentException("Title is a required field");
            }
        }

        if (values.containsKey(BookEntry.COLUMN_PRICE)) {
            String title = values.getAsString(BookEntry.COLUMN_PRICE);
            if (title == null) {
                throw new IllegalArgumentException("Price is a required field");
            }
        }

        if (values.containsKey(BookEntry.COLUMN_QUANTITY)) {
            String title = values.getAsString(BookEntry.COLUMN_QUANTITY);
            if (title == null) {
                throw new IllegalArgumentException("Quantity is a required field");
            }
        }

        if (values.containsKey(BookEntry.COLUMN_SUPPLIER_NAME)) {
            String supplier = values.getAsString(BookEntry.COLUMN_SUPPLIER_NAME);
            if (supplier == null) {
                throw new IllegalArgumentException("Supplier Name is a required field");
            }
        }

        if (values.containsKey(BookEntry.COLUMN_SUPPLIER_PHONE_NUMBER)) {
            String title = values.getAsString(BookEntry.COLUMN_SUPPLIER_PHONE_NUMBER);
            if (title == null) {
                throw new IllegalArgumentException("Supplier phone number is a required field");
            }
        }
        if (values.size() == 0) {
            return 0;
        }
        // get a writable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // find out if the database has been updated
        int rowsUpdated = database.update(BookEntry.TABLE_NAME, values, selection, selectionArgs);

        // if the database has changed (if more than 0 rows have been updated), then activate notifyChange
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // return the # of rows updated
        return rowsUpdated;
    }

    /**
     * Delete the data at the given selection and selection arguments.
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Get writable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // keep track of how many rows have been deleted
        int rowsDeleted;

        // use the sUriMatcher to check for the correct path
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                // if the sUriMatcher finds BOOKS, then delete all rows that match the selection and selectionArgs
                rowsDeleted = database.delete(BookEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case BOOKS_ID:
                // if the sUriMatcher finds BOOKS_ID, then delete only the row specified by the _id
                selection = BookEntry._ID + "=?";
                // find and extract the row id #, then add it to the uri
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(BookEntry.TABLE_NAME, selection, selectionArgs);
                break;
            // if neither BOOKS nor BOOKS_ID are found, then an error occurs
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
        // if rows deleted is 1 or greater, then activate notifyChange
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    /**
     * Returns the MIME type of data for the content URI.
     */
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                return BookEntry.CONTENT_LIST_TYPE;
            case BOOKS_ID:
                return BookEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }
}

