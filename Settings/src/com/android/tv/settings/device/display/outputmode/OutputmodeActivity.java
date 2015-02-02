package com.android.tv.settings.device.display.outputmode;


import com.android.tv.settings.R;
import com.android.tv.settings.dialog.old.Action;
import com.android.tv.settings.dialog.old.ActionAdapter;
import com.android.tv.settings.dialog.old.ActionFragment;
import com.android.tv.settings.dialog.old.ContentFragment;
import com.android.tv.settings.dialog.old.DialogActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;


public class OutputmodeActivity extends DialogActivity implements ActionAdapter.Listener {
    private final static String BEST_RESOLUTION = "best resolution";
    private ContentFragment mContentFragment;
    private ActionFragment mActionFragment;
    private OutputUiManager mOutputUiManager;
    private IntentFilter mIntentFilter;

    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mHandler.sendEmptyMessageDelayed(0, 1000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mOutputUiManager = new OutputUiManager(this);
        mContentFragment = createMainMenuContentFragment();
        mActionFragment = ActionFragment.newInstance(getMainActions());
        setContentAndActionFragments(mContentFragment, mActionFragment);

        mIntentFilter = new IntentFilter("android.intent.action.HDMI_PLUGGED");
        mIntentFilter.addAction(Intent.ACTION_TIME_TICK);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mIntentReceiver, mIntentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mIntentReceiver);
    }

    @Override
    public void onActionClicked(Action action) {
        String mode = action.getKey();

        if (mode.equals(BEST_RESOLUTION)) {
            mOutputUiManager.change2BestMode();
        }else {
            mOutputUiManager.change2NewMode(mode);
        }
        updateMainScreen();
    }

    private void goToMainScreen() {
        updateMainScreen();
        getFragmentManager().popBackStack(null, 0);
    }

    private void updateMainScreen() {
        mOutputUiManager.updateUiMode();
        ((ActionAdapter) mActionFragment.getAdapter()).setActions(getMainActions());
    }

    private ContentFragment createMainMenuContentFragment() {
        return ContentFragment.newInstance(
                getString(R.string.device_outputmode), getString(R.string.device_display),
                null, R.drawable.ic_settings_display,
                getResources().getColor(R.color.icon_background));
    }

    private ArrayList<Action> getMainActions() {
        ArrayList<Action> actions = new ArrayList<Action>();
        ArrayList<String> outputmodeTitleList = mOutputUiManager.getOutputmodeTitleList();
        ArrayList<String> outputmodeValueList = mOutputUiManager.getOutputmodeValueList();

        if (mOutputUiManager.getUiMode().equals(mOutputUiManager.HDMI_MODE)) {
            String best_resolution_description;
            if (mOutputUiManager.isBestOutputmode()) {
                best_resolution_description = getString(R.string.captions_display_on);
            } else{
                best_resolution_description = getString(R.string.captions_display_off);
            }
            actions.add(new Action.Builder().key(BEST_RESOLUTION)
                .title("        " + getString(R.string.device_outputmode_auto))
                .description("                " + best_resolution_description).build());
        }

        for (int i = 0; i < outputmodeTitleList.size(); i++) {
            if (i == mOutputUiManager.getCurrentModeIndex()) {
                actions.add(new Action.Builder().key(outputmodeValueList.get(i))
                    .title("        " + outputmodeTitleList.get(i))
                    .checked(true).build());
            }else {
                actions.add(new Action.Builder().key(outputmodeValueList.get(i))
                    .title("        " + outputmodeTitleList.get(i))
                    .description("").build());
            }
        }
        return actions;
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            updateMainScreen();
        }
    };
}
