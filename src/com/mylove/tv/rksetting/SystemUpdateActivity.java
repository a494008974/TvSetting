package com.mylove.tv.rksetting;

import android.app.Fragment;

import com.android.tv.settings.BaseSettingsFragment;
import com.android.tv.settings.MainFragment;
import com.android.tv.settings.TvSettingsActivity;
import com.android.tv.settings.MainSettings.SettingsFragment;

public class SystemUpdateActivity extends TvSettingsActivity{
	@Override
    protected Fragment createSettingsFragment() {
        return SystemUpdateFragment.newInstance();
    }
}
