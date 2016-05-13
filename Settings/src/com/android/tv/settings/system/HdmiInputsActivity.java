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

import com.android.tv.settings.ActionKey;
import com.android.tv.settings.ActionBehavior;
import com.android.tv.settings.BaseSettingsActivity;
import com.android.tv.settings.dialog.old.ActionAdapter;
import com.android.tv.settings.dialog.old.Action;
import com.android.tv.settings.R;

import android.content.Intent;
import android.content.Context;
import android.content.res.Resources;
import android.hardware.hdmi.HdmiTvClient.SelectCallback;
import android.hardware.hdmi.HdmiControlManager;
import android.hardware.hdmi.HdmiDeviceInfo;
import android.hardware.hdmi.HdmiTvClient;
import android.provider.Settings.System;
import android.provider.Settings.Global;
import android.media.tv.TvInputManager;
import android.media.tv.TvInputInfo;
import android.os.SystemProperties;
import android.os.ServiceManager;
import android.os.Bundle;
import android.util.Log;

import com.droidlogic.app.tv.DroidLogicTvUtils;

public class HdmiInputsActivity extends BaseSettingsActivity implements ActionAdapter.Listener {
    public static final String TAG = "HdmiInputsActivity";
    public static final String DEV = "HDMI";
    public static final boolean DEBUG = true;
    public static final int DISABLED = 0;
    public static final int ENABLED = 1;
    public static final int FIT = DroidLogicTvUtils.DEVICE_ID_HDMI1 - 1;

    enum ActionType {
        KEY_DEVICES,
        KEY_SEARCH,
        KEY_CONTROL,
        KEY_DEVICE_HDMI1,
        KEY_DEVICE_HDMI2,
        KEY_DEVICE_HDMI3,
        KEY_CONTROL_HDMI,
        KEY_DEVICE_OFF,
        KEY_TV_ON,
        KEY_OFF,
        KEY_ON;

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
            return new ActionKey<>(t, b).getKey();
        }

    }

    private HdmiTvClient mHdmiTvClient;
    private HdmiControlManager mHdmiManager;
    private TvInputManager mTvInputManager;

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        initObject();
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActionClicked(Action action) {
        /*
         * For list preferences
         */
        String key = null;
        ActionKey<ActionType, ActionBehavior> actionKey = new ActionKey<ActionType, ActionBehavior>(ActionType.class, ActionBehavior.class, action.getKey());
        final ActionType type = actionKey.getType();
        switch ((ActionType) mState) {
            case KEY_SEARCH:
                int portid = 0;
                if (type == ActionType.KEY_DEVICE_HDMI1) {
                    portid = 1;
                } else if(type == ActionType.KEY_DEVICE_HDMI2) {
                    portid = 2;
                } else if(type == ActionType.KEY_DEVICE_HDMI3){
                    portid = 3;
                }
                if (readCecOption(getCecOptionKey(ActionType.KEY_CONTROL_HDMI))) {
                    selectDevice(portid);
                    SwitchHdmiInput(portid);
                }
                goBack();
                return;
            case KEY_CONTROL_HDMI:
                if (key == null) {
                    key = getCecOptionKey(ActionType.KEY_CONTROL_HDMI);
                }
            case KEY_DEVICE_OFF:
                if (key == null) {
                    key = getCecOptionKey(ActionType.KEY_DEVICE_OFF);
                }
            case KEY_TV_ON:
                if (key == null) {
                    key = getCecOptionKey(ActionType.KEY_TV_ON);
                }
                if (mHdmiTvClient != null) {
                    writeCecOption(key, (type == ActionType.KEY_ON));
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
        return ActionType.KEY_DEVICES;
    }

    @Override
    protected void refreshActionList() {
        String str;

        mActions.clear();
        switch ((ActionType) mState) {
            case KEY_DEVICES:
                mActions.add(ActionType.KEY_SEARCH.toAction(mResources, getString(R.string.inputs_custom_name), getSummary(ActionType.KEY_SEARCH)));
                mActions.add(ActionType.KEY_CONTROL.toAction(mResources, getString(R.string.inputs_header_cec), getSummary(ActionType.KEY_CONTROL)));
                break;
            case KEY_SEARCH:
                for (HdmiDeviceInfo info : mHdmiTvClient.getDeviceList()) {
                    switch (info.getPhysicalAddress()) {
                        case 0x1000:
                            mActions.add(ActionType.KEY_DEVICE_HDMI1.toAction(mResources, DEV + 1, info.getDisplayName()));
                            break;
                        case 0x2000:
                            mActions.add(ActionType.KEY_DEVICE_HDMI2.toAction(mResources, DEV + 2, info.getDisplayName()));
                            break;
                        case 0x3000:
                            mActions.add(ActionType.KEY_DEVICE_HDMI3.toAction(mResources, DEV + 3, info.getDisplayName()));
                            break;
                    }
                }
                break;
            case KEY_CONTROL:
                mActions.add(ActionType.KEY_CONTROL_HDMI.toAction(mResources, getString(R.string.inputs_hdmi_control), getSummary(ActionType.KEY_CONTROL_HDMI)));
                if (readCecOption(getCecOptionKey(ActionType.KEY_CONTROL_HDMI))) {
                    mActions.add(ActionType.KEY_DEVICE_OFF.toAction(mResources, getString(R.string.inputs_device_auto_off), getSummary(ActionType.KEY_DEVICE_OFF)));
                    mActions.add(ActionType.KEY_TV_ON.toAction(mResources, getString(R.string.inputs_tv_auto_on), getSummary(ActionType.KEY_TV_ON)));
                }
                break;
            case KEY_CONTROL_HDMI:
            case KEY_DEVICE_OFF:
            case KEY_TV_ON:
                mActions.add(ActionType.KEY_ON.toAction(mResources, getString(R.string.on), null));
                mActions.add(ActionType.KEY_OFF.toAction(mResources, getString(R.string.off), null));
                break;
            default:
                break;
        }
    }

    @Override
    protected void updateView() {
        refreshActionList();
        switch ((ActionType) mState) {
            case KEY_DEVICES:
                setView(R.string.inputs_inputs, R.string.header_category_preferences, 0, R.drawable.ic_settings_inputs);
                break;
            case KEY_SEARCH:
                setView(R.string.inputs_custom_name, R.string.inputs_inputs, 0, R.drawable.ic_settings_inputs);
                break;
            case KEY_CONTROL:
                setView(R.string.inputs_header_cec, R.string.inputs_inputs, 0, R.drawable.ic_settings_inputs);
                break;
            case KEY_CONTROL_HDMI:
                setView(R.string.inputs_hdmi_control, R.string.inputs_inputs, 0, R.drawable.ic_settings_inputs);
                break;
            case KEY_DEVICE_OFF:
                setView(R.string.inputs_device_auto_off, R.string.inputs_inputs, 0, R.drawable.ic_settings_inputs);
                break;
            case KEY_TV_ON:
                setView(R.string.inputs_tv_auto_on, R.string.inputs_inputs, 0, R.drawable.ic_settings_inputs);
                break;
            default:
                break;
        }
    }

    @Override
    protected void setProperty(boolean enable) {
    }

    private void initObject() {
        mTvInputManager = (TvInputManager) getSystemService(Context.TV_INPUT_SERVICE);
        mHdmiManager = (HdmiControlManager) getSystemService(Context.HDMI_CONTROL_SERVICE);
        if (mHdmiManager == null) Log.e(TAG, "Failed to get HdmiManager ");
        mHdmiTvClient = mHdmiManager.getTvClient();
        if (mHdmiTvClient == null) Log.e(TAG, "Failed to get HdmiTvClient ");
    }

    private void SwitchHdmiInput(int portid) {
        for (TvInputInfo info : mTvInputManager.getTvInputList()) {
            String name = info.loadLabel(this).toString();
            String port = name.replaceAll(DEV, "");
            if (!port.equals("") && port.length() == 1) {
                int deviceid = Integer.parseInt(port);
                if (deviceid == portid) {
                    SystemProperties.set(DroidLogicTvUtils.SOURCE_NAME, name);
                    Intent intent = new Intent(Intent.ACTION_MAIN);
                    intent.putExtra(DroidLogicTvUtils.EXTRA_CHANNEL_NUMBER, name);
                    intent.setClassName("com.droidlogic.tvsource", "com.droidlogic.tvsource.DroidLogicTv");
                    startActivity(intent);
                    return;
                }
            }
        }
    }

    private void selectDevice(int portid) {
        for (HdmiDeviceInfo info : mHdmiTvClient.getDeviceList()) {
            if (mHdmiTvClient != null &&
                info.getPhysicalAddress() == (portid << 12)) {
                mHdmiTvClient.deviceSelect(info.getLogicalAddress(), new SelectCallback() {
                    @Override
                    public void onComplete(int result) {
                    }
                });
            }
        }
    }

    private int getCurrentDeviceId() {
        int id = System.getInt(getContentResolver(), DroidLogicTvUtils.TV_CURRENT_DEVICE_ID, 0);
        for (TvInputInfo info : mTvInputManager.getTvInputList()) {
            String name = info.loadLabel(this).toString();
            String port = name.replaceAll(DEV, "");
            if (!port.equals("") && port.length() == 1) {
                int deviceid = Integer.parseInt(port);
                if (id == (deviceid + FIT)) return deviceid;
            }
        }
        return 0;
    }

    private String getCecOptionKey(ActionType action) {
        switch (action) {
            case KEY_CONTROL_HDMI:
                return Global.HDMI_CONTROL_ENABLED;
            case KEY_DEVICE_OFF:
                return Global.HDMI_CONTROL_AUTO_DEVICE_OFF_ENABLED;
            case KEY_TV_ON:
                return Global.HDMI_CONTROL_AUTO_WAKEUP_ENABLED;
        }
        return "";
    }

    private static int toInt(boolean enabled) {
        return enabled ? ENABLED : DISABLED;
    }

    private boolean readCecOption(String key) {
        return Global.getInt(getContentResolver(), key, toInt(true)) == ENABLED;
    }

    private void writeCecOption(String key, boolean value) {
        if (DEBUG) {
            Log.d(TAG, "Writing CEC option " + key + " to " + value);
        }
        Global.putInt(getContentResolver(), key, toInt(value));
    }


    private String getSummary(ActionType action) {
        ActionType key = action;
        switch (action) {
            case KEY_SEARCH:
                int id = getCurrentDeviceId();
                if (id != 0) return DEV + id;
                else return "";
            case KEY_CONTROL:
                key = ActionType.KEY_CONTROL_HDMI;
            case KEY_CONTROL_HDMI:
            case KEY_DEVICE_OFF:
            case KEY_TV_ON:
                if (mHdmiTvClient != null && readCecOption(getCecOptionKey(key))) {
                    return getString(R.string.on);
                }
                return getString(R.string.off);
        }
        return "";
    }
}
