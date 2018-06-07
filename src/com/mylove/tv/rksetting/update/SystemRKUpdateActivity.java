package com.mylove.tv.rksetting.update;

import java.io.File;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.RecoverySystem.ProgressListener;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.tv.settings.R;
import android.os.RecoverySystem;

public class SystemRKUpdateActivity extends Activity implements View.OnClickListener{
	private Button local_immediate;
	private TextView tvVersion,tvModel,tvTitle,tvInfo;
	
	private String type;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.system_ota_update);
		type = getIntent().getStringExtra("TYPE");
		tvTitle = (TextView)findViewById(R.id.system_ota_update_title);
		tvModel = (TextView)findViewById(R.id.local_current_model);
		tvVersion = (TextView)findViewById(R.id.local_current_version);
		tvInfo = (TextView)findViewById(R.id.local_update_info);
		local_immediate = (Button)findViewById(R.id.local_immediate);
		
		
		tvModel.setText(Build.MODEL);
		tvVersion.setText(SystemUtils.getProp("ro.product.version"));
		if("LOCAL".equals(type)){
			tvTitle.setText(R.string.system_local_update);
		}else{
			tvTitle.setText(R.string.system_net_update);
			local_immediate.setText(R.string.check_now);
			tvInfo.setText(R.string.check_now);
		}
		local_immediate.setOnClickListener(this);
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
        if("LOCAL".equals(type)){
    		Intent intent = new Intent(this, FileSelector.class);
	        intent.putExtra(FileSelector.ROOT, "/");
	        startActivityForResult(intent, 0);
        }else{
        	Intent serviceIntent = new Intent(this, RKUpdateService.class);
            serviceIntent.putExtra("command", RKUpdateService.COMMAND_CHECK_REMOTE_UPDATING);
            serviceIntent.putExtra("delay", 0);
            startService(serviceIntent);
        }
		
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if(data != null){
	        Bundle bundle = data.getExtras();
	        final String file = bundle.getString("file");
	        if (file != null) {
	        	Intent serviceIntent = new Intent(this, RKUpdateService.class);
	            serviceIntent.putExtra("command", RKUpdateService.COMMAND_CHECK_LOCAL_UPDATING);
	            serviceIntent.putExtra("delay", 0);
	            serviceIntent.putExtra("localPath", file);
	            startService(serviceIntent);
	        }
        }
	}
}