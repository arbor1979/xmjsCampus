package com.dandian.campus.xmjs.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.Button;
import android.widget.TextView;

import com.dandian.campus.xmjs.R;
import com.dandian.campus.xmjs.base.ExitApplication;
import com.dandian.campus.xmjs.fragment.FragmentViewPager;

public class CourseListPage extends FragmentActivity {
	Button bn_title1, bn_title2;
	TextView title;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		ExitApplication.getInstance().addActivity(this);
		setContentView(R.layout.activity_homepage);
		FragmentViewPager localFragmentViewPager = new FragmentViewPager();
		getSupportFragmentManager().beginTransaction()
				.add(R.id.homepage_fragment, localFragmentViewPager).commit();
		setHead();
	}

	private void setHead() {
		bn_title1 = (Button) findViewById(R.id.btn_back);
		bn_title2 = (Button) findViewById(R.id.btn_goto);
		title = (TextView) findViewById(R.id.tv_title);
		bn_title1.setBackgroundResource(R.drawable.bg_title_homepage_back);
		bn_title2.setBackgroundResource(R.drawable.bg_title_homepage_go);
		title.setText("课程列表");
	}
}
