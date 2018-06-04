package com.mylove.tv.rksetting;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.SimpleAdapter;

import com.android.tv.settings.R;

public class MainActivity extends Activity implements OnItemClickListener{
	
	private GridView mGridView;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        mGridView = (GridView)findViewById(R.id.gridview);
        
        registerListener();
    }
	
	
	public void registerListener() {
		// TODO Auto-generated method stub
        ArrayList<HashMap<String, Object>> lstImageItem = new ArrayList();
        for (int i = 0; i < MainConstance.COUNT; i++) {
            HashMap<String, Object> map = new HashMap();
            map.put("ItemImage", Integer.valueOf(MainConstance.DRAWABLE[i%MainConstance.DRAWABLE.length]));
            map.put("ItemText", this.getResources().getString(MainConstance.TITLE[i%MainConstance.TITLE.length]));
            map.put("SecondText", "");
            lstImageItem.add(map);
        }

        this.mGridView.setAdapter(new SimpleAdapter(this, lstImageItem, R.layout.list_item_main_setting, new String[]{"ItemImage", "ItemText", "SecondText"}, new int[]{R.id.ItemImage, R.id.firstText, R.id.SecondText}));
        this.mGridView.setOnItemClickListener(this);

	}


	@Override
	public void onItemClick(AdapterView<?> adapterView, View view, int position, long param) {
		// TODO Auto-generated method stub
		Intent intent = new Intent();
        Class clazz = MainConstance.clazz[position % MainConstance.clazz.length];
        if(clazz.getSimpleName().equals(UserBackupActivity.class.getSimpleName())){
        	MainUtils.openApk(MainActivity.this, "com.xshuai.service");
        }else{
        	intent.setClass(MainActivity.this, clazz);
        	MainActivity.this.startActivity(intent);
        }
	}
}
