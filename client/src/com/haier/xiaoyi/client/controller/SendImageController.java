package com.haier.xiaoyi.client.controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.content.ContentResolver;
import android.net.Uri;
import android.os.Environment;
import android.os.Message;
import android.util.Pair;

import com.haier.xiaoyi.client.module.Utility;
import com.haier.xiaoyi.client.module.WifiP2pConfigInfo;
import com.haier.xiaoyi.client.util.Logger;

public class SendImageController {

	private static final String TAG = "SendImageController";

	private WifiP2pActivityListener mActivity;

	/** About SendFile and ReceiveFile */
	private long mRecvFileSize = 0;
	private long mSendFileSize = 0;
	private String mRecvFileName = "";
	private String mSendFileName = "";
	private long mRecvBytes = 0;
	private long mSendBytes = 0;
	private String mSelectHost = "";

	private static boolean mIsSendingFile = false;

	/** Verify file's operation */
	private CountDownLatch startRecvFileSignal = null;

	public void verifyRecvFile() {
		assert (startRecvFileSignal != null);
		startRecvFileSignal.countDown();
	}

	private boolean waitForVerifyRecvFile() {
		try {
			startRecvFileSignal = new CountDownLatch(1);//
			boolean res = startRecvFileSignal.await(10, TimeUnit.SECONDS);
			return res;
		} catch (InterruptedException e) {
			Logger.e(this.getClass().getName(), "waitForVerifyRecvFile e:", e);
			e.printStackTrace();
			return false;
		}
	}

	SendImageController() {

	}

	SendImageController(WifiP2pActivityListener listener) {
		mActivity = listener;
	}

	public void setListener(WifiP2pActivityListener listener) {
		mActivity = listener;
	}

	/** Getter & Setter */
	public void resetRecvBytes() {
		mRecvBytes = 0;
	}

	public void resetSendBytes() {
		mSendBytes = 0;
	}

	public void resetRecvFileInfo() {
		resetRecvBytes();
		mRecvFileName = "";
		mRecvFileSize = 0;
	}

	public void resetSendFileInfo() {
		resetSendBytes();
		mSendFileName = "";
		mSendFileSize = 0;
	}

	public String recvFileName() {
		return mRecvFileName;
	}

	public String sendFileName() {
		return mSendFileName;
	}

	public long getRecvFileSize() {
		return mRecvFileSize;
	}

	public void setRecvFileSize(long mRecvFileSize) {
		this.mRecvFileSize = mRecvFileSize;
	}

	public long getSendFileSize() {
		return mSendFileSize;
	}

	public void setSendFileSize(long mSendFileSize) {
		this.mSendFileSize = mSendFileSize;
	}

	public String getRecvFileName() {
		return mRecvFileName;
	}

	public void setRecvFileName(String mRecvFileName) {
		this.mRecvFileName = mRecvFileName;
	}

	public String getSendFileName() {
		return mSendFileName;
	}

	public void setSendFileName(String mSendFileName) {
		this.mSendFileName = mSendFileName;
	}

	public long getRecvBytes() {
		return mRecvBytes;
	}

	public void setRecvBytes(long mRecvBytes) {
		this.mRecvBytes = mRecvBytes;
	}

	public void incRecvBytes(long mRecvBytes) {
		this.mRecvBytes += mRecvBytes;
	}

	public long getSendBytes() {
		return mSendBytes;
	}

	public void setSendBytes(long mSendBytes) {
		this.mSendBytes = mSendBytes;
	}

	public void incSendBytes(long mSendBytes) {
		this.mSendBytes += mSendBytes;
	}

	public String getSelectHost() {
		return mSelectHost;
	}

	public void setSelectHost(String mSelectHost) {
		this.mSelectHost = mSelectHost;
	}

	/*******************************************
	 * Func's method
	 *********************************/

	/**
	 * 
	 * @param uri
	 * @param p2pService
	 */
	public void sendFile(Uri uri, WifiP2pService p2pService) {
		if (!mIsSendingFile) {
			mIsSendingFile = true;
			resetSendFileInfo();
			String host = "";
			if (p2pService.isPeer()) {
				host = p2pService.getHostAddress();
			} else {
				host = mSelectHost;
			}
			int port = WifiP2pConfigInfo.LISTEN_PORT;
			// send file
			p2pService.handleSendFile(host, port, uri);
			Logger.d(TAG, "send host:" + host + "port:" + port + "uri:" + uri);
		}
	}

	/** Get the file's info */
	public String getFileInfo(Uri uri) throws IOException {
		// get the name & fileSize of the uri-file
		Pair<String, Integer> pair = Utility.getFileNameAndSize((Activity) mActivity, uri);
		String name = pair.first;
		long size = pair.second;

		// set the file's name & size
		setSendFileName(name);
		setSendFileSize(size);

		return "size:" + size + "name:" + name;
	}

	/**
	 * Get the file's inputStream by uri
	 */
	public InputStream getInputStream(Uri uri) throws FileNotFoundException {
		ContentResolver cr = ((Activity) mActivity).getContentResolver();
		return cr.openInputStream(uri);
	}

	/** Send file finished */
	public void onSendFileEnd() {
		mIsSendingFile = false;
	}

	/**
	 * Call when receive file's inputStream from socket
	 */
	public void handleRecvFile(WifiP2pService service ,InputStream ins) {
		// Mark the file's info
		handleRecvFileInfo(ins);

		// Wait for ui's comfirm
		String extName = ".jpg"; // default .
		if (!mRecvFileName.isEmpty()) {
			int dotIndex = mRecvFileName.lastIndexOf(".");
			if (dotIndex != -1 && dotIndex != mRecvFileName.length() - 1) {
				extName = mRecvFileName.substring(dotIndex);
			}
		}
		Logger.d(TAG, "传输文件名：" + mRecvFileName + " ， 后缀 extName:" + extName);

		recvFileAndSave(service ,ins, extName);

		// // Wait for ui's comfirm
		// if (waitForVerifyRecvFile() && isbVerifyRecvFile()) {
		// Logger.d(TAG, "接收成功");
		// recvFileAndSave(ins, extName);
		// } else{
		// Logger.d(TAG, "接收失败");
		// postRecvFileResult(-1);
		// }
	}

	public void postRecvFileResult(int result) {
		Message msg = new Message();
		msg.what = WifiP2pConfigInfo.MSG_REPORT_RECV_FILE_RESULT;
		msg.arg1 = result;
		if (mActivity != null)
			mActivity.sendMessage(msg);
	}

	public void postSendRecvBytes(int sendBytes, int recvBytes) {
		Message msg = new Message();
		msg.what = WifiP2pConfigInfo.MSG_SEND_RECV_FILE_BYTES;
		msg.arg1 = sendBytes;// send;
		msg.arg2 = recvBytes;// recv;
		if (mActivity != null)
			mActivity.sendMessage(msg);
	}

	private boolean bVerifyRecvFile = false;

	public boolean isbVerifyRecvFile() {
		return bVerifyRecvFile;
	}

	public void setbVerifyRecvFile(boolean bVerifyRecvFile) {
		this.bVerifyRecvFile = bVerifyRecvFile;
	}

	/**
	 * Handle receive file's inputStream from socket
	 */
	public boolean handleRecvFileInfo(InputStream ins) {
		// reset the member of the controller
		resetRecvFileInfo();

		// receive the file's info
		try {
			int iSize = ins.read();
			byte[] buffer = new byte[iSize];
			int len = ins.read(buffer, 0, iSize);
			String strBuffer = new String(buffer, 0, len);
			int offset1 = strBuffer.indexOf("size:");
			int offset2 = strBuffer.indexOf("name:");
			Logger.d(TAG, "recvDistFileInfo strBuffer:" + strBuffer);
			if (offset1 != -1 && offset2 != -1) {
				String strSize = strBuffer.substring(offset1 + 5, offset2);
				mRecvFileSize = Long.parseLong(strSize);
				mRecvFileName = strBuffer.substring(offset2 + 5, strBuffer.length());

				Logger.d(TAG, "iFileSize:" + mRecvFileSize + " \nstrFileName:" + mRecvFileName);

				// show the verify dialog
				// postVerifyRecvFile();
				return true;
			}
			return false;
		} catch (IOException e) {
			Logger.e(TAG, e.getMessage());
			return false;
		}
	}

	public boolean recvFileAndSave(WifiP2pService service , InputStream ins, String extName) {
		try {
			final File recvFile = new File(Environment.getExternalStorageDirectory() + "/file-" + System.currentTimeMillis() + extName);

			File dirs = new File(recvFile.getParent());
			if (!dirs.exists())
				dirs.mkdirs();
			recvFile.createNewFile();

			FileOutputStream fileOutS = new FileOutputStream(recvFile);

			byte buf[] = new byte[1024];
			int len;
			while ((len = ins.read(buf)) != -1) {
				fileOutS.write(buf, 0, len);
				// Call back the ui for progress
				postSendRecvBytes(0, len);

			}
			fileOutS.close();
			String strFile = recvFile.getAbsolutePath();
			if (strFile != null) {
				// Go, let's go and test a new cool & powerful method.
				Utility.openFile(service, recvFile);
			}
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	/*************************
	 * UI's callback
	 *********************/

	/**
	 * post send-file-result to ui
	 */
	public void postSendFileResult(int result) {
		Message msg = new Message();
		msg.what = WifiP2pConfigInfo.MSG_REPORT_SEND_FILE_RESULT;
		msg.arg1 = result;
		if (mActivity != null)
			mActivity.sendMessage(msg);
	}

	/**
	 * Pose the received-byte to ui
	 * */
	public void postRecvBytes(int sendBytes, int recvBytes) {
		Message msg = new Message();
		msg.what = WifiP2pConfigInfo.MSG_SEND_RECV_FILE_BYTES;
		msg.arg1 = sendBytes;// send;
		msg.arg2 = recvBytes;// recv;
		if (mActivity != null)
			mActivity.sendMessage(msg);
	}

	/**
	 * Post a veriry
	 */
	public void postVerifyRecvFile() {
		Message msg = new Message();
		msg.what = WifiP2pConfigInfo.MSG_VERIFY_RECV_FILE_DIALOG;
		if (mActivity != null)
			mActivity.sendMessage(msg);
	}

}
