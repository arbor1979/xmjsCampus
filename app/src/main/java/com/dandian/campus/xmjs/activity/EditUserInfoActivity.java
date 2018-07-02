package com.dandian.campus.xmjs.activity;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.ImageOptions;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.dandian.campus.xmjs.R;
import com.dandian.campus.xmjs.api.CampusAPI;
import com.dandian.campus.xmjs.api.CampusException;
import com.dandian.campus.xmjs.api.CampusParameters;
import com.dandian.campus.xmjs.api.RequestListener;
import com.dandian.campus.xmjs.base.Constants;
import com.dandian.campus.xmjs.base.ExitApplication;
import com.dandian.campus.xmjs.db.DatabaseHelper;
import com.dandian.campus.xmjs.entity.User;
import com.dandian.campus.xmjs.fragment.BirthdayPickerFragment;
import com.dandian.campus.xmjs.util.AppUtility;
import com.dandian.campus.xmjs.util.Base64;
import com.dandian.campus.xmjs.util.DialogUtility;
import com.dandian.campus.xmjs.util.PrefUtility;

public class EditUserInfoActivity extends FragmentActivity implements
		OnClickListener {
	private Button bn_back, btn_name;
	private TextView tv_title,tv_go,departmentOrClassName;
	private ImageView photo;
	private EditText nickName,email,phone;
	private Spinner sex;
	public static TextView birthday;
	private String userId,userImage;
	private DatabaseHelper database;
	private Dao<User, Integer> userDao;
	private User userInfo;

	private Dialog mLoadingDialog;
	private static final String TAG = "EditTchInfoActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ExitApplication.getInstance().addActivity(this);
		setContentView(R.layout.activity_userinfo_edit);
		userId = getIntent().getStringExtra("userId");
		userImage = getIntent().getStringExtra("userImage");
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
		tv_title = (TextView) findViewById(R.id.setting_tv_title);
		tv_go = (TextView) findViewById(R.id.goto_text);
		tv_go.setVisibility(View.VISIBLE);
		tv_title.setText("用户详情修改");
		tv_go.setText("保存");
	}

	private void initContent() {
		try {
			userDao = getHelper().getUserDao();
			userInfo = userDao.queryBuilder().where().eq("id", userId)
					.queryForFirst();
			photo = (ImageView) findViewById(R.id.user_photo);
			nickName = (EditText) findViewById(R.id.et_teacherinfo_nick);
			sex = (Spinner) findViewById(R.id.et_teacherinfo_sex);
			email = (EditText) findViewById(R.id.et_teacherinfo_email);
			phone = (EditText) findViewById(R.id.et_teacherinfo_phone);
			birthday = (TextView) findViewById(R.id.et_teacherinfo_birthday);
			btn_name = (Button) findViewById(R.id.btn_user_name);
			departmentOrClassName = (TextView) findViewById(R.id.department_or_class_name);
			ArrayAdapter<CharSequence> adapter = ArrayAdapter
					.createFromResource(this, R.array.Sex,
							R.layout.view_spinner_item);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			String userType = userInfo.getUserType();
			sex.setAdapter(adapter);
			if (userType.equals("老师")) {
				btn_name.setText(userInfo.getName() + "(" + userType + ")");
				departmentOrClassName.setText(userInfo.getDepartment());
			} else {
				btn_name.setText(userInfo.getName() + "(" + userType + ")");
				departmentOrClassName.setText(userInfo.getsClass());
			}
			btn_name.setText(userInfo.getName());
			nickName.setText(userInfo.getNickname());
			email.setText(userInfo.getEmail());
			phone.setText(userInfo.getPhone());
			birthday.setText(userInfo.getBirthday());
			String gender = userInfo.getGender();
			if (gender != null && !gender.trim().toString().equals("")) {
				if (gender.equals("男")) {
					sex.setSelection(0);
				} else {
					sex.setSelection(1);
				}
			}
			if (AppUtility.isNotEmpty(userImage)) {
				AQuery aq = new AQuery(EditUserInfoActivity.this);
				ImageOptions options = new ImageOptions();
				options.round = 115;
				aq.id(photo).image(userImage, options);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	private void listener() {
		bn_back.setOnClickListener(this);
		tv_go.setOnClickListener(this);
		birthday.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.goto_text:
			String emailContent = email.getText().toString();
			String phoneContent = phone.getText().toString();
			if (AppUtility.checkEmail(emailContent)
					&& AppUtility.checkPhone(phoneContent)) {
				try {
					showDialog();
					userInfo.setNickname(nickName.getText().toString());
					userInfo.setGender(sex.getSelectedItem().toString());
					userInfo.setEmail(email.getText().toString());
					userInfo.setPhone(phone.getText().toString());
					userInfo.setBirthday(birthday.getText().toString());
					userDao.update(userInfo);

					/**
					 * 获取教师信息修改数据
					 */
					String teacherJsonStr = getChangeUserInfo(userInfo);
					System.out.println("teacherJsonStr:" + teacherJsonStr);
					// base64加密处理
					if (!"".equals(teacherJsonStr) && teacherJsonStr != null) {
						String teacherBase64 = Base64.encode(teacherJsonStr
								.getBytes());
						SubmitChangeinfo(teacherBase64, "changeuser");
					} else {
						closeDialog();
					}

				} catch (SQLException e) {
					e.printStackTrace();
					closeDialog();
				} catch (JSONException e) {
					e.printStackTrace();
					closeDialog();
				}

			} else {
				if (!AppUtility.checkEmail(emailContent)) {
					Toast.makeText(this, "请输入正确的邮箱格式", Toast.LENGTH_SHORT)
							.show();
				}
				if (!AppUtility.checkPhone(phoneContent)) {
					Toast.makeText(this, "请输入正确的手机号码", Toast.LENGTH_SHORT)
							.show();
				}
			}
			break;
		case R.id.back:
			finish();
			break;
		case R.id.et_teacherinfo_birthday:
			String number;
			if (birthday.getText().toString() != null
					&& !(birthday.getText().toString()).trim().toString()
							.equals("")) {
				number = birthday.getText().toString();
			} else {
				number = "nodata";
			}
			DialogFragment birthdayDialog = new BirthdayPickerFragment();
			Bundle localBundle = new Bundle();
			localBundle.putString("birthday", number);
			birthdayDialog.setArguments(localBundle);
			birthdayDialog.show(getSupportFragmentManager(), "datePicker");
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

	/**
	 * 功能描述:加工需要修改的教师信息
	 * 
	 * @author zhuliang 2013-12-6 下午4:31:06
	 * 
	 * @param userInfo
	 * @return
	 * @throws JSONException
	 */
	public String getChangeUserInfo(User userInfo) throws JSONException {
		if (userInfo != null) {
			JSONObject jo = new JSONObject();
			String checkCode = PrefUtility.get(Constants.PREF_CHECK_CODE, "");// 获取用户校验码
			jo.put("用户较验码", checkCode);
			jo.put("编号", userInfo.getId());
			jo.put("性别", userInfo.getGender());
			jo.put("呢称", userInfo.getNickname());
			jo.put("出生日期", userInfo.getBirthday());
			jo.put("手机", userInfo.getPhone());
			jo.put("电邮", userInfo.getEmail());
			return jo.toString();
		}
		return null;

	}

	/**
	 * 功能描述:提交服务器
	 * 
	 * @author yanzy 2013-12-4 上午10:10:42
	 * 
	 * @param base64Str
	 */
	public void SubmitChangeinfo(String base64Str, final String action) {
		CampusParameters params = new CampusParameters();
		params.add("action", action);
		params.add(Constants.PARAMS_DATA, base64Str);
		CampusAPI.Changeinfo(params, new RequestListener() {

			@Override
			public void onIOException(IOException e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onError(CampusException e) {
				Message msg = new Message();
				msg.what = 1;
				msg.obj = e.getMessage();
				mHandler.sendMessage(msg);
			}

			@Override
			public void onComplete(String response) {
				Bundle bundle = new Bundle();
				bundle.putString("action", action);
				bundle.putString("result", response.toString());
				Message msg = new Message();
				msg.what = 0;
				msg.obj = bundle;
				mHandler.sendMessage(msg);
			}
		});
	}

	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {

		public void handleMessage(Message msg) {
			Bundle bundle = new Bundle();
			switch (msg.what) {
			case 0:
				bundle = (Bundle) msg.obj;
				String action = bundle.getString("action");
				String result = bundle.getString("result");
				String resultStr = "";
				try {
					resultStr = new String(
							Base64.decode(result.getBytes("GBK")));
				} catch (UnsupportedEncodingException e1) {
					e1.printStackTrace();
				}
				System.out.println("action:" + action);
				System.out.println("resultStr:" + resultStr);

				try {
					JSONObject jo = new JSONObject(resultStr);
					if ("1".equals(jo.optString("成功"))) {
						if (mLoadingDialog != null) {
							mLoadingDialog.dismiss();
						}
						DialogUtility.showMsg(EditUserInfoActivity.this, "保存成功！");
						Log.d(TAG, "----------------->结束保存数据："
								+ new Date());
						finish();
					} else {
						closeDialog();
					}
				} catch (Exception e) {
					e.printStackTrace();
					closeDialog();
				}

				break;
			case 1:
				mLoadingDialog.dismiss();
				AppUtility.showErrorToast(EditUserInfoActivity.this, msg.obj.toString());
				break;

			}
		}
    };

	/**
	 * 功能描述:显示提示框
	 * 
	 * @author yanzy 2013-12-21 上午10:54:41
	 * 
	 */
	public void showDialog() {
		mLoadingDialog = DialogUtility.createLoadingDialog(
				EditUserInfoActivity.this, "数据保存中...");
		mLoadingDialog.show();
	}

	/**
	 * 功能描述:操作失败，提示
	 * 
	 * @author yanzy 2013-12-21 上午10:37:17
	 * 
	 */
	public void closeDialog() {
		if (mLoadingDialog != null) {
			mLoadingDialog.dismiss();
		}
		DialogUtility.showMsg(EditUserInfoActivity.this, "保存失败！");
	}
}
