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

package com.android.tv.settings.device.font;

import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceViewHolder;
import android.support.v7.preference.TwoStatePreference;
import com.android.tv.settings.RadioPreference;
import android.text.TextUtils;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.tv.settings.PreferenceControllerFragment;
import com.android.tv.settings.SettingsPreferenceFragment;
import com.android.tv.settings.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import android.util.Log;

/**
 * The "Sound" screen in TV Settings.
 */
public class FontFragment extends SettingsPreferenceFragment {

    static final String KEY_FONT_SIZE_CATEGORY = "font_size_category";

    private PreferenceCategory mFontCategoryPref;

    private Context mContext;
    private ContentResolver mResolver;
    private static final float[] fontValue = {
       0.85f,
       1.0f,
       1.15f,
       1.30f
    };

    private static final String[] fontPrefix = {
       "font_size_0",
       "font_size_1",
       "font_size_2",
       "font_size_3",
    };

    public static FontFragment newInstance() {
        return new FontFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        mResolver = context.getContentResolver();
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.fontsize, null);


        mFontCategoryPref =
                (PreferenceCategory) findPreference(KEY_FONT_SIZE_CATEGORY);
        createFormatPreferences();
    }

    /** Creates and adds switches for each surround sound format. */
    private void createFormatPreferences() {
        int index = fontSizeValueToIndex(getFontSize(), mContext.getResources().getStringArray(R.array.entryvalues_font_size));
        String[] fontStr = mContext.getResources().getStringArray(R.array.entries_font_size);
        for (int i = 0; i < fontStr.length; i++) {
            RadioPreference pref = new RadioPreference(mContext);
            pref.setPersistent(false);
            pref.setTitle(fontStr[i]);
            pref.setKey(fontPrefix[i]);
            pref.setRadioGroup(KEY_FONT_SIZE_CATEGORY);
            pref.setLayoutResource(R.layout.preference_reversed_widget);
            pref.setChecked(index == i ? true:false);
            pref.setEnabled(true);
            mFontCategoryPref.addPreference(pref);
        }
    }

    private float getFontSize() {
        return Settings.System.getFloat(mResolver, Settings.System.FONT_SCALE , 1.0f);
    }

    private void setFontSize(float val) {
        Settings.System.putFloat(mResolver, Settings.System.FONT_SCALE, val);
    }

    private int fontSizeValueToIndex(float val, String[] indices) {
        float lastVal = Float.parseFloat(indices[0]);
        for (int i=1; i<indices.length; i++) {
            float thisVal = Float.parseFloat(indices[i]);
            if (val < (lastVal + (thisVal-lastVal)*.5f)) {
                return i-1;
            }
            lastVal = thisVal;
        }
        return indices.length-1;
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
       if (preference instanceof RadioPreference) {
            final RadioPreference pref = (RadioPreference) preference;
            pref.clearOtherRadioPreferences(mFontCategoryPref);
            String key = pref.getKey();
            if (pref.isChecked()) {
               if (TextUtils.equals(key, fontPrefix[0])) {
                  setFontSize(fontValue[0]);
               } else if (TextUtils.equals(key, fontPrefix[1])) {
                  setFontSize(fontValue[1]);
               } else if (TextUtils.equals(key, fontPrefix[2])) {
                  setFontSize(fontValue[2]);
               } else if (TextUtils.equals(key, fontPrefix[3])) {
                  setFontSize(fontValue[3]);
               }
            }
            pref.setChecked(true);
        }
        return super.onPreferenceTreeClick(preference);
    }
    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.ACCESSIBILITY_FONT_SIZE;
    }

}
