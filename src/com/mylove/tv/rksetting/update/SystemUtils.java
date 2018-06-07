package com.mylove.tv.rksetting.update;


import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Random;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Environment;
import android.text.format.DateFormat;
import android.util.Log;

public class SystemUtils {
	private static final String TAG = "SystemUtility";

	public static native int setenv(String name, String value, boolean overwrite);

	private static int sArmArchitecture = -1;

	
	public static String getProp(String key){
		String value = null;
		//if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                Class c = Class.forName("android.os.SystemProperties");
                Method m = c.getDeclaredMethod("get", String.class);
                m.setAccessible(true);
                value = (String) m.invoke(null, key);
            } catch (Throwable e) {
            	
            }
       // }
		return value;
	}
	
	public static int getArmArchitecture() {
		if (sArmArchitecture != -1)
			return sArmArchitecture;
		try {
			InputStream is = new FileInputStream("/proc/cpuinfo");
			InputStreamReader ir = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(ir);
			try {
				String name = "CPU architecture";
				while (true) {
					String line = br.readLine();
					String[] pair = line.split(":");
					if (pair.length != 2)
						continue;
					String key = pair[0].trim();
					String val = pair[1].trim();
					if (key.compareToIgnoreCase(name) == 0) {
						String n = val.substring(0, 1);
						sArmArchitecture = Integer.parseInt(n);
						break;
					}
				}
			} finally {
				br.close();
				ir.close();
				is.close();
				if (sArmArchitecture == -1)
					sArmArchitecture = 6;
			}
		} catch (Exception e) {
			sArmArchitecture = 6;
		}
		return sArmArchitecture;
	}

	public static int getSDKVersionCode() {
		// TODO: fix this
		return Build.VERSION.SDK_INT;
	}

	public static String getExternalStoragePath() {
		boolean exists = Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED);
		if (exists)
			return Environment.getExternalStorageDirectory().getAbsolutePath();
		else
			return "/";
	}

	@SuppressWarnings("rawtypes")
	public static Object realloc(Object oldArray, int newSize) {
		int oldSize = java.lang.reflect.Array.getLength(oldArray);
		Class elementType = oldArray.getClass().getComponentType();
		Object newArray = java.lang.reflect.Array.newInstance(elementType,
				newSize);
		int preserveLength = Math.min(oldSize, newSize);
		if (preserveLength > 0)
			System.arraycopy(oldArray, 0, newArray, 0, preserveLength);
		return newArray;
	}

	public static String getTimeString(int msec) {
		if (msec < 0) {
			return String.format("--:--:--");
		}
		int total = msec / 1000;
		int hour = total / 3600;
		total = total % 3600;
		int minute = total / 60;
		int second = total % 60;
		return String.format("%02d:%02d:%02d", hour, minute, second);
	}

	protected static String sTempPath = "/data/local/tmp";

	public static String getTempPath() {
		return sTempPath;
	}


	public static int getStringHash(String s) {
		byte[] target = s.getBytes();
		int hash = 1315423911;
		for (int i = 0; i < target.length; i++) {
			byte val = target[i];
			hash ^= ((hash << 5) + val + (hash >> 2));
		}
		hash &= 0x7fffffff;
		return hash;
	}

	public static boolean isNetworkAvailable(Context context) {  
	    ConnectivityManager connectivity = (ConnectivityManager) context  
	            .getSystemService(Context.CONNECTIVITY_SERVICE);  
	    if (connectivity == null) {  
	    	Log.e(TAG, "getSystemService rend null");  
	    } else { 
	        NetworkInfo[] info = connectivity.getAllNetworkInfo();  
	        if (info != null) { 
	            for (int i = 0; i < info.length; i++) {  
	                if (info[i].getState() == NetworkInfo.State.CONNECTED) {  
	                    return true;  
	                }  
	            }  
	        }  
	    }  
	    return false;  
	}
	
    /**
     * get current data connection type name, like:Mobile/WIFI/OFFLINE
     * 
     * @param context
     * @return
     */
    public static String getConnectTypeName(Context context) {
            if (!isNetworkAvailable(context)) {
                    return "OFFLINE";
            }
            ConnectivityManager manager = (ConnectivityManager) context
                            .getSystemService(Activity.CONNECTIVITY_SERVICE);
            NetworkInfo info = manager.getActiveNetworkInfo();
            if (info != null) {
                    return info.getTypeName();
            } else {
                    return "OFFLINE";
            }
    }
    
    public static String getLocalMacAddress(Context ctx) {  
    	/*
        WifiManager wifi = (WifiManager)ctx.getSystemService(Context.WIFI_SERVICE);  
        WifiInfo info = wifi.getConnectionInfo();  
        return info.getMacAddress();  
        */
    	String text = "00:11:22:33:44:55";
		try {
			InputStream is = new FileInputStream("/sys/class/net/eth0/address");
			InputStreamReader ir = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(ir);
			try {
				text = br.readLine();
					
			} finally {
				br.close();
				ir.close();
				is.close();
			}
		} catch (Exception e) {
            Log.e(TAG, "Open File Error!" + e.toString());  
		}
        
        return text.trim();
    }
    
    public static String getCharAndNumr(int length)     
	{     
	    String val = "";
	    Random random = new Random();     
	    for(int i = 0; i < length; i++)     
	    {     
	        String charOrNum = random.nextInt(2) % 2 == 0 ? "char" : "num"; // 输出字母还是数字        
	        if("char".equalsIgnoreCase(charOrNum)) // 字符�?     
	        {     
	            int choice = random.nextInt(2) % 2 == 0 ? 65 : 97; //取得大写字母还是小写字母     
	            val += (char) (choice + random.nextInt(26));     
	        }     
	        else if("num".equalsIgnoreCase(charOrNum)) // 数字     
	        {     
	            val += String.valueOf(random.nextInt(10));     
	        }     
	    }     
	    return val;     
	} 
    
    public static String getTime(Context context) {
		final Calendar c = Calendar.getInstance();
		int hour = c.get(Calendar.HOUR_OF_DAY);
		int minute = c.get(Calendar.MINUTE);
		if (!DateFormat.is24HourFormat(context) && hour > 12) {
			hour = hour - 12;
		}
		String time = "";
		if (hour >= 10) {
			time += Integer.toString(hour);
		} else {
			time += "0" + Integer.toString(hour);
		}
		time += ":";

		if (minute >= 10) {
			time += Integer.toString(minute);
		} else {
			time += "0" + Integer.toString(minute);
		}
		return time;
	}
	
	public static String getStatu(){
		final Calendar c = Calendar.getInstance();
		String ampmValues;
		if (c.get(Calendar.AM_PM) == 0) {
			ampmValues = "AM";
		} else {
			ampmValues = "PM";
		}
		return ampmValues;
	}
	
	public static String getWeek(){
		SimpleDateFormat formatter = new SimpleDateFormat("EEEE");
		Date curDate = new Date(System.currentTimeMillis());
		return formatter.format(curDate);
	}
    
    public static String getLocalIpAddress() {  
        try {  
            for (Enumeration<NetworkInterface> en = NetworkInterface  
                    .getNetworkInterfaces(); en.hasMoreElements();) {  
                NetworkInterface intf = en.nextElement();  
                for (Enumeration<InetAddress> enumIpAddr = intf  
                        .getInetAddresses(); enumIpAddr.hasMoreElements();) {  
                    InetAddress inetAddress = enumIpAddr.nextElement();  
                    if (!inetAddress.isLoopbackAddress()) {  
                        return inetAddress.getHostAddress().toString();  
                    }  
                }  
            }  
        } catch (SocketException ex) {  
            Log.e("WifiPreference IpAddress", ex.toString());  
        }  
        return null;  
    }
}
