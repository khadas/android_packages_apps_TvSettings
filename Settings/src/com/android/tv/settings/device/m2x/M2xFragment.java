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

package com.android.tv.settings.device.m2x;

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
import com.android.tv.settings.boardInfo.BoardInfo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import android.util.Log;

public class M2xFragment extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {
    private static final String TAG = "M2xSettings";
    static final String KEY_M2X = "m2x_btn";
    private static final String SYS_ETHERNET_MODE ="/sys/class/mcu/ethernet_mode";
    private Context mContext;
    private BoardInfo mBoardInfo;

    public static M2xFragment newInstance() {
        return new M2xFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.m2x_prefs, null);
        
        mBoardInfo = new BoardInfo();
        final TwoStatePreference m2xPref = (TwoStatePreference) findPreference(KEY_M2X);
        m2xPref.setOnPreferenceChangeListener(this);
        if (mBoardInfo.isM2xNetSupport()) {
            int mode = getEthernetMode();
            m2xPref.setChecked(mode==0 ? false:true);
        } else {
            m2xPref.setVisible(false);
            getPreferenceScreen().removePreference(m2xPref);
        }

    }

    private void setEthernetMode(int mode) {
         try {
             BufferedWriter bufWriter = null;
             bufWriter = new BufferedWriter(new FileWriter(SYS_ETHERNET_MODE));
             bufWriter.write(String.valueOf(mode));
             bufWriter.close();
         } catch (IOException e) {
             e.printStackTrace();
         }
    }

    private int getEthernetMode() {
          int mode = 0;
          try {
             FileReader fread = new FileReader(SYS_ETHERNET_MODE);
             BufferedReader buffer = new BufferedReader(fread);
             String str = null;
             while ((str = buffer.readLine()) != null) {
                  if (str.equals("1"))
                     mode = 1;
                  else
                     mode = 0;
             }
             buffer.close();
             fread.close();
          } catch (IOException e) {
             Log.e(TAG, "IO Exception");
          }
          return mode;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (TextUtils.equals(preference.getKey(), KEY_M2X)) {
            boolean enabled = (boolean) newValue;
            if(enabled) {      
               setEthernetMode(1);
            }else {
               setEthernetMode(0);
             }
        }
        return true;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.KHADAS_M2X;
    }


}
