package com.android.tv.settings.system;

import com.android.tv.settings.R;
import com.android.tv.settings.dialog.old.Action;
import com.android.tv.settings.dialog.old.ActionAdapter;
import com.android.tv.settings.dialog.old.ActionFragment;
import com.android.tv.settings.dialog.old.ContentFragment;
import com.android.tv.settings.dialog.old.DialogActivity;
import com.android.tv.settings.SettingsConstant;

import android.content.Context;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;

public class PowerKeyActivity extends DialogActivity implements ActionAdapter.Listener {
    private static final String TAG = "PowerKeyDefinition";

    private static final String POWER_KEY_DEFINITION = "power_key_definition";
    private static final String POWER_KEY_SHUTDOWN = "power_key_shutdown";
    private static final String POWER_KEY_SUSPEND = "power_key_suspend";
    private static final String POWER_KEY_RESTART = "power_key_restart";
    private ContentFragment mContentFragment;
    private ActionFragment mActionFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContentFragment = createMainMenuContentFragment();
        mActionFragment = ActionFragment.newInstance(getMainActions());
        setContentAndActionFragments(mContentFragment, mActionFragment);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onActionClicked(Action action) {
        String mode = action.getKey();
        PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);

        if (mode.equals(POWER_KEY_SUSPEND)) {
            pm.goToSleep(20);
        }else if (mode.equals(POWER_KEY_SHUTDOWN)) {
            pm.shutdown(false,"userrequested",false);
        }else if (mode.equals(POWER_KEY_RESTART)) {
            pm.reboot("");
        }
    }

    private void goToMainScreen() {
        updateMainScreen();
        getFragmentManager().popBackStack(null, 0);
    }

    private void updateMainScreen() {
        ((ActionAdapter) mActionFragment.getAdapter()).setActions(getMainActions());
    }

    private ContentFragment createMainMenuContentFragment() {
        return ContentFragment.newInstance(
                getString(R.string.power_key_action), getString(R.string.power_key_action),
                null, R.drawable.ic_power_launch,
                getResources().getColor(R.color.icon_background));
    }

    private ArrayList<Action> getMainActions() {
        ArrayList<Action> actions = new ArrayList<Action>();

        actions.add(new Action.Builder().key(POWER_KEY_SUSPEND)
            .title(getString(R.string.power_action_suspend))
            .checked(false).build());

        actions.add(new Action.Builder().key(POWER_KEY_SHUTDOWN)
            .title(getString(R.string.power_action_shutdown))
            .checked(false).build());

        actions.add(new Action.Builder().key(POWER_KEY_RESTART)
            .title(getString(R.string.power_action_restart))
            .checked(false).build());

        return actions;
    }
}

