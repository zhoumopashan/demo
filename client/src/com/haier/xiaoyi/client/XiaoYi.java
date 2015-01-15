package com.haier.xiaoyi.client;

import android.net.Uri;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;

public class XiaoYi {
	private int mVolice = 0;
	private int mBright = 0;
	private boolean mIsConnect = false;
	private WifiP2pDevice mDevice;
	private WifiP2pInfo mWifiP2pInfo;
	private String mHostIp = null;
	private Uri mPhotoUri = null;
	
	// wifi info
	private String mWifiIp = null;
	private boolean mIsWifiAvailable = false;
	
	// xiaoyi info
	private String mName = "大伊";
	private String mAge = "18岁";

	public int getVolice() {
		return mVolice;
	}

	public void setVolice(int mVolice) {
		this.mVolice = mVolice;
	}

	public int getBright() {
		return mBright;
	}

	public void setBright(int mBright) {
		
		this.mBright = mBright;
	}
	
	public void setIsConnect(boolean isConnect){
		mIsConnect = isConnect;
	}
	
	public boolean isConnect(){
		return mIsConnect;
	}

	public WifiP2pDevice getDevice() {
		return mDevice;
	}

	public void setDevice(WifiP2pDevice mDevice) {
		this.mDevice = mDevice;
	}

	public WifiP2pInfo getWifiP2pInfo() {
		return mWifiP2pInfo;
	}

	public void setWifiP2pInfo(WifiP2pInfo mWifiP2pInfo) {
		this.mWifiP2pInfo = mWifiP2pInfo;
	}

	public String getHostIp() {
		return mHostIp;
	}

	public void setHostIp(String mHostIp) {
		this.mHostIp = mHostIp;
	}

	public Uri getPhotoUri() {
		return mPhotoUri;
	}

	public void setPhotoUri(Uri mPhotoUri) {
		this.mPhotoUri = mPhotoUri;
	}

	public String getWifiIp() {
		return mWifiIp;
	}

	public void setWifiIp(String mWifiIp) {
		this.mWifiIp = mWifiIp;
	}

	public boolean isWifiAvailable() {
		return mIsWifiAvailable;
	}

	public void setWifiAvailable(boolean mIsWifiAvailable) {
		this.mIsWifiAvailable = mIsWifiAvailable;
	}

	public String getName() {
		return mName;
	}

	public void setName(String mName) {
		this.mName = mName;
	}

	public String getAge() {
		return mAge;
	}

	public void setAge(String mAge) {
		this.mAge = mAge;
	}
	
}
