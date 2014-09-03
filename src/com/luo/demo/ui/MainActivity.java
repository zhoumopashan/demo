package com.luo.demo.ui;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.luo.demo.R;
import com.luo.demo.ui.viewpager.RootFeaturePageOnChangeListener;
import com.luo.demo.ui.viewpager.ViewPagesAdapter;

public class MainActivity extends Activity implements View.OnClickListener {

	/******************************
	 * Macros <br>
	 ******************************/

	public final static int PAGE_1 = 0;
	public final static int PAGE_2 = 1;
	public final static int PAGE_3 = 2;
	public final static int PAGE_4 = 3;

	/******************************
	 * public Members <br>
	 ******************************/

	/******************************
	 * private Members <br>
	 ******************************/
	/** Layouts & Views */
	private ViewPager mViewContainer;
	private ViewPagesAdapter mViewsAdapter;
	/** The cursor of the underTitle on the bottom of the device */
//	private ImageView mTabCursor;
	// The Cursor's width
	private int mCursorBmpW;
	// The offset of the Cursor
	private int mCursorOffset = 0;

	// Bottom bars text
	private TextView mPageText1;
	private TextView mPageText2;
	private TextView mPageText3;
	private TextView mPageText4;

	// Title
	private TextView mTitle;

	// private RootStatusManager mStatusManager;

	/******************************
	 * InnerClass <br>
	 ******************************/

	/** Message Hander */
	private MainHandler mMainHandler;

	// MainHandler Definition
	class MainHandler extends Handler {
//		static final int MSG_UPDATE_NEW_VERSION = 100;

		@Override
		public void handleMessage(Message msg) {
//			switch (msg.what) {
//			case MSG_DOWNLOAD_PROCESS_CHANGE:
//				getUpdateHelper().setDialogProcess(msg.arg1);
//				break;
//			default:
//				break;
//			}
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
		mViewsAdapter.onResume();
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
		mViewsAdapter.onStop();
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		mViewsAdapter.onDestory();
		super.onDestroy();
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.buttomTab1:
			mViewContainer.setCurrentItem(PAGE_1);
			break;
		case R.id.buttomTab2:
			mViewContainer.setCurrentItem(PAGE_2);
			break;
		case R.id.buttomTab3:
			mViewContainer.setCurrentItem(PAGE_3);
			break;
		case R.id.buttomTab4:
			mViewContainer.setCurrentItem(PAGE_4);
			break;
		default:
			break;
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
		setContentView(R.layout.activity_main);
		// getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
		// R.layout.windowtitle);

		// mTitle = (TextView)findViewById(R.id.left_title_text);
		// mTitle.setText(R.string.app_name_zh);
	}

	private void initLayoutsAndViews() {
		/** Init the Cursor */
//		mTabCursor = (ImageView) findViewById(R.id.buttomTabCursor);
		// get The Cursor's width
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		int screenW = dm.widthPixels;
		mCursorBmpW = screenW / 2;
		// mCursorOffset = (screenW / 3 - mCursorBmpW) / 2;
		// Matrix matrix = new Matrix();
		// matrix.postTranslate(mCursorOffset, 0);
		// // Set the init pos of the cursor
		// mTabCursor.setImageMatrix(matrix);
		// Set the Cursor's width
//		LayoutParams param = mTabCursor.getLayoutParams();
//		param.width = mCursorBmpW;
//		mTabCursor.setLayoutParams(param);
//		mTabCursor.getBackground().setAlpha(100);

		/** Init the buttomTab */
		mPageText1 = (TextView) findViewById(R.id.buttomTab1);
		mPageText2 = (TextView) findViewById(R.id.buttomTab2);
		mPageText3 = (TextView) findViewById(R.id.buttomTab3);
		mPageText4 = (TextView) findViewById(R.id.buttomTab4);
		// setOnClickListener
		mPageText1.setOnClickListener(this);
		mPageText2.setOnClickListener(this);
		mPageText3.setOnClickListener(this);
		mPageText4.setOnClickListener(this);

		/** Init the View pager */
		mViewContainer = (ViewPager) findViewById(R.id.viewContainer);
		mViewsAdapter = ViewPagesAdapter.newInstance(this, mPageText1, mPageText2 , mPageText3 , mPageText4);
		mViewContainer.setAdapter(mViewsAdapter);
		mViewsAdapter.setCurrentPage(0);
//		mViewContainer.setOnPageChangeListener(new RootFeaturePageOnChangeListener(mViewsAdapter, mCursorOffset, mCursorBmpW, mTabCursor));
		mViewContainer.setOnPageChangeListener(new RootFeaturePageOnChangeListener(mViewsAdapter, mCursorOffset, mCursorBmpW));
	}

}
