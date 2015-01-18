/*
 * Copyright 2014 Red Dye No. 2
 *
 * This file is part of Twik.
 *
 * Twik is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Twik is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Twik.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.reddyetwo.hashmypass.app;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import com.reddyetwo.hashmypass.app.data.Preferences;


public class SettingsFragment extends PreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    private Preference mRememberMasterKeyPreference;
    private Preference mCopyToClipboardPreference;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceManager().setSharedPreferencesName(Preferences.PREFS_NAME);

        addPreferencesFromResource(R.xml.settings);

        mRememberMasterKeyPreference = findPreference(
                getString(R.string.settings_key_remember_master_key));
        mCopyToClipboardPreference = findPreference(
                getString(R.string.settings_key_copy_to_clipboard));
    }

    @Override
    public void onResume() {
        super.onResume();

        getPreferenceManager().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
        updateRememberMasterKeySummary();
        updateCopyToClipboardSummary();
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceManager().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
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

    private void updateCopyToClipboardSummary() {
        boolean copyToClipboard = Preferences.getCopyToClipboard(getActivity());
        if (copyToClipboard) {
            setSummary(mCopyToClipboardPreference,
                    R.string.settings_summary_copy_to_clipboard_enabled);
        } else {
            setSummary(mCopyToClipboardPreference,
                    R.string.settings_summary_copy_to_clipboard_disabled);
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
        } else if (key
                .equals(getString(R.string.settings_key_copy_to_clipboard))) {
            updateCopyToClipboardSummary();
        }
    }
}
