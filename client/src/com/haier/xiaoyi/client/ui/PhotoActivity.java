package com.haier.xiaoyi.client.ui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import android.app.Activity;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.haier.xiaoyi.client.R;
import com.haier.xiaoyi.client.module.WifiP2pConfigInfo;
import com.haier.xiaoyi.client.util.Logger;

public class PhotoActivity extends Activity {

	private static final String TAG = "PhotoActivity";
	private SurfaceView surfaceView;
	private SurfaceHolder surfaceHolder;
	private Camera camera;
	private File mPicture;
	private String mIp;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.photo);
		setupViews();
		mIp = getIntent().getStringExtra("ip");
		mPicture = new File(Environment.getExternalStorageDirectory() + "/ecan/cache.jpg");
		new MyThread().start(); 
//		new Thread(new MySendFileRunable() ).start();
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

			try {
				FileOutputStream fos = new FileOutputStream(mPicture.getPath());
				fos.write(params[0]);
				fos.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			Logger.d(TAG,"照片保存完成");
			new Thread(new MySendFileRunable() ).start();
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
				Logger.e(TAG,"摄像头 error");
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
	
	/**
	 * A send file task, send the file(uri) to the device mark by host & port
	 * @author luochenxun
	 */
	class MySendFileRunable implements Runnable {
		
		@Override
		public void run() {
			sendFile();
		}
		
		private boolean sendFile() {
			Boolean result = Boolean.TRUE;
			boolean isShowDialog = false;
			Socket socket = new Socket();
			try {
				// connect the dst server
				socket.bind(null);
				
				socket.connect((new InetSocketAddress(mIp, WifiP2pConfigInfo.LISTEN_PORT)), WifiP2pConfigInfo.SOCKET_TIMEOUT);
				Logger.d(this.getClass().getName(), "Client socket - " + socket.isConnected());
				Logger.d(this.getClass().getName(), "mip - " + mIp );
				
				// Get the file's info
				OutputStream outs = socket.getOutputStream();
				
				// output the commandId
				outs.write(WifiP2pConfigInfo.COMMAND_ID_SEND_FILE);
				
				InputStream ins =  new FileInputStream(mPicture);
				
				byte buf[] = new byte[1024];
				int len = 0;
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