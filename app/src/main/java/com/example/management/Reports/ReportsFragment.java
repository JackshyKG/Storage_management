package com.example.management.Reports;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.management.DocFragment;
import com.example.management.Document;
import com.example.management.ItemRecyclerAdapter;
import com.example.management.PeriodPickerFragment;
import com.example.management.R;
import com.example.management.SQLiteDB;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class ReportsFragment extends Fragment {

    public static final int REQUEST_CODE_DATE_PICKER = 14;

    private Switch swReportType;
    private ImageButton formButton;
    private TextView date1, tvBetween, date2;
    private EditText etSearchReport;
    private RecyclerView recyclerView;
    private ReportRecyclerAdapter reportAdapter;

    private boolean date1Clicked;
    private boolean reportType;/*true - Remaining, false - Transaction*/
    private String argDate1, argDate2;
    private ArrayList<String[]> reportList;/*0 - item, 1 - count income, 2 - count outgo(or remaining)*/

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.reports_recycler_view, container, false);
        date1 = rootView.findViewById(R.id.tv_report_date_1);
        date2 = rootView.findViewById(R.id.tv_report_date_2);
        tvBetween = rootView.findViewById(R.id.tv_between_dates);
        swReportType = rootView.findViewById(R.id.sw_report_type);
        formButton = rootView.findViewById(R.id.ib_report);
        etSearchReport = rootView.findViewById(R.id.et_search_report);
        recyclerView = rootView.findViewById(R.id.rv_reports);


        try {
            initDates();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        init();

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

    private void init() {

        reportType = false;
        reportList = new ArrayList<>();

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        reportAdapter = new ReportRecyclerAdapter(getActivity(), reportList, reportType);
        recyclerView.setAdapter(reportAdapter);

        swReportType.setOnCheckedChangeListener((buttonView, isChecked) -> {

            reportType = isChecked;

            if (reportType) {
                date2.setVisibility(View.INVISIBLE);
                tvBetween.setVisibility(View.INVISIBLE);
            } else {
                date2.setVisibility(View.VISIBLE);
                tvBetween.setVisibility(View.VISIBLE);
            }

        });
        formButton.setOnClickListener(v -> {
            formReport();
        });
        etSearchReport.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                if (before > count && s.length() < 2) {
//                    fillReportList("");
//                    reportAdapter.notifyDataSetChanged();
//                }
            }

            @Override
            public void afterTextChanged(Editable s) {

//                if (s.toString().trim().isEmpty()) {
//                    s.clear();
//                }
//
//                String str = s.toString();
//                if (str.trim().length() > 1) {
//                    fillReportList(str);
//                    reportAdapter.notifyDataSetChanged();
//                }

            }

        });

    }

    private void openDatePicker() {

        final FragmentManager fragmentManager = getActivity().getSupportFragmentManager();

        AppCompatDialogFragment newFragment = new PeriodPickerFragment();
        newFragment.setTargetFragment(ReportsFragment.this, REQUEST_CODE_DATE_PICKER);
        newFragment.show(fragmentManager, "datePicker");

    }

    private void formReport() {

        if (reportType) {
            reportRemaining(etSearchReport.toString().trim());
        } else {
            reportTransaction(etSearchReport.toString().trim());/*Jack. EditText*/
        }
        reportAdapter.notifyDataSetChanged();

    }

    private void reportRemaining(String selectedString) {

        SQLiteDB sqLiteDB = new SQLiteDB(getContext());
        SQLiteDatabase database = sqLiteDB.getReadableDatabase();

        String tableQuery = "documents as doc inner join items as it on doc.item = it._id";
        String[] columns = {"it.item", "SUM(count)"};
        String selections = "doc.date <= ?" + (selectedString.isEmpty() ? "" : " AND it.item LIKE " + selectedString);
        String[] selectionsArgs = new String[] {argDate1};
        Cursor cursor = database.query(tableQuery, columns, selections, selectionsArgs, "item", null, "item");

        if (cursor.moveToFirst()) {

            int itemIndex = cursor.getColumnIndex("item");
            int countIndex = cursor.getColumnIndex("count");

            String itemString = cursor.getString(itemIndex);
            String countString = String.valueOf(cursor.getInt(countIndex));
            reportList.add(new String[]{itemString,"",countString});

            do {
                itemString = cursor.getString(itemIndex);
                countString = String.valueOf(cursor.getInt(countIndex));
                reportList.add(new String[]{itemString, "", countString});

            } while (cursor.moveToNext());

        }

        sqLiteDB.close();

    }

    private void reportTransaction(String selectedString) {

        SQLiteDB sqLiteDB = new SQLiteDB(getContext());
        SQLiteDatabase database = sqLiteDB.getReadableDatabase();

        /* Bad query
        SELECT income.item, IFNULL(isum, 0) AS resisum, IFNULL(osum, 0) AS resosum

FROM (SELECT it.item, SUM(IFNULL(count,0)) AS isum
    FROM items AS it LEFT JOIN (SELECT item, count FROM documents WHERE date BETWEEN ? AND ? AND count > 0) AS doc
    ON it._id = doc.item
    GROUP BY it.item) income

LEFT JOIN (SELECT osum, item

FROM (SELECT it.item, SUM(IFNULL(cplus,0)) AS osum
    FROM items AS it LEFT JOIN (SELECT item, count*-1 AS cplus FROM documents WHERE date BETWEEN ? AND ? AND count < 0) AS doc
    ON it._id = doc.item
    GROUP BY it.item)) outgo ON income.item = outgo.item
    WHERE resisum > 0 OR resosum > 0*/
        String sqlString = "SELECT item, SUM(iSum), SUM(oSum)"
                +" FROM (SELECT it.item, SUM(count) AS isum, 0 AS osum"
                +" FROM (SELECT item, count FROM documents WHERE date BETWEEN ? AND ? AND count > 0) AS doc LEFT JOIN items AS it"
                +" ON doc.item = it._id"
                +" GROUP BY it.item"
                +" UNION ALL"
                +" SELECT it.item, 0 AS isum, SUM(cplus) AS osum"
                +" FROM (SELECT item, count * -1 AS cplus FROM documents WHERE date BETWEEN ? AND ? AND count < 0) AS doc LEFT JOIN items AS it"
                +" ON doc.item = it._id"
                +" GROUP BY it.item)"
                +(selectedString.isEmpty() ? "" : " WHERE item LIKE " + selectedString)
                +" GROUP BY item";
        String[] selectionArgs = new String[]{argDate1, argDate2, argDate1, argDate2};
        Cursor cursor = database.rawQuery(sqlString, selectionArgs);

        if (cursor.moveToFirst()) {

            int itemIndex = cursor.getColumnIndex("item");
            int iSumIndex = cursor.getColumnIndex("iSum");
            int oSumIndex = cursor.getColumnIndex("oSum");

            String itemString = cursor.getString(itemIndex);
            String iSumString = String.valueOf(cursor.getInt(iSumIndex));
            String oSumString = String.valueOf(cursor.getInt(oSumIndex));
            reportList.add(new String[]{itemString, iSumString ,oSumString});

            do {

                itemString = cursor.getString(itemIndex);
                iSumString = String.valueOf(cursor.getInt(iSumIndex));
                oSumString = String.valueOf(cursor.getInt(oSumIndex));
                reportList.add(new String[]{itemString, iSumString ,oSumString});

            } while (cursor.moveToNext());

        }

        sqLiteDB.close();

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_CODE_DATE_PICKER && resultCode == Activity.RESULT_OK) {

            if (date1Clicked) {
                date1.setText(data.getStringExtra("stringDate"));
                argDate1 = data.getStringExtra("argDate");
            } else {
                date2.setText(data.getStringExtra("stringDate"));
                argDate2 = data.getStringExtra("argDate");
            }

        }

    }

}
