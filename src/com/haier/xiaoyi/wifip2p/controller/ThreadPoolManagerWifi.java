package com.haier.xiaoyi.wifip2p.controller;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.Process;

import com.haier.xiaoyi.util.Logger;
import com.haier.xiaoyi.videochat.Constant;
import com.haier.xiaoyi.wifip2p.module.WifiP2pConfigInfo;

/**
 * It's a HandlerThread bind by WifiP2pService
 * 
 * @author luochenxun
 * 
 */
public class ThreadPoolManagerWifi extends HandlerThread {

	private final static String TAG = "ThreadPoolManagerWifi";

	/** The socketServer */
	private final ServerSocket mServer;

	/** Thread Pool */
	private final ExecutorService mThreadPool;

	/** service */
	private final WifiP2pService mP2pService;

	private static MulticastSocket socket;
	private static DatagramPacket packet;
	private static int default_bufferSize = 1024 * 2;
	private static byte[] bufferData;// 用来接收UDP发送的数据,考虑发送消息的类型来设置其大小

	public ThreadPoolManagerWifi(WifiP2pService service, int port, int poolSize) throws IOException {
		super(TAG, Process.THREAD_PRIORITY_FOREGROUND);

		// Init the main member: service , serverSocket , threadPool
		this.mP2pService = service;
		mServer = new ServerSocket(port);
		mThreadPool = Executors.newFixedThreadPool(poolSize);
		Logger.d(TAG, "ThreadPoolManagerWifi Constructor ...");
	}

	private boolean isServiceRun = true;

	final void setServiceRun(boolean isRun) {
		this.isServiceRun = isRun;
	}

	final boolean isServiceRun() {
		return isServiceRun;
	}

	/**
	 * The main HandlerLoop of the P2pservice
	 */
	private Handler mHandler = null;

	public Handler getHandler() {
		return mHandler;
	}

	static private class ServiceThreadHandler extends Handler {
		private ThreadPoolManagerWifi sThread;

		ServiceThreadHandler(ThreadPoolManagerWifi service) {
			super(service.getLooper());
			this.sThread = service;
		}

		// Handle msg
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case WifiP2pConfigInfo.MSG_SERVICE_POOL_START_WIFI:
				while (sThread.isServiceRun()) {
					try {
						Logger.d(TAG, "run ...");

						socket.receive(packet);// 实时接收数据
						// Log.d("UDPListener", "接收长度："+packet.getLength());
						if (packet.getLength() == 0){
							continue;// 没有消息则继续循环
						}
						
						sThread.mThreadPool.execute(new HandleAcceptSocketWifi(sThread.mP2pService, bufferData, packet));
						
						// 每次接收完UDP数据后，重置长度。否则可能会导致下次收到数据包被截断。
						packet.setLength(default_bufferSize);
					} catch (Exception ex) {
						Logger.e(TAG, "Exception ex:" + ex);
						sThread.mThreadPool.shutdown();
						break;
					}
				}
				break;
			default:
				break;
			}
			super.handleMessage(msg);
		}
	}

	/**
	 * Init the ThreadHandler
	 */
	public void init() {
		Logger.d(this.getName(), "init - isAlive " + isAlive());
		setServiceRun(true);
		if (!this.isAlive()) {
			this.start();
			mHandler = new ServiceThreadHandler(this);
		}
		
		try {
			socket = new MulticastSocket(WifiP2pConfigInfo.WIFI_PORT);
		} catch (IOException e) {
			e.printStackTrace();
		}
		bufferData = new byte[default_bufferSize];
		packet = new DatagramPacket(bufferData, bufferData.length);
		
		Message msg = new Message();
		msg.what = WifiP2pConfigInfo.MSG_SERVICE_POOL_START_WIFI;
		getHandler().sendMessage(msg);
	}

	/**
	 * UnInit
	 */
	public void uninit() {
		Logger.d(this.getName(), "uninit");
		setServiceRun(false);
	}

	/**
	 * Execute a runnable by threadPool
	 */
	public void execute(Runnable command) {
		mThreadPool.execute(command);
	}

	/**
	 * shutdown the thread and set the ThreadHandler's run-state to false
	 */
	public void destory() {
		setServiceRun(false);
		shutdownAndAwaitTermination();
		this.quit();
	}

	/**
	 * shutdown the threadPool
	 */
	private void shutdownAndAwaitTermination() {// ExecutorService pool
		mThreadPool.shutdown(); // Disable new tasks from being submitted
		try {
			// Wait a while for existing tasks to terminate
			if (!mThreadPool.awaitTermination(60, TimeUnit.SECONDS)) {
				mThreadPool.shutdownNow(); // Cancel currently executing tasks
				// Wait a while for tasks to respond to being cancelled
				if (!mThreadPool.awaitTermination(60, TimeUnit.SECONDS))
					System.err.println("Pool did not terminate");
			}
			if (!mServer.isClosed()) {
				mServer.close();
			}
		} catch (InterruptedException ie) {
			// (Re-)Cancel if current thread also interrupted
			mThreadPool.shutdownNow();
			// Preserve interrupt status
			Thread.currentThread().interrupt();
			Logger.e(TAG, "InterruptedException ie:", ie);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Logger.e(TAG, "IOException e:", e);
		}
	}
}
	class HandleAcceptSocketWifi implements Runnable {
		byte[] data;
		DatagramPacket packet;
		private final WifiP2pService mService;

		HandleAcceptSocketWifi(WifiP2pService service, byte[] data, DatagramPacket packet) {
			this.mService = service;
			this.data = data;
			this.packet = packet;
		}

		public void run() {
			try {
				String temp = new String(data, 0, packet.getLength(), Constant.ENCOD);// 得到接收的消息
				// Log.d("====", "收到消息："+msg.toString());
				String sourceIp = packet.getAddress().getHostAddress();// 对方ip
				String udbMsp = new String(data);
				Logger.d("treadpoolWifi", "receive a msg from WIFI broadcast :" + udbMsp);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
