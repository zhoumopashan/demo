package com.haier.xiaoyi.client;

import android.app.Application;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.haier.xiaoyi.client.controller.WifiP2pActivityListener;
import com.haier.xiaoyi.client.controller.WifiP2pService;
import com.haier.xiaoyi.client.module.WifiP2pConfigInfo;
import com.haier.xiaoyi.client.util.Logger;

public class MainApplication extends Application {

	private static XiaoYi mXiaoyi = new XiaoYi();
	private WifiP2pActivityListener mUiListener = null;
	public static int ScreenHigh , ScreenWidth;

	@Override
	public void onCreate() {
		initEnvironment();
		super.onCreate();
	}

	@Override
	public void onTerminate() {
		// TODO Auto-generated method stub
		super.onTerminate();
	}

	public XiaoYi getXiaoyi() {
		return mXiaoyi;
	}

	public void setXiaoyi(XiaoYi xiaoyi) {
		mXiaoyi = xiaoyi;
	}

	public WifiP2pActivityListener getUiListener() {
		return mUiListener;
	}

	public void setUiListener(WifiP2pActivityListener mUiListener) {
		this.mUiListener = mUiListener;
	}
	
	/******************************
	 * Private methods
	 ***************************/
	
	private void initEnvironment() {
		startService( new Intent(this, WifiP2pService.class).setAction("discover_peers") );
//		startService( new Intent(this, WifiP2pService.class) );
		
		initDevice();
		
		// Get Screen High & Width
		DisplayMetrics dm = new DisplayMetrics();
		WindowManager manager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		manager.getDefaultDisplay().getMetrics(dm);
		ScreenHigh = dm.widthPixels;
		ScreenWidth = dm.heightPixels;
	}
	
	private void initDevice(){
		mXiaoyi.setBright(getScreenBrightness());
		mXiaoyi.setVolice(getDeviceVoice());
		
		if(!WifiP2pConfigInfo.isDebug){
			// set setting
			ContentResolver cr = getContentResolver();
    		ContentValues values = new ContentValues();
    		
    		Cursor cursor = cr.query( Uri.parse("content://com.haier.xiaoyi.settings/XIAOYI_SETTINGS") , null, null, null, null );
    		
    		String name = cursor.getString(cursor.getColumnIndex("COLUMN_XIAOYI_NAME"));
    		String age = cursor.getString(cursor.getColumnIndex("COLUMN_XIAOYI_AGE"));
    		
    		if( !TextUtils.isEmpty(name) && !TextUtils.isEmpty(age) ){
        		mXiaoyi.setName(name);
        		mXiaoyi.setAge(age);
    		}
		}
	}
	
	/**
	 * 获得当前屏幕亮度值 0--255
	 */
	private int getScreenBrightness() {
	    int screenBrightness = 255;
	    try {
	        screenBrightness = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
	    } catch (Exception localException) {
	    }
	    int bright = (int)(((double)screenBrightness / 255D) * 100D);
	    
	    return bright;
	}
	
	/**
	 * 设置当前屏幕亮度值 0--255
	 */
	public void saveScreenBrightness(int paramInt) {
		mXiaoyi.setBright(paramInt);
		int bright = (int)(((double)paramInt / 100D) * 255D);
		Logger.d("application","bright : " + bright);
	    try {
	        Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, bright);
	    } catch (Exception localException) {
	        localException.printStackTrace();
	    }
	}
	
	private int getDeviceVoice() {
		AudioManager mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);    
		int currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC); 
		int maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC); 
		currentVolume = (int)(((double)currentVolume /  (double)maxVolume) * 100D);
	    return currentVolume;
	}
	
	public void setDeviceVoice(int voice) {
		AudioManager mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		int maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC); 
		voice = (int)(((double)voice /  (double)100D) * maxVolume);
		Logger.d("application","voice : " + voice + ", max is :" + maxVolume);
		mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, voice, 0);
	}
}
