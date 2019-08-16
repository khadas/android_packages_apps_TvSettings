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
    public static String model;

    public BoardInfo() {
        onInit();
    }

    public String getString(String property) {
        return SystemProperties.get(property, "unknown");
    }

    public boolean isRedLedSupport() {
        if(model.equals("VIM2") || model.equals("VIM3") || model.equals("VIM3L"))
            return true;
        else
            return false;
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

    private void onInit() {
        model = Build.MODEL;
    }
}
