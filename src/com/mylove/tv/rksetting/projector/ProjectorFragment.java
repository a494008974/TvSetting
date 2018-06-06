package com.mylove.tv.rksetting.projector;


import android.os.Bundle;
import android.provider.Settings;
import android.support.v17.preference.LeanbackPreferenceFragment;
import com.android.tv.settings.R;
import com.android.tv.settings.util.JniCall;
import com.mylove.tv.rksetting.BaseLeanbackPreferenceFragment;
import android.content.ContentResolver;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.TwoStatePreference;
import android.text.TextUtils;
import android.util.Log;

public class ProjectorFragment extends BaseLeanbackPreferenceFragment  implements Preference.OnPreferenceChangeListener {
	
	private static final String TAG = "ProjectorFragment";
	private static final String KEY_LIGHT_SETTINGS = "light_settings";
    private static final String KEY_FLIP_SETTINGS = "flip_settings";
    private static final String KEY_AUTO_KEYSTONE= "auto_keystone";
	private ContentResolver mContentResolver;
	
	public static ProjectorFragment newInstance() {
        return new ProjectorFragment();
    }
	
	public void onCreatePreferences(Bundle savedInstanceState, String rootKey){
		mContentResolver = getActivity().getContentResolver();
        Log.d(TAG, "mContentResolver=====>" + mContentResolver);
		setPreferencesFromResource(R.xml.projector, null);
		
		
		final ListPreference light_settings =
                (ListPreference) findPreference(KEY_LIGHT_SETTINGS);
        light_settings.setValue(getDefaultLightSetting());
		light_settings.setOnPreferenceChangeListener(this);
        
        final ListPreference flip_settings =
                (ListPreference) findPreference(KEY_FLIP_SETTINGS);
        flip_settings.setValue(getDefaultFlipSetting());
        flip_settings.setOnPreferenceChangeListener(this);

		final TwoStatePreference autoKeystonePref =
			(TwoStatePreference) findPreference(KEY_AUTO_KEYSTONE);
		autoKeystonePref.setChecked(getAutoKeystoneEnabled());
	}
	
	
	@Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (TextUtils.equals(preference.getKey(), KEY_LIGHT_SETTINGS)) {
            final String selection = (String) newValue;
            int lightValue = Integer.parseInt(selection);
            JniCall.setProjectorLight(lightValue);
            
			Log.d(TAG, "lightValue ================================================= " + lightValue);
            return true;
        }else if(TextUtils.equals(preference.getKey(), KEY_FLIP_SETTINGS)){
        	final String selection = (String) newValue;
        	int flipValue = Integer.parseInt(selection);
        	JniCall.SetProjectorMode(flipValue);
        	
			Log.d(TAG, "flipValue ================================================= " + flipValue);
        	return true;
        } else if(TextUtils.equals(preference.getKey(), KEY_AUTO_KEYSTONE)) {
			Log.d(TAG, "keystone preference changed!");
			return true;
		}
		return true;
    }

	public boolean onPreferenceTreeClick(Preference preference) {
		if (TextUtils.equals(preference.getKey(), KEY_AUTO_KEYSTONE)) {
			final TwoStatePreference keystonePref = (TwoStatePreference) preference;
			setAutoKeystoneEnabled(keystonePref.isChecked());
		}
		return super.onPreferenceTreeClick(preference);
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

	private boolean getAutoKeystoneEnabled() {
		Log.w(TAG, "maxiongbo, getInt!");
		try {
			return Settings.System.getInt(mContentResolver, "auto_keystone_enabled", 1) != 0;
		} catch (Exception e) {
			Log.w(TAG, "maxiongbo, getInt:", e);
		}
		return true;
	}

	private void setAutoKeystoneEnabled(boolean enabled) {
		//Settings.System.canWrite
		Log.w(TAG, "maxiongbo, putInt!");
		try {
			Settings.System.putInt(mContentResolver, "auto_keystone_enabled", enabled ? 1 : 0);
		} catch (Exception e) {
			Log.w(TAG, "maxiongbo, putInt", e);
		}
	}
}
