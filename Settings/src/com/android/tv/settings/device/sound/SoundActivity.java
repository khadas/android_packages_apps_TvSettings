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

package com.android.tv.settings.device.sound;

import android.content.ContentResolver;
import android.graphics.drawable.Drawable;
import android.provider.Settings;
import com.android.tv.settings.BrowseInfoFactory;
import com.android.tv.settings.R;
import com.android.tv.settings.MenuActivity;
import com.android.tv.settings.device.sound.systemsound.SystemSoundActivity;
import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.provider.Settings;

import com.android.tv.settings.R;
import com.android.tv.settings.dialog.DialogFragment;
import com.android.tv.settings.dialog.DialogFragment.Action;

import java.util.ArrayList;

/**
 * Activity allowing the management of display settings.
 */
public class SoundActivity extends MenuActivity {

    @Override
    protected String getBrowseTitle() {
        return getString(R.string.device_sound_effects);
    }

    @Override
    protected Drawable getBadgeImage() {
        return getResources().getDrawable(R.drawable.ic_settings_sound_on);
    }

    @Override
    protected BrowseInfoFactory getBrowseInfoFactory() {
        SoundBrowseInfo soundBrowseInfo = new SoundBrowseInfo(this);
        soundBrowseInfo.init();
        return soundBrowseInfo;
    }

    public static String getPreferenceKey() {
        return SystemSoundActivity.getPreferenceKey();
    }

    public static int getIconResource(ContentResolver contentResolver) {
        return getSoundEffectsEnabled(contentResolver) ? R.drawable.settings_sound_on_icon
                : R.drawable.settings_sound_off_icon;
    }

    private void setSoundEffectsEnabled(int value) {
        Settings.System.putInt(getContentResolver(), Settings.System.SOUND_EFFECTS_ENABLED, value);
    }

    private static boolean getSoundEffectsEnabled(ContentResolver contentResolver) {
        return Settings.System.getInt(contentResolver, Settings.System.SOUND_EFFECTS_ENABLED, 1)
                != 0;
    }
}
