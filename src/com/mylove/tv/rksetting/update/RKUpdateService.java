package com.mylove.tv.rksetting.update;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Locale;
import java.util.Map;

//import org.apache.http.Header;
//import org.apache.http.HttpResponse;
//import org.apache.http.client.ClientProtocolException;
//import org.apache.http.client.HttpClient;
//import org.apache.http.client.methods.HttpHead;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RecoverySystem;
import android.util.Log;
import android.widget.Toast;
import android.os.SystemProperties;
import com.android.tv.settings.R;

public class RKUpdateService extends Service {
	public static final String VERSION = "1.8.0";
	private static final String TAG = "RKUpdateService";
    private static final boolean DEBUG = true;
	private static final boolean mIsNotifyDialog = true;
	private static final boolean mIsSupportUsbUpdate = true;
	
    private Context mContext;
    private volatile boolean mIsFirstStartUp = true;
    private static void LOG(String msg) {
        if ( DEBUG ) {
            Log.d(TAG, msg);  
        }
    }
 
    static {
        /*
         * Load the library.  If it's already loaded, this does nothing.
         */
//        System.loadLibrary("rockchip_update_jni");
    }

    public static String OTA_PACKAGE_FILE = "update.zip";
	public static String RKIMAGE_FILE = "update.img";	
	public static final int RKUPDATE_MODE = 1;
	public static final int OTAUPDATE_MODE = 2;      
	private static volatile boolean mWorkHandleLocked = false; 
	private static volatile boolean mIsNeedDeletePackage = false;
	
	public static final String EXTRA_IMAGE_PATH = "android.rockchip.update.extra.IMAGE_PATH";
    public static final String EXTRA_IMAGE_VERSION = "android.rockchip.update.extra.IMAGE_VERSION";
    public static final String EXTRA_CURRENT_VERSION = "android.rockchip.update.extra.CURRENT_VERSION";
    public static String DATA_ROOT = "/data/media/0";
    public static String FLASH_ROOT = Environment.getExternalStorageDirectory().getAbsolutePath();
    public static String SDCARD_ROOT = "/mnt/external_sd";
    public static String USB_ROOT = "/mnt/usb_storage";
    public static String CACHE_ROOT = Environment.getDownloadCacheDirectory().getAbsolutePath();
    
    public static final int COMMAND_NULL = 0;
    public static final int COMMAND_CHECK_LOCAL_UPDATING = 1;
    public static final int COMMAND_CHECK_REMOTE_UPDATING = 2;
    public static final int COMMAND_CHECK_REMOTE_UPDATING_BY_HAND = 3;
    public static final int COMMAND_DELETE_UPDATEPACKAGE = 4;
    
    private static final String COMMAND_FLAG_SUCCESS = "success";
    private static final String COMMAND_FLAG_UPDATING = "updating";
    
    public static final int UPDATE_SUCCESS = 1;
    public static final int UPDATE_FAILED = 2;
    
    private static final String[] IMAGE_FILE_DIRS = {
    	DATA_ROOT + "/",
        FLASH_ROOT + "/",  
        SDCARD_ROOT + "/",
        USB_ROOT + "/",
    };
    
    private String mLastUpdatePath;
    private WorkHandler mWorkHandler;
    private Handler mMainHandler;
    private SharedPreferences mAutoCheckSet;
   
    /*----------------------------------------------------------------------------------------------------*/
    public static URI mRemoteURI = null;
    public static URI mRemoteURIBackup = null;
    private String mTargetURI = null;
    private boolean mUseBackupHost = false;
    private String mOtaPackageVersion = null;
    private String mSystemVersion = null;
    private String mOtaPackageName = null;
    private String mOtaPackageLength = null;
    private String mDescription = null;
    private volatile boolean mIsOtaCheckByHand = false;	
	private String mForceUpdate =null;
    
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return mBinder;
	}
	
	private final LocalBinder mBinder = new LocalBinder();
	
	public class LocalBinder extends Binder {
		public void updateFirmware(String imagePath, int mode) {
			LOG("updateFirmware(): imagePath = " + imagePath);
	        try {       
				mWorkHandleLocked = true;
				if(mode == OTAUPDATE_MODE){
					RecoverySystem.installPackage(mContext, new File(imagePath));
				}else if(mode == RKUPDATE_MODE){
//					RecoverySystem.installRKimage(mContext, imagePath);
				}
	        } catch (IOException e) {
	            Log.e(TAG, "updateFirmware() : Reboot for updateFirmware() failed", e);
	        }
	    }
		
		public boolean doesOtaPackageMatchProduct(String imagePath) {
	      	LOG("doesImageMatchProduct(): start verify package , imagePath = " + imagePath);
			
			try{
				RecoverySystem.verifyPackage(new File(imagePath), null, null);
			}catch(GeneralSecurityException e){
				LOG("doesImageMatchProduct(): verifaPackage faild!");	
				return false;	
			}catch(IOException exc) {
	            LOG("doesImageMatchProduct(): verifaPackage faild!");
				return false;
	        }
	        return true;
	    }
		
		public void deletePackage(String path) {
			LOG("try to deletePackage...");
			File f = new File(path);
			if(f.exists()) {
				f.delete();
				LOG("delete complete! path=" + path);
			}else {
				LOG("path=" + path + " ,file not exists!");
			}
		}
		
		public void unLockWorkHandler() {
			LOG("unLockWorkHandler...");
			mWorkHandleLocked = false;
		}
		
		public void LockWorkHandler() {
			mWorkHandleLocked = true;
			LOG("LockWorkHandler...!");
		}
	}

	@Override
	public void onCreate() {
		super.onCreate();
		
		mContext = this;
        /*-----------------------------------*/
		LOG("starting RKUpdateService, version is " + VERSION);
		
		//whether is UMS or m-user 
		if(getMultiUserState()) {
			FLASH_ROOT = DATA_ROOT;
		}
		
        String ota_packagename = getOtaPackageFileName();
        if(ota_packagename != null) {
        	OTA_PACKAGE_FILE = ota_packagename;
        	LOG("get ota package name private is " + OTA_PACKAGE_FILE);
        }
        
        String rk_imagename = getRKimageFileName();
        if(rk_imagename != null) {
        	RKIMAGE_FILE = rk_imagename;
        	LOG("get rkimage name private is " + RKIMAGE_FILE);
        }
        
        try {
        	mRemoteURI = new URI(getRemoteUri());
        	mRemoteURIBackup = new URI(getRemoteUriBackup());
        	LOG("remote uri is " + mRemoteURI.toString());
        	LOG("remote uri backup is " + mRemoteURIBackup.toString());
        }catch(URISyntaxException e) {
        	e.printStackTrace();
        }
        
        mAutoCheckSet = getSharedPreferences("auto_check", MODE_PRIVATE);
        
        mMainHandler = new Handler(Looper.getMainLooper()); 
        HandlerThread workThread = new HandlerThread("UpdateService : work thread");
        workThread.start();
        mWorkHandler = new WorkHandler(workThread.getLooper());
        
        if(mIsFirstStartUp) {
        	LOG("first startup!!!");
			mIsFirstStartUp = false;
//			String command = RecoverySystem.readFlagCommand();
			String command = null;
			String path;
			if(command != null) {
				LOG("command = " + command);				
				if(command.contains("$path")) {
					path = command.substring(command.indexOf('=') + 1);
					LOG("last_flag: path = " + path);
				
					if(command.startsWith(COMMAND_FLAG_SUCCESS)) {
						if(!mIsNotifyDialog) {
							mIsNeedDeletePackage = true;
							mLastUpdatePath = path;
							return;
						}

						LOG("now try to start notifydialog activity!");
						Intent intent = new Intent(mContext, NotifyDeleteActivity.class);
						intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						intent.putExtra("flag", UPDATE_SUCCESS);
						intent.putExtra("path", path);
						startActivity(intent);
						mWorkHandleLocked = true;
						return;
					} 
					if(command.startsWith(COMMAND_FLAG_UPDATING)) {
						Intent intent = new Intent(mContext, NotifyDeleteActivity.class);
						intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						intent.putExtra("flag", UPDATE_FAILED);
						intent.putExtra("path", path);
						startActivity(intent);
						mWorkHandleLocked = true;
						return;
					}
				}
			}
		}
	}

	@Override
	public void onDestroy() {
		LOG("onDestroy.......");
		super.onDestroy();
	}

	@Override
	public void onStart(Intent intent, int startId) {
		LOG("onStart.......");
		
		super.onStart(intent, startId);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		LOG("onStartCommand.......");
		
		if(intent == null) {
			return Service.START_NOT_STICKY;
		}
		
		int command = intent.getIntExtra("command", COMMAND_NULL);
		int delayTime = intent.getIntExtra("delay", 1000);
		String localPath = intent.getStringExtra("localPath");
		
		LOG("command = " + command + " delaytime = " + delayTime);
		if(command == COMMAND_NULL) {
			return Service.START_NOT_STICKY;
		}
		
		if(command == COMMAND_CHECK_REMOTE_UPDATING) {
			mIsOtaCheckByHand = false;
			if(!mAutoCheckSet.getBoolean("auto_check", true)) {
				LOG("user set not auto check!");
				return Service.START_NOT_STICKY;
			}
		}
		
		if(command == COMMAND_CHECK_REMOTE_UPDATING_BY_HAND) {
			mIsOtaCheckByHand = true;
			//zhangyi--command = COMMAND_CHECK_REMOTE_UPDATING;
			command = COMMAND_CHECK_REMOTE_UPDATING_BY_HAND;
		}
		
		if(mIsNeedDeletePackage) {
			command = COMMAND_DELETE_UPDATEPACKAGE;
			delayTime = 20000;
			mWorkHandleLocked = true;
		}
		
		Message msg = new Message();
		msg.what = command;
		msg.obj = localPath;
		msg.arg1 = WorkHandler.NOT_NOTIFY_IF_NO_IMG;
		mWorkHandler.sendMessageDelayed(msg, delayTime);
		return Service.START_REDELIVER_INTENT;
	}
   
    
    /** @see mWorkHandler. */
    private class WorkHandler extends Handler {
        private static final int NOTIFY_IF_NO_IMG = 1;
        private static final int NOT_NOTIFY_IF_NO_IMG = 0;
        
        /*-----------------------------------*/
        
        public WorkHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {       

            String[] searchResult = null;       

            switch (msg.what) {

                case COMMAND_CHECK_LOCAL_UPDATING:
                    LOG("WorkHandler::handleMessage() : To perform 'COMMAND_CHECK_LOCAL_UPDATING'.");
					if(mWorkHandleLocked){
						LOG("WorkHandler::handleMessage() : locked !!!");
						return;
					}
					
//                    if ( null != (searchResult = getValidFirmwareImageFile(IMAGE_FILE_DIRS) ) ) {
//                        if ( 1 == searchResult.length ) { 
                            String path = (String)msg.obj; 
							String imageFileVersion = null;
							String currentVersion = null;
							
							//if it is rkimage, check the image
                            if(path.endsWith("img")){
								if(!checkRKimage(path)){
									LOG("WorkHandler::handleMessage() : not a valid rkimage !!");
									return;	
								}

								imageFileVersion = getImageVersion(path);

								LOG("WorkHandler::handleMessage() : Find a VALID image file : '" + path 
										+ "'. imageFileVersion is '" + imageFileVersion);
                             
								 currentVersion = getCurrentFirmwareVersion();
								 LOG("WorkHandler::handleMessage() : Current system firmware version : '" + currentVersion + "'.");
							}
                            startProposingActivity(path, imageFileVersion, currentVersion);
//                        }else {
//                            LOG("find more than two package files, so it is invalid!");
//                            return;
//                        }
//                    }
                    break; 
                case COMMAND_CHECK_REMOTE_UPDATING:
                	if(mWorkHandleLocked){
						LOG("WorkHandler::handleMessage() : locked !!!");
						return;
					}
                	
                	for(int i = 0; i < 2; i++) {
	                	try {
	                		boolean result;
	                		
	                		if(i == 0) {
	                			mUseBackupHost = false;
	                			result = requestRemoteServerForUpdate(mRemoteURI);
	                		}else{
	                			mUseBackupHost = true;
	                			result = requestRemoteServerForUpdate(mRemoteURIBackup);
	                		}
	                		
                			if(result) {
                    			LOG("find a remote update package, now start PackageDownloadActivity...");
                    			startNotifyActivity();
                    		}else {
                    			LOG("no find remote update package...");
                    			myMakeToast(mContext.getString(R.string.current_new));
                    		}
                			break;
	                	}catch(Exception e) {
	                		//e.printStackTrace();
	                		LOG("request remote server error...");
	                		myMakeToast(mContext.getString(R.string.current_new));
	                	}
	                	
	                	try{
	                		Thread.sleep(5000);
	                	}catch(InterruptedException e) {
	                		e.printStackTrace();
	                	}
                	}
                	break;
				case COMMAND_CHECK_REMOTE_UPDATING_BY_HAND:
						if(mWorkHandleLocked){
							LOG("WorkHandler::handleMessage() : locked !!!");
							return;
						}
						
						for(int i = 0; i < 2; i++) {
							try {
								boolean result;
								
								if(i == 0) {
									mUseBackupHost = false;
									result = requestRemoteServerForUpdate(mRemoteURI);
								}else{
									mUseBackupHost = true;
									result = requestRemoteServerForUpdate(mRemoteURIBackup);
								}
								
								if(result) {
									LOG("find a remote update package,COMMAND_CHECK_REMOTE_UPDATING_BY_HAND now start PackageDownloadActivity...");
									broadcastFoundOtaPackage();
									startNotifyActivity();
								}else {
									LOG("no find remote update package...");
									//zy--myMakeToast(mContext.getString(R.string.current_new));
									broadcastFoundNoOtaPackage();
								}
								break;
							}catch(Exception e) {
								//e.printStackTrace();
								LOG("request remote server error...");
//								myMakeToast(mContext.getString(R.string.current_new));
							}
							
							try{
								Thread.sleep(5000);
							}catch(InterruptedException e) {
								e.printStackTrace();
							}
						}
						break;

					
                case COMMAND_DELETE_UPDATEPACKAGE:
                	//if mIsNeedDeletePackage == true delete the package
					if(mIsNeedDeletePackage) {
						LOG("execute COMMAND_DELETE_UPDATEPACKAGE...");
						File f = new File(mLastUpdatePath);
						if(f.exists()) {
							f.delete();
							LOG("delete complete! path=" + mLastUpdatePath);
						}else {
							LOG("path=" + mLastUpdatePath + " ,file not exists!");
						}
						
						mIsNeedDeletePackage = false;
						mWorkHandleLocked = false;
					}
					
                	break;
                default:
                    break; 
            }
        }

    }  

    private String[] getValidFirmwareImageFile(String searchPaths[]) {
		for ( String dir_path : searchPaths) {
            String filePath = dir_path + OTA_PACKAGE_FILE;    
            LOG("getValidFirmwareImageFile() : Target image file path : " + filePath);
           
            if ((new File(filePath)).exists()) {
                return (new String[] {filePath} );
            }
        }

		//find rkimage
        for ( String dir_path : searchPaths) {
            String filePath = dir_path + RKIMAGE_FILE;
            //LOG("getValidFirmwareImageFile() : Target image file path : " + filePath);
           
            if ( (new File(filePath) ).exists() ) {
                return (new String[] {filePath} );
            }
        }
        
        if(mIsSupportUsbUpdate) {
	        //find usb device update package
	        File usbRoot = new File(USB_ROOT);
	        if(usbRoot.listFiles() == null) {
	        	return null;
	        }
	        
	        for(File tmp : usbRoot.listFiles()) {
	        	if(tmp.isDirectory()) {
	        		File[] files = tmp.listFiles(new FileFilter() {
	
						@Override
						public boolean accept(File arg0) {
							LOG("scan usb files: " + arg0.getAbsolutePath());
							if(arg0.isDirectory()) {
								return false;
							}
							
							if(arg0.getName().equals(RKIMAGE_FILE) || arg0.getName().equals(OTA_PACKAGE_FILE)){
								return true;
							}
							return false;
						}
	        			
	        		});
	        		
	        		if(files != null && files.length > 0) {
	        			return new String[] {files[0].getAbsolutePath()};
	        		}
	        	}
	        }
        }
        
        return null;
    }

    native private static String getImageVersion(String path);

    native private static String getImageProductName(String path);

    private void startProposingActivity(String path, String imageVersion, String currentVersion) {
    	
        Intent intent = new Intent();

        intent.setComponent(new ComponentName("com.android.tv.settings", "com.mylove.tv.rksetting.update.FirmwareUpdatingActivity") );
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(EXTRA_IMAGE_PATH, path);
        intent.putExtra(EXTRA_IMAGE_VERSION, imageVersion);
        intent.putExtra(EXTRA_CURRENT_VERSION, currentVersion);

        mContext.startActivity(intent);
    }
   
	private boolean checkRKimage(String path){
		String imageProductName = getImageProductName(path);
		LOG("checkRKimage() : imageProductName = " + imageProductName);
		if(imageProductName == null) {
			return false;
		}
		
		if(imageProductName.trim().equals(getProductName())){
			return true;
		}else {
			return false;
		}	
	} 

	private String getOtaPackageFileName() {
		String str = SystemProperties.get("ro.ota.packagename");	
		if(str == null || str.length() == 0) {
			return null;
		}
		if(!str.endsWith(".zip")) {
			return str + ".zip";
		}
		
		return str;
	}
	
	private String getRKimageFileName() {
		String str = SystemProperties.get("ro.rkimage.name");	
		if(str == null || str.length() == 0) {
			return null;
		}
		if(!str.endsWith(".img")) {
			return str + ".img";
		}
		
		return str;
	}
	
    private String getCurrentFirmwareVersion() {    
    	return SystemProperties.get("ro.firmware.version");
    }
    
    private static String getProductName() { 
    	return SystemProperties.get("ro.product.model");        
    }
    
    
    private void notifyInvalidImage(String path) {
        Intent intent = new Intent();

        intent.setComponent(new ComponentName("android.rockchip.update.service", "android.rockchip.update.service.InvalidFirmwareImageActivity") );
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(EXTRA_IMAGE_PATH, path); 

        mContext.startActivity(intent);
    }
    
    private void makeToast(final CharSequence msg) {
    	mMainHandler.post(new Runnable(){  
            public void run(){  
            	Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
            }  
        });  
    }
    
    /**********************************************************************************************************************
    											ota update
    ***********************************************************************************************************************/
    public static String getRemoteUri() {
    	return "http://" + getRemoteHost() + "/OtaUpdater/android?product=" + getOtaProductName() + "&version=" + getSystemVersion()
    			+ "&sn=" + getProductSN() + "&country=" + getCountry() + "&language=" + getLanguage();
    } 
    
    public static String getRemoteUriBackup() {
    	return "http://" + getRemoteHostBackup() + "/OtaUpdater/android?product=" + getOtaProductName() + "&version=" + getSystemVersion()
    			+ "&sn=" + getProductSN() + "&country=" + getCountry() + "&language=" + getLanguage();
    }
    
    public static String getRemoteHost() {
    	String remoteHost = SystemProperties.get("ro.product.ota.host");
    	if(remoteHost == null || remoteHost.length() == 0) {
    		remoteHost = "192.168.1.143:2300";
    	}
    	return remoteHost;
    }
    
    public static String getRemoteHostBackup() {
    	String remoteHost = SystemProperties.get("ro.product.ota.host2");
    	if(remoteHost == null || remoteHost.length() == 0) {
    		remoteHost = "192.168.1.143:2300";
    	}
    	return remoteHost;
    }
    
    public static String getOtaProductName() {
    	String productName = SystemProperties.get("ro.product.model");
    	if(productName.contains(" ")) {
    		productName = productName.replaceAll(" ", "");
    	}
    	
    	return productName;
    }
    
    public static boolean getMultiUserState() {    	
    	String multiUser = SystemProperties.get("ro.factory.hasUMS");
    	if(multiUser != null && multiUser.length() > 0) {
    		return !multiUser.equals("true");
    	}
    	
    	multiUser = SystemProperties.get("ro.factory.storage_policy");
    	if(multiUser != null && multiUser.length() > 0) {
    		return multiUser.equals("1");
    	}
    	
    	return false;
    }
    
    private void startNotifyActivity() {
    	Intent intent = new Intent(mContext, OtaUpdateNotifyActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra("uri", mTargetURI);
		intent.putExtra("OtaPackageLength", mOtaPackageLength);
		intent.putExtra("OtaPackageName", mOtaPackageName);
		intent.putExtra("OtaPackageVersion", mOtaPackageVersion);
		intent.putExtra("SystemVersion", mSystemVersion);
		intent.putExtra("description", mDescription);
		intent.putExtra("ForceUpdate", mForceUpdate);
		mContext.startActivity(intent);
		mWorkHandleLocked = true;
    }

	private void broadcastFoundNewOtaPackage() {	

		Intent intent = new Intent("p03-2.found.new.otapackage");    	
		intent.putExtra("uri", mTargetURI);
		intent.putExtra("OtaPackageLength", mOtaPackageLength);
		intent.putExtra("OtaPackageName", mOtaPackageName);
		intent.putExtra("OtaPackageVersion", mOtaPackageVersion);
		intent.putExtra("SystemVersion", mSystemVersion);
		intent.putExtra("description", mDescription);
		intent.putExtra("ForceUpdate", mForceUpdate); 	  
		sendBroadcast(intent);	
	}  
	
	private void broadcastFoundNoOtaPackage() {	
		Intent intent = new Intent();
		intent.setAction("p03.2.found.no.otapackage");		  
		sendBroadcast(intent);	
	}  

	private void broadcastFoundOtaPackage() {	
		Intent intent = new Intent();
		intent.setAction("p03.2.found.a.otapackage");		  
		sendBroadcast(intent);	

	}  
    private void myMakeToast(CharSequence msg) {
//    	if(mIsOtaCheckByHand) {
    		makeToast(msg);
//    	}	
    }
    
    private boolean requestRemoteServerForUpdate(URI remote) throws Exception{
		if(remote == null) {
			return false;
		}
		URL url = remote.toURL();
		HttpURLConnection opConnection = (HttpURLConnection) url.openConnection();
		
        opConnection.setConnectTimeout(5000);
        opConnection.setReadTimeout(5000);
        opConnection.setRequestMethod("GET");
        opConnection.connect();
        if (opConnection.getResponseCode()==200) {
        	Map<String, List<String>> map = opConnection.getHeaderFields();
        	
        	List<String> headLength = map.get("OtaPackageLength");
        	if(headLength != null && headLength.size() > 0){
        		mOtaPackageLength = headLength.get(0);
        	}else{
        		return false;
        	}
        	
        	List<String> headName = map.get("OtaPackageName");
        	if(headName != null && headName.size() > 0){
        		mOtaPackageName = headName.get(0);
        	}else{
        		return false;
        	}
        	
        	List<String> headVersion = map.get("OtaPackageVersion");
        	if(headVersion != null && headVersion.size() > 0){
        		mOtaPackageVersion = headVersion.get(0);
        	}else{
        		return false;
        	}
        	
        	List<String> headForceupdate = map.get("OtaPackageForce");
        	if(headForceupdate != null && headForceupdate.size() > 0){
        		mForceUpdate = headForceupdate.get(0);
        	}else{
        		return false;
        	}
        	
        	List<String> headTargetURI = map.get("OtaPackageUri");
        	if(headTargetURI != null && headTargetURI.size() > 0){
        		mTargetURI = headTargetURI.get(0);
        	}else{
        		return false;
        	}
        	
        	List<String> headDescription = map.get("description");
        	if(headDescription != null && headDescription.size() > 0){
        		mDescription = new String(headDescription.get(0).getBytes("ISO8859_1"), "UTF-8");
        	}
        	
        }else{
        	return false;
        }
//
////		HttpClient httpClient = CustomerHttpClient.getHttpClient();
//		HttpClient httpClient = null;
//    	HttpHead httpHead = new HttpHead(remote); 
//    	
//	    HttpResponse response = httpClient.execute(httpHead);       
//	    int statusCode = response.getStatusLine().getStatusCode();    
//	    
//	    if(statusCode != 200) {
//	    	return false;    
//	    }
//	    if(DEBUG){    
//	        for(Header header : response.getAllHeaders()){    
//	            LOG(header.getName()+":"+header.getValue());    
//	        }    
//	    }
//	    
//	    Header[] headLength = response.getHeaders("OtaPackageLength");
//	    if(headLength != null && headLength.length > 0) {
//	    	mOtaPackageLength = headLength[0].getValue();
//	    }
//	    
//	    Header[] headName = response.getHeaders("OtaPackageName");
//	    if(headName == null) {
//	    	return false;
//	    }
//	    if(headName.length > 0) {
//	    	mOtaPackageName = headName[0].getValue();
//	    }
//	    
//	    Header[] headVersion = response.getHeaders("OtaPackageVersion");
//	    if(headVersion != null && headVersion.length > 0) {
//	    	mOtaPackageVersion = headVersion[0].getValue();
//	    }
//
//	    Header[] headForceupdate = response.getHeaders("OtaPackageForce");
//	    if(headForceupdate != null && headForceupdate.length > 0) {
//	    	mForceUpdate = headForceupdate[0].getValue();
//	    }
//
//		
//	    Header[] headTargetURI = response.getHeaders("OtaPackageUri");
//	    if(headTargetURI == null) {
//	    	return false;
//	    }
//	    if(headTargetURI.length > 0) {
//	    	mTargetURI = headTargetURI[0].getValue();
//	    }
//	    
//	    if(mOtaPackageName == null || mTargetURI == null) {
//	    	LOG("server response format error!");
//	    	return false;
//	    }
//	    
//	    //get description from server response.
//	    Header[] headDescription = response.getHeaders("description");
//	    if(headDescription != null && headDescription.length > 0) {
//	    	mDescription = new String(headDescription[0].getValue().getBytes("ISO8859_1"), "UTF-8");
//	    }
//	    
//	    if(!mTargetURI.startsWith("http://") && !mTargetURI.startsWith("https://") && !mTargetURI.startsWith("ftp://")) {
//	    	mTargetURI = "http://" + (mUseBackupHost? getRemoteHostBackup() : getRemoteHost()) + (mTargetURI.startsWith("/") ? mTargetURI : ("/" + mTargetURI));
//	    }
//	    
	    mSystemVersion = getSystemVersion();
	    
//	    LOG("OtaPackageName = " + mOtaPackageName + " OtaPackageVersion = " + mOtaPackageVersion 
//	    			+ " OtaPackageLength = " + mOtaPackageLength + " SystemVersion = " + mSystemVersion
//	    			+ "OtaPackageUri = " + mTargetURI + " OtaForceUpdate= "+mForceUpdate);
	    return true;
    }
    
    public static String getSystemVersion() {
    	String version = SystemProperties.get("ro.product.version");
    	if(version == null || version.length() == 0) {
    		version = "1.0.0";
    	}
    	
    	return version;
    }
    
    public static String getProductSN() {
    	String sn = SystemProperties.get("ro.serialno");
    	if(sn == null || sn.length() == 0) {
    		sn = "unknown";
    	}
    	
    	return sn;
    }
    
    public static String getCountry() {
    	return Locale.getDefault().getCountry();
    }
    
    public static String getLanguage() {
    	return Locale.getDefault().getLanguage();
    }
    
}
