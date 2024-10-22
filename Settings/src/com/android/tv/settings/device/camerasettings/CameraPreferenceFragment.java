package com.android.tv.settings.device.camerasettings;

import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceViewHolder;
import android.support.v7.preference.TwoStatePreference;
import com.android.tv.settings.RadioPreference;
import android.text.TextUtils;
import android.os.SystemProperties;
import com.android.internal.logging.nano.MetricsProto;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.tv.settings.PreferenceControllerFragment;
import com.android.tv.settings.SettingsPreferenceFragment;
import com.android.tv.settings.R;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import android.util.Log;

public class CameraPreferenceFragment extends SettingsPreferenceFragment {

    static final String KEY_CAMERA_SETTINGS_FACEBACK_CATEGORY = "camera_settings_faceback_category";
    static final String KEY_CAMERA_SETTINGS_ORIENTATION_CATEGORY = "camera_settings_orientation_category";

    private PreferenceCategory mCamerasettingsfacebackCategoryPref;
    private PreferenceCategory mCamerasettingsorientationCategoryPref;
    private int mCurrentCamerafacebackIndex;
    private int mCurrentCameraorientationIndex;

    private Context mContext;

    private static final int[] camerafacebackValue = {
       0,
       1,
       2
    };
    private static final String[] camerafacebackPrefix = {
       "facing_front",
       "facing_back",
       "facing_external",
    };

    private static final int[] cameraorientationValue = {
       0,
       90,
       180,
       270
    };
    private static final String[] cameraorientationPrefix = {
       "orientation_0",
       "orientation_90",
       "orientation_180",
       "orientation_270",
    };

    public static CameraPreferenceFragment newInstance() {
        return new CameraPreferenceFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.camera_setting, null);

        mCamerasettingsfacebackCategoryPref = (PreferenceCategory) findPreference(KEY_CAMERA_SETTINGS_FACEBACK_CATEGORY);
        mCurrentCamerafacebackIndex = camerafacebackValueToIndex();
        setupFacebackPreferences();

        mCamerasettingsorientationCategoryPref = (PreferenceCategory) findPreference(KEY_CAMERA_SETTINGS_ORIENTATION_CATEGORY);
        mCurrentCameraorientationIndex = cameraorientationValueToIndex();
        setupOrientationPreferences();
    }

    private void setupFacebackPreferences() {
        int currentIndex = camerafacebackValueToIndex();
        String[] camerafacebackStr = mContext.getResources().getStringArray(R.array.camera_faceback_array);

        for (int i = 0; i < camerafacebackStr.length; i++) {
            RadioPreference pref = new RadioPreference(mContext);
            pref.setPersistent(false);
            pref.setTitle(camerafacebackStr[i]);
            pref.setKey(camerafacebackPrefix[i]);
            pref.setRadioGroup(KEY_CAMERA_SETTINGS_FACEBACK_CATEGORY);
            pref.setLayoutResource(R.layout.preference_reversed_widget);
            pref.setChecked(currentIndex == i ? true:false);
            pref.setEnabled(true);
            mCamerasettingsfacebackCategoryPref.addPreference(pref);
        }
    }

    private void setupOrientationPreferences() {
        int currentIndex = cameraorientationValueToIndex();
        String[] cameraorientationStr = mContext.getResources().getStringArray(R.array.camera_orientation_array);

        for (int i = 0; i < cameraorientationStr.length; i++) {
            RadioPreference pref = new RadioPreference(mContext);
            pref.setPersistent(false);
            pref.setTitle(cameraorientationStr[i]);
            pref.setKey(cameraorientationPrefix[i]);
            pref.setRadioGroup(KEY_CAMERA_SETTINGS_ORIENTATION_CATEGORY);
            pref.setLayoutResource(R.layout.preference_reversed_widget);
            pref.setChecked(currentIndex == i ? true:false);
            pref.setEnabled(true);
            mCamerasettingsorientationCategoryPref.addPreference(pref);
        }
    }

    private static int camerafacebackValueToIndex() {
        return SystemProperties.getInt("persist.sys.camera_usb_faceback", 2);
    }
    private void setCamerafaceback(int value) {
        SystemProperties.set("persist.sys.camera_usb_faceback", Integer.toString(value));
    }

    private static int cameraorientationValueToIndex() {
        int value = SystemProperties.getInt("persist.sys.camera_usb_orientation", 0);
        for (int i = 0; i < cameraorientationValue.length; i++) {
            if (cameraorientationValue[i] == value) {
                return i;
            }
        }
        return 0;
    }
    private void setCameraorientation(int value) {
        SystemProperties.set("persist.sys.camera_usb_orientation", Integer.toString(value));
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
    if (preference instanceof RadioPreference) {
        RadioPreference selectedPreference = (RadioPreference) preference;

        if (mCamerasettingsfacebackCategoryPref.findPreference(selectedPreference.getKey()) != null) {
            selectedPreference.clearOtherRadioPreferences(mCamerasettingsfacebackCategoryPref);

            for (int i = 0; i < camerafacebackPrefix.length; i++) {
                if (TextUtils.equals(selectedPreference.getKey(), camerafacebackPrefix[i])) {
                    setCamerafaceback(camerafacebackValue[i]);
                    break;
                }
            }
            Toast.makeText(mContext, mContext.getString(R.string.reboot_take_effect), Toast.LENGTH_LONG).show();
        } else if (mCamerasettingsorientationCategoryPref.findPreference(selectedPreference.getKey()) != null) {
            selectedPreference.clearOtherRadioPreferences(mCamerasettingsorientationCategoryPref);

            for (int i = 0; i < cameraorientationPrefix.length; i++) {
                if (TextUtils.equals(selectedPreference.getKey(), cameraorientationPrefix[i])) {
                    setCameraorientation(cameraorientationValue[i]);
                    break;
                }
            }
            Toast.makeText(mContext, mContext.getString(R.string.reboot_take_effect), Toast.LENGTH_LONG).show();
        }
        selectedPreference.setChecked(true);
    }
    return super.onPreferenceTreeClick(preference);
}

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.KHADAS_CAMERA_SETTINGS;
    }
}
