package com.haier.xiaoyi.ui;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.haier.xiaoyi.R;
import com.haier.xiaoyi.util.Logger;

public class PhotoActivity extends Activity {

	private static final String TAG = "PhotoActivity";
	private SurfaceView surfaceView;
	private SurfaceHolder surfaceHolder;
	private Camera camera;
	File picture;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.photo);
		setupViews();
		new MyThread().start(); // 开启线程，3秒后拍一张照片
	}

	private void setupViews() {
		surfaceView = (SurfaceView) findViewById(R.id.camera_preview);
		surfaceHolder = surfaceView.getHolder();
		surfaceHolder.addCallback(surfaceCallback);
		surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	private void takePic() {
		camera.takePicture(null, null, pictureCallback);
	}

	Camera.PictureCallback pictureCallback = new Camera.PictureCallback() {
		public void onPictureTaken(byte[] data, Camera camera) {
			new SavePictureTask().execute(data);
			camera.startPreview();
		}
	};

	class SavePictureTask extends AsyncTask<byte[], String, String> {
		@Override
		protected String doInBackground(byte[]... params) {
			File picture = new File("/sdcard/test.jpg");
			try {
				FileOutputStream fos = new FileOutputStream(picture.getPath());
				fos.write(params[0]);
				fos.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			Logger.d(TAG,"照片保存完成");
			return null;
		}
		
		@Override
		protected void onPostExecute(String result) {
			finish();
			super.onPostExecute(result);
		}
	}

	SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {

		public void surfaceCreated(SurfaceHolder holder) {
			camera = Camera.open();
			Logger.d(TAG,"摄像头open完成");
			try {
				camera.setPreviewDisplay(holder);
			} catch (IOException e) {
				camera.release();
				camera = null;
			}
		}

		public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
			Camera.Parameters parameters = camera.getParameters();
			parameters.setPictureFormat(PixelFormat.JPEG);
			camera.setDisplayOrientation(0);
			camera.setParameters(parameters);
			camera.startPreview();
		}

		public void surfaceDestroyed(SurfaceHolder holder) {
			camera.stopPreview();
			camera.release();
			camera = null;
		}
	};

	// 拍照线程
	class MyThread extends Thread {
		@Override
		public void run() {
			super.run();
			takePic();
		}
	}
}