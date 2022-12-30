package com.nhat.meetingcalendar.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.nhat.meetingcalendar.R;

public class SettingsActivity extends AppCompatActivity {

    private SharedPreferences userPrefs;
    private String userPrefsFileName;
    private SharedPreferences.Editor editor;
    private RadioGroup themeRadioGroup, themeRadioGroup2;
    private ScrollView mainLayout;
    private TextView appTitleText, subtitleText, fontSizeText, themeText;
    private LinearLayout fontSizeSeekbarTextLayout;
    private Button applyButton, backButton;
    private String fontSizeProgress, themeCheckedId;
    private String appTitleFontSize, subtitleFontSize, editTextFontSize, otherTextsFontSize;
    private String textColor, bgColor;
    private float tempAppTitleFontSize, tempSubtitleFontSize, tempEditTextFontSize, tempOtherTextsFontSize;
    private int tempTextColorValue, tempBgColorValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        //Set SharedPreferences keys
        fontSizeProgress = getString(R.string.font_size_progress_prefs);
        appTitleFontSize = getString(R.string.app_title_font_size_prefs);
        subtitleFontSize = getString(R.string.subtitle_font_size_prefs);
        editTextFontSize = getString(R.string.edittext_font_size_prefs);
        otherTextsFontSize = getString(R.string.other_texts_font_size_prefs);
        textColor = getString(R.string.text_color_prefs);
        bgColor = getString(R.string.bg_color_prefs);
        themeCheckedId = getString(R.string.theme_checked_id_prefs);

        //Initialize SharedPreferences and Editor
        userPrefsFileName = getResources().getString(R.string.user_prefs_file_name);
        userPrefs = getSharedPreferences(userPrefsFileName, MODE_PRIVATE);
        editor = userPrefs.edit();

        //Set temp values while creating the activity
        tempAppTitleFontSize = userPrefs.getFloat(appTitleFontSize, 28);
        tempSubtitleFontSize = userPrefs.getFloat(subtitleFontSize, 20);
        tempEditTextFontSize = userPrefs.getFloat(editTextFontSize, 18);
        tempOtherTextsFontSize = userPrefs.getFloat(otherTextsFontSize, 14);
        tempTextColorValue = userPrefs.getInt(textColor, getResources().getColor(R.color.black));
        tempBgColorValue = userPrefs.getInt(bgColor, getResources().getColor(R.color.old_paper));

        //Find setting views
        SeekBar fontSizeSeekBar = findViewById(R.id.font_size_seekbar);
        themeRadioGroup = findViewById(R.id.theme_radiogroup);
        themeRadioGroup2 = findViewById(R.id.theme_radiogroup_2);

        //Find other views for changing size and color
        mainLayout = findViewById(R.id.main_layout);
        appTitleText = findViewById(R.id.app_title_text);
        subtitleText = findViewById(R.id.settings_subtitle_text);
        fontSizeText = findViewById(R.id.font_size_text);
        fontSizeSeekbarTextLayout = findViewById(R.id.font_size_seekbar_text_layout);
        themeText = findViewById(R.id.theme_text);

        //Set previously checked radio button for Theme
        RadioButton preCheckedRadioButton = findViewById(userPrefs.getInt(themeCheckedId, -1));
        if (preCheckedRadioButton != null) {
            preCheckedRadioButton.setChecked(true);
        }

        //Set temporary settings
        fontSizeSeekBar.setProgress(userPrefs.getInt(fontSizeProgress, 1));
        fontSizeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                editor.putInt(fontSizeProgress, progress);
                switch (progress) {
                    case 0:
                        setTempTextSizeValues(24, 17, 16, 12);
                        break;
                    case 1:
                        setTempTextSizeValues(28, 20, 18, 14);
                        break;
                    case 2:
                        setTempTextSizeValues(32, 23, 20, 16);
                        break;
                    case 3:
                        setTempTextSizeValues(36, 27, 22, 18);
                        break;
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //Do nothing
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //Do nothing
            }
        });

        //Set OnCheckedChangeListener for RadioGroups
        themeRadioGroup.setOnCheckedChangeListener(themeChangedListener);
        themeRadioGroup2.setOnCheckedChangeListener(themeChangedListener2);

        //Get Apply and Back buttons
        applyButton = findViewById(R.id.apply_button);
        backButton = findViewById(R.id.back_button);

        applyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveTextSizeValues(tempAppTitleFontSize, tempSubtitleFontSize, tempEditTextFontSize, tempOtherTextsFontSize);
                saveColorValues(tempTextColorValue, tempBgColorValue);
                repaint();
                displayToast(getResources().getString(R.string.settings_applied));
            }
        });

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

    private void setTempTextSizeValues(float appTitle, float subtitle, float editText, float other) {
        tempAppTitleFontSize = appTitle;
        tempSubtitleFontSize = subtitle;
        tempEditTextFontSize = editText;
        tempOtherTextsFontSize = other;
    }

    private void saveTextSizeValues(float appTitle, float subtitle, float editText, float other) {
        editor.putFloat(appTitleFontSize, appTitle);
        editor.putFloat(subtitleFontSize, subtitle);
        editor.putFloat(editTextFontSize, editText);
        editor.putFloat(otherTextsFontSize, other);
        editor.commit();
    }

    private void setTempColorValues(int text, int bg) {
        tempTextColorValue = text;
        tempBgColorValue = bg;
    }

    private void saveColorValues(int text, int bg) {
        editor.putInt(textColor, text);
        editor.putInt(bgColor, bg);
        editor.commit();
    }

    private RadioGroup.OnCheckedChangeListener themeChangedListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
            editor.putInt(themeCheckedId, checkedId);
            RadioButton radioButton = findViewById(checkedId);
            if (radioButton != null) {
                int textColorValue, bgColorValue;
                switch (radioButton.getText().toString()) {
                    case "Black":
                        textColorValue = getResources().getColor(R.color.white);
                        bgColorValue = getResources().getColor(R.color.black);
                        break;
                    case "White":
                        textColorValue = getResources().getColor(R.color.black);
                        bgColorValue = getResources().getColor(R.color.white);
                        break;
                    case "Pink":
                        textColorValue = getResources().getColor(R.color.black);
                        bgColorValue = getResources().getColor(R.color.pink);
                        break;
                    case "Purple":
                        textColorValue = getResources().getColor(R.color.white);
                        bgColorValue = getResources().getColor(R.color.purple_700);
                        break;
                    default:
                        textColorValue = getResources().getColor(R.color.black);
                        bgColorValue = getResources().getColor(R.color.old_paper);
                }
                if (radioGroup.getId() == themeRadioGroup.getId()) {
                    //Clear check without triggering OnCheckedChangeListener on other RadioGroup
                    themeRadioGroup2.setOnCheckedChangeListener(null);
                    themeRadioGroup2.clearCheck();
                    themeRadioGroup2.setOnCheckedChangeListener(themeChangedListener2);
                    setTempColorValues(textColorValue, bgColorValue);
                }
            }
        }
    };
    private RadioGroup.OnCheckedChangeListener themeChangedListener2 = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
            editor.putInt(themeCheckedId, checkedId);
            RadioButton radioButton = findViewById(checkedId);
            if (radioButton != null) {
                int textColorValue, bgColorValue;
                switch (radioButton.getText().toString()) {
                    case "Orange":
                        textColorValue = getResources().getColor(R.color.black);
                        bgColorValue = getResources().getColor(R.color.orange);
                        break;
                    case "Yellow":
                        textColorValue = getResources().getColor(R.color.black);
                        bgColorValue = getResources().getColor(R.color.yellow);
                        break;
                    case "Cyan":
                        textColorValue = getResources().getColor(R.color.purple_700);
                        bgColorValue = getResources().getColor(R.color.cyan);
                        break;
                    case "Green":
                        textColorValue = getResources().getColor(R.color.blue);
                        bgColorValue = getResources().getColor(R.color.lawn_green);
                        break;
                    case "Blue":
                        textColorValue = getResources().getColor(R.color.white);
                        bgColorValue = getResources().getColor(R.color.blue);
                        break;
                    default:
                        textColorValue = getResources().getColor(R.color.black);
                        bgColorValue = getResources().getColor(R.color.old_paper);
                }
                if (radioGroup.getId() == themeRadioGroup2.getId()) {
                    //Clear check without triggering OnCheckedChangeListener on other RadioGroup
                    themeRadioGroup.setOnCheckedChangeListener(null);
                    themeRadioGroup.clearCheck();
                    themeRadioGroup.setOnCheckedChangeListener(themeChangedListener);
                    setTempColorValues(textColorValue, bgColorValue);
                }
            }
        }
    };

    private void repaint() {
        //Set user settings from SharedPreferences data while creating the activity
        //Set font size
        appTitleText.setTextSize(userPrefs.getFloat(appTitleFontSize, 28));
        subtitleText.setTextSize(userPrefs.getFloat(subtitleFontSize, 20));
        fontSizeText.setTextSize(userPrefs.getFloat(otherTextsFontSize, 14));
        for (int i = 0; i < fontSizeSeekbarTextLayout.getChildCount(); i++) {
            View v = fontSizeSeekbarTextLayout.getChildAt(i);
            if (v instanceof TextView) {
                TextView tv = (TextView) v;
                tv.setTextSize(userPrefs.getFloat(otherTextsFontSize, 14));
            }
        }
        themeText.setTextSize(userPrefs.getFloat(otherTextsFontSize, 14));
        for (int i = 0; i < themeRadioGroup.getChildCount(); i++) {
            RadioButton rb = (RadioButton) themeRadioGroup.getChildAt(i);
            rb.setTextSize(userPrefs.getFloat(otherTextsFontSize, 14));
        }
        for (int i = 0; i < themeRadioGroup2.getChildCount(); i++) {
            RadioButton rb = (RadioButton) themeRadioGroup2.getChildAt(i);
            rb.setTextSize(userPrefs.getFloat(otherTextsFontSize, 14));
        }
        applyButton.setTextSize(userPrefs.getFloat(otherTextsFontSize, 14));
        backButton.setTextSize(userPrefs.getFloat(otherTextsFontSize, 14));
        //Set text color (except texts in RadioGroups and Buttons)
        appTitleText.setTextColor(userPrefs.getInt(textColor, getResources().getColor(R.color.black)));
        subtitleText.setTextColor(userPrefs.getInt(textColor, getResources().getColor(R.color.black)));
        fontSizeText.setTextColor(userPrefs.getInt(textColor, getResources().getColor(R.color.black)));
        for (int i = 0; i < fontSizeSeekbarTextLayout.getChildCount(); i++) {
            View v = fontSizeSeekbarTextLayout.getChildAt(i);
            if (v instanceof TextView) {
                TextView tv = (TextView) v;
                tv.setTextColor(userPrefs.getInt(textColor, getResources().getColor(R.color.black)));
            }
        }
        themeText.setTextColor(userPrefs.getInt(textColor, getResources().getColor(R.color.black)));
        //Set background color
        mainLayout.setBackgroundColor(userPrefs.getInt(bgColor, getResources().getColor(R.color.old_paper)));
    }

    private void displayToast(String text) {
        Toast.makeText(SettingsActivity.this, text, Toast.LENGTH_SHORT).show();
    }
}