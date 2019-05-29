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
import android.os.SystemProperties;

import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class LedFragment extends SettingsPreferenceFragment {
    private static final String TAG = LedFragment.class.getSimpleName();
    private static final String LED_RADIO_GROUP = "led";
    private static final boolean DEBUG = false;
    private static final String PROP_LED_TRIGGER = "persist.sys.led.trigger";
    private static final String SYS_LED_TRIGGER = "/sys/class/leds/sys_led/trigger";

    private Context mContext;

    private static final int INDEX_HEARTBEAT = 0;
    private static final int INDEX_ON = 1;
    private static final int INDEX_OFF = 2;
    private static final int DEFAULT_MODE = INDEX_ON;

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
        final Context themedContext = getPreferenceManager().getContext();
        final PreferenceScreen screen =
                getPreferenceManager().createPreferenceScreen(themedContext);
        screen.setTitle(R.string.device_led);

        String[] list= mContext.getResources().getStringArray(R.array.led_title_list);
        int activeIndex = getLedModeProp();
        for (int i =0; i < INDEX_LED.length; i++) {
            final RadioPreference radioPreference = new RadioPreference(themedContext);
            radioPreference.setKey("led_mode_" + INDEX_LED[i]);
            radioPreference.setPersistent(false);
            radioPreference.setTitle(list[i]);
            radioPreference.setRadioGroup(LED_RADIO_GROUP);
            radioPreference.setLayoutResource(R.layout.preference_reversed_widget);
            radioPreference.setChecked((i == activeIndex) ? true : false);
            screen.addPreference(radioPreference);
        }

        setPreferenceScreen(screen);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public static int setLedMode(int mode) {

        if (DEBUG) Log.d(TAG,"setLedMode: " + mode);

        try {
            BufferedWriter bufWriter = null;
            bufWriter = new BufferedWriter(new FileWriter(SYS_LED_TRIGGER));
            bufWriter.write(ModeList[mode]);
            bufWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG,"can't write the " + SYS_LED_TRIGGER);
            return -1;
        }
        return 0;
    }

    public static void setLedModeProp(int mode) {

        SystemProperties.set(PROP_LED_TRIGGER, String.valueOf(mode));
    }

    public static int getLedModeProp() {

        int mode = SystemProperties.getInt(PROP_LED_TRIGGER, DEFAULT_MODE);
        return mode;
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference instanceof RadioPreference) {
            final RadioPreference radioPreference = (RadioPreference) preference;
            radioPreference.clearOtherRadioPreferences(getPreferenceScreen());
            if (radioPreference.isChecked()) {
                int index = 0;
                for (int i = 0; i < INDEX_LED.length; i++) {
                    if(TextUtils.equals(radioPreference.getKey(), "led_mode_"+INDEX_LED[i])) {
                        index = i;
                        break;
                    }
                }
                setLedMode(index);
                setLedModeProp(index);
                radioPreference.setChecked(true);
            } else {
                radioPreference.setChecked(true);
            }
        }
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.KHADAS_LED;
    }
}
