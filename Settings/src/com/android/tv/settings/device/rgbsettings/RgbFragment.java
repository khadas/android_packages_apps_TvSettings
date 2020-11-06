/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.tv.settings.device.rgbsettings;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import com.android.internal.logging.nano.MetricsProto;
import com.android.tv.settings.R;
import com.android.tv.settings.boardInfo.BoardInfo;
import android.support.v7.preference.TwoStatePreference;
import com.android.tv.settings.SettingsPreferenceFragment;
import android.support.v7.preference.PreferenceCategory;
import com.android.tv.settings.RadioPreference;
import android.os.SystemProperties;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import android.util.Log;
import android.provider.Settings;


public class RgbFragment extends SettingsPreferenceFragment {
    private static final String TAG = RgbFragment.class.getSimpleName();
    private static final boolean DEBUG = false;

    private static final String FAN_RADIO_GROUP = "rgb";
    static final String KEY_RGB_CATEGORY = "rgb_category";
    static final String KEY_RGB_FORMAT_PREFIX = "rgb_format_";
	public static final String ACCESSIBILITY_DISPLAY_COLOR_MATRIX ="accessibility_display_color_matrix";
	private static String color_default = "1.0,0.0,0.0,0.0,0.0,1.0,0.0,0.0,0.0,0.0,1.0,0.0,0.0,0.0,0.0,1.0" ;
	private static String color_standard = "1.5,0.0,0.0,0.0,0.0,1.5,0.0,0.0,0.0,0.0,1.5,0.0,0.0,0.0,0.0,1.0" ;
	private static String color_warm = "2.0,0.0,0.0,0.0,0.0,1.0,0.0,0.0,0.0,0.0,1.0,0.0,0.0,0.0,0.0,1.0" ;
	private static String color_cold = "1.0,0.0,0.0,0.0,0.0,1.0,0.0,0.0,0.0,0.0,2.0,0.0,0.0,0.0,0.0,1.0" ;
    private static String cur_color;


    private static final int INDEX_DEFAULT = 0;
    private static final int INDEX_STANDARD = 1;
    private static final int INDEX_WARM = 2;
    private static final int INDEX_COLD = 3;


    private static final int INDEX_COLOR[] = {
            INDEX_DEFAULT,
            INDEX_STANDARD,
            INDEX_COLD,
            INDEX_WARM,
    };




    private int INDEX_LENGTH = 4;
    private Context mContext;
    private PreferenceCategory mRgbCategoryPref;


    public static RgbFragment newInstance() {
        return new RgbFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

	public static int  getColorSetIndex() {

        int index = -1;
		if(cur_color.equals(color_default))
			index = 0;
		if(cur_color.equals(color_standard))
			index = 1;
		if(cur_color.equals(color_warm))
			index = 2;
		if(cur_color.equals(color_cold))
			index = 3;
        return index;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.rgb_prefs, null);

        mRgbCategoryPref = (PreferenceCategory) findPreference(KEY_RGB_CATEGORY);
	cur_color = Settings.Secure.getString(getContext().getContentResolver(), ACCESSIBILITY_DISPLAY_COLOR_MATRIX);
        if (cur_color == null)
            cur_color = color_default;

        createFormatPreferences();
        //updateFormatPreferencesStates();
    }

    /** Creates and adds radio button for each fan speed. */
    private void createFormatPreferences() {
        final Context themedContext = getPreferenceManager().getContext();

        String[] list= mContext.getResources().getStringArray(R.array.rgb_set_list);
		int setindex = getColorSetIndex();
        for (int i =0; i < INDEX_LENGTH; i++) {
            final RadioPreference radioPreference = new RadioPreference(themedContext);
            radioPreference.setKey(KEY_RGB_FORMAT_PREFIX + INDEX_COLOR[i]);
            radioPreference.setPersistent(false);
            radioPreference.setTitle(list[i]);
            radioPreference.setRadioGroup(FAN_RADIO_GROUP);
			radioPreference.setChecked((i == setindex) ? true : false);
            radioPreference.setLayoutResource(R.layout.preference_reversed_widget);
            mRgbCategoryPref.addPreference(radioPreference);
        }
    }

    private void updateFormatPreferencesStates() {
        for (int i =0; i < INDEX_LENGTH; i++) {
            Preference preference = mRgbCategoryPref.findPreference(KEY_RGB_FORMAT_PREFIX + INDEX_COLOR[i]);
            //preference.setEnabled(getFanEnableProp());
        }
    }




    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
     if (preference instanceof RadioPreference) {
            final RadioPreference radioPreference = (RadioPreference) preference;
            radioPreference.clearOtherRadioPreferences(mRgbCategoryPref);
            if (radioPreference.isChecked()) {
                int index = 0;
                for (int i = 0; i < INDEX_LENGTH; i++) {
                    if(TextUtils.equals(radioPreference.getKey(), KEY_RGB_FORMAT_PREFIX+INDEX_COLOR[i])) {
                        index = i;
                        break;
                    }
                }

                switch (index) {
                    case INDEX_DEFAULT:
                    Settings.Secure.putString(getContext().getContentResolver(), ACCESSIBILITY_DISPLAY_COLOR_MATRIX, color_default);
                    break;
                    case INDEX_STANDARD:
					Settings.Secure.putString(getContext().getContentResolver(), ACCESSIBILITY_DISPLAY_COLOR_MATRIX, color_standard);
					break;
                    case INDEX_COLD:
					Settings.Secure.putString(getContext().getContentResolver(), ACCESSIBILITY_DISPLAY_COLOR_MATRIX, color_cold);
					break;
                    case INDEX_WARM:
					Settings.Secure.putString(getContext().getContentResolver(), ACCESSIBILITY_DISPLAY_COLOR_MATRIX, color_warm);
					break;

                    default:
                        break;
                }
                radioPreference.setChecked(true);
            }else{
                radioPreference.setChecked(true);
            }
        }
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.KHADAS_FAN;
    }
}
