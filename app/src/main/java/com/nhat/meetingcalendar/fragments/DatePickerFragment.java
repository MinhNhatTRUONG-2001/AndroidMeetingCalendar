package com.nhat.meetingcalendar.fragments;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.DatePicker;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;

import com.nhat.meetingcalendar.R;

import java.util.Calendar;

public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //Here we use Calendar to set the current time as the default values for the time picker
        final Calendar today = Calendar.getInstance();
        int day = today.get(Calendar.DAY_OF_MONTH);
        int month = today.get(Calendar.MONTH);
        int year = today.get(Calendar.YEAR);
        //Here we create and return a new instance of TimePickerDialog
        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    //Write chosen date to "meeting_date_result" TextView
    @Override
    public void onDateSet(DatePicker datePickerView, int year, int month, int dayOfMonth) {
        TextView meetingDateResult = getActivity().findViewById(R.id.meeting_date_result);
        String meetingDateResultText = dayOfMonth + "/" + (month + 1) + "/" + year; //Calendar.JANUARY = 0, Calendar.FEBRUARY = 1,...
        meetingDateResult.setText(meetingDateResultText);
        meetingDateResult.setBackgroundColor(Color.TRANSPARENT);
    }
}
