package com.haier.xiaoyi.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.haier.xiaoyi.MainApplication;
import com.haier.xiaoyi.R;
import com.haier.xiaoyi.util.Logger;
import com.haier.xiaoyi.wifip2p.controller.WifiP2pService;
import com.haier.xiaoyi.wifip2p.module.WiFiPeerListAdapter;

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
	private View mSearchArea;

	/** the peers after discovery */
	private ListView mDiscoveryPeersListView;
	private Button mRetryBtn;
	private WiFiPeerListAdapter mDiscoveryPeersAdapter;

	/**
	 * ui's object
	 */
	private ProgressDialog mProgressDialog = null;

	/******************************
	 * InnerClass <br>
	 ******************************/

	/**
	 * ActivityHandler
	 */
	static private class ActivityHandler extends Handler {
		private static final String TAG = "ActivityHandler";
		private MainActivity mSelf;

		ActivityHandler(MainActivity activity) {
			this.mSelf = activity;
		}

		@Override
		public void handleMessage(Message msg) {

		}
	}

	private Handler mHandler = new ActivityHandler(this);

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
		
//		startService(new Intent(this,WifiP2pService.class).setAction("discover_peers"));
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			Logger.d(TAG,"back key");
		    AlertDialog.Builder builder = new AlertDialog.Builder(this);  
		    builder.setMessage(R.string.dialog_exit_app)  
		           .setPositiveButton("Yes", new DialogInterface.OnClickListener() {  
		               public void onClick(DialogInterface dialog, int id) {  
		                    getApplication().onTerminate();
		               }  
		           })  
		           .setNegativeButton("No", new DialogInterface.OnClickListener() {  
		               public void onClick(DialogInterface dialog, int id) {  
		            	    Logger.d(TAG,"cancel");
		                    dialog.cancel();  
		               }  
		           }).create().show();  
			return false;

		default:
			break;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onResume() {
		super.onResume();
		// add necessary intent values to be matched.
//		bindService(new Intent(this, WifiP2pService.class), mServiceConn, BIND_AUTO_CREATE);
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
//		if (mServiceConn != null) {
//			unbindService(mServiceConn);
//			mServiceConn = null;
//		}
		
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		releaseEnvironment();
		super.onDestroy();
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}

	@Override
	public void onClick(View v) {
		
//		if(!connectCheck() && v.getId() != R.id.main_func_middle_btn){
//			return;
//		}
		
		switch (v.getId()) {
		case R.id.btn1:
			Logger.d(TAG, "btn1 click");
			startActivity(new Intent(MainActivity.this, SmartHomeActivity.class));
			break;
		case R.id.btn2:
			Logger.d(TAG, "btn2 click");
			startActivity(new Intent(MainActivity.this, SettingActivity.class));
			break;
		case R.id.btn3:
			Logger.d(TAG, "btn3 click");
			startActivity(new Intent(MainActivity.this, ParentActivity.class));
			break;
		case R.id.btn4:
			Logger.d(TAG, "btn4 click");
			startActivity(new Intent(MainActivity.this, CareActivity.class));
			break;
		case R.id.main_func_middle_btn:
			Logger.d(TAG, "main_func_middle_btn click");
		    AlertDialog.Builder builder = new AlertDialog.Builder(this);  
		    builder.setMessage(R.string.dialog_exit_app)  
		           .setPositiveButton("Yes", new DialogInterface.OnClickListener() {  
		               public void onClick(DialogInterface dialog, int id) {  
		                    getApplication().onTerminate();
		               }  
		           })  
		           .setNegativeButton("No", new DialogInterface.OnClickListener() {  
		               public void onClick(DialogInterface dialog, int id) {  
		            	    Logger.d(TAG,"cancel");
		                    dialog.cancel();  
		               }  
		           }).create().show();  
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
	}

	/**
	 * Release the environment that init before
	 */
	private void releaseEnvironment() {
		((MainApplication)getApplication()).removeActivity(this);
	}

	private void initWindow() {
		// Define Custom Window Title
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
	}

	private void initLayoutsAndViews() {
		mMainFuncArea = findViewById(R.id.main_func_area);
		mSearchArea = findViewById(R.id.main_search_device_area);

		mBtnHome = findViewById(R.id.btn1);
		mBtnHome.setOnClickListener(this);
		mBtnHome.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					mMainFuncArea.setBackgroundResource(R.drawable.main_outer_btn1);
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
		mBtnParent.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					mMainFuncArea.setBackgroundResource(R.drawable.main_outer_btn3);
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

		mBtnSetting = findViewById(R.id.btn2);
		mBtnSetting.setOnClickListener(this);
		mBtnSetting.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					mMainFuncArea.setBackgroundResource(R.drawable.main_outer_btn2);
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

		mBtnCare = findViewById(R.id.btn4);
		mBtnCare.setOnClickListener(this);
		mBtnCare.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					mMainFuncArea.setBackgroundResource(R.drawable.main_outer_btn4);
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

		mMiddleBtn = findViewById(R.id.main_func_middle_btn);
		mMiddleBtn.setOnClickListener(this);

		// Scan result listview
		mDiscoveryPeersAdapter = new WiFiPeerListAdapter(this, null);
		mDiscoveryPeersListView = (ListView) findViewById(R.id.wifip2p_main_scan_result_listview);
		mDiscoveryPeersListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				WifiP2pDevice device = (WifiP2pDevice) mDiscoveryPeersAdapter.getItem(position);
				((MainApplication)getApplication()).getXiaoyi().setDevice(device);
				WifiP2pConfig config = new WifiP2pConfig();
				config.deviceAddress = device.deviceAddress;
				config.wps.setup = WpsInfo.PBC;
				if (mProgressDialog != null && mProgressDialog.isShowing()) {
					mProgressDialog.dismiss();
				}
				mProgressDialog = ProgressDialog.show(MainActivity.this, 
						getString(R.string.wifip2p_connecting_cancel), getString(R.string.wifip2p_connecting), 
						true, true, new DialogInterface.OnCancelListener() {
							@Override
							public void onCancel(DialogInterface dialog) {
//								Toast.makeText(MainActivity.this, getString(R.string.wifip2p_connecting_canceled), Toast.LENGTH_SHORT).show();
//								cancelConnect();
							}
						});
//				connect(config);
			}
		});
	}

	/**
	 * Convert device's status
	 */
	private static String getDeviceStatus(int deviceStatus) {
		switch (deviceStatus) {
		case WifiP2pDevice.AVAILABLE:// 3
			return "Available";
		case WifiP2pDevice.INVITED:// 1
			return "Invited";
		case WifiP2pDevice.CONNECTED:// 0
			return "Connected";
		case WifiP2pDevice.FAILED:// 2
			return "Failed";
		case WifiP2pDevice.UNAVAILABLE:
			return "Unavailable";
		default:
			return "Unknown";
		}
	}

	/**
	 * Show a toast,call by selfHandler
	 */
	public void showToastTips(String tips) {
		Toast toast = Toast.makeText(this, tips, Toast.LENGTH_LONG);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
	}

	/** Show a toast,call by selfHandler by StringSourceId */
	public void showToastTips(int tipsID) {
		Toast toast = Toast.makeText(this, tipsID, Toast.LENGTH_LONG);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
	}
	
	private boolean connectCheck(){
		WifiP2pDevice device = ((MainApplication)getApplication()).getLocalDevice();
		if(device.status != WifiP2pDevice.CONNECTED){
			Toast.makeText(this, R.string.wifip2p_wait, Toast.LENGTH_SHORT).show();
			startService(new Intent(this,WifiP2pService.class).setAction("discover_peers"));
			return false;
		}else if(TextUtils.isEmpty(((MainApplication)getApplication()).getXiaoyi().getHostIp())){
			
			WifiP2pInfo info = ((MainApplication) getApplication()).getXiaoyi().getWifiP2pInfo();
			if (info != null && info.groupFormed) {
				((MainApplication) getApplication()).getXiaoyi().setHostIp(info.groupOwnerAddress.getHostAddress());
				startService(new Intent(this,WifiP2pService.class).setAction("send_peer_info"));
				return true;
			}
			
			startService(new Intent(this,WifiP2pService.class).setAction("discover_peers"));
			return false;
		}
		return true;
	}

}
