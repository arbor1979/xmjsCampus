package com.dandian.campus.xmjs.fragment;

import java.util.ArrayList;

import com.dandian.campus.xmjs.R;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class FragmentViewPager extends Fragment {
	ViewPager viewpager;
	AdapterHomePage mAdapterHomePage;
	TextView yesterday, en_yesterday, today, en_today, tomorrow, en_tomorrow;
	ArrayList<TextView[]> groupText;
	TextView[] childText;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View localView = inflater.inflate(R.layout.viewpager, container, false);
		viewpager = (ViewPager) localView.findViewById(R.id.pager);
		mAdapterHomePage = new AdapterHomePage(getActivity()
				.getSupportFragmentManager());
		viewpager.setAdapter(mAdapterHomePage);
		handlerTitle(localView);
		viewpager.setOnPageChangeListener(new MyListener());

		return localView;
	}

	public void handlerTitle(View view) {
		yesterday = (TextView) view.findViewById(R.id.yesterday);
		en_yesterday = (TextView) view.findViewById(R.id.en_yesterday);
		today = (TextView) view.findViewById(R.id.today);
		en_today = (TextView) view.findViewById(R.id.en_today);
		tomorrow = (TextView) view.findViewById(R.id.tomorrow);
		en_tomorrow = (TextView) view.findViewById(R.id.en_tomorrow);

		groupText = new ArrayList<TextView[]>();
		childText = new TextView[2];
		childText[0] = yesterday;
		childText[1] = en_yesterday;
		groupText.add(childText);
		childText = new TextView[2];
		childText[0] = today;
		childText[1] = en_today;
		groupText.add(childText);
		childText = new TextView[2];
		childText[0] = tomorrow;
		childText[1] = en_tomorrow;
		groupText.add(childText);
		for (int i = 0; i < mAdapterHomePage.getCount(); i++) {
			if (i == 0) {
				groupText.get(i)[0].setTextColor(Color.WHITE);
				groupText.get(i)[1].setTextColor(Color.WHITE);
			} else {
				groupText.get(i)[0].setTextColor(Color.parseColor("#cddad3"));
				groupText.get(i)[1].setTextColor(Color.parseColor("#cddad3"));
			}
		}

	}

	public class AdapterHomePage extends FragmentPagerAdapter {

		public AdapterHomePage(FragmentManager fm) {
			super(fm);
			// TODO Auto-generated constructor stub
		}

		@Override
		public Fragment getItem(int arg0) {
			// TODO Auto-generated method stub
			FragmentCourse mFragmentCourse = new FragmentCourse();
			Bundle localbundle = new Bundle();
			localbundle.putInt("n", arg0);
			mFragmentCourse.setArguments(localbundle);
			return mFragmentCourse;
		}

		@Override
		public int getItemPosition(Object object) {
			// TODO Auto-generated method stub
			return super.getItemPosition(object);
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return 3;
		}

	}

	public class MyListener implements OnPageChangeListener {

		@Override
		public void onPageScrollStateChanged(int arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onPageSelected(int arg0) {
			// TODO Auto-generated method stub
			for (int i = 0; i < groupText.size(); i++) {
				groupText.get(i)[0].setTextColor(Color.WHITE);
		//		groupText.get(i)[0].setTextSize(R.dimen.text_size_big);
				groupText.get(i)[1].setTextColor(Color.WHITE);
		//		groupText.get(i)[1].setTextSize(R.dimen.text_size_micro);
				if (arg0 != i) {
					groupText.get(i)[0].setTextColor(Color
							.parseColor("#cddad3"));
		//			groupText.get(i)[0].setTextSize(R.dimen.text_size_normal);
					groupText.get(i)[1].setTextColor(Color
							.parseColor("#cddad3"));
		//			groupText.get(i)[1].setTextSize(R.dimen.text_size_xmicro);
				}
			}
		}
	}

}
