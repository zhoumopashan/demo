package com.haier.xiaoyi.ui;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;

import com.haier.xiaoyi.MainApplication;
import com.haier.xiaoyi.R;
import com.haier.xiaoyi.ui.ClockActivity.SendClockRunnable;
import com.haier.xiaoyi.util.Logger;
import com.haier.xiaoyi.wifip2p.module.WifiP2pConfigInfo;

public class XiaoyiActivity extends Activity {

	/******************************
	 * Macros <br>
	 ******************************/
	private static final String TAG = "XiaoyiActivity";

	/******************************
	 * public Members <br>
	 ******************************/

	/******************************
	 * private Members <br>
	 ******************************/
	/** Layouts & Views */
	private TextView mXiaoyiName;
	private TextView mXiaoyiAge;

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
		setContentView(R.layout.xiaoyisetting_layout);
	}

	private void initLayoutsAndViews() {
		mXiaoyiName = (TextView) findViewById(R.id.xiaoyi_name);
		mXiaoyiAge = (TextView) findViewById(R.id.xiaoyi_age);

		mXiaoyiName.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				LayoutInflater inflater = LayoutInflater.from(XiaoyiActivity.this);  
		        final View textEntryView = inflater.inflate(  
		                R.layout.dialoglayout, null);  
		        final EditText edtInput=(EditText)textEntryView.findViewById(R.id.edtInput);
		        edtInput.setInputType(InputType.TYPE_CLASS_TEXT);
		        final AlertDialog.Builder builder = new AlertDialog.Builder(XiaoyiActivity.this);  
		        builder.setCancelable(true);  
		        builder.setTitle(R.string.xiaoyi_name_title);  
		        builder.setView(textEntryView);  
		        builder.setPositiveButton("确认",  
		                new DialogInterface.OnClickListener() {  
		                    public void onClick(DialogInterface dialog, int whichButton) {  
		                    	mXiaoyiName.setText(getText(R.string.xiaoyi_name_prefx) + edtInput.getText().toString());
		                    	String ip = ((MainApplication) getApplication()).getXiaoyi().getHostIp();
		                    	new Thread(new SendXiaoyiNameRunnable(ip, edtInput.getText().toString(),
		                    			WifiP2pConfigInfo.COMMAND_ID_XIAOYI_NAME)).start();
		                    	
//		                    	ContentResolver cr = XiaoyiActivity.this.getContentResolver();
//		                		ContentValues values = new ContentValues();
//		                		
//		                		values.put("COLUMN_XIAOYI_NAME", edtInput.getText().toString());
//		                	    cr.insert(Uri.parse("content://com.haier.xiaoyi.settings/XIAOYI_SETTINGS"), values);
		                    }  
		                });  
		        builder.show();  
			}
		});
		mXiaoyiAge.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				LayoutInflater inflater = LayoutInflater.from(XiaoyiActivity.this);  
		        final View textEntryView = inflater.inflate(  
		                R.layout.dialoglayout, null);  
		        final EditText edtInput=(EditText)textEntryView.findViewById(R.id.edtInput);  
		        edtInput.setInputType(InputType.TYPE_CLASS_NUMBER);
		        final AlertDialog.Builder builder = new AlertDialog.Builder(XiaoyiActivity.this);  
		        builder.setCancelable(true);  
		        builder.setTitle(R.string.xiaoyi_age_title);  
		        builder.setView(textEntryView);  
		        builder.setPositiveButton("确认",  
		                new DialogInterface.OnClickListener() {  
		                    public void onClick(DialogInterface dialog, int whichButton) {  
		                    	mXiaoyiAge.setText(getText(R.string.xiaoyi_age_prefx) + edtInput.getText().toString() + 
		                    			getText(R.string.xiaoyi_age_fx)); 
		                    	String ip = ((MainApplication) getApplication()).getXiaoyi().getHostIp();
		                    	new Thread(new SendXiaoyiNameRunnable(ip, 
		                    			edtInput.getText().toString() + getText(R.string.xiaoyi_age_fx),
		                    			WifiP2pConfigInfo.COMMAND_ID_XIAOYI_AGE)).start();
//		                    	ContentResolver cr = XiaoyiActivity.this.getContentResolver();
//		                		ContentValues values = new ContentValues();
//		                		
//		                		values.put("COLUMN_XIAOYI_AGE", edtInput.getText().toString() + getText(R.string.xiaoyi_age_fx));
//		                	    cr.insert(Uri.parse("content://com.haier.xiaoyi.settings/XIAOYI_SETTINGS"), values);
		                    }  
		                });  
		        builder.show();  
			}
		});
	}
	
	class SendXiaoyiNameRunnable implements Runnable {

		private String mIp;
		private String mClockMsg;
		private int cmd;

		SendXiaoyiNameRunnable(String ip, String msg,int cmd) {
			mIp = ip;
			mClockMsg = msg;
			this.cmd = cmd;
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
				stream.write(this.cmd);
				// send data
				stream.write(mClockMsg.getBytes("UTF-8"));

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
