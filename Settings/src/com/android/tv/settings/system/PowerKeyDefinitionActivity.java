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
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;

public class PowerKeyDefinitionActivity extends DialogActivity implements ActionAdapter.Listener {
    private static final String TAG = "PowerKeyDefinition";

    private static final String POWER_KEY_DEFINITION = "power_key_definition";
    private static final String POWER_KEY_SHUTDOWN = "power_key_shutdown";
    private static final String POWER_KEY_SUSPEND = "power_key_suspend";

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

        if (mode.equals(POWER_KEY_SHUTDOWN)) {
            setShutdownDefinition(true);
        }else if (mode.equals(POWER_KEY_SUSPEND)) {
            setShutdownDefinition(false);
        }
        updateMainScreen();
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
                getString(R.string.power_key_definition), getString(R.string.power_key_definition),
                null, R.drawable.ic_settings_power,
                getResources().getColor(R.color.icon_background));
    }

    private ArrayList<Action> getMainActions() {
        ArrayList<Action> actions = new ArrayList<Action>();

        actions.add(new Action.Builder().key(POWER_KEY_SUSPEND)
            .title(getString(R.string.power_key_suspend))
            .checked(!isShutdownDefinition()).build());

        actions.add(new Action.Builder().key(POWER_KEY_SHUTDOWN)
            .title(getString(R.string.power_key_shutdown))
            .checked(isShutdownDefinition()).build());

        return actions;
    }


    private boolean isShutdownDefinition() {
        int default_value = 0;
        if (SettingsConstant.needDroidlogicTvFeature(this)) {
            default_value = 1;
        }
        return Settings.System.getInt(getContentResolver(), POWER_KEY_DEFINITION, default_value) == 1 ? true : false;
    }

    private void setShutdownDefinition (boolean isShutdown) {
        if (isShutdown) {
            Settings.System.putInt(getContentResolver(), POWER_KEY_DEFINITION, 1);
        } else {
            Settings.System.putInt(getContentResolver(), POWER_KEY_DEFINITION, 0);
        }
    }
}

