package com.haier.xiaoyi.client.module;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import android.net.Uri;
import android.text.TextUtils;

import com.haier.xiaoyi.client.XiaoYi;
import com.haier.xiaoyi.client.controller.WifiP2pService;
import com.haier.xiaoyi.client.util.Logger;
import com.haier.xiaoyi.client.videochat.Constant;

/**
 * Wrap all runnable
 * 
 * @author luochenxun
 */
public class WrapRunable {

	public static SendStreamRunable getSendStreamRunnable(String host, int port, InputStream ins, WifiP2pService netService) {
		return new SendStreamRunable(host, port, ins, netService);
	}

	public static SendStringRunable getSendStringRunable(String host, int port, String data, WifiP2pService netService) {
		return new SendStringRunable(host, port, data, netService);
	}

	public static SendPeerInfoRunable getSendPeerInfoRunable(PeerInfo peerInfo, WifiP2pService netService) {
		return new SendPeerInfoRunable(peerInfo, netService);
	}

	public static SendDeviceInfoRunnable getSendDeviceInfoRunnable(String ip , XiaoYi xiaoyi) {
		return new SendDeviceInfoRunnable(ip ,xiaoyi);
	}
	
	public static SendWifiInfoRunnable getSendWifiInfoRunnable(XiaoYi xiaoyi) {
		return new SendWifiInfoRunnable(xiaoyi);
	}

	public static SendFileRunable getSendFileRunable(String host, int port, Uri uri, WifiP2pService netService) {
		return new SendFileRunable(host, port, uri, netService);
	}
}

class SendDeviceInfoRunnable implements Runnable {
	private XiaoYi mDevice;
	private String mIp;
	SendDeviceInfoRunnable() {

	}

	SendDeviceInfoRunnable(String ip ,XiaoYi xiaoyi) {
		mDevice = xiaoyi;
		mIp = ip;
	}

	@Override
	public void run() {
		/* Construct socket */
		Socket socket = new Socket();
		
		boolean result = true;
		Logger.d("SendDeviceInfo", "sendPeerInfo, local ip is:" + mIp);
		try {
			socket.bind(null);
			socket.connect((new InetSocketAddress(mIp, WifiP2pConfigInfo.LISTEN_PORT)),
					WifiP2pConfigInfo.SOCKET_TIMEOUT);// host

			Logger.d("SendDeviceInfo", "Client socket - " + socket.isConnected());
			OutputStream stream = socket.getOutputStream();
			// write cmdId : COMMAND_ID_SEND_PEER_INFO
			stream.write(WifiP2pConfigInfo.COMMAND_ID_SEND_DEVICE_MSG);
			// write peerInfo: "peer:" + strIP + "port:" + port
			String strSend = "light:" + mDevice.getBright() + "sound:" + mDevice.getVolice();
			stream.write(strSend.getBytes("UTF-8"), 0, strSend.length());
			String sendMsg = "xyname:" + mDevice.getName() + "xyage:" + mDevice.getAge();
			stream.write(sendMsg.getBytes("UTF-8"));
			
			Logger.d("SendDeviceInfo", "Client: Data written strSend:" + strSend);
		} catch (IOException e) {
			Logger.e("SendDeviceInfo", e.getMessage());
			result = false;
		} 
		catch (Exception ex) {
			Logger.e("SendDeviceInfo", ex.getMessage());
			result = false;
		} finally {
			if (socket != null) {
				if (socket.isConnected()) {
					try {
						socket.close();
						Logger.d("SendDeviceInfo", "socket.close();");
					} catch (IOException e) {
						// Give up
						e.printStackTrace();
					}
				}
			}
		}
	}
}

class SendWifiInfoRunnable implements Runnable {
	private XiaoYi mDevice;
	SendWifiInfoRunnable() {

	}

	SendWifiInfoRunnable(XiaoYi xiaoyi) {
		mDevice = xiaoyi;
	}

	@Override
	public void run() {
		try {
			send(mDevice.getWifiIp(), InetAddress.getByName("255.255.255.255") );
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}
	
	public void send(String msg, InetAddress destIp) {
		// Log.d("====", "发送消息："+msg);
		send(msg, destIp, WifiP2pConfigInfo.WIFI_PORT);
	}
	
	public void send(final String msg, final InetAddress destIp, final int destPort) {
		try {
			DatagramPacket packet;
			if(TextUtils.isEmpty(msg)){
				packet = new DatagramPacket("hello".getBytes(Constant.ENCOD), "hello".length(), destIp, destPort);
			}else{
				packet = new DatagramPacket(msg.getBytes(Constant.ENCOD), msg.length(), destIp, destPort);
			}
			MulticastSocket  socket = new MulticastSocket(WifiP2pConfigInfo.WIFI_PORT);
			socket.send(packet);
//			socket.close();
			Logger.d("SendWifiInfoRunnable","send a wifi broadcast");
		} catch (IOException e) {
			e.printStackTrace();
		}
		Logger.d("SendWifiInfoRunnable","send success");
	}
}


class SendPeerInfoRunable implements Runnable {
	private PeerInfo peerInfo;
	private WifiP2pService mService;

	SendPeerInfoRunable(PeerInfo peerInfo, WifiP2pService netService) {
		this.peerInfo = peerInfo;
		this.mService = netService;
	}

	@Override
	public void run() {
		if (Utility.sendPeerInfo(peerInfo.host, peerInfo.port)) {
			mService.postSendPeerInfoResult(0);
		} else {
			mService.postSendPeerInfoResult(-1);
		}
	}
}

/**
 * A send file task, send the file(uri) to the device mark by host & port
 * 
 * @author luochenxun
 */
class SendFileRunable implements Runnable {

	private WifiP2pService mService;

	private String host;
	private int port;
	private Uri uri;

	SendFileRunable(String host, int port, Uri uri, WifiP2pService netService) {
		this.host = host;
		this.port = port;
		this.uri = uri;
		this.mService = netService;
	}

	@Override
	public void run() {
		if (sendFile()) {
			mService.getSendImageController().postSendFileResult(0);
		} else {
			mService.getSendImageController().postSendFileResult(-1);
		}
	}

	private boolean sendFile() {
		Boolean result = Boolean.TRUE;
		Socket socket = new Socket();
		try {
			// connect the dst server
			Logger.d(this.getClass().getName(), "Opening client socket - ");
			socket.bind(null);
			socket.connect((new InetSocketAddress(host, port)), WifiP2pConfigInfo.SOCKET_TIMEOUT);
			Logger.d(this.getClass().getName(), "Client socket - " + socket.isConnected());

			// Get the file's info
			OutputStream outs = socket.getOutputStream();

			// output the commandId
			outs.write(WifiP2pConfigInfo.COMMAND_ID_SEND_FILE);

			// output the file's info
			String fileInfo = mService.getSendImageController().getFileInfo(uri);
			Logger.d(this.getClass().getName(), "fileInfo:" + fileInfo);
			outs.write(fileInfo.length());
			outs.write(fileInfo.getBytes(), 0, fileInfo.length());

			// output the file's stream
			InputStream ins = mService.getSendImageController().getInputStream(uri);
			byte buf[] = new byte[1024];
			int len;
			while ((len = ins.read(buf)) != -1) {
				outs.write(buf, 0, len);
				mService.getSendImageController().postRecvBytes(len, 0);
			}

			// close socket
			ins.close();
			outs.close();
			Logger.d(this.getClass().getName(), "Client: Data written");
		} catch (FileNotFoundException e) {
			Logger.d(this.getClass().getName(), "send file exception " + e.toString());
		} catch (IOException e) {
			Logger.e(this.getClass().getName(), "send file exception " + e.getMessage());
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

/**
 * SendStreamRunable
 * <p>
 * 
 * A Task that send inputStream to the given device(by host & port) <br>
 * This method will callback the progress <br>
 * 
 * @author luochenxun
 */
class SendStreamRunable implements Runnable {

	private WifiP2pService mService;
	// device's info
	private String host;
	private int port;
	private InputStream ins;

	SendStreamRunable(String host, int port, InputStream ins, WifiP2pService netService) {
		this.host = host;
		this.port = port;
		this.ins = ins;
		this.mService = netService;
	}

	@Override
	public void run() {
		if (sendStream()) {
			mService.postSendStreamResult(0);
		} else {
			mService.postSendStreamResult(-1);
		}
	}

	private boolean sendStream() {
		Socket socket = new Socket();
		boolean result = true;

		try {
			Logger.d(this.getClass().getName(), "Opening client socket - ");
			socket.bind(null);
			socket.connect((new InetSocketAddress(host, port)), WifiP2pConfigInfo.SOCKET_TIMEOUT);

			Logger.d(this.getClass().getName(), "Client socket - " + socket.isConnected());
			OutputStream outs = socket.getOutputStream();

			byte buf[] = new byte[1024];
			int len;
			while ((len = ins.read(buf)) != -1) {
				outs.write(buf, 0, len);
			}
			ins.close();
			outs.close();
			Logger.d(this.getClass().getName(), "send stream ok.");

		} catch (IOException e) {
			Logger.e(this.getClass().getName(), e.getMessage());
			result = false;
		} finally {
			if (socket != null) {
				if (socket.isConnected()) {
					try {
						socket.close();
						Logger.d(this.getClass().getName(), "socket.close();");
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

/**
 * SendStreamRunable
 * <p>
 * 
 * A Task that send inputStream to the given device(by host & port) <br>
 * This method will not callback the progress <br>
 * 
 * @author luochenxun
 */
class SendStringRunable implements Runnable {

	// service
	private WifiP2pService mService;
	// device's info
	private String host;
	private int port;
	private String data;

	SendStringRunable(String host, int port, String data, WifiP2pService netService) {
		this.host = host;
		this.port = port;
		this.data = data;
		this.mService = netService;
	}

	@Override
	public void run() {
		if (sendString()) {
			mService.postSendStringResult(data.length());
		} else {
			mService.postSendStringResult(-1);
		}
	}

	private boolean sendString() {
		Socket socket = new Socket();
		boolean result = true;

		try {
			Logger.d(this.getClass().getName(), "Opening client socket - ");
			socket.bind(null);
			socket.connect((new InetSocketAddress(host, port)), WifiP2pConfigInfo.SOCKET_TIMEOUT);

			Logger.d(this.getClass().getName(), "Client socket - " + socket.isConnected());
			OutputStream outs = socket.getOutputStream();
			outs.write(WifiP2pConfigInfo.COMMAND_ID_SEND_STRING);
			outs.write(data.length());// NOTE: MAX = 255
			outs.write(data.getBytes());
			outs.close();
			Logger.d(this.getClass().getName(), "send string ok.");

		} catch (IOException e) {
			Logger.e(this.getClass().getName(), e.getMessage());
			result = false;
		} finally {
			if (socket != null) {
				if (socket.isConnected()) {
					try {
						socket.close();
						Logger.d(this.getClass().getName(), "socket.close();");
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
