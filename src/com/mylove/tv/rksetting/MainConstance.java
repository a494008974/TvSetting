package com.mylove.tv.rksetting;

import com.android.tv.settings.MainSettings;
import com.android.tv.settings.R;

import com.android.tv.settings.about.AboutActivity;
import com.android.tv.settings.connectivity.NetworkActivity;
import com.android.tv.settings.device.StorageResetActivity;
import com.android.tv.settings.device.sound.SoundActivity;
import com.android.tv.settings.system.DateTimeActivity;
import com.android.tv.settings.system.InputsActivity;
import com.android.tv.settings.system.LanguageActivity;
import com.mylove.tv.rksetting.projector.ProjectorActivity;
import com.mylove.tv.rksetting.system.SystemActivity;
import com.mylove.tv.rksetting.update.SystemUpdateActivity;



public class MainConstance {
	
	public static int COUNT = 8;
	
	public static int[] DRAWABLE = {
		R.drawable.network,R.drawable.display_sound,R.drawable.area_time_icon,R.drawable.user_back,
		R.drawable.system_setting,R.drawable.system_update,R.drawable.projector,R.drawable.about_icon
		};
	
	public static int[] TITLE = {R.string.connectivity_network, R.string.device_sound,R.string.system_date_time,R.string.bugreport_title, 
							     R.string.settings_app_name, R.string.about_system_update, R.string.projector_settings, R.string.about_preference};
	
	public static Class[] clazz = {
		NetworkActivity.class,SoundActivity.class,DateTimeActivity.class,UserBackupActivity.class,
		MainSettings.class,SystemUpdateActivity.class,ProjectorActivity.class,AboutActivity.class
		};
	
	
}
