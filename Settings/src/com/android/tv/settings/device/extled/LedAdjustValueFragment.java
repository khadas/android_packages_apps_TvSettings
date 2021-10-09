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

package com.android.tv.settings.device.extled;


import android.os.Bundle;
import android.os.Handler;
import android.support.v17.preference.LeanbackPreferenceFragment;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.os.SystemProperties;
import android.util.Log;
import android.text.TextUtils;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;
import android.provider.Settings;
import com.android.tv.settings.R;
import android.media.AudioManager;
import android.content.Context;
import android_serialport_api.KhadasLedLogoControl;

public class LedAdjustValueFragment extends LeanbackPreferenceFragment implements SeekBar.OnSeekBarChangeListener {

    private static final String TAG = "LedAdjustValueFragment";

    private SeekBar seekbar_logoled;

    private TextView text_logoled;

    private ImageView image_logoled;


    private boolean isSeekBarInited = false;

    private KhadasLedLogoControl khadasLedLogoControl;

    public static LedAdjustValueFragment newInstance() {
        return new LedAdjustValueFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.xml.ledseekbar, container, false);
        return view;
    }

    @Override
    public void onViewCreated (View view, Bundle savedInstanceState) {
        initSeekBar(view);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

    }

    private void initSeekBar(View view) {
        int status = -1;
        boolean hasfocused = false;
	khadasLedLogoControl = new KhadasLedLogoControl();
        seekbar_logoled = (SeekBar) view.findViewById(R.id.seekbar_logoled);
        text_logoled = (TextView) view.findViewById(R.id.text_logoled);
        image_logoled = (ImageView) view.findViewById(R.id.logoled_icon);
        seekbar_logoled.setOnSeekBarChangeListener(this);
        seekbar_logoled.setMax(31);
        seekbar_logoled.setProgress(1);
        seekbar_logoled.requestFocus();
        khadasLedLogoControl.Ledbrigthnessplus(1);
        hasfocused = true;
        isSeekBarInited = true;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (!isSeekBarInited) {
            return;
        }
        switch (seekBar.getId()) {
            case R.id.seekbar_logoled:{
                Log.d(TAG," progress seekbar_media: " + progress);
                khadasLedLogoControl.Ledbrigthnessplus(progress);
                break;
            }
            default:
                break;
        }
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

   }
}
