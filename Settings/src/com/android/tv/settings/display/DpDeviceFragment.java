package com.android.tv.settings.display;

import android.support.annotation.Keep;

/**
 * DpDeviceFragment.
 */

@Keep
public class DpDeviceFragment extends DeviceFragment {

    public DpDeviceFragment() {
        super();
    }

    @Override
    protected DisplayInfo getDisplayInfo() {
        return DrmDisplaySetting.getDpDisplayInfo();
    }
}
