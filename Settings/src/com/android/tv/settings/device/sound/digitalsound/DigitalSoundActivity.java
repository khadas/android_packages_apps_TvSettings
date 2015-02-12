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

    private static OutputModeManager mom;
    private DialogFragment mDialogFragment;
    private IntentFilter mHdmiPluggedFilter;

    private BroadcastReceiver mHdmiPluggedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (isDigitalSoundAuto()) {
                mDialogFragment.setActions(updateActions(OutputModeManager.IS_AUTO));
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mom = new OutputModeManager(this);
        mHdmiPluggedFilter = new IntentFilter(ACTION_HDMI_PLUGGED);

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
        registerReceiver(mHdmiPluggedReceiver, mHdmiPluggedFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mHdmiPluggedReceiver);
    }

    private ArrayList<Action> getActions() {
        int updateMode = OutputModeManager.IS_AUTO;
        String mode = getDigitalSoundMode();
        boolean isAuto = isDigitalSoundAuto();
        if (isAuto) {
            updateMode = OutputModeManager.IS_AUTO;
        }else{
            if (mode.contains(OutputModeManager.PCM)) {
                updateMode = OutputModeManager.IS_PCM;
            }else if (mode.contains(OutputModeManager.HDMI)) {
                updateMode = OutputModeManager.IS_HDMI;
            }else if (mode.contains(OutputModeManager.SPDIF)) {
                updateMode = OutputModeManager.IS_SPDIF;
            }
        }
        return updateActions(updateMode);
    }

    @Override
    public void onActionClicked(Action action) {
        int updateMode = OutputModeManager.IS_AUTO;
        if (ACTION_SOUND_AUTO.equals(action.getKey())) {
            autoSwitchDigitalSound();
            updateMode = OutputModeManager.IS_AUTO;
        } else if (ACTION_SOUND_PCM.equals(action.getKey())) {
            setDigitalSoundMode(OutputModeManager.IS_PCM);
            updateMode = OutputModeManager.IS_PCM;
        } else if (ACTION_SOUND_HDMI.equals(action.getKey())) {
            setDigitalSoundMode(OutputModeManager.IS_HDMI);
            updateMode = OutputModeManager.IS_HDMI;
        } else if (ACTION_SOUND_SPDIF.equals(action.getKey())) {
            setDigitalSoundMode(OutputModeManager.IS_SPDIF);
            updateMode = OutputModeManager.IS_SPDIF;
        }
        mDialogFragment.setActions(updateActions(updateMode));
    }

    private ArrayList<Action> updateActions(int mode) {
        String AutoMode = getString(R.string.device_sound_digital_auto_off);
        if (mode == OutputModeManager.IS_AUTO) {
            AutoMode = getDigitalSoundMode();
        }
        ArrayList<Action> actions = new ArrayList<Action>();
        switch (mode) {
            case OutputModeManager.IS_AUTO:
                actions.add(new Action.Builder()
                    .key(ACTION_SOUND_AUTO)
                    .title(getString(R.string.device_sound_digital_auto))
                    .description(AutoMode)
                    .checked(true)
                    .checkSetId(1)
                    .build());
                actions.add(new Action.Builder()
                    .key(ACTION_SOUND_PCM)
                    .title(getString(R.string.device_sound_digital_pcm))
                    .build());
                actions.add(new Action.Builder()
                    .key(ACTION_SOUND_HDMI)
                    .title(getString(R.string.device_sound_digital_hdmi))
                    .build());
                actions.add(new Action.Builder()
                    .key(ACTION_SOUND_SPDIF)
                    .title(getString(R.string.device_sound_digital_spdif))
                    .build());
                break;
            case OutputModeManager.IS_PCM:
                actions.add(new Action.Builder()
                    .key(ACTION_SOUND_AUTO)
                    .title(getString(R.string.device_sound_digital_auto))
                    .description(AutoMode)
                    .build());
                actions.add(new Action.Builder()
                    .key(ACTION_SOUND_PCM)
                    .title(getString(R.string.device_sound_digital_pcm))
                    .checked(true)
                    .checkSetId(1)
                    .build());
                actions.add(new Action.Builder()
                    .key(ACTION_SOUND_HDMI)
                    .title(getString(R.string.device_sound_digital_hdmi))
                    .build());
                actions.add(new Action.Builder()
                    .key(ACTION_SOUND_SPDIF)
                    .title(getString(R.string.device_sound_digital_spdif))
                    .build());
                break;
            case OutputModeManager.IS_HDMI:
                actions.add(new Action.Builder()
                    .key(ACTION_SOUND_AUTO)
                    .title(getString(R.string.device_sound_digital_auto))
                    .description(AutoMode)
                    .build());
                actions.add(new Action.Builder()
                    .key(ACTION_SOUND_PCM)
                    .title(getString(R.string.device_sound_digital_pcm))
                    .build());
                actions.add(new Action.Builder()
                    .key(ACTION_SOUND_HDMI)
                    .title(getString(R.string.device_sound_digital_hdmi))
                    .checked(true)
                    .checkSetId(1)
                    .build());
                actions.add(new Action.Builder()
                    .key(ACTION_SOUND_SPDIF)
                    .title(getString(R.string.device_sound_digital_spdif))
                    .build());
                break;
            case OutputModeManager.IS_SPDIF:
                actions.add(new Action.Builder()
                    .key(ACTION_SOUND_AUTO)
                    .title(getString(R.string.device_sound_digital_auto))
                    .description(AutoMode)
                    .build());
                actions.add(new Action.Builder()
                    .key(ACTION_SOUND_PCM)
                    .title(getString(R.string.device_sound_digital_pcm))
                    .build());
                actions.add(new Action.Builder()
                    .key(ACTION_SOUND_HDMI)
                    .title(getString(R.string.device_sound_digital_hdmi))
                    .build());
                actions.add(new Action.Builder()
                    .key(ACTION_SOUND_SPDIF)
                    .title(getString(R.string.device_sound_digital_spdif))
                    .checked(true)
                    .checkSetId(1)
                    .build());
                break;
        }
        return actions;
    }

    public boolean isDigitalSoundAuto(){
        return (getDigitalVoiceMode() & OutputModeManager.IS_AUTO) == OutputModeManager.IS_AUTO;
    }

    public String getDigitalSoundMode(){
        String mode = OutputModeManager.PCM;
        switch (getDigitalVoiceMode() & 0x0f) {
            case OutputModeManager.IS_PCM:
                mode = OutputModeManager.PCM;
                break;
            case OutputModeManager.IS_HDMI:
                mode = OutputModeManager.HDMI;
                break;
            case OutputModeManager.IS_SPDIF:
                mode = OutputModeManager.SPDIF;
                break;
        }
        return mode;
    }

    private int getDigitalVoiceMode(){
        return mom.getDigitalVoiceMode();
    }

    private int autoSwitchDigitalSound(){
        return mom.autoSwitchHdmiPassthough();
    }

    private void setDigitalSoundMode(int mode){
        String value = OutputModeManager.PCM;
        switch (mode) {
            case OutputModeManager.IS_PCM:
                value = OutputModeManager.PCM;
                break;
            case OutputModeManager.IS_HDMI:
                value = OutputModeManager.HDMI_RAW;
                break;
            case OutputModeManager.IS_SPDIF:
                value = OutputModeManager.SPDIF_RAW;
                break;
        }
        mom.setDigitalVoiceValue(value);
    }
}
