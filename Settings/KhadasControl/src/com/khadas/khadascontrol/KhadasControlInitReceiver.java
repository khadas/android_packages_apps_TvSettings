package com.khadas.khadascontrol;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.UserHandle;
import android.util.Log;
import android.net.Uri;
import android.os.Environment;
import java.io.File;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import android.widget.Toast;
import android.os.SystemProperties;
import android.hardware.input.InputManager;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.KeyCharacterMap;
import android.view.InputDevice;


public class KhadasControlInitReceiver extends BroadcastReceiver {
    private static final String TAG = "KhadasControlInitReceiver";
    private static final String udiskfile = "bootup.bmp";
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            context.startService(new Intent(context, KhadasControlReceiverService.class));
        }
		if (Intent.ACTION_MEDIA_MOUNTED.equals(action)) {
		    Uri uri = intent.getData();
		    if (uri.getScheme().equals("file")) {
				String path = uri.getPath();
				String externalStoragePath = Environment.getExternalStorageDirectory().getPath();
				String legacyPath = Environment.getLegacyExternalStorageDirectory().getPath();
				try {
					path = new File(path).getCanonicalPath();
				} catch (IOException e) {
                    Log.e(TAG, "couldn't canonicalize " + path);
                    return;
				}
                if (path.startsWith(legacyPath)) {
					path = externalStoragePath + path.substring(legacyPath.length());
               }
				String fullpath = path+"/"+udiskfile;
				Log.d(TAG, "Factory fullpath="+fullpath);
				File file = new File(fullpath);
		        if(file.exists() && file.isFile()) {
		           try {
				       Thread.sleep(2000);
		           } catch (InterruptedException e) {
				       e.printStackTrace();
			       }
		           String cmd = "cp " + fullpath + " /data/";
		           Log.d(TAG, "cmd: "+cmd);
		           exec(cmd);
		           exec("/system/bin/sh /vendor/bin/change_logo.sh");
		           Toast.makeText(context,"bootup logo has updated ,please reboot" ,Toast.LENGTH_LONG).show();
				}
			}
		}
    }
	public static String exec(String command) {
		Process process = null;
		BufferedReader reader = null;
		InputStreamReader is = null;
		DataOutputStream os = null;
		try {
		    process = Runtime.getRuntime().exec("su");
		    is = new InputStreamReader(process.getInputStream());
		    reader = new BufferedReader(is);
		    os = new DataOutputStream(process.getOutputStream());
		    os.writeBytes(command + "\n");
		    os.writeBytes("exit\n");
		    os.flush();
		    int read;
		    char[] buffer = new char[4096];
            StringBuilder output = new StringBuilder();
            while ((read = reader.read(buffer)) > 0) {
            output.append(buffer, 0, read);
            }
            process.waitFor();
            return output.toString();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
	          try {
					if (os != null) {
						os.close();
					}
					if (reader != null) {
						reader.close();
					}
					if (is != null) {
						is.close();
					}
					if (process != null) {
						process.destroy();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
}
