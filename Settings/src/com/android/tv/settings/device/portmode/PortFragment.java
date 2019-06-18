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

package com.android.tv.settings.device.portmode;

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

public class PortFragment extends SettingsPreferenceFragment {
    private static final String TAG = PortFragment.class.getSimpleName();
    private static final String PORT_RADIO_GROUP = "portmode";
    private static final boolean DEBUG = false;
    static final String PORT_FORMAT_PREFIX = "port_format_";
    private static final String PROP_PORT_MODE = "persist.sys.port.mode";
    private static final String PORT_UBOOT_ENV = "ubootenv.var.port_mode";

    private Context mContext;

    private static final int INDEX_USB3 = 0;
    private static final int INDEX_PCIE = 1;
    private static final int DEFAULT_MODE = INDEX_USB3;

    private static final int INDEX_PORT[] = {
            INDEX_USB3,
            INDEX_PCIE,
    };

    private static final String ModeList[] = {
            "USB3.0",
            "PCIE",
    };

    public static PortFragment newInstance() {
        return new PortFragment();
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
        screen.setTitle(R.string.device_portmode);

        String[] list= mContext.getResources().getStringArray(R.array.portmode_title_list);
        int activeIndex = getPortModeProp();
        for (int i =0; i < INDEX_PORT.length; i++) {
            final RadioPreference radioPreference = new RadioPreference(themedContext);
            radioPreference.setKey(PORT_FORMAT_PREFIX + INDEX_PORT[i]);
            radioPreference.setPersistent(false);
            radioPreference.setTitle(list[i]);
            radioPreference.setRadioGroup(PORT_RADIO_GROUP);
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

    private static void setPortModeProp(int mode) {

        SystemProperties.set(PROP_PORT_MODE, String.valueOf(mode));
    }

    private static int getPortModeProp() {

        int mode = SystemProperties.getInt(PROP_PORT_MODE, DEFAULT_MODE);
        return mode;
    }

    private static void setPortModeBootEnv(int mode) {
        String cmd = String.format("setbootenv %s %s\n", PORT_UBOOT_ENV, mode);
        try {
            Process exeCmd = Runtime.getRuntime().exec(cmd);
            exeCmd.getOutputStream().flush();
        } catch (IOException e) {
            Log.e(TAG, "Excute exception: " + e.getMessage());
        }
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference instanceof RadioPreference) {
            final RadioPreference radioPreference = (RadioPreference) preference;
            radioPreference.clearOtherRadioPreferences(getPreferenceScreen());
            if (radioPreference.isChecked()) {
                int index = 0;
                for (int i = 0; i < INDEX_PORT.length; i++) {
                    if(TextUtils.equals(radioPreference.getKey(), PORT_FORMAT_PREFIX+INDEX_PORT[i])) {
                        index = i;
                        break;
                    }
                }
                setPortModeProp(index);
                setPortModeBootEnv(index);
                radioPreference.setChecked(true);
            } else {
                radioPreference.setChecked(true);
            }
        }
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.KHADAS_PORTMODE;
    }
}
