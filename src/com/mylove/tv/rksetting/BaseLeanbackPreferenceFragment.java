package com.mylove.tv.rksetting;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v17.preference.LeanbackPreferenceFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import com.android.tv.settings.R;

public abstract class BaseLeanbackPreferenceFragment extends LeanbackPreferenceFragment{
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container,savedInstanceState);
        
	    if(container != null){
	    	ViewGroup.LayoutParams lp = container.getLayoutParams();
	        lp.width = FrameLayout.LayoutParams.MATCH_PARENT;
	        lp.height = FrameLayout.LayoutParams.MATCH_PARENT;
	        container.setLayoutParams(lp);
	    }
        		
        ViewGroup viewGroup = (ViewGroup)v;
        viewGroup.setBackgroundResource(R.drawable.setting_bg);
        View view0 = viewGroup.getChildAt(0);
        if(view0 != null){
        	view0.setBackgroundColor(Color.parseColor("#22222222"));
        }
        View view1 = viewGroup.getChildAt(1);
        if(view1 != null){
        	view1.setBackgroundColor(0);
        }
        
        return v;
    }
}
