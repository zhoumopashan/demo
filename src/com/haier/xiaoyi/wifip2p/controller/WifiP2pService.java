package com.haier.xiaoyi.wifip2p.controller;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.ChannelListener;
import android.os.Binder;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import com.haier.xiaoyi.MainApplication;
import com.haier.xiaoyi.util.DialogActivity;
import com.haier.xiaoyi.util.Logger;
import com.haier.xiaoyi.wifip2p.module.PeerInfo;
import com.haier.xiaoyi.wifip2p.module.WifiP2pConfigInfo;
import com.haier.xiaoyi.wifip2p.module.WrapRunable;

/**********************
 *   Main interface
 *********************/

/**
 * 
 * @author luochenxun
 * 
 */
public class WifiP2pService extends Service implements ChannelListener,
		WifiP2pServiceListener {
	private static final String TAG = "WifiP2pService";
	private static final int RETRY_CHANNEL_TIMES = 3;

	/** For connect failed retryOnce */
	private int mRetryChannelTime = RETRY_CHANNEL_TIMES;

	/** Binder class , use to bind the service */
	private WifiP2pServiceBinder mBinder = new WifiP2pServiceBinder();

	public class WifiP2pServiceBinder extends Binder {
		public WifiP2pService getService() {
			return WifiP2pService.this;
		}
	}

	/**
	 * A ThreadPool bind with this service, main handle the socket of the
	 * device, work as a serverSocket
	 */
	private ThreadPoolManager mThreadPoolManager = null;

	/** The wifi p2p manager */
	private WifiP2pManager mWifiP2pManager = null;

	/** @see android.net.wifi.p2p.WifiP2pManager.Channel */
	private Channel mChannel = null;
	private WifiP2pDevice mLocalDevice = null;

	/** WifiP2p BroadcastReceiver */
	private BroadcastReceiver mWifiP2pReceiver = null;
	private final IntentFilter intentFilter = new IntentFilter();

	/** The p2p device's list */
	private List<WifiP2pDevice> mP2pDeviceList = new ArrayList<WifiP2pDevice>();

	public List<WifiP2pDevice> getP2pDeviceList() {
		return mP2pDeviceList;
	}

//	/** Peerinfo's list */
//	private ArrayList<PeerInfo> mPeerInfoList = new ArrayList<PeerInfo>();

//	final public ArrayList<PeerInfo> getPeerInfoList() {
//		return mPeerInfoList;
//	}

	/**
	 * Manager wifiP2p-Group's info
	 */
	private WifiP2pInfo mWifiP2pInfo;

	public final String getHostAddress() {
		return mWifiP2pInfo.groupOwnerAddress.getHostAddress();
	}

	public final boolean isPeer() {
		return !mWifiP2pInfo.isGroupOwner;
	}

	public final boolean isGroupOwner() {
		return mWifiP2pInfo.isGroupOwner;
	}

	/** Getter & Setter & stateChecker */
	public boolean isWifiP2pAviliable() {
		return mWifiP2pManager != null;
	}

	public boolean isWifiP2pManager() {
		return mWifiP2pManager != null;
	}

	public boolean isWifiP2pChannel() {
		return mChannel != null;
	}

	/** Peers's operation Class */
	/** SendImageController */
	private static SendImageController mSendImageCtrl;

	public SendImageController getSendImageController() {
		return mSendImageCtrl;
	}

	@Override
	public IBinder onBind(Intent intent) {
		Logger.d(TAG, "P2p Service IBinder~~~");
		return mBinder;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		Logger.d(TAG, "P2p Service onUnbind~~~");
		return super.onUnbind(intent);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		if(intent == null || intent.getAction() == null){
			return START_STICKY;
		}
		String action = intent.getAction();
		if(action.equals("send_photo")){
			sendPhoto();
		}
		else if (action.equals("discover_peers")) {
			
			WifiP2pDevice device = ((MainApplication) getApplication()).getLocalDevice();
			if( device == null ){
				// show dialog
				showProgressDialog("discover_peers");
				discoverPeers();
			}else if(device.status == WifiP2pDevice.AVAILABLE){
				// show dialog
				showProgressDialog("discover_peers");
				discoverPeers();
			}else if( device.status == WifiP2pDevice.CONNECTED && 
					TextUtils.isEmpty(((MainApplication) getApplication()).getXiaoyi().getHostIp())){
				removeGroup();
				discoverPeers();
			}
			
		}
		
		return START_STICKY;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Logger.d(TAG, "P2p Service onCreate~~~");
		initEnvironment();
	}

	@SuppressLint("NewApi")
	@Override
	public void onDestroy() {
		Logger.d(TAG, "P2p Service onDestroy~~~");
		unregisterReceiver(mWifiP2pReceiver);
		cancelDisconnect();
		removeGroup();
		mThreadPoolManager.destory();
		if(mWifiP2pManager != null){
			try {
				mWifiP2pManager.stopPeerDiscovery(mChannel, null);
			} catch (Exception ex) {

			}
		}
		super.onDestroy();
	}

	@Override
	public void onChannelDisconnected() {
		// we will try once more
		if (isWifiP2pAviliable() && mRetryChannelTime-- != 0) {
//			Toast.makeText(this, "Channel lost. Trying again",
//					Toast.LENGTH_LONG).show();
			resetPeers();
			mChannel = initialize(this, getMainLooper(), this);
		} else {
//			Toast.makeText(
//					this,
//					"Severe! Channel is probably lost premanently. Try Disable/Re-Enable P2P.",
//					Toast.LENGTH_LONG).show();
		}
	}

	/***********************
	 * Public Methods
	 *********************/

	public boolean discoverPeers() {
		if (!isWifiP2pEnabled) {
//			Toast.makeText(this, R.string.wifip2p_p2p_not_open, Toast.LENGTH_SHORT).show();
			return false;
		} else {
			// do discoverPeers
			mWifiP2pManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
				@Override
				public void onSuccess() {
//					dismissProgressDialog();
//					Toast.makeText(WifiP2pService.this, R.string.wifip2p_discovery_sucess, Toast.LENGTH_SHORT).show();
				}

				@Override
				public void onFailure(int reasonCode) {
//					Toast.makeText(WifiP2pService.this, String.format(getResources().getString(R.string.wifip2p_discovery_failed), reasonCode), Toast.LENGTH_SHORT).show();
				}
			});
			
			return true;
		}
	}
	
	private void showProgressDialog(String action){
		Intent intent = new Intent(this,DialogActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setAction(action);
		startActivity(intent);
	}
	
	private void dismissProgressDialog(){
		Logger.d(TAG,"in service send dismissProgressDialog");
		Intent intent = new Intent(this,DialogActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setAction("dismiss");
		startActivity(intent);
	}

	/***********************
	 * Private Methods
	 *********************/

	/**
	 * Init the environment of this service
	 */
	private void initEnvironment() {
		// Get wifiP2p manager
		mWifiP2pManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);

		// Init channel
		mChannel = initialize(this, getMainLooper(), this);

		// init thread Pool manager
		try {
			mThreadPoolManager = new ThreadPoolManager(this,
					WifiP2pConfigInfo.LISTEN_PORT, 5);
		} catch (IOException ex) {
			Logger.e("NetworkService", "onActivityCreated() IOException ex", ex);
		}

		// Add wifi p2p state broadcastReceiver
		intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
		intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
		intentFilter
				.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
		intentFilter
				.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
		mWifiP2pReceiver = new WifiP2pBroadcastReceiver(this, this);
		registerReceiver(mWifiP2pReceiver, intentFilter);

		mApplication = (MainApplication) getApplication();
		mSendImageCtrl = new SendImageController(mApplication);
	}

	private void initServiceThread() {
		Logger.d(TAG, "initServiceThread.");
		mThreadPoolManager.init();
	}

	private void uninitServiceThread() {
		Logger.d(TAG, "uninitServiceThread.");
		mThreadPoolManager.uninit();
	}


	public void registerAcitivity(WifiP2pActivityListener activity) {
		// Update UI
		if (mLocalDevice != null) {
			updateLocalDevice(mLocalDevice);
		}
		// discoverPeers
		discoverPeers();
	}

	private MainApplication mApplication = null;

	/** Mark that is wifiP2p enable */
	private boolean isWifiP2pEnabled = false;

	final public void setIsWifiP2pEnabled(boolean isEnabled) {
		this.isWifiP2pEnabled = isEnabled;
		if (isWifiP2pEnabled) {
			initServiceThread();
		} else {
			uninitServiceThread();
		}
	}

	/** Connect a device by the given config */
	public void connect(WifiP2pConfig config) {
		mWifiP2pManager.connect(mChannel, config, new ActionListener() {

			@Override
			public void onSuccess() {
				// WiFiP2pBroadcastReceiver will notify us. Ignore for now.
			}

			@Override
			public void onFailure(int reason) {
//				Toast.makeText(WifiP2pService.this,
//						R.string.wifip2p_connecting_failed, Toast.LENGTH_SHORT)
//						.show();
			}
		});
	}

	/**  */
	public void cancelDisconnect() {
		// null check 
		if(mWifiP2pManager == null)return;
		
		// disconnect
		mWifiP2pManager.cancelConnect(mChannel, new ActionListener() {
			@Override
			public void onSuccess() {
//				Toast.makeText(WifiP2pService.this,
//						R.string.wifip2p_connecting_canceled,
//						Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onFailure(int reasonCode) {
//				Toast.makeText(
//						WifiP2pService.this,
//						String.format(
//								getResources()
//										.getString(
//												R.string.wifip2p_connecting_cancel_failed),
//								reasonCode), Toast.LENGTH_SHORT).show();
			}
		});
	}

	private WifiP2pManager.Channel initialize(Context srcContext,
			Looper srcLooper, WifiP2pManager.ChannelListener listener) {
		return mWifiP2pManager.initialize(srcContext, srcLooper, listener);
	}

	/**
	 * Request the current list of peers
	 * 
	 * @param listener
	 */
	public void requestPeers(WifiP2pManager.PeerListListener listener) {
		mWifiP2pManager.requestPeers(mChannel, listener);
	}

	public void removeGroup() {
		// null check
		if(mWifiP2pManager == null) return;
		
		// remove group
		mWifiP2pManager.removeGroup(mChannel, new ActionListener() {
			@Override
			public void onFailure(int reasonCode) {
				Logger.e(TAG, "Disconnect failed. Reason :" + reasonCode);
				// reason The reason for failure could be one of P2P_UNSUPPORTED
				// 1, ERROR 0 or BUSY 2.
			}

			@Override
			public void onSuccess() {
//				if (mActivity != null) {
//					mActivity.onDisconnect();
//				}
			}
		});
	}

	public void resetPeers() {
		mP2pDeviceList.clear();
//		if (mActivity != null) {
//			mActivity.resetPeers();
//		}
	}

	public void requestConnectionInfo(
			WifiP2pManager.ConnectionInfoListener listener) {
		mWifiP2pManager.requestConnectionInfo(mChannel, listener);
	}

	// private boolean bVerifyRecvFile = false;
	//
	// public boolean isbVerifyRecvFile() {
	// return bVerifyRecvFile;
	// }
	//
	// public void setbVerifyRecvFile(boolean bVerifyRecvFile) {
	// this.bVerifyRecvFile = bVerifyRecvFile;
	// }
	//
	public void postRecvPeerList(int count) {
		Message msg = new Message();
		msg.what = WifiP2pConfigInfo.MSG_REPORT_RECV_PEER_LIST;
		msg.arg1 = count;
//		if (mActivity != null)
//			mActivity.sendMessage(msg);
	}

	public void postSendStringResult(int sendBytes) {
		Message msg = new Message();
		msg.what = WifiP2pConfigInfo.MSG_SEND_STRING;
		msg.arg1 = sendBytes;// send;
//		if (mActivity != null)
//			mActivity.sendMessage(msg);
	}

	/** Send send-peer-infor-result to ui */
	public void postSendPeerInfoResult(int result) {
		Message msg = new Message();
		msg.what = WifiP2pConfigInfo.MSG_REPORT_SEND_PEER_INFO_RESULT;
		msg.arg1 = result;
//		if (mActivity != null)
//			mActivity.sendMessage(msg);
	}

	/** Send recv-peer-infor-result to ui */
	public void postRecvPeerInfo(PeerInfo info) {
		Message msg = new Message();
		msg.what = WifiP2pConfigInfo.MSG_RECV_PEER_INFO;
//		if (mActivity != null)
//			mActivity.sendMessage(msg);
	}

	public void postRecvFileResult(int result) {
		Message msg = new Message();
		msg.what = WifiP2pConfigInfo.MSG_REPORT_RECV_FILE_RESULT;
		msg.arg1 = result;
//		if (mActivity != null)
//			mActivity.sendMessage(msg);
	}

	public void postSendStreamResult(int result) {
		Message msg = new Message();
		msg.what = WifiP2pConfigInfo.MSG_REPORT_SEND_STREAM_RESULT;
		msg.arg1 = result;
//		if (mActivity != null)
//			mActivity.sendMessage(msg);
	}

	private SocketAddress remoteSockAddr;

	public void setRemoteSockAddress(SocketAddress sockAddr) {
		remoteSockAddr = sockAddr;
	}

	public SocketAddress getRemoteSockAddress() {
		return remoteSockAddr;
	}

	/**
	 * Handle when receive a peerInfo
	 */
	public boolean handleRecvPeerInfo(InputStream ins) {
		try {
			String strBuffer = "";
			byte[] buffer = new byte[1024];
			int len;
			while ((len = ins.read(buffer)) != -1) {
				strBuffer = strBuffer + new String(buffer, 0, len);
			}

			int offset1 = strBuffer.indexOf("peer:");
			int offset2 = strBuffer.indexOf("port:");
			Logger.d(TAG, "recvPeerSockAddr strBuffer:" + strBuffer);

			if (offset1 != -1 && offset2 != -1) {
				String host = strBuffer.substring(offset1 + 5, offset2);
				int port = Integer.parseInt(strBuffer.substring(offset2 + 5,
						strBuffer.length()));
				
//				PeerInfo info = new PeerInfo(host, port);
				mApplication.getXiaoyi().setHostIp(host);
			}
			return true;
		} catch (IOException e) {
			Logger.e(TAG, e.getMessage());
			return false;
		}
	}
	
	/**
	 * Handle when receive a peerInfo
	 */
	public boolean handleRecvDeviceInfo(InputStream ins) {
		try {
			String strBuffer = "";
			byte[] buffer = new byte[1024];
			int len;
			while ((len = ins.read(buffer)) != -1) {
				strBuffer = strBuffer + new String(buffer, 0, len);
			}

			int offset1 = strBuffer.indexOf("light:");
			int offset2 = strBuffer.indexOf("sound:");
			Logger.d(TAG, "recvPeerSockAddr strBuffer:" + strBuffer);

			if (offset1 != -1 && offset2 != -1) {
//				String host = strBuffer.substring(offset1 + 5, offset2);
				int light = Integer.parseInt( strBuffer.substring(offset1 + 6, offset2) );
				int voice = Integer.parseInt(strBuffer.substring(offset2 + 6,
						strBuffer.length()));
				
//				PeerInfo info = new PeerInfo(host, port);
				mApplication.getXiaoyi().setBright(light);
				mApplication.getXiaoyi().setVolice(voice);
			}
			return true;
		} catch (IOException e) {
			Logger.e(TAG, e.getMessage());
			return false;
		}
	}

	public void handleSendPeerInfo() {
		mThreadPoolManager.execute(WrapRunable.getSendPeerInfoRunable(
				new PeerInfo(getHostAddress(), WifiP2pConfigInfo.LISTEN_PORT),
				this));
	}
	
	/** Send file to the given device(host,port) */
	public void handleSendFile(String host, int port, Uri uri) {
		Logger.d(this.getClass().getName(), "handleSendFile");
		mThreadPoolManager.execute(WrapRunable.getSendFileRunable(host, port,
				uri, this));
	}

//	/** handleRecvFile from other peers */
//	public void handleRecvFile(InputStream ins) {
//		mSendImageCtrl.handleRecvFile(ins);
//	}
//
//	/** handleRecvFileInfo from other peers */
//	public void handleRecvFileInfo(InputStream ins) {
//		mSendImageCtrl.handleRecvFileInfo(ins);
//	}

//	/**
//	 * After the network reConnect, a group owner send broadcast <br>
//	 * to all the peer in the its mPeerInfoList
//	 */
//	public void handleBroadcastPeerList() {
//		if (isGroupOwner()) {
//			ByteArrayOutputStream outs = new ByteArrayOutputStream();
//			// ArrayList<PeerInfo> peerInfoLists = getPeerInfoList();
//			outs.write(mPeerInfoList.size());
//			for (PeerInfo peerInfo : mPeerInfoList) {
//				String tmp = peerInfo.toString();
//				// String tmp = "peer:" + peerInfo.host + "port:" +
//				// peerInfo.port;
//				outs.write(tmp.length());
//				try {
//					outs.write(tmp.getBytes());
//				} catch (IOException e) {
//					Logger.e(TAG, " e:" + e);
//					e.printStackTrace();
//				}
//			}
//
//			ByteArrayInputStream ins = new ByteArrayInputStream(
//					outs.toByteArray());
//			Logger.d(TAG, " ins's length:" + ins.available());
//
//			for (PeerInfo peerInfo : mPeerInfoList) {
//				handleSendStream(peerInfo.host, peerInfo.port, ins);
//			}
//		}
//	}

	/**
	 * Send the stream
	 */
	public void handleSendStream(String host, int port, InputStream ins) {
		// let's go and test ...
		mThreadPoolManager.execute(WrapRunable.getSendStreamRunnable(host,
				port, ins, this));
	}

	public void handleSendString(String host, int port, String data) {
		mThreadPoolManager.execute(WrapRunable.getSendStringRunable(host, port,
				data, this));
	}

	@Override
	public void updateLocalDevice(WifiP2pDevice device) {
		Logger.d(TAG,"updateLocalDevice , device.status is :" + device.status);
		mLocalDevice = device;
		((MainApplication) getApplication()).setLocalDevice(device);

		if (device.status == WifiP2pDevice.INVITED) {
//			showProgressDialog("connect");
		} else if (device.status != WifiP2pDevice.CONNECTED) {
			discoverPeers();
			showProgressDialog("discover_peers");
		} else {
			if(TextUtils.isEmpty(mApplication.getXiaoyi().getHostIp())){
				new Thread(new Runnable() {
					@Override
					public void run() {
						int retryTime = 3;
						while(	--retryTime > 0){
							try {
								Thread.sleep(3000);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							if(!TextUtils.isEmpty(mApplication.getXiaoyi().getHostIp())){
								dismissProgressDialog();
								return;
							}
						}
						removeGroup();
						discoverPeers();
					}
				}).start();
			}else{
				dismissProgressDialog();
			}
			
		}
		// if (mActivity != null) {
		// mActivity.updateLocalDevice(device);
		// }
	}
	
	/**
	 * Callback by discoveryPeers, return the peers that discovery
	 */
	@Override
	public void onPeersAvailable(WifiP2pDeviceList peers) {
		Logger.d(TAG, "onPeersAvailable");
		mP2pDeviceList.clear();
		mP2pDeviceList.addAll(peers.getDeviceList());
//		dismissProgressDialog();

//		if (mP2pDeviceList.size() == 0) {
//			Logger.e(TAG, "No devices found");
//			return;
//		}
//
//		String deviceTag;
//		boolean isContain = false;
//		// connect the devices
//		for (WifiP2pDevice deviceItem : mP2pDeviceList) {
//			
//			// check if the device has connect already
//			deviceTag = deviceItem.deviceAddress + deviceItem.deviceName;
//			for (String tagItem : mConectTagList) {
//				if (tagItem.equals(deviceTag)) {
//					isContain = true;
//				}
//			}
//			
//			// process the device
//			if (isContain == true && deviceItem.status != WifiP2pDevice.AVAILABLE) {
//				return;
//			}
//
//			// connecting the device
//			WifiP2pConfig config = new WifiP2pConfig();
//			config.deviceAddress = deviceItem.deviceAddress;
//			config.wps.setup = WpsInfo.PBC;
//			connect(config);
//		}
	}

	@Override
	public void onConnectionInfoAvailable(WifiP2pInfo info) {
		Logger.d(TAG, " onConnectionInfoAvailable ");
		mWifiP2pInfo = info;

		if (info.groupFormed && info.isGroupOwner) {
			Logger.d(TAG, "I'm the group's owner");
//			handleBroadcastPeerList();
		} else if (info.groupFormed) {
			((MainApplication) getApplication()).getXiaoyi().setHostIp(info.groupOwnerAddress.getHostAddress());
			handleSendPeerInfo();
			Logger.d(TAG, "peer - info.groupFormed.");
		}

		dismissProgressDialog();
		((MainApplication) getApplication()).getXiaoyi().setIsConnect(true);
		((MainApplication) getApplication()).getXiaoyi().setWifiP2pInfo(info);
	}
	
	private void sendPhoto(){
		mSendImageCtrl.sendFile();
		String host= mApplication.getXiaoyi().getHostIp();
		int port = WifiP2pConfigInfo.LISTEN_PORT;
		// send file 
		handleSendFile(host, port, ((MainApplication) getApplication()).getXiaoyi().getPhotoUri());
	}
}
