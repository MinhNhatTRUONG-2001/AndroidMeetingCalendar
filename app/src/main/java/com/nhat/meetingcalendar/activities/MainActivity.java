package com.nhat.meetingcalendar.activities;

import android.app.Activity;
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

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.loader.content.CursorLoader;

import com.google.android.material.button.MaterialButton;
import com.nhat.meetingcalendar.R;
import com.nhat.meetingcalendar.db_adapter.DBAdapter;
import com.nhat.meetingcalendar.fragments.DatePickerFragment;
import com.nhat.meetingcalendar.fragments.TimePickerFragment;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    private DBAdapter dbAdapter;
    private SharedPreferences userPrefs;
    private String userPrefsFileName;
    private ArrayList<String> participants = new ArrayList<>(); //Holds a temporary list of participants (while adding a meeting)
    private ArrayList<Bitmap> images = new ArrayList<>(); //Holds a temporary list of participant images (while adding a meeting)
    private DialogFragment datetimeFragment;
    private String appTitleFontSize, subtitleFontSize, editTextFontSize, otherTextsFontSize;
    private String textColor, bgColor;
    private ScrollView mainLayout;
    private TextView appTitleText, addSubtitleText, manageSubtitleText;
    private TextView meetingTitleText, meetingPlaceText, meetingParticipantsText;
    private TextView meetingDatetimeText, meetingDateText, meetingTimeText;
    private TextView meetingDateResult, meetingTimeResult, meetingImageUrlText;
    private EditText titleEditText, placeEditText, participantsEditText, meetingImageUrlEditText;
    private Button addParticipantButton, chooseImageButton, selectDateButton, selectTimeButton, getImageButton, submitButton;
    private Button manageSummaryButton, manageSearchButton, manageUpdateButton, manageSettingsButton, manageAboutButton;
    private ImageView participantImage, meetingImage;
    private Bitmap imageBitmap = null;
    private int galleryRequestCode = 1;
    private BackgroundTask backgroundTask = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

        //Find necessary meeting views in the form
        titleEditText = findViewById(R.id.meeting_title_edit);
        placeEditText = findViewById(R.id.meeting_place_edit);
        participantsEditText = findViewById(R.id.meeting_participants_edit);
        meetingDateResult = findViewById(R.id.meeting_date_result);
        meetingTimeResult = findViewById(R.id.meeting_time_result);
        meetingImageUrlEditText = findViewById(R.id.meeting_image_url_edit);

        //Find other views for changing size and color only
        mainLayout = findViewById(R.id.main_layout);
        appTitleText = findViewById(R.id.app_title_text);
        addSubtitleText = findViewById(R.id.add_subtitle_text);
        meetingTitleText = findViewById(R.id.meeting_title_text);
        meetingPlaceText = findViewById(R.id.meeting_place_text);
        meetingParticipantsText = findViewById(R.id.meeting_participants_text);
        meetingDatetimeText = findViewById(R.id.meeting_datetime_text);
        meetingDateText = findViewById(R.id.meeting_date_text);
        meetingTimeText = findViewById(R.id.meeting_time_text);
        meetingImageUrlText = findViewById(R.id.meeting_image_url_text);
        manageSubtitleText = findViewById(R.id.manage_subtitle_text);

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
                    //Each smaller LinearLayout contains a participant name text, an image and a delete button
                    LinearLayout participantContainer = new LinearLayout(MainActivity.this);
                    participantContainer.setOrientation(LinearLayout.HORIZONTAL);
                    participantContainer.setLayoutParams(participantContainerParams);
                    ImageView pImg = null;
                    if (imageBitmap != null) {
                        pImg = new ImageView(MainActivity.this);
                        pImg.setLayoutParams(pImgLayoutParams);
                        pImg.setImageBitmap(imageBitmap);
                    }
                    TextView participantText = new TextView(MainActivity.this);
                    participantText.setText(participantsEditText.getText());
                    participantText.setTextSize(userPrefs.getFloat(otherTextsFontSize, 14));
                    participantText.setTextColor(userPrefs.getInt(textColor, getResources().getColor(R.color.black)));
                    MaterialButton deleteParticipantButton = new MaterialButton(MainActivity.this);
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

        //Check user-provided meeting data in the form, then write these data to a new Meeting object and add to "meetings" ArrayList
        submitButton = findViewById(R.id.add_meeting_button);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String title = titleEditText.getText().toString().trim();
                String place = placeEditText.getText().toString().trim();
                String[] participantsArr = participants.toArray(new String[0]);
                String meetingDateResultStr = meetingDateResult.getText().toString();
                String meetingTimeResultStr = meetingTimeResult.getText().toString();
                //Check user-provided meeting data
                if (title.isEmpty() || place.isEmpty() || participantsArr.length == 0 || meetingDateResultStr.isEmpty() || meetingTimeResultStr.isEmpty()) {
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
                    //Insert new meeting to database
                    String participantsStr = String.join("%#%", participantsArr);
                    String datetimeStr = meetingDateResultStr + "%#%" + meetingTimeResultStr;
                    Bitmap meetingImageBitmap = ((BitmapDrawable) meetingImage.getDrawable()).getBitmap();
                    long id = dbAdapter.addMeeting(title, place, participantsStr, datetimeStr, meetingImageBitmap);
                    if (id == -1) {
                        displayToast(getString(R.string.db_add_error));
                    }
                    else {
                        for (int i = 0; i < participants.size(); i++) {
                            dbAdapter.addParticipantImage(id, participants.get(i), images.get(i));
                        }
                        displayToast(getString(R.string.meeting_added) + id);
                        //Reset form
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

            }
        });

        //Add event listeners to buttons in "Manage meetings"
        manageSummaryButton = findViewById(R.id.manage_summary_button);
        manageSummaryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent summaryIntent = new Intent(getApplication(), SummaryActivity.class);
                startActivity(summaryIntent);
            }
        });
        manageSearchButton = findViewById(R.id.manage_search_button);
        manageSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent searchIntent = new Intent(getApplication(), SearchActivity.class);
                startActivity(searchIntent);
            }
        });
        manageUpdateButton = findViewById(R.id.manage_update_button);
        manageUpdateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent updateIntent = new Intent(getApplication(), UpdateActivity.class);
                startActivity(updateIntent);
            }
        });
        manageSettingsButton = findViewById(R.id.manage_settings_button);
        manageSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent settingsIntent = new Intent(getApplication(), SettingsActivity.class);
                startActivity(settingsIntent);
            }
        });
        manageAboutButton = findViewById(R.id.manage_about_button);
        manageAboutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent aboutIntent = new Intent(getApplication(), AboutActivity.class);
                startActivity(aboutIntent);
            }
        });
    }


    //Repaint the activity when backing from other activities
    @Override
    protected void onResume() {
        super.onResume();
        repaint();
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
            meetingImage.setVisibility(View.VISIBLE);
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

    private void displayToast(String text) {
        Toast.makeText(MainActivity.this, text, Toast.LENGTH_LONG).show();
    }

    private void repaint() {
        //Set user settings from SharedPreferences data while creating (or backing to) the activity
        //Set font size
        appTitleText.setTextSize(userPrefs.getFloat(appTitleFontSize, 28));
        addSubtitleText.setTextSize(userPrefs.getFloat(subtitleFontSize, 20));
        meetingTitleText.setTextSize(userPrefs.getFloat(otherTextsFontSize, 14));
        meetingPlaceText.setTextSize(userPrefs.getFloat(otherTextsFontSize, 14));
        meetingParticipantsText.setTextSize(userPrefs.getFloat(otherTextsFontSize, 14));
        meetingDatetimeText.setTextSize(userPrefs.getFloat(otherTextsFontSize, 14));
        meetingDateText.setTextSize(userPrefs.getFloat(otherTextsFontSize, 14));
        meetingDateResult.setTextSize(userPrefs.getFloat(otherTextsFontSize, 14));
        meetingTimeText.setTextSize(userPrefs.getFloat(otherTextsFontSize, 14));
        meetingTimeResult.setTextSize(userPrefs.getFloat(otherTextsFontSize, 14));
        meetingImageUrlText.setTextSize(userPrefs.getFloat(otherTextsFontSize, 14));
        manageSubtitleText.setTextSize(userPrefs.getFloat(subtitleFontSize, 20));
        titleEditText.setTextSize(userPrefs.getFloat(editTextFontSize, 18));
        placeEditText.setTextSize(userPrefs.getFloat(editTextFontSize, 18));
        participantsEditText.setTextSize(userPrefs.getFloat(editTextFontSize, 18));
        meetingImageUrlEditText.setTextSize(userPrefs.getFloat(editTextFontSize, 18));
        addParticipantButton.setTextSize(userPrefs.getFloat(otherTextsFontSize, 14));
        chooseImageButton.setTextSize(userPrefs.getFloat(otherTextsFontSize, 14));
        selectDateButton.setTextSize(userPrefs.getFloat(otherTextsFontSize, 14));
        selectTimeButton.setTextSize(userPrefs.getFloat(otherTextsFontSize, 14));
        getImageButton.setTextSize(userPrefs.getFloat(otherTextsFontSize, 14));
        submitButton.setTextSize(userPrefs.getFloat(otherTextsFontSize, 14));
        manageSummaryButton.setTextSize(userPrefs.getFloat(otherTextsFontSize, 14));
        manageSearchButton.setTextSize(userPrefs.getFloat(otherTextsFontSize, 14));
        manageUpdateButton.setTextSize(userPrefs.getFloat(otherTextsFontSize, 14));
        manageSettingsButton.setTextSize(userPrefs.getFloat(otherTextsFontSize, 14));
        manageAboutButton.setTextSize(userPrefs.getFloat(otherTextsFontSize, 14));
        //Set text color (except texts in Buttons)
        appTitleText.setTextColor(userPrefs.getInt(textColor, getResources().getColor(R.color.black)));
        addSubtitleText.setTextColor(userPrefs.getInt(textColor, getResources().getColor(R.color.black)));
        meetingTitleText.setTextColor(userPrefs.getInt(textColor, getResources().getColor(R.color.black)));
        meetingPlaceText.setTextColor(userPrefs.getInt(textColor, getResources().getColor(R.color.black)));
        meetingParticipantsText.setTextColor(userPrefs.getInt(textColor, getResources().getColor(R.color.black)));
        meetingDatetimeText.setTextColor(userPrefs.getInt(textColor, getResources().getColor(R.color.black)));
        meetingDateText.setTextColor(userPrefs.getInt(textColor, getResources().getColor(R.color.black)));
        meetingDateResult.setTextColor(userPrefs.getInt(textColor, getResources().getColor(R.color.black)));
        meetingTimeText.setTextColor(userPrefs.getInt(textColor, getResources().getColor(R.color.black)));
        meetingTimeResult.setTextColor(userPrefs.getInt(textColor, getResources().getColor(R.color.black)));
        meetingImageUrlText.setTextColor(userPrefs.getInt(textColor, getResources().getColor(R.color.black)));
        manageSubtitleText.setTextColor(userPrefs.getInt(textColor, getResources().getColor(R.color.black)));
        titleEditText.setTextColor(userPrefs.getInt(textColor, getResources().getColor(R.color.black)));
        placeEditText.setTextColor(userPrefs.getInt(textColor, getResources().getColor(R.color.black)));
        participantsEditText.setTextColor(userPrefs.getInt(textColor, getResources().getColor(R.color.black)));
        meetingImageUrlEditText.setTextColor(userPrefs.getInt(textColor, getResources().getColor(R.color.black)));
        //Set background color
        mainLayout.setBackgroundColor(userPrefs.getInt(bgColor, getResources().getColor(R.color.old_paper)));
    }
}