package com.haier.xiaoyi.client;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.haier.xiaoyi.client.controller.WifiP2pService;

public class BootReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context arg0, Intent arg1) {
		arg0.startService(new Intent(arg0, WifiP2pService.class).setAction("discover_peers"));
	}

}
