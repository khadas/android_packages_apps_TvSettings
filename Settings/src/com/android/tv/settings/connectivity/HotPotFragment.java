package com.android.tv.settings.connectivity;

import android.support.v17.preference.LeanbackPreferenceFragment;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothPan;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.usb.UsbManager;
import android.net.ConnectivityManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.SystemProperties;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.UserManager;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.util.Log;

import android.app.DialogFragment;
import android.app.Fragment;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;





import com.android.tv.settings.R;
import com.android.internal.logging.nano.MetricsProto.MetricsEvent;


import com.android.settingslib.TetherUtil;


import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

import static android.net.ConnectivityManager.TETHERING_BLUETOOTH;
import static android.net.ConnectivityManager.TETHERING_USB;
import static android.net.ConnectivityManager.TETHERING_WIFI;

public class HotPotFragment extends LeanbackPreferenceFragment
        implements Preference.OnPreferenceChangeListener,
        DialogInterface.OnClickListener, DialogCreatable {

    private static final String USB_TETHER_SETTINGS = "usb_tether_settings";
    private static final String ENABLE_WIFI_AP = "enable_wifi_ap";
    private static final String ENABLE_WIFI_RSDB = "enable_wifi_rsdb";
    private static final String ENABLE_BLUETOOTH_TETHERING = "enable_bluetooth_tethering";
    private static final String TETHER_CHOICE = "TETHER_TYPE";
    private static final String DATA_SAVER_FOOTER = "disabled_on_data_saver";

    private static final int DIALOG_AP_SETTINGS = 1;

    private static final String TAG = "TetheringSettings";

    private SwitchPreference mUsbTether;

    private WifiApEnabler mWifiApEnabler;
    private SwitchPreference mEnableWifiAp;
    private SwitchPreference mEnableWifiRsdb;
    private SwitchPreference mBluetoothTether;

    private BroadcastReceiver mTetherChangeReceiver;

    private String[] mUsbRegexs;

    private String[] mWifiRegexs;

    private String[] mBluetoothRegexs;
    private AtomicReference<BluetoothPan> mBluetoothPan = new AtomicReference<BluetoothPan>();

    private Handler mHandler = new Handler();
    private OnStartTetheringCallback mStartTetheringCallback;

    private static final String WIFI_AP_SSID_AND_SECURITY = "wifi_ap_ssid_and_security";
    private static final int CONFIG_SUBTEXT = R.string.wifi_tether_configure_subtext;

    private String[] mSecurityType;
    private Preference mCreateNetwork;

    private WifiApDialog mDialog;
    private WifiManager mWifiManager;
    private WifiConfiguration mWifiConfig = null;
    private ConnectivityManager mCm;

    public static boolean mRestartWifiApAfterConfigChange;
    public static boolean mRestartRsdbAfterConfigChange;

    private boolean mUsbConnected;
    private boolean mMassStorageActive;

    private boolean mBluetoothEnableForTether;

    /* Stores the package name and the class name of the provisioning app */
    private String[] mProvisionApp;
    private static final int PROVISION_REQUEST = 0;

    private boolean mUnavailable;

    private boolean mDataSaverEnabled;
    private Preference mDataSaverFooter;
    private int mDelayTimeBeforRestartWifiAp = 1000; // ms
    private final String PROP_RSDB_NAME = "persist.sys.wifi.rsdb.name";
    private final String PROP_RSDB_PASSWD = "persist.sys.wifi.rsdb.passwd";
    private final String PROP_RSDB_SECURITY_TYPE = "persist.sys.wifi.rsdb.security.type";
    private final String PROP_RSDB_APBAND = "persist.sys.wifi.rsdb.apband";
    private final String PROP_RSDB_ENABLE = "sys.wifi.rsdb.enable";
    private final String PROP_WIFIAP_ENABLE = "sys.wifi.wifiap.enable";
    private final IntentFilter mIntentFilter = new IntentFilter();
    //private boolean hasRsdb;
    private boolean mRsdbEnabled;
    private int mRsdbNetId;
    public static final int OPEN_INDEX = 0;
    public static final int WPA2_INDEX = 1;

    public static HotPotFragment newInstance() {
        return new HotPotFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.hotpot);

        mDataSaverFooter = findPreference(DATA_SAVER_FOOTER);

        final Activity activity = getActivity();
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter != null) {
            adapter.getProfileProxy(activity.getApplicationContext(), mProfileServiceListener,
                    BluetoothProfile.PAN);
        }

        mEnableWifiAp =
                (SwitchPreference) findPreference(ENABLE_WIFI_AP);
        mEnableWifiRsdb = (SwitchPreference) findPreference(ENABLE_WIFI_RSDB);
        String rsdbenable = SystemProperties.get(PROP_RSDB_ENABLE, "0");
        String wifienable = SystemProperties.get(PROP_WIFIAP_ENABLE, "0");
        if(rsdbenable.equals("1")){
          mEnableWifiRsdb.setChecked(true);
        }
        if(wifienable.equals("1")){
          mEnableWifiAp.setChecked(true);
        }
        Preference wifiApSettings = findPreference(WIFI_AP_SSID_AND_SECURITY);
        mUsbTether = (SwitchPreference) findPreference(USB_TETHER_SETTINGS);
        mBluetoothTether = (SwitchPreference) findPreference(ENABLE_BLUETOOTH_TETHERING);

        mCm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        mWifiManager = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);

    /*  String model = SystemProperties.get("persist.sys.wifi.model", "6212");
        if (model.equals("6359")) {
            hasRsdb = true;
        } else {
            hasRsdb = true;
        }
        if (hasRsdb) {

        } else {
            getPreferenceScreen().removePreference(mEnableWifiRsdb);
        }*/

        mUsbRegexs = mCm.getTetherableUsbRegexs();
        mWifiRegexs = mCm.getTetherableWifiRegexs();
        mBluetoothRegexs = mCm.getTetherableBluetoothRegexs();

        final boolean usbAvailable = mUsbRegexs.length != 0;
        final boolean wifiAvailable = mWifiRegexs.length != 0;
        final boolean bluetoothAvailable = mBluetoothRegexs.length != 0;

        Log.i(TAG," usbAvailable = " + usbAvailable + "   wifiAvailable = " + wifiAvailable + "  bluetoothAvailable = " + bluetoothAvailable);

        if (!usbAvailable || ActivityManager.isUserAMonkey()) {
            getPreferenceScreen().removePreference(mUsbTether);
        }

        if (wifiAvailable && !ActivityManager.isUserAMonkey()) {
            mWifiApEnabler = new WifiApEnabler(activity, mEnableWifiAp);
            initWifiTethering();
        } else {
            getPreferenceScreen().removePreference(mEnableWifiAp);
            getPreferenceScreen().removePreference(wifiApSettings);
        }

        if (!bluetoothAvailable) {
            getPreferenceScreen().removePreference(mBluetoothTether);
        } else {
            BluetoothPan pan = mBluetoothPan.get();
            if (pan != null && pan.isTetheringOn()) {
                mBluetoothTether.setChecked(true);
            } else {
                mBluetoothTether.setChecked(false);
            }
        }
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
//        setPreferencesFromResource(R.xml.hotpot, null);
    }

    @Override
    public Dialog onCreateDialog(int id) {
        if (id == DIALOG_AP_SETTINGS) {
            final Activity activity = getActivity();
            //           mDialog = new WifiApDialog(activity, this, mWifiConfig);
            return mDialog;
        }

        return null;
    }

    public static boolean isProvisioningNeededButUnavailable(Context context) {
        return (TetherUtil.isProvisioningNeeded(context)
                && !isIntentAvailable(context));
    }

    private static boolean isIntentAvailable(Context context) {
        String[] provisionApp = context.getResources().getStringArray(
                com.android.internal.R.array.config_mobile_hotspot_provision_app);
        if (provisionApp.length < 2) {
            return false;
        }
        final PackageManager packageManager = context.getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setClassName(provisionApp[0], provisionApp[1]);
        return (packageManager.queryIntentActivities(intent,
                PackageManager.MATCH_DEFAULT_ONLY).size() > 0);
    }

    @Override
    public void onStart() {
        super.onStart();
        final Activity activity = getActivity();
        Log.d("TetheringSettings", "onStart");
        mStartTetheringCallback = new OnStartTetheringCallback(this);

        mMassStorageActive = Environment.MEDIA_SHARED.equals(Environment.getExternalStorageState());
        mTetherChangeReceiver = new TetherChangeReceiver();
        IntentFilter filter = new IntentFilter(ConnectivityManager.ACTION_TETHER_STATE_CHANGED);
        filter.addAction(WifiManager.WIFI_AP_STATE_CHANGED_ACTION);
        Intent intent = activity.registerReceiver(mTetherChangeReceiver, filter);

        filter = new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_STATE);
        activity.registerReceiver(mTetherChangeReceiver, filter);

        filter = new IntentFilter();
        filter.addAction(Intent.ACTION_MEDIA_SHARED);
        filter.addAction(Intent.ACTION_MEDIA_UNSHARED);
        filter.addDataScheme("file");
        activity.registerReceiver(mTetherChangeReceiver, filter);

        filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        activity.registerReceiver(mTetherChangeReceiver, filter);

        if (intent != null) mTetherChangeReceiver.onReceive(activity, intent);
        if (mWifiApEnabler != null) {
            mEnableWifiAp.setOnPreferenceChangeListener(this);
            mWifiApEnabler.resume();
        }

        mEnableWifiAp.setChecked(mRestartWifiApAfterConfigChange);
        //if (hasRsdb)
        mEnableWifiRsdb.setOnPreferenceChangeListener(this);
        if(mRestartRsdbAfterConfigChange){
        try{
            Log.d("TetheringSettings", "rsdb config change restart softap");
            Runtime run = Runtime.getRuntime();
            run.exec("./vendor/bin/start_softap.sh");
            }catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
           }
        }
        mRestartRsdbAfterConfigChange = false;
        updateState();
        updateCreateNetwork();
    }

    @Override
    public void onStop() {
        super.onStop();

        if (mUnavailable) {
            return;
        }
        getActivity().unregisterReceiver(mTetherChangeReceiver);
        mTetherChangeReceiver = null;
        mStartTetheringCallback = null;
        if (mWifiApEnabler != null) {
            mEnableWifiAp.setOnPreferenceChangeListener(null);
            mWifiApEnabler.pause();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void initWifiTethering() {

        final Activity activity = getActivity();
        mWifiConfig = mWifiManager.getWifiApConfiguration();
        mSecurityType = getResources().getStringArray(R.array.wifi_ap_security);

        mCreateNetwork = findPreference(WIFI_AP_SSID_AND_SECURITY);

        mRestartWifiApAfterConfigChange = false;
		mRestartRsdbAfterConfigChange = false;

        if (mWifiConfig == null) {
            final String s = activity.getString(
                    com.android.internal.R.string.wifi_tether_configure_ssid_default);
            mCreateNetwork.setSummary(String.format(activity.getString(CONFIG_SUBTEXT),
                    s, mSecurityType[WifiApDialog.OPEN_INDEX]));
        } else {
            int index = WifiApDialog.getSecurityTypeIndex(mWifiConfig);
            mCreateNetwork.setSummary(String.format(activity.getString(CONFIG_SUBTEXT),
                    mWifiConfig.SSID,
                    mSecurityType[index]));
        }

    }

     private void initWifiRsdb() {
         final Activity activity = getActivity();
         mWifiConfig = mWifiManager.getWifiApConfiguration();
         mSecurityType = getResources().getStringArray(R.array.wifi_ap_security);
         if (mWifiConfig != null) {
            SystemProperties.set(PROP_RSDB_NAME, mWifiConfig.SSID);
            SystemProperties.set(PROP_RSDB_PASSWD, mWifiConfig.preSharedKey);
            if (WifiApDialog.getSecurityTypeIndex(mWifiConfig) == WPA2_INDEX)
            {
                SystemProperties.set(PROP_RSDB_SECURITY_TYPE, "wpa2-psk");
            } else {
				SystemProperties.set(PROP_RSDB_SECURITY_TYPE, "open");
            }
            if(mWifiConfig.apBand == 0)
            {
                Log.d("TetheringSettings", "2.4G set");
                SystemProperties.set(PROP_RSDB_APBAND, "6");//2.4G
            } else {
                Log.d("TetheringSettings", "5G  set");
				SystemProperties.set(PROP_RSDB_APBAND, "48");//5G
            }
         }
     }

	 private void updateCreateNetwork() {
	     final Activity activity = getActivity();
	      mWifiConfig = mWifiManager.getWifiApConfiguration();
	      int index = WifiApDialog.getSecurityTypeIndex(mWifiConfig);
            mCreateNetwork.setSummary(String.format(activity.getString(CONFIG_SUBTEXT),
                    mWifiConfig.SSID,
                    mSecurityType[index]));
     }

    private class TetherChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context content, Intent intent) {
            String action = intent.getAction();
            if (action.equals(ConnectivityManager.ACTION_TETHER_STATE_CHANGED)) {
                // TODO - this should understand the interface types
                ArrayList<String> available = intent.getStringArrayListExtra(
                        ConnectivityManager.EXTRA_AVAILABLE_TETHER);
                ArrayList<String> active = intent.getStringArrayListExtra(
                        ConnectivityManager.EXTRA_ACTIVE_TETHER);
                ArrayList<String> errored = intent.getStringArrayListExtra(
                        ConnectivityManager.EXTRA_ERRORED_TETHER);
                updateState(available.toArray(new String[available.size()]),
                        active.toArray(new String[active.size()]),
                        errored.toArray(new String[errored.size()]));
                if (mWifiManager.getWifiApState() == WifiManager.WIFI_AP_STATE_DISABLED
                        && mRestartWifiApAfterConfigChange) {
                    mRestartWifiApAfterConfigChange = false;
                    Log.d(TAG, "Restarting WifiAp due to prior config change.");
                    try {
                        Log.d(TAG, "Sleep " + mDelayTimeBeforRestartWifiAp + "ms befor restarting WifiAp.");
                        Thread.sleep(mDelayTimeBeforRestartWifiAp);
                    } catch (InterruptedException ignore) {
                    }
                    startTethering(TETHERING_WIFI);
                }else if(mWifiManager.getWifiApState() == WifiManager.WIFI_AP_STATE_ENABLED
                    && mRestartWifiApAfterConfigChange)
                {
                    mRestartWifiApAfterConfigChange = false;
                    Log.d(TAG, "Restarting WifiAp due to prior config change.");
                    mCm.stopTethering(TETHERING_WIFI);
                    try {
                    Log.d(TAG, "Sleep " + mDelayTimeBeforRestartWifiAp + "ms befor restarting WifiAp.");
                        Thread.sleep(mDelayTimeBeforRestartWifiAp);
                    } catch (InterruptedException ignore) {
                    }
                    startTethering(TETHERING_WIFI);
                }
            } else if (action.equals(WifiManager.WIFI_AP_STATE_CHANGED_ACTION)) {
                int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_AP_STATE, 0);
                if (state == WifiManager.WIFI_AP_STATE_DISABLED
                        && mRestartWifiApAfterConfigChange) {
                    mRestartWifiApAfterConfigChange = false;
                    Log.d(TAG, "Restarting WifiAp due to prior config change.");
                    try {
                        Log.d(TAG, "Sleep " + mDelayTimeBeforRestartWifiAp + "ms befor restarting WifiAp.");
                        Thread.sleep(mDelayTimeBeforRestartWifiAp);
                    } catch (InterruptedException ignore) {
                    }
                    startTethering(TETHERING_WIFI);
                }
            } else if (action.equals(Intent.ACTION_MEDIA_SHARED)) {
                mMassStorageActive = true;
                updateState();
            } else if (action.equals(Intent.ACTION_MEDIA_UNSHARED)) {
                mMassStorageActive = false;
                updateState();
            } else if (action.equals(UsbManager.ACTION_USB_STATE)) {
                mUsbConnected = intent.getBooleanExtra(UsbManager.USB_CONNECTED, false);
                updateState();
            } else if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                if (mBluetoothEnableForTether) {
                    switch (intent
                            .getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)) {
                        case BluetoothAdapter.STATE_ON:
                            startTethering(TETHERING_BLUETOOTH);
                            mBluetoothEnableForTether = false;
                            break;

                        case BluetoothAdapter.STATE_OFF:
                        case BluetoothAdapter.ERROR:
                            mBluetoothEnableForTether = false;
                            break;

                        default:
                            // ignore transition states
                    }
                }
                updateState();
            }
        }
    }

    public void startTethering(int choice) {
        if (choice == TETHERING_BLUETOOTH) {
            // Turn on Bluetooth first.
            BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
            if (adapter == null) {
                return;
            }
            if (adapter.getState() == BluetoothAdapter.STATE_OFF) {
                mBluetoothEnableForTether = true;
                adapter.enable();
                mBluetoothTether.setSummary(R.string.bluetooth_turning_on);
                mBluetoothTether.setEnabled(false);
                return;
            }
        }

        mCm.startTethering(choice, true, mStartTetheringCallback, mHandler);
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference == mUsbTether) {
            if (mUsbTether.isChecked()) {
                startTethering(TETHERING_USB);
            } else {
                mCm.stopTethering(TETHERING_USB);
            }
        } else if (preference == mBluetoothTether) {
            if (mBluetoothTether.isChecked()) {
                startTethering(TETHERING_BLUETOOTH);
            } else {
                mCm.stopTethering(TETHERING_BLUETOOTH);
                // No ACTION_TETHER_STATE_CHANGED is fired or bluetooth unless a device is
                // connected. Need to update state manually.
                updateState();
            }
        } else if (preference == mCreateNetwork) {
            //           showDialog(DIALOG_AP_SETTINGS);
        }

        return super.onPreferenceTreeClick(preference);
    }
  /*
    @Override
    public boolean onPreferenceChange(Preference preference, Object value) {
        boolean enable = (Boolean) value;

        if (enable) {
            startTethering(TETHERING_WIFI);
        } else {
            mCm.stopTethering(TETHERING_WIFI);
        }
        return false;
    }
   */
	@Override
    public boolean onPreferenceChange(Preference preference, Object value) {
        boolean enable = (Boolean) value;
		BufferedReader br = null;
		Runtime runtime = Runtime.getRuntime();
        final String key = preference.getKey();
		Log.d(TAG, " onPreferenceChange : " + value);
        if (ENABLE_WIFI_AP.equals(key)) {
           if (enable) {
                Log.d(TAG, " onPreferenceChange stop rsdb" );
                mEnableWifiRsdb.setChecked(false);
			try{
			    Process p = Runtime.getRuntime().exec("ndc netd 6002 softap stopap");
              }catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
		     startTethering(TETHERING_WIFI);
			 if(mWifiManager.getWifiApState() == WifiManager.WIFI_AP_STATE_ENABLED){
			 Log.d(TAG, " onPreferenceChange start wifiap sucess");
			 SystemProperties.set(PROP_WIFIAP_ENABLE, "1");
			 }

           } else {
             mCm.stopTethering(TETHERING_WIFI);
			 SystemProperties.set(PROP_WIFIAP_ENABLE, "0");
           }
         } else if (ENABLE_WIFI_RSDB.equals(key)) {
            if (enable){
			 mEnableWifiRsdb.setChecked(true);
             mEnableWifiAp.setChecked(false);
			 if(mWifiManager.getWifiApState() == WifiManager.WIFI_AP_STATE_ENABLED){
			      Log.d(TAG, " onPreferenceChange disable wifiap " );
			      mCm.stopTethering(TETHERING_WIFI);
			 }
			 initWifiRsdb();
			 Log.d(TAG, " onPreferenceChange start rsdb" );
			 try{
			      Runtime run = Runtime.getRuntime();
			      run.exec("./vendor/bin/start_softap.sh");
            }catch (IOException e) {
            // TODO Auto-generated catch block
                  e.printStackTrace();
              }
			SystemProperties.set(PROP_RSDB_ENABLE, "1");
           } else {
		          mEnableWifiRsdb.setChecked(false);
			      Log.d(TAG, " onPreferenceChange stop rsdb" );
			 try{
			      Process p = Runtime.getRuntime().exec("ndc netd 6002 softap stopap");
            }catch (IOException e) {
                 // TODO Auto-generated catch block
                  e.printStackTrace();
              }
			SystemProperties.set(PROP_RSDB_ENABLE, "0");
           }
         }
        return false;
    }

    private BluetoothProfile.ServiceListener mProfileServiceListener =
            new BluetoothProfile.ServiceListener() {
                public void onServiceConnected(int profile, BluetoothProfile proxy) {
                    mBluetoothPan.set((BluetoothPan) proxy);
                }

                public void onServiceDisconnected(int profile) {
                    mBluetoothPan.set(null);
                }
            };

    private void updateState() {
        String[] available = mCm.getTetherableIfaces();
        String[] tethered = mCm.getTetheredIfaces();
        String[] errored = mCm.getTetheringErroredIfaces();
        updateState(available, tethered, errored);
    }

    private void updateState(String[] available, String[] tethered,
                             String[] errored) {
        updateUsbState(available, tethered, errored);
        updateBluetoothState(available, tethered, errored);
    }


    private void updateUsbState(String[] available, String[] tethered,
                                String[] errored) {
        boolean usbAvailable = mUsbConnected && !mMassStorageActive;
        int usbError = ConnectivityManager.TETHER_ERROR_NO_ERROR;
        for (String s : available) {
            for (String regex : mUsbRegexs) {
                if (s.matches(regex)) {
                    if (usbError == ConnectivityManager.TETHER_ERROR_NO_ERROR) {
                        usbError = mCm.getLastTetherError(s);
                    }
                }
            }
        }
        boolean usbTethered = false;
        for (String s : tethered) {
            for (String regex : mUsbRegexs) {
                if (s.matches(regex)) usbTethered = true;
            }
        }
        boolean usbErrored = false;
        for (String s : errored) {
            for (String regex : mUsbRegexs) {
                if (s.matches(regex)) usbErrored = true;
            }
        }

        if (usbTethered) {
            mUsbTether.setSummary(R.string.usb_tethering_active_subtext);
            mUsbTether.setEnabled(!mDataSaverEnabled);
            mUsbTether.setChecked(true);
        } else if (usbAvailable) {
            if (usbError == ConnectivityManager.TETHER_ERROR_NO_ERROR) {
                mUsbTether.setSummary(R.string.usb_tethering_available_subtext);
            } else {
                mUsbTether.setSummary(R.string.usb_tethering_errored_subtext);
            }
            mUsbTether.setEnabled(!mDataSaverEnabled);
            mUsbTether.setChecked(false);
        } else if (usbErrored) {
            mUsbTether.setSummary(R.string.usb_tethering_errored_subtext);
            mUsbTether.setEnabled(false);
            mUsbTether.setChecked(false);
        } else if (mMassStorageActive) {
            mUsbTether.setSummary(R.string.usb_tethering_storage_active_subtext);
            mUsbTether.setEnabled(false);
            mUsbTether.setChecked(false);
        } else {
            mUsbTether.setSummary(R.string.usb_tethering_unavailable_subtext);
            mUsbTether.setEnabled(false);
            mUsbTether.setChecked(false);
        }
    }

    private void updateBluetoothState(String[] available, String[] tethered,
                                      String[] errored) {
        boolean bluetoothErrored = false;
        for (String s : errored) {
            for (String regex : mBluetoothRegexs) {
                if (s.matches(regex)) bluetoothErrored = true;
            }
        }

        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null) {
            return;
        }
        int btState = adapter.getState();
        if (btState == BluetoothAdapter.STATE_TURNING_OFF) {
            mBluetoothTether.setEnabled(false);
            mBluetoothTether.setSummary(R.string.bluetooth_turning_off);
        } else if (btState == BluetoothAdapter.STATE_TURNING_ON) {
            mBluetoothTether.setEnabled(false);
            mBluetoothTether.setSummary(R.string.bluetooth_turning_on);
        } else {
            BluetoothPan bluetoothPan = mBluetoothPan.get();
            if (btState == BluetoothAdapter.STATE_ON && bluetoothPan != null
                    && bluetoothPan.isTetheringOn()) {
                mBluetoothTether.setChecked(true);
                mBluetoothTether.setEnabled(!mDataSaverEnabled);
                int bluetoothTethered = bluetoothPan.getConnectedDevices().size();
                if (bluetoothTethered > 1) {
                    String summary = getString(
                            R.string.bluetooth_tethering_devices_connected_subtext,
                            bluetoothTethered);
                    mBluetoothTether.setSummary(summary);
                } else if (bluetoothTethered == 1) {
                    mBluetoothTether.setSummary(
                            R.string.bluetooth_tethering_device_connected_subtext);
                } else if (bluetoothErrored) {
                    mBluetoothTether.setSummary(R.string.bluetooth_tethering_errored_subtext);
                } else {
                    mBluetoothTether.setSummary(R.string.bluetooth_tethering_available_subtext);
                }
            } else {
                mBluetoothTether.setEnabled(!mDataSaverEnabled);
                mBluetoothTether.setChecked(false);
                mBluetoothTether.setSummary(R.string.bluetooth_tethering_off_subtext);
            }
        }
    }

    public void onClick(DialogInterface dialogInterface, int button) {
        if (button == DialogInterface.BUTTON_POSITIVE) {
            mWifiConfig = mDialog.getConfig();
            if (mWifiConfig != null) {
                /**
                 * if soft AP is stopped, bring up
                 * else restart with new config
                 * TODO: update config on a running access point when framework support is added
                 */
                if (mWifiManager.getWifiApState() == WifiManager.WIFI_AP_STATE_ENABLED) {
                    Log.d("TetheringSettings",
                            "Wifi AP config changed while enabled, stop and restart");
                    mRestartWifiApAfterConfigChange = true;
                    mCm.stopTethering(TETHERING_WIFI);
                }
                mWifiManager.setWifiApConfiguration(mWifiConfig);
                int index = WifiApDialog.getSecurityTypeIndex(mWifiConfig);
                mCreateNetwork.setSummary(String.format(getActivity().getString(CONFIG_SUBTEXT),
                        mWifiConfig.SSID,
                        mSecurityType[index]));
            }
        }
    }

    private static final class OnStartTetheringCallback extends
            ConnectivityManager.OnStartTetheringCallback {
        final WeakReference<HotPotFragment> mTetherSettings;

        OnStartTetheringCallback(HotPotFragment settings) {
            mTetherSettings = new WeakReference<HotPotFragment>(settings);
        }

        @Override
        public void onTetheringStarted() {
            update();
        }

        @Override
        public void onTetheringFailed() {
            update();
        }

        private void update() {
            HotPotFragment settings = mTetherSettings.get();
            if (settings != null) {
                settings.updateState();
            }
        }
    }

}
