package com.haier.xiaoyi.ui;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.content.ContentResolver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.DatePicker;
import android.widget.DatePicker.OnDateChangedListener;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.TimePicker.OnTimeChangedListener;
import android.widget.Toast;

import com.haier.xiaoyi.MainApplication;
import com.haier.xiaoyi.R;
import com.haier.xiaoyi.util.Logger;
import com.haier.xiaoyi.wifip2p.module.WifiP2pConfigInfo;

public class SetDateActivity extends Activity {

	/******************************
	 * Macros <br>
	 ******************************/
	private static final String TAG = "SetDateActivity";

	/******************************
	 * public Members <br>
	 ******************************/

	/******************************
	 * private Members <br>
	 ******************************/
	/** Layouts & Views */
	private TimePicker mTimePicker;
	private DatePicker mDatePicker;
	private TextView mOk;

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
			Toast.makeText(SetDateActivity.this, R.string.setdate_success, Toast.LENGTH_LONG).show();
			finish();
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
		setContentView(R.layout.setdate_layout);
	}

	private void initLayoutsAndViews() {

		mDatePicker = (DatePicker) findViewById(R.id.datePicker);
		mTimePicker = (TimePicker) findViewById(R.id.timePicker);
		mTimePicker.setIs24HourView(true);
		mTimePicker.setEnabled(true);

		Calendar calendar = Calendar.getInstance();
		int year = calendar.get(Calendar.YEAR);
		int monthOfYear = calendar.get(Calendar.MONTH);
		int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
		mDatePicker.init(year, monthOfYear, dayOfMonth, new OnDateChangedListener() {
			public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
//				dateEt.setText("您选择的日期是：" + year + "年" + (monthOfYear + 1) + "月" + dayOfMonth + "日。");
				Logger.d(TAG,"您选择的日期是：" + year + "年" + (monthOfYear + 1) + "月" + dayOfMonth + "日。");
			}
		});

		mTimePicker.setOnTimeChangedListener(new OnTimeChangedListener() {

			public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
//				timeEt.setText("您选择的时间是：" + hourOfDay + "时" + minute + "分。");
				Logger.d(TAG,"您选择的时间是：" + hourOfDay + "时" + minute + "分。");
			}
		});

		mOk = (TextView)findViewById(R.id.setdate_ok);

		mOk.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				int h = mTimePicker.getCurrentHour();
				int m = mTimePicker.getCurrentMinute();
				int year = mDatePicker.getYear();
				int month = mDatePicker.getMonth();
				int date = mDatePicker.getDayOfMonth();

				Calendar clockTime = Calendar.getInstance();
				clockTime.setTime(new Date());
				clockTime.set(Calendar.YEAR, year);
				clockTime.set(Calendar.MONTH, month);
				clockTime.set(Calendar.DATE, date);
				clockTime.set(Calendar.HOUR_OF_DAY, h);
				clockTime.set(Calendar.MINUTE, m);
				long clockTimeLong = clockTime.getTimeInMillis();
				Logger.d(TAG,"year:" + year);
				Logger.d(TAG,"month:" + month);
				Logger.d(TAG,"date:" + date);
				Logger.d(TAG,"hour:" + h);
				Logger.d(TAG,"minute" + m);
				Logger.d(TAG,"ct:" + System.currentTimeMillis());
				Logger.d(TAG,"mt:" + clockTimeLong);
				Logger.d(TAG,"cha:" + (clockTimeLong - System.currentTimeMillis()));

				String ip = ((MainApplication) getApplication()).getXiaoyi().getHostIp();
				new Thread(new SendDateRunnable(ip , year , month , date , h , m) ).start();
			}
		});
		
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
	class SendDateRunnable implements Runnable {

		private String mIp;
		private int mYear;
		private int mMonth;
		private int mDate;
		private int mHour;
		private int mMin;

		SendDateRunnable(String ip, int year , int month , int date , int hour, int min) {
			this.mYear = year;
			this.mMonth = month;
			this.mDate = date;
			mIp = ip;
			mHour = hour;
			mMin = min;
		}

		@Override
		public void run() {
			/* Construct socket */
			Socket socket = new Socket();
			int port = WifiP2pConfigInfo.LISTEN_PORT;
			boolean isSuccess = true;

			try {
				socket.bind(null);

				if (((MainApplication) getApplication()).getXiaoyi().isWifiAvailable()) {
					mIp = ((MainApplication) getApplication()).getXiaoyi().getWifiIp();
					port = WifiP2pConfigInfo.WIFI_PORT;
				}

				socket.connect((new InetSocketAddress(mIp, port)), WifiP2pConfigInfo.SOCKET_TIMEOUT);// host

				Logger.d(TAG, "Client socket - " + socket.isConnected());
				OutputStream stream = socket.getOutputStream();
				// send cmd
				stream.write(WifiP2pConfigInfo.COMMAND_ID_DATE);
				// send data
				stream.write(mYear / 100);
				stream.write(mYear % 100);
				stream.write(mMonth);
				stream.write(mDate);
				stream.write(mHour);
				stream.write(mMin);

			} catch (IOException e) {
				isSuccess = false;
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
			
			if(isSuccess){
				mMainHandler.sendEmptyMessage(100);
			}
		}

	}


}
