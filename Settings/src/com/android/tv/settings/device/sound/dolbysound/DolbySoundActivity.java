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

package com.android.tv.settings.device.sound.dolbysound;

import com.android.tv.settings.ActionKey;
import com.android.tv.settings.ActionBehavior;
import com.android.tv.settings.BaseSettingsActivity;
import com.android.tv.settings.dialog.old.ActionAdapter;
import com.android.tv.settings.dialog.old.Action;
import com.android.tv.settings.R;

import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.SharedPreferences.Editor;
import android.os.SystemProperties;
import android.os.Bundle;
import android.util.Log;

import com.droidlogic.app.OutputModeManager;
import com.droidlogic.app.SystemControlManager;


public class DolbySoundActivity extends BaseSettingsActivity implements ActionAdapter.Listener {
    public static final String TAG = "DRCSound";
    public static final String LINE = "2";
    public static final String RF = "3";
    //For sharedPreferences
    public static final String PREFERENCE_BOX_SETTING = "preference_box_settings";
    public static final String DRC_MODE = "dolbydrc";
    public static final String DRC_OFF = "off";
    public static final String DRC_LINE = "line";
    public static final String DRC_RF = "rf";

    private SharedPreferences mSharepreference = null;
    private OutputModeManager mOMM;

    enum ActionType {
        DOLBY_SOUND,
        DOLBY_DRC_MODE,
        DOLBY_DRC_OFF,
        DOLBY_DRC_LINE,
        DOLBY_DRC_RF;

        Action toAction(Resources resources, String title, String description) {
            return new Action.Builder()
                    .key(getKey(this, ActionBehavior.INIT))
                    .title(title)
                    .description(description)
                    .enabled(true)
                    .checked(false)
                    .build();
        }

        private String getKey(ActionType t, ActionBehavior b) {
            return new ActionKey<ActionType, ActionBehavior>(t, b).getKey();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        mOMM = new OutputModeManager(this);
        mSharepreference = getSharedPreferences(PREFERENCE_BOX_SETTING, Context.MODE_PRIVATE);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActionClicked(Action action) {
        /*
         * For list preferences
         */
        String data = "";
        ActionKey<ActionType, ActionBehavior> actionKey = new ActionKey<ActionType, ActionBehavior>(ActionType.class, ActionBehavior.class, action.getKey());
        final ActionType type = actionKey.getType();
        switch ((ActionType) mState) {
            case DOLBY_DRC_MODE:
                if (type == ActionType.DOLBY_DRC_OFF) {
                    mOMM.enableDobly_DRC(false);
                    mOMM.setDoblyMode(LINE);
                    savePreference(DRC_MODE, DRC_OFF);
                } else if (type == ActionType.DOLBY_DRC_LINE) {
                    mOMM.enableDobly_DRC(true);
                    mOMM.setDoblyMode(LINE);
                    savePreference(DRC_MODE, DRC_LINE);
                } else if (type == ActionType.DOLBY_DRC_RF) {
                    mOMM.setDoblyMode(RF);
                    savePreference(DRC_MODE, DRC_RF);
                }
                goBack();
                return;
            default:
                break;
        }

        final ActionBehavior behavior = actionKey.getBehavior();
        if (behavior == null) {
            return;
        }
        switch (behavior) {
            case INIT:
                setState(type, true);
                break;
            default:
        }
    }

    @Override
    protected Object getInitialState() {
        return ActionType.DOLBY_SOUND;
    }

    @Override
    protected void refreshActionList() {
        int summary;
        mActions.clear();
        switch ((ActionType) mState) {
            case DOLBY_SOUND:
                String value = "";
                value = mSharepreference.getString(DRC_MODE, DRC_LINE);
                if (value.equals(DRC_OFF)) {
                    summary = R.string.device_sound_dolby_drcoff;
                } else if (value.equals(DRC_RF)) {
                    summary = R.string.device_sound_dolby_drcrf;
                } else {
                    summary = R.string.device_sound_dolby_drcline;
                }
                mActions.add(ActionType.DOLBY_DRC_MODE.toAction(mResources, getString(R.string.device_sound_dolby_drcmode), getString(summary)));
                break;
            case DOLBY_DRC_MODE:
                mActions.add(ActionType.DOLBY_DRC_OFF.toAction(mResources, getString(R.string.device_sound_dolby_drcoff), null));
                mActions.add(ActionType.DOLBY_DRC_LINE.toAction(mResources, getString(R.string.device_sound_dolby_drcline), null));
                mActions.add(ActionType.DOLBY_DRC_RF.toAction(mResources, getString(R.string.device_sound_dolby_drcrf), null));
                break;
            default:
                break;
        }
    }

    @Override
    protected void updateView() {
        refreshActionList();
        switch ((ActionType) mState) {
            case DOLBY_SOUND:
                setView(R.string.device_sound_dolby, R.string.header_category_device, 0, R.drawable.ic_settings_sound_on);
                break;
            case DOLBY_DRC_MODE:
                setView(R.string.device_sound_dolby_drcmode, R.string.device_sound_dts, 0, R.drawable.ic_settings_sound_on);
                break;
            default:
                break;
        }
    }

    @Override
    protected void setProperty(boolean enable) {
    }

    private void savePreference(String key, String value) {
        Editor editor = getSharedPreferences(PREFERENCE_BOX_SETTING, Context.MODE_PRIVATE).edit();
        editor.putString(key, value);
        editor.commit();
    }
}

