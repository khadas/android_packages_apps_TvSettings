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

package com.android.tv.settings.device.camera;

import android.provider.Settings;
import android.app.ActivityManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.preference.TwoStatePreference;
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

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;


public class CameraIRCutFragment extends SettingsPreferenceFragment
         implements Preference.OnPreferenceChangeListener {
    private static final String TAG = CameraIRCutFragment.class.getSimpleName();
    private static final String KEY_IRCUT = "camera_ircut_btn";
    private static final String SYS_CAMERA_IRCUT = "/sys/class/camera/ircut";
    private static final boolean DEBUG = false;

    private Context mContext;
    private TwoStatePreference mIRCutPreference;

    public static CameraIRCutFragment newInstance() {
        return new CameraIRCutFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.camera_ircut, null);
        mIRCutPreference = (TwoStatePreference) findPreference(KEY_IRCUT);
        mIRCutPreference.setOnPreferenceChangeListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateState();
    }

    private void updateState() {
        if (mIRCutPreference !=null) {
           boolean enable = getIRCut();
           mIRCutPreference.setChecked(enable);
        }
    }

    private int setIRCut(boolean enable) {
        File file = new File(SYS_CAMERA_IRCUT);
        if((file == null) || !file.exists()) {
            Log.e(TAG, "" + SYS_CAMERA_IRCUT + " no exist");
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
            Log.e(TAG, "setIRCut ERR: " + e);
            return -1;
        }
        return 0;
    }

    private boolean getIRCut() {
        boolean enable = false;
        try {
             FileReader fread = new FileReader(SYS_CAMERA_IRCUT);
             BufferedReader buffer = new BufferedReader(fread);
             String str = null;
             while ((str = buffer.readLine()) != null) {
                 if (str.equals("1")) {
                    enable = true;
                    break;
                 } else {
                    enable = false;
                 }
             }
             buffer.close();
             fread.close();
        } catch (IOException e) {
          Log.e(TAG, "IO Exception");
        }
        return enable;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {

        if (preference == mIRCutPreference) {
            boolean enable = (Boolean) newValue;
            setIRCut(enable);
        }
        return true;
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.KHADAS_CAMERA_IRCUT;
    }
}
