package com.haier.xiaoyi.wifip2p.controller;

import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;

public interface WifiP2pServiceListener extends ConnectionInfoListener, PeerListListener {
	public void updateLocalDevice(WifiP2pDevice device);
}