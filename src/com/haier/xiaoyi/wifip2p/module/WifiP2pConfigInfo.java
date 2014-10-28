package com.haier.xiaoyi.wifip2p.module;

public class WifiP2pConfigInfo {
	
	public static final int MSG_NULL = 20;
	public static final int MSG_RECV_PEER_INFO = 21;
	public static final int MSG_REPORT_SEND_PEER_INFO_RESULT = 22;
	public static final int MSG_SEND_RECV_FILE_BYTES = 23;
	public static final int MSG_VERIFY_RECV_FILE_DIALOG = 24;
	public static final int MSG_REPORT_RECV_FILE_RESULT = 25;
	public static final int MSG_REPORT_SEND_FILE_RESULT = 26;
	public static final int MSG_SERVICE_POOL_START = 27;
	public static final int MSG_SEND_STRING = 28;
	public static final int MSG_REPORT_RECV_PEER_LIST = 29;
	public static final int MSG_REPORT_SEND_STREAM_RESULT = 30;
	public static final int MSG_UPDATE_LOCAL_INFO = 31;
	

	public static final int REQUEST_CODE_SELECT_IMAGE = 50;
	public static final int REQUEST_CODE_SELECT_VIDEO = 51;
	public static final int REQUEST_CODE_SELECT_AUDIO = 52;
	public static final int REQUEST_CODE_SELECT_AUDIO_ARM = 53;
	public static final int REQUEST_CODE_SELECT_TAKE_VIDEO = 54;
	public static final int REQUEST_CODE_SELECT_TAKE_IMAGE = 55;

	public static final int COMMAND_ID_SEND_PEER_INFO = 100;
	public static final int COMMAND_ID_SEND_FILE = 101;
	public static final int COMMAND_ID_REQUEST_SEND_FILE = 102;
	public static final int COMMAND_ID_RESPONSE_SEND_FILE = 103;
	public static final int COMMAND_ID_BROADCAST_PEER_LIST = 104;
	public static final int COMMAND_ID_SEND_STRING = 105;
	
	/**  */
	public static final int COMMAND_ID_SEND_DEVICE_INFO = 200;
	public static final int COMMAND_ID_SENDBACK_DEVICE_INFO = 201;
	
	/**  **/
	public static final int COMMAND_ID_CLOCK = 205;
	public static final int COMMAND_ID_CLOSE_CLOCK = 206;
	
	/**  */
	public static final int COMMAND_ID_START_CLIENT_VIDEO = 220;
	public static final int COMMAND_ID_STOP_CLIENT_VIDEO = 221;
	
	/** About server Socket */
	/** ServerSocket's Port */
	public static final int LISTEN_PORT = 8988;
	/** ServerSocket's Timeout */
    public static final int SOCKET_TIMEOUT = 5000;
	
}