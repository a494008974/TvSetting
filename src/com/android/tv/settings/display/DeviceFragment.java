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

import android.annotation.SuppressLint;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.os.Bundle;
import android.support.v17.preference.LeanbackPreferenceFragment;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.util.Log;
import android.view.Display.Mode;
import android.view.View;
import android.widget.TextView;
import android.os.DisplayOutputManager;
import android.os.SystemProperties;
import android.support.annotation.Keep;
import com.android.tv.settings.R;
import com.android.tv.settings.data.ConstData;
import com.mylove.tv.rksetting.BaseLeanbackPreferenceFragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
@Keep
public class DeviceFragment extends BaseLeanbackPreferenceFragment implements Preference.OnPreferenceChangeListener,
Preference.OnPreferenceClickListener{
    protected static final String TAG = "DeviceFragment";
    public static final String KEY_RESOLUTION = "resolution";
    public static final String KEY_ZOOM = "zoom";
    public static final String KEY_ADVANCED_SETTINGS = "advanced_settings";
    protected PreferenceScreen mPreferenceScreen;
    /**
     * 鍒嗚鲸鐜囪缃�
     */
    protected ListPreference mResolutionPreference;
    /**
     * 缂╂斁璁剧疆
     */
    protected Preference mZoomPreference;
    /**
     * 楂樼骇璁剧疆
     */
    protected Preference mAdvancedSettingsPreference;
    /**
     * 褰撳墠鏄剧ず璁惧瀵瑰簲鐨勪俊鎭�
     */
    protected DisplayInfo mDisplayInfo;
    /**
     * 鏍囬
     */
    protected TextView mTextTitle;
    /**
     * 鏍囪瘑骞冲彴
     */
    protected String mStrPlatform;
    protected boolean mIsUseDisplayd;
    /**
     * 鏄剧ず绠＄悊
     */
    protected DisplayManager mDisplayManager;
    /**
    * 鍘熸潵鐨勫垎杈ㄧ巼
    */
    private String mOldResolution;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.display_device, null);
        initData();
        initEvent();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        rebuildView();
        updateResolutionValue();
    }


    @Override
    public void onPause() {
        super.onPause();
    }


    protected void initData(){
        mStrPlatform = SystemProperties.get("ro.board.platform");
        mIsUseDisplayd = SystemProperties.getBoolean("ro.rk.displayd.enable", true);
        mDisplayManager = (DisplayManager)getActivity().getSystemService(Context.DISPLAY_SERVICE);
        mPreferenceScreen = getPreferenceScreen();
        mAdvancedSettingsPreference = findPreference(KEY_ADVANCED_SETTINGS);
        mResolutionPreference = (ListPreference)findPreference(KEY_RESOLUTION);
        mZoomPreference = findPreference(KEY_ZOOM);
        mTextTitle = (TextView)getActivity().findViewById(android.support.v7.preference.R.id.decor_title);
        if (!mIsUseDisplayd) {
            mDisplayInfo = getDisplayInfo();
        } else {
            Intent intent = getActivity().getIntent();
            mDisplayInfo = (DisplayInfo) intent.getExtras().getSerializable(ConstData.IntentKey.DISPLAY_INFO);
        }
        //if(!mStrPlatform.contains("3328"))
        //mPreferenceScreen.removePreference(mAdvancedSettingsPreference);
    }

    protected void rebuildView(){
    	if(mDisplayInfo == null)
    		return;
        mResolutionPreference.setEntries(mDisplayInfo.getModes());
        mResolutionPreference.setEntryValues(mDisplayInfo.getModes());
        mTextTitle.setText(mDisplayInfo.getDescription());
    }


    protected void initEvent(){
        mResolutionPreference.setOnPreferenceChangeListener(this);
        mZoomPreference.setOnPreferenceClickListener(this);
        mAdvancedSettingsPreference.setOnPreferenceClickListener(this);
    }

    /**
     * 杩樺師鍒嗚鲸鐜囧��
     */
    public void updateResolutionValue(){
    	if(mDisplayInfo == null)
    		return;
        String resolutionValue = null;
        if(!mIsUseDisplayd){
            resolutionValue = DrmDisplaySetting.getCurDisplayMode(mDisplayInfo);
            Log.i(TAG, "drm resolutionValue:" + resolutionValue);
            if(resolutionValue != null)
                mResolutionPreference.setValue(resolutionValue);
            /*show mResolutionPreference current item*/
            List<String> modes = DrmDisplaySetting.getDisplayModes(mDisplayInfo);
            Log.i(TAG, "setValueIndex modes.toString()= "+modes.toString());
            int index = 0;
            index = modes.indexOf(resolutionValue);
            Log.i(TAG, "mResolutionPreference setValueIndex index= "+index);
            mResolutionPreference.setValueIndex(index);
        }else{
            DisplayOutputManager displayOutputManager = null;
            try{
                displayOutputManager = new DisplayOutputManager();
                resolutionValue = displayOutputManager.getCurrentMode(mDisplayInfo.getDisplayId() == 0 ? 0 : 1, mDisplayInfo.getType());
            }catch (Exception e){
                Log.i(TAG, "updateResolutionValue->exception:" + e);
            }
            if(resolutionValue != null)
                mResolutionPreference.setValue(resolutionValue);
            if(mOldResolution == null)
                mOldResolution = resolutionValue;
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object obj) {
        Log.i(TAG, "onPreferenceChange:" + obj);
        if(preference == mResolutionPreference){
            if(!mIsUseDisplayd){
                int index = mResolutionPreference.findIndexOfValue((String)obj);
                DrmDisplaySetting.setDisplayModeTemp(mDisplayInfo, index);
                showConfirmSetModeDialog();
            }else{
                DisplayOutputManager displayOutputManager = null;
                try{
                    displayOutputManager = new DisplayOutputManager();
                }catch (Exception e){
                    Log.i(TAG, "onPreferenceChange->exception:" + e);
                }

                if(displayOutputManager != null){
                    displayOutputManager.setMode(mDisplayInfo.getDisplayId(), mDisplayInfo.getType(), (String)obj);
                    showConfirmSetModeDialog();
                }
            }

        }
        return true;
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if(preference == mZoomPreference) {
            Intent screenScaleIntent = new Intent(getActivity(), ScreenScaleActivity.class);
            screenScaleIntent.putExtra(ConstData.IntentKey.PLATFORM, mStrPlatform);
            screenScaleIntent.putExtra(ConstData.IntentKey.DISPLAY_INFO, mDisplayInfo);
            startActivity(screenScaleIntent);
        } else if (preference == mResolutionPreference) {
            //updateResolutionValue();
        }else if(preference == mAdvancedSettingsPreference){
        	Intent advancedIntent = new Intent(getActivity(), AdvancedDisplaySettingsActivity.class);
			advancedIntent.putExtra(ConstData.IntentKey.DISPLAY_ID, mDisplayInfo.getDisplayId());
            startActivity(advancedIntent);
        }
        return true;
    }


    @SuppressLint("NewApi")
    protected void showConfirmSetModeDialog() {
        DialogFragment df = ConfirmSetModeDialogFragment.newInstance(mDisplayInfo, new ConfirmSetModeDialogFragment.OnDialogDismissListener() {
            @Override
            public void onDismiss(boolean isok) {
                Log.i(TAG, "showConfirmSetModeDialog->onDismiss->isok:" + isok);
                Log.i(TAG, "showConfirmSetModeDialog->onDismiss->mOldResolution:" + mOldResolution);
                if(!mIsUseDisplayd)
                    updateResolutionValue();
                else{
                    DisplayOutputManager displayOutputManager = null;
                    try{
                        displayOutputManager = new DisplayOutputManager();
                    }catch (Exception e){
                        Log.i(TAG, "onPreferenceChange->exception:" + e);
                    }
                    if(isok && displayOutputManager != null){
                        displayOutputManager.saveConfig();
                    }else if(!isok && displayOutputManager != null && mOldResolution != null){
                        //杩樺師鍘熸潵鐨勫垎杈ㄧ巼
                        displayOutputManager.setMode(mDisplayInfo.getDisplayId(), mDisplayInfo.getType(), mOldResolution);
                    }
                    updateResolutionValue();
                }
            }
        });
        df.show(getFragmentManager(), "ConfirmDialog");
    }

    protected DisplayInfo getDisplayInfo() {
        return null;
    }

}
