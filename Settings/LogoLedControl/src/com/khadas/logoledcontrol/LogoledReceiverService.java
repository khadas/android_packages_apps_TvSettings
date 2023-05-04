package com.khadas.logoledcontrol;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.util.Log;
import android.os.IBinder;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class LogoledReceiverService extends Service {
    private LogoledInitReceiver LogoledInitReceiver;

    @Override
    public void onCreate() {
        super.onCreate();
        LogoledInitReceiver = new LogoledInitReceiver();

        if(SystemProperties.get("sys.extboard.exist", "unknown").equals("1")) {
		new Thread(){
		    @Override
		    public void run() {
			super.run();
			while(true){
			    try {
				String mode = SystemProperties.get("persist.sys.logo.led.trigger","1");
				//LogoledInitReceiver.LogoLedModeControl(Integer.parseInt(mode));
			        Thread.sleep(1000);
			    } catch (InterruptedException e) {
			        e.printStackTrace();
			    }

			}
		    }
		}.start();

      }
    }

   @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
