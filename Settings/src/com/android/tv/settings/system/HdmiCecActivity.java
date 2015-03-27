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
import com.droidlogic.app.SystemControlManager;

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

import java.util.Locale;
import java.lang.reflect.Method;

public class HdmiCecActivity extends BaseSettingsActivity implements ActionAdapter.Listener {
    private static final String TAG = "HdmiCecOutput";
    private static final String CEC_DEVICE_FILE = "/sys/devices/virtual/switch/lang_config/state";
    private static final String CEC_SYS = "/sys/class/amhdmitx/amhdmitx0/cec_config";
    private static final String PREFERENCE_BOX_SETTING = "preference_box_settings";
    private static final String CEC_PROP = "ubootenv.var.cecconfig";
    private static final String CEC_0 = "cec0";
    private static final String CEC_F = "cecf";
    private static final String SWITCH_ON = "true";
    private static final String SWITCH_OFF = "false";
    private static final String SWITCH_CEC = "switch_cec";
    private static final String SWITCH_ONE_KEY_PLAY = "switch_one_key_play";
    private static final String SWITCH_ONE_KEY_POWER_OFF = "switch_one_key_power_off";
    private static final String SWITCH_AUTO_CHANGE_LANGUAGE = "switch_auto_change_languace";
    private static final String CEC_SERVICE = "com.android.tv.settings.system.CecService";
    private static final String CEC_ACTION = "CEC_LANGUAGE_AUTO_SWITCH";

    private static final int FUN_CEC = 0x00;
    private static final int FUN_ONE_KEY_POWER_OFF = 0x01;
    private static final int FUN_ONE_KEY_PLAY = 0x02;
    private static final int FUN_AUTO_CHANGE_LANGUAGE = 0x03;
    private static final boolean FUN_OPEN = true;
    private static final boolean FUN_CLOSE = false;

    private SharedPreferences sharepreference = null;
    private SystemControlManager mSystemControlManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        mSystemControlManager = new SystemControlManager(this);
        sharepreference = getSharedPreferences(PREFERENCE_BOX_SETTING, Context.MODE_PRIVATE);
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
        String isOpen = sharepreference.getString(SWITCH_CEC, SWITCH_OFF);
        Editor editor = this.getSharedPreferences(PREFERENCE_BOX_SETTING, Context.MODE_PRIVATE).edit();
        if (isOpen.equals(SWITCH_ON) && !on) {
            editor.putString(SWITCH_CEC, SWITCH_OFF);
            editor.putString(SWITCH_ONE_KEY_PLAY, SWITCH_OFF);
            editor.putString(SWITCH_ONE_KEY_POWER_OFF, SWITCH_OFF);
            editor.putString(SWITCH_AUTO_CHANGE_LANGUAGE, SWITCH_OFF);
            editor.commit();
            setCecSysfsValue(FUN_CEC, FUN_CLOSE);
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
            setCecSysfsValue(FUN_CEC, FUN_OPEN);
            updateCecLanguage();
        }
    }

    private void switchOneKeyPlay(boolean on) {
        String isOpen = sharepreference.getString(SWITCH_ONE_KEY_PLAY, SWITCH_OFF);
        Editor editor = this.getSharedPreferences(PREFERENCE_BOX_SETTING, Context.MODE_PRIVATE).edit();
        if (isOpen.equals(SWITCH_ON) && !on) {
            editor.putString(SWITCH_ONE_KEY_PLAY, SWITCH_OFF);
            editor.commit();
            setCecSysfsValue(FUN_ONE_KEY_PLAY, FUN_CLOSE);
        } else if (isOpen.equals(SWITCH_OFF) && on) {
            editor.putString(SWITCH_ONE_KEY_PLAY, SWITCH_ON);
            editor.commit();
            setCecSysfsValue(FUN_ONE_KEY_PLAY, FUN_OPEN);
        }
    }

    private void switchOneKeyPowerOff(boolean on) {
        String isOpen = sharepreference.getString(SWITCH_ONE_KEY_POWER_OFF, SWITCH_OFF);
        Editor editor = this.getSharedPreferences(PREFERENCE_BOX_SETTING, Context.MODE_PRIVATE).edit();
        if (isOpen.equals(SWITCH_ON) && !on) {
            editor.putString(SWITCH_ONE_KEY_POWER_OFF, SWITCH_OFF);
            editor.commit();
            setCecSysfsValue(FUN_ONE_KEY_POWER_OFF, FUN_CLOSE);
        } else if (isOpen.equals(SWITCH_OFF) && on) {
            editor.putString(SWITCH_ONE_KEY_POWER_OFF, SWITCH_ON);
            editor.commit();
            setCecSysfsValue(FUN_ONE_KEY_POWER_OFF, FUN_OPEN);
        }
    }

    private void switchAutoChangeLanguage(boolean on) {
        String isOpen = sharepreference.getString(SWITCH_AUTO_CHANGE_LANGUAGE, SWITCH_OFF);
        Editor editor = this.getSharedPreferences(PREFERENCE_BOX_SETTING, Context.MODE_PRIVATE).edit();
        if (isOpen.equals(SWITCH_ON) && !on) {
            editor.putString(SWITCH_AUTO_CHANGE_LANGUAGE, SWITCH_OFF);
            editor.commit();
            setCecSysfsValue(FUN_AUTO_CHANGE_LANGUAGE, FUN_CLOSE);
        } else if (isOpen.equals(SWITCH_OFF) && on) {
            editor.putString(SWITCH_AUTO_CHANGE_LANGUAGE, SWITCH_ON);
            editor.commit();
            if (!isCecServiceRunning()) {
                Intent serviceIntent = new Intent();
                serviceIntent.setAction(CEC_ACTION);
                this.startService(serviceIntent);
            }
            setCecSysfsValue(FUN_AUTO_CHANGE_LANGUAGE, FUN_OPEN);
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
        Editor editor = this.getSharedPreferences(PREFERENCE_BOX_SETTING, Context.MODE_PRIVATE).edit();
        String str = getBinaryString(mSystemControlManager.readSysFs(CEC_SYS));
        int []funs = getBinaryArray(str);
        if (funs.length == 4) {
            if (funs[FUN_CEC] != 0) {
                if (funs[FUN_ONE_KEY_PLAY] != 0) {
                    editor.putString(SWITCH_ONE_KEY_PLAY, SWITCH_ON);
                } else {
                    editor.putString(SWITCH_ONE_KEY_PLAY, SWITCH_OFF);
                }
                if (funs[FUN_ONE_KEY_POWER_OFF] != 0) {
                    editor.putString(SWITCH_ONE_KEY_POWER_OFF, SWITCH_ON);
                } else {
                    editor.putString(SWITCH_ONE_KEY_POWER_OFF, SWITCH_OFF);
                }
                if (funs[FUN_AUTO_CHANGE_LANGUAGE] != 0) {
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
        }
        editor.commit();
        mSystemControlManager.setBootenv(CEC_PROP, arrayToString(funs));
    }

    private boolean isSwitchCecOn() {
        String isSwitchCecOn = sharepreference.getString(SWITCH_CEC, SWITCH_OFF);
        return isSwitchCecOn.equals(SWITCH_ON);
    }

    private boolean isOneKeyPlayOn() {
        String isOneKeyPlayOn = sharepreference.getString(SWITCH_ONE_KEY_PLAY, SWITCH_OFF);
        return isOneKeyPlayOn.equals(SWITCH_ON);
    }

    private boolean isOneKeyPowerOff() {
        String isOneKeyPowerOff = sharepreference.getString(SWITCH_ONE_KEY_POWER_OFF, SWITCH_OFF);
        return isOneKeyPowerOff.equals(SWITCH_ON);
    }

    private boolean isAutoChangeLanguage() {
        String isAutoChangeLanguage = sharepreference.getString(SWITCH_AUTO_CHANGE_LANGUAGE, SWITCH_OFF);
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
        String curLanguage = mSystemControlManager.readSysFs(CEC_DEVICE_FILE);
        Log.d(TAG,"update curLanguage:" + curLanguage);
        if (curLanguage == null) return;

        int i = -1;
        String[] cec_language_list = getResources().getStringArray(R.array.cec_language);
        for (int j = 0; j < cec_language_list.length; j++) {
            if (curLanguage != null && curLanguage.trim().equals(cec_language_list[j])) {
                i = j;
                break;
            }
        }
        if (i >= 0) {
            String able = getResources().getConfiguration().locale.getCountry();
            String[] language_list = getResources().getStringArray(R.array.language);
            String[] country_list = getResources().getStringArray(R.array.country);
            if (able.equals(country_list[i])) {
                Log.d(TAG, "no need to change language");
                return;
            } else {
                Locale l = new Locale(language_list[i], country_list[i]);
                Log.d(TAG, "change the language right now !!!");
                updateLanguage(l);
            }
        } else {
            Log.d(TAG, "the language code is not support right now !!!");
        }
    }

    private void setCecSysfsValue(int fun, boolean isOn) {
        String cec_config = mSystemControlManager.getBootenv(CEC_PROP, CEC_0);
        String configString = getBinaryString(cec_config);
        int tmpArray[] = getBinaryArray(configString);
        if (fun != FUN_CEC) {
            if (CEC_0.equals(cec_config)) {
                return;
            }
        }

        if (fun == FUN_CEC) {
            if (isOn) {
                mSystemControlManager.setBootenv(CEC_PROP, CEC_F);
                mSystemControlManager.writeSysFs(CEC_SYS, "f");
            } else {
                mSystemControlManager.setBootenv(CEC_PROP, CEC_0);
                mSystemControlManager.writeSysFs(CEC_SYS, "0");
            }
        } else if (fun == FUN_ONE_KEY_PLAY) {
            if (isOn) {
                //tmpArray[2] = 1;
                //tmpArray[0] = 1;
                tmpArray[FUN_ONE_KEY_PLAY] = 1;
                tmpArray[FUN_CEC] = 1;
            } else {
                tmpArray[FUN_ONE_KEY_PLAY] = 0;
                tmpArray[FUN_CEC] = 1;
            }
            String writeConfig = arrayToString(tmpArray);
            mSystemControlManager.setBootenv(CEC_PROP, writeConfig);
            Log.d(TAG, "==== cec set config : " + writeConfig);
            String s = writeConfig.substring(writeConfig.length() - 1, writeConfig.length());
            mSystemControlManager.writeSysFs(CEC_SYS, s);
        } else if (fun == FUN_ONE_KEY_POWER_OFF) {
            if (isOn) {
                //tmpArray[1] = 1;
                //tmpArray[0] = 1;
                tmpArray[FUN_ONE_KEY_POWER_OFF] = 1;
                tmpArray[FUN_CEC] = 1;
            } else {
                tmpArray[FUN_ONE_KEY_POWER_OFF] = 0;
                tmpArray[FUN_CEC] = 1;
            }
            String writeConfig = arrayToString(tmpArray);
            mSystemControlManager.setBootenv(CEC_PROP, writeConfig);
            Log.d(TAG, "==== cec set config : " + writeConfig);
            String s = writeConfig.substring(writeConfig.length() - 1, writeConfig.length());
            mSystemControlManager.writeSysFs(CEC_SYS, s);
        }else if(fun == FUN_AUTO_CHANGE_LANGUAGE){
            if (isOn) {
                //tmpArray[3] = 1;
                //tmpArray[0] = 1;
                tmpArray[FUN_AUTO_CHANGE_LANGUAGE] = 1;
                tmpArray[FUN_CEC] = 1;
            } else {
                tmpArray[FUN_AUTO_CHANGE_LANGUAGE] = 0;
                tmpArray[FUN_CEC] = 1;
            }
            String writeConfig = arrayToString(tmpArray);
            mSystemControlManager.setBootenv(CEC_PROP, writeConfig);
            Log.d(TAG, "==== cec set config : " + writeConfig);
            String s = writeConfig.substring(writeConfig.length() - 1, writeConfig.length());
            mSystemControlManager.writeSysFs(CEC_SYS, s);
        }
    }

    private String arrayToString(int[] array) {
        String getIndexString = "0123456789abcdef";
        int total = 0;
        System.out.println();
        for (int i = 0; i < array.length; i++) {
            total = total + (int) (array[i] * Math.pow(2, array.length - i - 1));
        }
        Log.d(TAG, "in arrayToString cecConfig is:" + total);
        String cecConfig = "cec" + getIndexString.charAt(total);
        Log.d(TAG, "in arrayToString cecConfig is:" + cecConfig);
        return cecConfig;
    }

    private int[] getBinaryArray(String binaryString) {
        int[] tmp = new int[4];
        for (int i = 0; i < binaryString.length(); i++) {
            String tmpString = String.valueOf(binaryString.charAt(i));
            tmp[i] = Integer.parseInt(tmpString);
        }
        return tmp;
    }

    private String getBinaryString(String config) {
        String indexString = "0123456789abcdef";
        String configString = config.substring(config.length() - 1, config.length());
        int indexOfConfigNum = indexString.indexOf(configString);
        String ConfigBinary = Integer.toBinaryString(indexOfConfigNum);
        if (ConfigBinary.length() < 4) {
            for (int i = ConfigBinary.length(); i < 4; i++) {
                ConfigBinary = "0" + ConfigBinary;
            }
        }
        return ConfigBinary;
    }

    private void updateLanguage(Locale locale) {
        try {
            Object objIActMag;
            Class clzIActMag = Class.forName("android.app.IActivityManager");
            Class clzActMagNative = Class.forName("android.app.ActivityManagerNative");
            Method mtdActMagNative$getDefault = clzActMagNative.getDeclaredMethod("getDefault");

            objIActMag = mtdActMagNative$getDefault.invoke(clzActMagNative);
            Method mtdIActMag$getConfiguration = clzIActMag.getDeclaredMethod("getConfiguration");
            Configuration config = (Configuration) mtdIActMag$getConfiguration.invoke(objIActMag);
            config.locale = locale;

            Class[] clzParams = { Configuration.class };
            Method mtdIActMag$updateConfiguration = clzIActMag.getDeclaredMethod("updateConfiguration", clzParams);
            mtdIActMag$updateConfiguration.invoke(objIActMag, config);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

