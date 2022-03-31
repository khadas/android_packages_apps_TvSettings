package com.android.tv.settings.boardInfo;

import android.os.SystemProperties;
import android.os.Build;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import android.util.Log;

public class BoardInfo {
    private static String TAG = "BoardInfo";
    private static String HWVER_SYS = "/sys/class/khadas/hwver";
    private static String MCU_FAN_TYPE_SYS = "/sys/class/fan/type";
    private static String MIPI_CAMERA_SYS = "/sys/class/camera/cam_state";
    public static final int LED_WHITE = 0;
    public static final int LED_RED = 1;
    public static final int LED_BOTH = 2;
    public static String model;

    public BoardInfo() {
        onInit();
    }

    public String getString(String property) {
        return SystemProperties.get(property, "unknown");
    }

    /* return: 0: white red  1: red led 2: both */
    public int getLedType() {
        if(model.equals("VIM3") || model.equals("VIM3L"))
            return LED_BOTH;
        else if (model.equals("VIM2"))
            return LED_WHITE;
        else {
            String hwver = readFile(HWVER_SYS);
            if (hwver.equals("VIM1.V13"))
               return LED_WHITE;
            else
               return LED_RED;
        }

    }

    public boolean isWolSupport() {
        if(model.equals("VIM2") || model.equals("VIM3") || model.equals("VIM3L"))
            return true;
        else
            return false;
    }

    private String readFile(String file) {
        String content = "";
        File OutputFile = new File(file);
        if(!OutputFile.exists())
            return content;
        try {
            FileInputStream instream = new FileInputStream(file);
            if(instream != null) {
                InputStreamReader inputreader = new InputStreamReader(instream);
                BufferedReader buffreader = new BufferedReader(inputreader);
                String line;
                while((line = buffreader.readLine())  !=  null) {
                    content = content + line;
                }
                instream.close();
            }
        } catch (FileNotFoundException e) {
           Log.e(TAG, "The File doesn\'t not exist.");
        } catch(IOException e) {
            Log.e(TAG, " readFile error!");
        }
        return content;
    }

    public boolean isFanSupport() {
        String hwver = readFile(HWVER_SYS);
        if (hwver.equals("VIM1.V12") || hwver.equals("Unknow"))
		return false;
	return true;
    }

    public boolean isDutyControlVersion() {
        String str = readFile(MCU_FAN_TYPE_SYS);
	if (str.equals("1"))
           return true;
        else
	   return false;
    }

    public boolean isM2xNetSupport() {
       if (model.equals("VIM3") || model.equals("VIM3L"))
           return true;
       else
           return false;
    }

    public boolean isIRCutSupport() {
       if (model.equals("VIM3")) {
          String str = readFile(MIPI_CAMERA_SYS);
          if (str.equals("1"))
              return true;
          return false;
       }
       return false;
    }

    public boolean isPortModeSupport() {
       if (model.equals("VIM3")|| model.equals("VIM3L"))
          return true;
       else
          return false;
    }

    private void onInit() {
        model = Build.MODEL;
    }
}
