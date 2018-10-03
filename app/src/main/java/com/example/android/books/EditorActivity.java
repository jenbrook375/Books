package com.example.android.books;


import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.android.books.data.BookContract.BookEntry;
import com.example.android.books.data.BooksDbHelper;


public class EditorActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private BooksDbHelper mDbHelper;

    // identifier for the book data laoder
    private static final int EXISTING_BOOK_LOADER = 0;
    private static final double DEFAULT_PRICE = 0.00;

    // EditText fields and spinner for book info
    private EditText mTitleEditText;
    private Spinner mTypeSpinner;
    private EditText mPriceEditText;
    private EditText mQuantityEditText;
    private EditText mSupplierNameEditText;
    private EditText mSupplierPhoneEditText;

    // set default book type to 0/hardback (other possibilities are 1=paperback, 2=ebook)
    private int mType = 0;
    private static final int DEFAULT_QUANTITY = 0;
    private static final String TAG = "EditorActivity";
    // content uri for existing book
    private Uri mCurrentBookUri;
    // keep track of whether book entry has changed (true) or not (false)
    private boolean mBookHasChanged = false;

    // onTouchListener tells if the user has touched any of the edit fields and changes
    // the mBookHasChanged boolean to true
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent motionEvent) {
            mBookHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // get intent data from MainActivity
        Intent intent = getIntent();
        mCurrentBookUri = intent.getData();

        // see if there is an uri passed in from MainActivity
        // if there isn't then make title Add a  new Book
        if (mCurrentBookUri == null) {
            setTitle(getString(R.string.editor_add_book_label));
            // if there is an uri, then make title Edit an existing book
            invalidateOptionsMenu();
        } else {
            setTitle(getString(R.string.editor_edit_book_label));

            // Initialize a loader to read the BOOK data from the database
            // and display the current values in the editor
            getLoaderManager().initLoader(EXISTING_BOOK_LOADER, null, this);
        }

        // Find all relevant views that we will need to read user input from
        mTitleEditText = (EditText) findViewById(R.id.title);
        mTypeSpinner = (Spinner) findViewById(R.id.spinner_type);
        mPriceEditText = (EditText) findViewById(R.id.edit_price);
        mQuantityEditText = (EditText) findViewById(R.id.edit_quantity);
        mSupplierNameEditText = (EditText) findViewById(R.id.edit_supplier_name);
        mSupplierPhoneEditText = (EditText) findViewById(R.id.edit_supplier_phone);

        mTitleEditText.setOnTouchListener(mTouchListener);
        mTypeSpinner.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);
        mSupplierNameEditText.setOnTouchListener(mTouchListener);
        mSupplierPhoneEditText.setOnTouchListener(mTouchListener);

        setupSpinner();
    }

    // Drop-down menu that allows selection of book type
    private void setupSpinner() {
        // set up array with type options, sets layout
        ArrayAdapter typeSpinnerAdapter = ArrayAdapter.createFromResource
                (this, R.array.array_book_types, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        typeSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        //set the adapter on the spinner
        mTypeSpinner.setAdapter(typeSpinnerAdapter);

        // Set the integer mType to the constant values
        mTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.hardback))) {
                        mType = BookEntry.HARDBACK; // hardback book
                    } else if (selection.equals(getString(R.string.paperback))) {
                        mType = BookEntry.PAPERBACK; // paperback book
                    } else {
                        mType = BookEntry.EBOOK; // ebook
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mType = 0; // Hardback
            }
        });
    }

    // Inflate menu (menu.menu_editor.xml) to create app bar overflow menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (mCurrentBookUri == null) {
            // hide delete entry option if this is a new book entry
            MenuItem menuItem = menu.findItem(R.id.delete_entry);
            menuItem.setVisible(false);
        }
        return true;
    }

    // Handle user clicks on menu item
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // if user selects save option
            case R.id.action_save:
                // save user input to database
                saveBook();
                // exit editorActivity and return to mainActivity
                finish();
                return true;
            // if user selects EXIT then finish and return to mainActivity w/out saving
            case R.id.action_exit:
                finish();
                return true;
            // if user selects Delete Entry then
            case R.id.delete_entry:
                // run dialog to confirm deletion
                showDeleteConfirmationDialog();
                Log.i(TAG, "delete entry");
                return true;
            case android.R.id.home:


                // if there are no changes continue navigating up to MainActivity
                if (!mBookHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }
                // set up dialog to warn user that there are unsaved changes
                DialogInterface.OnClickListener discardButtonListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // if user clicks discard then return to parent activity
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };
                //show dialog that user has unsaved changes
                showUnsavedChangesDialog(discardButtonListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Get user input from edit text fields and spinner, then insert that data into the table
    private void saveBook() {
        String titleString = mTitleEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        String quantityString = mQuantityEditText.getText().toString().trim();
        String supplierNameString = mSupplierNameEditText.getText().toString().trim();
        String supplierPhoneString = mSupplierPhoneEditText.getText().toString().trim();

        // determine if this is a new book or an existing book
        // if there is no current uri then this is a new book
        if (mCurrentBookUri == null &&
                TextUtils.isEmpty(titleString)) {
            return;
        }

        // Create key value pairs that will be inserted into table
        ContentValues values = new ContentValues();
        values.put(BookEntry.COLUMN_PRODUCT_NAME, titleString);
        values.put(BookEntry.COLUMN_PRODUCT_TYPE, mType);
        values.put(BookEntry.COLUMN_PRICE, priceString);
        values.put(BookEntry.COLUMN_QUANTITY, quantityString);
        values.put(BookEntry.COLUMN_SUPPLIER_NAME, supplierNameString);
        values.put(BookEntry.COLUMN_SUPPLIER_PHONE_NUMBER, supplierPhoneString);

        if (mCurrentBookUri == null) {
            // insert new book
            Uri newUri = getContentResolver().insert(BookEntry.CONTENT_URI, values);

            // if insertion is successful then newUri should be made, if null, show error toast
            if (newUri == null) {
                Toast.makeText(this, R.string.error, Toast.LENGTH_LONG).show();
            } else
                Toast.makeText(this, R.string.saved, Toast.LENGTH_LONG).show();

            // if there is a value for mCurrentBookUri, then this is an existing entry
            // and the contentValues need to be updated instead of inserted
        } else {
            int rowsAffected = getContentResolver().update(mCurrentBookUri, values, null, null);
            if (rowsAffected == 0) {
                Toast.makeText(this, R.string.problem_updating, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, R.string.book_updated, Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
        // Since the editor shows all pet attributes, define a projection that contains
        // all columns from the pet table
        String[] projection = {
                BookEntry._ID,
                BookEntry.COLUMN_PRODUCT_NAME,
                BookEntry.COLUMN_PRODUCT_TYPE,
                BookEntry.COLUMN_PRICE,
                BookEntry.COLUMN_QUANTITY,
                BookEntry.COLUMN_SUPPLIER_NAME,
                BookEntry.COLUMN_SUPPLIER_PHONE_NUMBER};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,
                BookEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        if (cursor.moveToFirst()) {
            int titleColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_PRODUCT_NAME);
            int typeColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_PRODUCT_TYPE);
            int priceColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_QUANTITY);
            int supplierNameColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_SUPPLIER_NAME);
            int supplierNumberColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_SUPPLIER_PHONE_NUMBER);

            String title = cursor.getString(titleColumnIndex);
            int type = cursor.getInt(typeColumnIndex);
            double price = cursor.getDouble(priceColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            String supplierName = cursor.getString(supplierNameColumnIndex);
            String supplierNumber = cursor.getString(supplierNumberColumnIndex);

            mTitleEditText.setText(title);
            mPriceEditText.setText(Double.toString(price));
            mQuantityEditText.setText(Integer.toString(quantity));
            mSupplierNameEditText.setText(supplierName);
            mSupplierPhoneEditText.setText(supplierNumber);

            switch (type) {
                case BookEntry.HARDBACK:
                    mTypeSpinner.setSelection(0);
                    break;
                case BookEntry.PAPERBACK:
                    mTypeSpinner.setSelection(1);
                    break;
                case BookEntry.EBOOK:
                    mTypeSpinner.setSelection(2);
                    break;
            }
        }
        Log.i(TAG, "onFinishedLoading current uri" + mCurrentBookUri);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mTitleEditText.setText("");
        mTypeSpinner.setSelection(0);
        mPriceEditText.setText(Double.toString(DEFAULT_PRICE));
        mQuantityEditText.setText(Integer.toString(DEFAULT_QUANTITY));
        mSupplierNameEditText.setText("");
        mSupplierPhoneEditText.setText("");

        Log.i(TAG, "onLoaderReset current uri" + mCurrentBookUri);
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // creates an alertDialog,builder, sets the message and the listener
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.leave_wout_changing);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                // user wants to keep editing so dismiss dialog
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // create and show alertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.confirm_delete);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the pet.
                deleteBook();
            }
        });
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
    public void deleteBook() {
        if (mCurrentBookUri != null) {
            int rowsDeleted = getContentResolver().delete(mCurrentBookUri, null, null);
            if (rowsDeleted == 0) {
                Toast.makeText(this, R.string.problem_deleting, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, R.string.book_deleted, Toast.LENGTH_LONG).show();
            }
        }
        finish();
    }

    public void onBackPressed() {
        // if the user hasn't made any changes then proceed with implementing back button press
        if (mBookHasChanged) {
            super.onBackPressed();
            return;
        }
        // if there are unsaved changes then warn the user and use the dialogInterface
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }


}


