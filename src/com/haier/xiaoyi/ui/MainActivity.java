package com.haier.xiaoyi.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
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

//	/** WifiP2pService and the Binder */
//	private WifiP2pService mP2pService;
//
//	public WifiP2pService getP2pService() {
//		return mP2pService;
//	}
//
//	/**
//	 * ServiceConnection , Use bind interact with service
//	 */
//	private ServiceConnection mServiceConn = new ServiceConnection() {
//		public void onServiceConnected(ComponentName name, IBinder service) {
//			Logger.d(TAG, "bind service success");
//
//			WifiP2pServiceBinder binder = (WifiP2pServiceBinder) service;
//			mP2pService = binder.getService();
//
//			// register the listener to service
//			mP2pService.registerAcitivity(MainActivity.this);
//		}
//
//		public void onServiceDisconnected(ComponentName name) {
//			Logger.d("ServiceConnection", "unbind service success");
//		}
//	};

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
//			Logger.d(TAG, "handleMessage()  msg.what:" + msg.what);
//			switch (msg.what) {
//			case WifiP2pConfigInfo.MSG_RECV_PEER_INFO:
//				mSelf.showToastTips(R.string.wifip2p_rcv_peer_address);
//				// mSelf.showSendFileVeiw();
//				break;
//			case WifiP2pConfigInfo.MSG_SEND_RECV_FILE_BYTES:
//				// inc send byte & rec byte
//				mSelf.getP2pService().getSendImageController().incSendBytes(msg.arg1);
//				mSelf.getP2pService().getSendImageController().incRecvBytes(msg.arg2);
//				int progress1 = 0;
//				int progress2 = 0;
//				long sendSize = mSelf.getP2pService().getSendImageController().getSendBytes();
//				long sendFileSize = mSelf.getP2pService().getSendImageController().getSendFileSize();
//				long recvSize = mSelf.getP2pService().getSendImageController().getRecvBytes();
//				long recvFileSize = mSelf.getP2pService().getSendImageController().getRecvFileSize();
//				if (sendFileSize != 0) {
//					progress1 = (int) (sendSize * 100 / (sendFileSize));
//				}
//
//				if (recvFileSize != 0) {
//					progress2 = (int) (recvSize * 100 / (recvFileSize));
//				}
//
//				String tips = "\n send:" + progress1 + "(%) data(kb):" + sendSize / 1024 + "\n recv:" + progress2 + "(%) data(kb):" + recvSize / 1024;
//
//				// mSelf.showStatus(tips);
//				break;
//
//			case WifiP2pConfigInfo.MSG_VERIFY_RECV_FILE_DIALOG:
//				mSelf.verifyRecvFile();
//				break;
//
//			case WifiP2pConfigInfo.MSG_REPORT_RECV_FILE_RESULT:
//				if (msg.arg1 == 0) {
//					mSelf.showToastTips(R.string.wifip2p_rcv_file_success);
//				} else {
//					mSelf.showToastTips(R.string.wifip2p_rcv_file_failed);
//				}
//				break;
//
//			case WifiP2pConfigInfo.MSG_REPORT_SEND_FILE_RESULT:
//				if (msg.arg1 == 0) {
//					mSelf.showToastTips(R.string.wifip2p_send_file_success);
//				} else {
//					mSelf.showToastTips(R.string.wifip2p_send_file_failed);
//				}
//				mSelf.getP2pService().getSendImageController().onSendFileEnd();
//				break;
//			case WifiP2pConfigInfo.MSG_REPORT_SEND_PEER_INFO_RESULT:
//				if (msg.arg1 == 0) {
//					mSelf.showToastTips(R.string.wifip2p_send_peerinfo_success);
//				} else {
//					mSelf.showToastTips(R.string.wifip2p_send_peerinfo_failed);
//				}
//				break;
//			case WifiP2pConfigInfo.MSG_SEND_STRING:
//				if (msg.arg1 == -1)
//					mSelf.showToastTips("send string failed.");
//				else
//					mSelf.showToastTips("send string successed, length " + msg.arg1 + ".");
//				break;
//			case WifiP2pConfigInfo.MSG_REPORT_RECV_PEER_LIST:
//				mSelf.showToastTips("receive peer list.");
//			case WifiP2pConfigInfo.MSG_REPORT_SEND_STREAM_RESULT:
//				if (msg.arg1 == 0) {
//					Logger.d(TAG, "send stream sucess");
//					// mSelf.showToastTips();
//				} else {
//					Logger.d(TAG, "send stream failed");
//					// mSelf.showToastTips("send stream failed.");
//				}
//				break;
//			default:
//				mSelf.showToastTips("error msg id.");
//			}
//			super.handleMessage(msg);
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
		switch (v.getId()) {
		case R.id.btn1:
			Logger.d(TAG, "btn1 click");
			break;
		case R.id.btn2:
			Logger.d(TAG, "btn2 click");
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
			break;
		default:
			break;
		}
	}

//	@Override
//	public void sendMessage(Message msg) {
//		mHandler.sendMessage(msg);
//	}
//
//	/**
//	 * You can do sth when the device is <br>
//	 * scanning peers, like show a progressdialog
//	 */
//	@Override
//	public void showDiscoverPeers() {
//		if (mProgressDialog != null && mProgressDialog.isShowing()) {
//			mProgressDialog.dismiss();
//		}
//		mProgressDialog = ProgressDialog.show(this, getString(R.string.wifip2p_p2p_scanning_title), 
//				getString(R.string.wifip2p_p2p_scanning), true, true, 
//				new DialogInterface.OnCancelListener() {
//					@Override
//					public void onCancel(DialogInterface dialog) {
//							Logger.d(TAG, "onCancel discovery cancel.");
//					}
//		});
//	}

//	@Override
//	public void onActivityResult(int requestCode, int resultCode, Intent data) {
//		// User has picked an image. Transfer it to group owner i.e peer using
//		if (resultCode != RESULT_OK) {
//			return;
//		}
//
//		if (requestCode == WifiP2pConfigInfo.REQUEST_CODE_SELECT_IMAGE) {
//			if (data == null) {
//				Logger.e(this.getClass().getName(), "onActivityResult data == null, no choice.");
//				return;
//			}
//			Uri uri = data.getData();
//			mP2pService.getSendImageController().sendFile(uri, mP2pService);
//		}
//	}

//	/**
//	 * Remove all peers and clear all fields. This is called on
//	 * BroadcastReceiver receiving a state change event.
//	 */
//	@Override
//	public void resetPeers() {
//		// clear the deviceListview
//		mDiscoveryPeersListView.setEnabled(false);
//		mDiscoveryPeersListView.setVisibility(View.GONE);
//	}

//	@Override
//	public void onDisconnect() {
//	}

//	@Override
//	public void onConnectionInfoAvailable(WifiP2pInfo info) {
//		if (mProgressDialog != null && mProgressDialog.isShowing()) {
//			mProgressDialog.dismiss();
//		}
//		((MainApplication)getApplication()).getXiaoyi().setIsConnect(true);
//		((MainApplication)getApplication()).getXiaoyi().setWifiP2pInfo(info);
//		((MainApplication)getApplication()).getXiaoyi().setHostIp(info.groupOwnerAddress.getHostAddress());
//	}

//	@Override
//	public void onPeersAvailable(WifiP2pDeviceList peers) {
//		if (mProgressDialog != null && mProgressDialog.isShowing()) {
//			mProgressDialog.dismiss();
//		}
//
//		if (peers.getDeviceList().size() == 0) {
//			mRetryBtn.setVisibility(View.VISIBLE);
//			return;
//		}
//
//		if (peers.getDeviceList().size() == 1) {
//			WifiP2pDevice device = new ArrayList<WifiP2pDevice>(peers.getDeviceList()).get(0);
//			if(device == null) return ;
//			
//			WifiP2pDevice localDevice = ((MainApplication)getApplication()).getXiaoyi().getDevice();
//			boolean isConnect = ((MainApplication)getApplication()).getXiaoyi().isConnect();
//			if(isConnect == true && localDevice!= null && localDevice.deviceAddress.equals(device.deviceAddress)){
//				return;
//			}
//			
//			((MainApplication)getApplication()).getXiaoyi().setDevice(device);
//			WifiP2pConfig config = new WifiP2pConfig();
//			config.deviceAddress = device.deviceAddress;
//			config.wps.setup = WpsInfo.PBC;
//			if (mProgressDialog != null && mProgressDialog.isShowing()) {
//				mProgressDialog.dismiss();
//			}
//			
//			// show dialog
//			mProgressDialog = ProgressDialog.show(MainActivity.this, 
//					getString(R.string.wifip2p_connecting_cancel), 
//					getString(R.string.wifip2p_connecting), true, true,
//					new DialogInterface.OnCancelListener() {
//						@Override
//						public void onCancel(DialogInterface dialog) {
////							Toast.makeText(MainActivity.this, getString(R.string.wifip2p_connecting_canceled), Toast.LENGTH_SHORT).show();
////							cancelConnect();
//						}
//					});
//			
//			// connecting
//			connect(config);
//			return;
//		}
//
//		mSearchArea.setVisibility(View.VISIBLE);
//		mDiscoveryPeersListView.setEnabled(false);
//		mDiscoveryPeersListView.setVisibility(View.GONE);
//		mDiscoveryPeersAdapter.setDeviceList(peers.getDeviceList());
//		mDiscoveryPeersListView.setEnabled(true);
//		mDiscoveryPeersListView.setVisibility(View.VISIBLE);
//		mDiscoveryPeersListView.setAdapter(mDiscoveryPeersAdapter);
//	}

	/******************************
	 * public Methods <br>
	 ******************************/

	/******************************
	 * private Methods <br>
	 ******************************/

	private void initEnvironment() {
		
	}

	/**
	 * Release the environment that init before
	 */
	private void releaseEnvironment() {
		
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

//		// Scan btn
//		mRetryBtn = ((Button) findViewById(R.id.wifip2p_main_discovery_btn));
//		mRetryBtn.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				mP2pService.discoverPeers();
//			}
//		});

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

//	/**
//	 * Show a dialog to alow GroupOwner to select which peer to send file
//	 */
//	public void showSelectPeerDialog() {
//		mP2pService.getSendImageController().setSelectHost(null);
//
//		// Show select dialog
//		AlertDialog.Builder selectDialog = new AlertDialog.Builder(this);
//		selectDialog.setTitle(R.string.wifip2p_select_peer_dialog_title);
//		selectDialog.setIcon(android.R.drawable.ic_dialog_info);
//
//		// Mark all host of the peerList
//		final ArrayList<String> items = new ArrayList<String>();
//		Iterator<PeerInfo> it = mP2pService.getPeerInfoList().iterator();
//		while (it.hasNext()) {
//			items.add(it.next().host);
//		}
//		String[] strHosts = new String[items.size()];// size > 0;
//		items.toArray(strHosts);
//		selectDialog.setSingleChoiceItems(strHosts, 0, new DialogInterface.OnClickListener() {
//			public void onClick(DialogInterface dialog, int which) {
//				if (which < items.size() - 1) {
//					mP2pService.getSendImageController().setSelectHost(items.get(which));
//					Logger.d(TAG, "selectHost:" + mP2pService.getSendImageController().getSelectHost());
//					dialog.dismiss();
//
//					// Show image select dialog
//					startSelectImage();
//				}
//			}
//		});
//		selectDialog.setNegativeButton("CANCEL", null);
//		selectDialog.show();
//	}
//	
//	/** Show a image Select dialog, let user to select a image to send */
//	public void startSelectImage() {
//		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//		intent.setType("image/*");
//		startActivityForResult(intent, WifiP2pConfigInfo.REQUEST_CODE_SELECT_IMAGE);
//	}
//
//	/** When Receive a file, Show a dialog to user to sure download or not */
//	private void verifyRecvFile() {
//
//		String recvFileName = mP2pService.getSendImageController().getRecvFileName();
//		long recvFileSize = mP2pService.getSendImageController().getRecvFileSize();
//
//		AlertDialog.Builder normalDia = new AlertDialog.Builder(this);
//		normalDia.setIcon(R.drawable.ic_launcher);
//		normalDia.setTitle("Verify Receive File");
//		normalDia.setMessage("Receive a file " + recvFileName + "\nSIZE:" + (recvFileSize) / 1024 + "KB\nFROM:" + mP2pService.getRemoteSockAddress());
//
//		normalDia.setPositiveButton("确定", new DialogInterface.OnClickListener() {
//			@Override
//			public void onClick(DialogInterface dialog, int which) {
//				mP2pService.getSendImageController().setbVerifyRecvFile(true);
//				mP2pService.getSendImageController().verifyRecvFile();
//			}
//		});
//		normalDia.setNegativeButton("取消", new DialogInterface.OnClickListener() {
//			@Override
//			public void onClick(DialogInterface dialog, int which) {
//				mP2pService.getSendImageController().setbVerifyRecvFile(false);
//				mP2pService.getSendImageController().verifyRecvFile();
//			}
//		});
//		normalDia.create().show();
//	}
//
//	/** Invoke the service to connect the device by the given config */
//	private void connect(WifiP2pConfig config) {
//		mP2pService.connect(config);
//	}
//
//	/** RemoveGroup */
//	private void disconnect() {
//		// mDetailView.setVisibility(View.GONE);
//		mP2pService.removeGroup();
//		// reflash the peers' list
//		mP2pService.discoverPeers();
//	}

//	/** Invoke the service to cancel the connect */
//	public void cancelConnect() {
//		/*
//		 * A cancel abort request by user. <br> RemoveGroup if already
//		 * connected. <br> Else, request WifiP2pManager to abort the ongoing
//		 * request
//		 */
//		Logger.e(TAG, "cancelDisconnect.");
//		WifiP2pDevice remoteDevice = ((MainApplication)getApplication()).getXiaoyi().getDevice();
//		if (mP2pService.isWifiP2pAviliable()) {
//			if (remoteDevice == null || remoteDevice.status == WifiP2pDevice.CONNECTED) {
//				disconnect();
//			} else if (remoteDevice.status == WifiP2pDevice.AVAILABLE || remoteDevice.status == WifiP2pDevice.INVITED) {
//				mP2pService.cancelDisconnect();
//			}
//		}
//
//	}

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

//	@Override
//	public Activity getActivity() {
//		return this;
//	}
//
//	@Override
//	public void updateLocalDevice(WifiP2pDevice device) {
//
//	}

}
