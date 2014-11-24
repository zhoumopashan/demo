package com.haier.xiaoyi.client.ui;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.haier.xiaoyi.client.R;

public class ViewPhotoActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.view_photo_main);

		if (getIntent() == null) {
			finish();
		}

		String photoPath = getIntent().getExtras().getString("path");

		if (TextUtils.isEmpty(photoPath)) {
			finish();
		}

		Bitmap bitmap = getBitmapFromPathSafe(photoPath, 3, true);
		if (bitmap == null) {
			finish();
		}

		((ImageView) findViewById(R.id.view_photo)).setImageBitmap(bitmap);

//		new Thread(new Runnable() {
//
//			@Override
//			public void run() {
//				try {
//					Thread.sleep(5000);
//				} catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//				finish();
//			}
//		}).start();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		finish();
		return super.onKeyDown(keyCode, event);
	}

	private Bitmap getBitmapFromPathSafe(String path, int retryTime, boolean isDoCompress) {
		Bitmap tempBitmap = null;

		while (tempBitmap == null && retryTime > 0) {
			--retryTime;
			try {
				if (isDoCompress) {
					BitmapFactory.Options options = new BitmapFactory.Options();
					options.inSampleSize = 3 - retryTime;
					tempBitmap = BitmapFactory.decodeFile(path, options);
				} else {
					tempBitmap = BitmapFactory.decodeFile(path);
				}
			} catch (OutOfMemoryError eo) {
				System.gc();
				try {
					Thread.sleep(600);
				} catch (InterruptedException e) {
				}
			} catch (Exception e) {
				return null;
			}
		}
		return tempBitmap;
	}
}
