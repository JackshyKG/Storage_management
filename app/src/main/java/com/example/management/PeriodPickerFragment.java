package com.example.management;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.DatePicker;

import androidx.appcompat.app.AppCompatDialogFragment;

import java.util.Calendar;

public class PeriodPickerFragment extends AppCompatDialogFragment implements DatePickerDialog.OnDateSetListener {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        return new DatePickerDialog(getActivity(), PeriodPickerFragment.this, year, month, day);
    }

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

        String argDate = year + "" + monthString + dayString;
        String dateAsString = dayString + "." + monthString + "." + year;

        getTargetFragment().onActivityResult(
                getTargetRequestCode(),
                Activity.RESULT_OK,
                new Intent().putExtra(getString(R.string.m_date_as_string), dateAsString).putExtra(getString(R.string.m_argument_date), argDate)
        );
    }

}
