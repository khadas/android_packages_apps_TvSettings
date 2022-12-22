package com.khadas.khadascontrol;

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
import android.content.IntentFilter;
import java.io.PrintWriter;

public class KhadasControlReceiverService extends Service {
    private KhadasControlInitReceiver khadascontrolInitReceiver;
    private static final String TAG = "KhadasControlReceiverService";
    private static final String SYS_ADC_TABLE = "sys/class/adc_keypad/table";

    @Override
    public void onCreate() {
        super.onCreate();
        khadascontrolInitReceiver = new KhadasControlInitReceiver();
	setFunKey(SystemProperties.getInt("persist.sys.func.key.action", 3));
    }
    public static int setFunKey(int value) {
	    Log.d(TAG,"setFanKey: " + value);
	    File file = new File(SYS_ADC_TABLE);
	    if((file == null) || !file.exists()){
	        Log.e(TAG, "" + SYS_ADC_TABLE + " no exist");
	        return -1;
	    }
	    try {
	        FileOutputStream fout = new FileOutputStream(file);
	        PrintWriter pWriter = new PrintWriter(fout);
	        pWriter.println("home:"+String.valueOf(value)+":2:108:40");
	        pWriter.flush();
	        pWriter.close();
	        fout.close();
	    } catch (IOException e) {
	        Log.e(TAG, "setFanKey ERR: " + e);
	        return -1;
	    }
       return 0;
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
