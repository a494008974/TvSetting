package com.mylove.tv.rksetting.update;

import android.app.Application;

public class SettingApp extends Application {

	private static SettingApp appInstance;
	
	public SettingApp(){
		super();
		appInstance = this;
	}
	
	public static final SettingApp getAppInstance(){
		return appInstance;
	}
}
