package com.dandian.campus.xmjs.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.dandian.campus.xmjs.R;
import com.dandian.campus.xmjs.base.Constants;
import com.dandian.campus.xmjs.base.ExitApplication;
import com.dandian.campus.xmjs.util.PrefUtility;

public class ExperienceActivity extends Activity {

	ViewPager mViewPager;
	MyAdapter adapter;
	List<View> viewList;
	TextView[] textViewPoints, textViewBlanks;
	TextView textViewPoint, textViewBlank;
	LayoutInflater inflater;
	View view1, view2, view3;
	AQuery aq;
	int[] id = { R.drawable.experience_call, R.drawable.experience_course,
			R.drawable.experience_download};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ExitApplication.getInstance().addActivity(this);
		setContentView(R.layout.activity_experience);
		mViewPager = (ViewPager) findViewById(R.id.pager);
		
		initPageView();
		adapter = new MyAdapter(viewList);
		mViewPager.setAdapter(adapter);
		initPoints();
		mViewPager.setOnPageChangeListener(new PointListener());

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (viewList != null) {
			for (int i = 0; i < viewList.size(); i++) {
				ImageView image = (ImageView) viewList.get(i).findViewById(
						R.id.image);
				if (image.getDrawable() != null) {
					((BitmapDrawable) image.getDrawable()).getBitmap()
							.recycle();
				}
			}
		}

	}

	private void initPageView() {
		inflater = getLayoutInflater();
		view1 = inflater.inflate(R.layout.experience_view_one, null);
		view2 = inflater.inflate(R.layout.experience_view_two, null);
		view3 = inflater.inflate(R.layout.experience_view_three, null);
		viewList = new ArrayList<View>();
		viewList.add(view1);
		viewList.add(view2);
		viewList.add(view3);
	}

	private void initPoints() {
		ViewGroup viewGroup = (ViewGroup) findViewById(R.id.points);
		textViewPoints = new TextView[adapter.getCount()];
		textViewBlanks = new TextView[adapter.getCount()];
		for (int i = 0; i < adapter.getCount(); i++) {

			textViewPoint = new TextView(getApplicationContext());
			textViewBlank = new TextView(getApplicationContext());
			// textView.setLayoutParams(new LayoutParams(10, 10));
			textViewPoint.setLayoutParams(new LayoutParams(10, 10));
			textViewBlank.setLayoutParams(new LayoutParams(10, 10));
			textViewPoints[i] = textViewPoint;
			textViewBlanks[i] = textViewBlank;
			if (i == 0) {
				// 默认进入程序后第一张图片被选中;
				textViewPoints[i]
						.setBackgroundResource(R.drawable.stuinfo_radio_sel1);
			} else {
				textViewPoints[i]
						.setBackgroundResource(R.drawable.stuinfo_radio_nosel);
			}
			viewGroup.addView(textViewPoints[i]);
			viewGroup.addView(textViewBlanks[i]);

		}
	}

	class MyAdapter extends PagerAdapter {
		List<View> list;

		public MyAdapter(List<View> list) {
			this.list = list;
		}

		@Override
		public int getCount() {
			return this.list.size();
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView(this.list.get(position));
		}

		@Override
		public int getItemPosition(Object object) {
			return super.getItemPosition(object);
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			View localView = this.list.get(position);
			ImageView image = (ImageView) localView.findViewById(R.id.image);
			image.setImageResource(id[position]);
			container.addView(this.list.get(position));
			
				View view = this.list.get(position);
				Button toLogin = (Button) view.findViewById(R.id.to_login);
				toLogin.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						PrefUtility.put(Constants.PREF_CHECK_RUN, true);
						Intent intent = new Intent(ExperienceActivity.this, LoginActivity.class);
						startActivity(intent);
						finish();
					}
				});
			
			return this.list.get(position);
		}
	}
	
	public class PointListener implements OnPageChangeListener {

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
			for (int i = 0; i < textViewPoints.length; i++) {
				textViewPoints[i]
						.setBackgroundResource(R.drawable.stuinfo_radio_sel1);
				if (arg0 != i) {
					textViewPoints[i]
							.setBackgroundResource(R.drawable.stuinfo_radio_nosel);
				}
			}
		}
	}
}
