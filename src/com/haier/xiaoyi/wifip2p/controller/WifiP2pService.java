package com.haier.xiaoyi.wifip2p.controller;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.net.wifi.WifiManager;
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

import com.haier.xiaoyi.MainApplication;
import com.haier.xiaoyi.util.Logger;
import com.haier.xiaoyi.util.UdpHelper;
import com.haier.xiaoyi.util.WifiUtil;
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
public class WifiP2pService extends Service implements ChannelListener, WifiP2pServiceListener {
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
	private ThreadPoolManagerWifi mThreadPoolManagerWifi = null;

	/** The wifi p2p manager */
	private WifiP2pManager mWifiP2pManager = null;
	private WifiUtil mWifiUtil = null;
	private UdpHelper mUdpHelper = null;

	/** @see android.net.wifi.p2p.WifiP2pManager.Channel */
	private Channel mChannel = null;
	private WifiP2pDevice mLocalDevice = null;

	/** WifiP2p BroadcastReceiver */
	private BroadcastReceiver mWifiP2pReceiver = null;
	private IntentFilter mIntentFilter = null;
	private AlarmManager mAlarm = null;
	private static volatile int pHeartBeatTimes = 0;
	private static volatile int pDiscoveryTimes = 0;

	/** The p2p device's list */
	private List<WifiP2pDevice> mP2pDeviceList = new ArrayList<WifiP2pDevice>();

	public List<WifiP2pDevice> getP2pDeviceList() {
		return mP2pDeviceList;
	}

	// /** Peerinfo's list */
	// private ArrayList<PeerInfo> mPeerInfoList = new ArrayList<PeerInfo>();

	// final public ArrayList<PeerInfo> getPeerInfoList() {
	// return mPeerInfoList;
	// }

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

		if (intent == null || intent.getAction() == null) {
			return START_STICKY;
		}
		String action = intent.getAction();
		if (action.equals("send_photo")) {
			sendPhoto();
		} else if (action.equals("discover_peers")) {
			// WifiP2pDevice device = ((MainApplication)
			// getApplication()).getLocalDevice();
			showProgressDialog("discover_peers");
			discoverPeers();
		} else if (action.equals("send_peer_info")) {
			handleSendPeerInfo();
		} else if( action.equals("regular_job")){
			doRegularJobs();
		}

		intent = null;

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
		unregisterWifiP2pReceiver();
		cancelDisconnect();
		removeGroup();
		mUdpHelper.IsThreadDisable = true;
		mThreadPoolManager.destory();
		mThreadPoolManagerWifi.destory();
		if (mWifiP2pManager != null) {
			try {
				PendingIntent sender = PendingIntent.getBroadcast(this, 0, new Intent("regular_jobs"), 0);
				mAlarm.cancel(sender);
				mWifiP2pManager.stopPeerDiscovery(mChannel, null);
				Logger.d(TAG, "P2p Service Destroy done~~~");
			} catch (Exception ex) {

			}
		}
		super.onDestroy();
	}

	@Override
	public void onChannelDisconnected() {
		// we will try once more
		if (isWifiP2pAviliable() && mRetryChannelTime-- != 0) {
			// Toast.makeText(this, "Channel lost. Trying again",
			// Toast.LENGTH_LONG).show();
			resetPeers();
			mChannel = initialize(this, getMainLooper(), this);
		} else {
			// Toast.makeText(
			// this,
			// "Severe! Channel is probably lost premanently. Try Disable/Re-Enable P2P.",
			// Toast.LENGTH_LONG).show();
		}
	}

	/***********************
	 * Public Methods
	 *********************/

	public boolean discoverPeers() {
		if (!isWifiP2pEnabled) {
			mWifiUtil.openWifi();
		}
		// do discoverPeers
		mWifiP2pManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
			@Override
			public void onSuccess() {
				Logger.d(TAG,"discoverPeers success");
			}

			@Override
			public void onFailure(int reasonCode) {
				Logger.d(TAG,"discoverPeers failed");
			}
		});

		return true;
	}

	private void showProgressDialog(String action) {
		IShowDialog dialog = ((MainApplication) getApplication()).getDialogHolder();
		if (dialog != null)
			dialog.showProgressDialog(action);
	}

	private void dismissProgressDialog() {
		Logger.d(TAG, "in service send dismissProgressDialog");
		IShowDialog dialog = ((MainApplication) getApplication()).getDialogHolder();
		if (dialog != null)
			dialog.dismissProgressDialog();
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
		// Init wifi util
		mWifiUtil = new WifiUtil(getApplicationContext());

		// Init udp helper
		WifiManager manager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
		mUdpHelper = new UdpHelper(manager,this);
		Thread tReceived = new Thread(mUdpHelper);
		tReceived.start();

		// Init channel
		mChannel = initialize(this, getMainLooper(), this);

		// init thread Pool manager
		try {
			mThreadPoolManager = new ThreadPoolManager(this, WifiP2pConfigInfo.LISTEN_PORT, 5);
			mThreadPoolManagerWifi = new ThreadPoolManagerWifi(this, WifiP2pConfigInfo.WIFI_PORT, 5);
		} catch (IOException ex) {
			Logger.e("NetworkService", "onActivityCreated() IOException ex", ex);
		}

		registerWifiP2pReceiver();

		mApplication = (MainApplication) getApplication();
		mSendImageCtrl = new SendImageController(mApplication);
		
		startRegularCheck();
	}

	private void initServiceThread() {
		Logger.d(TAG, "initServiceThread.");
		mThreadPoolManager.init();
		mThreadPoolManagerWifi.init();
	}

	private void uninitServiceThread() {
		Logger.d(TAG, "uninitServiceThread.");
		mThreadPoolManager.uninit();
		mThreadPoolManagerWifi.uninit();
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
			}
		});
	}

	/**  */
	public void cancelDisconnect() {
		// null check
		if (mWifiP2pManager == null)
			return;

		try {
			// disconnect
			mWifiP2pManager.cancelConnect(mChannel, new ActionListener() {
				@Override
				public void onSuccess() {
				}

				@Override
				public void onFailure(int reasonCode) {
				}
			});
		} catch (Exception ex) {
			Logger.e(TAG, "cancelDisconnect error");
		}
	}

	private WifiP2pManager.Channel initialize(Context srcContext, Looper srcLooper, WifiP2pManager.ChannelListener listener) {
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
		if (mWifiP2pManager == null)
			return;

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
			}
		});
	}

	public void resetPeers() {
		mP2pDeviceList.clear();
	}

	public void requestConnectionInfo(WifiP2pManager.ConnectionInfoListener listener) {
		mWifiP2pManager.requestConnectionInfo(mChannel, listener);
	}

	public void postRecvPeerList(int count) {
		Message msg = new Message();
		msg.what = WifiP2pConfigInfo.MSG_REPORT_RECV_PEER_LIST;
		msg.arg1 = count;
		// if (mActivity != null)
		// mActivity.sendMessage(msg);
	}

	public void postSendStringResult(int sendBytes) {
		Message msg = new Message();
		msg.what = WifiP2pConfigInfo.MSG_SEND_STRING;
		msg.arg1 = sendBytes;// send;
		// if (mActivity != null)
		// mActivity.sendMessage(msg);
	}

	/** Send send-peer-infor-result to ui */
	public void postSendPeerInfoResult(int result) {
		Message msg = new Message();
		msg.what = WifiP2pConfigInfo.MSG_REPORT_SEND_PEER_INFO_RESULT;
		msg.arg1 = result;
		// if (mActivity != null)
		// mActivity.sendMessage(msg);
	}

	/** Send recv-peer-infor-result to ui */
	public void postRecvPeerInfo(PeerInfo info) {
		Message msg = new Message();
		msg.what = WifiP2pConfigInfo.MSG_RECV_PEER_INFO;
		// if (mActivity != null)
		// mActivity.sendMessage(msg);
	}

	public void postRecvFileResult(int result) {
		Message msg = new Message();
		msg.what = WifiP2pConfigInfo.MSG_REPORT_RECV_FILE_RESULT;
		msg.arg1 = result;
		// if (mActivity != null)
		// mActivity.sendMessage(msg);
	}

	public void postSendStreamResult(int result) {
		Message msg = new Message();
		msg.what = WifiP2pConfigInfo.MSG_REPORT_SEND_STREAM_RESULT;
		msg.arg1 = result;
		// if (mActivity != null)
		// mActivity.sendMessage(msg);
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
				int port = Integer.parseInt(strBuffer.substring(offset2 + 5, strBuffer.length()));

				// PeerInfo info = new PeerInfo(host, port);
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
			int offset3 = strBuffer.indexOf("xyname:");
			int offset4 = strBuffer.indexOf("xyage:");
			Logger.d(TAG, "recvPeerSockAddr strBuffer:" + strBuffer);

			if (offset1 != -1 && offset2 != -1) {
				// String host = strBuffer.substring(offset1 + 5, offset2);
				int light = Integer.parseInt(strBuffer.substring(offset1 + 6, offset2));
				int voice = Integer.parseInt(strBuffer.substring(offset2 + 6, offset3));
				String name = strBuffer.substring(offset3 + 7, offset4);
				String age = strBuffer.substring(offset4 + 6);

				// PeerInfo info = new PeerInfo(host, port);
				mApplication.getXiaoyi().setBright(light);
				mApplication.getXiaoyi().setVolice(voice);
				mApplication.getXiaoyi().setName(name);
				mApplication.getXiaoyi().setAge(age);
			}
			return true;
		} catch (IOException e) {
			Logger.e(TAG, e.getMessage());
			return false;
		}
	}

	public void handleSendPeerInfo() {
		mThreadPoolManager.execute(WrapRunable.getSendPeerInfoRunable(new PeerInfo(getHostAddress(), WifiP2pConfigInfo.LISTEN_PORT), this));
	}

	/** Send file to the given device(host,port) */
	public void handleSendFile(String host, int port, Uri uri) {
		Logger.d(this.getClass().getName(), "handleSendFile");
		
		if(((MainApplication) getApplication()).getXiaoyi().isWifiAvailable()){
			mThreadPoolManagerWifi.execute(
					WrapRunable.getSendFileRunable(
							((MainApplication) getApplication()).getXiaoyi().getWifiIp(), 
									WifiP2pConfigInfo.WIFI_PORT, uri, this ));
		}else{
			mThreadPoolManager.execute(WrapRunable.getSendFileRunable(host, port, uri, this));
		}
	}

	/**
	 * Send the stream
	 */
	public void handleSendStream(String host, int port, InputStream ins) {
		if(((MainApplication) getApplication()).getXiaoyi().isWifiAvailable()){
			mThreadPoolManagerWifi.execute(
					WrapRunable.getSendStreamRunnable(
							((MainApplication) getApplication()).getXiaoyi().getWifiIp(), 
									WifiP2pConfigInfo.WIFI_PORT, ins, this ));
		}else{
			mThreadPoolManager.execute(WrapRunable.getSendStreamRunnable(host, port, ins, this));
		}
	}

	public void handleSendString(String host, int port, String data) {
		if(((MainApplication) getApplication()).getXiaoyi().isWifiAvailable()){
			mThreadPoolManagerWifi.execute(
					WrapRunable.getSendStringRunable(
							((MainApplication) getApplication()).getXiaoyi().getWifiIp(), 
									WifiP2pConfigInfo.WIFI_PORT, data, this ));
		}else{
			mThreadPoolManager.execute(WrapRunable.getSendStringRunable(host, port, data, this));
		}
	}

	@Override
	public void updateLocalDevice(WifiP2pDevice device) {
		Logger.d(TAG, "updateLocalDevice , device.status is :" + device.status);
		mLocalDevice = device;
		((MainApplication) getApplication()).setLocalDevice(device);
	}

	/**
	 * Callback by discoveryPeers, return the peers that discovery
	 */
	@Override
	public void onPeersAvailable(WifiP2pDeviceList peers) {
		Logger.d(TAG, "onPeersAvailable :" + peers.getDeviceList().size() );
		mP2pDeviceList.clear();
		mP2pDeviceList.addAll(peers.getDeviceList());
	}

	@Override
	public void onConnectionInfoAvailable(WifiP2pInfo info) {
		Logger.d(TAG, " onConnectionInfoAvailable ");
		mWifiP2pInfo = info;

		if (info.groupFormed && info.isGroupOwner) {
			Logger.d(TAG, "I'm the group's owner");
			// handleBroadcastPeerList();
		} else if (info.groupFormed) {
			((MainApplication) getApplication()).getXiaoyi().setHostIp(info.groupOwnerAddress.getHostAddress());
			handleSendPeerInfo();
			Logger.d(TAG, "peer - info.groupFormed.");
		}

		dismissProgressDialog();
		((MainApplication) getApplication()).getXiaoyi().setIsConnect(true);
		((MainApplication) getApplication()).getXiaoyi().setWifiP2pInfo(info);
	}
	
	public void doRegularJobs() {
		Logger.d(TAG,"do it regular!");
		if(pHeartBeatTimes++ > (RETRY_CHANNEL_TIMES ) && 
				((MainApplication) getApplication()).getXiaoyi().isWifiAvailable()){
			Logger.d(TAG," Wifi Connect error!");
			((MainApplication) getApplication()).getXiaoyi().setWifiAvailable(false);
			((MainApplication) getApplication()).getXiaoyi().setWifiIp(null);
			showProgressDialog("discover_peers");
			if(!((MainApplication) getApplication()).getXiaoyi().isConnect()){
				discoverPeers();
			}
		}
		
		if( !((MainApplication) getApplication()).getXiaoyi().isWifiAvailable() &&
				!((MainApplication) getApplication()).getXiaoyi().isConnect() && (pDiscoveryTimes++) > (RETRY_CHANNEL_TIMES )){
			pDiscoveryTimes = 0;
			Logger.d(TAG," discovery peers in regular");
			discoverPeers();
		}
	}
	
	public void udpHeartBeat(String wifiIp){
		pHeartBeatTimes = 0;
		((MainApplication) getApplication()).getXiaoyi().setWifiAvailable(true);
		((MainApplication) getApplication()).getXiaoyi().setWifiIp(wifiIp);
		
		dismissProgressDialog();
	}
	
	private void registerWifiP2pReceiver() {
		try {
			mIntentFilter = new IntentFilter();
			mWifiP2pReceiver = new WifiP2pBroadcastReceiver(this, this);
			// Add wifi p2p state broadcastReceiver
			mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
			mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
			mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
			mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
			registerReceiver(mWifiP2pReceiver, mIntentFilter);
		} catch (Exception ex) {

		}
	}
	
	private void unregisterWifiP2pReceiver() {
		try {
			if (mWifiP2pReceiver != null) {
				unregisterReceiver(mWifiP2pReceiver);
			}
			mWifiP2pReceiver = null;
			mIntentFilter = null;
		} catch (Exception ex) {

		}
	}

	private void sendPhoto() {
		mSendImageCtrl.sendFile();
		String host = mApplication.getXiaoyi().getHostIp();
		int port = WifiP2pConfigInfo.LISTEN_PORT;
		// send file
		handleSendFile(host, port, ((MainApplication) getApplication()).getXiaoyi().getPhotoUri());
	}

	private void startRegularCheck() {
		Logger.d(TAG, "startRegularCheck");
		PendingIntent sender = PendingIntent.getBroadcast(this, 0, new Intent("regular_jobs"), 0);
		mAlarm = (AlarmManager) getSystemService(ALARM_SERVICE);
		mAlarm.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 5000, sender);
	}

}
