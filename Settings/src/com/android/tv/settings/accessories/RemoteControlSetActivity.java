package com.android.tv.settings.accessories;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.UserHandle ;
import android.provider.Settings.Global;
import android.util.Log;

import com.android.tv.settings.dialog.old.Action;
import com.android.tv.settings.dialog.old.ActionAdapter;
import com.android.tv.settings.dialog.old.ActionFragment;
import com.android.tv.settings.dialog.old.ContentFragment;
import com.android.tv.settings.dialog.old.DialogActivity;
import com.android.tv.settings.R;

import java.util.ArrayList;

/**
 * Activity for setting remote control.
 */
public class RemoteControlSetActivity extends DialogActivity implements ActionAdapter.Listener {
    private static final boolean DEBUG = false;
    private static final String TAG = "RemoteControlSetActivity";
    private final Context mContext = this;
    private Resources mResources;
    private ArrayList<Action> mActions;
    private Fragment mContentFragment;
    private ActionFragment mActionFragment;
    private static final int DISABLED = 0;
    private static final int ENABLED = 1;
    private static final String RC_STATE_KEY = "global_key_rc_state";
    private static final String KEY_RC_ON = "rc_on";
    private static final String KEY_RC_OFF = "rc_off";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mResources = getResources();
        init();
        setContentAndActionFragments(mContentFragment, mActionFragment);
    }

    @Override
    public void onActionClicked(Action action) {
        Intent i = new Intent();
        String key = action.getKey();
        if (key.equals(KEY_RC_ON) && readRCState() == 0) {
            i.setAction("com.amlogic.remoteControl.RC_START");
            mContext.sendBroadcastAsUser(i, UserHandle.ALL);
            writeRCState(1);
            if (DEBUG) {
                Log.i(TAG,"send broadcast start remote service");
            }
        }
        else if (key.equals(KEY_RC_OFF) && readRCState() == 1) {
            i.setAction("com.amlogic.remoteControl.RC_STOP");
            mContext.sendBroadcastAsUser(i, UserHandle.ALL );
            writeRCState(0);
            if (DEBUG) {
                Log.i(TAG,"send broadcast stop remote service");
            }
        }
        updateSelectedAction(key);
    }

    private void updateSelectedAction(String selectedKey) {
        if (selectedKey != null) {
            for (Action action : mActions) {
                if (action.getKey().equalsIgnoreCase(selectedKey)) {
                    action.setChecked(true);
                }
                else {
                    action.setChecked(false);
                }
            }

            // Update the main fragment.
            ActionAdapter adapter = (ActionAdapter) mActionFragment.getAdapter();
            if (adapter != null) {
                adapter.setActions(mActions);
            }
        }
    }

    private void makeContentFragment() {
        /*
        mContentFragment = ContentFragment.newInstance(
                mResources.getString(R.string.accessories_remote),
                null,
                null,
                R.drawable.ic_settings_remote,
                mResources.getColor(R.color.icon_background));
                */
    }

    private static int toInt(boolean enabled) {
        return enabled ? ENABLED : DISABLED;
    }

    private int readRCState() {//0:disabled 1:enabled
        return Global.getInt(mContext.getContentResolver(), RC_STATE_KEY, toInt(false));
    }

    private void writeRCState(int value) {
        Global.putInt(mContext.getContentResolver(), RC_STATE_KEY, value);
    }

    private void init() {
        mActions = new ArrayList<Action>();
        makeContentFragment();

        mActions.add(new Action.Builder()
                    .key(KEY_RC_ON)
                    .title(mResources.getString(R.string.on))
                    .checked(readRCState() != 0)
                    .build());
        mActions.add(new Action.Builder()
                    .key(KEY_RC_OFF)
                    .title(mResources.getString(R.string.off))
                    .checked(readRCState() == 0)
                    .build());
        mActionFragment = ActionFragment.newInstance(mActions);
    }
}
