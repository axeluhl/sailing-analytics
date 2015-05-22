package com.sap.sailing.racecommittee.app.ui.fragments.dialogs;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.DatePicker;
import com.sap.sailing.racecommittee.app.R;

public class DatePickerFragment extends LoggableDialogFragment implements DatePickerDialog.OnDateSetListener,
        DatePickerDialog.OnDismissListener {
    
    private DatePickerDialog.OnDateSetListener listener;
    private int currentYear;
    private int currentMonth;
    private int currentDay;
    
    public void setOnDateSetListener(DatePickerDialog.OnDateSetListener listener) {
        this.listener = listener;
    }

    public int getCurrentYear() {
        return currentYear;
    }

    public void setCurrentYear(int currentYear) {
        this.currentYear = currentYear;
    }

    public int getCurrentMonth() {
        return currentMonth;
    }

    public void setCurrentMonth(int currentMonth) {
        this.currentMonth = currentMonth;
    }

    public int getCurrentDay() {
        return currentDay;
    }

    public void setCurrentDay(int currentDay) {
        this.currentDay = currentDay;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // There is a bug in DatePickerDialog preventing it from correctly passing its onDateSet and onDismiss event
        // See https://code.google.com/p/android/issues/detail?id=34833
        // Therefore we will do all the event handling on our own.
        DatePickerDialog dialog = new DatePickerDialog(getActivity(), null, currentYear, currentMonth, currentDay);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.choose), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                DatePickerDialog dialog = (DatePickerDialog) dialogInterface;
                DatePicker picker = dialog.getDatePicker();
                DatePickerFragment.this.onDateSet(picker, picker.getYear(), picker.getMonth(), picker.getDayOfMonth());
            }
        });
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                DatePickerFragment.this.onDismiss(dialogInterface);
            }
        });
        return dialog;
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int day) {
        this.currentYear = year;
        this.currentMonth = month;
        this.currentDay = day;
        if (listener != null) {
            listener.onDateSet(view, year, month, day);
        }
    }

}
