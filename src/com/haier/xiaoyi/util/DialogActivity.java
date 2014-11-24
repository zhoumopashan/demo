package com.haier.xiaoyi.util;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import com.haier.xiaoyi.R;

public class DialogActivity extends Activity {
	
	private ProgressDialog mProgressDialog;
	private static final String TAG = "DialogActivity";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.dialog_activity_layout);
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		
		if(intent == null || intent.getAction() == null){
			return;
		}
		String action = intent.getAction();
		intent = null;
		setIntent(null);
		
		if(action.equals("discover_peers")){
			if (mProgressDialog != null && mProgressDialog.isShowing()) {
				mProgressDialog.dismiss();
			}
			mProgressDialog = ProgressDialog.show(this, getString(R.string.wifip2p_p2p_scanning_title), 
					getString(R.string.wifip2p_p2p_scanning), true, true, 
					new DialogInterface.OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					Logger.d(TAG, "onCancel discovery cancel.");
					finish();
				}
			});
			mProgressDialog.setCancelable(false);
		}
		else if(action.equals("connect")){
			if (mProgressDialog != null && mProgressDialog.isShowing()) {
				mProgressDialog.dismiss();
			}

			mProgressDialog = ProgressDialog.show(this,
					getString(R.string.wifip2p_connecting_cancel),
					getString(R.string.wifip2p_connecting), true, true,
					new DialogInterface.OnCancelListener() {
						@Override
						public void onCancel(DialogInterface dialog) {
							finish();
						}
					});
			mProgressDialog.setCancelable(false);
		}
		else if(action.equals("dismiss")){
			if (mProgressDialog != null && mProgressDialog.isShowing()) {
				mProgressDialog.dismiss();
			}
			finish();
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		
		Intent intent = getIntent();
		if(intent == null || intent.getAction() == null){
			return;
		}
		String action = intent.getAction();
		intent = null;
		setIntent(null);
		
		if(action.equals("discover_peers")){
			if (mProgressDialog != null && mProgressDialog.isShowing()) {
				mProgressDialog.dismiss();
			}
//			mProgressDialog.setCancelable(false);
			mProgressDialog = ProgressDialog.show(this, getString(R.string.wifip2p_p2p_scanning_title), 
					getString(R.string.wifip2p_p2p_scanning), true, true, 
					new DialogInterface.OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					Logger.d(TAG, "onCancel discovery cancel.");
					finish();
				}
			});
			mProgressDialog.setCancelable(false);
		}
		else if(action.equals("connect")){
			if (mProgressDialog != null && mProgressDialog.isShowing()) {
				mProgressDialog.dismiss();
			}

			mProgressDialog = ProgressDialog.show(this,
					getString(R.string.wifip2p_connecting_cancel),
					getString(R.string.wifip2p_connecting), true, true,
					new DialogInterface.OnCancelListener() {
						@Override
						public void onCancel(DialogInterface dialog) {
							finish();
						}
					});
			mProgressDialog.setCancelable(false);
		}
		else if(action.equals("dismiss")){
			if (mProgressDialog != null && mProgressDialog.isShowing()) {
				mProgressDialog.dismiss();
			}
			finish();
		}
	}
	
}
