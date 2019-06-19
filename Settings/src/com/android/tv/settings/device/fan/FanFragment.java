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

package com.android.tv.settings.device.fan;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import com.android.internal.logging.nano.MetricsProto;
import com.android.tv.settings.R;
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

public class FanFragment extends SettingsPreferenceFragment {
    private static final String TAG = FanFragment.class.getSimpleName();
    private static final boolean DEBUG = false;
    static final String KEY_FAN = "fan_btn";
    private static final String FAN_RADIO_GROUP = "fan";
    static final String KEY_FAN_CATEGORY = "fan_category";
    static final String KEY_FAN_FORMAT_PREFIX = "fan_format_";

    private static final String PROP_FAN_ENABLE = "persist.sys.fan.enable";
    private static final String PROP_FAN_MODE  = "persist.sys.fan.mode";
    private static final String PROP_FAN_LEVEL = "persist.sys.fan.level";
    private static final String PROP_FAN_INDEX = "persist.sys.fna.index";

    private static final String SYS_FAN_MODE = "/sys/class/fan/mode";
    private static final String SYS_FAN_LEVEL = "/sys/class/fan/level";
    private static final String SYS_FAN_ENABLE = "/sys/class/fan/enable";

    private static final int INDEX_AUTO = 0;
    private static final int INDEX_LEVEL_1 = 1;
    private static final int INDEX_LEVEL_2 = 2;
    private static final int INDEX_LEVEL_3 = 3;

    private static final int INDEX_FAN[] = {
            INDEX_AUTO,
            INDEX_LEVEL_1,
            INDEX_LEVEL_2,
            INDEX_LEVEL_3,
    };

    private static final int MANUAL_MODE = 0;
    private static final int AUTO_MODE = 1;
    private static final int DEFAULT_MODE = AUTO_MODE;

    private static final boolean STATE_DISABLE = false;
    private static final boolean STATE_ENABLE  = true;
    private static final boolean STATE_DEFAULT = STATE_DISABLE;

    private Context mContext;
    private PreferenceCategory mFanCategoryPref;
    private static boolean fanState = false;

    public static FanFragment newInstance() {
        return new FanFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.fan_prefs, null);

        final TwoStatePreference fanPref = (TwoStatePreference) findPreference(KEY_FAN);
        fanPref.setChecked(getFanEnableProp());

        mFanCategoryPref = (PreferenceCategory) findPreference(KEY_FAN_CATEGORY);
        createFormatPreferences();
        updateFormatPreferencesStates();
    }

    /** Creates and adds radio button for each fan speed. */
    private void createFormatPreferences() {
        final Context themedContext = getPreferenceManager().getContext();

        String[] list= mContext.getResources().getStringArray(R.array.fan_title_list);
        int activeIndex = SystemProperties.getInt(PROP_FAN_INDEX, 0);
        for (int i =0; i < INDEX_FAN.length; i++) {
            final RadioPreference radioPreference = new RadioPreference(themedContext);
            radioPreference.setKey(KEY_FAN_FORMAT_PREFIX + INDEX_FAN[i]);
            radioPreference.setPersistent(false);
            radioPreference.setTitle(list[i]);
            radioPreference.setRadioGroup(FAN_RADIO_GROUP);
            radioPreference.setLayoutResource(R.layout.preference_reversed_widget);
            radioPreference.setChecked((i == activeIndex) ? true : false);
            mFanCategoryPref.addPreference(radioPreference);
        }
    }

    private void updateFormatPreferencesStates() {
        for (int i =0; i < INDEX_FAN.length; i++) {
            Preference preference = mFanCategoryPref.findPreference(KEY_FAN_FORMAT_PREFIX + INDEX_FAN[i]);
            preference.setEnabled(getFanEnableProp());
        }
    }

    public static int setFanEnable(boolean enable) {

        if (DEBUG) Log.d(TAG,"setFanEnable: " + enable);

        File file = new File(SYS_FAN_ENABLE);
        if((file == null) || !file.exists()){
            Log.e(TAG, "" + SYS_FAN_ENABLE + " no exist");
            return -1;
        }

        try {
            FileOutputStream fout = new FileOutputStream(file);
            PrintWriter pWriter = new PrintWriter(fout);
            pWriter.println(enable ? "1" : "0");
            pWriter.flush();
            pWriter.close();
            fout.close();

        } catch (IOException e) {

            Log.e(TAG, "setFanEnable ERR: " + e);
            return -1;
        }
        return 0;
    }


    public static int setFanMode(boolean auto) {

        if (DEBUG) Log.d(TAG,"setFanMode: " + auto);

        File file = new File(SYS_FAN_MODE);
        if((file == null) || !file.exists()){
            Log.e(TAG, "" + SYS_FAN_MODE + " no exist");
            return -1;
        }

        try {
            FileOutputStream fout = new FileOutputStream(file);
            PrintWriter pWriter = new PrintWriter(fout);
            pWriter.println(auto ? "1" : "0");
            pWriter.flush();
            pWriter.close();
            fout.close();

        } catch (IOException e) {

            Log.e(TAG, "setFanMode ERR: " + e);
            return -1;
        }
        return 0;
    }


    public static int setFanLevel(int level) {

        if (DEBUG) Log.d(TAG,"setFanLevel: " + level);

        File file = new File(SYS_FAN_LEVEL);
        if((file == null) || !file.exists()){
            Log.e(TAG, "" + SYS_FAN_LEVEL + " no exist");
            return -1;
        }

        try {
            FileOutputStream fout = new FileOutputStream(file);
            PrintWriter pWriter = new PrintWriter(fout);
            pWriter.println(level);
            pWriter.flush();
            pWriter.close();
            fout.close();

        } catch (IOException e) {

            Log.e(TAG, "setFanMode ERR: " + e);
            return -1;
        }
        return 0;
    }

    public static void setFanModeProp(boolean auto) {

        SystemProperties.set(PROP_FAN_MODE, auto ? "1" : "0");

    }
    public static boolean getFanModeProp() {
        int auto = SystemProperties.getInt(PROP_FAN_MODE, DEFAULT_MODE);
        return auto == 1 ? true : false;
    }

    public static void setFanLevelProp(int level) {

        SystemProperties.set(PROP_FAN_LEVEL, String.valueOf(level));
    }

    public static int  getFanLevelProp() {

        int level = SystemProperties.getInt(PROP_FAN_LEVEL, INDEX_LEVEL_1);
        return level;
    }

    public static void setFanEnableProp(boolean enable) {
        SystemProperties.set(PROP_FAN_ENABLE, enable ? "true" : "false");

    }

    public static boolean getFanEnableProp() {
        boolean enable = SystemProperties.getBoolean(PROP_FAN_ENABLE, STATE_DEFAULT);
        return enable;
    }

    public static void setFanIndexProp(int index) {
        SystemProperties.set(PROP_FAN_INDEX, String.valueOf(index));
    }

    public static int getFanIndexProp() {
        int index = SystemProperties.getInt(PROP_FAN_INDEX, INDEX_AUTO);
        return index;
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (TextUtils.equals(preference.getKey(), KEY_FAN)) {
            boolean enabled = ((TwoStatePreference) preference).isChecked();
            setFanEnable(enabled);
            setFanEnableProp(enabled);
            updateFormatPreferencesStates();
        }else if (preference instanceof RadioPreference) {
            final RadioPreference radioPreference = (RadioPreference) preference;
            radioPreference.clearOtherRadioPreferences(mFanCategoryPref);
            if (radioPreference.isChecked()) {
                int index = 0;
                for (int i = 0; i < INDEX_FAN.length; i++) {
                    if(TextUtils.equals(radioPreference.getKey(), KEY_FAN_FORMAT_PREFIX+INDEX_FAN[i])) {
                        index = i;
                        break;
                    }
                }
                switch (index) {
                    case INDEX_AUTO:
                        setFanMode(true);
                        setFanModeProp(true);
                        break;
                    case INDEX_LEVEL_1:
                    case INDEX_LEVEL_2:
                    case INDEX_LEVEL_3:
                        setFanMode(false);
                        setFanLevel(index);
                        setFanModeProp(false);
                        setFanLevelProp(index);
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
