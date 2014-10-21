package com.haier.xiaoyi.ui;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Pair;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.haier.xiaoyi.MainApplication;
import com.haier.xiaoyi.R;
import com.haier.xiaoyi.util.Logger;
import com.haier.xiaoyi.wifip2p.module.Utility;
import com.haier.xiaoyi.wifip2p.module.WifiP2pConfigInfo;

public class ParentActivity extends Activity implements View.OnClickListener{

	/******************************
	 * Macros <br>
	 ******************************/
	private static final String TAG = "ParentActivity";

	/******************************
	 * public Members <br>
	 ******************************/

	/******************************
	 * private Members <br>
	 ******************************/
	/** Layouts & Views */
	private View mPushImage;
	private View mBrightSet;
	private View mVolumnSet;
	private View mPersonal;

	/******************************
	 * InnerClass <br>
	 ******************************/
	
	/** Message Hander */
	private MainHandler mMainHandler;

	// MainHandler Definition
	class MainHandler extends Handler {
		// static final int MSG_UPDATE_NEW_VERSION = 100;

		@Override
		public void handleMessage(Message msg) {
			// switch (msg.what) {
			// case MSG_DOWNLOAD_PROCESS_CHANGE:
			// getUpdateHelper().setDialogProcess(msg.arg1);
			// break;
			// default:
			// break;
			// }
		}
	}

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
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.push_image:
			Logger.d(TAG, "push_image");
			startSelectImage();
			break;
		case R.id.setting_bright:
			Logger.d(TAG, "setting_bright");
			break;
		case R.id.setting_volumn:
			Logger.d(TAG, "setting_volumn");
			break;
		case R.id.persional:
			Logger.d(TAG, "persional");
			break;
		default:
			break;
		}
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// User has picked an image. Transfer it to group owner i.e peer using
		if (resultCode != RESULT_OK) {
			return;
		}

		if (requestCode == WifiP2pConfigInfo.REQUEST_CODE_SELECT_IMAGE) {
			if (data == null) {
				Logger.e(TAG, "onActivityResult data == null, no choice.");
				return;
			}
			Uri uri = data.getData();
//			mP2pService.getSendImageController().sendFile(uri , mP2pService);
//			((MainApplication)getApplication()).getXiaoyi().setPhotoUri(uri);
//			startService(new Intent(this,WifiP2pService.class).setAction("send_photo"));
			String host= ((MainApplication)getApplication()).getXiaoyi().getHostIp();
			int port = WifiP2pConfigInfo.LISTEN_PORT;
			new Thread(new MySendFileRunable(host,port,uri,getFileInfo(uri),getInputStream(uri))).start();
		}
	}
	
	/******************************
	 * public Methods <br>
	 ******************************/

	/******************************
	 * private Methods <br>
	 ******************************/

	private void initEnvironment() {
		// Init Main Handler
		mMainHandler = new MainHandler();
	}

	private void initWindow() {
		// Define Custom Window Title
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.parent_layout);
	}

	private void initLayoutsAndViews() {
		mPushImage = (TextView) findViewById(R.id.push_image);
		mPushImage.setOnClickListener(this);

		mBrightSet = (TextView) findViewById(R.id.setting_bright);
		mBrightSet.setOnClickListener(this);

		mVolumnSet = (TextView) findViewById(R.id.setting_volumn);
		mVolumnSet.setOnClickListener(this);

		mPersonal = (TextView) findViewById(R.id.persional);
		mPersonal.setOnClickListener(this);
	}
	
	/** Show a image Select dialog, let user to select a image to send */
	private void startSelectImage() {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("image/*");
		startActivityForResult(intent, WifiP2pConfigInfo.REQUEST_CODE_SELECT_IMAGE);
	}
	
	/** Get the file's info */
	public String getFileInfo(Uri uri){
		// get the name & fileSize of the uri-file
		Pair<String, Integer> pair;
		try {
			pair = Utility.getFileNameAndSize(ParentActivity.this, uri);
		} catch (IOException e) {
			return null;
		}
		String name = pair.first;
		long size = pair.second;
		
		return "size:" + size + "name:" + name;
	}
	
	/**
	 * Get the file's inputStream by uri
	 */
	public InputStream getInputStream(Uri uri) {
		ContentResolver cr = getContentResolver();
		try {
			return cr.openInputStream(uri);
		} catch (FileNotFoundException e) {
			return null;
		}
	}

	/**
	 * A send file task, send the file(uri) to the device mark by host & port
	 * @author luochenxun
	 */
	class MySendFileRunable implements Runnable {
		
		private String host;
		private int port;
		private Uri uri;
		private String fileInfo;
		private InputStream ins;
		
		MySendFileRunable(String host, int port, Uri uri , String info ,InputStream in) {
			this.host = host;
			this.port = port;
			this.uri = uri;
			this.fileInfo = info;
			ins = in;
		}
		
		@Override
		public void run() {
			sendFile();
		}
		
		private boolean sendFile() {
			Boolean result = Boolean.TRUE;
			Socket socket = new Socket();
			try {
				// connect the dst server
				socket.bind(null);
				socket.connect((new InetSocketAddress(host, port)), WifiP2pConfigInfo.SOCKET_TIMEOUT);
				Logger.d(this.getClass().getName(), "Client socket - " + socket.isConnected());
				
				// Get the file's info
				OutputStream outs = socket.getOutputStream();
				
				// output the commandId
				outs.write(WifiP2pConfigInfo.COMMAND_ID_SEND_FILE);
				
				// output the file's info
				if(fileInfo == null){
					return false;
				}
				Logger.d(this.getClass().getName(), "fileInfo:" + fileInfo);
				outs.write(fileInfo.length());
				outs.write(fileInfo.getBytes(), 0, fileInfo.length());
				
				// output the file's stream
				if(ins == null){
					return false;
				}
				byte buf[] = new byte[1024];
				int len;
				while ((len = ins.read(buf)) != -1) {
					outs.write(buf, 0, len);
				}

				// close socket
				ins.close();
				outs.close();
				Logger.d(this.getClass().getName(), "Client: Data written");
			} catch (FileNotFoundException e) {
				Logger.d(this.getClass().getName(), "send file exception " + e.toString());
			} catch (IOException e) {
				Logger.e(this.getClass().getName(),
						"send file exception " + e.getMessage());
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

}
