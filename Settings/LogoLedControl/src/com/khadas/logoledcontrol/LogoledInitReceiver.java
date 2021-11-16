package com.khadas.logoledcontrol;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.UserHandle;
import android.util.Log;

public class LogoledInitReceiver extends BroadcastReceiver {
    private static final String TAG = "LogoledInitReceiver";

    /**
     * Sets alarm on ACTION_BOOT_COMPLETED. Resets alarm on TIME_SET, TIMEZONE_CHANGED
     * @param context Context
     * @param intent Intent
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(TAG, "LogoledInitReceiver" + action);
        int userId = UserHandle.myUserId();
        Log.d(TAG, "userId = " + userId);
        if (userId != UserHandle.USER_OWNER) {
        Log.d(TAG, "not owner , return ,don't start LogoledReceiverService");
            return;
        }
        if (context.getContentResolver() == null) {
        Log.e(TAG, "LogoledInitReceiver: FAILURE unable to get content resolver.  Alarms inactive.");
            return;
        }
        LogoledReceiverService.processBroadcastIntent(context, intent);
    }
}
