package com.mylove.tv.rksetting.update;

import android.util.Log;
import com.android.tv.settings.R;
public class Utils {
	public static final String GROBLE_TAG = "BswUpdater";

	
	
	public static final String FIRST_SERVER_URL_IN_DOMAIN = SettingApp
			.getAppInstance().getString(R.string.first_server_url_in_domain);
	public static final String SECOND_SERVER_URL_IN_DOMAIN = SettingApp.getAppInstance()
			.getString(R.string.second_server_url_in_domain);

	public static final String DOWNLOAD_PATH = "/sdcard/ota.zip";
	public static final boolean DEBUG = false; // false
	public static final int CHECK_CYCLE_DAY = Integer.parseInt(SettingApp
			.getAppInstance().getString(R.string.check_cycle));
	
	public static final String CACHE_DIR = SettingApp.getAppInstance()
			.getString(R.string.cache_dir);
	
	static{
		if(DEBUG){ 
			Log.v(GROBLE_TAG,"domain:" +FIRST_SERVER_URL_IN_DOMAIN);
			Log.v(GROBLE_TAG,"ip:" + SECOND_SERVER_URL_IN_DOMAIN);
			Log.v(GROBLE_TAG,"check cycle:" + CHECK_CYCLE_DAY);
			Log.v(GROBLE_TAG,"cache dir:" + CACHE_DIR);
		}
	}
}
