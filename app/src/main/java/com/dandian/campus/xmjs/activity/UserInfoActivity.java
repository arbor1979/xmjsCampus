package com.dandian.campus.xmjs.activity;

import java.sql.SQLException;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.androidquery.callback.ImageOptions;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.dandian.campus.xmjs.R;
import com.dandian.campus.xmjs.base.ExitApplication;
import com.dandian.campus.xmjs.db.DatabaseHelper;
import com.dandian.campus.xmjs.entity.User;
import com.dandian.campus.xmjs.util.AppUtility;

public class UserInfoActivity extends Activity implements OnClickListener {
	Button bn_back;
	TextView tv_go;
	TextView tv_title;
	LinearLayout littleTitle,layoutTeacher,layoutStuOrJZ;
	ImageView userPhoto;// 头像
	TextView userName, userType;// 姓名,部门或班级名称

	TextView gender;// 性别
	TextView teacherPhone;// 手机
	TextView teacherEmail;// 邮箱
	TextView studentSno;// 学号
	TextView studentClass;// 班级
	TextView department;// 部门
	TextView teacherClass;// 所带班级
	TextView teacherCourse;// 所带课程
	TextView loginTime;// 所带课程
	
	DatabaseHelper database;
	Dao<User, Integer> userDao;
	User userInfo;
	String userId;
	String userImage;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_userinfo);
		ExitApplication.getInstance().addActivity(this);
		userId = getIntent().getStringExtra("userId");
		initTitle();
		initContent();
		listener();
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		initContent();
	}

	private void initTitle() {
		bn_back = (Button) findViewById(R.id.back);
		tv_go = (TextView) findViewById(R.id.goto_text);
		tv_title = (TextView) findViewById(R.id.setting_tv_title);
		//tv_go.setVisibility(View.VISIBLE);
		//tv_go.setText("编辑");
		tv_title.setText("用户详情");	
	}

	private void listener() {
		tv_go.setOnClickListener(this);
		bn_back.setOnClickListener(this);
	}

	private void initContent() {
		try {
			userDao = getHelper().getUserDao();
			userInfo = userDao.queryBuilder().where().eq("id", userId)
					.queryForFirst();
			layoutTeacher = (LinearLayout) findViewById(R.id.ly_teacher);
			layoutStuOrJZ = (LinearLayout) findViewById(R.id.ly_stu_or_jz);
			
			userPhoto = (ImageView) findViewById(R.id.user_photo);
			userName = (TextView) findViewById(R.id.user_name);
			userType = (TextView) findViewById(R.id.tv_user_type);
			gender = (TextView) findViewById(R.id.teacher_sex);
			studentSno=(TextView) findViewById(R.id.student_sno);
			studentClass=(TextView) findViewById(R.id.student_class);
			teacherPhone = (TextView) findViewById(R.id.teacher_phone);
			teacherEmail = (TextView) findViewById(R.id.teacher_email);
			department = (TextView) findViewById(R.id.teacher_department);
			teacherClass = (TextView) findViewById(R.id.teacher_class);
			teacherCourse = (TextView) findViewById(R.id.teacher_course);
			loginTime = (TextView) findViewById(R.id.tv_login_time);
			String userTypeStr = userInfo.getUserType();
			userName.setText(userInfo.getName());
			userType.setText("(" + userTypeStr + ")");
			if(userTypeStr.equals("老师")){
				layoutTeacher.setVisibility(View.VISIBLE);
				layoutStuOrJZ.setVisibility(View.GONE);
			}else{
				layoutTeacher.setVisibility(View.GONE);
				layoutStuOrJZ.setVisibility(View.VISIBLE);
			}
			studentSno.setText(userInfo.getId());
			studentClass.setText(userInfo.getsClass());
			gender.setText(userInfo.getGender());
			teacherPhone.setText(userInfo.getPhone());
			teacherEmail.setText(userInfo.getEmail());
			department.setText(userInfo.getDepartment());
			teacherClass.setText(userInfo.getWithClass());
			teacherCourse.setText(userInfo.getWithCourse());
			loginTime.setText(userInfo.getLoginTime());
			userImage = userInfo.getUserImage();
			if (AppUtility.isNotEmpty(userImage)) {
				AQuery aq = new AQuery(UserInfoActivity.this);
				ImageOptions options = new ImageOptions();
				options.memCache=false;
				options.targetWidth=200;
				options.round = 100;
				aq.id(userPhoto).image(userImage, options);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.goto_text:
			Intent editIntent = new Intent(this, EditUserInfoActivity.class);
			editIntent.putExtra("userId", userId);
			editIntent.putExtra("userImage", userImage);
			startActivity(editIntent);
			break;
		case R.id.back:
			finish();
			break;
		default:
			break;
		}
	}

	private DatabaseHelper getHelper() {
		if (database == null) {
			database = OpenHelperManager.getHelper(this, DatabaseHelper.class);
		}
		return database;
	}
}
