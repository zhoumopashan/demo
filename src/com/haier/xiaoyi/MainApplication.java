package com.haier.xiaoyi;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.haier.xiaoyi.util.Logger;
import com.haier.xiaoyi.wifip2p.controller.WifiP2pService;

public class MainApplication extends Application {
	private final static String TAG = "MainApplication";

	private static XiaoYi mXiaoyi = new XiaoYi();
	private List<Activity> activities = new ArrayList<Activity>();
	public static int ScreenHigh , ScreenWidth;

	@Override
	public void onCreate() {
		super.onCreate();
		startService(new Intent(this, WifiP2pService.class));
		initEnvironment();
	}

	@Override
	public void onTerminate() {
		stopService(new Intent(this, WifiP2pService.class));
		super.onTerminate();
		for (Activity activity : activities) {
			if (activity != null) {
				try {
					activity.finish();
				} catch (Exception ex) {
					Logger.i(TAG, "activity finish error");
				}
			}
		}
		Logger.i(TAG, "terminate");
		// System.exit(0);
	}

	public void addActivity(Activity activity) {
		activities.add(activity);
	}

	public void removeActivity(Activity activity) {
		activities.remove(activity);
	}

	public XiaoYi getXiaoyi() {
		return mXiaoyi;
	}

	public void setXiaoyi(XiaoYi xiaoyi) {
		mXiaoyi = xiaoyi;
	}

	private void initEnvironment() {
		// Get Screen High & Width
		DisplayMetrics dm = new DisplayMetrics();
		WindowManager manager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		manager.getDefaultDisplay().getMetrics(dm);
		ScreenHigh = dm.widthPixels;
		ScreenWidth = dm.heightPixels;
	}

}
