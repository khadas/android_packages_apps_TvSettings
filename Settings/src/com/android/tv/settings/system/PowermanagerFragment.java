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

package com.android.tv.settings.system;

import android.content.Context;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.annotation.Keep;
import android.support.annotation.NonNull;
import android.support.v17.leanback.app.GuidedStepFragment;
import android.support.v17.leanback.widget.GuidanceStylist;
import android.support.v17.leanback.widget.GuidedAction;
import android.view.View;
import android.os.SystemClock;
import com.android.tv.settings.R;

import java.util.List;

@Keep
public class PowermanagerFragment extends GuidedStepFragment{
    private static final String TAG = "PowerKeyDefinition";

    private static final String POWER_KEY_DEFINITION = "power_key_definition";
    private static final String POWER_KEY_SHUTDOWN = "power_key_shutdown";
    private static final String POWER_KEY_SUSPEND = "power_key_suspend";
    private static final String POWER_KEY_RESTART = "power_key_restart";
    public static PowermanagerFragment newInstance() {
        PowermanagerFragment fragment = new PowermanagerFragment();
        return fragment;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setSelectedActionPosition(1);
    }
    @Override
    public @NonNull
    GuidanceStylist.Guidance onCreateGuidance(Bundle savedInstanceState) {
        return new GuidanceStylist.Guidance(
                    getString(R.string.power_key_action),
                    null,
                    null,
                    getActivity().getDrawable(R.drawable.ic_settings_power));
    }
    @Override
    public void onGuidedActionClicked(GuidedAction action) {
        PowerManager pm = (PowerManager)getActivity().getSystemService(Context.POWER_SERVICE);
        if (action.getId() == R.string.power_action_suspend) {
            pm.goToSleep(SystemClock.uptimeMillis());
        }else if (action.getId() == R.string.power_action_shutdown) {
            pm.shutdown(false,"userrequested",false);
        }else if (action.getId() == R.string.power_action_restart) {
            pm.reboot("");
        }
    }

    @Override
    public void onCreateActions(@NonNull List<GuidedAction> actions,
            Bundle savedInstanceState) {
        final Context context = getActivity();
        actions.add(new GuidedAction.Builder(context)
            .id(R.string.power_action_suspend)
            .title(R.string.power_action_suspend)
            .build());
        actions.add(new GuidedAction.Builder(context)
            .id(R.string.power_action_shutdown)
            .title(R.string.power_action_shutdown)
            .build());
        actions.add(new GuidedAction.Builder(context)
            .id(R.string.power_action_restart)
            .title(R.string.power_action_restart)
            .build());
    }
}
