package com.mylove.tv.rksetting;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

public class MainUtils {
	public static void openApk(Context context,String pkg){
		PackageManager pm = context.getPackageManager();
		Intent intent = pm.getLaunchIntentForPackage(pkg);
		try {
			context.startActivity(intent);
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
}
