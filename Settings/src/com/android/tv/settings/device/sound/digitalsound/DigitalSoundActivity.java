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
import android.content.ContentResolver;
import android.content.Context;
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

    private static final int IS_AUTO  = 0x10;
    private static final int IS_PCM   = 0x01;
    private static final int IS_HDMI  = 0x02;
    private static final int IS_SPDIF = 0x04;

    private static OutputModeManager mom;
    private DialogFragment mDialogFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mom = new OutputModeManager(this);

        mDialogFragment = new DialogFragment.Builder()
                .title(getString(R.string.device_sound_digital))
                .breadcrumb(getString(R.string.header_category_device))
                .iconResourceId(R.drawable.ic_settings_sound_on)
                .iconBackgroundColor(getResources().getColor(R.color.icon_background))
                .actions(getActions()).build();
        DialogFragment.add(getFragmentManager(), mDialogFragment);
    }

    private ArrayList<Action> getActions() {
        int updateMode = IS_AUTO;
        String mode = getDigitalSoundMode();
        boolean isAuto = isDigitalSoundAuto();
        if (isAuto) {
            updateMode = IS_AUTO;
        }else{
            if (mode.contains("Pcm")) {
                updateMode = IS_PCM;
            }else if (mode.contains("Hdmi")) {
                updateMode = IS_HDMI;
            }else if (mode.contains("Spdif")) {
                updateMode = IS_SPDIF;
            }
        }
        return updateActions(updateMode);
    }

    @Override
    public void onActionClicked(Action action) {
        int updateMode = IS_AUTO;
        if (ACTION_SOUND_AUTO.equals(action.getKey())) {
            autoSwitchDigitalSound();
            updateMode = IS_AUTO;
        } else if (ACTION_SOUND_PCM.equals(action.getKey())) {
            setDigitalSoundMode(IS_PCM);
            updateMode = IS_PCM;
        } else if (ACTION_SOUND_HDMI.equals(action.getKey())) {
            setDigitalSoundMode(IS_HDMI);
            updateMode = IS_HDMI;
        } else if (ACTION_SOUND_SPDIF.equals(action.getKey())) {
            setDigitalSoundMode(IS_SPDIF);
            updateMode = IS_SPDIF;
        }
        mDialogFragment.setActions(updateActions(updateMode));
    }

    private ArrayList<Action> updateActions(int mode) {
        String AutoMode = "off";
        if (mode == IS_AUTO) {
            AutoMode = getDigitalSoundMode();
        }
        ArrayList<Action> actions = new ArrayList<Action>();
        switch (mode) {
            case IS_AUTO:
                actions.add(new Action.Builder()
                    .key(ACTION_SOUND_AUTO)
                    .title(getString(R.string.device_sound_digital_auto))
                    .description(AutoMode)
                    .drawableResource(R.drawable.ic_settings_sound_on)
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
            case IS_PCM:
                actions.add(new Action.Builder()
                    .key(ACTION_SOUND_AUTO)
                    .title(getString(R.string.device_sound_digital_auto))
                    .description(AutoMode)
                    .build());
                actions.add(new Action.Builder()
                    .key(ACTION_SOUND_PCM)
                    .title(getString(R.string.device_sound_digital_pcm))
                    .drawableResource(R.drawable.ic_settings_sound_on)
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
            case IS_HDMI:
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
                    .drawableResource(R.drawable.ic_settings_sound_on)
                    .build());
                actions.add(new Action.Builder()
                    .key(ACTION_SOUND_SPDIF)
                    .title(getString(R.string.device_sound_digital_spdif))
                    .build());
                break;
            case IS_SPDIF:
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
                    .drawableResource(R.drawable.ic_settings_sound_on)
                    .build());
                break;
        }
        return actions;
    }

    public boolean isDigitalSoundAuto(){
        return (getDigitalVoiceMode() & IS_AUTO) == IS_AUTO;
    }

    public String getDigitalSoundMode(){
        String mode = "Pcm";
        switch (getDigitalVoiceMode() & 0x0f) {
            case IS_PCM:
                mode = "Pcm";
                break;
            case IS_HDMI:
                mode = "Hdmi";
                break;
            case IS_SPDIF:
                mode = "Spdif";
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
        String value = "PCM";
        switch (mode) {
            case IS_PCM:
                value = "PCM";
                break;
            case IS_HDMI:
                value = "HDMI passthrough";
                break;
            case IS_SPDIF:
                value = "SPDIF passthrough";
                break;
        }
        mom.setDigitalVoiceValue(value);
    }
}
