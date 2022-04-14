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

package com.android.tv.settings.display;

import android.annotation.SuppressLint;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.os.Bundle;
import android.support.v17.preference.LeanbackPreferenceFragment;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.util.Log;
import android.view.Display.Mode;
import android.view.View;
import android.widget.TextView;
import android.os.SystemProperties;
import android.support.annotation.Keep;

import com.android.tv.settings.R;
import com.android.tv.settings.data.ConstData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


@Keep
public class DeviceFragment extends LeanbackPreferenceFragment implements Preference.OnPreferenceChangeListener,
        Preference.OnPreferenceClickListener {
    protected static final String TAG = "DeviceFragment";
    public static final String KEY_RESOLUTION = "resolution";
    public static final String KEY_COLOR = "color";
    public static final String KEY_ZOOM = "zoom";
    public static final String KEY_ADVANCED_SETTINGS = "advanced_settings";
    protected PreferenceScreen mPreferenceScreen;
    /**
     * 分辨率设置
     */
    protected ListPreference mResolutionPreference;
    /**
     * 屏幕颜色率设置
     */
    protected ListPreference mColorPreference;
    /**
     * 缩放设置
     */
    protected Preference mZoomPreference;
    /**
     * 高级设置
     */
    protected Preference mAdvancedSettingsPreference;
    /**
     * 当前显示设备对应的信息
     */
    protected DisplayInfo mDisplayInfo;
    /**
     * 标题
     */
    protected TextView mTextTitle;
    /**
     * 标识平台
     */
    protected String mStrPlatform;
    protected boolean mIsUseDisplayd;
    /**
     * 显示管理
     */
    protected DisplayManager mDisplayManager;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.display_device, null);
        initData();
        initEvent();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        rebuildView();
        updateResolutionValue();
        updateColorValue();
    }


    @Override
    public void onPause() {
        super.onPause();
    }


    protected void initData() {
        mStrPlatform = SystemProperties.get("ro.board.platform");
        mIsUseDisplayd = false;//SystemProperties.getBoolean("ro.rk.displayd.enable", true);
        mDisplayManager = (DisplayManager) getActivity().getSystemService(Context.DISPLAY_SERVICE);
        mPreferenceScreen = getPreferenceScreen();
        mAdvancedSettingsPreference = findPreference(KEY_ADVANCED_SETTINGS);
        mResolutionPreference = (ListPreference) findPreference(KEY_RESOLUTION);
        mColorPreference = (ListPreference) findPreference(KEY_COLOR);

        mZoomPreference = findPreference(KEY_ZOOM);
        mTextTitle = (TextView) getActivity().findViewById(android.support.v7.preference.R.id.decor_title);
        if (!mIsUseDisplayd) {
            mDisplayInfo = getDisplayInfo();
        }
        //if(!mStrPlatform.contains("3328"))
        //mPreferenceScreen.removePreference(mAdvancedSettingsPreference);
        //if(mStrPlatform.contains("3328"))
          //  mPreferenceScreen.removePreference(mColorPreference);
    }

    protected void rebuildView() {
        if (mDisplayInfo == null)
            return;
        mResolutionPreference.setEntries(mDisplayInfo.getModes());
        mResolutionPreference.setEntryValues(mDisplayInfo.getModes());
        mColorPreference.setEntries(mDisplayInfo.getColors());
        mColorPreference.setEntryValues(mDisplayInfo.getColors());
        mTextTitle.setText(mDisplayInfo.getDescription());
    }


    protected void initEvent() {
        mResolutionPreference.setOnPreferenceChangeListener(this);
        mColorPreference.setOnPreferenceChangeListener(this);
        mZoomPreference.setOnPreferenceClickListener(this);
        mAdvancedSettingsPreference.setOnPreferenceClickListener(this);
    }
    public void updateColorValue() {
        if (mDisplayInfo == null)
            return;
        String curColorMode = DrmDisplaySetting.getColorMode();
        Log.i(TAG, "curColorMode:" + curColorMode);
        if (curColorMode != null)
            mColorPreference. setValue(curColorMode);
            List<String> colors = DrmDisplaySetting.getColorModeList();
            Log.i(TAG, "setValueIndex colors.toString()= " + colors.toString());
            int index = colors.indexOf(curColorMode);
            if (index < 0) {
                Log.w(TAG, "DeviceFragment - updateColorValue - warning index(" + index + ") < 0, set index = 0");
                index = 0;
            }
            Log.i(TAG, "updateColorValue setValueIndex index= " + index);
            mColorPreference.setValueIndex(index);

        }

    /**
     * 还原分辨率值
     */
    public void updateResolutionValue() {
        if (mDisplayInfo == null)
            return;
        String resolutionValue = null;

            resolutionValue = DrmDisplaySetting.getCurDisplayMode(mDisplayInfo);
            /*防止读值不同步导致的UI值与实际设置的值不相符
            1.如DrmDisplaySetting.curSetHdmiMode 已赋值，且tmpSetHdmiMode为空，则从DrmDisplaySetting.curSetHdmiMode取上一次设置的值
            2.如DrmDisplaySetting.curSetHdmiMode 未赋值，且tmpSetHdmiMode为空，则getCurDisplayMode直接获取
            */
            if(DrmDisplaySetting.curSetHdmiMode !=null && DrmDisplaySetting.tmpSetHdmiMode == null)
                resolutionValue = DrmDisplaySetting.curSetHdmiMode;
            Log.i(TAG, "drm resolutionValue:" + resolutionValue);

            if (resolutionValue != null)
                mResolutionPreference.setValue(resolutionValue);
            /*show mResolutionPreference current item*/
            List<String> modes = DrmDisplaySetting.getDisplayModes(mDisplayInfo);
            Log.i(TAG, "setValueIndex modes.toString()= " + modes.toString());
            int index = modes.indexOf(resolutionValue);
            if (index < 0) {
                Log.w(TAG, "DeviceFragment - updateResolutionValue - warning index(" + index + ") < 0, set index = 0");
                index = 0;
            }
            Log.i(TAG, "mResolutionPreference setValueIndex index= " + index);
            mResolutionPreference.setValueIndex(index);

    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object obj) {
        Log.i(TAG, "onPreferenceChange:" + obj);
        if (preference == mResolutionPreference) {
            if (!mIsUseDisplayd) {
                int index = mResolutionPreference.findIndexOfValue((String) obj);
                DrmDisplaySetting.setDisplayModeTemp(mDisplayInfo, index);
                showConfirmSetModeDialog();
            }
        } else if (preference == mColorPreference) {
                DrmDisplaySetting.setColorMode(mDisplayInfo.getDisplayId(), mDisplayInfo.getType(), (String) obj);
        }
        return true;
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (preference == mZoomPreference) {
            Intent screenScaleIntent = new Intent(getActivity(), ScreenScaleActivity.class);
            screenScaleIntent.putExtra(ConstData.IntentKey.PLATFORM, mStrPlatform);
            screenScaleIntent.putExtra(ConstData.IntentKey.DISPLAY_INFO, mDisplayInfo);
            startActivity(screenScaleIntent);
        } else if (preference == mResolutionPreference) {
            //updateResolutionValue();
        } else if (preference == mAdvancedSettingsPreference) {
            Intent advancedIntent = new Intent(getActivity(), AdvancedDisplaySettingsActivity.class);
            advancedIntent.putExtra(ConstData.IntentKey.DISPLAY_ID, mDisplayInfo.getDisplayId());
            startActivity(advancedIntent);
        }
        return true;
    }


    @SuppressLint("NewApi")
    protected void showConfirmSetModeDialog() {
        DialogFragment df = ConfirmSetModeDialogFragment.newInstance(mDisplayInfo, new ConfirmSetModeDialogFragment.OnDialogDismissListener() {
            @Override
            public void onDismiss(boolean isok) {
                Log.i(TAG, "showConfirmSetModeDialog->onDismiss->isok:" + isok);
                    updateResolutionValue();
            }
        });
        df.show(getFragmentManager(), "ConfirmDialog");
    }

    protected DisplayInfo getDisplayInfo() {
        return null;
    }

}
