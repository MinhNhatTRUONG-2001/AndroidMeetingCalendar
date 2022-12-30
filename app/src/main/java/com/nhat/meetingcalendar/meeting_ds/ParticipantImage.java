package com.nhat.meetingcalendar.meeting_ds;

import android.widget.ImageView;

public class ParticipantImage {
    private int id;
    private int meetingsId;
    private String participant;
    private ImageView image;

    public ParticipantImage(int id, int meetingsId, String participant, ImageView image) {
        this.id = id;
        this.meetingsId = meetingsId;
        this.participant = participant;
        this.image = image;
    }


    public int getId() {
        return id;
    }

    public int getMeetingsId() {
        return meetingsId;
    }

    public void setMeetingsId(int meetingsId) {
        this.meetingsId = meetingsId;
    }

    public String getParticipant() {
        return participant;
    }

    public void setParticipant(String participant) {
        this.participant = participant;
    }

    public ImageView getImage() {
        return image;
    }

    public void setImage(ImageView image) {
        this.image = image;
    }
}
