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

package com.android.tv.settings.device.led;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.preference.Preference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.PreferenceScreen;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;
import android.util.Log;

import com.android.internal.logging.nano.MetricsProto;
import com.android.tv.settings.R;
import com.android.tv.settings.SettingsPreferenceFragment;
import com.android.tv.settings.RadioPreference;
import com.android.tv.settings.boardInfo.BoardInfo;
import android.os.SystemProperties;

import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class LedFragment extends SettingsPreferenceFragment
         implements Preference.OnPreferenceChangeListener {
    private static final String TAG = LedFragment.class.getSimpleName();
    private static final String LED_RADIO_GROUP = "led";
    private static final boolean DEBUG = false;
    private static final String PROP_LED_WHITE_TRIGGER = "persist.sys.white.led.trigger";
    private static final String PROP_LED_RED_MODE = "persist.sys.red.led.mode";
    private static final String SYS_LED_WHITE_TRIGGER = "/sys/class/leds/sys_led/trigger";
    private static final String SYS_LED_RED_MODE = "/sys/class/redled/mode";
    private static final String KEY_LED_WHITE = "whiteLed";
    private static final String KEY_LED_RED = "redLed";
    private BoardInfo mBoardInfo;
    private static int mLedType;

    private Context mContext;

    private static final int INDEX_HEARTBEAT = 0;
    private static final int INDEX_ON = 1;
    private static final int INDEX_OFF = 2;
    private static final int DEFAULT_MODE = INDEX_ON;
    public static final int LED_WHITE = 0;
    public static final int LED_RED   = 1;

    private static final int INDEX_LED[] = {
            INDEX_HEARTBEAT,
            INDEX_ON,
            INDEX_OFF,
    };

    private static final String ModeList[] = {
            "heartbeat",
            "default-on",
            "off",
    };

    public static LedFragment newInstance() {
        return new LedFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        int mode = 0;
        setPreferencesFromResource(R.xml.leds, null);
        String[] list= mContext.getResources().getStringArray(R.array.led_title_entries);
        final ListPreference whitePref = (ListPreference) findPreference(KEY_LED_WHITE);
        final ListPreference redPref = (ListPreference) findPreference(KEY_LED_RED);
        mBoardInfo = new BoardInfo();
        mLedType = mBoardInfo.getLedType();
        if(mLedType == BoardInfo.LED_BOTH) {
            mode = getLedModeProp(LED_WHITE);
            whitePref.setValue(Integer.toString(mode));
            whitePref.setSummary(list[mode]);
            whitePref.setOnPreferenceChangeListener(this);

            mode = getLedModeProp(LED_RED);
            redPref.setValue(Integer.toString(mode));
            redPref.setSummary(list[mode]);
            redPref.setOnPreferenceChangeListener(this);
        } else if (mLedType == BoardInfo.LED_WHITE){
            mode = getLedModeProp(LED_WHITE);
            whitePref.setValue(Integer.toString(mode));
            whitePref.setSummary(list[mode]);
            whitePref.setOnPreferenceChangeListener(this);
            if (redPref != null) {
                redPref.setVisible(false);
            }
        } else {
            mode = getLedModeProp(LED_RED);
            redPref.setValue(Integer.toString(mode));
            redPref.setSummary(list[mode]);
            redPref.setOnPreferenceChangeListener(this);
            if (whitePref != null) {
                whitePref.setVisible(false);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshFromBackend();
    }

    public static int setLedMode(int type, int mode) {

        if (DEBUG) Log.d(TAG,"setLedMode: " + mode);

        try {
            BufferedWriter bufWriter = null;
            if (mLedType == BoardInfo.LED_RED) {
                bufWriter = new BufferedWriter(new FileWriter(SYS_LED_WHITE_TRIGGER));
                bufWriter.write(ModeList[mode]);
            } else {
                if (type == LED_WHITE) {
                   bufWriter = new BufferedWriter(new FileWriter(SYS_LED_WHITE_TRIGGER));
                   bufWriter.write(ModeList[mode]);
                } else {
                   bufWriter = new BufferedWriter(new FileWriter(SYS_LED_RED_MODE));
                   bufWriter.write(String.valueOf(INDEX_LED[mode]));
                }
            }
            bufWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG,"can't write the led node");
            return -1;
        }
        return 0;
    }

    public static void setLedModeProp(int type, int mode) {

        if (type == LED_WHITE)
            SystemProperties.set(PROP_LED_WHITE_TRIGGER, String.valueOf(mode));
        else
            SystemProperties.set(PROP_LED_RED_MODE, String.valueOf(mode));
    }

    public static int getLedModeProp(int type) {

        int mode;
        if (type == LED_WHITE)
            mode = SystemProperties.getInt(PROP_LED_WHITE_TRIGGER, DEFAULT_MODE);
        else
            mode = SystemProperties.getInt(PROP_LED_RED_MODE, DEFAULT_MODE);
        return mode;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        int mode = Integer.parseInt((String) newValue);
        switch (preference.getKey()) {
             case KEY_LED_WHITE:
                  setLedMode(LED_WHITE, mode);
                  setLedModeProp(LED_WHITE, mode);
                  break;
             case KEY_LED_RED:
                  setLedMode(LED_RED, mode);
                  setLedModeProp(LED_RED, mode);
                  break;
        }
        return true;
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        return super.onPreferenceTreeClick(preference);
    }

    private void refreshFromBackend() {
        int mode;
        if (getActivity() == null) {
            Log.d(TAG, "No activity, not refreshing");
            return;
        }
        String[] list= mContext.getResources().getStringArray(R.array.led_title_entries);
        final ListPreference whitePref = (ListPreference) findPreference(KEY_LED_WHITE);
        if (whitePref != null) {
            mode = getLedModeProp(LED_WHITE);
            whitePref.setValue(Integer.toString(mode));
            whitePref.setSummary(list[mode]);
        }
        final ListPreference redPref = (ListPreference) findPreference(KEY_LED_RED);
        if (redPref != null) {
            mode = getLedModeProp(LED_RED);
            redPref.setValue(Integer.toString(mode));
            redPref.setSummary(list[mode]);
        }
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.KHADAS_LED;
    }
}
