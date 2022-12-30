package com.nhat.meetingcalendar.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.nhat.meetingcalendar.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class AboutActivity extends AppCompatActivity {

    private SharedPreferences userPrefs;
    private String userPrefsFileName;
    private ScrollView mainLayout;
    private TextView appTitleText, helpSubtitleText, aboutSubtitleText;
    private TextView helpDetails, aboutDetails;
    private Button backButton;
    private String appTitleFontSize, subtitleFontSize, otherTextsFontSize;
    private String textColor, bgColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

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
        helpSubtitleText = findViewById(R.id.help_subtitle_text);
        aboutSubtitleText = findViewById(R.id.about_subtitle_text);
        helpDetails = findViewById(R.id.help_details);
        aboutDetails = findViewById(R.id.about_details);
        backButton = findViewById(R.id.back_button);

        //Here we access the resource file (about.txt) under res/raw/ folder
        InputStream is = getResources().openRawResource(R.raw.about);
        //Here we define a BufferedReader object to read the file content
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String copyrightLine = null;
        StringBuilder instruction = new StringBuilder();
        try {
            //Read first line (copyright line) in about.txt
            copyrightLine = br.readLine();
            //Read other lines (instruction lines) in about.txt
            String line = null;
            while((line=br.readLine()) != null) {
                instruction.append(line + "\n");
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Write read text to screen
        aboutDetails.setText(copyrightLine);
        helpDetails.setText(instruction.toString());

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

    private void repaint() {
        //Set user settings from SharedPreferences data while creating the activity
        //Set font size
        appTitleText.setTextSize(userPrefs.getFloat(appTitleFontSize, 28));
        helpSubtitleText.setTextSize(userPrefs.getFloat(subtitleFontSize, 20));
        aboutSubtitleText.setTextSize(userPrefs.getFloat(subtitleFontSize, 20));
        helpDetails.setTextSize(userPrefs.getFloat(otherTextsFontSize, 14));
        aboutDetails.setTextSize(userPrefs.getFloat(otherTextsFontSize, 14));
        backButton.setTextSize(userPrefs.getFloat(otherTextsFontSize, 14));
        //Set text color (except texts in Buttons)
        appTitleText.setTextColor(userPrefs.getInt(textColor, getResources().getColor(R.color.black)));
        helpSubtitleText.setTextColor(userPrefs.getInt(textColor, getResources().getColor(R.color.black)));
        aboutSubtitleText.setTextColor(userPrefs.getInt(textColor, getResources().getColor(R.color.black)));
        helpDetails.setTextColor(userPrefs.getInt(textColor, getResources().getColor(R.color.black)));
        aboutDetails.setTextColor(userPrefs.getInt(textColor, getResources().getColor(R.color.black)));
        //Set background color
        mainLayout.setBackgroundColor(userPrefs.getInt(bgColor, getResources().getColor(R.color.old_paper)));
    }
}