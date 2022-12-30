package com.nhat.meetingcalendar.activities;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import com.nhat.meetingcalendar.R;
import com.nhat.meetingcalendar.db_adapter.DBAdapter;
import com.nhat.meetingcalendar.fragments.DatePickerFragment;
import com.nhat.meetingcalendar.fragments.TimePickerFragment;
import com.nhat.meetingcalendar.meeting_ds.Meeting;
import com.nhat.meetingcalendar.meeting_ds.ParticipantImage;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Vector;

public class SearchActivity extends AppCompatActivity {

    private DBAdapter dbAdapter;
    private SharedPreferences userPrefs;
    private String userPrefsFileName;
    private ArrayList<Meeting> meetings;
    private ArrayList<ParticipantImage> participantImages;
    private ArrayList<Meeting> searchResult; //Holds meetings that match search criteria
    private DialogFragment datetimeFragment;
    private ScrollView mainLayout;
    private TextView appTitleText, searchSubtitleText, searchByText, searchResultText;
    private TextView meetingDateText, meetingTimeText, meetingDateResult, meetingTimeResult;
    private EditText searchParticipantNameEditText;
    private RadioGroup searchCriteriaRadioGroup;
    private Button selectDateButton, selectTimeButton, clearDateButton, clearTimeButton;
    private Button searchButton, backButton;
    private LinearLayout searchResultListLayout;
    private LinearLayout.LayoutParams meetingContainerParams, imageContainerParams, imageLayoutParams;
    private String appTitleFontSize, subtitleFontSize, editTextFontSize, otherTextsFontSize;
    private String textColor, bgColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        //Initialize the dbAdapter object
        dbAdapter = new DBAdapter(getApplicationContext());

        //Set SharedPreferences keys
        appTitleFontSize = getString(R.string.app_title_font_size_prefs);
        subtitleFontSize = getString(R.string.subtitle_font_size_prefs);
        editTextFontSize = getString(R.string.edittext_font_size_prefs);
        otherTextsFontSize = getString(R.string.other_texts_font_size_prefs);
        textColor = getString(R.string.text_color_prefs);
        bgColor = getString(R.string.bg_color_prefs);

        //Initialize SharedPreferences
        userPrefsFileName = getResources().getString(R.string.user_prefs_file_name);
        userPrefs = getSharedPreferences(userPrefsFileName, MODE_PRIVATE);

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

        //Find search layouts and views
        searchParticipantNameEditText = findViewById(R.id.search_participant_name_edit);
        meetingDateResult = findViewById(R.id.meeting_date_result);
        meetingTimeResult = findViewById(R.id.meeting_time_result);
        LinearLayout searchMeetingDatetimeTextLayout = findViewById(R.id.search_meeting_datetime_text_layout);
        RelativeLayout searchMeetingDatetimeButtonsLayout = findViewById(R.id.search_meeting_datetime_buttons_layout);
        RelativeLayout clearMeetingDatetimeButtonsLayout = findViewById(R.id.clear_meeting_datetime_buttons_layout);
        searchButton = findViewById(R.id.search_button);
        backButton = findViewById(R.id.back_button);

        //Find other views for changing size and color only
        mainLayout = findViewById(R.id.main_layout);
        appTitleText = findViewById(R.id.app_title_text);
        searchSubtitleText = findViewById(R.id.search_subtitle_text);
        searchByText = findViewById(R.id.search_by_text);
        meetingDateText = findViewById(R.id.meeting_date_text);
        meetingTimeText = findViewById(R.id.meeting_time_text);
        searchResultText = findViewById(R.id.search_result_text);

        searchCriteriaRadioGroup = findViewById(R.id.search_criteria_radiogroup);
        searchCriteriaRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                RadioButton checkedRadioButton = findViewById(radioGroup.getCheckedRadioButtonId());
                if (checkedRadioButton.getText().toString().equals(getResources().getString(R.string.search_participant_name))) {
                    searchParticipantNameEditText.setVisibility(View.VISIBLE);
                    searchMeetingDatetimeTextLayout.setVisibility(View.GONE);
                    searchMeetingDatetimeButtonsLayout.setVisibility(View.GONE);
                    clearMeetingDatetimeButtonsLayout.setVisibility(View.GONE);
                }
                else if (checkedRadioButton.getText().toString().equals(getResources().getString(R.string.meeting_datetime))) {
                    searchParticipantNameEditText.setVisibility(View.GONE);
                    searchMeetingDatetimeTextLayout.setVisibility(View.VISIBLE);
                    searchMeetingDatetimeButtonsLayout.setVisibility(View.VISIBLE);
                    clearMeetingDatetimeButtonsLayout.setVisibility(View.VISIBLE);
                }
            }
        });

        //Set onClickListener for SELECT DATE and SELECT TIME buttons
        selectDateButton = findViewById(R.id.date_picker_button);
        selectDateButton.setOnClickListener(selectDatetimeButtonListener);
        selectTimeButton = findViewById(R.id.time_picker_button);
        selectTimeButton.setOnClickListener(selectDatetimeButtonListener);

        //Set onClickListener for CLEAR DATE and CLEAR TIME buttons
        View.OnClickListener clearDatetimeButtonListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Button button = (Button) view;
                if (button.getText().toString().equals(getResources().getString(R.string.clear_date_button))) {
                    meetingDateResult.setText("");
                }
                else if (button.getText().toString().equals(getResources().getString(R.string.clear_time_button))) {
                    meetingTimeResult.setText("");
                }
            }
        };
        clearDateButton = findViewById(R.id.clear_date_button);
        clearDateButton.setOnClickListener(clearDatetimeButtonListener);
        clearTimeButton = findViewById(R.id.clear_time_button);
        clearTimeButton.setOnClickListener(clearDatetimeButtonListener);

        //Find searchResultListLayout view and set parameters for its children
        searchResultListLayout = findViewById(R.id.search_result_list_ll);
        meetingContainerParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        meetingContainerParams.setMargins(250, 50, 250, 50);
        imageContainerParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        imageLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        //Initialize searchResult
        searchResult = new ArrayList<>();

        //Set onClickListener for Search button
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchResult.clear();
                searchResultListLayout.removeAllViews();
                RadioButton checkedRadioButton = findViewById(searchCriteriaRadioGroup.getCheckedRadioButtonId());
                //Search by (a part of, case-insensitive) participant name
                if (checkedRadioButton.getText().toString().equals(getResources().getString(R.string.search_participant_name))) {
                    String participantName = searchParticipantNameEditText.getText().toString().toLowerCase().trim();
                    if (participantName.isEmpty()) {
                        searchParticipantNameEditText.setBackgroundColor(getResources().getColor(R.color.tomato));
                    }
                    else {
                        for (Meeting meeting : meetings) {
                            String[] participants = meeting.getParticipants();
                            for (String p : participants) {
                                if (p.toLowerCase().contains(participantName)) {
                                    searchResult.add(meeting);
                                    break;
                                }
                            }
                        }
                        if (searchResult.size() == 0) {
                            showNoMeetingMessage();
                        }
                        else {
                            showSearchResult();
                        }
                        displayToast(getResources().getString(R.string.search_complete));
                    }
                }
                //Search by datetime
                else if (checkedRadioButton.getText().toString().equals(getResources().getString(R.string.meeting_datetime))) {
                    String date = meetingDateResult.getText().toString();
                    String time = meetingTimeResult.getText().toString();
                    if (date.isEmpty() && time.isEmpty()) {
                        meetingDateResult.setBackgroundColor(getResources().getColor(R.color.tomato));
                        meetingTimeResult.setBackgroundColor(getResources().getColor(R.color.tomato));
                    }
                    else {
                        if (!date.isEmpty() && time.isEmpty()) { //Search date only
                            for (Meeting meeting : meetings) {
                                if (meeting.getStringDate().equals(date)) {
                                    searchResult.add(meeting);
                                }
                            }
                        }
                        else if (date.isEmpty() && !time.isEmpty()) { //Search time only
                            for (Meeting meeting : meetings) {
                                if (meeting.getStringTime().equals(time)) {
                                    searchResult.add(meeting);
                                }
                            }
                        }
                        else {
                            for (Meeting meeting : meetings) { //Search both date and time
                                if (meeting.getStringDate().equals(date) && meeting.getStringTime().equals(time)) {
                                    searchResult.add(meeting);
                                }
                            }
                        }
                        if (searchResult.size() == 0) {
                            showNoMeetingMessage();
                        }
                        else {
                            showSearchResult();
                        }
                        displayToast(getResources().getString(R.string.search_complete));
                    }
                }
            }
        });

        //Back to MainActivity
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        //Set textChangedListener for EditText views
        TextWatcher textChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //Do nothing
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //Do nothing
            }
            @Override
            public void afterTextChanged(Editable editable) {
                if (searchParticipantNameEditText.isFocused()) {
                    searchParticipantNameEditText.setBackgroundColor(Color.TRANSPARENT);
                }
            }
        };
        searchParticipantNameEditText.addTextChangedListener(textChangedListener);
    }

    //Repaint the activity when it is invoked
    @Override
    protected void onResume() {
        super.onResume();
        repaint();
    }

    private View.OnClickListener selectDatetimeButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Button button = (Button) v;
            if (button.getText().toString().equals(getString(R.string.date_picker_button))) {
                datetimeFragment = new DatePickerFragment();
                datetimeFragment.show(getSupportFragmentManager(), "DatePicker");
            }
            else if (button.getText().toString().equals(getString(R.string.time_picker_button))) {
                datetimeFragment = new TimePickerFragment();
                datetimeFragment.show(getSupportFragmentManager(), "TimePicker");
            }
        }
    };

    private void showNoMeetingMessage() {
        TextView searchResultListText = new TextView(SearchActivity.this);
        searchResultListText.setGravity(Gravity.CENTER);
        searchResultListText.setTextSize(userPrefs.getFloat(otherTextsFontSize, 14));
        searchResultListText.setTextColor(userPrefs.getInt(textColor, getResources().getColor(R.color.black)));
        searchResultListText.setText(getResources().getString(R.string.no_meeting));
        searchResultListLayout.addView(searchResultListText);
    }

    //The list layout contains meetingContainer(s)
    //Structure of a meetingContainer: |-meetingImage
    //                                 |-meetingText
    //                                 |-participantImageContainer: |-participantNameTv
    //                                                              |-participantImage
    private void showSearchResult() {
        for (Meeting meeting : searchResult) {
            LinearLayout meetingContainer = new LinearLayout(SearchActivity.this);
            meetingContainer.setOrientation(LinearLayout.VERTICAL);
            meetingContainer.setLayoutParams(meetingContainerParams);

            if (meeting.getImage().getDrawable() != null) {
                ImageView meetingImage = meeting.getImage();
                meetingImage.setLayoutParams(imageLayoutParams);
                if (meetingImage.getParent() != null) { //Avoid IllegalStateException when displaying the same view the second time
                    ((ViewGroup)meetingImage.getParent()).removeView(meetingImage);
                }
                meetingContainer.addView(meetingImage);
            }

            TextView meetingText = new TextView(SearchActivity.this);
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
            if (meetingText.getParent() != null) { //Avoid IllegalStateException when displaying the same view the second time
                ((ViewGroup)meetingText.getParent()).removeView(meetingText);
            }
            meetingContainer.addView(meetingText);

            for (ParticipantImage pImg : participantImages) {
                if (pImg.getMeetingsId() == meeting.getId()) {
                    LinearLayout participantImageContainer = new LinearLayout(SearchActivity.this);
                    participantImageContainer.setOrientation(LinearLayout.HORIZONTAL);
                    participantImageContainer.setLayoutParams(imageContainerParams);
                    TextView participantNameTv = new TextView(SearchActivity.this);
                    participantNameTv.setTextSize(userPrefs.getFloat(otherTextsFontSize, 14));
                    participantNameTv.setTextColor(userPrefs.getInt(textColor, getResources().getColor(R.color.black)));
                    StringBuilder sb2 = new StringBuilder();
                    sb2.append("- ").append(pImg.getParticipant()).append(" ");
                    participantNameTv.setText(sb2.toString());
                    if (participantNameTv.getParent() != null) { //Avoid IllegalStateException when displaying the same view the second time
                        ((ViewGroup)participantNameTv.getParent()).removeView(participantNameTv);
                    }
                    participantImageContainer.addView(participantNameTv);
                    if (pImg.getImage().getDrawable() != null) {
                        ImageView participantImage = pImg.getImage();
                        participantImage.setLayoutParams(imageLayoutParams);
                        if (participantImage.getParent() != null) { //Avoid IllegalStateException when displaying the same view the second time
                            ((ViewGroup)participantImage.getParent()).removeView(participantImage);
                        }
                        participantImageContainer.addView(participantImage);
                    }
                    if (participantImageContainer.getParent() != null) { //Avoid IllegalStateException when displaying the same view the second time
                        ((ViewGroup)participantImageContainer.getParent()).removeView(participantImageContainer);
                    }
                    meetingContainer.addView(participantImageContainer);
                }
            }

            searchResultListLayout.addView(meetingContainer);
        }
    }

    private void repaint() {
        //Set user settings from SharedPreferences data while creating the activity
        //Set font size
        appTitleText.setTextSize(userPrefs.getFloat(appTitleFontSize, 28));
        searchSubtitleText.setTextSize(userPrefs.getFloat(subtitleFontSize, 20));
        searchByText.setTextSize(userPrefs.getFloat(otherTextsFontSize, 14));
        for (int i = 0; i < searchCriteriaRadioGroup.getChildCount(); i++) {
            RadioButton rb = (RadioButton) searchCriteriaRadioGroup.getChildAt(i);
            rb.setTextSize(userPrefs.getFloat(otherTextsFontSize, 14));
        }
        searchParticipantNameEditText.setTextSize(userPrefs.getFloat(editTextFontSize, 18));
        meetingDateText.setTextSize(userPrefs.getFloat(otherTextsFontSize, 14));
        meetingDateResult.setTextSize(userPrefs.getFloat(otherTextsFontSize, 14));
        meetingTimeText.setTextSize(userPrefs.getFloat(otherTextsFontSize, 14));
        meetingTimeResult.setTextSize(userPrefs.getFloat(otherTextsFontSize, 14));
        selectDateButton.setTextSize(userPrefs.getFloat(otherTextsFontSize, 14));
        selectTimeButton.setTextSize(userPrefs.getFloat(otherTextsFontSize, 14));
        clearDateButton.setTextSize(userPrefs.getFloat(otherTextsFontSize, 14));
        clearTimeButton.setTextSize(userPrefs.getFloat(otherTextsFontSize, 14));
        searchButton.setTextSize(userPrefs.getFloat(otherTextsFontSize, 14));
        searchResultText.setTextSize(userPrefs.getFloat(subtitleFontSize, 20));
        backButton.setTextSize(userPrefs.getFloat(otherTextsFontSize, 14));
        //Set text color (except texts in Buttons)
        appTitleText.setTextColor(userPrefs.getInt(textColor, getResources().getColor(R.color.black)));
        searchSubtitleText.setTextColor(userPrefs.getInt(textColor, getResources().getColor(R.color.black)));
        searchByText.setTextColor(userPrefs.getInt(textColor, getResources().getColor(R.color.black)));
        for (int i = 0; i < searchCriteriaRadioGroup.getChildCount(); i++) {
            RadioButton rb = (RadioButton) searchCriteriaRadioGroup.getChildAt(i);
            rb.setTextColor(userPrefs.getInt(textColor, getResources().getColor(R.color.black)));
        }
        searchParticipantNameEditText.setTextColor(userPrefs.getInt(textColor, getResources().getColor(R.color.black)));
        meetingDateText.setTextColor(userPrefs.getInt(textColor, getResources().getColor(R.color.black)));
        meetingDateResult.setTextColor(userPrefs.getInt(textColor, getResources().getColor(R.color.black)));
        meetingTimeText.setTextColor(userPrefs.getInt(textColor, getResources().getColor(R.color.black)));
        meetingTimeResult.setTextColor(userPrefs.getInt(textColor, getResources().getColor(R.color.black)));
        searchResultText.setTextColor(userPrefs.getInt(textColor, getResources().getColor(R.color.black)));
        //Set background color
        mainLayout.setBackgroundColor(userPrefs.getInt(bgColor, getResources().getColor(R.color.old_paper)));
    }

    private void displayToast(String text) {
        Toast.makeText(SearchActivity.this, text, Toast.LENGTH_SHORT).show();
    }
}