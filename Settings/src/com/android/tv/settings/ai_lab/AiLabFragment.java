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

package com.android.tv.settings.ai_lab;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.SystemProperties;

import android.provider.Settings;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreference;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.android.tv.settings.R;
import com.android.tv.settings.SettingsPreferenceFragment;
import com.android.tv.settings.dialog.AIIndepSwitchDialog;
import com.android.tv.settings.dialog.AIIndepSwitchDialog.AlgoType;

/**
 * The AI settings screen in TV settings.
 */
public class AiLabFragment extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String TAG = "AiLabFragment";

    //Vision PQ
    private static final String KEY_AI_LAB_SR_MODE = "ai_lab_sr_mode";
    private static final String KEY_AI_LAB_SD_MODE = "ai_lab_sd_mode";
    private static final String KEY_AI_LAB_MEMC_MODE = "ai_lab_memc_mode";
    private static final String KEY_AI_LAB_SLIDER_MODE = "ai_lab_slider_mode";

    //AI Audio
    private static final String KEY_AI_LAB_VOCAL = "ai_lab_vocal";
    private static final String KEY_AI_LAB_KALAOK = "ai_lab_kalaok";

    private static final String KEY_AI_LAB_ASR = "ai_lab_asr";
    private static final String KEY_AI_LAB_LLM = "ai_lab_llm";

    private static final String Prop_ASR_ENABLE = "sys.audio.asr";

    private static final String Prop_KALAOK_ENABLE = "vendor.kalaok.enable";
    private static final String Prop_KALAOK_AGC = "vendor.kalaok.agc.enable";
    private static final String Prop_VOCAL_ENABLE = "system.audio.vocal.mode";
    private static final String Prop_OTHER_LEVEL = "persist.sys.seperate.other_level";
    private static final String Prop_VOCAL_LEVEL = "persist.sys.seperate.vocal_level";
    private static final String Prop_SCULPTOR_ENABLE = "persist.vendor.sculptor.mode";
    private static final String Prop_SCULPTOR_C2_ENABLE = "persist.vendor.sculptor.c2.mode";
    private static final String Prop_SD_ENABLE = "persist.vendor.rkpq.hwpq_aisd_enable";
    private static final String Prop_HWPQ_SD_ENABLE = "persist.vendor.rkhwpq.aisd_enable";
    private static final String Prop_MEMC_ENABLE = "persist.vendor.rkpq.memc.enable";
    private static final String Prop_MEMC_SRSTRENGH = "persist.vendor.rkpq.memc.strength";
    private static final String Prop_SR_ENABLE = "persist.vendor.rkpq.sr.enable";
    private static final String Prop_SR_SRSTRENGH = "persist.vendor.rkpq.sr.strength";
    private static final String Prop_SLIDER_ENABLE = "persist.vendor.sculptor.slider.enable";
    private static final String Prop_SLIDER_DYNAMIC = "persist.vendor.sculptor.slider.dynamic";

    private static final String Prop_WATERMARK_ENABLE = "persist.vendor.rkpq.memc.watermark";

    private ListPreference mSrPreference;
    private ListPreference mMemcPreference;
    private ListPreference mSliderPreference;
    private ListPreference mVocalPreference;
    private ListPreference mKalaokPreference;
    private SwitchPreference mAsrPreference;
    private SwitchPreference mSdPreference;
    private SwitchPreference mLlmPreference;


    public static AiLabFragment newInstance() {
        return new AiLabFragment();
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.ai_lab_settings_prefs, null);
    }

    // When selecting the location preference, LeanbackPreferenceFragment
    // creates an inner view with the selection options; that's when we want to
    // register our receiver, bacause from now on user can change the location
    // providers.
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //getActivity().registerReceiver(mReceiver, new IntentFilter(LocationManager.MODE_CHANGED_ACTION));
    }

    @Override
    public void onResume() {
        super.onResume();
        mSrPreference = (ListPreference) findPreference(KEY_AI_LAB_SR_MODE);
        mSdPreference = (SwitchPreference) findPreference(KEY_AI_LAB_SD_MODE);
        mMemcPreference = (ListPreference) findPreference(KEY_AI_LAB_MEMC_MODE);
        mSliderPreference = (ListPreference) findPreference(KEY_AI_LAB_SLIDER_MODE);
        updateVisionPqStatus();
        mSrPreference.setOnPreferenceChangeListener(this);
        mMemcPreference.setOnPreferenceChangeListener(this);
        mSliderPreference.setOnPreferenceChangeListener(this);

        mVocalPreference = (ListPreference) findPreference(KEY_AI_LAB_VOCAL);
        updateVocalStatus();
        mVocalPreference.setOnPreferenceChangeListener(this);

        mKalaokPreference = (ListPreference) findPreference(KEY_AI_LAB_KALAOK);
        updateKalaokStatus();
        mKalaokPreference.setOnPreferenceChangeListener(this);

        mAsrPreference = (SwitchPreference) findPreference(KEY_AI_LAB_ASR);
        mAsrPreference.setChecked(getASRStatus());

        mLlmPreference = (SwitchPreference) findPreference(KEY_AI_LAB_LLM);
        mLlmPreference.setChecked(false);
        mLlmPreference.setEnabled(false);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //getActivity().unregisterReceiver(mReceiver);
    }

    private void updateVisionPqStatus() {
        String sr_strength = SystemProperties.get(Prop_SR_SRSTRENGH, "100");
        if (getSrModeStatus()) {
            switch (sr_strength) {
                case "50":
                    mSrPreference.setValue("1");
                    break;
                case "75":
                    mSrPreference.setValue("2");
                    break;
                default:
                    mSrPreference.setValue("3");
                    break;
            }
        } else {
            mSrPreference.setValue("0");
        }

        String memc_strength = SystemProperties.get(Prop_MEMC_SRSTRENGH, "100");
        if (getMemcStatus()) {
            switch (memc_strength) {
                case "50":
                    mMemcPreference.setValue("1");
                    break;
                case "75":
                    mMemcPreference.setValue("2");
                    break;
                default:
                    mMemcPreference.setValue("3");
                    break;
            }
        } else {
            mMemcPreference.setValue("0");
        }

        if (getSilderModeStatus()) {
            if ("1".equals(SystemProperties.get(Prop_SLIDER_DYNAMIC, "0")))
                mSliderPreference.setValue("2");
            else
                mSliderPreference.setValue("1");
        } else {
            mSliderPreference.setValue("0");
        }

        mSdPreference.setChecked(getSdModeStatus());
    }

    private boolean getSrModeStatus() {
        return "-1".equals(SystemProperties.get(Prop_SR_ENABLE, "0")) && "1".equals(SystemProperties.get(Prop_SCULPTOR_ENABLE, "0"));
    }

    private void setSrModeStatus(boolean isEnable, String level) {
        if (isEnable) {
            updateVisionPq(true);
        }
        SystemProperties.set(Prop_SR_ENABLE, String.valueOf(isEnable ? -1 : 0));
        SystemProperties.set("persist.vendor.rkpq.dc.enable", String.valueOf(isEnable ? 1 : 0));
        SystemProperties.set("persist.vendor.rkpq.hwpq_shp_en", String.valueOf(isEnable ? 1 : 0));
        SystemProperties.set("persist.vendor.rkpq.hwpq_lce_ratio", String.valueOf(isEnable ? 10 : 0));
        SystemProperties.set("vendor.tvinput.rkpq.vdpp_shp_en", String.valueOf(isEnable ? 1 : 0));
        SystemProperties.set("vendor.tvinput.rkpq.update_vdpp_cfg", String.valueOf(1));
        //if(!isEnable) SystemProperties.set("persist.vendor.rkpq.fe.enable", String.valueOf(0));

        if (!"-1".equals(level))
            SystemProperties.set(Prop_SR_SRSTRENGH, level);//0~100 50-75-100
    }

    private boolean getSdModeStatus() {
        return "1".equals(SystemProperties.get(Prop_SD_ENABLE, "0")) && "1".equals(SystemProperties.get(Prop_SCULPTOR_ENABLE, "0"));
    }

    private void setSdModeStatus(boolean isEnable) {
        if (isEnable) {
            updateVisionPq(true);
        }
        SystemProperties.set(Prop_SD_ENABLE, String.valueOf(isEnable ? 1 : 0));
        SystemProperties.set(Prop_HWPQ_SD_ENABLE, String.valueOf(isEnable ? 1 : 0));
    }

    private boolean getMemcStatus() {
        return "-1".equals(SystemProperties.get(Prop_MEMC_ENABLE, "0")) && "1".equals(SystemProperties.get(Prop_SCULPTOR_ENABLE, "0"));
    }

    private void setMemcStatus(boolean isEnable, String level) {
        if (isEnable) {
            updateVisionPq(true);
        }
        SystemProperties.set(Prop_MEMC_ENABLE, String.valueOf(isEnable ? -1 : 0));
        if (!"-1".equals(level))
            SystemProperties.set(Prop_MEMC_SRSTRENGH, level);//0~100 50-75-100
    }

    private boolean getSilderModeStatus() {
        return "1".equals(SystemProperties.get(Prop_SLIDER_ENABLE, "0")) && "1".equals(SystemProperties.get(Prop_SCULPTOR_ENABLE, "0"));
    }

    private void setSilderModeStatus(boolean isEnable, boolean dynamic) {
        if (isEnable) {
            updateVisionPq(true);
        }
        SystemProperties.set(Prop_SLIDER_ENABLE, String.valueOf(isEnable ? 1 : 0));
        if (isEnable)
            SystemProperties.set(Prop_SLIDER_DYNAMIC, String.valueOf(dynamic ? 1 : 0));
    }

    private void updateVisionPq(boolean isEnable) {
        if (isEnable) {
	    setKalaokStatus("false");
            setASRStatus("false");
            setVocalStatus("false");
            if("0".equals(SystemProperties.get(Prop_SCULPTOR_ENABLE, "0")))
               Toast.makeText(getActivity(), this.getString(R.string.ai_lab_switch_visionpq,"Vision PQ"), Toast.LENGTH_SHORT).show();
	    SystemProperties.set(Prop_WATERMARK_ENABLE,"1");
        } else {
            //disable visionpq config
            setSrModeStatus(false, "-1");
            setSdModeStatus(false);
            setMemcStatus(false, "-1");
            setSilderModeStatus(false, false);
            Toast.makeText(getActivity(), this.getString(R.string.ai_lab_close_visionpq,"Vision PQ"), Toast.LENGTH_SHORT).show();
	    SystemProperties.set(Prop_WATERMARK_ENABLE,"0");
        }
        SystemProperties.set(Prop_SCULPTOR_ENABLE, isEnable ? "1" : "0");
        SystemProperties.set(Prop_SCULPTOR_C2_ENABLE, isEnable ? "1" : "0");
        //updateVisionPqStatus();

    }

    private boolean getASRStatus() {
        return SystemProperties.getBoolean(Prop_ASR_ENABLE, false);
    }

    private void setASRStatus(String state) {
        boolean isEnable = "true".equals(state);
        SystemProperties.set(Prop_ASR_ENABLE, state);
        if (isEnable)
            updateVisionPq(false);
        mAsrPreference.setChecked(getASRStatus());
    }

    private void updateKalaokStatus() {
        if (getKalaokStatus()) {
            if ("0".equals(SystemProperties.get(Prop_KALAOK_AGC, "0")))
                mKalaokPreference.setValue("2");
            else
                mKalaokPreference.setValue("1");
        } else {
            mKalaokPreference.setValue("0");
        }
    }

    private boolean getKalaokStatus() {
        return SystemProperties.getBoolean(Prop_KALAOK_ENABLE, false);
    }

    private void setKalaokStatus(String state) {
        boolean isEnable = "true".equals(state);
        SystemProperties.set(Prop_KALAOK_ENABLE, state);
        if (isEnable)
            updateVisionPq(false);
    }

    private void setKalaokAGC(boolean on) {
        if(on)
           SystemProperties.set(Prop_KALAOK_AGC, "1");
        else
           SystemProperties.set(Prop_KALAOK_AGC, "0");
    }

    private void updateVocalStatus() {
        if (getVocalStatus()) {
            if ("0".equals(SystemProperties.get(Prop_VOCAL_LEVEL, "0")))
                mVocalPreference.setValue("2");
            else
                mVocalPreference.setValue("1");
        } else {
            mVocalPreference.setValue("0");
        }
    }

    private boolean getVocalStatus() {
        return SystemProperties.getBoolean(Prop_VOCAL_ENABLE, false);
    }

    private void setVocalStatus(String state) {
        boolean isEnable = "1".equals(state);
        SystemProperties.set(Prop_VOCAL_ENABLE, state);
        if (isEnable)
            updateVisionPq(false);
    }

    private void setVocalLevel(int level) {
        SystemProperties.set(Prop_VOCAL_LEVEL, level + "");
        SystemProperties.set(Prop_OTHER_LEVEL, (100 - level) + "");
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (TextUtils.equals(preference.getKey(), KEY_AI_LAB_ASR)) {
            setASRStatus(((SwitchPreference) preference).isChecked() ? "true" : "false");
            updateVisionPqStatus();
        }
        if (TextUtils.equals(preference.getKey(), KEY_AI_LAB_SD_MODE)) {
            setSdModeStatus(((SwitchPreference) preference).isChecked() ? true : false);
            updateVocalStatus();
            updateVisionPqStatus();
        }
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {

        if (TextUtils.equals(preference.getKey(), KEY_AI_LAB_SR_MODE)) {
            switch ((String) newValue) {
                case "1":
                    setSrModeStatus(true, "50");
                    break;
                case "2":
                    setSrModeStatus(true, "75");
                    break;
                case "3":
                    setSrModeStatus(true, "100");
                    break;
                default:
                    setSrModeStatus(false, "-1");
                    break;
            }
        } else if (TextUtils.equals(preference.getKey(), KEY_AI_LAB_MEMC_MODE)) {
            switch ((String) newValue) {
                case "1":
                    setMemcStatus(true, "50");
                    break;
                case "2":
                    setMemcStatus(true, "75");
                    break;
                case "3":
                    setMemcStatus(true, "100");
                    break;
                default:
                    setMemcStatus(false, "-1");
                    break;
            }
        } else if (TextUtils.equals(preference.getKey(), KEY_AI_LAB_SLIDER_MODE)) {
            switch ((String) newValue) {
                case "1":
                    setSilderModeStatus(true, false);
                    break;
                case "2":
                    setSilderModeStatus(true, true);
                    break;
                default:
                    setSilderModeStatus(false, false);
                    break;
            }
        } else if (TextUtils.equals(preference.getKey(), KEY_AI_LAB_VOCAL)) {
            switch ((String) newValue) {
                case "1":
                    setVocalStatus("1");
                    setVocalLevel(100);
                    break;
                case "2":
                    setVocalStatus("1");
                    setVocalLevel(0);
                    break;
                default:
                    setVocalStatus("0");
                    break;
            }
        }else if (TextUtils.equals(preference.getKey(), KEY_AI_LAB_KALAOK)) {
            switch ((String) newValue) {
                case "1":
                    setKalaokStatus("1");
                    setKalaokAGC(true);
                    break;
                case "2":
                    setKalaokStatus("1");
                    setKalaokAGC(false);
                    break;
                default:
                    setKalaokStatus("0");
                    break;
            }
        }
        updateKalaokStatus();
        updateVocalStatus();
        updateVisionPqStatus();
        return true;
    }

    private AlgoType[] mAlgoTypes = {AlgoType.NONE, AlgoType.SR, AlgoType.SD, AlgoType.MEMC, AlgoType.SLIDER,
            AlgoType.NONE, AlgoType.VOCAL, AlgoType.KALAOK, AlgoType.ASR, AlgoType.LLM};

    public boolean onKey(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
            if (getTitle() == null) return false;

            RecyclerView recyclerView = getListView();
            View focusedChild = recyclerView.getFocusedChild();
            if (focusedChild != null) {
                RecyclerView.ViewHolder viewHolder = recyclerView.getChildViewHolder(focusedChild);
                int position = viewHolder.getAdapterPosition();
                if (position >= 0 && position < mAlgoTypes.length) {
                    AlgoType type = mAlgoTypes[position];
                    if (type != AlgoType.NONE) {
                        Dialog dialog = new AIIndepSwitchDialog(requireActivity().getApplication(), R.style.transparent_dialog,
                                type);
                        dialog.show();

                        requireActivity().finishAffinity();
                    }
                }
            }

            return true;
        }
        return false;
    }

    public String getTitle() {
        final View view = getView();
        final TextView decorTitle = view == null
                ? null : (TextView) view.findViewById(R.id.decor_title);
        if (decorTitle != null) {
            return decorTitle.getText().toString();
        } else {
            return null;
        }
    }
}
