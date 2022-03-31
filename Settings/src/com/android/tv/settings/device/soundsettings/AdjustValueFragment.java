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

package com.android.tv.settings.device.soundsettings;


import android.os.Bundle;
import android.os.Handler;
import android.support.v17.preference.LeanbackPreferenceFragment;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.os.SystemProperties;
import android.util.Log;
import android.text.TextUtils;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;

import android.provider.Settings;


import com.android.tv.settings.R;

import android.media.AudioManager;
import android.content.Context;



public class AdjustValueFragment extends LeanbackPreferenceFragment implements SeekBar.OnSeekBarChangeListener {

    private static final String TAG = "AdjustValueFragment";

    private SeekBar seekbar_media;
    private SeekBar seekbar_alarm;
    private SeekBar seekbar_notification;
    private TextView text_media;
    private TextView text_alarm;
    private TextView text_notification;
    private ImageView image_media;
    private ImageView image_alarm;
    private ImageView image_notification;

    private boolean isSeekBarInited = false;
	AudioManager mAudioManager;

    public static AdjustValueFragment newInstance() {
        return new AdjustValueFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.xml.seekbar, container, false);
        return view;
    }

    @Override
    public void onViewCreated (View view, Bundle savedInstanceState) {
        initSeekBar(view);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

    }

    private void initSeekBar(View view) {
        if (mAudioManager == null) {
            mAudioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
        }
        int status = -1;
        int MaxMediaVolume;
        int MinMediaVolume;
        int CurMediaVolume;
        int MaxAlarmVolume;
        int MinAlarmVolume;
        int CurAlarmVolume;
        int MaxNotificationVolume;
        int MinNotificationVolume;
        int CurNotificationVolume;
        boolean hasfocused = false;

        MaxMediaVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        MinMediaVolume = mAudioManager.getStreamMinVolume(AudioManager.STREAM_MUSIC);
        CurMediaVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        Log.d(TAG,"MaxMediaVolume: "+ MaxMediaVolume + "MinMediaVolume"+ MinMediaVolume + "CurMediaVolume : "+CurMediaVolume);
	
        seekbar_media = (SeekBar) view.findViewById(R.id.seekbar_media);
        text_media = (TextView) view.findViewById(R.id.text_media);
        image_media = (ImageView) view.findViewById(R.id.media_icon);
 
        if(CurMediaVolume == 0) {
              image_media.setImageResource(R.drawable.ic_volume_off);
        }
        seekbar_media.setOnSeekBarChangeListener(this);
        seekbar_media.setMax(MaxMediaVolume);
        seekbar_media.setProgress(CurMediaVolume);
        seekbar_media.requestFocus();
        hasfocused = true;
        MaxAlarmVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM);
        MinAlarmVolume = mAudioManager.getStreamMinVolume(AudioManager.STREAM_ALARM);
        CurAlarmVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_ALARM);

        Log.d(TAG,"MaxAlarmVolume: "+ MaxAlarmVolume + "MinAlarmVolume"+ MinAlarmVolume + "CurAlarmVolume : "+CurAlarmVolume);
        seekbar_alarm = (SeekBar) view.findViewById(R.id.seekbar_alarm);
        text_alarm = (TextView) view.findViewById(R.id.text_alarm);
        image_alarm = (ImageView) view.findViewById(R.id.alarm_icon);
        if(CurAlarmVolume == 1) {
             image_alarm.setImageResource(R.drawable.ic_volume_alarm_mute);
        }
        seekbar_alarm.setOnSeekBarChangeListener(this);
        seekbar_alarm.setMax(MaxAlarmVolume);
        seekbar_alarm.setMin(MinAlarmVolume);
        seekbar_alarm.setProgress(CurAlarmVolume);          
        if (!hasfocused) {
            seekbar_alarm.requestFocus();
            hasfocused = true;
        }
        MaxNotificationVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION);
        MinNotificationVolume = mAudioManager.getStreamMinVolume(AudioManager.STREAM_NOTIFICATION);
        CurNotificationVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION);
        Log.d(TAG,"MaxNotificationVolume: "+ MaxNotificationVolume + "MinNotificationVolume"+ MinNotificationVolume + "CurNotificationVolume : "+CurNotificationVolume);
        seekbar_notification = (SeekBar) view.findViewById(R.id.seekbar_notification);
        text_notification = (TextView) view.findViewById(R.id.text_notification);
        image_notification = (ImageView) view.findViewById(R.id.notification_icon);
        if(CurNotificationVolume == 0) {
            image_notification.setImageResource(R.drawable.ic_notifications_off_24dp);
        }
        seekbar_notification.setOnSeekBarChangeListener(this);
	seekbar_notification.setMax(MaxNotificationVolume);
        seekbar_notification.setProgress(CurNotificationVolume);    
        if (!hasfocused) {
            seekbar_notification.requestFocus();
            hasfocused = true;
        }
        isSeekBarInited = true;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (!isSeekBarInited) {
            return;
        }
        switch (seekBar.getId()) {
            case R.id.seekbar_media:{
                //Log.d(TAG,"progress seekbar_media: " + progress);
                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,progress,0);
                if(progress == 0) {
                       image_media.setImageResource(R.drawable.ic_volume_off);
                } else {
                       image_media.setImageResource(R.drawable.ic_volume_up);
                }
                break;
            }
            case R.id.seekbar_alarm:{
                //Log.d(TAG,"progress seekbar_alarm: " + progress);
                mAudioManager.setStreamVolume(AudioManager.STREAM_ALARM,progress,0);
                if(progress == 1) {
                       image_alarm.setImageResource(R.drawable.ic_volume_alarm_mute);
                } else {
                       image_alarm.setImageResource(R.drawable.ic_volume_alarm);
                }
                break;
            }
            case R.id.seekbar_notification:{
				//Log.d(TAG,"progress seekbar_notification: " + progress);
                mAudioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION,progress,0);
                if(progress == 0) {
                       image_notification.setImageResource(R.drawable.ic_notifications_off_24dp);
                } else {
                       image_notification.setImageResource(R.drawable.ic_notifications);
                }
                break;
            }
            default:
                break;
        }
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    private void setShow(int id, int value) {
        switch (id) {
            case R.id.seekbar_media:{
                text_media.setText(getShowString(R.string.media_volume, value));
                break;
            }
  
            case R.id.seekbar_alarm:{
                text_alarm.setText(getShowString(R.string.alarm_volume, value));
                break;
            }
            case R.id.seekbar_notification:{
                text_notification.setText(getShowString(R.string.notification_volume, value));
                break;
            }
  
            default:
                break;
        }
    }

    private String getShowString(int resid, int value) {
        return getActivity().getResources().getString(resid) + ": " + value + "%";
    }
}
