/*
 * @author Datecs Ltd. Software Department
 */

package com.datecs.demo.ui.main.tools;

import android.app.TimePickerDialog;
import android.content.Context;
import android.view.View;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.Calendar;

public class SetTime implements View.OnClickListener, TimePickerDialog.OnTimeSetListener {

    private static String timeSeparator = ":";

    private TextView editText;
    private Calendar myCalendar;
    private Context mContext;

    public SetTime(Context context, TextView view, String separator) {
        this(context, view);
        timeSeparator = separator;
    }

    public String getTimeSeparator() {
        return timeSeparator;
    }


    public SetTime(Context context, TextView view) {
        this.editText = view;
        this.editText.setOnClickListener(this);
        this.editText.setFocusable(false);
        this.myCalendar = Calendar.getInstance();
        mContext = context;
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub

        int hour = myCalendar.get(Calendar.HOUR_OF_DAY);
        int minute = myCalendar.get(Calendar.MINUTE);

        new TimePickerDialog(mContext, this, hour, minute, true).show();

    }


    private static String padLeft(String s) {
        String padded = "00".substring(s.length()) + s;
        return padded;
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        // TODO Auto-generated method stub

        String sHH = padLeft(String.valueOf(hourOfDay));
        String sMM = padLeft(String.valueOf(minute));
        this.editText.setText(sHH + timeSeparator + sMM + timeSeparator + "00");
    }
}