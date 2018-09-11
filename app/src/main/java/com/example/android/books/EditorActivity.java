package com.example.android.books;


import android.content.ContentValues;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.android.books.data.BookContract.BookEntry;
import com.example.android.books.data.BooksDbHelper;

public class EditorActivity extends AppCompatActivity {

    private BooksDbHelper mDbHelper;

    // EditText fields and spinner for book info
    private EditText mTitleEditText;
    private Spinner mTypeSpinner;
    private EditText mPriceEditText;
    private EditText mQuantityEditText;
    private EditText mSupplierNameEditText;
    private EditText mSupplierPhoneEditText;

    // set default book type to 0/hardback (other possibilities are 1=paperback, 2=ebook)
    private int mType = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Instantiate the dbHelper
        mDbHelper = new BooksDbHelper(this);

        // Find all relevant views that we will need to read user input from
        mTitleEditText = (EditText) findViewById(R.id.title);
        mTypeSpinner = (Spinner) findViewById(R.id.spinner_type);
        mPriceEditText = (EditText) findViewById(R.id.edit_price);
        mQuantityEditText = (EditText) findViewById(R.id.edit_quantity);
        mSupplierNameEditText = (EditText) findViewById(R.id.edit_supplier_name);
        mSupplierPhoneEditText = (EditText) findViewById(R.id.edit_supplier_phone);

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

    // Handle user clicks on menu item
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // if user selects save option
            case R.id.action_save:
                // save user input to database
                insertBook();
                // exit editorActivity and return to mainActivity
                finish();
                return true;
            // if user selects EXIT then finish and return to mainActivity w/out saving
            case R.id.action_exit:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Get user input from edit text fields and spinner, then insert that data into the table
    private void insertBook()

    {
        String titleString = mTitleEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        double price = Double.parseDouble(priceString);
        String quantityString = mQuantityEditText.getText().toString().trim();
        int quantity = Integer.parseInt(quantityString);
        String supplierNameString = mSupplierNameEditText.getText().toString().trim();
        String supplierPhoneString = mSupplierPhoneEditText.getText().toString().trim();

        // Create key value pairs that will be inserted into table
        ContentValues values = new ContentValues();
        values.put(BookEntry.COLUMN_PRODUCT_NAME, titleString);
        values.put(BookEntry.COLUMN_PRODUCT_TYPE, mType);
        values.put(BookEntry.COLUMN_PRICE, price);
        values.put(BookEntry.COLUMN_QUANTITY, quantity);
        values.put(BookEntry.COLUMN_SUPPLIER_NAME, supplierNameString);
        values.put(BookEntry.COLUMN_SUPPLIER_PHONE_NUMBER, supplierPhoneString);

        Uri newUri = getContentResolver().insert(BookEntry.CONTENT_URI, values);

        if (newUri == null) {
            Toast.makeText(this, R.string.error, Toast.LENGTH_LONG).show();
        } else
            Toast.makeText(this, R.string.saved, Toast.LENGTH_LONG).show();
    }
}

