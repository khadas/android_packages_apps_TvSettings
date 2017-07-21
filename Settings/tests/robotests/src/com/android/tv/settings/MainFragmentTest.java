/*
 * Copyright (C) 2017 The Android Open Source Project
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

package com.android.tv.settings;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import android.provider.Settings;
import android.support.v7.preference.Preference;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = TestConfig.MANIFEST_PATH, sdk = TestConfig.SDK_VERSION)
public class MainFragmentTest {

    @Spy
    private MainFragment mMainFragment;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        doReturn(RuntimeEnvironment.application).when(mMainFragment).getContext();
    }

    @Test
    public void testUpdateDeveloperOptions_developerDisabled() {
        Settings.Global.putInt(RuntimeEnvironment.application.getContentResolver(),
                Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0);
        final Preference developerPref = spy(Preference.class);
        doReturn(developerPref).when(mMainFragment).findPreference(MainFragment.KEY_DEVELOPER);
        mMainFragment.updateDeveloperOptions();
        verify(developerPref, atLeastOnce()).setVisible(false);
        verify(developerPref, never()).setVisible(true);
    }

    @Test
    public void testUpdateDeveloperOptions_developerEnabled() {
        Settings.Global.putInt(RuntimeEnvironment.application.getContentResolver(),
                Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 1);
        final Preference developerPref = spy(Preference.class);
        doReturn(developerPref).when(mMainFragment).findPreference(MainFragment.KEY_DEVELOPER);
        mMainFragment.updateDeveloperOptions();
        verify(developerPref, atLeastOnce()).setVisible(true);
        verify(developerPref, never()).setVisible(false);
    }
}
