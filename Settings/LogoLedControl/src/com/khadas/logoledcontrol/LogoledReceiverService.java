package com.khadas.logoledcontrol;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import android_serialport_api.KhadasLedLogoControl;

public class LogoledReceiverService extends IntentService {
    private static final String TAG = "LogoledReceiverService";
    private static final String ACTION_BROADCAST = "broadcast_receiver";
    private KhadasLedLogoControl khadasLedLogoControl;
    public LogoledReceiverService() {
        super("LogoledReceiverService");
	khadasLedLogoControl = new KhadasLedLogoControl();
    }

    public static void processBroadcastIntent(Context context, Intent broadcastIntent) {
        // Launch the Service
        Intent i = new Intent(context, LogoledReceiverService.class);
        i.setAction(ACTION_BROADCAST);
        i.putExtra(Intent.EXTRA_INTENT, broadcastIntent);
  Log.d(TAG, "startService");
        context.startService(i);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        int userId = UserHandle.myUserId();
  Log.d(TAG, "onHandleIntent, User Id = " + userId);
        final String action = intent.getAction();
        if (!ACTION_BROADCAST.equals(action)) {
            return;
        }

        final Intent broadcastIntent = intent.getParcelableExtra(Intent.EXTRA_INTENT);
        final String broadcastAction = broadcastIntent.getAction();
  Log.d(TAG, "action= " + broadcastAction);
        if (Intent.ACTION_BOOT_COMPLETED.equals(broadcastAction)) {
            // ALPS00448092.
	    String brightness = SystemProperties.get("persist.sys.logoled.brightness", "unknown");
	    khadasLedLogoControl.Ledbrigthnessplus(Integer.parseInt(brightness));
            Log.d(TAG, "revieve ACTION_BOOT_COMPLETED" );
        }
    }

}
