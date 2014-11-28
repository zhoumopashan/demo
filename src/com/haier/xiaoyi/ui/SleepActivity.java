package com.haier.xiaoyi.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.haier.xiaoyi.MainApplication;
import com.haier.xiaoyi.R;
import com.haier.xiaoyi.util.Logger;

public class SleepActivity extends Activity implements View.OnClickListener {

	/******************************
	 * Macros <br>
	 ******************************/
	private static final String TAG = "SleepActivity";

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
	private View mBtn5;

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
		case R.id.sleep15s:
			Logger.d(TAG, "setting1_btn");
			break;
		case R.id.sleep30s:
			Logger.d(TAG, "setting2_btn");
			break;
		case R.id.sleep1m:
			Logger.d(TAG, "setting3_btn");
//			startActivity(new Intent(this,ClockActivity.class).setAction("eat"));
			break;
		case R.id.sleep2m:
			Logger.d(TAG, "setting4_btn");
			break;
		case R.id.never:
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
		setContentView(R.layout.sleep_layout);
	}

	private void initLayoutsAndViews() {
		mBtn1 = (TextView) findViewById(R.id.sleep15s);
		mBtn1.setOnClickListener(this);

		mBtn2 = (TextView) findViewById(R.id.sleep30s);
		mBtn2.setOnClickListener(this);

		mBtn3 = (TextView) findViewById(R.id.sleep1m);
		mBtn3.setOnClickListener(this);

		mBtn4 = (TextView) findViewById(R.id.sleep2m);
		mBtn4.setOnClickListener(this);

		mBtn5 = (TextView) findViewById(R.id.never);
		mBtn5.setOnClickListener(this);
	}

}
