package com.example.management;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class DocFragment extends Fragment {

    public static final int REQUEST_CODE_DATE_PICKER = 11;
    public static final int REQUEST_CODE_NEW_DOCUMENT = 12;
    public static final int REQUEST_CODE_CURRENT_DOCUMENT = 13;

    private Button bAdd, bShow;
    private boolean date1Clicked;
    private TextView date1, date2;
    private String argDate1, argDate2;

    private ArrayList<Document> docList;
    private RecyclerView recyclerView;
    private DocRecyclerAdapter docAdapter;

    private int currentDocumentPosition;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.doc_recycler_view, container, false);
        recyclerView = rootView.findViewById(R.id.rv_documents);

        date1 = rootView.findViewById(R.id.tv_date_1);
        date2 = rootView.findViewById(R.id.tv_date_2);
        try {
            initDates();
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }

        bAdd = rootView.findViewById(R.id.b_add_document);
        bShow = rootView.findViewById(R.id.b_show);

        docList = new ArrayList<>();

        initButtons();
        fillDocList();

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        docAdapter = new DocRecyclerAdapter(getActivity(), docList);
        recyclerView.setAdapter(docAdapter);
        recViewOnClickCommands();
        return rootView;

    }

    private void initDates() throws ParseException {

        String dateString1;
        String dateString2;

        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
        dateString2 = format.format(date);

        Calendar c = Calendar.getInstance();
        c.setTime(format.parse(dateString2));
        c.add(Calendar.DATE, -30);
        dateString1 = format.format(c.getTime());

        date1.setText(dateString1);
        date2.setText(dateString2);

        argDate1 = String.valueOf(Document.getDateOrTimeAsInt(dateString1 + " 00:00", true));
        argDate2 = String.valueOf(Document.getDateOrTimeAsInt(dateString2 + " 00:00", true));

        date1.setOnClickListener(v -> {
            date1Clicked = true;
            openDatePicker();
        });
        date2.setOnClickListener(v -> {
            date1Clicked = false;
            openDatePicker();
        });

    }

    private void initButtons() {

        bAdd.setOnClickListener(v -> {
            addDocument();
        });
        bShow.setOnClickListener(v -> {
//            SQLiteDB sqLiteDB = new SQLiteDB(getContext());
//            SQLiteDatabase database = sqLiteDB.getWritableDatabase();
//            ContentValues cv = new ContentValues();
//            cv.put(SQLiteDB.KEY_ITEM,"Хлеб");
//            database.insert(SQLiteDB.TABLE_ITEM, null, cv);
//            cv.put(SQLiteDB.KEY_ITEM,"Соль");
//            database.insert(SQLiteDB.TABLE_ITEM, null, cv);
//            cv.put(SQLiteDB.KEY_ITEM,"Сахар");
//            database.insert(SQLiteDB.TABLE_ITEM, null, cv);
//            sqLiteDB.close();
            fillDocList();
            docAdapter.notifyDataSetChanged();

        });
    }

    private void openDatePicker() {

        final FragmentManager fragmentManager = getActivity().getSupportFragmentManager();

        AppCompatDialogFragment newFragment = new PeriodPickerFragment();
        newFragment.setTargetFragment(DocFragment.this, REQUEST_CODE_DATE_PICKER);
        newFragment.show(fragmentManager, "datePicker");

    }



    private void fillDocList() {

        docList.clear();
        SQLiteDB sqLiteDB = new SQLiteDB(getContext());
        SQLiteDatabase database = sqLiteDB.getReadableDatabase();

        String tableQuery = "documents as DOC inner join items as IT on DOC.item = IT._id";
        String[] columns = {"number as Number", "date as Date", "IT.item as Item", "count as Count", "time as Time"};
        String selections = "Date BETWEEN ? AND ?";
        String[] selectionsArgs = new String[] {argDate1, argDate2};
        Cursor cursor = database.query(tableQuery, columns, selections, selectionsArgs, null, null, "Number");

        if (cursor.moveToFirst()) {

            ArrayList<String[]> tableList = new ArrayList<>();

            int nIndex = cursor.getColumnIndex("Number");
            int dIndex = cursor.getColumnIndex("Date");
            int tIndex = cursor.getColumnIndex("Time");
            int iIndex = cursor.getColumnIndex("Item");
            int cIndex = cursor.getColumnIndex("Count");

            int number = cursor.getInt(nIndex);
            int date = cursor.getInt(dIndex);
            int time = cursor.getInt(tIndex);

            /* To identify the type of document*/
            int count = cursor.getInt(cIndex);
            boolean documentIn = (count > 0);

            do {
                if (number != cursor.getInt(nIndex)) {

                    docList.add(new Document(number, date, time, documentIn, tableList));

                    number = cursor.getInt(nIndex);
                    date = cursor.getInt(dIndex);
                    time = cursor.getInt(tIndex);

                    /* To identify the type of document*/
                    count = cursor.getInt(cIndex);
                    documentIn = (count > 0);

                    tableList = new ArrayList<>();
                }

                String[] tableItem = new String[3];
                tableItem[0] = cursor.getString(iIndex);
                tableItem[1] = String.valueOf(documentIn ? cursor.getInt(cIndex) : -1 * cursor.getInt(cIndex));
                tableItem[2] = "";/* for id to fill on DocItemActivity -> onClickSaveDocument*/
                tableList.add(tableItem);

            } while (cursor.moveToNext());

            docList.add(new Document(number, date, time, documentIn, tableList));// last one

        }

        cursor.close();
        sqLiteDB.close();

    }

    private void recViewOnClickCommands() {

        recyclerView.addOnItemTouchListener(
                new DocRecyclerItemClickListener(getActivity(), (view, position) -> {

                    currentDocumentPosition = position;

                    Intent intent = new Intent(getContext(), DocItemActivity.class);
                    intent.putExtra(getString(R.string.intent_document), docList.get(position));
                    intent.putExtra(getString(R.string.intent_document_in), docList.get(position).getDocumentIn());
                    startActivityForResult(intent, REQUEST_CODE_CURRENT_DOCUMENT);

                })
        );

    }

    public void addDocument() {
        currentDocumentPosition = -1;
        Intent intent = new Intent(getActivity(), DocItemActivity.class);
        startActivityForResult(intent, REQUEST_CODE_NEW_DOCUMENT);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        /*on date picker*/
        if (requestCode == REQUEST_CODE_DATE_PICKER && resultCode == Activity.RESULT_OK) {

            if (date1Clicked) {
                date1.setText(data.getStringExtra("stringDate"));
                argDate1 = data.getStringExtra("argDate");
            } else {
                date2.setText(data.getStringExtra("stringDate"));
                argDate2 = data.getStringExtra("argDate");
            }

        /*on new document added*/
        } else if (requestCode == REQUEST_CODE_NEW_DOCUMENT && data != null  && data.hasExtra(getString(R.string.intent_document))) {

            Document document = data.getParcelableExtra(getString(R.string.intent_document));
            docList.add(document);
            docAdapter.notifyItemInserted(docList.size() - 1);

        /*on document edited*/
        } else if (requestCode == REQUEST_CODE_CURRENT_DOCUMENT && data != null && data.hasExtra(getString(R.string.intent_document))) {

            Document document = data.getParcelableExtra(getString(R.string.intent_document));
            docList.set(currentDocumentPosition, document);
            docAdapter.notifyItemChanged(currentDocumentPosition);

        }

    }

}





















