/*
 * Copyright 2014 Emma Guy
 * Modifications Copyright(C) 2015 Fred Grott(GrottWorkShop)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.shareme.gwscleanstatusbar;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.github.shareme.gwscleanstatusbar.prefs.TimePreference;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initSwitch();

        getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();
    }

    private void initSwitch() {
        Switch masterSwitch = new Switch(this);
        masterSwitch.setChecked(CleanStatusBarService.isRunning());
        masterSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                Intent service = new Intent(MainActivity.this, CleanStatusBarService.class);
                if (b) {
                    startService(service);
                } else {
                    stopService(service);
                }
            }
        });

        final ActionBar bar = getActionBar();
        final ActionBar.LayoutParams lp = new ActionBar.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.RIGHT | Gravity.CENTER_VERTICAL;
        lp.rightMargin = getResources().getDimensionPixelSize(R.dimen.master_switch_margin_right);
        if (bar != null) {
            bar.setCustomView(masterSwitch, lp);
        }
        if (bar != null) {
            bar.setDisplayShowCustomEnabled(true);
        }
    }

    public static int getAPIValue(Context context, SharedPreferences prefs) {
        String apiValue = prefs.getString(context.getString(R.string.key_api_level), "");
        if (!TextUtils.isEmpty(apiValue)) {
            return Integer.valueOf(apiValue);
        }

        return Build.VERSION_CODES.LOLLIPOP;
    }

    public static class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.prefs);

            initSummary();
            updateEnableKitKatGradientOption(getPreferenceManager().getSharedPreferences());
            updateTimePreference();
        }

        @Override
        public void onResume() {
            super.onResume();

            getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause() {
            getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);

            super.onPause();
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            updatePrefsSummary(findPreference(key));

            if (CleanStatusBarService.isRunning()) {
                Intent service = new Intent(getActivity(), CleanStatusBarService.class);
                getActivity().startService(service);
            }

            if (key.equals(getString(R.string.key_api_level))) {
                updateEnableKitKatGradientOption(sharedPreferences);
            } else if (key.equals(getString(R.string.key_use_24_hour_format))) {
                updateTimePreference();
            }
        }

        private void updateTimePreference() {
            CheckBoxPreference pref = (CheckBoxPreference) findPreference(getString(R.string.key_use_24_hour_format));

            TimePreference timePreference = (TimePreference) findPreference(getString(R.string.key_clock_time));
            timePreference.setIs24HourFormat(pref.isChecked());

            updatePrefsSummary(timePreference);
        }

        private void updateEnableKitKatGradientOption(SharedPreferences sharedPreferences) {
            boolean isKitKat = getAPIValue(getActivity(), sharedPreferences) == Build.VERSION_CODES.KITKAT;
            findPreference(getString(R.string.key_kit_kat_gradient)).setEnabled(isKitKat);
        }

        protected void initSummary() {
            for (int i = 0; i < getPreferenceScreen().getPreferenceCount(); i++) {
                initPrefsSummary(getPreferenceScreen().getPreference(i));
            }
        }

        protected void initPrefsSummary(Preference p) {
            if (p instanceof PreferenceCategory) {
                PreferenceCategory cat = (PreferenceCategory) p;
                for (int i = 0; i < cat.getPreferenceCount(); i++) {
                    initPrefsSummary(cat.getPreference(i));
                }
            } else {
                updatePrefsSummary(p);
            }
        }

        protected void updatePrefsSummary(Preference pref) {
            if (pref == null) {
                return;
            }

            if (pref instanceof ListPreference) {
                ListPreference lst = (ListPreference) pref;
                String currentValue = lst.getValue();

                int index = lst.findIndexOfValue(currentValue);
                CharSequence[] entries = lst.getEntries();
                CharSequence[] entryValues = lst.getEntryValues();
                if (index >= 0 && index < entries.length) {
                    // Show info explaining that the small letters e.g. 3G/LTE etc are only shown when WiFi is off - this is standard Android behaviour
                    boolean currentValueIsOffOrEmpty = currentValue.equals(entryValues[0]) || currentValue.equals(entryValues[1]);
                    if (pref.getKey().equals(getString(R.string.key_signal_3g)) && !currentValueIsOffOrEmpty) {
                        pref.setSummary(entries[index] + " - " + getString(R.string.network_icon_info));
                    } else {
                        pref.setSummary(entries[index]);
                    }
                }
            } else if (pref instanceof TimePreference) {
                if (pref.getKey().equals(getString(R.string.key_clock_time))) {
                    String time = getPreferenceManager().getSharedPreferences().getString(getString(R.string.key_clock_time), TimePreference.DEFAULT_TIME_VALUE);
                    pref.setSummary(time);
                }
            }
        }
    }
}
