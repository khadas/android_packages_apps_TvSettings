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

package com.android.tv.settings.system;

import com.android.tv.settings.ActionBehavior;
import com.android.tv.settings.ActionKey;
import com.android.tv.settings.BaseSettingsActivity;
import com.android.tv.settings.R;
import com.android.tv.settings.dialog.old.Action;
import com.android.tv.settings.dialog.old.ActionAdapter;

import android.os.Bundle;
import android.text.TextUtils;
import android.provider.Settings.Global;
import android.util.Log;

import com.droidlogic.app.HdmiCecManager;

public class HdmiCecActivity extends BaseSettingsActivity implements ActionAdapter.Listener {
    private static final String TAG = "HdmiCecOutput";
    private static final int DISABLED = 0;
    private static final int ENABLED = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private void switchCec(boolean on) {
        writeCecOption(Global.HDMI_CONTROL_ENABLED, on);
    }

    private void switchOneTouchPlay(boolean on) {
        writeCecOption(HdmiCecManager.HDMI_CONTROL_ONE_TOUCH_PLAY_ENABLED, on);
    }

    private void switchOneTouchPowerOff(boolean on) {
        writeCecOption(Global.HDMI_CONTROL_AUTO_DEVICE_OFF_ENABLED, on);
    }

    private void switchAutoChangeLanguage(boolean on) {
        writeCecOption(HdmiCecManager.HDMI_CONTROL_AUTO_CHANGE_LANGUAGE_ENABLED, on);
    }

    @Override
    public void onActionClicked(Action action) {
        /*
         * For list preferences
         */
        ActionKey<ActionType, ActionBehavior> actionKey = new ActionKey<ActionType, ActionBehavior>(ActionType.class, ActionBehavior.class, action.getKey());
        final ActionType type = actionKey.getType();
        switch ((ActionType) mState) {
            case CEC_OVERVIEW_SWITCH:
                switchCec(type == ActionType.CEC_OVERVIEW_SWITCH_ON);
                goBack();
                return;
            case CEC_OVERVIEW_ONE_KEY_PLAY:
                switchOneTouchPlay(type == ActionType.CEC_OVERVIEW_SWITCH_ON);
                goBack();
                return;
            case CEC_OVERVIEW_ONE_KEY_POWER_OFF:
                switchOneTouchPowerOff(type == ActionType.CEC_OVERVIEW_SWITCH_ON);
                goBack();
                return;
            case CEC_OVERVIEW_AUTO_CHANGE_LANGUAGE:
                switchAutoChangeLanguage(type == ActionType.CEC_OVERVIEW_SWITCH_ON);
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
        return ActionType.CEC_OVERVIEW_CONTROL;
    }

    @Override
    protected void refreshActionList() {
        mActions.clear();
        switch ((ActionType) mState) {
            case CEC_OVERVIEW_CONTROL:
                String isOn = getStatus(isSwitchCecOn());
                mActions.add(ActionType.CEC_OVERVIEW_SWITCH.toAction(mResources, TextUtils.isEmpty(isOn) ? "" : isOn));
                if (getString(R.string.settings_on).equals(isOn)) {
                    isOn = getStatus(isOneTouchPlayOn());
                    mActions.add(ActionType.CEC_OVERVIEW_ONE_KEY_PLAY.toAction(mResources, TextUtils.isEmpty(isOn) ? "" : isOn));
                    isOn = getStatus(isOneTouchPowerOff());
                    mActions.add(ActionType.CEC_OVERVIEW_ONE_KEY_POWER_OFF.toAction(mResources, TextUtils.isEmpty(isOn) ? "" : isOn));
                    isOn = getStatus(isAutoChangeLanguage());
                    mActions.add(ActionType.CEC_OVERVIEW_AUTO_CHANGE_LANGUAGE.toAction(mResources, TextUtils.isEmpty(isOn) ? "" : isOn));
                }
                break;
            case CEC_OVERVIEW_SWITCH:
                Action switch_on = ActionType.CEC_OVERVIEW_SWITCH_ON.toAction(mResources);
                Action switch_off = ActionType.CEC_OVERVIEW_SWITCH_OFF.toAction(mResources);
                if (isSwitchCecOn()) {
                    switch_on.setChecked(true);
                    switch_off.setChecked(false);
                } else {
                    switch_on.setChecked(false);
                    switch_off.setChecked(true);
                }
                mActions.add(switch_on);
                mActions.add(switch_off);
                break;
            case CEC_OVERVIEW_ONE_KEY_PLAY:
                Action one_key_play_on = ActionType.CEC_OVERVIEW_SWITCH_ON.toAction(mResources);
                Action one_key_play_off = ActionType.CEC_OVERVIEW_SWITCH_OFF.toAction(mResources);
                if (isOneTouchPlayOn()) {
                    one_key_play_on.setChecked(true);
                    one_key_play_off.setChecked(false);
                } else {
                    one_key_play_on.setChecked(false);
                    one_key_play_off.setChecked(true);
                }
                mActions.add(one_key_play_on);
                mActions.add(one_key_play_off);
                break;
            case CEC_OVERVIEW_ONE_KEY_POWER_OFF:
                Action one_key_power_off_on = ActionType.CEC_OVERVIEW_SWITCH_ON.toAction(mResources);
                Action one_key_power_off_off = ActionType.CEC_OVERVIEW_SWITCH_OFF.toAction(mResources);
                if (isOneTouchPowerOff()) {
                    one_key_power_off_on.setChecked(true);
                    one_key_power_off_off.setChecked(false);
                } else {
                    one_key_power_off_on.setChecked(false);
                    one_key_power_off_off.setChecked(true);
                }
                mActions.add(one_key_power_off_on);
                mActions.add(one_key_power_off_off);
                break;
            case CEC_OVERVIEW_AUTO_CHANGE_LANGUAGE:
                Action auto_change_language_on = ActionType.CEC_OVERVIEW_SWITCH_ON.toAction(mResources);
                Action auto_change_language_off = ActionType.CEC_OVERVIEW_SWITCH_OFF.toAction(mResources);
                if (isAutoChangeLanguage()) {
                    auto_change_language_on.setChecked(true);
                    auto_change_language_off.setChecked(false);
                } else {
                    auto_change_language_on.setChecked(false);
                    auto_change_language_off.setChecked(true);
                }
                mActions.add(auto_change_language_on);
                mActions.add(auto_change_language_off);
                break;
            default:
                break;
        }
    }

    @Override
    protected void updateView() {
        refreshActionList();
        switch ((ActionType) mState) {
            case CEC_OVERVIEW_CONTROL:
                setView(R.string.cec_control, R.string.cec_control, 0, R.drawable.ic_settings_outputs);
                break;
            case CEC_OVERVIEW_SWITCH:
                setView(R.string.cec_switch, R.string.cec_control, 0, R.drawable.ic_settings_outputs);
                break;
            case CEC_OVERVIEW_ONE_KEY_PLAY:
                setView(R.string.cec_one_key_play, R.string.cec_control, 0, R.drawable.ic_settings_outputs);
                break;
            case CEC_OVERVIEW_ONE_KEY_POWER_OFF:
                setView(R.string.cec_one_key_power_off, R.string.cec_control, 0, R.drawable.ic_settings_outputs);
                break;
            case CEC_OVERVIEW_AUTO_CHANGE_LANGUAGE:
                setView(R.string.cec_auto_change_language, R.string.cec_control, 0, R.drawable.ic_settings_outputs);
                break;
            default:
                break;
        }
    }

    @Override
    protected void setProperty(boolean enable) {
    }

    private boolean isSwitchCecOn() {
        return readCecOption(Global.HDMI_CONTROL_ENABLED);
    }

    private boolean isOneTouchPlayOn() {
        return readCecOption(HdmiCecManager.HDMI_CONTROL_ONE_TOUCH_PLAY_ENABLED);
    }

    private boolean isOneTouchPowerOff() {
        return readCecOption(Global.HDMI_CONTROL_AUTO_DEVICE_OFF_ENABLED);
    }

    private boolean isAutoChangeLanguage() {
        return readCecOption(HdmiCecManager.HDMI_CONTROL_AUTO_CHANGE_LANGUAGE_ENABLED);
    }

    private void writeCecOption(String key, boolean value) {
        Global.putInt(getContentResolver(), key, toInt(value));
    }

    private boolean readCecOption(String key) {
        return Global.getInt(getContentResolver(), key, toInt(true)) == ENABLED;
    }

    private int toInt(boolean enabled) {
        return enabled ? ENABLED : DISABLED;
    }

    private String getStatus(boolean status) {
        if (status) {
            return getString(R.string.settings_on);
        } else {
            return getString(R.string.settings_off);
        }
    }
}

