package com.luo.demo.ui.viewpager;

import android.app.Activity;
import android.view.View;

public abstract class SubActivity {
	protected Activity mParent;
	protected View mMainView;

	public SubActivity(Activity activity) {
		mParent = activity;
	}

	public abstract void onCreate();

	public abstract View onCreateView();

	public abstract void onResume();

	public abstract void onStop();

	public abstract void onDestory();

	/**
	 * Set Content View
	 */
	public void setContentView(int res) {
		mMainView = mParent.getLayoutInflater().inflate(res, null);
	}

	/**
	 * Get MainView
	 */
	public View getMainView() {
		return mMainView;
	}
}
