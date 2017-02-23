/*
 * Copyright (C) 2015 The Android Open Source Project
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
 * limitations under the License
 */

package com.android.tv.settings.device.display;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.storage.DiskInfo;
import android.os.storage.VolumeInfo;
import android.os.storage.VolumeRecord;
import android.os.SystemProperties;
import android.support.v17.preference.LeanbackPreferenceFragment;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.util.ArraySet;
import android.util.Log;

import com.android.tv.settings.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class DisplayFragment extends LeanbackPreferenceFragment {

    private static final String TAG = "DisplayFragment";
    private static final String KEY_SCREENRESOLUTION_PASSTHROUGH = "outputmode";
    private static final String KEY_HDR_PASSTHROUGH = "hdr";
    private static final String KEY_SDR_PASSTHROUGH = "sdr";

    public static DisplayFragment newInstance() {
        return new DisplayFragment();
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.display, null);
        final Preference screenresolutionPref =
                (Preference) findPreference(KEY_SCREENRESOLUTION_PASSTHROUGH);
        final Preference hdrPref =
                (Preference) findPreference(KEY_HDR_PASSTHROUGH);
        final Preference sdrPref =
                (Preference) findPreference(KEY_SDR_PASSTHROUGH);
        if (SystemProperties.getBoolean("ro.platform.has.tvuimode", false)) {
            screenresolutionPref.setVisible(false);
            hdrPref.setVisible(false);
            sdrPref.setVisible(false);
            Log.d(TAG,"tv don't need screen resolution hdr&sdr&sdr control switch!");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }
}
