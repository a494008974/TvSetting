package com.mylove.tv.rksetting.update;

import android.os.Bundle;
import android.support.v17.preference.LeanbackPreferenceFragment;
import com.android.tv.settings.R;
import com.mylove.tv.rksetting.BaseLeanbackPreferenceFragment;

public class SystemUpdateFragment extends BaseLeanbackPreferenceFragment {
	public static SystemUpdateFragment newInstance() {
        return new SystemUpdateFragment();
    }
	
	public void onCreatePreferences(Bundle savedInstanceState, String rootKey){
		setPreferencesFromResource(R.xml.update, null);
	}
}
