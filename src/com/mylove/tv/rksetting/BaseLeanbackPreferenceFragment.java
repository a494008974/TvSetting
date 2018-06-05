package com.mylove.tv.rksetting;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v17.preference.LeanbackPreferenceFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

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
        	view0.setBackgroundColor(Color.parseColor("#882f3b7c"));
        }
        View view1 = viewGroup.getChildAt(1);
        if(view1 != null){
        	if(view1 instanceof LinearLayout){
        		LinearLayout layout = (LinearLayout)view1;
        		LinearLayout.LayoutParams ll = (LayoutParams) layout.getLayoutParams();
        		ll.setMargins(120, 20, 120, 20);
        		view1.setLayoutParams(ll);
        	}
        	
        	
        	view1.setBackgroundColor(0);
        }
        
        return v;
    }
}
