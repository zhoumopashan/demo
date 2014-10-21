package com.haier.xiaoyi;

import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;

public class XiaoYi {
	private int mVolice = 0;
	private int mBright = 0;
	private boolean mIsConnect = false;
	private WifiP2pDevice mDevice;
	private WifiP2pInfo mWifiP2pInfo;
	private String mHostIp = null;

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
}
