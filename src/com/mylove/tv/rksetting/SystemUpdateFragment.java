package com.mylove.tv.rksetting;

import android.os.Bundle;
import android.support.v17.preference.LeanbackPreferenceFragment;
import com.android.tv.settings.R;

public class SystemUpdateFragment extends LeanbackPreferenceFragment {
	public static SystemUpdateFragment newInstance() {
        return new SystemUpdateFragment();
    }
	
	public void onCreatePreferences(Bundle savedInstanceState, String rootKey){
		setPreferencesFromResource(R.xml.main_prefs, null);
	}
}
