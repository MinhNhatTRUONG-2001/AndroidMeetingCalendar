package com.nhat.meetingcalendar.meeting_ds;

import android.widget.ImageView;

import java.io.Serializable;
import java.util.Calendar;

public class Meeting implements Serializable { //This implement is for sending meeting data to another activity
    private int id;
    private String title;
    private String place;
    private String[] participants;
    private Calendar datetime;
    private ImageView image;

    public Meeting(int id, String title, String place, String[] participants, Calendar datetime, ImageView image) {
        this.id = id;
        this.title = title;
        this.place = place;
        this.participants = participants;
        this.datetime = datetime;
        this.image = image;
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public String[] getParticipants() {
        return participants;
    }

    public void setParticipants(String[] participants) {
        this.participants = participants;
    }

    public Calendar getDatetime() {
        return datetime;
    }

    public void setDatetime(Calendar datetime) {
        this.datetime = datetime;
    }

    public ImageView getImage() {
        return image;
    }

    public void setImage(ImageView image) {
        this.image = image;
    }

    public String getStringDate() {
        String dayOfMonth = String.valueOf(getDatetime().get(Calendar.DAY_OF_MONTH));
        String month = String.valueOf(getDatetime().get(Calendar.MONTH) + 1);
        String year = String.valueOf(getDatetime().get(Calendar.YEAR));
        String date = String.join("/", dayOfMonth, month, year);
        return date;
    }

    public String getStringTime() {
        String hour = String.valueOf(getDatetime().get(Calendar.HOUR_OF_DAY));
        int minuteInt = getDatetime().get(Calendar.MINUTE);
        String minute = String.valueOf(minuteInt < 10 ? "0" + minuteInt : minuteInt);
        String time = String.join(":", hour, minute);
        return time;
    }
}
