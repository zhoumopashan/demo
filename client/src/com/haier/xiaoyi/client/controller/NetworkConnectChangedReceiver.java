package com.haier.xiaoyi.client.controller;

import com.haier.xiaoyi.client.util.Logger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

public class NetworkConnectChangedReceiver extends BroadcastReceiver {

	public static final String TAG = "NetworkConnectChangedReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
			ConnectivityManager conManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo networkInfo = conManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

			//判断网络连接类型，只有在3G或wifi里进行一些数据更新。
			int netType = networkInfo.getType();
			if (networkInfo.isAvailable() && netType == ConnectivityManager.TYPE_WIFI) {
				WifiManager mWifiManager;
				/** WifiInfo */
				WifiInfo mWifiInfo;
				// Get WifiManager
				mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
				// Get WifiInfo
				mWifiInfo = mWifiManager.getConnectionInfo();
				Logger.d(TAG, "ip:" + intToString(mWifiInfo.getIpAddress()));
				
				context.startService(new Intent(context,WifiP2pService.class).
						setAction("wifi_connect").
						putExtra("wifi_ip", intToString(mWifiInfo.getIpAddress())));
			}else{
				context.startService(new Intent(context,WifiP2pService.class).
						setAction("wifi_disconnect") );
			}
		}
	}

	private static String intToString(int a) {
		StringBuffer sb = new StringBuffer();
		int b = (a >> 0) & 0xff;
		sb.append(b + ".");
		b = (a >> 8) & 0xff;
		sb.append(b + ".");
		b = (a >> 16) & 0xff;
		sb.append(b + ".");
		b = (a >> 24) & 0xff;
		sb.append(b);
		return sb.toString();
	}
}