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

import android.content.Context;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import com.droidlogic.app.PlayBackManager;

public class PlayBackSettingsActivity extends BaseSettingsActivity implements ActionAdapter.Listener {
    private static final String TAG = "PlayBackSettings";

    private PlayBackManager mPlayBackManager;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        mPlayBackManager = new PlayBackManager(this);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActionClicked(Action action) {
        /*
         * For list preferences
         */
        ActionKey<ActionType, ActionBehavior> actionKey = new ActionKey<ActionType, ActionBehavior>(ActionType.class, ActionBehavior.class, action.getKey());
        final ActionType type = actionKey.getType();
        switch ((ActionType) mState) {
            case PLAYBACK_HDMI_SELFADAPTION_SWITCH:
                mPlayBackManager.setHdmiSelfadaption(type == ActionType.PLAYBACK_OVERVIEW_SWITCH_ON);
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
        return ActionType.PLAYBACK_SETTINGS;
    }

    @Override
    protected void refreshActionList() {
        mActions.clear();
        switch ((ActionType) mState) {
            case PLAYBACK_SETTINGS:
                String isOn = getStatus(mPlayBackManager.isHdmiSelfadaptionOn());
                mActions.add(ActionType.PLAYBACK_HDMI_SELFADAPTION_SWITCH.toAction(mResources, TextUtils.isEmpty(isOn) ? "" : isOn));
                break;
            case PLAYBACK_HDMI_SELFADAPTION_SWITCH:
                Action switch_on = ActionType.PLAYBACK_OVERVIEW_SWITCH_ON.toAction(mResources);
                Action switch_off = ActionType.PLAYBACK_OVERVIEW_SWITCH_OFF.toAction(mResources);
                if (mPlayBackManager.isHdmiSelfadaptionOn()) {
                    switch_on.setChecked(true);
                    switch_off.setChecked(false);
                } else {
                    switch_on.setChecked(false);
                    switch_off.setChecked(true);
                }
                mActions.add(switch_on);
                mActions.add(switch_off);
                break;
            default:
                break;
        }
    }

    @Override
    protected void updateView() {
        refreshActionList();
        switch ((ActionType) mState) {
            case PLAYBACK_SETTINGS:
                setView(R.string.playback_settings, R.string.playback_settings, 0, R.drawable.ic_settings_now_movies);
                break;
            case PLAYBACK_HDMI_SELFADAPTION_SWITCH:
                setView(R.string.playback_hdmi_selfadaption, R.string.playback_settings, 0, R.drawable.ic_settings_now_movies);
                break;
            default:
                break;
        }
    }

    private String getStatus(boolean status) {
        if (status) {
            return getString(R.string.settings_on);
        } else {
            return getString(R.string.settings_off);
        }
    }

    @Override
    protected void setProperty(boolean enable) {
    }
}

