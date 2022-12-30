package com.nhat.meetingcalendar.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.nhat.meetingcalendar.R;
import com.nhat.meetingcalendar.db_adapter.DBAdapter;
import com.nhat.meetingcalendar.meeting_ds.Meeting;
import com.nhat.meetingcalendar.meeting_ds.ParticipantImage;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Vector;

public class SummaryActivity extends AppCompatActivity {

    private DBAdapter dbAdapter;
    private SharedPreferences userPrefs;
    private String userPrefsFileName;
    private ArrayList<Meeting> meetings;
    private ArrayList<ParticipantImage> participantImages;
    private ScrollView mainLayout;
    private TextView appTitleText, summarySubtitleText;
    private LinearLayout meetingListLayout;
    private LinearLayout.LayoutParams meetingContainerParams, imageContainerParams, imageLayoutParams;
    private Button backButton;
    private String appTitleFontSize, subtitleFontSize, otherTextsFontSize;
    private String textColor, bgColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);

        //Initialize the dbAdapter object
        dbAdapter = new DBAdapter(getApplicationContext());

        //Set SharedPreferences keys
        appTitleFontSize = getString(R.string.app_title_font_size_prefs);
        subtitleFontSize = getString(R.string.subtitle_font_size_prefs);
        otherTextsFontSize = getString(R.string.other_texts_font_size_prefs);
        textColor = getString(R.string.text_color_prefs);
        bgColor = getString(R.string.bg_color_prefs);

        //Initialize SharedPreferences
        userPrefsFileName = getResources().getString(R.string.user_prefs_file_name);
        userPrefs = getSharedPreferences(userPrefsFileName, MODE_PRIVATE);

        //Find other views for changing size and color only
        mainLayout = findViewById(R.id.main_layout);
        appTitleText = findViewById(R.id.app_title_text);
        summarySubtitleText = findViewById(R.id.summary_subtitle_text);
        backButton = findViewById(R.id.back_button);

        //Read all meetings from database
        meetings = new ArrayList<>();
        participantImages = new ArrayList<>();
        Vector<Object[]> dataRows = dbAdapter.getAllMeetings();
        Vector<Object[]> imgDataRows = dbAdapter.getAllParticipantImages();
        for (Object[] dataRow : dataRows) {
            int id = (int) dataRow[0];
            String title = (String) dataRow[1];
            String place = (String) dataRow[2];
            String participantsStr = (String) dataRow[3];
            String datetimeStr = (String) dataRow[4];
            String[] participantsArr = participantsStr.split("%#%");
            String[] datetimeArr = datetimeStr.split("%#%");
            String meetingDateResultStr = datetimeArr[0];
            String meetingTimeResultStr = datetimeArr[1];
            String[] dateElementsStr = meetingDateResultStr.split("/");
            int[] dateElements = new int[dateElementsStr.length];
            for (int i = 0; i < dateElementsStr.length; i++) {
                dateElements[i] = Integer.parseInt(dateElementsStr[i]);
            }
            String[] timeElementsStr = meetingTimeResultStr.split(":");
            int[] timeElements = new int[timeElementsStr.length];
            for (int i = 0; i < timeElementsStr.length; i++) {
                timeElements[i] = Integer.parseInt(timeElementsStr[i]);
            }
            Calendar datetime = Calendar.getInstance();
            //Calendar.JANUARY = 0, Calendar.FEBRUARY = 1,...
            datetime.set(dateElements[2], (dateElements[1] - 1), dateElements[0], timeElements[0], timeElements[1]);
            ImageView image = (ImageView) dataRow[5];
            meetings.add(new Meeting(id, title, place, participantsArr, datetime, image));
        }
        for (Object[] imgDataRow : imgDataRows) {
            int id = (int) imgDataRow[0];
            int meetingsId = (int) imgDataRow[1];
            String participant = (String) imgDataRow[2];
            ImageView image = (ImageView) imgDataRow[3];
            participantImages.add(new ParticipantImage(id, meetingsId, participant, image));
        }


        //Find meetingListLayout view and set parameters for its children
        meetingListLayout = findViewById(R.id.meeting_list_ll);
        meetingContainerParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        meetingContainerParams.setMargins(250, 50, 250, 50);
        imageContainerParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        imageLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        //Display all meeting data
        if (meetings.size() == 0) {
            showNoMeetingMessage();
        }
        else {
            showMeetingList();
            displayToast(getString(R.string.number_of_meetings) + meetings.size());
        }

        //Back to MainActivity
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    //Repaint the activity when it is invoked
    @Override
    protected void onResume() {
        super.onResume();
        repaint();
    }

    private void showNoMeetingMessage() {
        TextView meetingListText = new TextView(this);
        meetingListText.setGravity(Gravity.CENTER);
        meetingListText.setTextSize(userPrefs.getFloat(otherTextsFontSize, 14));
        meetingListText.setTextColor(userPrefs.getInt(textColor, getResources().getColor(R.color.black)));
        meetingListText.setText(getResources().getString(R.string.no_meeting));
        meetingListLayout.addView(meetingListText);
    }

    //The list layout contains meetingContainer(s)
    //Structure of a meetingContainer: |-meetingImage
    //                                 |-meetingText
    //                                 |-participantImageContainer: |-participantNameTv
    //                                                              |-participantImage
    private void showMeetingList() {
        for (Meeting meeting : meetings) {
            LinearLayout meetingContainer = new LinearLayout(this);
            meetingContainer.setOrientation(LinearLayout.VERTICAL);
            meetingContainer.setLayoutParams(meetingContainerParams);

            if (meeting.getImage().getDrawable() != null) {
                ImageView meetingImage = meeting.getImage();
                meetingImage.setLayoutParams(imageLayoutParams);
                meetingContainer.addView(meetingImage);
            }

            TextView meetingText = new TextView(this);
            meetingText.setTextSize(userPrefs.getFloat(otherTextsFontSize, 14));
            meetingText.setTextColor(userPrefs.getInt(textColor, getResources().getColor(R.color.black)));
            StringBuilder sb = new StringBuilder();
            sb.append("Meeting ID: ");
            sb.append(meeting.getId());
            sb.append("\n");
            sb.append("Title: ");
            sb.append(meeting.getTitle());
            sb.append("\n");
            sb.append("Place: ");
            sb.append(meeting.getPlace());
            sb.append("\n");
            sb.append("Date: ");
            sb.append(meeting.getStringDate());
            sb.append("\n");
            sb.append("Time: ");
            sb.append(meeting.getStringTime());
            sb.append("\n");
            sb.append("Participants: ");
            meetingText.setText(sb.toString());
            meetingContainer.addView(meetingText);

            for (ParticipantImage pImg : participantImages) {
                if (pImg.getMeetingsId() == meeting.getId()) {
                    LinearLayout participantImageContainer = new LinearLayout(this);
                    participantImageContainer.setOrientation(LinearLayout.HORIZONTAL);
                    participantImageContainer.setLayoutParams(imageContainerParams);
                    TextView participantName = new TextView(this);
                    participantName.setTextSize(userPrefs.getFloat(otherTextsFontSize, 14));
                    participantName.setTextColor(userPrefs.getInt(textColor, getResources().getColor(R.color.black)));
                    StringBuilder sb2 = new StringBuilder();
                    sb2.append("- ").append(pImg.getParticipant()).append(" ");
                    participantName.setText(sb2.toString());
                    participantImageContainer.addView(participantName);
                    if (pImg.getImage().getDrawable() != null) {
                        ImageView participantImage = pImg.getImage();
                        participantImage.setLayoutParams(imageLayoutParams);
                        participantImageContainer.addView(participantImage);
                    }
                    meetingContainer.addView(participantImageContainer);
                }
            }

            meetingListLayout.addView(meetingContainer);
        }
    }

    private void displayToast(String text) {
        Toast.makeText(SummaryActivity.this, text, Toast.LENGTH_LONG).show();
    }

    private void repaint() {
        //Set user settings from SharedPreferences data while creating the activity
        //Set font size
        appTitleText.setTextSize(userPrefs.getFloat(appTitleFontSize, 28));
        summarySubtitleText.setTextSize(userPrefs.getFloat(subtitleFontSize, 20));
        backButton.setTextSize(userPrefs.getFloat(otherTextsFontSize, 14));
        //Set text color (except texts in Buttons)
        appTitleText.setTextColor(userPrefs.getInt(textColor, getResources().getColor(R.color.black)));
        summarySubtitleText.setTextColor(userPrefs.getInt(textColor, getResources().getColor(R.color.black)));
        //Set background color
        mainLayout.setBackgroundColor(userPrefs.getInt(bgColor, getResources().getColor(R.color.old_paper)));
    }
}