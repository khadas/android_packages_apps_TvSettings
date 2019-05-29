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

package com.android.tv.settings.device.wol;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import com.android.internal.logging.nano.MetricsProto;
import com.android.tv.settings.R;
import com.android.tv.settings.SettingsPreferenceFragment;
import android.support.v7.preference.TwoStatePreference;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import android.util.Log;

public class WolFragment extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {
    private static final String TAG = "WolSettings";
    static final String KEY_WOL = "wol_btn";
    private static final String WOL_STATE_SYS = "/sys/class/wol/enable";

    private Context mContext;

    public static WolFragment newInstance() {
        return new WolFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.wol_prefs, null);

        final TwoStatePreference wolPref = (TwoStatePreference) findPreference(KEY_WOL);
        wolPref.setOnPreferenceChangeListener(this);
        wolPref.setChecked(getWolState());
    }

    private boolean getWolState() {
        boolean enabled = false;
        try {
            FileReader fread = new FileReader(WOL_STATE_SYS);
            BufferedReader buffer = new BufferedReader(fread);
            String str = null;
            while ((str = buffer.readLine()) != null) {
                if (str.equals("1")) {
                    enabled = true;
                    break;
                } else {
                    enabled = false;
                }
            }
            buffer.close();
            fread.close();
        } catch (IOException e) {
            Log.e(TAG, "IO Exception");
        }
        return enabled;
    }

    private void setWolState(boolean enabled) {
        try {
            RandomAccessFile rdf = null;
            rdf = new RandomAccessFile(WOL_STATE_SYS, "rw");
            rdf.writeBytes(enabled ? "1" : "0");
            rdf.close();
        } catch (IOException re) {
            Log.e(TAG, "IO Exception");
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (TextUtils.equals(preference.getKey(), KEY_WOL)) {
            boolean enabled = (boolean) newValue;
            setWolState(enabled);
        }
        return true;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.DATE_TIME;
    }
}
