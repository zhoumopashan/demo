package com.haier.xiaoyi.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.haier.xiaoyi.MainApplication;
import com.haier.xiaoyi.R;
import com.haier.xiaoyi.util.Logger;

public class SettingActivity extends Activity implements View.OnClickListener {

	/******************************
	 * Macros <br>
	 ******************************/
	private static final String TAG = "SettingActivity";

	/******************************
	 * public Members <br>
	 ******************************/

	/******************************
	 * private Members <br>
	 ******************************/
	/** Layouts & Views */
	private View mBtn1;
	private View mBtn2;
	private View mBtn3;
	private View mBtn4;

	/******************************
	 * InnerClass <br>
	 ******************************/

	/** Message Hander */
	private MainHandler mMainHandler;
	private ProgressDialog mProgressDialog;

	// MainHandler Definition
	class MainHandler extends Handler {
		// static final int MSG_UPDATE_NEW_VERSION = 100;

		@Override
		public void handleMessage(Message msg) {
//			switch (msg.what) {
//			case MSG_SHOWDIALOG:
//				break;
//			default:
//				break;
//			}
		}
	}

	/******************************
	 * Constructor <br>
	 ******************************/

	/******************************
	 * implement Methods <br>
	 ******************************/

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		initEnvironment();
		initWindow();
		initLayoutsAndViews();
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onRestoreInstanceState(savedInstanceState);
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		((MainApplication)getApplication()).removeActivity(this);
		super.onDestroy();
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.setting1:
			Logger.d(TAG, "setting1_btn");
			if(((MainApplication)getApplication()).getXiaoyi().isWifiAvailable()){
				Toast.makeText(SettingActivity.this, R.string.already_login_wifi, Toast.LENGTH_LONG).show();
				return;
			}
			startActivity(new Intent(this,WifiActivity.class));
			break;
		case R.id.setting2:
			startActivity(new Intent(this,SetDateActivity.class));
			Logger.d(TAG, "setting2_btn");
			break;
		case R.id.setting3:
			Logger.d(TAG, "setting3_btn");
			startActivity(new Intent(this,SleepActivity.class));
			break;
		case R.id.setting4:
			startActivity(new Intent(this,XiaoyiActivity.class));
			Logger.d(TAG, "setting4_btn");
			break;
		default:
			break;
		}
	}

	/******************************
	 * public Methods <br>
	 ******************************/

	/******************************
	 * private Methods <br>
	 ******************************/

	private void initEnvironment() {
		((MainApplication)getApplication()).addActivity(this);
		// Init Main Handler
		mMainHandler = new MainHandler();
	}

	private void initWindow() {
		// Define Custom Window Title
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.setting_layout);
	}

	private void initLayoutsAndViews() {
		mBtn1 = (TextView) findViewById(R.id.setting1);
		mBtn1.setOnClickListener(this);

		mBtn2 = (TextView) findViewById(R.id.setting2);
		mBtn2.setOnClickListener(this);

		mBtn3 = (TextView) findViewById(R.id.setting3);
		mBtn3.setOnClickListener(this);

		mBtn4 = (TextView) findViewById(R.id.setting4);
		mBtn4.setOnClickListener(this);

	}

}
