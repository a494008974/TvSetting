package com.mylove.tv.rksetting;

import android.os.Bundle;
import android.support.v17.preference.LeanbackPreferenceFragment;
import com.android.tv.settings.R;

public class SystemFragment extends LeanbackPreferenceFragment {
	public static SystemFragment newInstance() {
        return new SystemFragment();
    }
	
	public void onCreatePreferences(Bundle savedInstanceState, String rootKey){
		setPreferencesFromResource(R.xml.system, null);
	}
}