package com.example.management;

import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.strictmode.SqliteObjectLeakedViolation;
import android.view.View;
import android.widget.DatePicker;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
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
    private TextView docDate;
    private TextView docNumber;

    private Document document;
    private boolean documentIn;
    private boolean newDocument;
    public ArrayList<String[]> itemList;
    private ItemRecyclerAdapter itemAdapter;
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
        documentIn = intent.getBooleanExtra(getString(R.string.intent_document_in), true);
        REQUEST_CODE = newDocument ? DocFragment.REQUEST_CODE_NEW_DOCUMENT : DocFragment.REQUEST_CODE_CURRENT_DOCUMENT;

        if (newDocument) {

            Date currentDate = new Date();
            SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy HH:mm");
            String strDate = format.format(currentDate);
            docDate.setText(strDate);
            swOutDoc.setChecked(false);/*In as default*/
            itemList = new ArrayList<>();

        } else {

            document = intent.getParcelableExtra(getString(R.string.intent_document));
            docNumber.setText(String.valueOf(document.getNumber()));
            docDate.setText(document.getDateAsStringType());
            itemList = document.getTableList();
            swOutDoc.setChecked(!documentIn);

        }
        fillTable();

        /*DELETE ROW ON SWIPE (to right)*/
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                deleteItemFromList(viewHolder.getAdapterPosition());
            }
        }).attachToRecyclerView(recyclerView);

    }

    private void fillTable() {

        recyclerView = findViewById(R.id.rv_doc_table);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(DocItemActivity.this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);

        itemAdapter = new ItemRecyclerAdapter(DocItemActivity.this, itemList);
        recyclerView.setAdapter(itemAdapter);

    }

    public void onClickSaveDocument(View v) {

        if (itemList.size() < 1) {

            Toast.makeText(this, "Таблица товаров пуста!", Toast.LENGTH_SHORT).show();
            return;

        } else {
            for (String[] itemLine : itemList) {

                if (itemLine[0] == null || itemLine[0].isEmpty()) {
                    Toast.makeText(this, "В таблице не указаны товары!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (itemLine[1] == null || itemLine[1].isEmpty() || Integer.parseInt(itemLine[1]) <= 0) {
                    Toast.makeText(this, "В таблице количество товаров указаны неверно!", Toast.LENGTH_SHORT).show();
                    return;
                }

            }
        }

        documentIn = !swOutDoc.isChecked();
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

            String tableQuery = "items";
            String[] columns = {"item", "_id"};
            String selection = "item = ?";
            String[] selectionArgs = new String[1];

            for (String[] strLine : itemList) {

                selectionArgs[0] = strLine[0];
                cursor = database.query(tableQuery, columns, selection, selectionArgs, null, null, null);

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

            String resString = "Документ сохранен!";
            if (intResult == 0) {
                for (String[] itemLine : itemList) {
                    itemLine[2] = "";
                }
                resString = "В таблице присутствуют товары, которых нет в базе данных!";
            } else {
                saveDocument();
            }

            Toast.makeText(getApplicationContext(), resString, Toast.LENGTH_SHORT).show();
        }

    }

    private void saveDocument() {

        String dateString = docDate.getText().toString();

        int dateInt = Document.getDateOrTimeAsInt(dateString, true);
        int timeInt = Document.getDateOrTimeAsInt(dateString, false);

        SQLiteDB sqLiteDB = new SQLiteDB(this);
        SQLiteDatabase database = sqLiteDB.getReadableDatabase();

        if (newDocument) {/*Create new*/

            newDocument = false;

            int newNumber = getNewNumber();
            document = new Document(newNumber, dateInt, timeInt, documentIn, itemList);

        } else {/*delete*/

            document.setDate(dateInt);
            document.setTime(timeInt);
            document.setTableList(itemList);
            database.delete("documents", "number = " + document.getNumber(), null);

        }

        // add
        ContentValues contentValues = new ContentValues();
        for (String[] strLine : itemList) {

            contentValues.put(SQLiteDB.KEY_NUMBER, document.getNumber());
            contentValues.put(SQLiteDB.KEY_DATE, document.getDate());
            contentValues.put(SQLiteDB.KEY_TIME, document.getTime());
            contentValues.put(SQLiteDB.KEY_ITEM_ID, Integer.parseInt(strLine[2]));
            contentValues.put(SQLiteDB.KEY_COUNT, documentIn ? Integer.parseInt(strLine[1]) : -1 * Integer.parseInt(strLine[1]));
            database.insert(SQLiteDB.TABLE_DOC, null, contentValues);

        }

        sqLiteDB.close();
        database.close();/* Here. Update list in DocFragment*/

        Intent intent = new Intent();
        intent.putExtra(getString(R.string.intent_document), document);
        setResult(REQUEST_CODE,intent);

    }

    private int getNewNumber() {

        int lastNumber = 0;

        SQLiteDB sqLiteDB = new SQLiteDB(this);
        SQLiteDatabase database = sqLiteDB.getReadableDatabase();

        String tableQuery = "documents";
        String[] columns = {"number"};
        Cursor cursor = database.query(tableQuery, columns, null, null, null, null, "number DESC","1");

        if (cursor.moveToFirst()) {
            int nIndex = cursor.getColumnIndex("number");
            lastNumber = cursor.getInt(nIndex);
        }

        cursor.close();
        sqLiteDB.close();

        return lastNumber + 1;

    }


    public void onClickAddItem(View v) {
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
