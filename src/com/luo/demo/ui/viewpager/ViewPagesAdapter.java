package com.luo.demo.ui.viewpager;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.luo.demo.R;
import com.luo.demo.ui.Tab1SubActivity;
import com.luo.demo.ui.Tab2SubActivity;
import com.luo.demo.ui.Tab3SubActivity;
import com.luo.demo.ui.Tab4SubActivity;

public class ViewPagesAdapter extends PagerAdapter {

	private static ViewPagesAdapter mAdapter;
	public ArrayList<SubActivity> mViewList;
	private Context mContext;
	private int mCurrentPage = 0;
	private TextView mTabTextView1;
	private TextView mTabTextView2;
	private TextView mTabTextView3;
	private TextView mTabTextView4;

	public int getCurrentPage() {
		return mCurrentPage;
	}

	public void setCurrentPage(int mCurrentPage) {
		this.mCurrentPage = mCurrentPage;
		switch (mCurrentPage) {
		case 0:
			mTabTextView1.setTextColor(mContext.getResources().getColor(R.color.actionbar_buttom_press));
			mTabTextView2.setTextColor(mContext.getResources().getColor(R.color.actionbar_buttom_normal));
			mTabTextView3.setTextColor(mContext.getResources().getColor(R.color.actionbar_buttom_normal));
			mTabTextView4.setTextColor(mContext.getResources().getColor(R.color.actionbar_buttom_normal));
			break;
		case 1:
			mTabTextView1.setTextColor(mContext.getResources().getColor(R.color.actionbar_buttom_normal));
			mTabTextView2.setTextColor(mContext.getResources().getColor(R.color.actionbar_buttom_press));
			mTabTextView3.setTextColor(mContext.getResources().getColor(R.color.actionbar_buttom_normal));
			mTabTextView4.setTextColor(mContext.getResources().getColor(R.color.actionbar_buttom_normal));
			break;
		case 2:
			mTabTextView1.setTextColor(mContext.getResources().getColor(R.color.actionbar_buttom_normal));
			mTabTextView2.setTextColor(mContext.getResources().getColor(R.color.actionbar_buttom_normal));
			mTabTextView3.setTextColor(mContext.getResources().getColor(R.color.actionbar_buttom_press));
			mTabTextView4.setTextColor(mContext.getResources().getColor(R.color.actionbar_buttom_normal));
			break;
		case 3:
			mTabTextView1.setTextColor(mContext.getResources().getColor(R.color.actionbar_buttom_normal));
			mTabTextView2.setTextColor(mContext.getResources().getColor(R.color.actionbar_buttom_normal));
			mTabTextView3.setTextColor(mContext.getResources().getColor(R.color.actionbar_buttom_normal));
			mTabTextView4.setTextColor(mContext.getResources().getColor(R.color.actionbar_buttom_press));
			break;
		default:
			break;
		}
	}

	private ViewPagesAdapter(Activity activity, TextView tab1, TextView tab2, TextView tab3, TextView tab4) {
		mContext = activity.getApplicationContext();
		mViewList = new ArrayList<SubActivity>();
		mViewList.add(new Tab1SubActivity(activity));
		mViewList.add(new Tab2SubActivity(activity));
		mViewList.add(new Tab3SubActivity(activity));
		mViewList.add(new Tab4SubActivity(activity));

		mTabTextView1 = tab1;
		mTabTextView2 = tab2;
		mTabTextView3 = tab3;
		mTabTextView4 = tab4;

		for (SubActivity subActivity : mViewList) {
			subActivity.onCreate();
		}
	}

	public static ViewPagesAdapter newInstance(Activity activity, TextView tab1, TextView tab2, TextView tab3, TextView tab4) {
		mAdapter = new ViewPagesAdapter(activity, tab1, tab2 , tab3 , tab4);
		return mAdapter;
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		container.addView(mViewList.get(position).getMainView());
		return mViewList.get(position);
	}

	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		return arg0 == ((SubActivity) arg1).getMainView();
	}

	@Override
	public int getCount() {
		return mViewList.size();
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		container.removeView(mViewList.get(position).getMainView());
	}

	@Override
	public int getItemPosition(Object object) {
		return mViewList.indexOf(object);
	}

	public void onResume() {
		for (SubActivity subActivity : mViewList) {
			subActivity.onResume();
		}
	}

	public boolean onBackPressed() {
		for (SubActivity subActivity : mViewList) {
			if (subActivity instanceof IBackKeyClickable)
				return ((IBackKeyClickable) subActivity).onBackPressed();
		}
		return true;
	}

	public void onStop() {
		for (SubActivity subActivity : mViewList) {
			subActivity.onStop();
		}
	}

	public void onDestory() {
		for (SubActivity subActivity : mViewList) {
			subActivity.onDestory();
		}
		mViewList.clear();
		mAdapter = null;
	}
}
