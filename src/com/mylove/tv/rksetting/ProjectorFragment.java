package com.mylove.tv.rksetting;


import android.os.Bundle;
import android.support.v17.preference.LeanbackPreferenceFragment;
import com.android.tv.settings.R;

public class ProjectorFragment extends LeanbackPreferenceFragment {
	public static ProjectorFragment newInstance() {
        return new ProjectorFragment();
    }
	
	public void onCreatePreferences(Bundle savedInstanceState, String rootKey){
		setPreferencesFromResource(R.xml.sound, null);
	}
}
