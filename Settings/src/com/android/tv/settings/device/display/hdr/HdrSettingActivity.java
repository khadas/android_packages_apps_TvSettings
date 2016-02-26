package com.android.tv.settings.device.display.hdr;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import com.android.tv.settings.dialog.old.Action;
import com.android.tv.settings.dialog.old.ActionAdapter;
import com.android.tv.settings.dialog.old.ActionFragment;
import com.android.tv.settings.dialog.old.ContentFragment;
import com.android.tv.settings.dialog.old.DialogActivity;
import com.android.tv.settings.BrowseInfo;
import com.android.tv.settings.R;

import com.droidlogic.app.HdrManager;

import java.util.ArrayList;


public class HdrSettingActivity extends DialogActivity implements ActionAdapter.Listener {
    private static final String ACTION_AUTO = "auto";
    private static final String ACTION_ON = "on";
    private static final String ACTION_OFF = "off";

    private ContentFragment mContentFragment;
    private ActionFragment mActionFragment;
    private HdrManager mHdrManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHdrManager = new HdrManager(this);

        mContentFragment = createMainMenuContentFragment();
        mActionFragment = ActionFragment.newInstance(getActions());
        setContentAndActionFragments(mContentFragment, mActionFragment);
    }

    private ContentFragment createMainMenuContentFragment() {
        return ContentFragment.newInstance(
                getString(R.string.device_hdr),
                getString(R.string.device_display),
                null,
                getIconResource(getContentResolver()),
                getResources().getColor(R.color.icon_background));
    }

    private ArrayList<Action> getActions() {
        int mode = mHdrManager.getHdrMode();
        ArrayList<Action> actions = new ArrayList<Action>();
        actions.add(new Action.Builder()
                .key(ACTION_AUTO)
                .title(getString(R.string.device_sound_dts_trans_auto))
                .checked(mode == HdrManager.MODE_AUTO)
                .build());
        actions.add(new Action.Builder()
                .key(ACTION_ON)
                .title(getString(R.string.on))
                .checked(mode == HdrManager.MODE_ON)
                .build());
        actions.add(new Action.Builder()
                .key(ACTION_OFF)
                .title(getString(R.string.off))
                .checked(mode == HdrManager.MODE_OFF)
                .build());
        return actions;
    }

    @Override
    public void onActionClicked(Action action) {
        if (ACTION_AUTO.equals(action.getKey())) {
            mHdrManager.setHdrMode(HdrManager.MODE_AUTO);
        } else if (ACTION_ON.equals(action.getKey())) {
            mHdrManager.setHdrMode(HdrManager.MODE_ON);
        } else if (ACTION_OFF.equals(action.getKey())) {
            mHdrManager.setHdrMode(HdrManager.MODE_OFF);
        }
        updateMainScreen();
    }

    private void updateMainScreen() {
        ((ActionAdapter) mActionFragment.getAdapter()).setActions(getActions());
    }

    public int getIconResource(ContentResolver contentResolver) {
        return R.drawable.ic_settings_hdr;
    }
}

