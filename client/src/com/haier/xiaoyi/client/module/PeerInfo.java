package com.haier.xiaoyi.client.module;
/**
 * Inner class PeerInfo
 * @author luochenxun
 */
public class PeerInfo {
	public String host;
	public int port;

	public PeerInfo(String host, int port) {
		this.host = host;
		this.port = port;
	}

	@Override
	public String toString() {
		return "peer:" + host + "port:" + port;
	}
}