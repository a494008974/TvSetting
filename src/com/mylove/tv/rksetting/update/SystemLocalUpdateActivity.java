package com.mylove.tv.rksetting.update;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.android.tv.settings.R;

public class SystemLocalUpdateActivity extends Activity implements View.OnClickListener{
	private Button local_immediate;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.system_local_update);
		
		local_immediate = (Button)findViewById(R.id.local_immediate);
		local_immediate.setOnClickListener(this);
	}
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		Intent intent = new Intent(this, FileSelector.class);
        intent.putExtra(FileSelector.ROOT, "/mnt");
        startActivityForResult(intent, 0);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if(data != null){
	        Bundle bundle = data.getExtras();
	        String file = bundle.getString("file");
	        System.out.println(file + " >>>>>>>>>>>>>>>>>>>>>>> ");
//	        if (file != null) {
//	            final Dialog dlg = new Dialog(this, android.R.style.Theme_Holo_Light_Dialog);
//	            dlg.setTitle(R.string.confirm_update);
//	            LayoutInflater inflater = LayoutInflater.from(this);
//	            InstallPackage dlgView = (InstallPackage) inflater.inflate(R.layout.install_ota, null,
//	                    false);
//	            dlgView.setPackagePath(file);
//	            dlg.setContentView(dlgView);
//	            dlg.findViewById(R.id.confirm_cancel).setOnClickListener(new View.OnClickListener() {
//	                @Override
//	                public void onClick(View v) {
//	                    dlg.dismiss();
//	                }
//	            });
//	            dlg.show();
//	        }
        }
	}
}