package com.android.tv.settings.device.sound.dolbysound;

import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.BroadcastReceiver;
import android.os.SystemProperties;

import com.droidlogic.app.OutputModeManager;
import com.droidlogic.app.SystemControlManager;

public class DolbyBootReceiver extends BroadcastReceiver {
    private boolean mSupportDRC = SystemProperties.getBoolean("ro.platform.support.dolby", false);

    @Override
    public void onReceive(Context context, Intent intent) {
        if (mSupportDRC) initDRCMode(context);
    }

    public void initDRCMode(Context context) {
        String data = "";
        OutputModeManager omm = new OutputModeManager(context);
        SharedPreferences spf = context.getSharedPreferences(DolbySoundActivity.PREFERENCE_BOX_SETTING, Context.MODE_PRIVATE);
        data = spf.getString(DolbySoundActivity.DRC_MODE, DolbySoundActivity.DRC_LINE);
        if (data.equals(DolbySoundActivity.DRC_OFF)) {
            omm.enableDobly_DRC(false);
        } else if (data.equals(DolbySoundActivity.DRC_RF)) {
            omm.enableDobly_DRC(true);
            omm.setDoblyMode(DolbySoundActivity.RF);
        } else {
            omm.enableDobly_DRC(true);
            omm.setDoblyMode(DolbySoundActivity.LINE);
        }
    }
}
