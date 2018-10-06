package com.example.android.books;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.android.books.data.BookContract.BookEntry;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    // identifier for book data loader
    private static final int URI_LOADER = 0;
    private static final String TAG = "MainActivity";
    // adapter for the listView
    BooksCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Create floating action button (@link activity_main.xml) and set OnClick Listener that opens EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        ListView bookListView = (ListView) findViewById(R.id.list);

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = findViewById(R.id.empty_view);
        bookListView.setEmptyView(emptyView);

        // set up an adapter to create a list item for each row of the database
        mCursorAdapter = new BooksCursorAdapter(this, null);
        bookListView.setAdapter(mCursorAdapter);

        // set up an onClickListener to open intent when list item is clicked
        bookListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            // opens up EditorActivity
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);

                // pass the contentUri plus the id # of the list item selected
                Uri currentBookUri = ContentUris.withAppendedId(BookEntry.CONTENT_URI, id);

                // pass the correct uri to the intent
                intent.setData(currentBookUri);

                // open the activity
                startActivity(intent);
                Log.i(TAG, "FABOnClickListener" + currentBookUri);
            }
        });

        // kick off the loader
        getLoaderManager().initLoader(URI_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // The projection defines the columns that will be queried
        String[] projection = {BookEntry._ID,
                BookEntry.COLUMN_PRODUCT_NAME,
                BookEntry.COLUMN_PRODUCT_TYPE,
                BookEntry.COLUMN_PRICE,
                BookEntry.COLUMN_QUANTITY};

        return new CursorLoader(this,
                BookEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {


        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);

    }

    public void sellBook(int bookID, int quantity) {
        // Perform saleButton ImageView and decrease quantity by one
        quantity--;

        // check if there is at least 1 book to sell, if there is then update the quantity
        if (quantity >= 0) {
            // Create a ContentValues object where column names are the keys,
            // and new book attributes are the values.
            ContentValues values = new ContentValues();
            values.put(BookEntry.COLUMN_QUANTITY, quantity);
            Uri updateUri = ContentUris.withAppendedId(BookEntry.CONTENT_URI, bookID);
            int rowsAffected = getContentResolver().update(updateUri, values, null, null);
            // if the row has been updated then confirm sale
            if (rowsAffected == 1) {
                Toast.makeText(this, R.string.confirm_sale, Toast.LENGTH_LONG).show();
            }
            // if the quantity of books is already 0, then do not decrease quantity
            // and show toast message declining sale
        } else {
            Toast.makeText(this, R.string.no_sale, Toast.LENGTH_LONG).show();
        }
    }
}

