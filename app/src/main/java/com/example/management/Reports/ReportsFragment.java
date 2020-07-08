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
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.management.Document;
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
    private Button formButton;
    private TextView date1, tvBetween, date2;
    private EditText etSearchReport;
    private RecyclerView recyclerView;
    private ReportRecyclerAdapter reportAdapter;

    private boolean date1Clicked;
    private boolean reportType;/*true - Remaining, false - Transaction*/
    private boolean formButtonClicked;
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

        SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
        if (argDate2 == null) {
            Date date = new Date();
            dateString2 = format.format(date);
        } else {
            dateString2 = argDate1;
        }

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
                date1.setVisibility(View.GONE);
                tvBetween.setVisibility(View.GONE);
            } else {
                date1.setVisibility(View.VISIBLE);
                tvBetween.setVisibility(View.VISIBLE);
            }

        });
        formButton.setOnClickListener(v -> {
            formButtonClicked = true;
            formReport();

        });
        etSearchReport.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {

                if (s.toString().trim().isEmpty()) {
                    s.clear();
                    return;
                } else if (!formButtonClicked) {
                    return;
                }

                formReport();
                reportAdapter.notifyDataSetChanged();

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

        reportList.clear();

        String etString = etSearchReport.getText().toString().trim();
        if (etString.length() < 2) {
            etString = "";
        }

        if (reportType) {
            reportRemaining(etString);
        } else {
            reportTransaction(etString);
        }
        reportAdapter.notifyDataSetChanged();

    }

    private void reportRemaining(String selectedString) {

        SQLiteDB sqLiteDB = new SQLiteDB(getContext());
        SQLiteDatabase database = sqLiteDB.getReadableDatabase();

        String tableQuery = "documents as doc inner join items as it on doc.item = it._id";
        String[] columns = {"it.item AS Item", "SUM(count) AS Count"};
        String selections = "doc.date <= ? AND it.item LIKE ?";
        String[] selectionsArgs = new String[] {argDate2, "%" + selectedString + "%"};
        Cursor cursor = database.query(tableQuery, columns, selections, selectionsArgs, "it.item", null, "item");

        if (cursor.moveToFirst()) {

            int itemIndex = cursor.getColumnIndex("Item");
            int countIndex = cursor.getColumnIndex("Count");

            String itemString, countString;

            do {
                itemString = cursor.getString(itemIndex);
                countString = String.valueOf(cursor.getInt(countIndex));
                reportList.add(new String[]{itemString, "", countString});

            } while (cursor.moveToNext());

        }
        cursor.close();
        sqLiteDB.close();

    }

    private void reportTransaction(String selectedString) {

        SQLiteDB sqLiteDB = new SQLiteDB(getContext());
        SQLiteDatabase database = sqLiteDB.getReadableDatabase();

        String sqlString = "SELECT item, SUM(iSum) AS iSum, SUM(oSum) AS oSum"
                +" FROM (SELECT it.item, SUM(count) AS iSum, 0 AS oSum"
                +" FROM (SELECT item, count FROM documents WHERE date BETWEEN ? AND ? AND count > 0) AS doc LEFT JOIN items AS it"
                +" ON doc.item = it._id"
                +" GROUP BY it.item"
                +" UNION ALL"
                +" SELECT it.item, 0 AS iSum, SUM(cplus) AS oSum"
                +" FROM (SELECT item, count * -1 AS cplus FROM documents WHERE date BETWEEN ? AND ? AND count < 0) AS doc LEFT JOIN items AS it"
                +" ON doc.item = it._id"
                +" GROUP BY it.item)"
                +" WHERE item LIKE ?"
                +" GROUP BY item";
        String[] selectionArgs = new String[]{argDate1, argDate2, argDate1, argDate2, "%" + selectedString + "%"};
        Cursor cursor = database.rawQuery(sqlString, selectionArgs);

        if (cursor.moveToFirst()) {

            int itemIndex = cursor.getColumnIndex("item");
            int iSumIndex = cursor.getColumnIndex("iSum");
            int oSumIndex = cursor.getColumnIndex("oSum");

            String itemString, iSumString, oSumString;

            do {

                itemString = cursor.getString(itemIndex);
                iSumString = String.valueOf(cursor.getInt(iSumIndex));
                oSumString = String.valueOf(cursor.getInt(oSumIndex));
                reportList.add(new String[]{itemString, iSumString ,oSumString});

            } while (cursor.moveToNext());

        }
        cursor.close();
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
