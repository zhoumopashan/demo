package com.haier.xiaoyi;

import android.app.Application;
import android.content.Intent;

import com.haier.xiaoyi.wifip2p.controller.WifiP2pService;

public class MainApplication extends Application {

	private static XiaoYi mXiaoyi = new XiaoYi();

	@Override
	public void onCreate() {
		startService(new Intent(this, WifiP2pService.class));
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
}
