package com.haier.xiaoyi.ui;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Pair;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.haier.xiaoyi.MainApplication;
import com.haier.xiaoyi.R;
import com.haier.xiaoyi.util.Logger;
import com.haier.xiaoyi.wifip2p.module.Utility;
import com.haier.xiaoyi.wifip2p.module.WifiP2pConfigInfo;

public class CareActivity extends Activity implements View.OnClickListener {

	/******************************
	 * Macros <br>
	 ******************************/
	private static final String TAG = "ParentActivity";
	private static final int MSG_SHOWDIALOG = 100;
	private static final int MSG_DISSMISS_DIALOG = 101;
	private static final int MSG_SHOW_DIALOG_PROCESS = 102;

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
			switch (msg.what) {
			case MSG_SHOWDIALOG:
				if (mProgressDialog != null && mProgressDialog.isShowing()) {
					mProgressDialog.dismiss();
				}
				mProgressDialog = new ProgressDialog(CareActivity.this);  
				mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);  
				mProgressDialog.setTitle(getString(R.string.wifip2p_p2p_scanning_title));  
				mProgressDialog.setMessage(getString(R.string.send_long_time));  
				mProgressDialog.setMax(msg.arg1);  
				mProgressDialog.setProgress(0);  
				mProgressDialog.setIndeterminate(false);  
				mProgressDialog.setCancelable(false); 
				mProgressDialog.show();
				break;
			case MSG_DISSMISS_DIALOG:
				if (mProgressDialog != null && mProgressDialog.isShowing()) {
					mProgressDialog.dismiss();
				}
				break;
			case MSG_SHOW_DIALOG_PROCESS:
				if (mProgressDialog != null && mProgressDialog.isShowing()) {
					mProgressDialog.setProgress(msg.arg1);
				}
				break;
			default:
				break;
			}
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
		case R.id.care1_btn:
			Logger.d(TAG, "care1_btn");
			startActivity(new Intent(this,ClockActivity.class).setAction("get_up"));
			break;
		case R.id.care2_btn:
			Logger.d(TAG, "care2_btn");
			startActivity(new Intent(this,ClockActivity.class).setAction("sleep"));
			break;
		case R.id.care3_btn:
			Logger.d(TAG, "care3_btn");
			startActivity(new Intent(this,ClockActivity.class).setAction("eat"));
			break;
		case R.id.care4_btn:
			startSelectVideo();
			Logger.d(TAG, "care4_btn");
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
		setContentView(R.layout.care_layout);
	}

	private void initLayoutsAndViews() {
		
		boolean isBtn1Set = getSharedPreferences("default", 0).getBoolean("clock" + ClockActivity.GET_UP, false);
		boolean isBtn2Set = getSharedPreferences("default", 0).getBoolean("clock" + ClockActivity.SLEEP, false);
		boolean isBtn3Set = getSharedPreferences("default", 0).getBoolean("clock" + ClockActivity.EAT, false);
		
		mBtn1 = (TextView) findViewById(R.id.care1_btn);
		mBtn1.setOnClickListener(this);
		if(isBtn1Set){
			mBtn1.setSelected(true);
		}

		mBtn2 = (TextView) findViewById(R.id.care2_btn);
		mBtn2.setOnClickListener(this);
		if(isBtn2Set){
			mBtn2.setSelected(true);
		}

		mBtn3 = (TextView) findViewById(R.id.care3_btn);
		mBtn3.setOnClickListener(this);
		if(isBtn3Set){
			mBtn3.setSelected(true);
		}

		mBtn4 = (TextView) findViewById(R.id.care4_btn);
		mBtn4.setOnClickListener(this);

	}
	
	/** Show a image Select dialog, let user to select a image to send */
	private void startSelectVideo() {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("audio/*");
		startActivityForResult(intent, WifiP2pConfigInfo.REQUEST_CODE_SELECT_AUDIO);
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// User has picked an image. Transfer it to group owner i.e peer using
		if (resultCode != RESULT_OK) {
			return;
		}

		if (requestCode == WifiP2pConfigInfo.REQUEST_CODE_SELECT_AUDIO) {
			if (data == null) {
				Logger.e(TAG, "onActivityResult data == null, no choice.");
				return;
			}
			Uri uri = data.getData();
			String host= ((MainApplication)getApplication()).getXiaoyi().getHostIp();
			int port = WifiP2pConfigInfo.LISTEN_PORT;
			new Thread(new MySendFileRunable(host,port,uri,getFileInfo(uri),getInputStream(uri))).start();
		}
	}
	
	/**
	 * Get the file's inputStream by uri
	 */
	private InputStream getInputStream(Uri uri) {
		ContentResolver cr = getContentResolver();
		try {
			return cr.openInputStream(uri);
		} catch (FileNotFoundException e) {
			return null;
		}
	}
	
	/** Get the file's info */
	private String getFileInfo(Uri uri){
		// get the name & fileSize of the uri-file
		Pair<String, Integer> pair;
		try {
			pair = Utility.getFileNameAndSize(CareActivity.this, uri);
		} catch (IOException e) {
			return null;
		}
		String name = pair.first;
		long size = pair.second;
		
		return "size:" + size + "name:" + name;
	}
	
	/**
	 * A send file task, send the file(uri) to the device mark by host & port
	 * @author luochenxun
	 */
	class MySendFileRunable implements Runnable {
		
		private String host;
		private int port;
		private Uri uri;
		private String fileInfo;
		private InputStream ins;
		
		MySendFileRunable(String host, int port, Uri uri , String info ,InputStream in) {
			this.host = host;
			this.port = port;
			this.uri = uri;
			this.fileInfo = info;
			ins = in;
		}
		
		@Override
		public void run() {
			sendFile();
		}
		
		private boolean sendFile() {
			Boolean result = Boolean.TRUE;
			boolean isShowDialog = false;
			Socket socket = new Socket();
			try {
				// connect the dst server
				socket.bind(null);
				
				if(((MainApplication) getApplication()).getXiaoyi().isWifiAvailable()){
					host = ((MainApplication) getApplication()).getXiaoyi().getWifiIp();
					port = WifiP2pConfigInfo.WIFI_PORT;
				}
				
				socket.connect((new InetSocketAddress(host, port)), WifiP2pConfigInfo.SOCKET_TIMEOUT);
				Logger.d(this.getClass().getName(), "Client socket - " + socket.isConnected());
				
				// Get the file's info
				OutputStream outs = socket.getOutputStream();
				
				// output the commandId
				outs.write(WifiP2pConfigInfo.COMMAND_ID_SEND_FILE);
				
				// output the file's info
				if(fileInfo == null){
					return false;
				}
				Logger.d(this.getClass().getName(), "fileInfo:" + fileInfo);
				outs.write(fileInfo.length());
				outs.write(fileInfo.getBytes(), 0, fileInfo.length());
				
				
				// show a dialog if need a long time
				isShowDialog = (ins.available() > 500000);
				Message mmsg;
				if(isShowDialog){
					mmsg = mMainHandler.obtainMessage(MSG_SHOWDIALOG);
					mmsg.arg1 = ins.available();
					mMainHandler.sendMessage(mmsg);
				}
				
				// output the file's stream
				if(ins == null){
					return false;
				}
				byte buf[] = new byte[1024];
				int len,sum  = 0;
				while ((len = ins.read(buf)) != -1) {
					sum += len;
					// show a dialog if need a long time
					if(isShowDialog && !mMainHandler.hasMessages(MSG_SHOW_DIALOG_PROCESS)){
						mmsg = mMainHandler.obtainMessage(MSG_SHOW_DIALOG_PROCESS);
						mmsg.arg1 = sum;
						mMainHandler.sendMessage(mmsg);
					}
					outs.write(buf, 0, len);
				}
				// show a dialog if need a long time
				if(isShowDialog){
					mMainHandler.sendEmptyMessage(MSG_DISSMISS_DIALOG);
				}

				// close socket
				ins.close();
				outs.close();
				Logger.d(this.getClass().getName(), "Client: Data written");
			} catch (FileNotFoundException e) {
				Logger.d(this.getClass().getName(), "send file exception " + e.toString());
			} catch (IOException e) {
				Logger.e(this.getClass().getName(),
						"send file exception " + e.getMessage());
				result = Boolean.FALSE;
			} finally {
				if (socket != null) {
					if (socket.isConnected()) {
						try {
							socket.close();
							Logger.d(this.getClass().getName(), "socket.close()");
						} catch (IOException e) {
							// Give up
							e.printStackTrace();
						}
					}
				}
			}
			return result;
		}
	}

}
