/*
 * Copyright (C) 2020 The Android Open Source Project
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

package com.android.tv.settings.privacy;

import static com.android.tv.settings.overlay.FlavorUtils.FLAVOR_TWO_PANEL;
import static com.android.tv.settings.overlay.FlavorUtils.FLAVOR_VENDOR;
import static com.android.tv.settings.overlay.FlavorUtils.FLAVOR_X;
import static com.android.tv.settings.util.InstrumentationUtils.logEntrySelected;

import android.app.tvsettings.TvSettingsEnums;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Keep;
import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;

import com.android.tv.settings.R;
import com.android.tv.settings.SettingsPreferenceFragment;
import com.android.tv.settings.overlay.FlavorUtils;
import com.android.tv.settings.util.SliceUtils;
import com.android.tv.twopanelsettings.slices.CustomContentDescriptionPreference;
import com.android.tv.twopanelsettings.slices.SlicePreference;

/**
 * The Privacy policies screen in Settings.
 */
@Keep
public class PrivacyFragment extends SettingsPreferenceFragment {
    private static final String KEY_ACCOUNT_SETTINGS_CATEGORY = "accountSettings";
    private static final String KEY_USAGE = "usageAndDiag";
    private static final String KEY_ADS = "ads";
    private static final String KEY_ASSISTANT = "assistant";
    private static final String KEY_PURCHASES = "purchases";
    private static final String KEY_SECURITY = "security";
    private static final String KEY_PLAY_PROTECT = "play_protect";
    private static final String KEY_MIC = "microphone";
    private static final String KEY_CAMERA = "camera";

    private int getPreferenceScreenResId() {
        switch (FlavorUtils.getFlavor(getContext())) {
            case FLAVOR_TWO_PANEL:
            case FLAVOR_X:
            case FLAVOR_VENDOR:
                return R.xml.privacy_x;
            default:
                return R.xml.privacy;
        }
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        setPreferencesFromResource(getPreferenceScreenResId(), null);
        PreferenceCategory accountPrefCategory = findPreference(KEY_ACCOUNT_SETTINGS_CATEGORY);
        Preference assistantSlicePreference = findPreference(KEY_ASSISTANT);
        Preference purchasesSlicePreference = findPreference(KEY_PURCHASES);
        Preference adsPreference = findPreference(KEY_ADS);
        final Preference securityPreference = findPreference(KEY_SECURITY);
        final Preference playProtectPreference = findPreference(KEY_PLAY_PROTECT);

        PrivacyToggle.MIC_TOGGLE.preparePreferenceWithSensorFragment(getContext(),
                findPreference(KEY_MIC), SensorFragment.TOGGLE_EXTRA);
        PrivacyToggle.CAMERA_TOGGLE.preparePreferenceWithSensorFragment(getContext(),
                findPreference(KEY_CAMERA), SensorFragment.TOGGLE_EXTRA);

        if (FlavorUtils.getFeatureFactory(getContext()).getBasicModeFeatureProvider()
                .isBasicMode(getContext())) {
            accountPrefCategory.setVisible(false);
            assistantSlicePreference.setVisible(false);
            purchasesSlicePreference.setVisible(false);
            adsPreference.setVisible(false);
            // playProtectPreference can be present only in two panel settings
            if (playProtectPreference != null) {
                playProtectPreference.setVisible(false);
            }
            if (isPlayProtectPreferenceEnabled(playProtectPreference)) {
                // By default show securityPreference unless playProtectPreference is enabled
                securityPreference.setVisible(false);
            }
            return;
        }
        if (assistantSlicePreference instanceof SlicePreference
                && SliceUtils.isSliceProviderValid(
                        getContext(), ((SlicePreference) assistantSlicePreference).getUri())) {
            assistantSlicePreference.setVisible(true);
        }
        if (purchasesSlicePreference instanceof SlicePreference
                && SliceUtils.isSliceProviderValid(
                        getContext(), ((SlicePreference) purchasesSlicePreference).getUri())) {
            purchasesSlicePreference.setVisible(true);
        }
        accountPrefCategory.setVisible(
                assistantSlicePreference.isVisible() || purchasesSlicePreference.isVisible());
        findPreference(KEY_ADS).setOnPreferenceClickListener(preference -> {
            Intent intent = new Intent();
            intent.setAction("com.google.android.gms.settings.ADS_PRIVACY");
            startActivity(intent);
            return true;
        });
        if (adsPreference instanceof CustomContentDescriptionPreference) {
            ((CustomContentDescriptionPreference) adsPreference).setContentDescription(
                    getResources().getString(R.string.ads_content_description));
        }
        if (isPlayProtectPreferenceEnabled(playProtectPreference)) {
            showPlayProtectPreference(playProtectPreference, securityPreference);
        } else {
            showSecurityPreference(securityPreference, playProtectPreference);
        }
    }

    private boolean isPlayProtectPreferenceEnabled(@Nullable Preference playProtectPreference) {
        return playProtectPreference instanceof SlicePreference
                && SliceUtils.isSliceProviderValid(
                        getContext(), ((SlicePreference) playProtectPreference).getUri());
    }

    private void showPlayProtectPreference(
            @Nullable Preference playProtectPreference,
            Preference securityPreference) {
        if (playProtectPreference != null) {
            playProtectPreference.setVisible(true);
        }
        securityPreference.setVisible(false);
    }

    private void showSecurityPreference(
            Preference securityPreference,
            @Nullable Preference playProtectPreference) {
        securityPreference.setVisible(true);
        if (playProtectPreference != null) {
            playProtectPreference.setVisible(false);
        }
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        switch (preference.getKey()) {
            case KEY_USAGE:
                logEntrySelected(TvSettingsEnums.PRIVACY_DIAGNOSTICS);
                break;
            case KEY_ADS:
                logEntrySelected(TvSettingsEnums.PRIVACY_ADS);
                break;
        }
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    protected int getPageId() {
        return TvSettingsEnums.PRIVACY;
    }
}
