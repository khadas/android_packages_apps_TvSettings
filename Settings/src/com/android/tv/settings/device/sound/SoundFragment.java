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
import java.util.List;
import java.util.Map;
import android.util.Log;
import android.os.SystemProperties;

/**
 * The "Sound" screen in TV Settings.
 */
public class SoundFragment extends PreferenceControllerFragment implements Preference.OnPreferenceChangeListener {

    static final String KEY_SOUND_EFFECTS = "sound_effects";
    static final String KEY_SOUND_SAFE = "sound_safe";
    static final String KEY_SURROUND_PASSTHROUGH = "surround_passthrough";
    static final String KEY_SURROUND_SOUND_CATEGORY = "surround_sound_category";
    static final String KEY_SURROUND_SOUND_FORMAT_PREFIX = "surround_sound_format_";
    static final String KEY_SORROUND_PASSTHROUGH_SPDIF = "surround_passthrough_spdif";

    private static final String KEY_AUDIO_DEVICE = "audio_device";
    private static final String VAL_AUDIO_OUTPUT_DEFAUL = "default";
    private static final String VAL_AUDIO_OUTPUT_SPDIF = "spdif";
    private static final String VAL_AUDIO_OUTPUT_HDMI = "hdmi";

    static final String VAL_SURROUND_SOUND_AUTO = "auto";
    static final String VAL_SURROUND_SOUND_NEVER = "never";
    static final String VAL_SURROUND_SOUND_ALWAYS = "always";
    static final String VAL_SURROUND_SOUND_MANUAL = "manual";

    static final String HDMI = "hdmi";
    static final String SPDIF = "spdif";

    private AudioManager mAudioManager;
    private Map<Integer, Boolean> mFormats;
    private List<AbstractPreferenceController> mPreferenceControllers;
    private PreferenceCategory mSurroundSoundCategoryPref;

    private ListPreference audiodevicePref;
    private ListPreference spdifSurroundPref;
    private ListPreference surroundPref;

    public static SoundFragment newInstance() {
        return new SoundFragment();
    }

    @Override
    public void onAttach(Context context) {
        mAudioManager = context.getSystemService(AudioManager.class);
        mFormats = mAudioManager.getSurroundFormats();
        super.onAttach(context);
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.sound;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.sound, null);

        final TwoStatePreference soundPref = (TwoStatePreference) findPreference(KEY_SOUND_EFFECTS);
        soundPref.setChecked(getSoundEffectsEnabled());
        
        final TwoStatePreference soundSafe = (TwoStatePreference) findPreference(KEY_SOUND_SAFE);
        soundSafe.setChecked(getSoundBootSafeEnabled());

        audiodevicePref = (ListPreference) findPreference(KEY_AUDIO_DEVICE);
        spdifSurroundPref = (ListPreference) findPreference(KEY_SORROUND_PASSTHROUGH_SPDIF);
        mSurroundSoundCategoryPref = (PreferenceCategory) findPreference(KEY_SURROUND_SOUND_CATEGORY);
        surroundPref = (ListPreference) findPreference(KEY_SURROUND_PASSTHROUGH);
        int bitStreamDevices = getPassthroughDevice(getContext());
        audiodevicePref.setValue(bitStreamDevices == AudioSystem.DEVICE_OUT_SPDIF ? SPDIF : HDMI);
        audiodevicePref.setOnPreferenceChangeListener(this);

        createSurroundPref();

        createFormatPreferences();
        updateFormatPreferencesStates();

    }

    @Override
    protected List<AbstractPreferenceController> onCreatePreferenceControllers(Context context) {
        mPreferenceControllers = new ArrayList<>(mFormats.size());
        for (Map.Entry<Integer, Boolean> format : mFormats.entrySet()) {
            mPreferenceControllers.add(new SoundFormatPreferenceController(context, format.getKey() /* formatId */));
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
        mFormats = mAudioManager.getSurroundFormats();
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
        default:
            // Fallback in case new formats have been added that we don't know of.
            return AudioFormat.toDisplayName(formatId);
        }
    }

    private void createSurroundPref() {
        boolean removePreference = false;
        if (getPassthroughDevice(getContext()) == AudioSystem.DEVICE_OUT_SPDIF) {
            mSurroundSoundCategoryPref.addPreference(spdifSurroundPref);
            removePreference = mSurroundSoundCategoryPref.removePreference(surroundPref);
            spdifSurroundPref.setValue(getSpdifPassthroughModeSetting(getContext()));
            spdifSurroundPref.setOnPreferenceChangeListener(this);
        } else {
            mSurroundSoundCategoryPref.addPreference(surroundPref);
            removePreference = mSurroundSoundCategoryPref.removePreference(spdifSurroundPref);
            surroundPref.setValue(getSurroundPassthroughSetting(getContext()));
            surroundPref.setOnPreferenceChangeListener(this);
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
        if (TextUtils.equals(preference.getKey(), KEY_SOUND_EFFECTS)) {
            final TwoStatePreference soundPref = (TwoStatePreference) preference;
            setSoundEffectsEnabled(soundPref.isChecked());
        }
        if (TextUtils.equals(preference.getKey(), KEY_SOUND_SAFE)) {
            final TwoStatePreference soundSafe = (TwoStatePreference) preference;
            setSoundBootSafeEnabled(soundSafe.isChecked());
        }
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (TextUtils.equals(preference.getKey(), KEY_SURROUND_PASSTHROUGH)) {
            final String selection = (String) newValue;
            switch (selection) {
            case VAL_SURROUND_SOUND_AUTO:
                setSurroundPassthroughSetting(Settings.Global.ENCODED_SURROUND_OUTPUT_AUTO);
                break;
            case VAL_SURROUND_SOUND_NEVER:
                setSurroundPassthroughSetting(Settings.Global.ENCODED_SURROUND_OUTPUT_NEVER);
                break;
            case VAL_SURROUND_SOUND_ALWAYS:
                setSurroundPassthroughSetting(Settings.Global.ENCODED_SURROUND_OUTPUT_ALWAYS);
                break;
            case VAL_SURROUND_SOUND_MANUAL:
                setSurroundPassthroughSetting(Settings.Global.ENCODED_SURROUND_OUTPUT_MANUAL);
                break;
            default:
                throw new IllegalArgumentException("Unknown surround sound pref value: " + selection);
            }
            updateFormatPreferencesStates();
            return true;
        } else if (TextUtils.equals(preference.getKey(), KEY_SORROUND_PASSTHROUGH_SPDIF)) {
            final String selection = (String) newValue;
            switch (selection) {
            case VAL_SURROUND_SOUND_NEVER:
                setSpdifPassthroughModeSetting(Settings.Global.ENCODED_SURROUND_OUTPUT_NEVER);
                break;
            case VAL_SURROUND_SOUND_ALWAYS:
                setSpdifPassthroughModeSetting(Settings.Global.ENCODED_SURROUND_OUTPUT_ALWAYS);
                break;
            case VAL_SURROUND_SOUND_MANUAL:
                setSpdifPassthroughModeSetting(Settings.Global.ENCODED_SURROUND_OUTPUT_MANUAL);
                break;
            default:
                throw new IllegalArgumentException("Unknown surround sound pref value: " + selection);
            }
            updateFormatPreferencesStates();
            return true;
        } else if (TextUtils.equals(preference.getKey(), KEY_AUDIO_DEVICE)) {
            final String selection = (String) newValue;
            switch (selection) {
            case HDMI:
                setPassthroughDevice(AudioSystem.DEVICE_OUT_AUX_DIGITAL);
                break;
            case SPDIF:
                setPassthroughDevice(AudioSystem.DEVICE_OUT_SPDIF);
                break;
            default:
                throw new IllegalArgumentException("Unknown audio device pref value: " + selection);
            }
            createSurroundPref();
            createFormatPreferences();
            updateFormatPreferencesStates();
            return true;
        }
        return true;
    }

    private boolean getSoundBootSafeEnabled() {
        return "true".equals(SystemProperties.get("persist.sys.audio.enforce_safevolume","true"));
    }
    private void setSoundBootSafeEnabled(boolean enabled) {
        if(enabled)
            SystemProperties.set("persist.sys.audio.enforce_safevolume","true");
        else
            SystemProperties.set("persist.sys.audio.enforce_safevolume","false");
    }

    private boolean getSoundEffectsEnabled() {
        return getSoundEffectsEnabled(getActivity().getContentResolver());
    }

    public static boolean getSoundEffectsEnabled(ContentResolver contentResolver) {
        return Settings.System.getInt(contentResolver, Settings.System.SOUND_EFFECTS_ENABLED, 1) != 0;
    }

    private void setSoundEffectsEnabled(boolean enabled) {
        if (enabled) {
            mAudioManager.loadSoundEffects();
        } else {
            mAudioManager.unloadSoundEffects();
        }
        Settings.System.putInt(getActivity().getContentResolver(), Settings.System.SOUND_EFFECTS_ENABLED,
                enabled ? 1 : 0);
    }

    private void setSurroundPassthroughSetting(int newVal) {
        Settings.Global.putInt(getContext().getContentResolver(), Settings.Global.ENCODED_SURROUND_OUTPUT, newVal);
    }

    private void setSpdifPassthroughModeSetting(int newVal) {
        Settings.Global.putInt(getContext().getContentResolver(), Settings.Global.BITSTREAM_SPDIF_OUTPUT_MODE, newVal);
    }

    static String getSurroundPassthroughSetting(Context context) {
        final int value = Settings.Global.getInt(context.getContentResolver(), Settings.Global.ENCODED_SURROUND_OUTPUT,
                Settings.Global.ENCODED_SURROUND_OUTPUT_AUTO);
        switch (value) {
        case Settings.Global.ENCODED_SURROUND_OUTPUT_AUTO:
        default:
            return VAL_SURROUND_SOUND_AUTO;
        case Settings.Global.ENCODED_SURROUND_OUTPUT_NEVER:
            return VAL_SURROUND_SOUND_NEVER;
        // On Android P ALWAYS is replaced by MANUAL.
        case Settings.Global.ENCODED_SURROUND_OUTPUT_ALWAYS:
        case Settings.Global.ENCODED_SURROUND_OUTPUT_MANUAL:
            return VAL_SURROUND_SOUND_MANUAL;
        }
    }

    static String getSpdifPassthroughModeSetting(Context context) {
        final int value = Settings.Global.getInt(context.getContentResolver(),
                Settings.Global.BITSTREAM_SPDIF_OUTPUT_MODE, Settings.Global.ENCODED_SURROUND_OUTPUT_MANUAL);
        switch (value) {
        case Settings.Global.ENCODED_SURROUND_OUTPUT_NEVER:
            return VAL_SURROUND_SOUND_NEVER;
        // On Android P ALWAYS is replaced by MANUAL.
        case Settings.Global.ENCODED_SURROUND_OUTPUT_ALWAYS:
        case Settings.Global.ENCODED_SURROUND_OUTPUT_MANUAL:
        default:
            return VAL_SURROUND_SOUND_MANUAL;
        }
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.SOUND;
    }

    private void setPassthroughDevice(int device) {
        if ((device == AudioSystem.DEVICE_OUT_SPDIF) || (device == AudioSystem.DEVICE_OUT_AUX_DIGITAL)) {
            Log.d("SoundFragment", "setPassthroughDevice: device = " + device);
            Settings.Global.putInt(getContext().getContentResolver(), Settings.Global.BITSTREAM_OUTPUT_DEVICE, device);
        }
    }

    public static int getPassthroughDevice(Context context) {
        return Settings.Global.getInt(context.getContentResolver(), Settings.Global.BITSTREAM_OUTPUT_DEVICE,
                AudioSystem.DEVICE_OUT_AUX_DIGITAL);
    }
}
