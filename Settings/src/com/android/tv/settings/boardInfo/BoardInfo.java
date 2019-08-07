package com.android.tv.settings.boardInfo;

import android.os.SystemProperties;
import android.os.Build;

public class BoardInfo {
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

    private void onInit() {
        model = Build.MODEL;
    }
}
