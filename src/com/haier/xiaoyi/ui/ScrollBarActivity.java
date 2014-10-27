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
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.haier.xiaoyi.MainApplication;
import com.haier.xiaoyi.R;
import com.haier.xiaoyi.ui.ParentActivity.MySendFileRunable;
import com.haier.xiaoyi.util.Logger;
import com.haier.xiaoyi.wifip2p.module.Utility;
import com.haier.xiaoyi.wifip2p.module.WifiP2pConfigInfo;

public class ScrollBarActivity extends Activity implements View.OnClickListener {

	/******************************
	 * Macros <br>
	 ******************************/
	private static final String TAG = "ParentActivity";
	private static final int BRIGHT = 0;
	private static final int VOICE = 1;
	private int mScrollType = BRIGHT;

	/******************************
	 * public Members <br>
	 ******************************/

	/******************************
	 * private Members <br>
	 ******************************/
	/** Layouts & Views */
	private TextView mTitle;
	private SeekBar mSeekBar;
	private ImageView mLeftView;
	private ImageView mRightView;

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
		((MainApplication) getApplication()).removeActivity(this);
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
		((MainApplication) getApplication()).addActivity(this);
	}

	private void initWindow() {
		// Define Custom Window Title
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.scrollbar_layout);
	}

	private void initLayoutsAndViews() {
		mTitle = (TextView) findViewById(R.id.scrollbar_layout_title);
		mLeftView = (ImageView) findViewById(R.id.scrollbar_layout_light_left);
		mRightView = (ImageView) findViewById(R.id.scrollbar_layout_light_right);
		mSeekBar = (SeekBar) findViewById(R.id.scrollbar_layout_seekbar);

		Intent intent = getIntent();
		if (intent == null || intent.getAction() == null) {
			return;
		}

		if (intent.getAction().equals("light")) {
			mTitle.setText(getString(R.string.light));
			mLeftView.setBackgroundResource(R.drawable.light_left);
			mRightView.setBackgroundResource(R.drawable.light_right);
			mSeekBar.setProgress(((MainApplication) getApplication()).getXiaoyi().getBright());
			mScrollType = BRIGHT;
		} else if (intent.getAction().equals("sound")) {
			mTitle.setText(getString(R.string.sound));
			mLeftView.setBackgroundResource(R.drawable.sound_left);
			mRightView.setBackgroundResource(R.drawable.sound_right);
			mSeekBar.setProgress(((MainApplication) getApplication()).getXiaoyi().getVolice());
			mScrollType = VOICE;
		}

		mSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// Logger.d(TAG, "onStopTrackingTouch");
				String ip = ((MainApplication) getApplication()).getXiaoyi().getHostIp();
				int bright = ((MainApplication) getApplication()).getXiaoyi().getBright();
				int voice = ((MainApplication) getApplication()).getXiaoyi().getVolice();
				new Thread(new SendDeviceInfoRunnable(ip, bright, voice)).start();
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// Logger.d(TAG, "onStartTrackingTouch");
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				switch (mScrollType) {
				case BRIGHT:
					((MainApplication) getApplication()).getXiaoyi().setBright(progress);
					break;
				case VOICE:
					((MainApplication) getApplication()).getXiaoyi().setVolice(progress);
					break;
				default:
					break;
				}
			}
		});
	}

	/** Show a image Select dialog, let user to select a image to send */
	private void startSelectImage() {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("image/*");
		startActivityForResult(intent, WifiP2pConfigInfo.REQUEST_CODE_SELECT_IMAGE);
	}

	/** Get the file's info */
	public String getFileInfo(Uri uri) {
		// get the name & fileSize of the uri-file
		Pair<String, Integer> pair;
		try {
			pair = Utility.getFileNameAndSize(ScrollBarActivity.this, uri);
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
	 * 
	 * @author luochenxun
	 */
	class SendDeviceInfoRunnable implements Runnable {

		private String mIp;
		private int mBright;
		private int mVoice;

		SendDeviceInfoRunnable(String ip, int bright, int voice) {
			mIp = ip;
			mBright = bright;
			mVoice = voice;
		}

		@Override
		public void run() {
			/* Construct socket */
			Socket socket = new Socket();

			try {
				socket.bind(null);
				socket.connect((new InetSocketAddress(mIp, WifiP2pConfigInfo.LISTEN_PORT)), WifiP2pConfigInfo.SOCKET_TIMEOUT);// host

				Logger.d(TAG, "Client socket - " + socket.isConnected());
				OutputStream stream = socket.getOutputStream();
				// send cmd
				stream.write(WifiP2pConfigInfo.COMMAND_ID_SENDBACK_DEVICE_INFO);
				// send data
				String strSend = "light:" + mBright + "sound:" + mVoice;
				stream.write(strSend.getBytes(), 0, strSend.length());

				Logger.d(TAG, "Client: Data written strSend:" + strSend);
			} catch (IOException e) {
				Logger.e(TAG, e.getMessage());
			} finally {
				if (socket != null) {
					if (socket.isConnected()) {
						try {
							socket.close();
							Logger.d(TAG, "socket.close();");
						} catch (IOException e) {
							// Give up
							e.printStackTrace();
						}
					}
				}
			}
		}

	}

}
