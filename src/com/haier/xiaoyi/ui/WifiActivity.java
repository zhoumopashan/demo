package com.haier.xiaoyi.ui;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.haier.xiaoyi.MainApplication;
import com.haier.xiaoyi.R;
import com.haier.xiaoyi.util.Logger;
import com.haier.xiaoyi.util.WifiUtil;
import com.haier.xiaoyi.wifip2p.module.WifiP2pConfigInfo;

public class WifiActivity extends Activity implements View.OnClickListener {

	/******************************
	 * Macros <br>
	 ******************************/
	private static final String TAG = "WifiActivity";

	/******************************
	 * public Members <br>
	 ******************************/

	/******************************
	 * private Members <br>
	 ******************************/
	ListView mListView;
	/** wifi util */
	WifiUtil mWifiUtil;
	/** wifi names */
	private ArrayList<String> mWifiNameList = new ArrayList<String>();

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
			Toast.makeText(WifiActivity.this, R.string.setdate_success, Toast.LENGTH_LONG).show();
			finish();
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

		initWindow();
		initLayoutsAndViews();
		initEnvironment();
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
			break;
		case R.id.setting2:
			Logger.d(TAG, "setting2_btn");
			break;
		case R.id.setting3:
			Logger.d(TAG, "setting3_btn");
			startActivity(new Intent(this,SleepActivity.class));
			break;
		case R.id.setting4:
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
		
		mWifiUtil = new WifiUtil(getApplicationContext());
		mWifiUtil.openWifi();
		mWifiNameList = mWifiUtil.getScanResult();
		this.updateWifiList();
	}

	private void initWindow() {
		// Define Custom Window Title
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.wifi_layout);
	}

	private void initLayoutsAndViews() {
		mListView = (ListView) findViewById(R.id.wifi_list);
		mListView.setVisibility(View.GONE);
		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				final String ssid = mWifiNameList.get(position);
				Logger.d(TAG, "ssid:" + ssid);
				
				LayoutInflater inflater = LayoutInflater.from(WifiActivity.this);
				final View textEntryView = inflater.inflate(R.layout.dialoglayout, null);
				final EditText edtInput = (EditText) textEntryView.findViewById(R.id.edtInput);
				final AlertDialog.Builder builder = new AlertDialog.Builder(WifiActivity.this);
				builder.setCancelable(true);
				builder.setTitle("请输入密码");
				builder.setView(textEntryView);
				builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						Logger.d(TAG, "pass word :" + edtInput.getText().toString());
//						mWifiUtil.connectNetwork( ssid , edtInput.getText().toString());
						// ssid & psw
						String ip = ((MainApplication) getApplication()).getXiaoyi().getHostIp();
						new Thread(new SendWifiInfoRunnable(ip , ssid, edtInput.getText().toString())).start();
					}
				});
				builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
//						setTitle("");
					}
				});
				builder.show();
			}
		});
	}
	
	private void updateWifiList(){
		mListView.setVisibility(View.GONE);
		mListView.setAdapter(new ArrayAdapter<String>(this, R.layout.wifi_list_item, mWifiNameList ));
		mListView.setVisibility(View.VISIBLE);
	}

	class SendWifiInfoRunnable implements Runnable {

		private String mIp;
		private String mSsid;
		private String mPsw;

		SendWifiInfoRunnable(String ip ,  String ssid, String psw ) {
			this.mIp = ip;
			this.mSsid = ssid;
			this.mPsw = psw;
		}

		@Override
		public void run() {
			/* Construct socket */
			Socket socket = new Socket();
			boolean isSuccess = true;

			try {
				socket.bind(null);
				socket.connect((new InetSocketAddress(mIp, WifiP2pConfigInfo.LISTEN_PORT)), WifiP2pConfigInfo.SOCKET_TIMEOUT);// host

				Logger.d(TAG, "Client socket - " + socket.isConnected());
				OutputStream stream = socket.getOutputStream();
				// send cmd
				stream.write(WifiP2pConfigInfo.COMMAND_ID_SEND_WIFI);
				// send data
				String strSend = "xiaoyissid:" + mSsid + "xiaoyisspsw:" + mPsw;
				stream.write(strSend.getBytes(), 0, strSend.length());

				Logger.d(TAG, "Client: Data written strSend:" + strSend);
			} catch (IOException e) {
				isSuccess = false;
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
			
			if(isSuccess){
				mMainHandler.sendEmptyMessage(100);
			}
		}

	}
	
}
