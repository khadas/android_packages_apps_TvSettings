package com.android.tv.settings.system;

import com.android.tv.settings.R;

import com.android.tv.settings.TvSettingsActivity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v17.preference.LeanbackPreferenceFragment;
import android.support.v17.preference.LeanbackSettingsFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Space;

public class PowerKeyActivity extends TvSettingsActivity {

    private static final String PREFERENCE_FRAGMENT_TAG =
            "android.support.v17.preference.LeanbackSettingsFragment.PREFERENCE_FRAGMENT";
    @Override
    protected Fragment createSettingsFragment() {
        return SettingsFragment.newInstance();
    }

    public static class SettingsFragment extends LeanbackPreferenceFragment {

        public static SettingsFragment newInstance() {
            return new SettingsFragment();
        }
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.device_info_settings, null);
        }
         @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            final View v = inflater.inflate(R.layout.leanback_settings_fragment, container, false);
            return v;
        }

         @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            Fragment fragment = PowermanagerFragment.newInstance();
            final FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
            final Fragment preferenceFragment =
                getChildFragmentManager().findFragmentByTag(PREFERENCE_FRAGMENT_TAG);
            if (preferenceFragment != null && !preferenceFragment.isHidden()) {
                if (android.os.Build.VERSION.SDK_INT < 23) {
                    transaction.add(R.id.settings_preference_fragment_container, new DummyFragment());
                }
                transaction.remove(preferenceFragment);
            }
            transaction
                    .add(R.id.settings_dialog_container, fragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    public static class DummyFragment extends Fragment {

        @Override
        public @Nullable View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            final View v = new Space(inflater.getContext());
            v.setVisibility(View.GONE);
            return v;
        }
    }
}

