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

package com.android.tv.settings.device.statusbar;

import android.provider.Settings;
import android.app.ActivityManager;
import android.app.StatusBarManager;
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

public class StatusBarFragment extends SettingsPreferenceFragment
         implements Preference.OnPreferenceChangeListener {
    private static final String TAG = StatusBarFragment.class.getSimpleName();
    private static final String KEY_STATUSBAR_BOTTOM = "bottom_statusbar_btn";
    private static final String KEY_STATUSBAR_UPPER = "upper_statusbar_btn";
    private static final boolean DEBUG = false;

    private Context mContext;
    private StatusBarManager mStatusBarManager;
    private TwoStatePreference mStatusBarBottomPreference;
    private TwoStatePreference mStatusBarUpperPreference;

    public static StatusBarFragment newInstance() {
        return new StatusBarFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.statusbar, null);
	mStatusBarManager = (StatusBarManager) mContext.getSystemService(Context.STATUS_BAR_SERVICE);
        mStatusBarBottomPreference = (TwoStatePreference) findPreference(KEY_STATUSBAR_BOTTOM);
        mStatusBarUpperPreference = (TwoStatePreference) findPreference(KEY_STATUSBAR_UPPER);
        mStatusBarBottomPreference.setOnPreferenceChangeListener(this);
        mStatusBarUpperPreference.setOnPreferenceChangeListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateState();
    }

    private void updateState() {
        if (mStatusBarBottomPreference !=null) {
           int value = Settings.Global.getInt(mContext.getContentResolver(), Settings.Global.STATUS_BAR_BOTTOM, 0);
           mStatusBarBottomPreference.setChecked(value == 1);
        }

        if (mStatusBarUpperPreference !=null) {
           int value = Settings.Global.getInt(mContext.getContentResolver(), Settings.Global.STATUS_BAR_UPPER, 0);
           mStatusBarUpperPreference.setChecked(value == 1);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mStatusBarBottomPreference) {
            boolean value = (Boolean) newValue;
            if (value)
               mStatusBarManager.addBottomBar();
            else
               mStatusBarManager.removeBottomBar();
        }

        if (preference == mStatusBarUpperPreference) {
            boolean value = (Boolean) newValue;
            if (value)
               mStatusBarManager.addUpperBar();
            else
               mStatusBarManager.removeUpperBar();
        }
        return true;
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.KHADAS_STATUS_BAR;
    }
}
