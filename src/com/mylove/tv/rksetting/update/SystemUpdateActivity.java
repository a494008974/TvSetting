package com.mylove.tv.rksetting.update;

import android.app.Fragment;

import com.android.tv.settings.BaseSettingsFragment;
import com.android.tv.settings.MainFragment;
import com.android.tv.settings.TvSettingsActivity;
import com.android.tv.settings.MainSettings.SettingsFragment;
import com.mylove.tv.rksetting.system.SystemFragment;

public class SystemUpdateActivity extends TvSettingsActivity{
	@Override
    protected Fragment createSettingsFragment() {
        return SettingsFragment.newInstance();
    }
	
	public static class SettingsFragment extends BaseSettingsFragment {

        public static SettingsFragment newInstance() {
            return new SettingsFragment();
        }

        @Override
        public void onPreferenceStartInitialScreen() {
            final SystemUpdateFragment fragment = SystemUpdateFragment.newInstance();
            startPreferenceFragment(fragment);
        }
    }
}
