package com.haier.xiaoyi.client.ui;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.haier.xiaoyi.client.R;

public class ClockActivity extends Activity {
	
	MediaPlayer mMediaPlayer = new MediaPlayer();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.clock_main);

		if (getIntent() == null) {
			finish();
		}

		String msg = getIntent().getExtras().getString("clock_msg");
		boolean isOpen = getIntent().getExtras().getBoolean("clock_open");
		if (isOpen == false) {
			finish();
		}

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日\n   HH:mm");
		String str = sdf.format(new Date());
		((TextView) findViewById(R.id.clock_msg)).setText(msg);
		((TextView) findViewById(R.id.clock_msg2)).setText(str);

		setIntent(null);

		playRing();

		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					Thread.sleep(1000 * 60);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				finish();
			}
		}).start();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		finish();
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onDestroy() {
		stopPlayRing();
		super.onDestroy();
	}

	private void playRing() {
		try {
			Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
			mMediaPlayer.setDataSource(this, alert);
			mMediaPlayer.setAudioStreamType(AudioManager.STREAM_RING);
			mMediaPlayer.setLooping(true);
			mMediaPlayer.prepare();
			mMediaPlayer.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void stopPlayRing() {
		try {
			if (this.mMediaPlayer != null) {
				if (this.mMediaPlayer.isPlaying()) {
					this.mMediaPlayer.stop();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
