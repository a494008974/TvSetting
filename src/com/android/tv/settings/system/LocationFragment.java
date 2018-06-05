/*
 * Copyright (C) 2015 The Android Open Source Project
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
 * limitations under the License
 */

package com.android.tv.settings.system;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.UserManager;
import android.provider.Settings;
import android.support.v17.preference.LeanbackPreferenceFragment;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceGroup;
import android.support.v7.preference.PreferenceScreen;
import android.text.TextUtils;
import android.util.Log;

import com.android.settingslib.location.RecentLocationApps;
import com.android.tv.settings.R;
import com.android.tv.settings.device.apps.AppManagementFragment;
import com.mylove.tv.rksetting.BaseLeanbackPreferenceFragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class LocationFragment extends BaseLeanbackPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String TAG = "LocationFragment";

    private static final String LOCATION_MODE_WIFI = "wifi";
    private static final String LOCATION_MODE_OFF = "off";

    private static final String KEY_LOCATION_MODE = "locationMode";

    private static final String MODE_CHANGING_ACTION =
            "com.android.settings.location.MODE_CHANGING";
    private static final String CURRENT_MODE_KEY = "CURRENT_MODE";
    private static final String NEW_MODE_KEY = "NEW_MODE";

    private ListPreference mLocationMode;

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "Received location mode change intent: " + intent);
            }
            refreshLocationMode();
        }
    };

    public static LocationFragment newInstance() {
        return new LocationFragment();
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        final Context themedContext = getPreferenceManager().getContext();
        final PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(
                themedContext);
        screen.setTitle(R.string.system_location);

        mLocationMode = new ListPreference(themedContext);
        screen.addPreference(mLocationMode);
        mLocationMode.setKey(KEY_LOCATION_MODE);
        mLocationMode.setPersistent(false);
        mLocationMode.setTitle(R.string.location_status);
        mLocationMode.setDialogTitle(R.string.location_status);
        mLocationMode.setSummary("%s");
        mLocationMode.setEntries(new CharSequence[] {
                getString(R.string.location_mode_wifi_description),
                getString(R.string.off)
        });
        mLocationMode.setEntryValues(new CharSequence[] {
                LOCATION_MODE_WIFI,
                LOCATION_MODE_OFF
        });
        mLocationMode.setOnPreferenceChangeListener(this);

        final UserManager um = UserManager.get(getContext());
        mLocationMode.setEnabled(!um.hasUserRestriction(UserManager.DISALLOW_SHARE_LOCATION));

        final PreferenceCategory recentRequests = new PreferenceCategory(themedContext);
        screen.addPreference(recentRequests);
        recentRequests.setTitle(R.string.location_category_recent_location_requests);

        List<RecentLocationApps.Request> recentLocationRequests =
                new RecentLocationApps(themedContext).getAppList();
        List<Preference> recentLocationPrefs = new ArrayList<>(recentLocationRequests.size());
        for (final RecentLocationApps.Request request : recentLocationRequests) {
            Preference pref = new Preference(themedContext);
            pref.setIcon(request.icon);
            pref.setTitle(request.label);
            if (request.isHighBattery) {
                pref.setSummary(R.string.location_high_battery_use);
            } else {
                pref.setSummary(R.string.location_low_battery_use);
            }
            pref.setFragment(AppManagementFragment.class.getName());
            AppManagementFragment.prepareArgs(pref.getExtras(), request.packageName);
            recentLocationPrefs.add(pref);
        }

        if (recentLocationRequests.size() > 0) {
            addPreferencesSorted(recentLocationPrefs, recentRequests);
        } else {
            // If there's no item to display, add a "No recent apps" item.
            Preference banner = new Preference(themedContext);
            banner.setTitle(R.string.location_no_recent_apps);
            banner.setSelectable(false);
            recentRequests.addPreference(banner);
        }

        // TODO: are location services relevant on TV?

        setPreferenceScreen(screen);
    }

    @Override
    public void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter();
        filter.addAction(LocationManager.MODE_CHANGED_ACTION);
        getActivity().registerReceiver(mReceiver, filter);
        refreshLocationMode();
    }

    @Override
    public void onStop() {
        super.onStop();
        try {
            getActivity().unregisterReceiver(mReceiver);
        } catch (RuntimeException e) {
            // Ignore exceptions caused by race condition
        }
    }

    private void addPreferencesSorted(List<Preference> prefs, PreferenceGroup container) {
        // If there's some items to display, sort the items and add them to the container.
        Collections.sort(prefs, new Comparator<Preference>() {
            @Override
            public int compare(Preference lhs, Preference rhs) {
                return lhs.getTitle().toString().compareTo(rhs.getTitle().toString());
            }
        });
        for (Preference entry : prefs) {
            container.addPreference(entry);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (TextUtils.equals(preference.getKey(), KEY_LOCATION_MODE)) {
            int mode = Settings.Secure.LOCATION_MODE_OFF;
            if (TextUtils.equals((CharSequence) newValue, LOCATION_MODE_WIFI)) {
                // TODO
                // com.google.android.gms/com.google.android.location.network.ConfirmAlertActivity
                // pops up when we turn this on.
                mode = Settings.Secure.LOCATION_MODE_HIGH_ACCURACY;
            } else if (TextUtils.equals((CharSequence) newValue, LOCATION_MODE_OFF)) {
                mode = Settings.Secure.LOCATION_MODE_OFF;
            } else {
                Log.wtf(TAG, "Tried to set unknown location mode!");
            }

            int currentMode = Settings.Secure.getInt(getActivity().getContentResolver(),
                    Settings.Secure.LOCATION_MODE, Settings.Secure.LOCATION_MODE_OFF);
            Intent intent = new Intent(MODE_CHANGING_ACTION);
            intent.putExtra(CURRENT_MODE_KEY, currentMode);
            intent.putExtra(NEW_MODE_KEY, mode);
            getActivity().sendBroadcast(intent, android.Manifest.permission.WRITE_SECURE_SETTINGS);
            Settings.Secure.putInt(getActivity().getContentResolver(),
                    Settings.Secure.LOCATION_MODE, mode);

            refreshLocationMode();
        }
        return true;
    }

    private void refreshLocationMode() {
        if (mLocationMode == null) {
            return;
        }
        final int mode = Settings.Secure.getInt(getActivity().getContentResolver(),
                Settings.Secure.LOCATION_MODE, Settings.Secure.LOCATION_MODE_OFF);
        if (mode == Settings.Secure.LOCATION_MODE_HIGH_ACCURACY) {
            mLocationMode.setValue(LOCATION_MODE_WIFI);
        } else if (mode == Settings.Secure.LOCATION_MODE_OFF) {
            mLocationMode.setValue(LOCATION_MODE_OFF);
        } else {
            Log.d(TAG, "Unknown location mode: " + mode);
        }
    }
}
