package com.nhat.meetingcalendar.activities;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.loader.content.CursorLoader;

import com.google.android.material.button.MaterialButton;
import com.nhat.meetingcalendar.R;
import com.nhat.meetingcalendar.db_adapter.DBAdapter;
import com.nhat.meetingcalendar.fragments.DatePickerFragment;
import com.nhat.meetingcalendar.fragments.TimePickerFragment;
import com.nhat.meetingcalendar.meeting_ds.Meeting;
import com.nhat.meetingcalendar.meeting_ds.ParticipantImage;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Vector;
import java.util.concurrent.ExecutionException;

public class UpdateActivity extends AppCompatActivity {

    private DBAdapter dbAdapter;
    private SharedPreferences userPrefs;
    private String userPrefsFileName;
    private ArrayList<Meeting> meetings;
    private ArrayList<ParticipantImage> participantImages;
    private ArrayList<String> participants = new ArrayList<>(); //Holds a temporary list of participants (while updating a meeting)
    private ArrayList<Bitmap> images = new ArrayList<>(); //Holds a temporary list of participant images (while adding a meeting)
    private DialogFragment datetimeFragment;
    private LinearLayout selectedUpdateContainer = null;
    private ScrollView mainLayout;
    private TextView appTitleText, updateSubtitleText, meetingIdText, meetingId, meetingDateResult, meetingTimeResult;
    private TextView meetingTitleText, meetingPlaceText, meetingParticipantsText, meetingDatetimeText;
    private TextView meetingDateText, meetingTimeText, updateOtherSubtitleText, meetingImageUrlText;
    private EditText titleEditText, placeEditText, participantsEditText, meetingImageUrlEditText;
    private Button addParticipantButton, chooseImageButton, selectDateButton, selectTimeButton, getImageButton, updateButton, deleteButton, backButton;
    private ImageView participantImage, meetingImage;
    private Bitmap imageBitmap = null, imageBitmap2 = null;
    private int galleryRequestCode = 1;
    private String appTitleFontSize, subtitleFontSize, editTextFontSize, otherTextsFontSize;
    private String textColor, bgColor;
    BackgroundTask backgroundTask = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);

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

        //Find meeting views in the form
        meetingId = findViewById(R.id.meeting_id);
        titleEditText = findViewById(R.id.meeting_title_edit);
        placeEditText = findViewById(R.id.meeting_place_edit);
        participantsEditText = findViewById(R.id.meeting_participants_edit);
        meetingDateResult = findViewById(R.id.meeting_date_result);
        meetingTimeResult = findViewById(R.id.meeting_time_result);
        meetingImageUrlEditText = findViewById(R.id.meeting_image_url_edit);

        //Find updateList view
        LinearLayout updateList = findViewById(R.id.update_list);

        //Find other views for changing size and color only
        mainLayout = findViewById(R.id.main_layout);
        appTitleText = findViewById(R.id.app_title_text);
        updateSubtitleText = findViewById(R.id.update_subtitle_text);
        meetingIdText = findViewById(R.id.meeting_id_text);
        meetingTitleText = findViewById(R.id.meeting_title_text);
        meetingPlaceText = findViewById(R.id.meeting_place_text);
        meetingParticipantsText = findViewById(R.id.meeting_participants_text);
        meetingDatetimeText = findViewById(R.id.meeting_datetime_text);
        meetingDateText = findViewById(R.id.meeting_date_text);
        meetingTimeText = findViewById(R.id.meeting_time_text);
        meetingImageUrlText = findViewById(R.id.meeting_image_url_text);
        updateOtherSubtitleText = findViewById(R.id.update_other_subtitle_text);

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
                if (titleEditText.isFocused()) {
                    titleEditText.setBackgroundColor(Color.TRANSPARENT);
                }
                else if (placeEditText.isFocused()) {
                    placeEditText.setBackgroundColor(Color.TRANSPARENT);
                }
                else if (participantsEditText.isFocused()) {
                    participantsEditText.setBackgroundColor(Color.TRANSPARENT);
                }
            }
        };
        titleEditText.addTextChangedListener(textChangedListener);
        placeEditText.addTextChangedListener(textChangedListener);
        participantsEditText.addTextChangedListener(textChangedListener);

        //Button listener for choosing a participant image
        chooseImageButton = findViewById(R.id.choose_image_button);
        chooseImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Here we create an Intent to be used for invoking the gallery application
                Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                //Here we invoke the gallery application to pick an image
                startActivityForResult(Intent.createChooser(galleryIntent, getString(R.string.choose_image_title)), galleryRequestCode);
            }
        });

        //Handle adding and deleting participants
        //A LinearLayout holds a list of smaller LinearLayouts
        LinearLayout participantsListContainer = findViewById(R.id.meeting_participants_list);
        addParticipantButton = findViewById(R.id.add_participant_button);
        LinearLayout.LayoutParams participantContainerParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        participantContainerParams.setMargins(0, 0, 50, 0);
        LinearLayout.LayoutParams pImgLayoutParams = new LinearLayout.LayoutParams(100, 100);
        LinearLayout.LayoutParams deleteButtonParams = new LinearLayout.LayoutParams(150, ViewGroup.LayoutParams.WRAP_CONTENT);
        deleteButtonParams.setMargins(0, 0, 10, 0);
        addParticipantButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (participantsEditText.getText().toString().trim().isEmpty()) {
                    participantsEditText.setBackgroundColor(getResources().getColor(R.color.tomato));
                }
                else {
                    participants.add(participantsEditText.getText().toString().trim());
                    if (participantImage != null) {
                        imageBitmap = ((BitmapDrawable) participantImage.getDrawable()).getBitmap();
                    }
                    images.add(imageBitmap);
                    //Each smaller LinearLayout contains a participant name text and a delete button
                    LinearLayout participantContainer = new LinearLayout(UpdateActivity.this);
                    participantContainer.setOrientation(LinearLayout.HORIZONTAL);
                    participantContainer.setLayoutParams(participantContainerParams);
                    ImageView pImg = null;
                    if (imageBitmap != null) {
                        pImg = new ImageView(UpdateActivity.this);
                        pImg.setLayoutParams(pImgLayoutParams);
                        pImg.setImageBitmap(imageBitmap);
                    }
                    TextView participantText = new TextView(UpdateActivity.this);
                    participantText.setText(participantsEditText.getText());
                    participantText.setTextSize(userPrefs.getFloat(otherTextsFontSize, 14));
                    participantText.setTextColor(userPrefs.getInt(textColor, getResources().getColor(R.color.black)));
                    MaterialButton deleteParticipantButton = new MaterialButton(UpdateActivity.this);
                    deleteParticipantButton.setText("X");
                    deleteParticipantButton.setTextSize(userPrefs.getFloat(otherTextsFontSize, 14));
                    deleteParticipantButton.setBackgroundColor(getResources().getColor(R.color.tomato));
                    deleteParticipantButton.setLayoutParams(deleteButtonParams);
                    deleteParticipantButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            String removedParticipant = participantText.getText().toString();
                            int i = participants.indexOf(removedParticipant);
                            participants.remove(removedParticipant);
                            images.remove(i);
                            participantsListContainer.removeView(participantContainer);
                        }
                    });
                    participantContainer.addView(deleteParticipantButton);
                    if (imageBitmap != null) {
                        participantContainer.addView(pImg);
                        participantImage.setImageBitmap(null);
                        participantImage.setVisibility(View.GONE);
                    }
                    participantContainer.addView(participantText);
                    participantsListContainer.addView(participantContainer, 0);

                    participantsEditText.setText("");
                }
            }
        });

        //Set onClickListener for SELECT DATE and SELECT TIME buttons
        selectDateButton = findViewById(R.id.date_picker_button);
        selectDateButton.setOnClickListener(selectDatetimeButtonListener);
        selectTimeButton = findViewById(R.id.time_picker_button);
        selectTimeButton.setOnClickListener(selectDatetimeButtonListener);

        //Button listener for getting a meeting image from the Internet
        meetingImage = findViewById(R.id.meeting_image);
        getImageButton = findViewById(R.id.get_image_button);
        getImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String imageUrl = meetingImageUrlEditText.getText().toString();
                if (!imageUrl.isEmpty()) {
                    //Here we initialize the backgroundTask object
                    backgroundTask = new BackgroundTask();
                    //Here we call the execute() method of backgroundTask object.
                    //This will cause calling methods of backgroundTask object.
                    try {
                        backgroundTask.execute(imageUrl).get();
                    } catch (ExecutionException | InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                else {
                    meetingImage.setImageBitmap(null);
                    meetingImage.setVisibility(View.GONE);
                }
            }
        });

        //Check user-provided meeting data in the form, then update the corresponding meeting to "meetings" ArrayList
        updateButton = findViewById(R.id.update_button);
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String idStr = meetingId.getText().toString();
                String title = titleEditText.getText().toString().trim();
                String place = placeEditText.getText().toString().trim();
                String[] participantsArr = participants.toArray(new String[0]);
                String meetingDateResultStr = meetingDateResult.getText().toString();
                String meetingTimeResultStr = meetingTimeResult.getText().toString();
                //Check user-provided meeting data
                if (idStr.isEmpty() || title.isEmpty() || place.isEmpty() || participantsArr.length == 0 || meetingDateResultStr.isEmpty() || meetingTimeResultStr.isEmpty()) {
                    if (idStr.isEmpty()) {
                        meetingId.setBackgroundColor(getResources().getColor(R.color.tomato));
                    }
                    if (title.isEmpty()) {
                        titleEditText.setBackgroundColor(getResources().getColor(R.color.tomato));
                    }
                    if (place.isEmpty()) {
                        placeEditText.setBackgroundColor(getResources().getColor(R.color.tomato));
                    }
                    if (participantsArr.length == 0) {
                        participantsEditText.setBackgroundColor(getResources().getColor(R.color.tomato));
                    }
                    if (meetingDateResultStr.isEmpty()) {
                        meetingDateResult.setBackgroundColor(getResources().getColor(R.color.tomato));
                    }
                    if (meetingTimeResultStr.isEmpty()) {
                        meetingTimeResult.setBackgroundColor(getResources().getColor(R.color.tomato));
                    }
                }
                else {
                    meetingId.setBackgroundColor(Color.TRANSPARENT);
                    //Update meeting in database
                    int id = Integer.parseInt(idStr);
                    String participantsStr = String.join("%#%", participantsArr);
                    String datetimeStr = meetingDateResultStr + "%#%" + meetingTimeResultStr;
                    Bitmap meetingImageBitmap = ((BitmapDrawable) meetingImage.getDrawable()).getBitmap();
                    boolean isUpdated = dbAdapter.updateMeeting(id, title, place, participantsStr, datetimeStr, meetingImageBitmap);
                    if (isUpdated) {
                        //Update participant images in database
                        dbAdapter.deleteParticipantImageByMeetingsId(id);
                        for (int i = 0; i < participants.size(); i++) {
                            dbAdapter.addParticipantImage(id, participants.get(i), images.get(i));
                        }
                        //Update also "meetings", "participantImages" ArrayList and MeetingInfoText
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
                        for (Meeting meeting : meetings) {
                            if (meeting.getId() == id) {
                                Meeting updatedMeeting = meeting;
                                updatedMeeting.setTitle(title);
                                updatedMeeting.setPlace(place);
                                updatedMeeting.setParticipants(participantsArr);
                                updatedMeeting.setDatetime(datetime);
                                updatedMeeting.setImage(meetingImage);
                                updateMeetingInfoText(updatedMeeting);

                                //Re-read participant images from database to update "participantImages"
                                participantImages.clear();
                                Vector<Object[]> imgDataRows = dbAdapter.getAllParticipantImages();
                                for (Object[] imgDataRow : imgDataRows) {
                                    int pImgId = (int) imgDataRow[0];
                                    int meetingsId = (int) imgDataRow[1];
                                    String participant = (String) imgDataRow[2];
                                    ImageView image = (ImageView) imgDataRow[3];
                                    participantImages.add(new ParticipantImage(pImgId, meetingsId, participant, image));
                                }

                                break;
                            }
                        }
                        displayToast(getString(R.string.meeting_updated));
                    }
                    else {
                        displayToast(getString(R.string.db_update_error));
                    }
                }

            }
        });

        //Check if a meeting is selected, then ask for delete confirmation, then delete the meeting by id
        deleteButton = findViewById(R.id.delete_button);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String idStr = meetingId.getText().toString();
                if (idStr.isEmpty()) {
                    meetingId.setBackgroundColor(getResources().getColor(R.color.tomato));
                }
                else {
                    int id = Integer.parseInt(idStr);

                    // Create delete confirmation dialog box
                    AlertDialog.Builder builder = new AlertDialog.Builder(UpdateActivity.this);
                    builder.setTitle(R.string.delete_dialog_title);
                    builder.setMessage(R.string.delete_dialog_message);
                    builder.setCancelable(false);
                    builder.setPositiveButton(R.string.delete_dialog_positive, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //Delete meeting from database
                            boolean isDeleted = dbAdapter.deleteMeeting(id);
                            if (isDeleted) {
                                //Delete participant images from database
                                boolean isDeleted2 = dbAdapter.deleteParticipantImageByMeetingsId(id);
                                if (isDeleted2) {
                                    //Update also "meetings", "participantImages" ArrayList and MeetingInfoText
                                    for (Meeting meeting : meetings) {
                                        if (meeting.getId() == id) {
                                            meetings.remove(meeting);
                                            break;
                                        }
                                    }
                                    ArrayList<ParticipantImage> removedParticipantImages = new ArrayList<>();
                                    for (ParticipantImage pImg : participantImages) {
                                        if (pImg.getMeetingsId() == id) {
                                            removedParticipantImages.add(pImg);
                                        }
                                    }
                                    participantImages.removeAll(removedParticipantImages);
                                    updateList.removeView(selectedUpdateContainer);
                                    displayToast(getString(R.string.meeting_deleted));
                                    //Reset form
                                    meetingId.setText("");
                                    titleEditText.setText("");
                                    placeEditText.setText("");
                                    participantsEditText.setText("");
                                    participantsListContainer.removeAllViews();
                                    participants.clear();
                                    images.clear();
                                    meetingDateResult.setText("");
                                    meetingTimeResult.setText("");
                                    meetingImageUrlEditText.setText("");
                                    meetingImage.setImageBitmap(null);
                                    meetingImage.setVisibility(View.GONE);
                                }
                            }
                            else {
                                displayToast(getString(R.string.db_delete_error));
                            }
                        }
                    });
                    builder.setNegativeButton(R.string.delete_dialog_negative, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

                    //Show the dialog box
                    builder.create().show();
                }
            }
        });

        //List all meetings to select
        LinearLayout.LayoutParams updateContainerParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        LinearLayout.LayoutParams selectMeetingButtonParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        selectMeetingButtonParams.setMargins(0, 0, 10, 10);
        for (Meeting meeting : meetings) {
            LinearLayout updateContainer = new LinearLayout(UpdateActivity.this);
            updateContainer.setOrientation(LinearLayout.HORIZONTAL);
            updateContainer.setLayoutParams(updateContainerParams);
            TextView meetingInfoText = new TextView(UpdateActivity.this);
            StringBuilder sb = new StringBuilder();
            sb.append(meeting.getId());
            sb.append(" - ");
            sb.append(meeting.getTitle());
            sb.append(", at ");
            sb.append(meeting.getStringTime());
            sb.append(" ");
            sb.append(meeting.getStringDate());
            meetingInfoText.setText(sb.toString());
            meetingInfoText.setTextSize(userPrefs.getFloat(otherTextsFontSize, 14));
            meetingInfoText.setTextColor(userPrefs.getInt(textColor, getResources().getColor(R.color.black)));
            MaterialButton selectMeetingButton = new MaterialButton(UpdateActivity.this);
            selectMeetingButton.setText(getResources().getString(R.string.select_button));
            meetingInfoText.setTextSize(userPrefs.getFloat(otherTextsFontSize, 14));
            selectMeetingButton.setBackgroundColor(getResources().getColor(R.color.forest_green));
            selectMeetingButton.setLayoutParams(selectMeetingButtonParams);
            selectMeetingButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Button clickedSelectButton = (Button) view;
                    selectedUpdateContainer = (LinearLayout) clickedSelectButton.getParent();
                    meetingId.setText(String.valueOf(meeting.getId()));
                    titleEditText.setText(meeting.getTitle());
                    placeEditText.setText(meeting.getPlace());
                    participantsEditText.setText("");
                    participantsListContainer.removeAllViews();
                    participants.clear();
                    for (ParticipantImage pImg : participantImages) {
                        if (pImg.getMeetingsId() == meeting.getId()) {
                            participants.add(pImg.getParticipant());
                            imageBitmap = ((BitmapDrawable) pImg.getImage().getDrawable()).getBitmap();
                            images.add(imageBitmap);
                            //Each smaller LinearLayout contains a participant name text, an image and a delete button
                            LinearLayout participantContainer = new LinearLayout(UpdateActivity.this);
                            participantContainer.setOrientation(LinearLayout.HORIZONTAL);
                            participantContainer.setLayoutParams(participantContainerParams);
                            ImageView pImgView = null;
                            if (imageBitmap != null) {
                                pImgView = new ImageView(UpdateActivity.this);
                                pImgView.setLayoutParams(pImgLayoutParams);
                                pImgView.setImageBitmap(imageBitmap);
                            }
                            TextView participantText = new TextView(UpdateActivity.this);
                            participantText.setText(pImg.getParticipant());
                            participantText.setTextSize(userPrefs.getFloat(otherTextsFontSize, 14));
                            participantText.setTextColor(userPrefs.getInt(textColor, getResources().getColor(R.color.black)));
                            MaterialButton deleteParticipantButton = new MaterialButton(UpdateActivity.this);
                            deleteParticipantButton.setText("X");
                            deleteParticipantButton.setTextSize(userPrefs.getFloat(otherTextsFontSize, 14));
                            deleteParticipantButton.setBackgroundColor(getResources().getColor(R.color.tomato));
                            deleteParticipantButton.setLayoutParams(deleteButtonParams);
                            deleteParticipantButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    String removedParticipant = participantText.getText().toString();
                                    int i = participants.indexOf(removedParticipant);
                                    participants.remove(removedParticipant);
                                    images.remove(i);
                                    participantsListContainer.removeView(participantContainer);
                                }
                            });
                            participantContainer.addView(deleteParticipantButton);
                            if (imageBitmap != null) {
                                participantContainer.addView(pImgView);
                                //participantImage.setImageBitmap(null);
                                //participantImage.setVisibility(View.GONE);
                            }
                            participantContainer.addView(participantText);
                            participantsListContainer.addView(participantContainer, 0);
                        }
                    }
                    meetingDateResult.setText(meeting.getStringDate());
                    meetingTimeResult.setText(meeting.getStringTime());
                    if (meeting.getImage().getDrawable() != null) {
                        imageBitmap2 = ((BitmapDrawable) meeting.getImage().getDrawable()).getBitmap();
                        meetingImage.setImageBitmap(imageBitmap2);
                        meetingImage.setVisibility(View.VISIBLE);
                    }
                    else {
                        meetingImage.setImageBitmap(null);
                        meetingImage.setVisibility(View.GONE);
                    }

                    //Make sure that background color of all fields are transparent after clicking any Select button
                    meetingId.setBackgroundColor(Color.TRANSPARENT);
                    titleEditText.setBackgroundColor(Color.TRANSPARENT);
                    placeEditText.setBackgroundColor(Color.TRANSPARENT);
                    participantsEditText.setBackgroundColor(Color.TRANSPARENT);
                    meetingDateResult.setBackgroundColor(Color.TRANSPARENT);
                    meetingTimeResult.setBackgroundColor(Color.TRANSPARENT);
                }
            });
            updateContainer.addView(selectMeetingButton);
            updateContainer.addView(meetingInfoText);
            updateList.addView(updateContainer);
        }

        //Back to MainActivity
        backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private String getRealPathFromURI(Uri contentUri) {
        String[] projection = { MediaStore.Images.Media.DATA};
        CursorLoader loader = new CursorLoader(getApplicationContext(), contentUri, projection, null, null, null);
        Cursor cursor = loader.loadInBackground();
        assert cursor != null;
        int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(columnIndex);
    }
    //This method will be called when we get back from selecting an image from the gallery
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == galleryRequestCode) {
            if (resultCode == Activity.RESULT_OK) {
                Uri selectedImageUri = data.getData();
                //Here we set the text of imagePathEditText the directory path of the
                //selected image
                String imagePath = getRealPathFromURI(selectedImageUri);
                participantImage = findViewById(R.id.participant_image);
                participantImage.setVisibility(View.VISIBLE);
                InputStream inputStream = null;
                Bitmap bitmap = null;
                try {
                    //We call the decodeFile() method to scale down the image
                    bitmap = decodeFile(selectedImageUri);
                    //Here we set the image of the ImageView to the selected image
                    if (bitmap != null) {
                        participantImage.setImageBitmap(bitmap);
                    } else {
                        displayToast(getString(R.string.null_bitmap));
                    }
                } catch (FileNotFoundException e) {
                    displayToast(e.getLocalizedMessage());
                } catch (IOException e) {
                    displayToast(e.getLocalizedMessage());
                } finally {
                    if (inputStream != null)
                        try {
                            inputStream.close();
                        } catch (IOException e) {
                            displayToast(e.getLocalizedMessage());
                        }
                }
            }
        }
    }
    //Here we decode the image and scales it to reduce memory consumption
    private Bitmap decodeFile(Uri selectedImageUri) throws IOException{
        //Here we open an input stream to access the content of the image
        InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
        //Decode image size
        BitmapFactory.Options imageSizeOptions = new BitmapFactory.Options();
        //If set to true, the decoder will return null (no bitmap), but
        //the out... fields will still be set, allowing the caller to query
        //the bitmap without having to allocate the memory for its pixels.
        imageSizeOptions.inJustDecodeBounds = true;
        //Here we fetch image meta data
        BitmapFactory.decodeStream(inputStream, null, imageSizeOptions);
        //The new size we want to scale to
        final int REQUIRED_SIZE=150;
        //Here we find the correct scale value. It should be a power of 2.
        int scale=1;
        //Here we scale the result height and width of the image based on the required size
        while(imageSizeOptions.outWidth/scale/2>=REQUIRED_SIZE && imageSizeOptions.outHeight/scale/2>=REQUIRED_SIZE)
            scale*=2;
        //Decode with inSampleSize
        BitmapFactory.Options inSampleSizeOption = new BitmapFactory.Options();
        //Here we do the actual decoding. If set to a value > 1, requests the decoder to subsample the
        //original image, returning a smaller image to save memory. The
        //sample size is the number of pixels in either dimension that correspond
        //to a single pixel in the decoded bitmap.
        inSampleSizeOption.inSampleSize=scale;
        inputStream.close();
        //Here we initialize the inputStream again
        inputStream = getContentResolver().openInputStream(selectedImageUri);
        return BitmapFactory.decodeStream(inputStream, null, inSampleSizeOption);
    }

    //Display DatePickerDialog or TimePickerDialog
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

    //Here we define BackgroundTask class, which inherits AsyncTask class.
    //We declare the class as static to prevent possible memory leakage.
    private class BackgroundTask extends AsyncTask<String, Integer, Bitmap> {
        //This method receives an array of URLs as strings and returns
        //an object of type Bitmap. This method calls getInputStream() method
        //which makes HTTP connection to the given URL and returns the input stream
        @Override
        protected Bitmap doInBackground(String...urls) {
            Bitmap bitmap = null;
            try {
                //Here we create an InputStream object
                InputStream inputStream = getInputStream(urls[0]);
                //Here we create a bitmap out of the received input stream
                bitmap = BitmapFactory.decodeStream(inputStream);
                if(bitmap != null)
                    //Here we update the progress of the background job
                    //by passing the number of bytes in the bitmap
                    publishProgress(bitmap.getByteCount());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return bitmap;
        }
        //This method updates the progress
        @Override
        protected void onProgressUpdate(Integer...progress){
            //Here we display the size of the bitmap
            displayToast(getString(R.string.bitmap_size) + progress[0]);
        }
        //This method will be called when the background job has finished
        @Override
        protected void onPostExecute(Bitmap bitmap){
            //Here we se the image of the imageView object
            meetingImage.setImageBitmap(bitmap);
        }
    }
    // Makes HttpURLConnection and returns InputStream
    private InputStream getInputStream(String urlString) throws IOException {
        //Here we declare an object of type InputStream
        InputStream inputStream = null;
        //Here we create an URL object
        URL url = new URL(urlString);
        //Here we make an URL connection
        URLConnection urlConnection = url.openConnection();
        try {
            //Here we create a URLConnection with support for HTTP-specific features
            HttpURLConnection httpConnection = (HttpURLConnection) urlConnection;
            //Here we determine whether if required the system can ask the user
            //for additional input
            httpConnection.setAllowUserInteraction(false);
            // Sets whether HTTP redirects (requests with response code 3xx)
            //should be automatically followed by this HttpURLConnection instance.
            httpConnection.setInstanceFollowRedirects(true);
            //Set the method for the URL request
            httpConnection.setRequestMethod("GET");
            //Here we establish HttpURLConnection
            httpConnection.connect();
            //Here we make sure that the HttpURL connection has been successfully
            //established
            if (httpConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                //Here we get the input stream that reads from this open connection
                inputStream = httpConnection.getInputStream();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return inputStream;
    }

    private void updateMeetingInfoText(Meeting meeting) {
        if (selectedUpdateContainer != null) {
            //REMINDER: Each updateContainer has selectMeetingButton (index 0) and meetingInfoText (index 1) views
            TextView selectedMeetingInfoText = (TextView) selectedUpdateContainer.getChildAt(1);
            StringBuilder sb = new StringBuilder();
            sb.append(meeting.getId());
            sb.append(" - ");
            sb.append(meeting.getTitle());
            sb.append(", at ");
            sb.append(meeting.getStringTime());
            sb.append(" ");
            sb.append(meeting.getStringDate());
            selectedMeetingInfoText.setText(sb.toString());
        }
    }

    //Repaint the activity when it is invoked
    @Override
    protected void onResume() {
        super.onResume();
        repaint();
    }

    private void repaint() {
        //Set user settings from SharedPreferences data while creating the activity
        //Set font size
        appTitleText.setTextSize(userPrefs.getFloat(appTitleFontSize, 28));
        updateSubtitleText.setTextSize(userPrefs.getFloat(subtitleFontSize, 20));
        meetingIdText.setTextSize(userPrefs.getFloat(otherTextsFontSize, 14));
        meetingId.setTextSize(userPrefs.getFloat(otherTextsFontSize, 14));
        meetingTitleText.setTextSize(userPrefs.getFloat(otherTextsFontSize, 14));
        meetingPlaceText.setTextSize(userPrefs.getFloat(otherTextsFontSize, 14));
        meetingParticipantsText.setTextSize(userPrefs.getFloat(otherTextsFontSize, 14));
        meetingDatetimeText.setTextSize(userPrefs.getFloat(otherTextsFontSize, 14));
        meetingDateText.setTextSize(userPrefs.getFloat(otherTextsFontSize, 14));
        meetingDateResult.setTextSize(userPrefs.getFloat(otherTextsFontSize, 14));
        meetingTimeText.setTextSize(userPrefs.getFloat(otherTextsFontSize, 14));
        meetingTimeResult.setTextSize(userPrefs.getFloat(otherTextsFontSize, 14));
        meetingImageUrlText.setTextSize(userPrefs.getFloat(otherTextsFontSize, 14));
        updateOtherSubtitleText.setTextSize(userPrefs.getFloat(subtitleFontSize, 20));
        titleEditText.setTextSize(userPrefs.getFloat(editTextFontSize, 18));
        placeEditText.setTextSize(userPrefs.getFloat(editTextFontSize, 18));
        participantsEditText.setTextSize(userPrefs.getFloat(editTextFontSize, 18));
        meetingImageUrlEditText.setTextSize(userPrefs.getFloat(editTextFontSize, 18));
        chooseImageButton.setTextSize(userPrefs.getFloat(otherTextsFontSize, 14));
        addParticipantButton.setTextSize(userPrefs.getFloat(otherTextsFontSize, 14));
        selectDateButton.setTextSize(userPrefs.getFloat(otherTextsFontSize, 14));
        selectTimeButton.setTextSize(userPrefs.getFloat(otherTextsFontSize, 14));
        getImageButton.setTextSize(userPrefs.getFloat(otherTextsFontSize, 14));
        updateButton.setTextSize(userPrefs.getFloat(otherTextsFontSize, 14));
        deleteButton.setTextSize(userPrefs.getFloat(otherTextsFontSize, 14));
        backButton.setTextSize(userPrefs.getFloat(otherTextsFontSize, 14));
        //Set text color (except texts in Buttons)
        appTitleText.setTextColor(userPrefs.getInt(textColor, getResources().getColor(R.color.black)));
        updateSubtitleText.setTextColor(userPrefs.getInt(textColor, getResources().getColor(R.color.black)));
        meetingIdText.setTextColor(userPrefs.getInt(textColor, getResources().getColor(R.color.black)));
        meetingId.setTextColor(userPrefs.getInt(textColor, getResources().getColor(R.color.black)));
        meetingTitleText.setTextColor(userPrefs.getInt(textColor, getResources().getColor(R.color.black)));
        meetingPlaceText.setTextColor(userPrefs.getInt(textColor, getResources().getColor(R.color.black)));
        meetingParticipantsText.setTextColor(userPrefs.getInt(textColor, getResources().getColor(R.color.black)));
        meetingDatetimeText.setTextColor(userPrefs.getInt(textColor, getResources().getColor(R.color.black)));
        meetingDateText.setTextColor(userPrefs.getInt(textColor, getResources().getColor(R.color.black)));
        meetingDateResult.setTextColor(userPrefs.getInt(textColor, getResources().getColor(R.color.black)));
        meetingTimeText.setTextColor(userPrefs.getInt(textColor, getResources().getColor(R.color.black)));
        meetingTimeResult.setTextColor(userPrefs.getInt(textColor, getResources().getColor(R.color.black)));
        meetingImageUrlText.setTextColor(userPrefs.getInt(textColor, getResources().getColor(R.color.black)));
        updateOtherSubtitleText.setTextColor(userPrefs.getInt(textColor, getResources().getColor(R.color.black)));
        titleEditText.setTextColor(userPrefs.getInt(textColor, getResources().getColor(R.color.black)));
        placeEditText.setTextColor(userPrefs.getInt(textColor, getResources().getColor(R.color.black)));
        participantsEditText.setTextColor(userPrefs.getInt(textColor, getResources().getColor(R.color.black)));
        meetingImageUrlEditText.setTextColor(userPrefs.getInt(textColor, getResources().getColor(R.color.black)));
        //Set background color
        mainLayout.setBackgroundColor(userPrefs.getInt(bgColor, getResources().getColor(R.color.old_paper)));
    }

    private void displayToast(String text) {
        Toast.makeText(UpdateActivity.this, text, Toast.LENGTH_SHORT).show();
    }
}