/*
 * Copyright (C) 2018 The Android Open Source Project
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

package com.android.tv.settings.device.sound;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioSetting;
import android.provider.Settings;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.text.TextUtils;
import android.util.Log;

import com.android.settingslib.core.AbstractPreferenceController;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.HashSet;
import java.util.Map;
import android.media.AudioSystem;

/**
 * Controller for the surround sound switch preferences.
 */
public class SoundFormatPreferenceControllerBitstream extends AbstractPreferenceController {

    private static final String TAG = "SoundFormatPreferenceControllerBitstream";

    private int mFormatId;
    private AudioManager mAudioManager;
    private Map<Integer, Boolean> mReportedFormats;
    private AudioSetting mAudioSetting;

    public SoundFormatPreferenceControllerBitstream(Context context, int formatId) {
        super(context);
        mFormatId = formatId;
        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        mAudioSetting = BitStreamFragment.mAudioSetting;
        mReportedFormats = new LinkedHashMap<Integer, Boolean>();
        mReportedFormats.put(AudioFormat.ENCODING_AC3, mAudioSetting.isEnable(AudioFormat.ENCODING_AC3));
        mReportedFormats.put(AudioFormat.ENCODING_E_AC3, mAudioSetting.isEnable(AudioFormat.ENCODING_E_AC3));
        mReportedFormats.put(AudioFormat.ENCODING_DOLBY_TRUEHD, mAudioSetting.isEnable(AudioFormat.ENCODING_DOLBY_TRUEHD));
        mReportedFormats.put(AudioFormat.ENCODING_E_AC3_JOC, mAudioSetting.isEnable(AudioFormat.ENCODING_E_AC3_JOC));
//        mReportedFormats.put(AudioFormat.ENCODING_AC4, mAudioSetting.isEnable(AudioFormat.ENCODING_AC4));
        mReportedFormats.put(AudioFormat.ENCODING_DTS, mAudioSetting.isEnable(AudioFormat.ENCODING_DTS));
        mReportedFormats.put(AudioFormat.ENCODING_DTS_HD, mAudioSetting.isEnable(AudioFormat.ENCODING_DTS_HD));
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public String getPreferenceKey() {
        return BitStreamFragment.KEY_SURROUND_SOUND_FORMAT_PREFIX + mFormatId;
    }

    @Override
    public void updateState(Preference preference) {
        super.updateState(preference);
        if (preference.getKey().equals(getPreferenceKey())) {
            preference.setEnabled(getFormatPreferencesEnabledState());
            ((SwitchPreference) preference).setChecked(getFormatPreferenceCheckedState());
        }
    }

    @Override
    public boolean handlePreferenceTreeClick(Preference preference) {
        if (preference.getKey().equals(getPreferenceKey())) {
            setSurroundManualFormatsSetting(((SwitchPreference) preference).isChecked());
        }
        return super.handlePreferenceTreeClick(preference);
    }

    /**
     * @return checked state of a surround sound format switch based on passthrough
     *         setting and audio manager state for the format.
     */
    private boolean getFormatPreferenceCheckedState() {
        return mAudioSetting.isEnable(mFormatId);
    }

    /**
     * @return true if the format checkboxes should be enabled, i.e. in manual mode.
     */
    private boolean getFormatPreferencesEnabledState() {
        Log.i(TAG, "getFormatPreferencesEnabledState device = " + mAudioSetting.getDevice() + ", mode = " + mAudioSetting.getMode());
        if (mAudioSetting.getDevice() == AudioSystem.DEVICE_OUT_SPDIF) {
            return true;
        } else if (mAudioSetting.getDevice() == AudioSystem.DEVICE_OUT_HDMI && mAudioSetting.getMode() == AudioSetting.AUDIO_SETTING_MODE_BITSTREAM_MANUAL) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Writes enabled/disabled state for a given format to the global settings.
     */
    private void setSurroundManualFormatsSetting(boolean enabled) {
        mAudioSetting.setFormats(mFormatId, enabled);
    }

}
