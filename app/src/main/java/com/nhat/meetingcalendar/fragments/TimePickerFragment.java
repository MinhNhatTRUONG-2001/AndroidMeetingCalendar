package com.nhat.meetingcalendar.fragments;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.fragment.app.DialogFragment;

import com.nhat.meetingcalendar.R;

import java.util.Calendar;

public class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        //Here we use Calendar to set the current time as the default values for the time picker
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        //Here we create and return a new instance of TimePickerDialog
        return new TimePickerDialog(getActivity(),this, hour, minute, DateFormat.is24HourFormat(getActivity()));
    }

    //Write chosen time to "meeting_time_result" TextView
    @Override
    public void onTimeSet(TimePicker timePickerView, int hourOfDay, int minute) {
        TextView meetingTimeResult = getActivity().findViewById(R.id.meeting_time_result);
        String meetingTimeResultText = hourOfDay + ":" + (minute < 10 ? "0" + minute : minute);
        meetingTimeResult.setText(meetingTimeResultText);
        meetingTimeResult.setBackgroundColor(Color.TRANSPARENT);
    }
}
