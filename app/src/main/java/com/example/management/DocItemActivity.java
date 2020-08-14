package com.example.management;

import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class DocItemActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    private int REQUEST_CODE;
    private Switch swOutDoc;
    private TextView docDate, docNumber;

    private Document document;
    private boolean documentIn, newDocument;
    public ArrayList<String[]> itemList;
    private DocItemsRecyclerAdapter itemAdapter;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.open_document);

        init();

    }

    private void init() {

        swOutDoc = findViewById(R.id.sw_in_out);
        docDate = findViewById(R.id.tv_open_date);
        docNumber = findViewById(R.id.tv_open_number);

        Intent intent = getIntent();
        newDocument = !intent.hasExtra(getString(R.string.intent_document));
        documentIn = intent.getBooleanExtra("documentIn", true);
        REQUEST_CODE = newDocument ? DocFragment.REQUEST_CODE_NEW_DOCUMENT : DocFragment.REQUEST_CODE_CURRENT_DOCUMENT;

        if (newDocument) {

            Date currentDate = new Date();
            SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy HH:mm");
            String strDate = format.format(currentDate);
            docDate.setText(strDate);
            swOutDoc.setChecked(true);/*In as default*/
            itemList = new ArrayList<>();

        } else {

            document = intent.getParcelableExtra(getString(R.string.intent_document));
            docNumber.setText(String.valueOf(document.getNumber()));
            docDate.setText(document.getDateAsStringType());
            itemList = document.getTableList();
            swOutDoc.setChecked(documentIn);

        }
        fillTable();

        /*DELETE ROW ON SWIPE (to right)*/
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) { return false; }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) { deleteItemFromList(viewHolder.getAdapterPosition()); }
        }).attachToRecyclerView(recyclerView);

    }

    private void fillTable() {

        recyclerView = findViewById(R.id.rv_doc_table);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(DocItemActivity.this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);

        itemAdapter = new DocItemsRecyclerAdapter(DocItemActivity.this, itemList);
        recyclerView.setAdapter(itemAdapter);

    }

    /*Save document*/
    public void onClickSaveDocument(View v) {

        if (itemList.size() < 1) {

            Toast.makeText(this, R.string.alert_table_is_empty, Toast.LENGTH_SHORT).show();
            return;

        } else {

            int itemCount;
            for (String[] itemLine : itemList) {

                if (itemLine[0] == null || itemLine[0].isEmpty()) {
                    Toast.makeText(this, R.string.alert_no_items_in_table, Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    itemCount = Integer.parseInt(itemLine[1]);
                } catch (NumberFormatException e) {
                    Toast.makeText(this, R.string.alert_count_is_too_big, Toast.LENGTH_SHORT).show();
                    return;
                }

                if (itemLine[1] == null || itemLine[1].isEmpty() || itemCount <= 0) {
                    Toast.makeText(this, R.string.alert_incorrect_quantity, Toast.LENGTH_SHORT).show();
                    return;
                }

            }
        }

        documentIn = swOutDoc.isChecked();
        new saveDocumentTask().execute();
    }

    class saveDocumentTask extends AsyncTask<Void, Void, Integer> {

        @Override
        protected Integer doInBackground(Void... voids) {

            int idIndex;
            Cursor cursor = null;
            Integer result = 1;

            SQLiteDB sqLiteDB = new SQLiteDB(getApplicationContext());
            SQLiteDatabase database = sqLiteDB.getWritableDatabase();

            String selection = "item = ?";
            String[] selectionArgs = new String[1];

            for (String[] strLine : itemList) {

                selectionArgs[0] = strLine[0];
                cursor = database.query(SQLiteDB.TABLE_ITEM, null, selection, selectionArgs, null, null, null);

                if (cursor.moveToFirst()) {
                    idIndex = cursor.getColumnIndex("_id");
                    strLine[2] = String.valueOf(cursor.getInt(idIndex));
                } else {
                    result = 0;
                    break;
                }

            }

            sqLiteDB.close();
            cursor.close();

            return result;

        }

        @Override
        protected void onPostExecute(Integer intResult) {

            String resString;
            if (intResult == 0) {
                for (String[] itemLine : itemList) {
                    itemLine[2] = "";
                }
                resString = getString(R.string.alert_item_not_in_database);
            } else {
                saveDocument();
                resString = getString(R.string.toast_doc_saved);
            }

            Toast.makeText(getApplicationContext(), resString, Toast.LENGTH_SHORT).show();
        }

    }

    private void saveDocument() {

        String dateString = docDate.getText().toString();

        int dateInt = Document.getDateOrTimeAsInt(dateString, true);
        int timeInt = Document.getDateOrTimeAsInt(dateString, false);

        SQLiteDB sqLiteDB = new SQLiteDB(this);
        SQLiteDatabase database = sqLiteDB.getWritableDatabase();

        if (newDocument) {/*Create new*/

            newDocument = false;

            int newNumber = getNewNumber();
            docNumber.setText(String.valueOf(newNumber));
            document = new Document(newNumber, dateInt, timeInt, documentIn, itemList);

        } else {/*delete*/

            int result = database.delete(SQLiteDB.TABLE_DOC, SQLiteDB.KEY_NUMBER + " = " + document.getNumber(), null);
            if (result < 1) {
                sqLiteDB.close();
                Toast.makeText(this, R.string.alert_item_save_changes, Toast.LENGTH_SHORT).show();
                return;
            }

            document.setDate(dateInt);
            document.setTime(timeInt);
            document.setDocumentIn(documentIn);
            document.setTableList(itemList);

        }

        // add
        int itemCount;
        ContentValues contentValues = new ContentValues();
        database.beginTransaction();
        for (String[] strLine : itemList) {

            itemCount = Integer.parseInt(strLine[1]);

            if (!documentIn) {
                itemCount = -1 * itemCount;
            }

            contentValues.put(SQLiteDB.KEY_NUMBER, document.getNumber());
            contentValues.put(SQLiteDB.KEY_DATE, document.getDate());
            contentValues.put(SQLiteDB.KEY_TIME, document.getTime());
            contentValues.put(SQLiteDB.KEY_ITEM_ID, Integer.parseInt(strLine[2]));
            contentValues.put(SQLiteDB.KEY_COUNT, itemCount);
            database.insert(SQLiteDB.TABLE_DOC, null, contentValues);

        }

        database.setTransactionSuccessful();
        database.endTransaction();
        sqLiteDB.close();

        Intent intent = new Intent();
        intent.putExtra(getString(R.string.intent_document), document);
        setResult(REQUEST_CODE, intent);

    }

    private int getNewNumber() {

        int lastNumber = 0;

        SQLiteDB sqLiteDB = new SQLiteDB(this);
        SQLiteDatabase database = sqLiteDB.getReadableDatabase();

        Cursor cursor = database.query(SQLiteDB.TABLE_DOC, new String[]{"number"}, null, null, null, null, "number DESC","1");

        if (cursor.moveToFirst()) {
            int nIndex = cursor.getColumnIndex("number");
            lastNumber = cursor.getInt(nIndex);
        }

        cursor.close();
        sqLiteDB.close();

        return lastNumber + 1;

    }


    /*Buttons*/
    public void onDocClickAddItem(View v) {
        itemList.add(new String[3]);
        itemAdapter.notifyDataSetChanged();
    }

    public void onClickDate(View v) {
        DialogFragment datePickerFragment = new DatePickerFragment();
        datePickerFragment.show(getSupportFragmentManager(), "date_picker");
    }


    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        String dayString;
        String monthString;

        dayString = String.valueOf(dayOfMonth);
        if (dayOfMonth < 10) {
            dayString = "0" + dayString;
        }

        monthString = String.valueOf(month+1);
        if (month < 9) {
            monthString = "0" + monthString;
        }

        docDate.setText(dayString + "." + monthString + "." + year + " 00:00");

    }

    private void deleteItemFromList(int position) {
        itemList.remove(position);
        itemAdapter.notifyDataSetChanged();
    }

}
