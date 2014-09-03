package com.luo.demo.ui.viewpager;

import android.support.v4.view.ViewPager;

public class RootFeaturePageOnChangeListener implements ViewPager.OnPageChangeListener {
	
	private int mCurrentPage = 0;
	private int PAGE_1;
	
//	private ImageView mCursor; 
	private ViewPagesAdapter mAdapter;

//	public RootFeaturePageOnChangeListener(ViewPagesAdapter adapter,int offset,int bmpW,ImageView cursor) {
	public RootFeaturePageOnChangeListener(ViewPagesAdapter adapter,int offset,int bmpW) {
		PAGE_1 = offset * 2 + bmpW;
//		mCursor = cursor;
		mAdapter = adapter;
	}

	
	public void onPageScrollStateChanged(int arg0) {
		
		
	}

	public void onPageScrolled(int arg0, float arg1, int arg2) {
		
		
	}

	public void onPageSelected(int arg0) {
//		Animation animation = new TranslateAnimation(PAGE_1 * mCurrentPage, PAGE_1 * arg0, 0, 0);
		mCurrentPage = arg0;
		mAdapter.setCurrentPage(mCurrentPage);
//		animation.setFillAfter(true);
//		animation.setDuration(300);
//		mCursor.startAnimation(animation);
	}

}