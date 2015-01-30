package com.haier.xiaoyi.client.controller;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.ChannelListener;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;

import com.haier.xiaoyi.client.MainApplication;
import com.haier.xiaoyi.client.module.PeerInfo;
import com.haier.xiaoyi.client.module.WifiP2pConfigInfo;
import com.haier.xiaoyi.client.module.WrapRunable;
import com.haier.xiaoyi.client.ui.ClockActivity;
import com.haier.xiaoyi.client.util.Logger;
import com.haier.xiaoyi.client.util.UdpHelper;
import com.haier.xiaoyi.client.util.WifiUtil;

/**********************
 *   Main interface
 *********************/

/**
 * 
 * @author luochenxun
 * 
 */
public class WifiP2pService extends Service implements ChannelListener, WifiP2pServiceListener {
	private static final String TAG = "AppNetService";
	private static final int RETRY_CHANNEL_TIMES = 3;

	/** For connect failed retryOnce */
	private int mRetryChannelTime = RETRY_CHANNEL_TIMES;
	private int mDiscoveryTime = 0;

	/**
	 * A ThreadPool bind with this service, main handle the socket of the
	 * device, work as a serverSocket
	 */
	private ThreadPoolManager mThreadPoolManager = null;
	private ThreadPoolManagerWifi mThreadPoolManagerWifi = null;

	/** The wifi p2p manager */
	private WifiP2pManager mWifiP2pManager = null;

	/** @see android.net.wifi.p2p.WifiP2pManager.Channel */
	private Channel mChannel = null;
	private WifiP2pDevice mLocalDevice = null;
	private WifiUtil mWifiUtil = null;
	//
	private UdpHelper mUdpHelper = null;

	/** WifiP2p BroadcastReceiver */
	private BroadcastReceiver mWifiP2pReceiver = null;
	private AlarmManager mAlarm = null;
	private final IntentFilter intentFilter = new IntentFilter();

	/** The p2p device's list */
	private List<WifiP2pDevice> mP2pDeviceList = new ArrayList<WifiP2pDevice>();

	public List<WifiP2pDevice> getP2pDeviceList() {
		return mP2pDeviceList;
	}

	private List<String> mConectTagList = new ArrayList<String>();

	/** Peerinfo's list */
	private ArrayList<PeerInfo> mPeerInfoList = new ArrayList<PeerInfo>();

	final public ArrayList<PeerInfo> getPeerInfoList() {
		return mPeerInfoList;
	}

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
	public int onStartCommand(Intent intent, int flags, int startId) {

		// Bind the operation's controller
		if (mSendImageCtrl == null) {
			mSendImageCtrl = new SendImageController(((MainApplication) getApplication()).getUiListener());
		} else {
			mSendImageCtrl.setListener(((MainApplication) getApplication()).getUiListener());
		}

		// Update UI
		if (mLocalDevice != null) {
			updateLocalDevice(mLocalDevice);
		}

		if (intent == null || intent.getAction() == null) {
			return START_STICKY;
		}
		String action = intent.getAction();
		if (action.equals("send_photo")) {
			// sendPhoto();
		} else if (action.equals("discover_peers")) {
			discoverPeers();
		} else if(action.equals("wifi_connect")){
			updateWifiState(intent);
		} else if(action.equals("wifi_disconnect")){
			disableWifiState();
		} else{
			discoverPeers();
		}

		return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		Logger.d(TAG, "P2p Service IBinder~~~");
		return null;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		Logger.d(TAG, "P2p Service onUnbind~~~");
		return super.onUnbind(intent);
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Logger.d(TAG, "P2p Service onCreate~~~");
		initEnvironment();
	}

	@Override
	public void onDestroy() {
		Logger.d(TAG, "P2p Service onDestroy~~~");
		unregisterReceiver(mWifiP2pReceiver);
		mThreadPoolManager.destory();
		mThreadPoolManagerWifi.destory();
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
			// Toast.makeText(this,
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
		
		mWifiP2pManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
			@Override
			public void onSuccess() {
				Logger.d(TAG,"discovery success");
			}

			@Override
			public void onFailure(int reasonCode) {
				Logger.d(TAG,"discovery failed");
			}
		});
		
		// Update UI
		if (((MainApplication) getApplication()).getUiListener() != null) {
			((MainApplication) getApplication()).getUiListener().showDiscoverPeers();
			return true;
		}
		
		return false;
	}
	
	/***********************
	 * Private Methods
	 *********************/

	/**
	 * Init the environment of this service
	 */
	private void initEnvironment() {
//		// CrashHandler
//		CrashHandler crashHandler = CrashHandler.getInstance();    
//        crashHandler.init(this); 
        
		// Get wifiP2p manager
		mWifiP2pManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
		mWifiUtil = new WifiUtil(getApplicationContext());

		// Init channel
		mChannel = initialize(this, getMainLooper(), this);

		// init thread Pool manager
		try {
			mThreadPoolManager = new ThreadPoolManager(this, WifiP2pConfigInfo.LISTEN_PORT, 5);
			mThreadPoolManagerWifi = new ThreadPoolManagerWifi(this, WifiP2pConfigInfo.WIFI_PORT, 5);
		} catch (IOException ex) {
			Logger.e("NetworkService", "onActivityCreated() IOException ex", ex);
		}

		// Add wifi p2p state broadcastReceiver
		intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
		intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
		intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
		intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
		intentFilter.addAction("regular_jobs");
		mWifiP2pReceiver = new WifiP2pBroadcastReceiver(this, this);
		registerReceiver(mWifiP2pReceiver, intentFilter);

		// init Udp Helper
		WifiManager manager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
		mUdpHelper = new UdpHelper(manager);
		Thread tReceived = new Thread(mUdpHelper);
		tReceived.start();
		startRegularCheck();
	}

	private void startRegularCheck() {
		Logger.d(TAG, "startRegularCheck");
		PendingIntent sender = PendingIntent.getBroadcast(this, 0, new Intent("regular_jobs"), 0);
		mAlarm = (AlarmManager) getSystemService(ALARM_SERVICE);
		mAlarm.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 5000, sender);
	}

	public void doRegularJobs() {
		Logger.d(TAG,"do it regular!");
//		discoverPeers();
//		getDeviceInfo();
		new Thread(new Runnable() {
			@Override
			public void run() {
				if( WifiUtil.isWifiEnable( getApplicationContext()) ){
					Logger.d(TAG,"Sending msg while wifi is good");
					mUdpHelper.send("luo");	
				}
			}
		}).start();
		
		if( !((MainApplication) getApplication()).getXiaoyi().isWifiAvailable() &&
				!((MainApplication) getApplication()).getXiaoyi().isConnect() && (mDiscoveryTime++) > RETRY_CHANNEL_TIMES ){
			mDiscoveryTime = 0;
			Logger.d(TAG," discovery peers in regular");
			discoverPeers();
		}
		
//		mThreadPoolManager.execute(WrapRunable.getSendWifiInfoRunnable(((MainApplication) getApplication()).getXiaoyi()));
	}

	private void getDeviceInfo() {

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

	/**
	 * This object is use to pass the callback of peer-event back to activity
	 */
	public void registerAcitivity(WifiP2pActivityListener activity) {
		// discoverPeers
		// discoverPeers();
	}

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
		Logger.d(TAG,"connect peer");
		mWifiP2pManager.connect(mChannel, config, new ActionListener() {

			@Override
			public void onSuccess() {
				// WiFiP2pBroadcastReceiver will notify us. Ignore for now.
			}

			@Override
			public void onFailure(int reason) {
				// Toast.makeText(WifiP2pService.this,
				// R.string.wifip2p_connecting_failed,
				// Toast.LENGTH_SHORT).show();
			}
		});
	}

	/**  */
	public void cancelDisconnect() {
		mWifiP2pManager.cancelConnect(mChannel, new ActionListener() {
			@Override
			public void onSuccess() {
				// Toast.makeText(WifiP2pService.this,
				// R.string.wifip2p_connecting_canceled,
				// Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onFailure(int reasonCode) {
				// Toast.makeText(WifiP2pService.this,
				// String.format(getResources().getString(R.string.wifip2p_connecting_cancel_failed),
				// reasonCode), Toast.LENGTH_SHORT).show();
			}
		});
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
		mWifiP2pManager.removeGroup(mChannel, new ActionListener() {
			@Override
			public void onFailure(int reasonCode) {
				Logger.e(TAG, "Disconnect failed. Reason :" + reasonCode);
				// reason The reason for failure could be one of P2P_UNSUPPORTED
				// 1, ERROR 0 or BUSY 2.
			}

			@Override
			public void onSuccess() {
				((MainApplication) getApplication()).getUiListener().onDisconnect();
			}
		});
	}

	public void resetPeers() {
		mP2pDeviceList.clear();
		mConectTagList.clear();
		if (((MainApplication) getApplication()).getUiListener() != null) {
			((MainApplication) getApplication()).getUiListener().resetPeers();
		}
	}

	public void requestConnectionInfo(WifiP2pManager.ConnectionInfoListener listener) {
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
		if (((MainApplication) getApplication()).getUiListener() != null) {
			((MainApplication) getApplication()).getUiListener().sendMessage(msg);
		}
	}

	public void postSendStringResult(int sendBytes) {
		Message msg = new Message();
		msg.what = WifiP2pConfigInfo.MSG_SEND_STRING;
		msg.arg1 = sendBytes;// send;
		if (((MainApplication) getApplication()).getUiListener() != null) {
			((MainApplication) getApplication()).getUiListener().sendMessage(msg);
		}
	}

	/** Send send-peer-infor-result to ui */
	public void postSendPeerInfoResult(int result) {
		Message msg = new Message();
		msg.what = WifiP2pConfigInfo.MSG_REPORT_SEND_PEER_INFO_RESULT;
		msg.arg1 = result;
		if (((MainApplication) getApplication()).getUiListener() != null) {
			((MainApplication) getApplication()).getUiListener().sendMessage(msg);
		}
	}

	/** Send recv-peer-infor-result to ui */
	public void postRecvPeerInfo(PeerInfo info) {
		Message msg = new Message();
		msg.what = WifiP2pConfigInfo.MSG_RECV_PEER_INFO;
		if (((MainApplication) getApplication()).getUiListener() != null) {
			((MainApplication) getApplication()).getUiListener().sendMessage(msg);
		}
	}

	public void postRecvFileResult(int result) {
		Message msg = new Message();
		msg.what = WifiP2pConfigInfo.MSG_REPORT_RECV_FILE_RESULT;
		msg.arg1 = result;
		if (((MainApplication) getApplication()).getUiListener() != null) {
			((MainApplication) getApplication()).getUiListener().sendMessage(msg);
		}
	}

	public void postSendStreamResult(int result) {
		Message msg = new Message();
		msg.what = WifiP2pConfigInfo.MSG_REPORT_SEND_STREAM_RESULT;
		msg.arg1 = result;
		if (((MainApplication) getApplication()).getUiListener() != null) {
			((MainApplication) getApplication()).getUiListener().sendMessage(msg);
		}
	}

	private SocketAddress remoteSockAddr;

	public void setRemoteSockAddress(SocketAddress sockAddr) {
		remoteSockAddr = sockAddr;
	}

	public SocketAddress getRemoteSockAddress() {
		return remoteSockAddr;
	}

	// public boolean handleRecvPeerList(InputStream ins) {
	// try {
	// mPeerInfoList.clear();
	// int peerListSize = ins.read();
	// for (int i = 0; i < peerListSize; ++i) {
	// int bufferLen = ins.read();
	// byte[] buffer = new byte[256];
	// ins.read(buffer, 0, bufferLen);
	// String strBuffer = new String(buffer, 0, bufferLen);
	// int offset1 = strBuffer.indexOf("peer:");
	// int offset2 = strBuffer.indexOf("port:");
	// Logger.d(WiFiDirectActivity.TAG, "recvPeerSockAddr strBuffer:" +
	// strBuffer);
	// if (offset1 != -1 && offset2 != -1) {
	// assert (offset1 < offset2);
	// String host = strBuffer.substring(offset1 + 5, offset2);
	// int port = Integer.parseInt(strBuffer.substring(offset2 + 5,
	// strBuffer.length()));
	// mPeerInfoList.add(new PeerInfo(host, port));
	// Logger.d(WiFiDirectActivity.TAG, "peerInfoList.add(...). size:" +
	// mPeerInfoList.size());
	// }
	// }
	// postRecvPeerList(mPeerInfoList.size());
	// return true;
	// } catch (IOException e) {
	// Logger.e(WiFiDirectActivity.TAG, e.getMessage());
	// return false;
	// }
	// }

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
				Logger.d(TAG, "new host:" + host);

				PeerInfo info = new PeerInfo(host, port);
				((MainApplication) getApplication()).getXiaoyi().setHostIp(host);
				handleSendDeviceInfo(host);

				if (((MainApplication) getApplication()).getUiListener() != null) {
					((MainApplication) getApplication()).getUiListener().onHostSure(host);
				}

				// send the event to ui
				postRecvPeerInfo(info);

				for (Iterator<PeerInfo> iter = mPeerInfoList.iterator(); iter.hasNext();) {
					PeerInfo peer = iter.next();
					if (peer.host.equals(host)) {
						Logger.d(TAG, "Peer is exist already");
						return true;
					}
				}
				// add peer if not exist
				mPeerInfoList.add(info);
				Logger.d(TAG, "peerInfoList.add(...). size:" + mPeerInfoList.size());
			}
			return true;
		} catch (IOException e) {
			Logger.e(TAG, e.getMessage());
			return false;
		}
	}

	public void handleSendPeerInfo() {
		mThreadPoolManager.execute(WrapRunable.getSendPeerInfoRunable(new PeerInfo(((MainApplication) getApplication()).getXiaoyi().getHostIp(), WifiP2pConfigInfo.LISTEN_PORT), this));
	}

	public void handleSendDeviceInfo(String ip) {
		mThreadPoolManager.execute(WrapRunable.getSendDeviceInfoRunnable(ip, ((MainApplication) getApplication()).getXiaoyi()));
	}

	/** Send file to the given device(host,port) */
	public void handleSendFile(String host, int port, Uri uri) {
		Logger.d(this.getClass().getName(), "handleSendFile");
		mThreadPoolManager.execute(WrapRunable.getSendFileRunable(host, port, uri, this));
	}

	/** handleRecvFile from other peers */
	public void handleRecvFile(InputStream ins) {
		mSendImageCtrl.handleRecvFile(this, ins);
	}

	/** handleRecvFileInfo from other peers */
	public void handleRecvFileInfo(InputStream ins) {
		mSendImageCtrl.handleRecvFileInfo(ins);
	}

	/** handleRecvPeerList from other peers */
	public boolean handleRecvPeerList(InputStream ins) {
		try {
			mPeerInfoList.clear();
			int peerListSize = ins.read();
			for (int i = 0; i < peerListSize; ++i) {
				int bufferLen = ins.read();
				byte[] buffer = new byte[256];
				ins.read(buffer, 0, bufferLen);
				String strBuffer = new String(buffer, 0, bufferLen);
				int offset1 = strBuffer.indexOf("peer:");
				int offset2 = strBuffer.indexOf("port:");
				Log.d(TAG, "recvPeerSockAddr strBuffer:" + strBuffer);
				if (offset1 != -1 && offset2 != -1) {
					String host = strBuffer.substring(offset1 + 5, offset2);
					int port = Integer.parseInt(strBuffer.substring(offset2 + 5, strBuffer.length()));
					mPeerInfoList.add(new PeerInfo(host, port));
				}
			}
			postRecvPeerList(mPeerInfoList.size());
			return true;
		} catch (IOException e) {
			Logger.e(TAG, e.getMessage());
			return false;
		}
	}

	/** handleDeviceInfo from other peers */
	public boolean handleDeviceInfo(InputStream ins) {
		try {
			String strBuffer = "";
			byte[] buffer = new byte[1024];
			int len;
			while ((len = ins.read(buffer)) != -1) {
				strBuffer = strBuffer + new String(buffer, 0, len);
			}

			int offset1 = strBuffer.indexOf("light:");
			int offset2 = strBuffer.indexOf("sound:");
			int light = 0, voice = 0;
			Logger.d(TAG, "recvPeerSockAddr strBuffer:" + strBuffer);

			if (offset1 != -1 && offset2 != -1) {
				light = Integer.parseInt(strBuffer.substring(offset1 + 6, offset2));
				voice = Integer.parseInt(strBuffer.substring(offset2 + 6, strBuffer.length()));

				((MainApplication) getApplication()).saveScreenBrightness(light);
				((MainApplication) getApplication()).setDeviceVoice(voice);

			}

			WifiP2pActivityListener activity = ((MainApplication) getApplication()).getUiListener();
			if (activity != null) {
				activity.printfmsg("Bright is set to : " + light);
				activity.printfmsg("Volunm is set to : " + voice);
			}

			return true;
		} catch (IOException e) {
			Logger.e(TAG, e.getMessage());
			return false;
		}
	}

	/** handle receive clock */
	public boolean handleRecvClock(InputStream ins) {
		try {
			String strBuffer = "";

			int clockType = ins.read();
			int hour = ins.read();
			int minute = ins.read();

			byte[] buffer = new byte[1024];
			int len;
			while ((len = ins.read(buffer)) != -1) {
				strBuffer = strBuffer + new String(buffer, 0, len);
			}
			Logger.d(TAG, "recvPeerSockAddr strBuffer:" + strBuffer);

			Calendar clockTime = Calendar.getInstance();
			clockTime.setTime(new Date());

			int nowHour = clockTime.get(Calendar.HOUR_OF_DAY);
			int nowMin = clockTime.get(Calendar.MINUTE);
			if (nowHour > hour) {
				clockTime.add(Calendar.DAY_OF_MONTH, 1);
			} else if (nowHour == hour && nowMin > minute) {
				clockTime.add(Calendar.DAY_OF_MONTH, 1);
			}
			clockTime.set(Calendar.HOUR_OF_DAY, hour);
			clockTime.set(Calendar.MINUTE, minute);

			long clockTimeLong = clockTime.getTimeInMillis();

			WifiP2pActivityListener activity = ((MainApplication) getApplication()).getUiListener();
			if (activity != null) {
				activity.printfmsg(" -------------------   Set a Clock  -------------");
				activity.printfmsg("Clock type is  : " + clockType);
				activity.printfmsg("Msg is set to : " + strBuffer);
				activity.printfmsg("Clock Time now is       : " + System.currentTimeMillis());
				activity.printfmsg("Clock Time is is set to : " + clockTimeLong);
				activity.printfmsg(" -------------------------------------------------");
			}

			// 操作：发送一个广播，广播接收后Toast提示定时操作完成
			Intent intent = new Intent(this, ClockActivity.class);
			intent.putExtra("clock_id", clockType);
			intent.putExtra("clock_open", true);
			intent.putExtra("clock_msg", strBuffer);
			PendingIntent sender = PendingIntent.getActivity(this, clockType, intent, 0);

			AlarmManager alarm = (AlarmManager) getSystemService(ALARM_SERVICE);
			alarm.setRepeating(AlarmManager.RTC_WAKEUP, clockTimeLong, 1000 * 60 * 24, sender);
			// alarm.set(AlarmManager.RTC_WAKEUP, clockTimeLong, sender);

			return true;
		} catch (IOException e) {
			Logger.e(TAG, e.getMessage());
			return false;
		}
	}
	
	/** handle receive clock */
	public boolean handleRecvXiaoyiName(InputStream ins,int cmd) {
		try {
			String strBuffer = "";
			String name , age;

			byte[] buffer = new byte[1024];
			int len;
			while ((len = ins.read(buffer)) != -1) {
				strBuffer = strBuffer + new String(buffer, 0, len);
			}
			Logger.d(TAG, "xiaoyi's info is:" + strBuffer);
			if(strBuffer == null || TextUtils.isEmpty(strBuffer)){
				return false;
			}
			name = strBuffer.split(":")[0];
			age = strBuffer.split(":")[1];
			Logger.d(TAG, "name:" + name + ",age :" + age);
			

			
			if(!WifiP2pConfigInfo.isDebug){
				Logger.d(TAG,"setting xiaoyi's name :" + name + "," + age);
				// set setting
				ContentResolver cr = getContentResolver();
	    		ContentValues values = new ContentValues();
		    	values.put("COLUMN_XIAOYI_NAME", name);
		    	values.put("COLUMN_XIAOYI_AGE", age);
		    	
		    	if(!isXiaoyiEmpty()){
		    		Logger.d(TAG,"not empty");
		    		cr.update(Uri.parse("content://com.haier.xiaoyi.settings/XIAOYI_SETTINGS"), values ,null ,null);
		    	}else{
		    		Logger.d(TAG,"empty");
		    		cr.insert(Uri.parse("content://com.haier.xiaoyi.settings/XIAOYI_SETTINGS"), values);
		    	}
			}
			
			// set application
			( (MainApplication)getApplication()).getXiaoyi().setName(name);
			( (MainApplication)getApplication()).getXiaoyi().setAge(age);

			return true;
		} catch (IOException e) {
			Logger.e(TAG, e.getMessage());
			return false;
		}
	}
	
	public boolean isXiaoyiEmpty(){
		// set setting
		ContentResolver cr = getContentResolver();
		Cursor cursor = cr.query( Uri.parse("content://com.haier.xiaoyi.settings/XIAOYI_SETTINGS") , null, null, null, null );
		if(cursor == null || cursor.getCount() <= 0){  
			cursor.close();
			return true;
		}
		
		cursor.moveToFirst();
		String name = cursor.getString(cursor.getColumnIndex("COLUMN_XIAOYI_NAME"));
		String age = cursor.getString(cursor.getColumnIndex("COLUMN_XIAOYI_AGE"));
		
		Logger.d("initDevice","name: " + name + ",age: " + age);
		
		if( TextUtils.isEmpty(name) || TextUtils.isEmpty(age) ){
			cursor.close();
    		return true;
		}
		
		cursor.close();
		return false;
	}
	
	
	/** handle receive Date */
	public boolean handleRecvDate(InputStream ins) {
		try {
			int yearH = ins.read();
			int yearL = ins.read();
			int year = yearH * 100 + yearL;
			int month = ins.read();
			int date = ins.read();
			int hour = ins.read();
			int minute = ins.read();
			
			Logger.d(TAG,"year:" + year);
			Logger.d(TAG,"month:" + month);
			Logger.d(TAG,"date:" + date);
			Logger.d(TAG,"hour:" + hour);
			Logger.d(TAG,"minute" + minute);

			Calendar clockTime = Calendar.getInstance();
			clockTime.setTime(new Date());
			clockTime.set(Calendar.YEAR, year);
			clockTime.set(Calendar.MONTH, month);
			clockTime.set(Calendar.DATE, date);
			clockTime.set(Calendar.HOUR_OF_DAY, hour);
			clockTime.set(Calendar.MINUTE, minute);
			long clockTimeLong = clockTime.getTimeInMillis();
			Logger.d(TAG,"currentTime" + System.currentTimeMillis());
			Logger.d(TAG,"clockTimeLong" + clockTimeLong);
			SystemClock.setCurrentTimeMillis(clockTimeLong);

			return true;
		} catch (IOException e) {
			Logger.e(TAG, e.getMessage());
			return false;
		}
	}
	
	public void handleRecvCloseClock(InputStream ins){
		try {
			String strBuffer = "";

			int clockType = ins.read();

			WifiP2pActivityListener activity = ((MainApplication) getApplication()).getUiListener();
			if (activity != null) {
				activity.printfmsg(" -------------------   Close a Clock  -------------");
				activity.printfmsg("Clock type is  : " + clockType);
				activity.printfmsg(" -------------------------------------------------");
			}

			// 操作：发送一个广播，广播接收后Toast提示定时操作完成
			Intent intent = new Intent(this, ClockActivity.class);
			intent.putExtra("clock_id", clockType);
			intent.putExtra("clock_open", true);
			intent.putExtra("clock_msg", strBuffer);
			PendingIntent sender = PendingIntent.getActivity(this, clockType, intent, 0);

			AlarmManager alarm = (AlarmManager) getSystemService(ALARM_SERVICE);
			alarm.cancel(sender);

		} catch (IOException e) {
			Logger.e(TAG, e.getMessage());
		}
	}
	
	public void handleRecvWifi(InputStream ins){
		try {
			String strBuffer = "";
			byte[] buffer = new byte[1024];
			int len;
			while ((len = ins.read(buffer)) != -1) {
				strBuffer = strBuffer + new String(buffer, 0, len);
			}

			int offset1 = strBuffer.indexOf("xiaoyissid:");
			int offset2 = strBuffer.indexOf("xiaoyisspsw:");
			String ssid , psw ;
			Logger.d(TAG, "recvPeerSockAddr strBuffer:" + strBuffer);

			if (offset1 != -1 && offset2 != -1) {
				ssid = strBuffer.substring(offset1 + 11, offset2);
				psw = strBuffer.substring(offset2 + 12, strBuffer.length());
				
				mWifiUtil.openWifi();
				mWifiUtil.getScanResult();
				mWifiUtil.connectNetwork( ssid , psw );
				
				WifiP2pActivityListener activity = ((MainApplication) getApplication()).getUiListener();
				if (activity != null) {
					activity.printfmsg(" -------------------   Set a wifi  -------------");
					activity.printfmsg("Ssid is set to : " + ssid);
					activity.printfmsg("Psw is set to : " + psw);
					activity.printfmsg("\n");
				}
			}


		} catch (IOException e) {
			Logger.e(TAG, e.getMessage());
		}
	}
	
	public void handleSleep(InputStream ins){
		try {
			String strBuffer = "";
			byte[] buffer = new byte[1024];
			int len;
			while ((len = ins.read(buffer)) != -1) {
				strBuffer = strBuffer + new String(buffer, 0, len);
			}

			int offset1 = strBuffer.indexOf("sleep:");
			Logger.d(TAG, "recvPeerSockAddr strBuffer:" + strBuffer);

			if (offset1 != -1) {
				int sleep = Integer.parseInt(strBuffer.substring(offset1 + 6, strBuffer.length()));
				Logger.d(TAG,"Sleep : " + sleep);
				if(sleep != -1){
					sleep *= 1000;
				}
				Settings.System.putInt(getContentResolver(),android.provider.Settings.System.SCREEN_OFF_TIMEOUT, sleep);
			}

		} catch (IOException e) {
			Logger.e(TAG, e.getMessage());
		}
	}


	/**
	 * After the network reConnect, a group owner send broadcast <br>
	 * to all the peer in the its mPeerInfoList
	 */
	public void handleBroadcastPeerList() {
		if (isGroupOwner()) {
			ByteArrayOutputStream outs = new ByteArrayOutputStream();
			// ArrayList<PeerInfo> peerInfoLists = getPeerInfoList();
			outs.write(mPeerInfoList.size());
			for (PeerInfo peerInfo : mPeerInfoList) {
				String tmp = peerInfo.toString();
				// String tmp = "peer:" + peerInfo.host + "port:" +
				// peerInfo.port;
				outs.write(tmp.length());
				try {
					outs.write(tmp.getBytes());
				} catch (IOException e) {
					Logger.e(TAG, " e:" + e);
					e.printStackTrace();
				}
			}

			ByteArrayInputStream ins = new ByteArrayInputStream(outs.toByteArray());
			Logger.d(TAG, " ins's length:" + ins.available());

			for (PeerInfo peerInfo : mPeerInfoList) {
				handleSendStream(peerInfo.host, peerInfo.port, ins);
			}
		}
	}

	/**
	 * Send the stream
	 */
	public void handleSendStream(String host, int port, InputStream ins) {
		// let's go and test ...
		mThreadPoolManager.execute(WrapRunable.getSendStreamRunnable(host, port, ins, this));
	}

	public void handleSendString(String host, int port, String data) {
		mThreadPoolManager.execute(WrapRunable.getSendStringRunable(host, port, data, this));
	}

	@Override
	public void updateLocalDevice(WifiP2pDevice device) {
		mLocalDevice = device;
		if (((MainApplication) getApplication()).getUiListener() != null) {
			((MainApplication) getApplication()).getUiListener().updateLocalDevice(device);
			((MainApplication) getApplication()).getUiListener().printfmsg("device state:" + getDeviceStatus(device.status));
		}
	}

	/**
	 * Callback by discoveryPeers, return the peers that discovery
	 */
	@Override
	public void onPeersAvailable(WifiP2pDeviceList peers) {
		Logger.d(TAG,"onPeersAvailable");
		mP2pDeviceList.clear();
		mP2pDeviceList.addAll(peers.getDeviceList());

		if (mP2pDeviceList.size() == 0) {
			Logger.e(TAG, "No devices found");
			discoverPeers();
			return;
		}

		String deviceTag;
		boolean isContain = false;
		// connect the devices
		for (WifiP2pDevice deviceItem : mP2pDeviceList) {

			// check if the device has connect already
			deviceTag = deviceItem.deviceAddress + deviceItem.deviceName;
			for (String tagItem : mConectTagList) {
				if (tagItem.equals(deviceTag)) {
					isContain = true;
				}
			}

			// process the device
			if (isContain == true && deviceItem.status != WifiP2pDevice.AVAILABLE) {
				continue;
			}

			// connecting the device
			WifiP2pConfig config = new WifiP2pConfig();
			config.deviceAddress = deviceItem.deviceAddress;
			config.wps.setup = WpsInfo.PBC;

			// add it into the list
			if (!isContain) {
				mConectTagList.add(deviceTag);
			}

			Logger.d(TAG,"connect ip: " + deviceItem.deviceAddress);
			// connnetc it
			connect(config);
		}

		if (((MainApplication) getApplication()).getUiListener() != null) {
			((MainApplication) getApplication()).getUiListener().onPeersAvailable(peers);
		}
	}

	@Override
	public void onConnectionInfoAvailable(WifiP2pInfo info) {
		Logger.d(TAG, " onConnectionInfoAvailable ");
		mWifiP2pInfo = info;
		((MainApplication) getApplication()).getXiaoyi().setWifiP2pInfo(info);

		if (info.groupFormed && info.isGroupOwner) {
			Logger.d(TAG, "I'm the group's owner");
			// handleBroadcastPeerList();
		} else if (info.groupFormed) {
			((MainApplication) getApplication()).getXiaoyi().setHostIp(info.groupOwnerAddress.getHostAddress());
			if (((MainApplication) getApplication()).getUiListener() != null) {
				((MainApplication) getApplication()).getUiListener().onHostSure(info.groupOwnerAddress.getHostAddress());
			}
			handleSendPeerInfo();
			handleSendDeviceInfo(info.groupOwnerAddress.getHostAddress());
			Logger.d(TAG, "peer - info.groupFormed.");
		}

		if (((MainApplication) getApplication()).getUiListener() != null) {
			((MainApplication) getApplication()).getUiListener().onConnectionInfoAvailable(info);
		}
	}
	
    private static String getDeviceStatus(int deviceStatus) {
        switch (deviceStatus) {
            case WifiP2pDevice.AVAILABLE://3
                return "Available";
            case WifiP2pDevice.INVITED://1
                return "Invited";
            case WifiP2pDevice.CONNECTED://0
                return "Connected";
            case WifiP2pDevice.FAILED://2
                return "Failed";
            case WifiP2pDevice.UNAVAILABLE:
                return "Unavailable";
            default:
                return "Unknown";
        }
    }
    
    /*******************************
     * wifi  module
     */
    private void updateWifiState(Intent intent){
    	String ip = intent.getStringExtra("wifi_ip");
    	((MainApplication) getApplication()).getXiaoyi().setWifiIp(ip);
    	((MainApplication) getApplication()).getXiaoyi().setWifiAvailable(true);
    	startRegularCheck();
//    	mThreadPoolManager.startWifiRegularCheck();
    }
    private void disableWifiState(){
    	((MainApplication) getApplication()).getXiaoyi().setWifiIp(null);
    	((MainApplication) getApplication()).getXiaoyi().setWifiAvailable(false);
    	PendingIntent sender = PendingIntent.getBroadcast(this, 0, new Intent("regular_jobs"), 0);
    	if(mAlarm != null)  mAlarm.cancel(sender);
//    	mThreadPoolManager.stopWifiRegularCheck();
    }
}
