package com.android.tv.settings.funkey;
import android.content.Context;
import android.support.v14.preference.SwitchPreference;
import android.support.v17.preference.LeanbackPreferenceFragment;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import com.android.tv.settings.R;
import android.util.Log;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.preference.PreferenceGroup;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.support.v7.preference.EditTextPreference;
import android.os.SystemProperties;
import android.support.annotation.Keep;
import android.text.TextUtils;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import com.android.tv.settings.RadioPreference;

@Keep
public class FunkeyFragment extends LeanbackPreferenceFragment implements Preference.OnPreferenceClickListener {
    private static final String TAG = "FunkeyFragment";
    static final String KEY_FUNCTION = "function_key";
    private static final String SYS_ADC_TABLE = "sys/class/adc_keypad/table";
    static final String KEY_FUN_KEY_CATEGORY = "fun_key_category";
    private PreferenceCategory mFunkeyCategoryPref;
    private PreferenceScreen mPreferenceScreen;
    private EditTextPreference mfunctionkeyPref;
    private Context mContext;
    private static final int[] funkeyValue = {
       102,
       158,
       115,
       114
    };
    private static final String[] funkeyPrefix = {
       "fun_key_0",
       "fun_key_1",
       "fun_key_2",
       "fun_key_3",
    };
    public static FunkeyFragment newInstance() {
        return new FunkeyFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
	super.onResume();
    }

    @Override
    public void onPause() {
	super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.funkey, null);
        mPreferenceScreen = getPreferenceScreen();
        mFunkeyCategoryPref =
        (PreferenceCategory) findPreference(KEY_FUN_KEY_CATEGORY);
        createFormatPreferences();
        mfunctionkeyPref = (EditTextPreference)findPreference(KEY_FUNCTION);
	mfunctionkeyPref.setSummary(Integer.toString(SystemProperties.getInt("persist.sys.func.key.action", 3)));
	mfunctionkeyPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener(){
	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue){
	    mfunctionkeyPref.setSummary(newValue.toString());
            setFunKey(Integer.parseInt(newValue.toString()));
	    return false;
	    }
	});

    }

    private void createFormatPreferences() {
        int index = funkeyValueToIndex();
        String[] funkeyStr = mContext.getResources().getStringArray(R.array.entries_funkey_value);
        for (int i = 0; i < funkeyStr.length; i++) {
            RadioPreference pref = new RadioPreference(mContext);
            pref.setPersistent(false);
            pref.setTitle(funkeyStr[i]);
            pref.setKey(funkeyPrefix[i]);
            pref.setRadioGroup(KEY_FUN_KEY_CATEGORY);
            pref.setLayoutResource(R.layout.preference_reversed_widget);
            pref.setChecked(index == i ? true:false);
            pref.setEnabled(true);
            mFunkeyCategoryPref.addPreference(pref);
        }
    }

    public static int funkeyValueToIndex() {
	int keyvalue = SystemProperties.getInt("persist.sys.func.key.action", 3);
	if(keyvalue == 102) {
		return 0;
	}else if(keyvalue == 158) {
		return 1;
	}else if(keyvalue == 115) {
		return 2;
	}else if(keyvalue == 114) {
		return 3;
	}
	return -1;
	}

    public static int setFunKey(int value) {
	Log.d(TAG,"setFanKey: " + value);
	File file = new File(SYS_ADC_TABLE);
	if((file == null) || !file.exists()){
	    Log.e(TAG, "" + SYS_ADC_TABLE + " no exist");
	    return -1;
	}
	try {
	    FileOutputStream fout = new FileOutputStream(file);
	    PrintWriter pWriter = new PrintWriter(fout);
	    pWriter.println("home:"+String.valueOf(value)+":2:108:40");
	    pWriter.flush();
	    pWriter.close();
	    fout.close();
	} catch (IOException e) {
	    Log.e(TAG, "setFanKey ERR: " + e);
	    return -1;
	}
	SystemProperties.set("persist.sys.func.key.action", Integer.toString(value));
	return 0;
   }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
       if (preference instanceof RadioPreference) {
            final RadioPreference pref = (RadioPreference) preference;
            pref.clearOtherRadioPreferences(mFunkeyCategoryPref);
            String key = pref.getKey();
            if (pref.isChecked()) {
               if (TextUtils.equals(key, funkeyPrefix[0])) {
                  setFunKey(funkeyValue[0]);
               } else if (TextUtils.equals(key, funkeyPrefix[1])) {
                  setFunKey(funkeyValue[1]);
               } else if (TextUtils.equals(key, funkeyPrefix[2])) {
                  setFunKey(funkeyValue[2]);
               } else if (TextUtils.equals(key, funkeyPrefix[3])) {
                  setFunKey(funkeyValue[3]);
               }
            }
            pref.setChecked(true);
            mfunctionkeyPref.setSummary(Integer.toString(SystemProperties.getInt("persist.sys.func.key.action", 3)));
        }
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        return false;
    }

}

