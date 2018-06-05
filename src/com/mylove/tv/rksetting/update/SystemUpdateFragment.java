package com.mylove.tv.rksetting.update;

import android.content.Intent;
import android.os.Bundle;
import android.support.v17.preference.LeanbackPreferenceFragment;
import android.text.TextUtils;

import com.android.tv.settings.R;
import com.mylove.tv.rksetting.BaseLeanbackPreferenceFragment;
import android.support.v7.preference.Preference;

public class SystemUpdateFragment extends BaseLeanbackPreferenceFragment {
	
	private static final String KEY_LOCAL_UPDATE = "local_update";
    private static final String KEY_NET_UPDATE = "net_update";
	
    private Preference mLocalUpdatePreference;
    private Preference mNetUpdatePreference;
    
	public static SystemUpdateFragment newInstance() {
        return new SystemUpdateFragment();
    }
	
	public void onCreatePreferences(Bundle savedInstanceState, String rootKey){
		setPreferencesFromResource(R.xml.update, null);
		mLocalUpdatePreference = findPreference(KEY_LOCAL_UPDATE);
		mNetUpdatePreference = findPreference(KEY_NET_UPDATE);
	}
	
	
	@Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (TextUtils.equals(preference.getKey(), KEY_LOCAL_UPDATE)) {
        	
        }else if(TextUtils.equals(preference.getKey(), KEY_NET_UPDATE)){
        	
        }
        return super.onPreferenceTreeClick(preference);
    }
}
