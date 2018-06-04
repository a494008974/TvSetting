package com.android.tv.settings.util;

public class JniCall {
	static {
		System.loadLibrary("tvsettings-jni");
	}

	//public static native boolean test();
	public static native int[] get(double x, double y);
	public static native int[] getOther(double x, double y);
	public static native boolean isSupportHDR();
	public static native void setHDREnable(int enable);
	public static native int[] getEetf(float maxDst, float minDst);
	public static native int[] getOetf(float maxDst, float minDst);
	public static native int[] getMaxMin(float maxDst, float minDst);
	
	public static native int setProjectorLight(int level);
    public static native int SetProjectorMode(int mode);
	public static native int fetchProjectorMode();
    public static native int fetchProjectorLight();
}
