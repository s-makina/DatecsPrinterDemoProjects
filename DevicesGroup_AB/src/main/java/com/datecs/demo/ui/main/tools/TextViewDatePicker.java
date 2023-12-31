/*
 * @author Datecs Ltd. Software Department
 */

package com.datecs.demo.ui.main.tools;

import android.app.DatePickerDialog;
import android.content.Context;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class TextViewDatePicker implements View.OnClickListener, DatePickerDialog.OnDateSetListener {
    public static String getDateServerPattern() {
        return DATE_SERVER_PATTERN;
    }

    public static void setDateServerPattern(String dateServerPattern) {
        DATE_SERVER_PATTERN = dateServerPattern;
    }

    private static String DATE_SERVER_PATTERN = "dd-MM-yy";

    private DatePickerDialog mDatePickerDialog;
    private TextView mView;
    private Context mContext;
    private long mMinDate;
    private long mMaxDate;


    public TextViewDatePicker(Context context, TextView view) {
        this(context, view, 0, 0);
    }

    public TextViewDatePicker(Context context, TextView view, long minDate, long maxDate) {
        mView = view;
        mView.setOnClickListener(this);
        mView.setFocusable(false);
        mContext = context;
        mMinDate = minDate;
        mMaxDate = maxDate;

    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, monthOfYear);
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        Date date = calendar.getTime();

        SimpleDateFormat formatter = new SimpleDateFormat(DATE_SERVER_PATTERN);
        mView.setText(formatter.format(date));
    }

    @Override
    public void onClick(View v) {
        try {
            String startDate=mView.getText().toString();
            Date date1=new SimpleDateFormat(DATE_SERVER_PATTERN).parse(startDate);

            Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
            calendar.setTime(date1);
            mDatePickerDialog = new DatePickerDialog(mContext,
                    this,
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (mMinDate != 0) {
            mDatePickerDialog.getDatePicker().setMinDate(mMinDate);
        }
        if (mMaxDate != 0) {
            mDatePickerDialog.getDatePicker().setMaxDate(mMaxDate);
        }
        mDatePickerDialog.show();
    }
    public DatePickerDialog getDatePickerDialog() {
        return mDatePickerDialog;
    }
    public void setMinDate(long minDate) {
        mMinDate = minDate;
    }
    public void setMaxDate(long maxDate) {
        mMaxDate = maxDate;
    }

}