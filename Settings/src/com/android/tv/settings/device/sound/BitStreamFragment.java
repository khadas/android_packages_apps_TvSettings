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

package com.android.tv.settings.device.sound;

import android.content.ContentResolver;
import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioSetting;
import android.media.AudioSystem;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceViewHolder;
import android.support.v7.preference.TwoStatePreference;
import android.text.TextUtils;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.tv.settings.PreferenceControllerFragment;
import com.android.tv.settings.R;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import android.util.Log;
import android.os.SystemProperties;

/**
 * The "BitStreamFragment" screen in Sound Settings.
 */
public class BitStreamFragment extends PreferenceControllerFragment implements Preference.OnPreferenceChangeListener {
    private static final String TAG = "BitStreamFragment";

    static final String KEY_SURROUND_PASSTHROUGH = "bit_stream_surround_passthrough";
    static final String KEY_SURROUND_SOUND_CATEGORY = "bit_stream_surround_sound_category";
    static final String KEY_SURROUND_SOUND_FORMAT_PREFIX = "surround_sound_format_";
    static final String KEY_AUDIO_DEVICE = "audio_device";

    static final String VAL_SURROUND_SOUND_AUTO = "Auto";
    static final String VAL_SURROUND_SOUND_NEVER = "never";
    static final String VAL_SURROUND_SOUND_ALWAYS = "always";
    static final String VAL_SURROUND_SOUND_MANUAL = "Manual";

    private static final String DECODE = "decode";
    private static final String HDMI = "hdmi";
    private static final String SPDIF = "spdif";

    static public AudioSetting mAudioSetting;
    private Map<Integer, Boolean> mFormats;
    private List<AbstractPreferenceController> mPreferenceControllers;
    private PreferenceCategory mSurroundSoundCategoryPref;
    private ListPreference audioDevicePref;
    private ListPreference audioDeviceHdmiPref;
    private ListPreference surroundPref;

    public static BitStreamFragment newInstance() {
        return new BitStreamFragment();
    }

    @Override
    public void onAttach(Context context) {
        mAudioSetting = new AudioSetting(context);
        mAudioSetting.readSetting();
        mFormats = new LinkedHashMap<Integer, Boolean>();
        getFormats();
        super.onAttach(context);
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.bitstream;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.bitstream, null);

        audioDevicePref = (ListPreference)findPreference(KEY_AUDIO_DEVICE);

        audioDevicePref.setOnPreferenceChangeListener(this);

        surroundPref = (ListPreference) findPreference(KEY_SURROUND_PASSTHROUGH);
        surroundPref.setOnPreferenceChangeListener(this);

        mSurroundSoundCategoryPref =
                (PreferenceCategory) findPreference(KEY_SURROUND_SOUND_CATEGORY);
        buildPref(getDevice());

        createFormatPreferences();
        updateFormatPreferencesStates();
    }

    @Override
    protected List<AbstractPreferenceController> onCreatePreferenceControllers(Context context) {
        mPreferenceControllers = new ArrayList<>(mFormats.size());
        for (Map.Entry<Integer, Boolean> format : mFormats.entrySet()) {
            mPreferenceControllers.add(new SoundFormatPreferenceControllerBitstream(context, format.getKey() /* formatId */));
        }
        return mPreferenceControllers;
    }

    /** Creates and adds switches for each surround sound format. */
    private void createFormatPreferences() {
        for (AbstractPreferenceController controller : mPreferenceControllers) {
            Preference preference = mSurroundSoundCategoryPref.findPreference(controller.getPreferenceKey());
            if (preference != null) {
                mSurroundSoundCategoryPref.removePreference(preference);
            }
        }
        getFormats();
        for (Map.Entry<Integer, Boolean> format : mFormats.entrySet()) {
            int formatId = format.getKey();
            boolean enabled = format.getValue();
            SwitchPreference pref = new SwitchPreference(getPreferenceManager().getContext()) {
                @Override
                public void onBindViewHolder(PreferenceViewHolder holder) {
                    super.onBindViewHolder(holder);
                    // Enabling the view will ensure that the preference is focusable even if it
                    // the preference is disabled. This allows the user to scroll down over the
                    // disabled surround sound formats and see them all.
                    holder.itemView.setEnabled(true);
                }
            };
            pref.setTitle(getFormatDisplayName(formatId));
            pref.setKey(KEY_SURROUND_SOUND_FORMAT_PREFIX + formatId);
            pref.setChecked(enabled);
            mSurroundSoundCategoryPref.addPreference(pref);
        }
    }

    /**
     * @return the display name for each surround sound format.
     */
    private String getFormatDisplayName(int formatId) {
        switch (formatId) {
        case AudioFormat.ENCODING_AC3:
            return getContext().getResources().getString(R.string.surround_sound_format_ac3);
        case AudioFormat.ENCODING_E_AC3:
            return getContext().getResources().getString(R.string.surround_sound_format_e_ac3);
        case AudioFormat.ENCODING_DTS:
            return getContext().getResources().getString(R.string.surround_sound_format_dts);
        case AudioFormat.ENCODING_DTS_HD:
            return getContext().getResources().getString(R.string.surround_sound_format_dts_hd);
        case AudioFormat.ENCODING_AC4:
            return "AC4";
        default:
            // Fallback in case new formats have been added that we don't know of.
            return AudioFormat.toDisplayName(formatId);
        }
    }

    private void updateFormatPreferencesStates() {
        for (AbstractPreferenceController controller : mPreferenceControllers) {
            Preference preference = mSurroundSoundCategoryPref.findPreference(controller.getPreferenceKey());
            if (preference != null) {
                controller.updateState(preference);
            }
        }
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (TextUtils.equals(preference.getKey(), KEY_SURROUND_PASSTHROUGH)) {
            final String selection = (String) newValue;
            switch (selection) {
            case VAL_SURROUND_SOUND_AUTO:
                mAudioSetting.setDeviceAndMode(AudioSystem.DEVICE_OUT_HDMI, AudioSetting.AUDIO_SETTING_MODE_BITSTREAM_AUTO);
                break;
            case VAL_SURROUND_SOUND_MANUAL:
                mAudioSetting.setDeviceAndMode(AudioSystem.DEVICE_OUT_HDMI, AudioSetting.AUDIO_SETTING_MODE_BITSTREAM_MANUAL);
                break;
            default:
                throw new IllegalArgumentException("Unknown surround sound pref value: " + selection);
            }
            updateFormatPreferencesStates();
            return true;
        } else if (TextUtils.equals(preference.getKey(), KEY_AUDIO_DEVICE)) {
            final String selection = (String)newValue;
            if (selection.equals(DECODE)) {
                mAudioSetting.setDeviceAndMode(AudioSystem.DEVICE_NONE, AudioSetting.AUDIO_SETTING_MODE_DECODE);
            }
            if (selection.equals(SPDIF)) {
                mAudioSetting.setDeviceAndMode(AudioSystem.DEVICE_OUT_SPDIF, AudioSetting.AUDIO_SETTING_MODE_BITSTREAM_MANUAL);
            }
            if (selection.equals(HDMI)) {
                mAudioSetting.setDeviceAndMode(AudioSystem.DEVICE_OUT_HDMI, AudioSetting.AUDIO_SETTING_MODE_BITSTREAM_AUTO);
            }
            buildPref(selection);
            createFormatPreferences();
            updateFormatPreferencesStates();
        }
        return true;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.SOUND;
    }

    private String getDevice() {
        String device = DECODE;
        switch(mAudioSetting.getDevice()) {
            case AudioSystem.DEVICE_NONE:
                device = DECODE;
                break;
            case AudioSystem.DEVICE_OUT_HDMI:
                device = HDMI;
                break;
            case AudioSystem.DEVICE_OUT_SPDIF:
                device = SPDIF;
                break;
            default:
                device = DECODE;
                break;
        }
        return device;
    }

    private void buildPref(String device) {
        Log.i(TAG, "buildPref device = " + mAudioSetting.getDevice() + ", mode = " + mAudioSetting.getMode());
        audioDevicePref.setValue(device);
        if (!device.equals(HDMI)) {
            mSurroundSoundCategoryPref.removePreference(surroundPref);
        } else {
            getPreferenceScreen().removePreference(mSurroundSoundCategoryPref);
            mSurroundSoundCategoryPref.removePreference(surroundPref);
            getPreferenceScreen().addPreference(mSurroundSoundCategoryPref);
            mSurroundSoundCategoryPref.addPreference(surroundPref);
            if (mAudioSetting.getMode() == AudioSetting.AUDIO_SETTING_MODE_BITSTREAM_AUTO) {
                surroundPref.setValue(VAL_SURROUND_SOUND_AUTO);
            }
            if (mAudioSetting.getMode() == AudioSetting.AUDIO_SETTING_MODE_BITSTREAM_MANUAL) {
                surroundPref.setValue(VAL_SURROUND_SOUND_MANUAL);
            }
            surroundPref.setOnPreferenceChangeListener(this);
        }
    }

    private void getFormats() {
        mFormats.clear();
        mFormats.put(AudioFormat.ENCODING_AC3, mAudioSetting.isEnable(AudioFormat.ENCODING_AC3));
        mFormats.put(AudioFormat.ENCODING_E_AC3, mAudioSetting.isEnable(AudioFormat.ENCODING_E_AC3));
        mFormats.put(AudioFormat.ENCODING_DOLBY_TRUEHD, mAudioSetting.isEnable(AudioFormat.ENCODING_DOLBY_TRUEHD));
        mFormats.put(AudioFormat.ENCODING_E_AC3_JOC, mAudioSetting.isEnable(AudioFormat.ENCODING_E_AC3_JOC));
//        mFormats.put(AudioFormat.ENCODING_AC4, mAudioSetting.isEnable(AudioFormat.ENCODING_AC4));
        mFormats.put(AudioFormat.ENCODING_DTS, mAudioSetting.isEnable(AudioFormat.ENCODING_DTS));
        mFormats.put(AudioFormat.ENCODING_DTS_HD, mAudioSetting.isEnable(AudioFormat.ENCODING_DTS_HD));
    }
}
