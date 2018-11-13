package com.android.tv.settings.connectivity;

import com.android.tv.settings.BaseInputActivity;
import com.android.tv.settings.R;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.AuthAlgorithm;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.*;

import android.widget.Button;
import android.view.View.OnClickListener;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.net.ConnectivityManager;
import android.support.v7.preference.Preference;
import android.os.Handler;


import android.content.Intent;
import android.content.BroadcastReceiver;


import static android.net.ConnectivityManager.TETHERING_WIFI;

import java.lang.ref.WeakReference;


import java.nio.charset.Charset;

public class HotpotDialogActivity extends BaseInputActivity implements View.OnClickListener,
        TextWatcher, AdapterView.OnItemSelectedListener {

    static final int BUTTON_SUBMIT = DialogInterface.BUTTON_POSITIVE;
    private static final String DATA_SAVER_FOOTER = "disabled_on_data_saver";

//  private final DialogInterface.OnClickListener mListener;

    public static final int OPEN_INDEX = 0;
    public static final int WPA2_INDEX = 1;

    private String[] mSecurityType;

    private boolean mDataSaverEnabled;


    private ConnectivityManager mCm;
    private OnStartTetheringCallback mStartTetheringCallback;
    private Handler mHandler = new Handler();

    private Button my_save_button;
    private Button my_cancel_button;
    private Preference mCreateNetwork;
    private Spinner security;

    private EditText mSsid;
    private int mSecurityTypeIndex = OPEN_INDEX;
    private EditText mPassword;
    private int mBandIndex = OPEN_INDEX;

    WifiConfiguration mWifiConfig;
    WifiManager mWifiManager;
    private Context mContext;

    private static final String TAG = "WifiApDialog";
    private static final String WIFI_AP_SSID_AND_SECURITY = "wifi_ap_ssid_and_security";

    @Override
    public void init() {
        // TODO Auto-generated method stub

    }

    @Override
    public int getContentLayoutRes() {
        // TODO Auto-generated method stub
        return R.layout.wifi_ap_dialog_my;
    }

    @Override
    public String getInputTitle() {
        // TODO Auto-generated method stub
        return "Hotpot Setup";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        security = (Spinner) findViewById(R.id.security);

        Spinner mSecurity = ((Spinner) findViewById(R.id.security));
        final Spinner mChannel = (Spinner) findViewById(R.id.choose_channel);

        mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        mWifiConfig = mWifiManager.getWifiApConfiguration();


        mStartTetheringCallback = new OnStartTetheringCallback(this);

        mSsid = (EditText) findViewById(R.id.ssid);
        mSsid.requestFocus();
        mPassword = (EditText) findViewById(R.id.password);
        my_save_button = (Button) findViewById(R.id.OK);
        my_cancel_button = (Button) findViewById(R.id.NO);

        my_save_button.setOnClickListener(new myListener());
        my_cancel_button.setOnClickListener(new myListener());

        if (mWifiConfig != null) {
            mSsid.setText(mWifiConfig.SSID);
            if (mWifiConfig.apBand == 0) {
                mBandIndex = 0;
            } else {
                mBandIndex = 1;
            }

            security.setSelection(getSecurityTypeIndex(mWifiConfig));
            if (getSecurityTypeIndex(mWifiConfig) == WPA2_INDEX) {
                mPassword.setText(mWifiConfig.preSharedKey);
            }
        }

        mChannel.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    boolean mInit = true;

                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int position,
                                               long id) {
                        if (!mInit) {
                            mBandIndex = position;
                            mWifiConfig.apBand = mBandIndex;
                            Log.i(TAG, "config on channelIndex : " + mBandIndex + " Band: " +
                                    mWifiConfig.apBand);
                        } else {
                            mInit = false;
                            mChannel.setSelection(mBandIndex);
                        }

                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {

                    }
                }
        );

        mSsid.addTextChangedListener(this);
        mPassword.addTextChangedListener(this);
        ((CheckBox) findViewById(R.id.show_password)).setOnClickListener(this);
        mSecurity.setOnItemSelectedListener(this);

        validate();

    }

    class myListener implements OnClickListener {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.OK: {
                    mWifiConfig = getConfig();
                    if (mWifiConfig != null) {
                        if (mWifiManager.getWifiApState() == WifiManager.WIFI_AP_STATE_ENABLED) {
                            Log.d("TetheringSettings", "Wifi AP config changed while enabled, stop and restart");
                        }
                        mWifiManager.setWifiApConfiguration(mWifiConfig);
                        HotPotFragment.mRestartWifiApAfterConfigChange = true;
                        Intent intent = new Intent();
                        intent.setAction(ConnectivityManager.ACTION_TETHER_STATE_CHANGED);
                        sendBroadcast(intent);
                        finish();
                    }
                }
                break;
                case R.id.NO:
                    finish();
                    break;
            }
        }
    }

    private void validate() {
        String mSsidString = mSsid.getText().toString();
        if ((mSsid != null && mSsid.length() == 0)
                || ((mSecurityTypeIndex == WPA2_INDEX) && mPassword.length() < 8)
                || (mSsid != null &&
                Charset.forName("UTF-8").encode(mSsidString).limit() > 32)) {
            my_save_button.setEnabled(false);
        } else {
            my_save_button.setEnabled(true);
        }
    }

    public static int getSecurityTypeIndex(WifiConfiguration wifiConfig) {
        if (wifiConfig.allowedKeyManagement.get(KeyMgmt.WPA2_PSK)) {
            return WPA2_INDEX;
        }
        return OPEN_INDEX;
    }

    public WifiConfiguration getConfig() {

        WifiConfiguration config = new WifiConfiguration();

        /**
         * TODO: SSID in WifiConfiguration for soft ap
         * is being stored as a raw string without quotes.
         * This is not the case on the client side. We need to
         * make things consistent and clean it up
         */
        config.SSID = mSsid.getText().toString();

        config.apBand = mBandIndex;

        switch (mSecurityTypeIndex) {
            case OPEN_INDEX:
                config.allowedKeyManagement.set(KeyMgmt.NONE);
                return config;

            case WPA2_INDEX:
                config.allowedKeyManagement.set(KeyMgmt.WPA2_PSK);
                config.allowedAuthAlgorithms.set(AuthAlgorithm.OPEN);
                if (mPassword.length() != 0) {
                    String password = mPassword.getText().toString();
                    config.preSharedKey = password;
                }
                return config;
        }
        return null;
    }

    public boolean checkIfSupportDualBand() {
        File file = new File("/data/wifi_chip");
        BufferedReader reader = null;

        try {
            reader = new BufferedReader(new FileReader(file));
            String tempString = null;
            while ((tempString = reader.readLine()) != null) {
                Log.d(TAG, "Get wifi chip name: " + tempString);
                if (tempString.contains("AP6234") || tempString.contains("AP6330")
                        || tempString.contains("AP6335") || tempString.contains("AP6354")
                        || tempString.contains("AP6441") || tempString.contains("AP6356S")
                        || tempString.contains("RTL8822BS") || tempString.contains("RTL8822BU")) {
                    reader.close();
                    return true;
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                }
            }
        }

        return false;
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mPassword.setInputType(
                InputType.TYPE_CLASS_TEXT |
                        (((CheckBox) findViewById(R.id.show_password)).isChecked() ?
                                InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD :
                                InputType.TYPE_TEXT_VARIATION_PASSWORD));
    }

    public void onClick(View view) {
        mPassword.setInputType(
                InputType.TYPE_CLASS_TEXT | (((CheckBox) view).isChecked() ?
                        InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD :
                        InputType.TYPE_TEXT_VARIATION_PASSWORD));
    }

    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    public void afterTextChanged(Editable editable) {
        validate();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        mSecurityTypeIndex = position;
        showSecurityFields();
        validate();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    private void showSecurityFields() {
        if (mSecurityTypeIndex == OPEN_INDEX) {
            findViewById(R.id.fields).setVisibility(View.GONE);
            return;
        }
        findViewById(R.id.fields).setVisibility(View.VISIBLE);
    }

    private static final class OnStartTetheringCallback extends
            ConnectivityManager.OnStartTetheringCallback {
        final WeakReference<HotpotDialogActivity> mTetherSettings;

        OnStartTetheringCallback(HotpotDialogActivity settings) {
            mTetherSettings = new WeakReference<HotpotDialogActivity>(settings);
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

        }
    }
}
