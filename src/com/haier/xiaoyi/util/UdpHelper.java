package com.haier.xiaoyi.util;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import com.haier.xiaoyi.wifip2p.controller.WifiP2pService;

import android.net.wifi.WifiManager;

/**
 * 
 */
public class UdpHelper implements Runnable {
	public Boolean IsThreadDisable = false;// 指示监听线程是否终止
	private static WifiManager.MulticastLock lock;
	InetAddress mInetAddress;
	
	/**  service */
	private final WifiP2pService mP2pService;

	public UdpHelper(WifiManager manager , WifiP2pService service) {
		this.mP2pService = service;
		this.lock = manager.createMulticastLock("UDPwifi");
	}

	public void StartListen() {
		// UDP服务器监听的端口
		Integer port = 8904;
		// 接收的字节大小，客户端发送的数据不能超过这个大小
		byte[] message = new byte[100];
		try {
			// 建立Socket连接
			DatagramSocket datagramSocket = new DatagramSocket(port);
			datagramSocket.setBroadcast(true);
			DatagramPacket datagramPacket = new DatagramPacket(message, message.length);
			try {
				while (!IsThreadDisable) {
					// 准备接收数据
					Logger.d("UDP Demo", "准备接受");
					this.lock.acquire();

					datagramSocket.receive(datagramPacket);
					String strMsg = new String(datagramPacket.getData()).trim();
					
					mP2pService.udpHeartBeat(datagramPacket.getAddress().getHostAddress().toString());
					
					Logger.d("UDP Demo", datagramPacket.getAddress().getHostAddress().toString() + ":" + strMsg);
					this.lock.release();
				}
			} catch (IOException e) {// IOException
				Logger.e("UDP Demo", "error");e.printStackTrace();
			}
		} catch (SocketException e) {
			Logger.e("UDP Demo", "error");e.printStackTrace();
		} catch (Exception e) {
			Logger.e("UDP Demo", "error");e.printStackTrace();
		}

	}

	public static void send(String message) {
		message = (message == null ? "Hello IdeasAndroid!" : message);
		int server_port = 8904;
		Logger.d("UDP Demo", "UDP发送数据:" + message);
		DatagramSocket s = null;
		try {
			s = new DatagramSocket();
		} catch (SocketException e) {
			Logger.e("UDP Demo", "error");e.printStackTrace();
		}
		InetAddress local = null;
		try {
			local = InetAddress.getByName("255.255.255.255");
		} catch (UnknownHostException e) {
			Logger.e("UDP Demo", "error");e.printStackTrace();
		}
		int msg_length = message.length();
		byte[] messageByte = message.getBytes();
		DatagramPacket p = new DatagramPacket(messageByte, msg_length, local, server_port);
		try {

			s.send(p);
			s.close();

		} catch (IOException e) {
			Logger.e("UDP Demo", "error");e.printStackTrace();
		}
	}

	@Override
	public void run() {
		StartListen();
	}
}