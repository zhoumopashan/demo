package com.haier.xiaoyi.ui;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.haier.xiaoyi.R;
import com.haier.xiaoyi.util.Logger;
import com.haier.xiaoyi.wifip2p.controller.WifiP2pActivityListener;
import com.haier.xiaoyi.wifip2p.controller.WifiP2pService;
import com.haier.xiaoyi.wifip2p.controller.WifiP2pService.WifiP2pServiceBinder;
import com.haier.xiaoyi.wifip2p.module.WifiP2pConfigInfo;

public class ParentActivity extends Activity implements View.OnClickListener,WifiP2pActivityListener {

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
	private View mPushImage;
	private View mBrightSet;
	private View mVolumnSet;
	private View mPersonal;

	/******************************
	 * InnerClass <br>
	 ******************************/
	
	/** WifiP2pService and the Binder */
	private WifiP2pService mP2pService;

	public WifiP2pService getP2pService() {
		return mP2pService;
	}

	/**
	 * ServiceConnection , Use bind interact with service
	 */
	private ServiceConnection mServiceConn = new ServiceConnection() {
		public void onServiceConnected(ComponentName name, IBinder service) {
			Logger.d(TAG, "bind service success");

			WifiP2pServiceBinder binder = (WifiP2pServiceBinder) service;
			mP2pService = binder.getService();

			// register the listener to service
			mP2pService.registerAcitivity(ParentActivity.this);
		}

		public void onServiceDisconnected(ComponentName name) {
			Logger.d("ServiceConnection", "unbind service success");
		}
	};

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
		
		// add necessary intent values to be matched.
		bindService(new Intent(this, WifiP2pService.class), mServiceConn, BIND_AUTO_CREATE);
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
		if (mServiceConn != null) {
			unbindService(mServiceConn);
			mServiceConn = null;
		}
		
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

		if (requestCode == WifiP2pConfigInfo.REQUEST_CODE_SELECT_IMAGE) {
			if (data == null) {
				Logger.e(this.getClass().getName(), "onActivityResult data == null, no choice.");
				return;
			}
			Uri uri = data.getData();
			mP2pService.getSendImageController().sendFile(uri , mP2pService);
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
		setContentView(R.layout.parent_layout);
	}

	private void initLayoutsAndViews() {
		mPushImage = (TextView) findViewById(R.id.push_image);
		mPushImage.setOnClickListener(this);

		mBrightSet = (TextView) findViewById(R.id.setting_bright);
		mBrightSet.setOnClickListener(this);

		mVolumnSet = (TextView) findViewById(R.id.setting_volumn);
		mVolumnSet.setOnClickListener(this);

		mPersonal = (TextView) findViewById(R.id.persional);
		mPersonal.setOnClickListener(this);
	}
	
	/** Show a image Select dialog, let user to select a image to send */
	private void startSelectImage() {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("image/*");
		startActivityForResult(intent, WifiP2pConfigInfo.REQUEST_CODE_SELECT_IMAGE);
	}

	@Override
	public void onConnectionInfoAvailable(WifiP2pInfo info) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPeersAvailable(WifiP2pDeviceList peers) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void showDiscoverPeers() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDisconnect() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void resetPeers() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateLocalDevice(WifiP2pDevice device) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sendMessage(Message msg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Activity getActivity() {
		return this;
	}

}
