package com.luo.demo.ui;


import android.app.Activity;
import android.view.View;
import android.view.View.OnClickListener;

import com.luo.demo.R;
import com.luo.demo.ui.viewpager.SubActivity;


public class Tab2SubActivity extends SubActivity implements OnClickListener {

	private static final String TAG = "Tab2SubActivity";



	public Tab2SubActivity(Activity activity) {
		super(activity);
	}


	@Override
	public void onCreate() {
		setContentView(R.layout.activity_tab2);
		initLayoutAndViews();
		initEnvironment();
	}

	@Override
	public View onCreateView() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onResume() {

	}

	@Override
	public void onStop() {

	}

	@Override
	public void onDestory() {

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
//		case R.id.subactivity_tools_btn_edit:

//			break;
		default:
			break;
		}
	}

	/****************
	 * Private Methods
	 ***************/
	private void initEnvironment() {


	}
	


	private void initLayoutAndViews() {

	}

}
