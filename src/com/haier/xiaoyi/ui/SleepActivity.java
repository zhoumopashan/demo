package com.haier.xiaoyi.ui;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

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
import com.haier.xiaoyi.wifip2p.module.WifiP2pConfigInfo;

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
			// switch (msg.what) {
			// case MSG_SHOWDIALOG:
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
		((MainApplication) getApplication()).removeActivity(this);
		super.onDestroy();
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}

	@Override
	public void onClick(View v) {

		String ip = ((MainApplication) getApplication()).getXiaoyi().getHostIp();
		int sleepTime = -1;
		switch (v.getId()) {
		case R.id.sleep15s:
			sleepTime = 15;
			Logger.d(TAG, "setting1_btn");
			break;
		case R.id.sleep30s:
			sleepTime = 30;
			Logger.d(TAG, "setting2_btn");
			break;
		case R.id.sleep1m:
			sleepTime = 60;
			Logger.d(TAG, "setting3_btn");
			break;
		case R.id.sleep2m:
			sleepTime = 120;
			Logger.d(TAG, "setting4_btn");
			break;
		case R.id.never:
			sleepTime = -1;
			Logger.d(TAG, "setting4_btn");
			break;
		default:
			break;
		}

		new Thread(new SendDeviceInfoRunnable(ip, sleepTime)).start();
	}

	/******************************
	 * public Methods <br>
	 ******************************/

	/******************************
	 * private Methods <br>
	 ******************************/

	private void initEnvironment() {
		((MainApplication) getApplication()).addActivity(this);
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

	class SendDeviceInfoRunnable implements Runnable {

		private String mIp;
		private int mSleepTime;

		SendDeviceInfoRunnable(String ip, int sleepTime) {
			mIp = ip;
			mSleepTime = sleepTime;
		}

		@Override
		public void run() {
			/* Construct socket */
			Socket socket = new Socket();

			try {
				int port = WifiP2pConfigInfo.LISTEN_PORT;
				socket.bind(null);

				if (((MainApplication) getApplication()).getXiaoyi().isWifiAvailable()) {
					mIp = ((MainApplication) getApplication()).getXiaoyi().getWifiIp();
					port = WifiP2pConfigInfo.WIFI_PORT;
				}

				socket.connect((new InetSocketAddress(mIp, port)), WifiP2pConfigInfo.SOCKET_TIMEOUT);// host

				Logger.d(TAG, "Client socket - " + socket.isConnected());
				OutputStream stream = socket.getOutputStream();
				// send cmd
				stream.write(WifiP2pConfigInfo.COMMAND_ID_SLEEP_TIME);
				// send data
				String strSend = "sleep:" + mSleepTime;
				stream.write(strSend.getBytes(), 0, strSend.length());

				Logger.d(TAG, "Client: Data written strSend:" + strSend);
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
