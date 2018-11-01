package com.dandian.campus.xmjs.activity;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TabActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;
import android.widget.Toolbar;

import com.dandian.campus.xmjs.util.TimeUtility;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.dandian.campus.xmjs.CampusApplication;
import com.dandian.campus.xmjs.R;
import com.dandian.campus.xmjs.api.CampusAPI;
import com.dandian.campus.xmjs.api.CampusException;
import com.dandian.campus.xmjs.api.CampusParameters;
import com.dandian.campus.xmjs.api.RequestListener;
import com.dandian.campus.xmjs.base.Constants;
import com.dandian.campus.xmjs.db.DatabaseHelper;
import com.dandian.campus.xmjs.db.InitData;
import com.dandian.campus.xmjs.entity.AlbumMsgInfo;
import com.dandian.campus.xmjs.entity.ChatFriend;
import com.dandian.campus.xmjs.entity.Notice;
import com.dandian.campus.xmjs.entity.User;
import com.dandian.campus.xmjs.lib.SlidingMenu;
import com.dandian.campus.xmjs.service.Alarmreceiver;
import com.dandian.campus.xmjs.util.AppUtility;
import com.dandian.campus.xmjs.util.AppUtility.CallBackInterface;
import com.dandian.campus.xmjs.util.BaiduPushUtility;
import com.dandian.campus.xmjs.util.Base64;
import com.dandian.campus.xmjs.util.DateHelper;
import com.dandian.campus.xmjs.util.DialogUtility;
import com.dandian.campus.xmjs.util.FileUtility;
import com.dandian.campus.xmjs.util.PrefUtility;
import com.dandian.campus.xmjs.widget.BottomTabLayout;
import com.dandian.campus.xmjs.widget.BottomTabLayout.OnCheckedChangeListener;
import com.umeng.analytics.MobclickAgent;
import com.umeng.message.PushAgent;

import static anet.channel.util.Utils.context;

@SuppressWarnings("deprecation")
public class TabHostActivity extends TabActivity   {
	private String TAG = "TabHostActivity";
	public SlidingMenu menu;
	private BottomTabLayout mainTab;
	private TabHost tabHost;
	private Intent workIntent;
	private Intent messageIntent;
	private Intent communicationIntent;
	private Intent schoolIntent;
	private Intent albumIntent;
	private Intent myStatusIntent,submitDataIntent;
	private TextView pageTime, pageName, departmentOrClassName;
	private ImageView pagePhoto;
	private Button pageMyInfo, pageMyAlbum,/* pageRecommend, */pageSetting,pageChangepwd,pageAboutus, pageClearCache,
			pageFeedback, pageExit;
	private Dao<User, Integer> userDao;
	private Dao<ChatFriend,Integer> chatFriendDao;
	private Dao<AlbumMsgInfo,Integer> albumMsgDao;
	private List<ChatFriend> chatFriendList;
	private User userInfo;
	private final static String TAB_TAG_WORK = "tab_tag_work";
	private final static String TAB_TAG_MESSAGE = "tab_tag_message";
	private final static String TAB_TAG_COMMUNICATION = "tab_tag_communication";
	// private final static String TAB_TAG_SUMMARY = "tab_tag_summary";
	private final static String TAB_TAG_SCHOOL = "tab_tag_school";
	private final static String TAB_TAG_ALBUM = "tab_tag_album";
	private final static String TAB_TAG_MYSELF = "tab_tag_mystatus";
	private final static String TAB_TAG_FINISH = "tab_tag_finish";
	private Dao<Notice, Integer> noticeInfoDao;
	// public static int currentWeek = 0,selectedWeek = 0,maxWeek =
	// 0;//当前周次,选择周次,选择周次
	DatabaseHelper database;
	private Dialog clearCacheDialog;
	private final String ACTION_NAME_REMIND = "remindSubject";
	private final String ACTION_CHATINTERACT =  "ChatInteract";
	private final String ACTION_CHANGEHEAD =  "ChangeHead";
	
	public final String STitle = "showmsg_title";
	public final String SMessage = "showmsg_message";
	public final String BAThumbData = "showmsg_thumb_data";
	private User user;
	private boolean isIntoBack=false;
	public static MenuListener menuListener;
	public static DisplayImageOptions headOptions;
	private String downloadUrl;
	private File downloadFile;
	public CallBackInterface callBack;

	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case -1:
				AppUtility.showErrorToast(TabHostActivity.this, msg.obj.toString());
				break;
			
			case 1:
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
						if(AppUtility.isNotEmpty(tips)&&AppUtility.isNotEmpty(downLoadPath)){
							showUpdateTips(tips,downLoadPath,newVer);
						}
					}
				}
				break;
			case 2:
				result = msg.obj.toString();
				resultStr = "";
				if (AppUtility.isNotEmpty(result)) {
					try {
						resultStr = new String(Base64.decode(result.getBytes("GBK")));
						Log.d(TAG, "----resultStr:"+resultStr);
						
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
					JSONObject jo = null;
					try 
					{
						albumMsgDao=database.getAlbumMsgDao();
						jo = new JSONObject(resultStr);
						if(jo!=null)
						{
							JSONArray ja=jo.getJSONArray("result");
							int unreadCount = ja.length();
							if(unreadCount!=0){
								
								try {
									
									for(int i=0;i<ja.length();i++)
									{
										JSONObject item=ja.getJSONObject(i);
										AlbumMsgInfo u=new AlbumMsgInfo(item);
										if(u.getImageObject()!=null && !u.getImageObject().equals("null") && u.getImageObject().length()>0)
											albumMsgDao.create(u);
									}
								} catch (SQLException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								Intent intentChat = new Intent("hasUnreadAlbumMsg");
								TabHostActivity.this.sendBroadcast(intentChat);
							}
						
						}
						updateUnreadCount();
						
					} catch (JSONException e) {
						e.printStackTrace();
					}
					catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				break;
			default:
				break;
			}
		}
	};
	/*
	private ServiceConnection connection = new ServiceConnection() {
		public void onServiceDisconnected(ComponentName name) {
			schoolService = null;
			Log.d(TAG, "Client ->Disconnected the LocalService");
		}

		public void onServiceConnected(ComponentName name, IBinder binder) {
			// 获取连接的服务对象
			schoolService = ((MyIBinder) binder).getService();
			Log.d(TAG, "Client ->Connected the Service");
		}
	};
	*/
	private void iniImageLoader()
	{
        headOptions =
                new DisplayImageOptions.Builder()
                        .cacheOnDisc(true)//图片存本地
                        .cacheInMemory(false)
                        .showImageOnFail(R.drawable.ic_launcher)
                                //.displayer(new FadeInBitmapDisplayer(50))
                        .displayer(new RoundedBitmapDisplayer(45))
                        .bitmapConfig(Bitmap.Config.RGB_565)
                        .imageScaleType(ImageScaleType.EXACTLY)
                        .build();
		//初始化图片加载库
				DisplayImageOptions defaultOptions =
					        new DisplayImageOptions.Builder()
					            .cacheOnDisc(true)//图片存本地
					            .cacheInMemory(false)
					            .showImageOnFail(R.drawable.empty_photo)
					            //.displayer(new FadeInBitmapDisplayer(50))
					           // .decodingOptions(decodingOptions)
					            .bitmapConfig(Bitmap.Config.RGB_565)
					            .imageScaleType(ImageScaleType.EXACTLY ) // default
					            .build();
				
				//DisplayImageOptions defaultOptions = DisplayImageOptions.createSimple();
					    ImageLoaderConfiguration config =
					        new ImageLoaderConfiguration.Builder(getApplicationContext())
					    
					    //.memoryCacheExtraOptions(480, 800) // max width, max height，即保存的每个缓存文件的最大长宽  
					    //.discCacheExtraOptions(480, 800, CompressFormat.JPEG, 75, null) 
					    //.threadPriority(Thread.NORM_PRIORITY - 2)  
		                //.denyCacheImageMultipleSizesInMemory()  
		                //.memoryCache(new FIFOLimitedMemoryCache(2 * 1024 * 1024))
		                //.memoryCacheSize(2 * 1024 * 1024)    
		                //.discCacheSize(50 * 1024 * 1024) 
		                .defaultDisplayImageOptions(defaultOptions).build();
					    /*
					            .memoryCache(new UsingFreqLimitedMemoryCache(2 * 1024 * 1024))
					            .memoryCacheSize(2 * 1024 * 1024)  
					            .memoryCacheSizePercentage(13) // default  
					            .denyCacheImageMultipleSizesInMemory()
					     
					            .defaultDisplayImageOptions(defaultOptions).build();
					      */ 
					    ImageLoader.getInstance().init(config);
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);


		PushAgent.getInstance(context).onAppStart();
		
		iniImageLoader();
		/*
		//判断用户是否第一次运行
		boolean checkrun = PrefUtility.getBoolean(Constants.PREF_CHECK_RUN,
		 false);
		 if (!checkrun) {
			 Intent intent = new Intent(this, ExperienceActivity.class);
			 startActivity(intent);
			 finish();
			 return;
		 } 
		*/
		user=((CampusApplication)getApplicationContext()).getLoginUserObjAllowNull();
		String checkCode=PrefUtility.get(Constants.PREF_CHECK_CODE, "");
		String userNumber=PrefUtility.get(Constants.PREF_CHECK_HOSTID, "");
		if(user==null || checkCode.length()==0 || userNumber.length()==0 || (!user.getsStatus().equals("新生状态") && ((CampusApplication)getApplicationContext()).getLinkManDic()==null))
		{
            Intent intent = new Intent(this,
                    LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
			return;
		}

		/*
		String contentText = getIntent().getStringExtra("contentText");
		if (AppUtility.isNotEmpty(contentText)) {
			showDialog(contentText);
		}
		*/

		setContentView(R.layout.activity_tabhost);

		mainTab = (BottomTabLayout) findViewById(R.id.bottom_tab_layout);
		mainTab.setOnCheckedChangeListener(changeListener);

		menuListener=new MenuListener();
		showMenu();
        prepareIntent();
        setupIntent();

		clearCacheDialog = new Dialog(TabHostActivity.this, R.style.dialog);
		//Intent intent = new Intent(TabHostActivity.this,SchoolService.class);
		//bindService(intent, connection, Context.BIND_AUTO_CREATE);
		
		//showUnreadCnt();
		
		//UpdateManager.checkUpdate(this);
		//版本检测
		versionDetection();
			
		//regToWx(); // 注册微信
		registerBoradcastReceiver();
		
		String toTag = getIntent().getStringExtra("tab");
		if(toTag==null)
			findView();
		else if(toTag.equals("2"))
		{
			tabHost.setCurrentTabByTag(TAB_TAG_MESSAGE);
			View nearBtn = mainTab.findViewById(R.id.bottom_tab_message);
			nearBtn.setSelected(true);
			
		}
		else if(toTag.equals("1"))
		{
			tabHost.setCurrentTabByTag(TAB_TAG_WORK);
			View nearBtn = mainTab.findViewById(R.id.bottom_tab_work);
			nearBtn.setSelected(true);
		}
		Intent intent = new Intent(AppUtility.getContext(), Alarmreceiver.class);
		intent.setAction("getMsgList");
		sendBroadcast(intent);
		getAlbumUnreadCount();
		Log.d(TAG,"生命周期:onCreate");

	}

	@Override
	protected void onStart() {
		super.onStart();
		showUnreadCnt();
		updateUnreadCount();
		if(isIntoBack)
		{
			isIntoBack=false;
			//Intent intentChat = new Intent("Campus_reloadNotice");
			//TabHostActivity.this.sendBroadcast(intentChat);
			//getNetLocation();
            Intent intent = new Intent(AppUtility.getContext(), Alarmreceiver.class);
            intent.setAction("getMsgList");
            sendBroadcast(intent);
			getAlbumUnreadCount();
		}
		
		//上次登录时间并非当前周则重新获取课表
		int week1=DateHelper.getWeekIndexOfYear(DateHelper.getStringDate(user.getLoginTime(), ""));
		int week2=DateHelper.getWeekIndexOfYear(new Date());
		if(week2>week1)
		{
			PrefUtility.put(Constants.PREF_SELECTED_WEEK, 0);
			String checkCode = PrefUtility.get(Constants.PREF_CHECK_CODE, "");
			InitData initData = new InitData(TabHostActivity.this,getHelper(), null,"xmjs_refreshSubject",checkCode);
			initData.initAllInfo();
		}
		Log.d(TAG,"生命周期:onStart");
	}

	@Override
	protected void onStop() {
		super.onStop();
	
		if(AppUtility.isApplicationBroughtToBackground(this))
			isIntoBack=true;
		Log.d(TAG,"生命周期:onStop");
	}
	@Override
	public void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}
	@Override
	public void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}
	public void showMenu() {
		menu = new SlidingMenu(this);
		menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
		menu.setShadowWidthRes(R.dimen.shadow_width);
		menu.setShadowDrawable(R.drawable.shadow);
		menu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
		menu.setFadeDegree(0.35f);
		menu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
		LayoutInflater inflater = LayoutInflater.from(TabHostActivity.this);
		View localView = inflater.inflate(R.layout.view_page_menu, null);
		initMenu(localView);
		menu.setMenu(localView);
		
		menu.showContent();
	}

	/**
	 * 功能描述: 对menu的操作
	 * 
	 * @author zhuliang 2013-12-5 下午4:55:16
	 * 
	 * @param view
	 */
	private void initMenu(View view) {

		pageTime = (TextView) view.findViewById(R.id.page_time);
		pagePhoto = (ImageView) view.findViewById(R.id.page_photo);
		pageName = (TextView) view.findViewById(R.id.page_name);
		departmentOrClassName = (TextView) view
				.findViewById(R.id.department_or_class_name);
		pageMyInfo = (Button) view.findViewById(R.id.page_myinfo);
		pageMyAlbum= (Button) view.findViewById(R.id.page_myalbum);
		pageSetting = (Button) view.findViewById(R.id.page_setting);
		pageChangepwd = (Button) view.findViewById(R.id.page_changepwd);
		pageAboutus = (Button) view.findViewById(R.id.page_aboutus);
		pageClearCache = (Button) view.findViewById(R.id.page_clear_cache);
		pageFeedback = (Button) view.findViewById(R.id.page_feedback);
		pageExit = (Button) view.findViewById(R.id.page_exit);
		pageTime.setText(AppUtility.getWeekAndDate(new Date()));

		try {
			

			String photoUrl = user.getUserImage();
            /*
			ImageOptions options = new ImageOptions();
			Bitmap bm=aq.getCachedImage(photoUrl);
			if(bm!=null)
			{
				options.preset=bm;
				options.round=bm.getHeight()/2;
			}
			else
			{
				options.memCache=false;
				options.targetWidth=100;
				options.round = 50;
				
			}
			aq.id(pagePhoto).image(photoUrl, options);
			*/
            ImageLoader.getInstance().displayImage(photoUrl,pagePhoto,headOptions);
			userDao = getHelper().getUserDao();
			chatFriendDao = getHelper().getChatFriendDao();
			noticeInfoDao = getHelper().getNoticeInfoDao();
			String number = user.getId();
			userInfo = userDao.queryBuilder().where().eq("id", number)
					.queryForFirst();
			if (userInfo != null) {
				String userType = userInfo.getUserType();
				if (userType.equals("老师")) {
					pageName.setText("您好," + userInfo.getName() + "("
							+ userType + ")");
					departmentOrClassName.setText(userInfo.getDepartment());
				} else {
					pageName.setText("您好," + userInfo.getName().replace("[家长]", "") + "("
							+ userType + ")");
					departmentOrClassName.setText(userInfo.getsClass());
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		pagePhoto.setOnClickListener(new MenuInfoListener());
		pageSetting.setOnClickListener(new MenuInfoListener());
		pageChangepwd.setOnClickListener(new MenuInfoListener());
		pageMyInfo.setOnClickListener(new MenuInfoListener());
		pageClearCache.setOnClickListener(new MenuInfoListener());
		pageFeedback.setOnClickListener(new MenuInfoListener());
		pageAboutus.setOnClickListener(new MenuInfoListener());
		pageExit.setOnClickListener(new MenuInfoListener());
		pageMyAlbum.setOnClickListener(new MenuInfoListener());
	}

	/**
	 * 准备tab的内容Intent
	 */
	private void prepareIntent() {
		if (user.getsStatus().equals("新生状态")) {
			myStatusIntent = new Intent(this, MyStatusActivity.class);
			schoolIntent = new Intent(this, TabSchoolActivtiy.class);
			submitDataIntent = new Intent(this, SubmitDataActivity.class);
			messageIntent = new Intent(this, ChatFriendActivity.class);
			albumIntent = new Intent(this, AlbumFlowActivity.class);
		}
		else if(user.getsStatus().equals("迎新管理员") || user.getsStatus().equals("班主任"))
		{
			myStatusIntent = new Intent(this, MyStatusActivity.class);
			schoolIntent = new Intent(this, TabSchoolActivtiy.class);
			workIntent = new Intent(this, SubjectActivity.class);
			messageIntent = new Intent(this, ChatFriendActivity.class);
			communicationIntent = new Intent(this, ContactsActivity.class);
		}
		else {
			workIntent = new Intent(this, SubjectActivity.class);
			messageIntent = new Intent(this, ChatFriendActivity.class);
			communicationIntent = new Intent(this, ContactsActivity.class);
			// summaryIntent = new Intent(this, SummaryActivity.class);
			schoolIntent = new Intent(this, TabSchoolActivtiy.class);
			albumIntent = new Intent(this, AlbumFlowActivity.class);
		}
		
		
	}

	private void setupIntent() {
		this.tabHost = getTabHost();
		TabHost localTabHost = this.tabHost;
		FrameLayout bottom_tab_myself=(FrameLayout)findViewById(R.id.bottom_tab_myself);
		FrameLayout bottom_tab_finish=(FrameLayout)findViewById(R.id.bottom_tab_finish);
		FrameLayout bottom_tab_work=(FrameLayout)findViewById(R.id.bottom_tab_work);
		FrameLayout bottom_tab_communication=(FrameLayout)findViewById(R.id.bottom_tab_communication);
		FrameLayout bottom_tab_album=(FrameLayout)findViewById(R.id.bottom_tab_album);
		if (user.getsStatus().equals("新生状态"))
		{
			bottom_tab_work.setVisibility(View.GONE);
			bottom_tab_communication.setVisibility(View.GONE);
			localTabHost.addTab(buildTabSpec(TAB_TAG_SCHOOL, R.string.school,
					R.drawable.ic_launcher, schoolIntent));
			localTabHost.addTab(buildTabSpec(TAB_TAG_MYSELF, R.string.mystatus,
					R.drawable.ic_launcher, myStatusIntent));
			localTabHost.addTab(buildTabSpec(TAB_TAG_FINISH,
					R.string.curriculum, R.drawable.ic_launcher,submitDataIntent));
			localTabHost.addTab(buildTabSpec(TAB_TAG_MESSAGE, R.string.message,
					R.drawable.ic_launcher, messageIntent));
			localTabHost.addTab(buildTabSpec(TAB_TAG_ALBUM,	R.string.album, R.drawable.ic_launcher,
					albumIntent));
		}
		else if(user.getsStatus().equals("迎新管理员") || user.getsStatus().equals("班主任"))
		{
			bottom_tab_album.setVisibility(View.GONE);
			bottom_tab_finish.setVisibility(View.GONE);
			localTabHost.addTab(buildTabSpec(TAB_TAG_SCHOOL, R.string.school,
					R.drawable.ic_launcher, schoolIntent));
			localTabHost.addTab(buildTabSpec(TAB_TAG_MYSELF, R.string.mystatus,
					R.drawable.ic_launcher, myStatusIntent));
			localTabHost.addTab(buildTabSpec(TAB_TAG_WORK, R.string.study,
					R.drawable.ic_launcher, workIntent));
			localTabHost.addTab(buildTabSpec(TAB_TAG_MESSAGE, R.string.message,
					R.drawable.ic_launcher, messageIntent));
			localTabHost.addTab(buildTabSpec(TAB_TAG_COMMUNICATION,
					R.string.curriculum, R.drawable.ic_launcher,
					communicationIntent));
		}
		else {
			bottom_tab_myself.setVisibility(View.GONE);
			bottom_tab_finish.setVisibility(View.GONE);
			localTabHost.addTab(buildTabSpec(TAB_TAG_SCHOOL, R.string.school,
					R.drawable.ic_launcher, schoolIntent));
			localTabHost.addTab(buildTabSpec(TAB_TAG_WORK, R.string.study,
					R.drawable.ic_launcher, workIntent));
			localTabHost.addTab(buildTabSpec(TAB_TAG_MESSAGE, R.string.message,
					R.drawable.ic_launcher, messageIntent));
			localTabHost.addTab(buildTabSpec(TAB_TAG_COMMUNICATION,
					R.string.curriculum, R.drawable.ic_launcher,
					communicationIntent));
			localTabHost.addTab(buildTabSpec(TAB_TAG_ALBUM,
					R.string.album, R.drawable.ic_launcher,
					albumIntent));
		}
		// localTabHost.addTab(buildTabSpec(TAB_TAG_SUMMARY, R.string.summary,
		// R.drawable.ic_launcher, summaryIntent));

	}

	/**
	 * 构建TabHost的Tab页
	 * 
	 * @param tag
	 *            标记
	 * @param resLabel
	 *            标签
	 * @param resIcon
	 *            图标
	 * @param content
	 *            该tab展示的内容
	 * @return 一个tab
	 */
	private TabSpec buildTabSpec(String tag, int resLabel, int resIcon,
			final Intent content) {
		return this.tabHost
				.newTabSpec(tag)
				.setIndicator(getString(resLabel),
						getResources().getDrawable(resIcon))
				.setContent(content);
	}

	// 设置默认选中项
	private void findView() {
		View nearBtn = mainTab.findViewById(R.id.bottom_tab_school);
		nearBtn.setSelected(true);
	}

	OnCheckedChangeListener changeListener = new OnCheckedChangeListener() {
		@Override
		public void OnCheckedChange(View checkview) {
			switch (checkview.getId()) {
			case R.id.bottom_tab_myself:
					tabHost.setCurrentTabByTag(TAB_TAG_MYSELF);
					break;
			case R.id.bottom_tab_work:
				tabHost.setCurrentTabByTag(TAB_TAG_WORK);
				
				break;
			case R.id.bottom_tab_message:
				tabHost.setCurrentTabByTag(TAB_TAG_MESSAGE);
				
				break;
			case R.id.bottom_tab_communication:
				tabHost.setCurrentTabByTag(TAB_TAG_COMMUNICATION);
				
				break;
			/*
			 * case R.id.bottom_tab_summary:
			 * tabHost.setCurrentTabByTag(TAB_TAG_SUMMARY);
			 * SummaryActivity.layout_menu.setOnClickListener(new
			 * MenuListener()); break;
			 */
			case R.id.bottom_tab_school:
				tabHost.setCurrentTabByTag(TAB_TAG_SCHOOL);
				
				break;
			case R.id.bottom_tab_finish:
				tabHost.setCurrentTabByTag(TAB_TAG_FINISH);

				break;
			case R.id.bottom_tab_album:
				tabHost.setCurrentTabByTag(TAB_TAG_ALBUM);
				
				break;
			
			}
			
		}

		@Override
		public void OnCheckedClick(View checkview) {

		}
	};

	/**
	 * 对tab上的四个activity上的menu进行监听 by zhuliang
	 */
	class MenuListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			if(user.getsStatus().equals("新生状态"))
				((CampusApplication)getApplicationContext()).reLogin_newStudent();
			else
				menu.toggle();
		}
	}

	/**
	 * 对menu界面的监听
	 */
	class MenuInfoListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.page_photo:
				String photoUrl = user.getUserImage();
				if (AppUtility.isNotEmpty(photoUrl)) {
					DialogUtility.showImageDialog(TabHostActivity.this,photoUrl);
					//showImageDialog(photoUrl);
				}
				break;
			case R.id.page_setting:
				Intent settingIntent = new Intent(getApplicationContext(),
						SysSettingActivity.class);
				startActivity(settingIntent);
				break;
			case R.id.page_myinfo:
				/*
				Intent infoIntent = new Intent(TabHostActivity.this,
						UserInfoActivity.class);
				infoIntent.putExtra("userId", userInfo.getId());
				startActivity(infoIntent);
				*/
				Intent intent = new Intent(getApplicationContext(),
						ShowPersonInfo.class);
				intent.putExtra("studentId", user.getUserNumber());
				String myPic = user.getUserImage();
				intent.putExtra("userImage", myPic);
				startActivity(intent);
				break;
			// case R.id.page_questions:
			// Intent questionsIntent = new Intent(TabHostActivity.this,
			// WebSiteActivity.class);
			// questionsIntent.putExtra("url", CampusAPI.commonQuestionUrl);
			// questionsIntent.putExtra("title", "常见问题");
			// startActivity(questionsIntent);
			// break;
			// case R.id.page_recommend:
			// String[] data = { "新浪微博", "微信好友", "微信朋友圈" };
			// showDownloadDialog(data);
			// break;
			case R.id.page_myalbum:
				Intent albumIntent = new Intent(getApplicationContext(),
						AlbumPersonalActivity.class);
				startActivity(albumIntent);
				break;
			case R.id.page_changepwd:
			/*
			Intent contractIntent = new Intent(SysSettingActivity.this,WebSiteActivity.class);
			contractIntent.putExtra("url", CampusAPI.contractUrl);
			contractIntent.putExtra("title", getResources().getString(R.string.settings_contract));
			startActivity(contractIntent);
			*/
					//修改密码
					final EditText et=new EditText(TabHostActivity.this);
					new AlertDialog.Builder(TabHostActivity.this).setTitle("请输入旧密码").setView(et)
							.setPositiveButton("确定", new DialogInterface.OnClickListener()
							{

								@Override
								public void onClick(DialogInterface dialog, int which) {
									// TODO Auto-generated method stub
									String oldpwd=et.getText().toString();
									String mPassword=PrefUtility.get(Constants.PREF_LOGIN_PASS, "");
									if(!oldpwd.equals(mPassword))
									{
										AppUtility.showToastMsg(TabHostActivity.this, "旧密码不正确！");
									}
									else
									{
										Intent intent = new Intent(TabHostActivity.this,ChangePwdActivity.class);
										intent.putExtra("oldpwd", oldpwd);
										startActivity(intent);
									}
								}

							}).setNegativeButton("取消", null).show();
					TimeUtility.popSoftKeyBoard(TabHostActivity.this,et);
					break;
				case R.id.page_aboutus:
					Intent aboutusIntent = new Intent(TabHostActivity.this,WebSiteActivity.class);
					aboutusIntent.putExtra("url", CampusAPI.aboutusUrl);
					aboutusIntent.putExtra("title", getResources().getString(R.string.settings_aboutus));
					startActivity(aboutusIntent);
					break;
			case R.id.page_clear_cache:
				showClearCacheDialog();
				break;
			case R.id.page_feedback:
				Intent feedbackIntent = new Intent(TabHostActivity.this,
						FeedbackActivity.class);
				startActivity(feedbackIntent);
				break;
			case R.id.page_exit:
				showExit();
				break;
			default:
				break;
			}
		}
	}

	private DatabaseHelper getHelper() {
		if (database == null) {
			database = OpenHelperManager.getHelper(this, DatabaseHelper.class);
		}
		return database;
	}

	public void showDialog(String contentText) {
		AlertDialog.Builder builder = new AlertDialog.Builder(
				TabHostActivity.this);
		builder.setTitle("课程提醒");
		builder.setMessage(contentText);
		builder.setNegativeButton("知道了", new cancelStudentPicListener());
		AlertDialog ad = builder.create();
		ad.show();
	}

	private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(ACTION_NAME_REMIND)) {
				Log.d(TAG, "----------->BroadcastReceiver："
						+ ACTION_NAME_REMIND);
				String contentText = intent.getStringExtra("contentText");
				showDialog(contentText);
			}else if(action.equals(ACTION_CHATINTERACT)){
				showUnreadCnt();
			}
			else if(action.equals(ACTION_CHANGEHEAD))
			{
				String newhead=intent.getStringExtra("newhead");
				if(newhead!=null)
				{
                    /*
					AQuery aq = new AQuery(TabHostActivity.this);
					ImageOptions options = new ImageOptions();
					options.memCache=false;
					options.targetWidth=100;
					options.round = 50;
					aq.id(pagePhoto).image(newhead, options);
					*/
                    ImageLoader.getInstance().displayImage(newhead,pagePhoto,headOptions);
				}
			}
		}
	};

	/**
	 * 功能描述:显示消息数量
	 *
	 * @author shengguo  2014-5-29 下午3:07:35
	 *
	 */
	private void showUnreadCnt() {
		int count = 0;
		try {
			chatFriendList = chatFriendDao.queryBuilder().where().eq("hostid", user.getUserNumber()).query();
			for (ChatFriend chatFriend:chatFriendList) {
				count += chatFriend.getUnreadCnt();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		TextView unreadCnt = (TextView) mainTab.findViewById(R.id.unreadCnt);
		if(count!=0){
			unreadCnt.setText(String.valueOf(count));
			unreadCnt.setVisibility(View.VISIBLE);
		}else{
			unreadCnt.setVisibility(View.INVISIBLE);
		}
		
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "--------------注销广播/关闭服务-------------");
		try
		{
			unregisterReceiver(mBroadcastReceiver);
		}
		catch(IllegalArgumentException e)
		{
			
		}
		/*
		if(schoolService != null){
			unbindService(connection);
		}
		*/
		Log.d(TAG,"生命周期:onDestroy");
	}

    public void registerBoradcastReceiver() {
		IntentFilter myIntentFilter = new IntentFilter();
		myIntentFilter.addAction(ACTION_NAME_REMIND);
		myIntentFilter.addAction(ACTION_CHATINTERACT);
		myIntentFilter.addAction(ACTION_CHANGEHEAD);
		// 注册广播
		registerReceiver(mBroadcastReceiver, myIntentFilter);
	}

	private class cancelStudentPicListener implements
			DialogInterface.OnClickListener {

		@Override
		public void onClick(DialogInterface dialog, int which) {
			dialog.dismiss();
		}

	}

	private void showExit() {
		View dialog_view = LayoutInflater.from(TabHostActivity.this).inflate(
				R.layout.dialog_exit, null);
		AlertDialog dialog_exit = new AlertDialog.Builder(TabHostActivity.this)
				.setView(dialog_view)
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// 取消登陆成功状态
						((CampusApplication)getApplicationContext()).setLoginUserObj(null);
						((CampusApplication)getApplicationContext()).setLinkManDic(null);
						((CampusApplication)getApplicationContext()).setLinkGroupList(null);
						((CampusApplication)getApplicationContext()).setStudentDic(null);
						PrefUtility.put(Constants.PREF_INIT_BASEDATE_FLAG,
								false);
						PrefUtility
								.put(Constants.PREF_INIT_CONTACT_FLAG, false);
						PrefUtility.put(Constants.PREF_SELECTED_WEEK, 0);
						PrefUtility.put(Constants.PREF_LOGIN_NAME, "");
						PrefUtility.put(Constants.PREF_LOGIN_PASS, "");
						PrefUtility.put(Constants.PREF_INIT_CONTACT_STR, "");
						PrefUtility.put(Constants.PREF_INIT_DATA_STR, "");
						Intent intent = new Intent(TabHostActivity.this,
								LoginActivity.class);
						intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
								| Intent.FLAG_ACTIVITY_NEW_TASK);
						startActivity(intent);
						System.exit(0);
						dialog.dismiss();
					}
				})
				.setNegativeButton("取消", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				}).create();
		dialog_exit.show();
	}

	// /**
	// * 功能描述:下载弹出框
	// *
	// * @author zhuliang 2013-12-25 下午1:24:26
	// *
	// */
	// private void showDownloadDialog(String[] data) {
	// View view = getLayoutInflater()
	// .inflate(R.layout.view_exam_dialog, null);
	// Button cancel = (Button) view.findViewById(R.id.cancel);
	// ListView mList = (ListView) view.findViewById(R.id.list);
	// DialogAdapter dialogAdapter = new DialogAdapter(data);
	// mList.setAdapter(dialogAdapter);
	// downloadDialog.setContentView(view);
	// downloadDialog.show();
	// Window window = downloadDialog.getWindow();
	// window.setGravity(Gravity.BOTTOM);// 在底部弹出
	// window.setWindowAnimations(R.style.CustomDialog);
	// cancel.setOnClickListener(new OnClickListener() {
	//
	// @Override
	// public void onClick(View v) {
	// downloadDialog.dismiss();
	// }
	// });
	// }

	/**
	 * 功能描述:清除缓存
	 * 
	 * @author shengguo 2014-5-5 下午3:45:04
	 * 
	 */
	private void showClearCacheDialog() {
		View view = getLayoutInflater()
				.inflate(R.layout.view_clear_cache, null);
		Button cancel = (Button) view.findViewById(R.id.cancel);
		Button ok = (Button) view.findViewById(R.id.ok);
		clearCacheDialog.setContentView(view);
		clearCacheDialog.show();
		Window window = clearCacheDialog.getWindow();
		window.setGravity(Gravity.BOTTOM);// 在底部弹出
		window.setWindowAnimations(R.style.CustomDialog);
		cancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				clearCacheDialog.dismiss();
			}
		});
		ok.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				
				getApplication().onLowMemory();
				try {
					List<Notice> typelist=noticeInfoDao.query(noticeInfoDao.queryBuilder().distinct().selectColumns("newsType").prepare());
					
					noticeInfoDao.delete(noticeInfoDao.deleteBuilder().prepare());
					for (Notice item:typelist)
					{
						Intent intent = new Intent("refreshUnread");
						intent.putExtra("title", item.getNewsType());
						sendBroadcast(intent);
					}
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				clearCacheDialog.dismiss();
				AppUtility.showToastMsg(TabHostActivity.this,
						getString(R.string.cleared_all_cached_images));
			}
		});
	}


	
	
	/**
	 * 功能描述:版本检测
	 *
	 * @author shengguo  2014-6-3 下午4:05:05
	 *
	 */
	private void versionDetection() {
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
				msg.what = 1;
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
		View view = LayoutInflater.from(TabHostActivity.this).inflate(
				R.layout.view_textview, null);
		TextView tvTip = (TextView) view.findViewById(R.id.tv_text);
		tvTip.setText(tips);
		AlertDialog dialog_UpdateTips = new AlertDialog.Builder(TabHostActivity.this)
				.setView(view)
				.setTitle(newVer+"版更新提示")
				.setPositiveButton("下载更新", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						Log.d(TAG, "-------------downLoadPath:" + downLoadPath);
						//schoolService.downLoadUpdate(downLoadPath, 1001);
						downloadFile(downLoadPath);
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
	
	private void downloadFile(String url)
	{
		AppUtility.downloadUrl(url, null, this);
	}
	
	private void getAlbumUnreadCount() {
		
		long datatime = System.currentTimeMillis();
		String checkCode = PrefUtility.get(Constants.PREF_CHECK_CODE, "");
		JSONObject jo = new JSONObject();
		try {
			jo.put("action", "相册未读消息");
			jo.put("用户较验码", checkCode);
			jo.put("DATETIME", datatime);
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		String base64Str = Base64.encode(jo.toString().getBytes());
		CampusParameters params = new CampusParameters();
		params.add(Constants.PARAMS_DATA, base64Str);
		CampusAPI.getDownloadSubject(params, "AlbumPraise.php", new RequestListener() {

			@Override
			public void onIOException(IOException e) {
				// TODO Auto-generated method stub

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
				msg.what = 2;
				msg.obj = response;
				mHandler.sendMessage(msg);
				
			}
		});
	}
	private void updateUnreadCount()
	{
		TextView unreadCnt = (TextView) mainTab.findViewById(R.id.unreadCntAlbum);
		List<AlbumMsgInfo> unreadList;
		try {
			albumMsgDao=database.getAlbumMsgDao();
			String hostId=PrefUtility.get(Constants.PREF_CHECK_HOSTID, "");
			unreadList = albumMsgDao.queryBuilder().where().eq("ifRead",0).and().eq("toId", hostId).query();
			if(unreadList.size()>0)
			{
				unreadCnt.setText(String.valueOf(unreadList.size()));
				unreadCnt.setVisibility(View.VISIBLE);
			}
			else
			{
				unreadCnt.setVisibility(View.INVISIBLE);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	@TargetApi(23)
	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
	{
		if(callBack!=null)
			AppUtility.permissionResult(requestCode,grantResults,this,callBack);
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
	}


}
