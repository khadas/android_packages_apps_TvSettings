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

import android.app.ActivityManager.RunningServiceInfo;
import android.app.ActivityManager;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.droidlogic.app.HdmiCecManager;


public class HdmiCecActivity extends BaseSettingsActivity implements ActionAdapter.Listener {
    private static final String TAG = "HdmiCecOutput";

    //For sharedPreferences
    private static final String PREFERENCE_BOX_SETTING = "preference_box_settings";
    private static final String SWITCH_ON = "true";
    private static final String SWITCH_OFF = "false";
    private static final String SWITCH_CEC = "switch_cec";
    private static final String SWITCH_ONE_KEY_PLAY = "switch_one_key_play";
    private static final String SWITCH_ONE_KEY_POWER_OFF = "switch_one_key_power_off";
    private static final String SWITCH_AUTO_CHANGE_LANGUAGE = "switch_auto_change_languace";

    //For start service
    private static final String CEC_SERVICE = "com.android.tv.settings.system.CecService";
    private static final String CEC_ACTION = "CEC_LANGUAGE_AUTO_SWITCH";

    private SharedPreferences mSharepreference = null;
    private HdmiCecManager mHdmiCecManager;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        initCecFun();
        super.onCreate(savedInstanceState);
    }

    private boolean isCecServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (CEC_SERVICE.equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private void switchCec(boolean on) {
        String isOpen = mSharepreference.getString(SWITCH_CEC, SWITCH_OFF);
        Log.d(TAG, "switch CEC, on:" + on + ", isOpen:" + isOpen);
        Editor editor = this.getSharedPreferences(PREFERENCE_BOX_SETTING, Context.MODE_PRIVATE).edit();
        if (isOpen.equals(SWITCH_ON) && !on) {
            editor.putString(SWITCH_CEC, SWITCH_OFF);
            editor.putString(SWITCH_ONE_KEY_PLAY, SWITCH_OFF);
            editor.putString(SWITCH_ONE_KEY_POWER_OFF, SWITCH_OFF);
            editor.putString(SWITCH_AUTO_CHANGE_LANGUAGE, SWITCH_OFF);
            editor.commit();
            mHdmiCecManager.setCecSysfsValue(HdmiCecManager.FUN_CEC, HdmiCecManager.FUN_CLOSE);
        } else if (isOpen.equals(SWITCH_OFF) && on) {
            editor.putString(SWITCH_CEC, SWITCH_ON);
            editor.putString(SWITCH_ONE_KEY_PLAY, SWITCH_ON);
            editor.putString(SWITCH_ONE_KEY_POWER_OFF, SWITCH_ON);
            editor.putString(SWITCH_AUTO_CHANGE_LANGUAGE, SWITCH_ON);
            editor.commit();
            if (!isCecServiceRunning()) {
                Intent serviceIntent = new Intent();
                serviceIntent.setAction(CEC_ACTION);
                this.startService(serviceIntent);
            }
            mHdmiCecManager.setCecSysfsValue(HdmiCecManager.FUN_CEC, HdmiCecManager.FUN_OPEN);
            updateCecLanguage();
        }
    }

    private void switchOneKeyPlay(boolean on) {
        String isOpen = mSharepreference.getString(SWITCH_ONE_KEY_PLAY, SWITCH_OFF);
        Editor editor = this.getSharedPreferences(PREFERENCE_BOX_SETTING, Context.MODE_PRIVATE).edit();
        if (isOpen.equals(SWITCH_ON) && !on) {
            editor.putString(SWITCH_ONE_KEY_PLAY, SWITCH_OFF);
            editor.commit();
            mHdmiCecManager.setCecSysfsValue(HdmiCecManager.FUN_ONE_KEY_PLAY, HdmiCecManager.FUN_CLOSE);
        } else if (isOpen.equals(SWITCH_OFF) && on) {
            editor.putString(SWITCH_ONE_KEY_PLAY, SWITCH_ON);
            editor.commit();
            mHdmiCecManager.setCecSysfsValue(HdmiCecManager.FUN_ONE_KEY_PLAY, HdmiCecManager.FUN_OPEN);
        }
    }

    private void switchOneKeyPowerOff(boolean on) {
        String isOpen = mSharepreference.getString(SWITCH_ONE_KEY_POWER_OFF, SWITCH_OFF);
        Editor editor = this.getSharedPreferences(PREFERENCE_BOX_SETTING, Context.MODE_PRIVATE).edit();
        if (isOpen.equals(SWITCH_ON) && !on) {
            editor.putString(SWITCH_ONE_KEY_POWER_OFF, SWITCH_OFF);
            editor.commit();
            mHdmiCecManager.setCecSysfsValue(HdmiCecManager.FUN_ONE_KEY_POWER_OFF, HdmiCecManager.FUN_CLOSE);
        } else if (isOpen.equals(SWITCH_OFF) && on) {
            editor.putString(SWITCH_ONE_KEY_POWER_OFF, SWITCH_ON);
            editor.commit();
            mHdmiCecManager.setCecSysfsValue(HdmiCecManager.FUN_ONE_KEY_POWER_OFF, HdmiCecManager.FUN_OPEN);
        }
    }

    private void switchAutoChangeLanguage(boolean on) {
        String isOpen = mSharepreference.getString(SWITCH_AUTO_CHANGE_LANGUAGE, SWITCH_OFF);
        Editor editor = this.getSharedPreferences(PREFERENCE_BOX_SETTING, Context.MODE_PRIVATE).edit();
        if (isOpen.equals(SWITCH_ON) && !on) {
            editor.putString(SWITCH_AUTO_CHANGE_LANGUAGE, SWITCH_OFF);
            editor.commit();
            mHdmiCecManager.setCecSysfsValue(HdmiCecManager.FUN_AUTO_CHANGE_LANGUAGE, HdmiCecManager.FUN_CLOSE);
        } else if (isOpen.equals(SWITCH_OFF) && on) {
            editor.putString(SWITCH_AUTO_CHANGE_LANGUAGE, SWITCH_ON);
            editor.commit();
            if (!isCecServiceRunning()) {
                Intent serviceIntent = new Intent();
                serviceIntent.setAction(CEC_ACTION);
                this.startService(serviceIntent);
            }
            mHdmiCecManager.setCecSysfsValue(HdmiCecManager.FUN_AUTO_CHANGE_LANGUAGE, HdmiCecManager.FUN_OPEN);
            updateCecLanguage();
        }
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
                switchOneKeyPlay(type == ActionType.CEC_OVERVIEW_SWITCH_ON);
                goBack();
                return;
            case CEC_OVERVIEW_ONE_KEY_POWER_OFF:
                switchOneKeyPowerOff(type == ActionType.CEC_OVERVIEW_SWITCH_ON);
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
                    isOn = getStatus(isOneKeyPlayOn());
                    mActions.add(ActionType.CEC_OVERVIEW_ONE_KEY_PLAY.toAction(mResources, TextUtils.isEmpty(isOn) ? "" : isOn));
                    isOn = getStatus(isOneKeyPowerOff());
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
                if (isOneKeyPlayOn()) {
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
                if (isOneKeyPowerOff()) {
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

    private void initCecFun(){
        mHdmiCecManager = new HdmiCecManager(this);
        mSharepreference = getSharedPreferences(PREFERENCE_BOX_SETTING, Context.MODE_PRIVATE);

        Editor editor = this.getSharedPreferences(PREFERENCE_BOX_SETTING, Context.MODE_PRIVATE).edit();
        String str = mHdmiCecManager.getCurConfig();
        // get rid of '0x' prefix
        int cec_config = Integer.valueOf(str.substring(2, str.length()), 16);
        Log.d(TAG, "cec config str:" + str + ", value:" + cec_config);
        if ((cec_config & HdmiCecManager.MASK_FUN_CEC) != 0) {
            if ((cec_config & HdmiCecManager.MASK_ONE_KEY_PLAY) != 0) {
                editor.putString(SWITCH_ONE_KEY_PLAY, SWITCH_ON);
            } else {
                editor.putString(SWITCH_ONE_KEY_PLAY, SWITCH_OFF);
            }
            if ((cec_config & HdmiCecManager.MASK_ONE_KEY_STANDBY) != 0) {
                editor.putString(SWITCH_ONE_KEY_POWER_OFF, SWITCH_ON);
            } else {
                editor.putString(SWITCH_ONE_KEY_POWER_OFF, SWITCH_OFF);
            }
            if ((cec_config & HdmiCecManager.MASK_AUTO_CHANGE_LANGUAGE) != 0) {
                editor.putString(SWITCH_AUTO_CHANGE_LANGUAGE, SWITCH_ON);
            } else {
                editor.putString(SWITCH_AUTO_CHANGE_LANGUAGE, SWITCH_OFF);
            }
            editor.putString(SWITCH_CEC, SWITCH_ON);
        } else {
            editor.putString(SWITCH_ONE_KEY_PLAY, SWITCH_OFF);
            editor.putString(SWITCH_ONE_KEY_POWER_OFF, SWITCH_OFF);
            editor.putString(SWITCH_AUTO_CHANGE_LANGUAGE, SWITCH_OFF);
            editor.putString(SWITCH_CEC, SWITCH_OFF);
        }
        editor.commit();
        mHdmiCecManager.setCecEnv(cec_config);
    }

    private boolean isSwitchCecOn() {
        String isSwitchCecOn = mSharepreference.getString(SWITCH_CEC, SWITCH_OFF);
        return isSwitchCecOn.equals(SWITCH_ON);
    }

    private boolean isOneKeyPlayOn() {
        String isOneKeyPlayOn = mSharepreference.getString(SWITCH_ONE_KEY_PLAY, SWITCH_OFF);
        return isOneKeyPlayOn.equals(SWITCH_ON);
    }

    private boolean isOneKeyPowerOff() {
        String isOneKeyPowerOff = mSharepreference.getString(SWITCH_ONE_KEY_POWER_OFF, SWITCH_OFF);
        return isOneKeyPowerOff.equals(SWITCH_ON);
    }

    private boolean isAutoChangeLanguage() {
        String isAutoChangeLanguage = mSharepreference.getString(SWITCH_AUTO_CHANGE_LANGUAGE, SWITCH_OFF);
        return isAutoChangeLanguage.equals(SWITCH_ON);
    }

    private String getStatus(boolean status) {
        if (status) {
            return getString(R.string.settings_on);
        } else {
            return getString(R.string.settings_off);
        }
    }

    private void updateCecLanguage(){
        String curLanguage = mHdmiCecManager.getCurLanguage();
        Log.d(TAG,"update curLanguage:" + curLanguage);
        if (curLanguage == null) return;

        String[] cec_language_list = getResources().getStringArray(R.array.cec_language);
        String[] language_list = getResources().getStringArray(R.array.language);
        String[] country_list = getResources().getStringArray(R.array.country);
        mHdmiCecManager.setLanguageList(cec_language_list, language_list, country_list);
        mHdmiCecManager.doUpdateCECLanguage(curLanguage);

    }
}

