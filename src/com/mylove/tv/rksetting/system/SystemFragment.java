package com.mylove.tv.rksetting.system;

import android.os.Bundle;
import android.support.v17.preference.LeanbackPreferenceFragment;
import com.android.tv.settings.R;
import com.mylove.tv.rksetting.BaseLeanbackPreferenceFragment;

public class SystemFragment extends BaseLeanbackPreferenceFragment {
	public static SystemFragment newInstance() {
        return new SystemFragment();
    }
	
	public void onCreatePreferences(Bundle savedInstanceState, String rootKey){
		setPreferencesFromResource(R.xml.system, null);
	}
}
