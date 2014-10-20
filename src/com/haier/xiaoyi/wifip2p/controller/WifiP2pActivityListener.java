package com.haier.xiaoyi.wifip2p.controller;

import android.app.Activity;
import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Message;

public interface WifiP2pActivityListener extends WifiP2pServiceListener{
	public void showDiscoverPeers();
	public void onDisconnect();
	public void resetPeers();
	public void updateLocalDevice(WifiP2pDevice device);
	public void sendMessage(Message msg);
	public Activity getActivity();
}