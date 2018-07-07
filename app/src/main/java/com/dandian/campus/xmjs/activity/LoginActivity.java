package com.dandian.campus.xmjs.activity;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import net.minidev.json.JSONValue;

import org.android.agoo.common.AgooConstants;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.TableRow;
import android.widget.TextView;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.dandian.campus.xmjs.CampusApplication;
import com.dandian.campus.xmjs.R;
import com.dandian.campus.xmjs.api.CampusAPI;
import com.dandian.campus.xmjs.api.CampusException;
import com.dandian.campus.xmjs.api.CampusParameters;
import com.dandian.campus.xmjs.api.RequestListener;
import com.dandian.campus.xmjs.base.Constants;
import com.dandian.campus.xmjs.base.ExitApplication;
import com.dandian.campus.xmjs.db.DatabaseHelper;
import com.dandian.campus.xmjs.db.InitData;
import com.dandian.campus.xmjs.entity.AccountInfo;
import com.dandian.campus.xmjs.entity.Equipment;
import com.dandian.campus.xmjs.entity.TeacherInfo;
import com.dandian.campus.xmjs.entity.User;
import com.dandian.campus.xmjs.service.Alarmreceiver;
import com.dandian.campus.xmjs.util.AppUtility;
import com.dandian.campus.xmjs.util.BaiduPushUtility;
import com.dandian.campus.xmjs.util.Base64;
import com.dandian.campus.xmjs.util.DateHelper;
import com.dandian.campus.xmjs.util.DialogUtility;
import com.dandian.campus.xmjs.util.PrefUtility;
import com.j256.ormlite.stmt.PreparedDelete;
import com.umeng.message.PushAgent;
import com.umeng.message.UTrack;
import com.umeng.message.UmengNotifyClickActivity;

import android.provider.Settings;

public class LoginActivity extends UmengNotifyClickActivity implements OnClickListener,
		OnDismissListener {
	private static final String TAG = "LoginActivity";
	private EditText mUsernameView, mPasswordView;
	private Button loginButton;
	private CheckBox cb_remeberPwd;
	private TableRow table_item;
	private String mUsername, mPassword;
	private Dao<Equipment, Integer> eqmDao;
	private Dao<User, Integer> userDao;
	private Dao<AccountInfo, Integer> accountInfoDao;
	private Dialog mLoadingDialog, experienceDialog, userTypeDialog;
    private User user;
	private DatabaseHelper database;
	private ImageButton login_choose;
	private ListView listView;
	private PopupWindow popupWindow;
	private loginHistoryAdapter adapter;
	private AccountInfo lastLogin;
    private final String domain="@xmjsxy.cn";
	private boolean flag1=false,flag2=false;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		

		setContentView(R.layout.activity_login);
		ExitApplication.getInstance().addActivity(this);
		try {
			accountInfoDao = getHelper().getAccountInfoDao();
			lastLogin = accountInfoDao.queryBuilder()
					.orderBy("loginTime", false).queryForFirst();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		//listData = new ArrayList<String>();
		// 初始化视图组件
		buildComponents();

		// Push: 以apikey的方式登录，一般放在主Activity的onCreate中。
		// 这里把apikey存放于manifest文件中，只是一种存放方式，
		// 您可以用自定义常量等其它方式实现，来替换参数中的Utils.getMetaValue(PushDemoActivity.this,
		// "api_key")
		// 通过share preference实现的绑定标志开关，如果已经成功绑定，就取消这次绑定
		// if (!BaiduPushUtility.hasBind()) {
		PrefUtility.put(Constants.PREF_BAIDU_USERID, "");
		PrefUtility.put(Constants.PREF_CHECK_CODE,"");
		
		// }
		
		registerBoradcastReceiver();
		
		mUsername=PrefUtility.get(Constants.PREF_LOGIN_NAME, "");
		mPassword=PrefUtility.get(Constants.PREF_LOGIN_PASS, "");
		
		if(AppUtility.isNotEmpty(mUsername) && PrefUtility.getBoolean(Constants.PREF_LOGIN_REMEBER, false))
		{
			mUsernameView.setText(mUsername);
			mPasswordView.setText(mPassword);
			doLogin();
		}
		
	}

	public void getInitDataAndContracts()
	{
		flag1=false;
		flag2=false;
		PrefUtility.put(Constants.PREF_INIT_BASEDATE_FLAG, false);
		PrefUtility.put(Constants.PREF_INIT_CONTACT_FLAG, false);
		mLoadingDialog.show();
		
		PrefUtility.put(Constants.PREF_SELECTED_WEEK, 0);
		String checkCode = PrefUtility.get(Constants.PREF_CHECK_CODE, "");
		InitData initData = new InitData(LoginActivity.this,getHelper(), null,"refreshSubject",checkCode);
		initData.initAllInfo();
		
		initData = new InitData(LoginActivity.this,
				getHelper(), null, "refreshContact", PrefUtility.get(Constants.PREF_CHECK_CODE, ""));
		initData.initContactInfo();
		
		
	}
	public void registerBoradcastReceiver() {
		IntentFilter myIntentFilter = new IntentFilter();
		myIntentFilter.addAction("refreshSubject");
		myIntentFilter.addAction("refreshContact");
		// 注册广播
		registerReceiver(mBroadcastReceiver, myIntentFilter);
	}

	private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			
			Log.d(TAG, "----------->BroadcastReceiver：" + action);
		
			if(action.equals("refreshSubject"))
				flag1=true;
			if( action.equals("refreshContact"))
				flag2=true;
			
			if(flag1 && flag2)
			{
				if(PrefUtility.getBoolean(Constants.PREF_INIT_BASEDATE_FLAG, false) && PrefUtility.getBoolean(Constants.PREF_INIT_CONTACT_FLAG, false)) 
					jumpMain();
				else
				{
					if(!PrefUtility.getBoolean(Constants.PREF_INIT_BASEDATE_FLAG, false))
						AppUtility.showToastMsg(LoginActivity.this,"初始化课表失败");
					else if(!PrefUtility.getBoolean(Constants.PREF_INIT_CONTACT_FLAG, false))
						AppUtility.showToastMsg(LoginActivity.this,"初始化联系人失败");
				}
				if(mLoadingDialog!=null)
					mLoadingDialog.dismiss();
					
			}
		}
	};
	private void buildComponents() {
		table_item = (TableRow) findViewById(R.id.table_item);
		mUsernameView = (EditText) findViewById(R.id.login_username);
		mPasswordView = (EditText) findViewById(R.id.login_password);
		loginButton = (Button) findViewById(R.id.btn_login);
        cb_remeberPwd= (CheckBox) findViewById(R.id.cb_remeberPwd);
        if(PrefUtility.getBoolean(Constants.PREF_LOGIN_REMEBER, false))
            cb_remeberPwd.setChecked(true);
        else
            cb_remeberPwd.setChecked(false);
		login_choose = (ImageButton) findViewById(R.id.login_choose);
		mLoadingDialog = DialogUtility.createLoadingDialog(this, "正在登录...");
		if (lastLogin != null) {
			mUsernameView.setText(lastLogin.getUserName());
			mPasswordView.setText(lastLogin.getPassWord());
		}
		// 注册按钮点击事件
		loginButton.setOnClickListener(this);
		login_choose.setOnClickListener(this);
		/*
		mUsernameView.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				if (lastLogin != null) {
					String mString = mUsernameView.getText().toString();
					if (!mString.equals(lastLogin.getUserName())) {
						mPasswordView.setText("");
					}
				}

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub

			}

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub

			}
		});
		*/
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onClick(View v) {
		switch (v.getId()) {

		case R.id.btn_login:
			PrefUtility.put(Constants.PREF_CHECK_TEST, false);
			attemptLogin();
			break;
		case R.id.login_choose:
			if (adapter == null) {
				listView = new ListView(this);
				listView.setBackgroundColor(Color.GRAY);
				adapter = new loginHistoryAdapter();
				listView.setAdapter(adapter);
				popupWindow = new PopupWindow(listView, table_item.getWidth(),
						LayoutParams.WRAP_CONTENT);
				popupWindow.setFocusable(true);
				// 点击外部消失
				popupWindow.setOutsideTouchable(true);
				
				popupWindow.setBackgroundDrawable(new BitmapDrawable());
				popupWindow.showAsDropDown(table_item);
				login_choose.setImageResource(R.drawable.login_btn_bg_sel);
			} else {
				adapter.notifyDataSetChanged();
				popupWindow = new PopupWindow(listView, table_item.getWidth(),
						LayoutParams.WRAP_CONTENT);
				popupWindow.setFocusable(true);
				// 点击外部消失
				popupWindow.setOutsideTouchable(true);
				popupWindow.setBackgroundDrawable(new BitmapDrawable());
				popupWindow.showAsDropDown(table_item);
				login_choose.setImageResource(R.drawable.login_btn_bg_sel);
			}
			popupWindow.setOnDismissListener(this);
			break;

		}
	}

	private void attemptLogin() {
		// Reset errors.
		mUsernameView.setError(null);
		mPasswordView.setError(null);

		// Store values at the time of the login attempt.
		mUsername = mUsernameView.getText().toString();
		mPassword = mPasswordView.getText().toString();

		boolean cancel = false;
		View focusView = null;

		if (TextUtils.isEmpty(mUsername)) {
			mUsernameView.setError(getString(R.string.error_username_required));
			focusView = mUsernameView;
			cancel = true;
		}
		if (cancel) {
			focusView.requestFocus();
		} else {
			// 显示登录提示
			doLogin();
		}
	}

	/**
	 * 执行登录操作
	 * 
	 * @description 变量:"用户名"和"密码" <br/>
	 *              对变量名进行JSON编码后，再进行Base64编码，然后提交，提交使用的参数名称为"DATA"。<br/>
	 * 
	 * @return 密文，先进行Base64解码,再进行JSON数据解析。
	 * 
	 */
	@SuppressWarnings("deprecation")
	private void doLogin() {
		mLoadingDialog.show();
		String dataResult = "";
		String baidu_userid = PrefUtility.get(Constants.PREF_BAIDU_USERID, "");
		Log.d(TAG, "-------------------->baidu_userid:" + baidu_userid);
		try {
			JSONObject jsonObj = new JSONObject();
			jsonObj.put("用户名", mUsername+domain);
			jsonObj.put("密码", mPassword);
			
			dataResult = Base64.encode(jsonObj.toString().getBytes());
		} catch (JSONException e1) {
			e1.printStackTrace();
		}

		CampusParameters params = new CampusParameters();
		params.add(Constants.PARAMS_DATA, dataResult);
		final Date dt=new Date();
		CampusAPI.loginCheck(params, new RequestListener() {

			@Override
			public void onIOException(IOException e) {

			}

			@Override
			public void onError(CampusException e) {
				Message msg = new Message();
				msg.what = -1;
				msg.obj = e.getMessage();
				mHandler.sendMessage(msg);
			}

			@Override
			public void onComplete(String response) {
				Log.d(TAG, "----------登录耗时:" + (new Date().getTime()-dt.getTime()));
				Message msg = new Message();
				msg.what = 0;
				msg.obj = response;
				mHandler.sendMessage(msg);
			}
		});
	}

	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {

		public void handleMessage(Message msg) {
			switch (msg.what) {
			case -1:
				mLoadingDialog.dismiss();
				AppUtility.showErrorToast(LoginActivity.this,
						msg.obj.toString());
				break;
			case 0:
				Date dt=new Date();
				String result = msg.obj.toString();
				try {
					result = new String(Base64.decode(result.getBytes("GBK")));
				} catch (UnsupportedEncodingException e1) {
					e1.printStackTrace();
				}
				Log.d(TAG, "--->  " + result);
				try {
					//JSONObject jo = new JSONObject(result);
					//String loginStatus = jo.optString("STATUS");

					net.minidev.json.JSONObject obj=(net.minidev.json.JSONObject)JSONValue.parseStrict(result);
					String loginStatus = String.valueOf(obj.get("STATUS"));
					if (AppUtility.isNotEmpty(loginStatus)) {
						Log.d(TAG, "--->  登录失败！");
						AppUtility.showToastMsg(LoginActivity.this, loginStatus,1);
						if(mLoadingDialog!=null)
							mLoadingDialog.dismiss();
					} else {

						user = new User(obj);
					    
						if (user.getLoginStatus().equals("登录成功")) {
							Log.d(TAG, "--->  登录成功！");
							String checkCode = PrefUtility.get(Constants.PREF_CHECK_CODE, "");// 获取用户校验码
							Log.d(TAG, "-------------checkCode"+ checkCode);
							Log.d(TAG,"--------------->服务器返回校验码："+ user.getCheckCode());
							// 保存用户校验码
							//PrefUtility.put(Constants.PREF_DOMAIN,user.getDomain());
							PrefUtility.put(Constants.PREF_CHECK_CODE,user.getCheckCode());
							PrefUtility.put(Constants.PREF_LOGIN_NAME, mUsername);
                            PrefUtility.put(Constants.PREF_LOGIN_PASS, mPassword);
                            PrefUtility.put(Constants.PREF_LOGIN_REMEBER, cb_remeberPwd.isChecked());
							PrefUtility.put(Constants.PREF_SCHOOL_DOMAIN,user.getDomain());
							PrefUtility.put(Constants.PREF_CHECK_HOSTID,user.getUserNumber());
							PrefUtility.put(Constants.PREF_CHECK_USERTYPE,user.getUserType());
							PrefUtility.put(Constants.PREF_CHECK_USERSTATUS,user.getsStatus());
							checkCode = PrefUtility.get(Constants.PREF_CHECK_CODE, "");// 获取用户校验码
							
							//PrefUtility.putObject("user", user);
							((CampusApplication)getApplicationContext()).setLoginUserObj(user);
							
							Log.d(TAG, "login_-------------checkCode"+ checkCode);
							Log.d(TAG, "login_-------------user.getDomain()"+ user.getDomain());
							
							eqmDao = getHelper().getEqmDao();
							userDao = getHelper().getUserDao();
							userDao.delete(userDao.deleteBuilder().prepare());
							userDao.create(user);
							eqmDao.delete(eqmDao.deleteBuilder().prepare());
							for (Equipment eqm : user.getLoginEquipments()) {
								eqmDao.create(eqm);
							}
							Boolean check_test = PrefUtility.getBoolean(
									Constants.PREF_CHECK_TEST, false);
							if (!check_test && cb_remeberPwd.isChecked()) {
								AccountInfo info = accountInfoDao.queryBuilder().where()
										.eq("userName", mUsername).queryForFirst();
								if (info == null) {
									AccountInfo accountInfo = new AccountInfo();
									long time = new Date().getTime();
									accountInfo.setUserName(mUsername);
									accountInfo.setPassWord(mPassword);
									accountInfo.setLoginTime(time);
									accountInfoDao.create(accountInfo);
								} else {
									long time = new Date().getTime();
									info.setUserName(mUsername);
									info.setPassWord(mPassword);
									info.setLoginTime(time);
									accountInfoDao.update(info);
								}
							}
							
							getInitDataAndContracts();
							/*
							if (Build.VERSION.SDK_INT >= 23)
						    {
								if (ActivityCompat.checkSelfPermission(LoginActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
									AppUtility.beginGPS(getApplication(),user.getUserType());
						    }
							else
								AppUtility.beginGPS(getApplication(),user.getUserType());
							*/
							String baiduUserId=PrefUtility.get(Constants.PREF_BAIDU_USERID, "");
							if(baiduUserId.length()>0)
							{
								InitData initData = new InitData(LoginActivity.this, getHelper(), null,"postBaiDuUserId",checkCode);
								initData.postBaiduUserId();
								PushAgent mPushAgent = PushAgent.getInstance(AppUtility.getContext());
								mPushAgent.setAlias(user.getUserNumber(), "唯一码",

										new UTrack.ICallBack() {

											@Override
											public void onMessage(boolean isSuccess, String message) {
												Log.d("app_setAlias",isSuccess+":"+message);
											}

										});

							}
							CampusAPI.schoolYingXinUrl="http://"+user.getDomain().replace("/appserver/","/NewStudent/mobiles/");

							
						}
						else if(user.getLoginStatus().equals("新生"))
						{
							PrefUtility.put(Constants.PREF_SCHOOL_DOMAIN,user.getDomain());
							CampusAPI.schoolYingXinUrl="http://"+user.getDomain().replace("/appserver/","/NewStudent/mobiles/");
							loginAsNewStudent();
						}
						else {
							// mLoadingDialog.dismiss(); // 关闭登陆提醒
							AppUtility.showToastMsg(LoginActivity.this,
									user.getLoginStatus());
							if(mLoadingDialog!=null)
								mLoadingDialog.dismiss();
						}
						
					}
				} catch (Exception e) {
					if(mLoadingDialog!=null)
						mLoadingDialog.dismiss();
					e.printStackTrace();
				}
				Log.d(TAG, "----------登录处理耗时:" + (new Date().getTime()-dt.getTime()));
				break;
			case 2:
				experienceDialog.dismiss();
				break;
			case 3:
				result = msg.obj.toString();
				try {
					result = new String(Base64.decode(result.getBytes("GBK")));
				} catch (UnsupportedEncodingException e1) {
					e1.printStackTrace();
				}
				Log.d(TAG, "--->  " + result);
				try
				{
					JSONObject jo = new JSONObject(result);
					String loginStatus = jo.optString("结果");

					if (!loginStatus.equals("成功")) {
						AppUtility.showToastMsg(LoginActivity.this, loginStatus,1);
						if(mLoadingDialog!=null)
							mLoadingDialog.dismiss();
					} else
					{
						user = new User(jo.optJSONObject("用户信息"));
						PrefUtility.put(Constants.PREF_LOGIN_NAME, mUsername);
						PrefUtility.put(Constants.PREF_LOGIN_PASS, mPassword);
						PrefUtility.put(Constants.PREF_CHECK_CODE, user.getCheckCode());
						PrefUtility.put(Constants.PREF_CHECK_HOSTID, user.getUserNumber());
						PrefUtility.put(Constants.PREF_CHECK_USERTYPE, user.getUserType());
						PrefUtility.put(Constants.PREF_CHECK_USERSTATUS,user.getsStatus());
						PrefUtility.put(Constants.PREF_INIT_DATA_STR, jo.optJSONObject("用户信息").toString());
						PrefUtility.put(Constants.PREF_INIT_CONTACT_STR, jo.optString("显示字段"));

						((CampusApplication)getApplicationContext()).setLoginUserObj(user);

						getHelper().getEqmDao();
						userDao = getHelper().getUserDao();
						userDao.delete((PreparedDelete<User>) userDao.deleteBuilder().prepare());
						userDao.create(user);

						if (cb_remeberPwd.isChecked()) {
							AccountInfo info = accountInfoDao.queryBuilder().where()
									.eq("userName", mUsername).queryForFirst();
							if (info == null) {
								AccountInfo accountInfo = new AccountInfo();
								long time = new Date().getTime();
								accountInfo.setUserName(mUsername);
								accountInfo.setPassWord(mPassword);
								accountInfo.setLoginTime(time);
								accountInfoDao.create(accountInfo);
							} else {
								long time = new Date().getTime();
								info.setUserName(mUsername);
								info.setPassWord(mPassword);
								info.setLoginTime(time);
								accountInfoDao.update(info);
							}
						}
						String baiduUserId=PrefUtility.get(Constants.PREF_BAIDU_USERID, "");
						if(baiduUserId.length()>0)
						{
							InitData initData = new InitData(LoginActivity.this, getHelper(), null,"postBaiDuUserId",user.getCheckCode());
							initData.postBaiduUserId();
						}

						jumpMain();

					}
				} catch (Exception e) {
					if(mLoadingDialog!=null)
						mLoadingDialog.dismiss();
					e.printStackTrace();
				}
				break;
			}

		}
    };

	/**
	 * 功能描述:跳转到主页
	 * 
	 * @author yanzy 2013-12-30 上午11:34:40
	 * 
	 */
	private void jumpMain() {

		Intent intent = new Intent(this, TabHostActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
		if(mLoadingDialog!=null)
			mLoadingDialog.dismiss();
		this.finish();
	}

	
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (database != null) {
			OpenHelperManager.releaseHelper();
			database = null;
		}
		unregisterReceiver(mBroadcastReceiver);
		
	}

	/**
     */
	private DatabaseHelper getHelper() {
		if (database == null) {
			database = OpenHelperManager.getHelper(this, DatabaseHelper.class);
		}
		return database;
	}

	/**
	 * 功能描述:选择用户类型进入演示
	 * 
	 * @author shengguo 2014-5-10 上午11:05:35
	 * 
	 * @param data
	 */
	private void showUserTypeDialog(String[] data) {
		userTypeDialog = new Dialog(LoginActivity.this, R.style.dialog);
		View view = LayoutInflater.from(getBaseContext()).inflate(
				R.layout.view_exam_login_dialog, null);
		ListView mList = (ListView) view.findViewById(R.id.list);
		DialogAdapter dialogAdapter = new DialogAdapter(data);
		mList.setAdapter(dialogAdapter);
		Window window = userTypeDialog.getWindow();
		window.setGravity(Gravity.BOTTOM);// 在底部弹出
		window.setWindowAnimations(R.style.CustomDialog);
		window.setGravity(Gravity.CENTER);
		userTypeDialog.setContentView(view);
		userTypeDialog.show();
		
	}

	/**
	 * 弹出窗口listview适配器
	 */
	public class DialogAdapter extends BaseAdapter {
		String[] arrayData;

		public DialogAdapter(String[] array) {
			this.arrayData = array;
		}

		@Override
		public int getCount() {
			return arrayData == null ? 0 : arrayData.length;
		}

		@Override
		public Object getItem(int position) {
			return arrayData[position];
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup arg2) {
			ViewHolder holder = null;
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = getLayoutInflater().inflate(
						R.layout.view_testing_pop, null);
				holder.title = (TextView) convertView.findViewById(R.id.time);

				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			final String text = arrayData[position];
			holder.title.setText(text);
			holder.title.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					PrefUtility.put(Constants.PREF_CHECK_TEST, false);
					if ("老师".equals(text)) {
						mUsernameView.setText("0038@dandian.net");
						mPasswordView.setText("0038");
						attemptLogin();
					} else if ("家长".equals(text)) {
						mUsernameView.setText("jz1229641397@dandian.net");
						mPasswordView.setText("123456");
						attemptLogin();
					} else if ("学生".equals(text)) {
						mUsernameView.setText("1229641397@dandian.net");
						mPasswordView.setText("123456");
						attemptLogin();
					}
					userTypeDialog.dismiss();
				}
			});
			return convertView;
		}

	}

	class ViewHolder {
		TextView title;
	}

	class loginHistoryAdapter extends BaseAdapter {
		List<AccountInfo> accountInfoList;
		LayoutInflater inflater;

		@SuppressWarnings("deprecation")
		public loginHistoryAdapter() {
			inflater = LayoutInflater.from(getApplicationContext());
			try {
				accountInfoList = accountInfoDao.queryBuilder()
						.orderBy("loginTime", false).limit(8).query();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		@Override
		public int getCount() {
			return accountInfoList == null ? 0 : accountInfoList.size();
		}

		@Override
		public Object getItem(int position) {
			return accountInfoList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			PopHolder holder = null;
			if (convertView == null) {
				holder = new PopHolder();
				convertView = inflater.inflate(
						R.layout.view_login_poplist_item, null);
				holder.userName = (TextView) convertView
						.findViewById(R.id.account);
				holder.deleteButton = (ImageButton) convertView
						.findViewById(R.id.delete_account);
				convertView.setTag(holder);
			} else {
				holder = (PopHolder) convertView.getTag();
			}

			final AccountInfo info = this.accountInfoList.get(position);
			Log.i(TAG, info.getUserName() + info.getPassWord());
			holder.userName.setText(info.getUserName());
			holder.userName.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					popupWindow.dismiss();
					mUsernameView.setText(info.getUserName());
					mPasswordView.setText(info.getPassWord());
					attemptLogin();
				}
			});
			holder.deleteButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					try {
						accountInfoDao.delete(info);
						accountInfoList.remove(info);
					} catch (SQLException e) {
						e.printStackTrace();
					}
					adapter.notifyDataSetChanged();
					popupWindow.update();
					if (accountInfoList.size() == 0) {
						popupWindow.dismiss();
					}
				}
			});
			return convertView;
		}

	}

	class PopHolder {
		TextView userName;
		ImageButton deleteButton;
	}

	@Override
	public void onDismiss() {
		login_choose.setImageResource(R.drawable.login_btn_bg_nor);
	}

	@Override
	public void onMessage(Intent intent) {
		super.onMessage(intent);  //此方法必须调用，否则无法统计打开数
		String body = intent.getStringExtra(AgooConstants.MESSAGE_BODY);
		Log.i(TAG, body);
	}

	private void loginAsNewStudent()
	{
		String dataResult = "";
		Locale locale = getResources().getConfiguration().locale;
		String language = locale.getCountry();
		try {
			JSONObject jsonObj = new JSONObject();
			jsonObj.put("action", "login");
			jsonObj.put("身份证", mUsername);
			jsonObj.put("密码", mPassword);
			jsonObj.put("language", language);
			jsonObj.put("用户类型","学生");
			dataResult = Base64.encode(jsonObj.toString().getBytes());
		} catch (JSONException e1) {
			e1.printStackTrace();
		}

		CampusParameters params = new CampusParameters();
		params.add(Constants.PARAMS_DATA, dataResult);
		CampusAPI.loginCheckNewStudent(params, new RequestListener() {

			@Override
			public void onIOException(IOException e) {

			}

			@Override
			public void onError(CampusException e) {
				Message msg = new Message();
				msg.what = -1;
				msg.obj = e.getMessage();
				mHandler.sendMessage(msg);
			}

			@Override
			public void onComplete(String response) {

				Message msg = new Message();
				msg.what = 3;
				msg.obj = response;
				mHandler.sendMessage(msg);
			}
		});
	}
}
