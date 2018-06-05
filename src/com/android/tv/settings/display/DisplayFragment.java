/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package com.android.tv.settings.display;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.display.DisplayManager;
import android.os.Bundle;
import android.support.v17.preference.LeanbackPreferenceFragment;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;
import android.util.Log;
import android.view.Display;
import android.os.DisplayOutputManager;
import android.os.SystemProperties;
import com.android.tv.settings.R;
import com.android.tv.settings.data.ConstData;
import com.mylove.tv.rksetting.BaseLeanbackPreferenceFragment;
public class DisplayFragment extends BaseLeanbackPreferenceFragment{
	private static final String TAG = "DisplayFragment";
	public static final String KEY_MAIN_DISPLAY = "main_display";
	public static final String KEY_SECOND_DISPLAY = "second_display";
	public static final String KEY_DISPLAY_DEVICE_CATEGORY = "display_device_category";
	public static final String HDMI_PLUG_ACTION = "android.intent.action.HDMI_PLUGGED";
	private PreferenceScreen mPreferenceScreen;
	private static String mStrPlatform;
	private static boolean mIsUseDisplayd;

	/**
	 * rk_fb杈撳嚭鐩稿叧
	 */
	private DisplayOutputManager mDisplayOutputManager;
	/**
	 * 鍘熺敓鏍囧噯鏄剧ず绠＄悊鎺ュ彛,鐢ㄤ簬DRM鏄剧ず鐩稿叧
	 */
	private DisplayManager mDisplayManager;
	/**
	 * 鎻掓嫈鏄剧ず璁惧鐩戝惉
	 */
	private DisplayListener mDisplayListener;
	/**
	 * 涓绘樉绀�
	 */
	private Preference mMainDisplayPreference;
	/**
	 * 娆℃樉绀�
	 */
	private Preference mSecondDisPreference;
	/**
	 * HDMI鐑彃鎷旀帴鏀跺櫒
	 */
	private HDMIReceiver mHdmiReceiver;
	private PreferenceCategory mDisplayDeviceCategory;
    public static DisplayFragment newInstance() {
        return new DisplayFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        mIsUseDisplayd = SystemProperties.getBoolean("ro.rk.displayd.enable", true);
        if (!mIsUseDisplayd) {
            setPreferencesFromResource(R.xml.display_drm, null);
        } else {
            setPreferencesFromResource(R.xml.display, null);
        }
        initData();
        rebuildView();
    }


    @Override
    public void onStart() {
        Log.i(TAG, "onStart");
        super.onStart();
    }

    @Override
    public void onResume() {
        Log.i(TAG, "onResume");
    	super.onResume();
    	registerDisplayListener();
    	registerHDMIReceiver();
    }

    @Override
    public void onPause() {
        Log.i(TAG, "onPause");
    	super.onPause();
    	unRegiserDisplayListener();
    	unRegisterHDMIReceiver();
    }

    private void initData(){
    	mPreferenceScreen = getPreferenceScreen();
    	mMainDisplayPreference = findPreference(KEY_MAIN_DISPLAY);
    	mSecondDisPreference = findPreference(KEY_SECOND_DISPLAY);
    	mDisplayDeviceCategory = (PreferenceCategory)findPreference(KEY_DISPLAY_DEVICE_CATEGORY);
    	mDisplayManager = (DisplayManager)getActivity().getSystemService(Context.DISPLAY_SERVICE);
    	mDisplayListener = new DisplayListener();
    	mHdmiReceiver = new HDMIReceiver();
    	Log.i(TAG, "screenTitle:" + mPreferenceScreen.getTitle());
    }

    /**
     * 娉ㄥ唽鏄剧ず鐩戝惉
     */
    private void registerDisplayListener(){
    	mDisplayManager.registerDisplayListener(mDisplayListener, null);
    }

    /**
     * 鍙栨秷鏄剧ず鐩戝惉
     */
    private void unRegiserDisplayListener(){
    	mDisplayManager.unregisterDisplayListener(mDisplayListener);
    }

    /**
     * 娉ㄥ唽HDMI鎺ユ敹鍣�
     */
    private void registerHDMIReceiver(){
        IntentFilter filter = new IntentFilter(HDMI_PLUG_ACTION);
        getActivity().registerReceiver(mHdmiReceiver, filter);
    }


    /**
     * 鍙栨秷娉ㄥ唽HDMI鎺ユ敹鍣�
     */
    private void unRegisterHDMIReceiver(){
        getActivity().unregisterReceiver(mHdmiReceiver);
    }

    /**
     * 閲嶆柊鏋勯�犻〉闈�
     */
    private void rebuildView(){
    	mDisplayDeviceCategory.removeAll();
    	List<DisplayInfo> displayInfos = getDisplayInfos();
    	Log.i(TAG, "rebuildView->displayInfos:" + displayInfos);
    	if(displayInfos.size() > 0){
    		for(DisplayInfo displayInfo : displayInfos){
				Intent intent = new Intent();
				intent.putExtra(ConstData.IntentKey.DISPLAY_INFO, displayInfo);
				getActivity().setIntent(intent);
    			if(displayInfo.getDisplayId() == 0){
    				mMainDisplayPreference.setTitle(displayInfo.getDescription());
    				mDisplayDeviceCategory.addPreference(mMainDisplayPreference);
    			}else{
    				mSecondDisPreference.setTitle(displayInfo.getDescription());
    				mDisplayDeviceCategory.addPreference(mSecondDisPreference);
    			}
    		}
    	}
    }

    /**
     * 鑾峰彇鎵�鏈夊鎺ユ樉绀鸿澶囦俊鎭�,姝ゆ柟娉曞凡鍏煎rk_fb涓嶥RM
     * @param <mDisplayOutputManager>
     * @return
     */
    private List<DisplayInfo> getDisplayInfos(){
    	List<DisplayInfo> displayInfos = new ArrayList<DisplayInfo>();
    	mDisplayOutputManager = null;
    	try{
    		mDisplayOutputManager = new DisplayOutputManager();
    	}catch (Exception e){
    		Log.i(TAG, "new DisplayOutputManger exception:" + e);
    	}

        mIsUseDisplayd = SystemProperties.getBoolean("ro.rk.displayd.enable", true);
    	Display[] displays = mDisplayManager.getDisplays();
		if(!mIsUseDisplayd){
			displayInfos.addAll(DrmDisplaySetting.getDisplayInfoList());
    	}else{
    		//浣跨敤rk_fb鏂瑰紡鑾峰彇鏄剧ず鍒楄〃
    		int[] mainTypes = mDisplayOutputManager.getIfaceList(mDisplayOutputManager.MAIN_DISPLAY);
    		int[] externalTypes = mDisplayOutputManager.getIfaceList(mDisplayOutputManager.AUX_DISPLAY);
    		//RK绯诲垪鑺墖锛岀洰鍓嶆渶澶氬彧鑳芥敮鎸�2涓睆骞�
    		if(mainTypes != null && mainTypes.length > 0){
    			int currMainType = mDisplayOutputManager.getCurrentInterface(mDisplayOutputManager.MAIN_DISPLAY);
    			//涓诲睆鍙兘鏈変竴涓�
    			DisplayInfo displayInfo = new DisplayInfo();
				displayInfo.setDisplayId(0);
				displayInfo.setDescription((String)invokeMethod(mDisplayOutputManager, "typetoface", new Class[]{int.class}, new Integer[]{currMainType}));
				displayInfo.setType(currMainType);
				displayInfo.setModes(mDisplayOutputManager.getModeList(0,currMainType));
				displayInfos.add(displayInfo);
    		}
    		if(externalTypes != null && externalTypes.length > 0){
    			int currExternalType =  mDisplayOutputManager.getCurrentInterface(mDisplayOutputManager.AUX_DISPLAY);
    			//鍓睆鍙兘鏈変竴涓�
    			DisplayInfo displayInfo = new DisplayInfo();
    			displayInfo.setType(currExternalType);
    			displayInfo.setModes(mDisplayOutputManager.getModeList(1,currExternalType));
    			displayInfo.setDescription((String)invokeMethod(mDisplayOutputManager, "typetoface", new Class[]{int.class}, new Integer[]{currExternalType}));
    			//鍓睆鐨刬d闇�瑕佹悳绱㈡爣鍑嗘帴鍙�
    			for(Display display : displays){
    				if(display.getDisplayId() != 0){
    					displayInfo.setDisplayId(display.getDisplayId());
    					break;
    				}
    			}
    			displayInfos.add(displayInfo);
    		}
    	}
    	return displayInfos;
    }


    /**
     * 鍙嶅皠璋冪敤鐩稿叧鏂规硶
     * @param object
     * @param methodName
     * @param parameterTypes
     * @param args
     * @return
     */
    private Object invokeMethod(Object object, String methodName, Class<?>[] parameterTypes, Object[] args){
    	Object result = null;
    	try{
    		Method method = object.getClass().getDeclaredMethod(methodName, parameterTypes);
    		method.setAccessible(true);
    		result = method.invoke(object, args);
    	}catch (Exception e){
    		Log.i(TAG, "invokeMethod->exception:" + e);
    	}
    	return result;
    }


    /**
     * 杞崲鏄剧ず鎺ュ彛
     */
    private void changeDisplayInterface(boolean isHDMIConnect){
        mDisplayOutputManager = null;
        try{
            mDisplayOutputManager = new DisplayOutputManager();
        }catch (Exception e){
            Log.i(TAG, "new DisplayOutputManger exception:" + e);
        }
        if(!isHDMIConnect){
            mDisplayOutputManager.setInterface(mDisplayOutputManager.MAIN_DISPLAY,1, true);
        }
    }


    /**
     * 鏄剧ず璁惧鎻掓嫈鐩戝惉鍣�
     * @author GaoFei
     *
     */
    class DisplayListener implements DisplayManager.DisplayListener{

		@Override
		public void onDisplayAdded(int displayId) {
		    Log.i(TAG, "DisplayListener->onDisplayAdded");
			rebuildView();
		}

		@Override
		public void onDisplayRemoved(int displayId) {
		    Log.i(TAG, "DisplayListener->onDisplayRemoved");
			rebuildView();
		}

		@Override
		public void onDisplayChanged(int displayId) {
		    Log.i(TAG, "DisplayListener->onDisplayChanged");

		}

    }


    /**
     * HDMI 鐑彃鎷斾簨浠�
     * @author GaoFei
     *
     */
    class HDMIReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            boolean state = intent.getBooleanExtra("state", true);
            changeDisplayInterface(state);
            //Log.i(TAG, "HDMIReceiver->onReceive");
            rebuildView();
        }

    }
}
