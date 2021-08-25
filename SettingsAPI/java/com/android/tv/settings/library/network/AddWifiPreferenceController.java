/*
 * Copyright (C) 2021 The Android Open Source Project
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

package com.android.tv.settings.library.network;

import android.content.Context;
import android.os.UserManager;

import com.android.tv.settings.library.UIUpdateCallback;
import com.android.tv.settings.library.util.RestrictedPreferenceController;

/** Preference controller for add wifi preference in NetworkState. */
public class AddWifiPreferenceController extends RestrictedPreferenceController {
    private static final String KEY_ADD = "wifi_add";

    public AddWifiPreferenceController(Context context,
            UIUpdateCallback callback, int stateIdentifier) {
        super(context, callback, stateIdentifier);
    }

    @Override
    public boolean useAdminDisabledSummary() {
        return false;
    }

    @Override
    public String getAttrUserRestriction() {
        return UserManager.DISALLOW_CONFIG_WIFI;
    }

    @Override
    public String[] getPreferenceKey() {
        return new String[]{KEY_ADD};
    }
}