package com.example.android.books.data;

import android.net.Uri;
import android.provider.BaseColumns;

public final class BookContract {


    // Empty constructor
    private BookContract(){}

    // constant for the content authority
    public static final String CONTENT_AUTHORITY = "com.example.android.bookspart2";

    // Use the content_authority to create the base uri that will contact the content provider
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // path that can be appended to the uri when calling the entire table "books"
    public static final String PATH_BOOKS = "books";


    public static abstract class BookEntry implements BaseColumns {

        /** The content URI to access the books data in the provider */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_BOOKS);

        // name of the table
        public static final String TABLE_NAME = "books";

        //schema for the table
        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_PRODUCT_NAME = "name";
        public static final String COLUMN_PRODUCT_TYPE = "type";
        public static final String COLUMN_PRICE = "price";
        public static final String COLUMN_QUANTITY = "quantity";
        public static final String COLUMN_SUPPLIER_NAME = "supplier";
        public static final String COLUMN_SUPPLIER_PHONE_NUMBER = "phone";

        // constants for product type
        public static final int HARDBACK = 0;
        public static final int PAPERBACK = 1;
        public static final int EBOOK = 2;
    }
}
