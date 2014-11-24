package com.haier.xiaoyi.ui;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.haier.xiaoyi.MainApplication;
import com.haier.xiaoyi.R;
import com.haier.xiaoyi.util.Logger;
import com.haier.xiaoyi.videochat.VideoChat;
import com.haier.xiaoyi.wifip2p.module.WifiP2pConfigInfo;

public class SmartHomeActivity extends Activity implements View.OnClickListener {

	/******************************
	 * Macros <br>
	 ******************************/
	private static final String TAG = "ParentActivity";

	/******************************
	 * public Members <br>
	 ******************************/

	/******************************
	 * private Members <br>
	 ******************************/
	/** Layouts & Views */
	private View mBtn1;
	private View mBtn2;
//	private View mBtn3;
//	private View mBtn4;

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
		case R.id.smart_home_btn_1:
			Logger.d(TAG, "care1_btn");
			handleSmartHomeBtnClick();
			break;
		case R.id.smart_home_btn_2:
			Logger.d(TAG, "care2_btn");
			break;
//		case R.id.care3_btn:
//			Logger.d(TAG, "care3_btn");
//			break;
//		case R.id.care4_btn:
//			Logger.d(TAG, "care4_btn");
//			break;
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
		setContentView(R.layout.smart_home_layout);
	}

	private void initLayoutsAndViews() {
		mBtn1 = (TextView) findViewById(R.id.smart_home_btn_1);
		mBtn1.setOnClickListener(this);

		mBtn2 = (TextView) findViewById(R.id.smart_home_btn_2);
		mBtn2.setOnClickListener(this);
//
//		mBtn3 = (TextView) findViewById(R.id.care3_btn);
//		mBtn3.setOnClickListener(this);
//
//		mBtn4 = (TextView) findViewById(R.id.care4_btn);
//		mBtn4.setOnClickListener(this);
	}
	
	private void handleSmartHomeBtnClick(){
		String ip = ((MainApplication) getApplication()).getXiaoyi().getHostIp();
		new Thread(new OpenClientVideoRunnable(ip)).start();
		startActivity(new Intent(SmartHomeActivity.this,VideoChat.class));
	}
	
	class OpenClientVideoRunnable implements Runnable {

		private String mIp;

		OpenClientVideoRunnable(String ip) {
			mIp = ip;
		}

		@Override
		public void run() {
			/* Construct socket */
			Socket socket = new Socket();

			try {
				socket.bind(null);
				socket.connect((new InetSocketAddress(mIp, WifiP2pConfigInfo.LISTEN_PORT)),
						WifiP2pConfigInfo.SOCKET_TIMEOUT);// host

				Logger.d(TAG, "Client socket - " + socket.isConnected());
				OutputStream stream = socket.getOutputStream();
				// send cmd
				stream.write(WifiP2pConfigInfo.COMMAND_ID_START_CLIENT_VIDEO);

			} catch (IOException e) {
				Logger.e(TAG, e.getMessage());
			} finally {
				if (socket != null) {
					if (socket.isConnected()) {
						try {
							socket.close();
							Logger.d(TAG, "socket.close();");
						} catch (IOException e) {
							// Give up
							e.printStackTrace();
						}
					}
				}
			}
		}

	}

}
