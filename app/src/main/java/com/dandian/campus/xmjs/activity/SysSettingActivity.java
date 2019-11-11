package com.dandian.campus.xmjs.activity;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.json.JSONException;
import org.json.JSONObject;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;


import com.j256.ormlite.android.apptools.OpenHelperManager;
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
import com.dandian.campus.xmjs.fragment.ScheduleDialogFragment;
import com.dandian.campus.xmjs.util.AppUtility;
import com.dandian.campus.xmjs.util.Base64;
import com.dandian.campus.xmjs.util.DialogUtility;
import com.dandian.campus.xmjs.util.FileUtility;
import com.dandian.campus.xmjs.util.ImageUtility;
import com.dandian.campus.xmjs.util.PrefUtility;
import com.dandian.campus.xmjs.util.TimeUtility;
import com.dandian.campus.xmjs.util.AppUtility.CallBackInterface;
import com.dandian.campus.xmjs.widget.MyTimePickerDialog;
import com.dandian.campus.xmjs.widget.SwitchButton;

/**
 * 
 * #(c) ruanyun PocketCampus <br/>
 * 
 * 版本说明: $id:$ <br/>
 * 
 * 功能说明: 系统设置
 * 
 * <br/>
 * 创建说明: 2013-12-11 下午5:53:14 zhuliang 创建文件<br/>
 * 
 * 修改历史:<br/>
 * 
 */
public class SysSettingActivity extends FragmentActivity implements
		 OnClickListener {
	private String TAG = "SysSettingActivity";
	private DatabaseHelper database;
	private Dialog mLoadingDialog;
	private final String ACTION_NAME = "alertInitResult";
	private DialogFragment dialogFragment;
	private Button bn_back/*,bn_exit*/;
	//private ImageView count, bn_set1, bn_set2;
	private TextView tv_title/*, bn_title*/, timeInfo,tvVersion,tv_reset_bg,tv_change_bg;
	private ViewGroup /*time, initInfo,*/ schedule, alertTime;
	private String setTime;
	private SwitchButton dayAlert;
	private RelativeLayout versionDetection;
	private RadioGroup weekfirstday;
	private static final int PIC_Select_CODE_ImageFromLoacal = 3;
	
	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case -1:
				if(mLoadingDialog!=null)
					mLoadingDialog.dismiss();
				AppUtility.showErrorToast(SysSettingActivity.this, msg.obj.toString());
				
				break;
			case 0:
				if(mLoadingDialog!=null)
					mLoadingDialog.dismiss();
				
				String result = msg.obj.toString();
				String resultStr = "";
				if (AppUtility.isNotEmpty(result)) {
					try {
						resultStr = new String(Base64.decode(result.getBytes("GBK")));
						Log.d(TAG, "----resultStr:"+resultStr);
						
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
					JSONObject jo = null;
					try {
						jo = new JSONObject(resultStr);
					} catch (JSONException e) {
						e.printStackTrace();
					}
					if(jo!=null){
						String tips = jo.optString("功能更新");
						String downLoadPath = jo.optString("下载地址");
						String newVer=jo.optString("最新版本号");
						//downLoadPath = "http://192.168.1.6:9090/yey/upload/apkfile/69d3cd549afe4cbc2a7ffb1c3257207a.apk";
						if(AppUtility.isNotEmpty(tips)&&AppUtility.isNotEmpty(downLoadPath)){
							showUpdateTips(tips,downLoadPath,newVer);
						}
						else
							AppUtility.showToastMsg(SysSettingActivity.this,"当前已是最新版");
					}
					else
						AppUtility.showToastMsg(SysSettingActivity.this,"获取版本失败");
				}
				break;
			}
		}
	};
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle(R.string.settings_option);

		setContentView(R.layout.activity_setting);
		ExitApplication.getInstance().addActivity(this);
		initView();
		initContent();
		listener();

	}
	@Override
	public void onDestroy()
	{
		AppUtility.beginReminder(this);
		super.onDestroy();
	}
	private void initView() {
		bn_back = (Button) findViewById(R.id.back);
		tv_title = (TextView) findViewById(R.id.setting_tv_title);
		tv_title.setText("系统设置");
		dialogFragment = ScheduleDialogFragment.newInstance("上课时间表");
		//CampusApplication.getVersion();
		tvVersion = (TextView) findViewById(R.id.tv_app_version);
		tvVersion.setText(CampusApplication.getVersion());
		TextView tv_cur_xueqi= (TextView) findViewById(R.id.tv_cur_xueqi);
		tv_cur_xueqi.setText(PrefUtility.get(Constants.PREF_CUR_XUEQI,""));
	}

	private void initContent() {
		
		dayAlert = (SwitchButton) findViewById(R.id.settings_dayalert);
		schedule = (ViewGroup) findViewById(R.id.schedule);
		alertTime = (ViewGroup) findViewById(R.id.alerttime);
		timeInfo = (TextView) findViewById(R.id.time);
		//String time = dateFormat(Calendar.HOUR, Calendar.MINUTE);
		String str = PrefUtility.get("remindClassTime", "前一天 20:00");
		if(str != null && !str.trim().equals("")){
			timeInfo.setText(str);
		}else{
			timeInfo.setText("前一天 20:00");
		}
		
		dayAlert.setChecked(PrefUtility.getBoolean("booleanReminderDayClass", true));
		//commonQuestions = (RelativeLayout) findViewById(R.id.common_questions);
		versionDetection = (RelativeLayout) findViewById(R.id.version_detection);
		weekfirstday=(RadioGroup)findViewById(R.id.weekfirstday);
		tv_reset_bg=(TextView)findViewById(R.id.tv_reset_bg);
		tv_change_bg=(TextView)findViewById(R.id.tv_select_bg);
		RadioButton rdbtn=null;
		if(PrefUtility.getInt("weekFirstDay", 1)==0)
			rdbtn=(RadioButton)weekfirstday.getChildAt(0);
		else
			rdbtn=(RadioButton)weekfirstday.getChildAt(1);
		rdbtn.setChecked(true);
	}

	private void listener() {
		bn_back.setOnClickListener(this);
		schedule.setOnClickListener(this);	
		alertTime.setOnClickListener(this);
		//commonQuestions.setOnClickListener(this);
		versionDetection.setOnClickListener(this);
		
//		bn_exit.setOnClickListener(this);
		doReminder();
		
		weekfirstday.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group,
					int checkedId) {
				if(checkedId==R.id.radio_sunday)
					PrefUtility.put("weekFirstDay", 0);
				else
					PrefUtility.put("weekFirstDay", 1);
				String checkCode = PrefUtility.get(Constants.PREF_CHECK_CODE,"");
				InitData initData = new InitData(SysSettingActivity.this,
						getHelper(), mLoadingDialog, "xmjs_refreshSubject", checkCode);
				initData.initAllInfo();
			}
		});
		tv_reset_bg.setOnClickListener(this);
		tv_change_bg.setOnClickListener(this);
	}

	private void doReminder(){
		
		dayAlert.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				PrefUtility.put("booleanReminderDayClass", isChecked);
			}
		});
		
	}
	
	private MyTimePickerDialog.OnTimeSetListener mTimeSetListener =
	         new MyTimePickerDialog.OnTimeSetListener()
	{
	         public void onTimeSet(TimePicker view,String radioSel, int hourOfDay, int minute)
	         {
	        	 	setTime = dateFormat(hourOfDay, minute);
	     			timeInfo.setText(radioSel+" "+setTime);
	     			PrefUtility.put("remindClassTime", radioSel+" "+setTime);
	         }
	        
	};
	
	@SuppressWarnings({ "deprecation" })
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.back:
			finish();
			break;
		case R.id.schedule:
			if(!dialogFragment.isAdded()){
				dialogFragment.show(getSupportFragmentManager(), "schedule");
			}	
			break;
		case R.id.alerttime:
			if (dayAlert.isChecked()) {
				String str=(String)timeInfo.getText();
				String[] arr=str.split(" ");
				String radioSel=arr[0];
				String[] timearr=arr[1].split(":");
				int hour=Integer.parseInt(timearr[0]);
				int minute=Integer.parseInt(timearr[1]);
				MyTimePickerDialog dialog = new MyTimePickerDialog(this, mTimeSetListener,
						radioSel,hour, minute, true);
				dialog.setButton2("取消", (DialogInterface.OnClickListener)null);
				dialog.show();
			} else {
			}
			break;
		

		/*
		case R.id.common_questions:
			Intent questionsIntent = new Intent(SysSettingActivity.this,
					WebSiteActivity.class);
			questionsIntent.putExtra("url", CampusAPI.commonQuestionUrl);
			questionsIntent.putExtra("title", "常见问题");
			startActivity(questionsIntent);
			break;
		*/
		case R.id.version_detection:
			versionDetection();
			break;
		case R.id.tv_reset_bg:
			PrefUtility.put("scheduleBg", "default");
			Intent intent=new Intent("changeScheduleBg");
			sendBroadcast(intent);
			
			break;
		case R.id.tv_select_bg:
			if (Build.VERSION.SDK_INT >= 23) 
			{
				if(AppUtility.checkPermission(SysSettingActivity.this,7,Manifest.permission.READ_EXTERNAL_STORAGE))
					getPictureFromLocation();
			}
			else
				getPictureFromLocation();
			break;
		default:
			break;
		}
	}
	public void getPictureFromLocation() {
		String status = Environment.getExternalStorageState();
		if (status.equals(Environment.MEDIA_MOUNTED)) {// 判断是否有SD卡
			/*
			Intent intent = new Intent();
			intent.setType("image/*");
			intent.setAction(Intent.ACTION_GET_CONTENT);
			startActivityForResult(intent, REQUEST_CODE_TAKE_PICTURE);
			*/
			Intent intent; 
			intent = new Intent(Intent.ACTION_PICK, 
			                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI); 
			startActivityForResult(intent, PIC_Select_CODE_ImageFromLoacal);
			
		} else {
			AppUtility.showToastMsg(this, "SD卡不可用");
		}
	}
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == PIC_Select_CODE_ImageFromLoacal) {
			
			if(data==null) return;
			Uri uri = data.getData();
			String img_path=FileUtility.getFilePathInSD(this,uri);
			/*
			String[] pojo  = { MediaStore.Images.Media.DATA };
			CursorLoader cursorLoader = new CursorLoader(this, uri, pojo, null,null, null); 
			Cursor cursor = cursorLoader.loadInBackground();
			cursor.moveToFirst(); 
			String img_path = cursor.getString(cursor.getColumnIndex(pojo[0]));
			*/ 
			//String extName=FileUtility.getFileExtName(img_path);
			//String tempPath =FileUtility.getRandomSDFileName(FileUtility.SDPATH,extName);
			String tempPath =FileUtility.getCacheDir()+FileUtility.getFileRealName(img_path);
			FileUtility.copyFile(img_path,tempPath);
			ImageUtility.rotatingImageIfNeed(tempPath);
			PrefUtility.put("scheduleBg", tempPath);
			Intent intent=new Intent("changeScheduleBg");
			sendBroadcast(intent);
			
		}
	}
	/**
	 * 功能描述:版本检测
	 *
	 * @author shengguo  2014-6-3 下午4:05:05
	 *
	 */
	
	private void versionDetection() {
		mLoadingDialog = DialogUtility.createLoadingDialog(this, "正在检测新版本...");
		mLoadingDialog.show();
		String thisVersion = CampusApplication.getVersion();
		String check=PrefUtility.get(Constants.PREF_CHECK_CODE, "");
		long datatime = System.currentTimeMillis();
		String base64Str = null;
		try {
			JSONObject jsonObj = new JSONObject();
			jsonObj.put("当前版本号", thisVersion);
			jsonObj.put("用户较验码", check);
			jsonObj.put("DATETIME", datatime);

			base64Str = Base64.encode(jsonObj.toString().getBytes());
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		Log.d(TAG, "---------------->base64Str:" + base64Str);
		CampusParameters params = new CampusParameters();
		params.add(Constants.PARAMS_DATA, base64Str);
		CampusAPI.versionDetection(params, new RequestListener() {
			
			@Override
			public void onIOException(IOException e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onError(CampusException e) {
				Log.d(TAG, "----response"+e.getMessage());
				Message msg=new Message();
				msg.what = -1;
				msg.obj= e.getMessage();
				mHandler.sendMessage(msg);
			}
			
			@Override
			public void onComplete(String response) {
				Log.d(TAG, "----response"+response);
				
				Message msg=new Message();
				msg.what = 0;
				msg.obj= response;
				mHandler.sendMessage(msg);
			}
		});
	}
	
	/**
	 * 功能描述:询问是否更新
	 *
	 * @author shengguo  2014-6-3 下午4:31:55
	 *
	 */
	private void showUpdateTips(String tips,final String downLoadPath,String newVer) {
		View view = LayoutInflater.from(SysSettingActivity.this).inflate(
				R.layout.view_textview, null);
		TextView tvTip = (TextView) view.findViewById(R.id.tv_text);
		tvTip.setText(tips);
		AlertDialog dialog_UpdateTips = new AlertDialog.Builder(SysSettingActivity.this)
				.setView(view)
				.setTitle(newVer+"版更新提示")
				.setPositiveButton("下载更新", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						Log.d(TAG, "-------------downLoadPath:" + downLoadPath);
						AppUtility.downloadUrl(downLoadPath, null, SysSettingActivity.this);
						
						dialog.dismiss();
					}
				})
				.setNegativeButton("取消", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				}).create();
		dialog_UpdateTips.show();
	}
	

	/**
	 * 功能描述:格式化时间
	 * 
	 * @author zhuliang 2013-12-11 下午5:53:46
	 * 
	 * @param hour
	 * @param minute
	 * @return
	 */
	@SuppressLint("SimpleDateFormat")
	private String dateFormat(int hour, int minute) {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, hour);
		calendar.set(Calendar.MINUTE, minute);
		SimpleDateFormat df = new SimpleDateFormat("HH:mm");
		String time = df.format(calendar.getTime());
		return time;
	}

	private DatabaseHelper getHelper() {
		if (database == null) {
			database = OpenHelperManager.getHelper(this, DatabaseHelper.class);
		}
		return database;
	}

	private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(ACTION_NAME)) {
				System.out.println("----------->BroadcastReceiver："
						+ ACTION_NAME);
				AlertDialog.Builder builder = new AlertDialog.Builder(context);
				builder.setTitle("信息提示");
				builder.setMessage("数据初始化完毕！");
				builder.setNegativeButton("关闭", new closeStudentPicListener());
				AlertDialog ad = builder.create();
				ad.show();
				//注销广播
                unregisterReceiver(mBroadcastReceiver);
			}
		}

	};

	public void registerBoradcastReceiver() {
		IntentFilter myIntentFilter = new IntentFilter();
		myIntentFilter.addAction(ACTION_NAME);
		// 注册广播
		registerReceiver(mBroadcastReceiver, myIntentFilter);
	}

	/**
	 * 功能描述:加载联系人头像
	 * 
	 * @author yanzy 2013-12-11 下午6:03:00
	 * 
	 */
	public void initStudentPic() {
		InitData initData = new InitData(SysSettingActivity.this, getHelper(),
				mLoadingDialog, ACTION_NAME,null);
		initData.initStudentPic();
	}

	private class closeStudentPicListener implements
			DialogInterface.OnClickListener {

		@Override
		public void onClick(DialogInterface dialog, int which) {
			dialog.dismiss();
		}
	}
	@TargetApi(23)
	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
	{
		AppUtility.permissionResult(requestCode,grantResults,this,callBack);
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
	}
	public CallBackInterface callBack=new CallBackInterface()
	{

		@Override
		public void getLocation1(int rqcode) {
			// TODO Auto-generated method stub
		
		}

		@Override
		public void getPictureByCamera1(int rqcode) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void getPictureFromLocation1() {
			// TODO Auto-generated method stub
			getPictureFromLocation();
		}

		@Override
		public void sendCall1() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void sendMsg1() {
			// TODO Auto-generated method stub
			
		}
		@Override
		public void getFujian1() {
			// TODO Auto-generated method stub
		}
		
	};
}
