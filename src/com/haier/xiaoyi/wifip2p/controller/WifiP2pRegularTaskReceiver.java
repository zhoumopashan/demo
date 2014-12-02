package com.haier.xiaoyi.wifip2p.controller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

/**
 * A BroadcastReceiver that notifies of important wifi p2p events.
 */
public class WifiP2pRegularTaskReceiver extends BroadcastReceiver {
	
	private static final String TAG = "WifiP2pRegularTaskReceiver";


    /**
     * @param manager WifiP2pManager system service
     * @param channel Wifi p2p channel
     * @param activity activity associated with the receiver
     */
    public WifiP2pRegularTaskReceiver() {
        super();
    }

    @Override
	public void onReceive(Context context, Intent intent) {

		/* Param check */
		if (intent == null) {
			return;
		}
		String action = intent.getAction();
		if (action == null || TextUtils.isEmpty(action)) {
			return;
		}

		if("regular_jobs".equals(action)){
			context.startService( new Intent(context,WifiP2pService.class).setAction("regular_job"));
		}
	}
}