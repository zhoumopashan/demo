package com.haier.xiaoyi.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;

import com.haier.xiaoyi.R;
import com.haier.xiaoyi.util.Logger;

public class MainActivity extends Activity implements View.OnClickListener {

	/******************************
	 * Macros <br>
	 ******************************/
	private static final String TAG = "MainActivity";

	/******************************
	 * public Members <br>
	 ******************************/

	/******************************
	 * private Members <br>
	 ******************************/
	/** Layouts & Views */
	private View mMainFuncArea;
	private View mBtnHome;
	private View mBtnParent;
	private View mBtnSetting;
	private View mBtnCare;
	private View mMiddleBtn;

	/******************************
	 * InnerClass <br>
	 ******************************/

	/** Message Hander */
	private MainHandler mMainHandler;

	// MainHandler Definition
	class MainHandler extends Handler {
		// static final int MSG_UPDATE_NEW_VERSION = 100;

		@Override
		public void handleMessage(Message msg) {
			// switch (msg.what) {
			// case MSG_DOWNLOAD_PROCESS_CHANGE:
			// getUpdateHelper().setDialogProcess(msg.arg1);
			// break;
			// default:
			// break;
			// }
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
		super.onDestroy();
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn1:
			Logger.d(TAG, "btn1 click");
			break;
		case R.id.btn2:
			Logger.d(TAG, "btn2 click");
			break;
		case R.id.btn3:
			Logger.d(TAG, "btn3 click");
			startActivity(new Intent(MainActivity.this,ParentActivity.class));
			break;
		case R.id.btn4:
			Logger.d(TAG, "btn4 click");
			startActivity(new Intent(MainActivity.this,CareActivity.class));
			break;
		case R.id.main_func_middle_btn:
			Logger.d(TAG, "main_func_middle_btn click");
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
		// Init Main Handler
		mMainHandler = new MainHandler();
	}

	private void initWindow() {
		// Define Custom Window Title
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
	}

	private void initLayoutsAndViews() {
		mMainFuncArea = findViewById(R.id.main_func_area);
		
		mBtnHome = findViewById(R.id.btn1);
		mBtnHome.setOnClickListener(this);
		mBtnHome.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					mMainFuncArea.setBackgroundResource(R.drawable.color_blue);
					break;

				case MotionEvent.ACTION_UP:
					mMainFuncArea.setBackgroundResource(R.drawable.main_outer_btn);
					break;

				default:
					break;
				}
				return false;
			}
		});

		mBtnParent = findViewById(R.id.btn3);
		mBtnParent.setOnClickListener(this);

		mBtnSetting = findViewById(R.id.btn2);
		mBtnSetting.setOnClickListener(this);

		mBtnCare = findViewById(R.id.btn4);
		mBtnCare.setOnClickListener(this);

		mMiddleBtn = findViewById(R.id.main_func_middle_btn);
		mMiddleBtn.setOnClickListener(this);
	}

}
