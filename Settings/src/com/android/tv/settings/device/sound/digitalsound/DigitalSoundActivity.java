/*
 * Copyright (C) 2014 The Android Open Source Project
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

package com.android.tv.settings.device.sound.digitalsound;

import android.app.Activity;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.IntentFilter;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import com.android.tv.settings.dialog.DialogFragment;
import com.android.tv.settings.dialog.DialogFragment.Action;
import com.android.tv.settings.BrowseInfo;
import com.android.tv.settings.R;
import com.droidlogic.app.OutputModeManager;

import java.util.ArrayList;

/**
 * Activity that allows the enabling and disabling of sound effects.
 */
public class DigitalSoundActivity extends Activity implements Action.Listener {
    private static final String ACTION_SOUND_AUTO = "sound_auto";
    private static final String ACTION_SOUND_PCM = "sound_pcm";
    private static final String ACTION_SOUND_HDMI = "sound_hdmi";
    private static final String ACTION_SOUND_SPDIF = "sound_spdif";
    private static final String ACTION_HDMI_PLUGGED = "android.intent.action.HDMI_PLUGGED";
    private static final String PREFERENCE = "preference_box_settings";
    private static final String KEY_AUTO = "auto";

    private static OutputModeManager mom;
    private DialogFragment mDialogFragment;
    private IntentFilter mHdmiPluggedFilter;
    private SharedPreferences mPreference;

    private BroadcastReceiver mHdmiPluggedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (isDigitalSoundAuto()) {
                autoSwitchDigitalSound();
            } else {
                setDigitalSoundMode(getDigitalVoiceMode());
            }
            mDialogFragment.setActions(getActions());
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mom = new OutputModeManager(this);
        mHdmiPluggedFilter = new IntentFilter(ACTION_HDMI_PLUGGED);
        mPreference = this.getSharedPreferences(PREFERENCE, Context.MODE_PRIVATE);

        mDialogFragment = new DialogFragment.Builder()
                .title(getString(R.string.device_sound_digital))
                .breadcrumb(getString(R.string.header_category_device))
                .iconResourceId(R.drawable.ic_settings_sound_on)
                .iconBackgroundColor(getResources().getColor(R.color.icon_background))
                .actions(getActions()).build();
        DialogFragment.add(getFragmentManager(), mDialogFragment);
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.registerReceiver(mHdmiPluggedReceiver, mHdmiPluggedFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.unregisterReceiver(mHdmiPluggedReceiver);
    }

    private ArrayList<Action> getActions() {
        return updateActions();
    }

    @Override
    public void onActionClicked(Action action) {
        if (ACTION_SOUND_AUTO.equals(action.getKey())) {
            autoSwitchDigitalSound();
        } else if (ACTION_SOUND_PCM.equals(action.getKey())) {
            setDigitalSoundMode(OutputModeManager.PCM);
        } else if (ACTION_SOUND_HDMI.equals(action.getKey())) {
            setDigitalSoundMode(OutputModeManager.HDMI_RAW);
        } else if (ACTION_SOUND_SPDIF.equals(action.getKey())) {
            setDigitalSoundMode(OutputModeManager.SPDIF_RAW);
        }
        mDialogFragment.setActions(getActions());
    }

    private ArrayList<Action> updateActions() {
        boolean isAuto = isDigitalSoundAuto();
        String mode = getDigitalVoiceMode();
        String AutoMode = isAuto ? mode : getString(R.string.device_sound_digital_auto_off);

        ArrayList<Action> actions = new ArrayList<Action>();
        actions.add(new Action.Builder()
            .key(ACTION_SOUND_AUTO)
            .title(getString(R.string.device_sound_digital_auto))
            .description(AutoMode)
            .checked(isAuto)
            .build());
        actions.add(new Action.Builder()
            .key(ACTION_SOUND_PCM)
            .title(getString(R.string.device_sound_digital_pcm))
            .checked(!isAuto && mode.equals(OutputModeManager.PCM))
            .build());
        actions.add(new Action.Builder()
            .key(ACTION_SOUND_HDMI)
            .title(getString(R.string.device_sound_digital_hdmi))
            .checked(!isAuto && mode.equals(OutputModeManager.HDMI_RAW))
            .build());
        if (getResources().getBoolean(R.bool.platform_support_spdif)) {
            actions.add(new Action.Builder()
                .key(ACTION_SOUND_SPDIF)
                .title(getString(R.string.device_sound_digital_spdif))
                .checked(!isAuto && mode.equals(OutputModeManager.SPDIF_RAW))
                .build());
        }
        return actions;
    }

    private void savePreference(String key, boolean value) {
        Editor editor = mPreference.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    public boolean isDigitalSoundAuto(){
        return mPreference.getBoolean(KEY_AUTO, false);
    }

    private String getDigitalVoiceMode(){
        return mom.getDigitalVoiceMode();
    }

    private int autoSwitchDigitalSound(){
        savePreference(KEY_AUTO, true);
        return mom.autoSwitchHdmiPassthough();
    }

    private void setDigitalSoundMode(String mode){
        savePreference(KEY_AUTO, false);
        mom.setDigitalMode(mode);
    }
}
