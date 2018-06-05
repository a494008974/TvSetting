package com.mylove.tv.rksetting;


import android.os.Bundle;
import android.provider.Settings;
import android.support.v17.preference.LeanbackPreferenceFragment;
import com.android.tv.settings.R;
import com.android.tv.settings.util.JniCall;

import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.text.TextUtils;

public class ProjectorFragment extends BaseLeanbackPreferenceFragment  implements Preference.OnPreferenceChangeListener {
	
	private static final String KEY_LIGHT_SETTINGS = "light_settings";
    private static final String KEY_FLIP_SETTINGS = "flip_settings";
	
	public static ProjectorFragment newInstance() {
        return new ProjectorFragment();
    }
	
	public void onCreatePreferences(Bundle savedInstanceState, String rootKey){
		setPreferencesFromResource(R.xml.projector, null);
		
		
		final ListPreference light_settings =
                (ListPreference) findPreference(KEY_LIGHT_SETTINGS);
        light_settings.setValue(getDefaultLightSetting());
		light_settings.setOnPreferenceChangeListener(this);
        
        final ListPreference flip_settings =
                (ListPreference) findPreference(KEY_FLIP_SETTINGS);
        flip_settings.setValue(getDefaultFlipSetting());
        flip_settings.setOnPreferenceChangeListener(this);
	}
	
	
	@Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (TextUtils.equals(preference.getKey(), KEY_LIGHT_SETTINGS)) {
            final String selection = (String) newValue;
            int lightValue = Integer.parseInt(selection);
            JniCall.setProjectorLight(lightValue);
            
            System.out.println("lightValue ================================================= " + lightValue);
            return true;
        }else if(TextUtils.equals(preference.getKey(), KEY_FLIP_SETTINGS)){
        	final String selection = (String) newValue;
        	int flipValue = Integer.parseInt(selection);
        	JniCall.SetProjectorMode(flipValue);
        	
        	System.out.println("flipValue ================================================= " + flipValue);
        	return true;
        }
        return true;
    }
	
	private String getDefaultLightSetting() {
		//jni 获取light值		
		int n = JniCall.fetchProjectorLight();
		return String.valueOf(n);
    }
	
	private String getDefaultFlipSetting() {
		//jni 获取flip值
		int n = JniCall.fetchProjectorMode();
		return String.valueOf(n);
	}
}
