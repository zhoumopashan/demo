package com.haier.xiaoyi.ui;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Pair;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.TextView;
import android.widget.TimePicker;

import com.haier.xiaoyi.MainApplication;
import com.haier.xiaoyi.R;
import com.haier.xiaoyi.util.Logger;
import com.haier.xiaoyi.wifip2p.module.Utility;
import com.haier.xiaoyi.wifip2p.module.WifiP2pConfigInfo;

public class SetDateActivity extends Activity implements View.OnClickListener {

	/******************************
	 * Macros <br>
	 ******************************/
	private static final String TAG = "ClockActivity";
	private static final int GET_UP = 0;
	private static final int SLEEP = 1;
	private static final int EAT = 2;
	private int mClockType = GET_UP;

	/******************************
	 * public Members <br>
	 ******************************/

	/******************************
	 * private Members <br>
	 ******************************/
	/** Layouts & Views */
	private TextView mTitle;
	private TimePicker mTimePicker;
	private View mOk;
	private View mClose;

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
		((MainApplication) getApplication()).removeActivity(this);
		super.onDestroy();
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.push_image:
			Logger.d(TAG, "push_image");
			startSelectImage();
			break;
		case R.id.setting_bright:
			Logger.d(TAG, "setting_bright");
			break;
		case R.id.setting_volumn:
			Logger.d(TAG, "setting_volumn");
			break;
		case R.id.persional:
			Logger.d(TAG, "persional");
			break;
		default:
			break;
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// User has picked an image. Transfer it to group owner i.e peer using
		if (resultCode != RESULT_OK) {
			return;
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
		((MainApplication) getApplication()).addActivity(this);
	}

	private void initWindow() {
		// Define Custom Window Title
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.clock_layout);
	}

	private void initLayoutsAndViews() {
		mTitle = (TextView) findViewById(R.id.clock_layout_title);
		mTimePicker = (TimePicker) findViewById(R.id.clock_layout_datepicker);
		mTimePicker.setIs24HourView(true);
		mTimePicker.setEnabled(true);
		mOk = findViewById(R.id.clock_layout_ok);
		mClose = findViewById(R.id.clock_layout_close);

		Intent intent = getIntent();
		if (intent == null || intent.getAction() == null) {
			return;
		}

		if (intent.getAction().equals("get_up")) {
			mClockType = GET_UP;
			mTitle.setText(R.string.clock_getup);
		} else if (intent.getAction().equals("sleep")) {
			mClockType = SLEEP;
			mTitle.setText(R.string.clock_sleep);
		} else if (intent.getAction().equals("eat")) {
			mClockType = EAT;
			mTitle.setText(R.string.clock_eat);
		}

		mOk.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				int h = mTimePicker.getCurrentHour();
				int m = mTimePicker.getCurrentMinute();
				Logger.d(TAG, h + " : " + m);
				
				// Logger.d(TAG, "onStopTrackingTouch");
				String ip = ((MainApplication) getApplication()).getXiaoyi().getHostIp();
				new Thread(new SendClockRunnable(ip, h,m , mTitle.getText().toString())).start();
			}
		});
		
		mClose.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String ip = ((MainApplication) getApplication()).getXiaoyi().getHostIp();
				new Thread(new SendCloseClockRunnable(ip) ).start();
			}
		});

	}

	/** Show a image Select dialog, let user to select a image to send */
	private void startSelectImage() {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("image/*");
		startActivityForResult(intent, WifiP2pConfigInfo.REQUEST_CODE_SELECT_IMAGE);
	}

	/** Get the file's info */
	public String getFileInfo(Uri uri) {
		// get the name & fileSize of the uri-file
		Pair<String, Integer> pair;
		try {
			pair = Utility.getFileNameAndSize(SetDateActivity.this, uri);
		} catch (IOException e) {
			return null;
		}
		String name = pair.first;
		long size = pair.second;

		return "size:" + size + "name:" + name;
	}

	/**
	 * Get the file's inputStream by uri
	 */
	public InputStream getInputStream(Uri uri) {
		ContentResolver cr = getContentResolver();
		try {
			return cr.openInputStream(uri);
		} catch (FileNotFoundException e) {
			return null;
		}
	}

	/**
	 * A send file task, send the file(uri) to the device mark by host & port
	 * 
	 * @author luochenxun
	 */
	class SendClockRunnable implements Runnable {

		private String mIp;
		private int mHour;
		private int mMin;
		private String mClockMsg;

		SendClockRunnable(String ip, int hour , int min , String msg) {
			mIp = ip;
			mHour = hour;
			mMin = min;
			mClockMsg = msg;
		}

		@Override
		public void run() {
			/* Construct socket */
			Socket socket = new Socket();
			int port = WifiP2pConfigInfo.LISTEN_PORT;

			try {
				socket.bind(null);
				
				if(((MainApplication) getApplication()).getXiaoyi().isWifiAvailable()){
					mIp = ((MainApplication) getApplication()).getXiaoyi().getWifiIp();
					port = WifiP2pConfigInfo.WIFI_PORT;
				}
				
				socket.connect((new InetSocketAddress(mIp,port)), WifiP2pConfigInfo.SOCKET_TIMEOUT);// host

				Logger.d(TAG, "Client socket - " + socket.isConnected());
				OutputStream stream = socket.getOutputStream();
				// send cmd
				stream.write(WifiP2pConfigInfo.COMMAND_ID_CLOCK);
				// send data
				stream.write(mClockType);
				stream.write(mHour);
				stream.write(mMin);
				mClockMsg += "     " +  "      \n" ;
				stream.write(mClockMsg.getBytes(), 0, mClockMsg.length());

				Logger.d(TAG, "Client: Data written strSend:" + mClockMsg);
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
	
	class SendCloseClockRunnable implements Runnable {

		private String mIp;
		private int mHour;
		private int mMin;
		private String mClockMsg;

		SendCloseClockRunnable(String ip) {
			mIp = ip;
		}

		@Override
		public void run() {
			/* Construct socket */
			Socket socket = new Socket();
			int port = WifiP2pConfigInfo.LISTEN_PORT;

			try {
				socket.bind(null);
				
				if(((MainApplication) getApplication()).getXiaoyi().isWifiAvailable()){
					mIp = ((MainApplication) getApplication()).getXiaoyi().getWifiIp();
					port = WifiP2pConfigInfo.WIFI_PORT;
				}
				
				socket.connect((new InetSocketAddress(mIp,port)), WifiP2pConfigInfo.SOCKET_TIMEOUT);// host

				Logger.d(TAG, "Client socket - " + socket.isConnected());
				OutputStream stream = socket.getOutputStream();
				// send cmd
				stream.write(WifiP2pConfigInfo.COMMAND_ID_CLOSE_CLOCK);
				// send data
				stream.write(mClockType);

				Logger.d(TAG, "Client: Data written strSend:" + mClockMsg);
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
