package com.reddyetwo.hashmypass.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;

import com.reddyetwo.hashmypass.app.data.Preferences;


public class SettingsFragment extends PreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    private Preference mRememberMasterKeyPreference;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceManager().setSharedPreferencesName(Preferences
                .PREFS_NAME);

        addPreferencesFromResource(R.xml.settings);

        mRememberMasterKeyPreference = (Preference) findPreference(
                getString(R.string.settings_key_remember_master_key));
    }

    @Override
    public void onResume() {
        super.onResume();

        getPreferenceManager().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
        updateRememberMasterKeySummary();
    }

    private void updateRememberMasterKeySummary() {
        int masterKeyMins = Preferences.getRememberMasterKeyMins(getActivity());
        if (masterKeyMins == 0) {
            setSummary(mRememberMasterKeyPreference,
                    R.string.settings_summary_remember_master_key_never);
        } else if (masterKeyMins < 60) {
            setSummary(mRememberMasterKeyPreference,
                    R.string.settings_summary_remember_master_key_minutes,
                    masterKeyMins);
        } else {
            setSummary(mRememberMasterKeyPreference,
                    R.string.settings_summary_remember_master_key_hours,
                    masterKeyMins / 60);
        }
    }

    private void setSummary(Preference preference, int summaryId,
                            Object... args) {
        preference.setSummary(getString(summaryId, args));
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key) {
        if (key.equals(getString(R.string.settings_key_remember_master_key))) {
            updateRememberMasterKeySummary();
        }
    }
}
