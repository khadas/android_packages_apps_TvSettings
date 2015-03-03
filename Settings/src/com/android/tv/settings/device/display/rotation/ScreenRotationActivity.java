package com.android.tv.settings.device.display.rotation;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import com.droidlogic.app.SystemControlManager;
import com.android.tv.settings.dialog.DialogFragment;
import com.android.tv.settings.dialog.DialogFragment.Action;
import com.android.tv.settings.BrowseInfo;
import com.android.tv.settings.R;

import java.util.ArrayList;


public class ScreenRotationActivity extends Activity implements Action.Listener {
    private static final String ACTION_ROTATION_MIDDLE = "rotation_middle";
    private static final String ACTION_ROTATION_LAND = "rotation_land";
    private static final String ACTION_ROTATION_ORIGINAL = "rotation_original";

    private DialogFragment mDialogFragment;
    private SystemControlManager mSystemControlManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSystemControlManager = new SystemControlManager(this);

        mDialogFragment = new DialogFragment.Builder()
                .title(getString(R.string.device_rotation))
                .breadcrumb(getString(R.string.device_display))
                .description(getString(R.string.device_rotation_description))
                .iconResourceId(getIconResource(getContentResolver()))
                .iconBackgroundColor(getResources().getColor(R.color.icon_background))
                .actions(getActions()).build();
        DialogFragment.add(getFragmentManager(), mDialogFragment);
    }

    private ArrayList<Action> getActions() {
        String type = getRotationType();
        ArrayList<Action> actions = new ArrayList<Action>();
        actions.add(new Action.Builder()
                .key(ACTION_ROTATION_MIDDLE)
                .title(getString(R.string.device_rotation_middle))
                .checked(type.equals("middle_port"))
                .checkSetId(1)
                .build());
        actions.add(new Action.Builder()
                .key(ACTION_ROTATION_LAND)
                .title(getString(R.string.device_rotation_land))
                .checked(type.equals("force_land"))
                .checkSetId(1)
                .build());
        actions.add(new Action.Builder()
                .key(ACTION_ROTATION_ORIGINAL)
                .title(getString(R.string.device_rotation_original))
                .checked(type.equals("original"))
                .checkSetId(1)
                .build());
        return actions;
    }

    @Override
    public void onActionClicked(Action action) {
        if (ACTION_ROTATION_MIDDLE.equals(action.getKey())) {
            setRotationType("middle_port");
        } else if (ACTION_ROTATION_LAND.equals(action.getKey())) {
            setRotationType("force_land");
        } else if (ACTION_ROTATION_ORIGINAL.equals(action.getKey())) {
            setRotationType("original");
        }
        //mDialogFragment.setIcon(getIconResource(getContentResolver()));
    }

    public int getIconResource(ContentResolver contentResolver) {
        return R.drawable.ic_settings_rotation;
        //return getRotationEnabled() ? R.drawable.ic_settings_overscan : R.drawable.ic_settings_rotation;
    }

    private void setRotationType(String type) {
        mSystemControlManager.setProperty("persist.sys.app.rotation", type);
    }

    private String getRotationType() {
        return mSystemControlManager.getPropertyString("persist.sys.app.rotation", "middle_port");
    }
}
