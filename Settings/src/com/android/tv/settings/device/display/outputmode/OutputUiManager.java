package com.android.tv.settings.device.display.outputmode;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.droidlogic.app.SystemControlManager;
import com.droidlogic.app.OutputModeManager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.UserHandle ;

public class OutputUiManager {
    private static final String TAG = "OutputUiManager";
    private static boolean DEBUG = false;

    private static final String[] HDMI_LIST = {"1080p","1080p50hz","1080p24hz","720p","720p50hz","4k2k24hz","4k2k25hz","4k2k30hz","4k2ksmpte","576p","480p","1080i","1080i50hz","576i","480i"};
    private static final String[] HDMI_TITLE = {"1080p-60hz","1080p-50hz","1080p-24hz","720p-60hz","720p-50hz","4k2k-24hz","4k2k-25hz","4k2k-30hz","4k2k-smpte","576p-50hz","480p-60hz","1080i-60hz","1080i-50hz","576i-50hz","480i-60hz" };
    private static final String[] CVBS_MODE_VALUE_LIST = {"480cvbs","576cvbs"};
    private static final String[] CVBS_MODE_TITLE_LIST = {"480 CVBS","576 CVBS"};
    private static String[] ALL_HDMI_MODE_VALUE_LIST;
    private static String[] ALL_HDMI_MODE_TITLE_LIST;

    private ArrayList<String> mTitleList = null;
    private ArrayList<String> mValueList = null;
    private ArrayList<String> mSupportList = null;

    private SystemControlManager sw;
    private OutputModeManager mom;
    private Context mContext;

    private static String mUiMode;
    public static final String CVBS_MODE = "cvbs";
    public static final String HDMI_MODE = "hdmi";

    public OutputUiManager(Context context){
        mContext = context;
        sw = new SystemControlManager(mContext);
        mom = new OutputModeManager(mContext);

        mUiMode = getUiMode();
        mTitleList = new ArrayList<String>();
        mValueList = new ArrayList<String>();
        initModeValues(mUiMode);
    }

    public String getUiMode(){
        String currentMode = sw.readSysFs(mom.DISPLAY_MODE).replaceAll("\n","");
        if (currentMode.contains(CVBS_MODE)) {
            mUiMode = CVBS_MODE;
        } else {
            mUiMode = HDMI_MODE;
        }
        return mUiMode;
    }

    public int getCurrentModeIndex(){
         String currentMode = sw.readSysFs(mom.DISPLAY_MODE).replaceAll("\n","");
         for (int i=0 ; i < mValueList.size();i++) {
             if (currentMode.equals(mValueList.get(i))) {
                return i ;
             }
         }
         if (mUiMode.equals(HDMI_MODE)) {
            return 4;
         }else{
            return 0;
         }
    }

    private void initModeValues(String mode){
        mTitleList = new ArrayList<String>();
        mValueList = new ArrayList<String>();
        mSupportList = new ArrayList<String>();

        filterOutputMode();

        if (mode.equalsIgnoreCase(HDMI_MODE)) {
            for (int i=0 ; i< ALL_HDMI_MODE_VALUE_LIST.length; i++) {
                if (ALL_HDMI_MODE_TITLE_LIST[i] != null && ALL_HDMI_MODE_TITLE_LIST[i].length() != 0) {
                    mTitleList.add(ALL_HDMI_MODE_TITLE_LIST[i]);
                    mValueList.add(ALL_HDMI_MODE_VALUE_LIST[i]);
                }
            }
        }else if (mode.equalsIgnoreCase(CVBS_MODE)) {
            for (int i = 0 ; i< CVBS_MODE_VALUE_LIST.length; i++) {
                mTitleList.add(CVBS_MODE_VALUE_LIST[i]);
            }
            for (int i=0 ; i < CVBS_MODE_VALUE_LIST.length ; i++) {
                mValueList.add(CVBS_MODE_VALUE_LIST[i]);
            }
        }
    }

    public String getCurrentOutPutModeTitle(int type) {
        String currentHdmiMode = sw.readSysFs(mom.DISPLAY_MODE).replaceAll("\n", "");
        if (type == 0) {  // cvbs
        if (currentHdmiMode.contains(CVBS_MODE)) {
            for (int i=0 ; i < CVBS_MODE_VALUE_LIST.length ; i++) {
                if (currentHdmiMode.equals(CVBS_MODE_VALUE_LIST[i])) {
                    return CVBS_MODE_TITLE_LIST[i] ;
                    }
                }
            }
            return CVBS_MODE_TITLE_LIST[0];
        }else{      // hdmi
            for (int i=0 ; i< ALL_HDMI_MODE_VALUE_LIST.length ; i++) {
                if (currentHdmiMode.equals(ALL_HDMI_MODE_VALUE_LIST[i])) {
                    return ALL_HDMI_MODE_TITLE_LIST[i] ;
                }
            }
            return ALL_HDMI_MODE_TITLE_LIST[4];
        }
    }

    public void change2NewMode(final String mode) {
        mom.setBestMode(mode);
    }

    public void change2BestMode() {
        mom.setBestMode(null);
    }

    public String getBestMatchResolution() {
        return mom.getBestMatchResolution();
    }

    public boolean isBestOutputmode(){
        return mom.isBestOutputmode();
    }

    public ArrayList<String> getOutputmodeTitleList(){
        return mTitleList;
    }

    public ArrayList<String> getOutputmodeValueList(){
        return mValueList;
    }

    public void hdmiPlugged(){
        Log.d(TAG,"===== hdmiPlugged()");
        mom.setHdmiPlugged();
    }

    public void hdmiUnPlugged(){
        Log.d(TAG,"===== hdmiUnPlugged()");
        mom.setHdmiUnPlugged();
    }

    public boolean isHDMIPlugged() {
        return mom.isHDMIPlugged();
    }

    public boolean ifModeIsSetting() {
        return mom.ifModeIsSetting();
    }

    public void  filterOutputMode() {
        List<String> list_value = new ArrayList<String>();
        List<String> list_title = new ArrayList<String>();

        ALL_HDMI_MODE_VALUE_LIST = HDMI_LIST;
        ALL_HDMI_MODE_TITLE_LIST = HDMI_TITLE;

        for (int i = 0; i < ALL_HDMI_MODE_VALUE_LIST.length; i++) {
            if (ALL_HDMI_MODE_VALUE_LIST[i] != null) {
                list_value.add(ALL_HDMI_MODE_VALUE_LIST[i]);
                list_title.add(ALL_HDMI_MODE_TITLE_LIST[i]);
            }
        }

        String str_filter_mode = sw.getPropertyString("ro.platform.filter.modes", "");
        if (str_filter_mode != null && str_filter_mode.length() != 0) {
            String[] array_filter_mode = str_filter_mode.split(",");
            for (int i = 0; i < array_filter_mode.length; i++) {
                for (int j = 0; j < list_value.size(); j++) {
                    if ((list_value.get(j).toString()).equals(array_filter_mode[i])) {
                        list_value.remove(j);
                        list_title.remove(j);
                    }
                }
            }
        }

        String str_edid = mom.getHdmiSupportList();
        if (str_edid != null && str_edid.length() != 0 && !str_edid.contains("null")) {
            List<String> list_hdmi_mode = new ArrayList<String>();
            List<String> list_hdmi_title = new ArrayList<String>();
            for (int i = 0; i < list_value.size(); i++) {
                if (str_edid.contains(list_value.get(i))) {
                    list_hdmi_mode.add(list_value.get(i));
                    list_hdmi_title.add(list_title.get(i));
                }

            }
            ALL_HDMI_MODE_VALUE_LIST = list_hdmi_mode.toArray(new String[list_value.size()]);
            ALL_HDMI_MODE_TITLE_LIST = list_hdmi_title.toArray(new String[list_title.size()]);
        } else {
            ALL_HDMI_MODE_VALUE_LIST = list_value.toArray(new String[list_value.size()]);
            ALL_HDMI_MODE_TITLE_LIST = list_title.toArray(new String[list_title.size()]);
        }
    }

    public int autoSwitchHdmiPassthough (){
        return mom.autoSwitchHdmiPassthough();
    }

    public void setDigitalVoiceValue(String value) {
        mom.setDigitalVoiceValue(value);
    }

    public void enableDobly_DRC (boolean enable){
        mom.enableDobly_DRC(enable);
    }

    public void setDoblyMode (String mode){
        mom.setDoblyMode(mode);
    }

    public void setDTS_DownmixMode(String mode){
        mom.setDTS_DownmixMode(mode);
    }

    public void enableDTS_DRC_scale_control (boolean enable){
        mom.enableDTS_DRC_scale_control(enable);
    }

    public void enableDTS_Dial_Norm_control (boolean enable){
        mom.enableDTS_Dial_Norm_control(enable);
    }
}
