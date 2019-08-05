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

    private void onInit() {
        model = Build.MODEL;
    }
}
