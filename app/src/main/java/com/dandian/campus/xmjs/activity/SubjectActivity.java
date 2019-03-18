package com.dandian.campus.xmjs.activity;

import java.util.Calendar;
import java.util.Date;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.NumberPicker.OnValueChangeListener;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;


import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.dandian.campus.xmjs.R;
import com.dandian.campus.xmjs.activity.TabHostActivity.MenuListener;
import com.dandian.campus.xmjs.base.Constants;
import com.dandian.campus.xmjs.base.ExitApplication;
import com.dandian.campus.xmjs.db.DatabaseHelper;
import com.dandian.campus.xmjs.db.InitData;
import com.dandian.campus.xmjs.fragment.SubjectFragment;
import com.dandian.campus.xmjs.util.AppUtility;
import com.dandian.campus.xmjs.util.DateHelper;
import com.dandian.campus.xmjs.util.DialogUtility;
import com.dandian.campus.xmjs.util.ImageUtility;
import com.dandian.campus.xmjs.util.PrefUtility;

@SuppressLint("NewApi")
public class SubjectActivity extends FragmentActivity {
	Button bn_refresh;
	static Button bn_menu;
	static LinearLayout layout_menu;
	private TextView tv_title;
	private LinearLayout initlayout;
	boolean selection = true;
	private View view, selectWeekView;
	private TextView tvClose, tvOk, tvRight;
	private NumberPicker nPicker1;
	private PopupWindow popupWindow;
	private final String ACTION_NAME = "xmjs_refreshSubject";
	private Dialog mLoadingDialog;
	private SubjectFragment subjectFragment;
	private DatabaseHelper database;
	private int currentWeek, selectedWeek, maxWeek;// 当前周次，选择周次 "最大周次
	private static final String TAG = "SubjectActivity";
	private boolean isInitDate = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Log.d(TAG, "----------------onCreate-----------------------");
		setContentView(R.layout.activity_subject);
		ExitApplication.getInstance().addActivity(this);
		mLoadingDialog = DialogUtility.createLoadingDialog(this, "正在获取数据...");
		Calendar nowCal = Calendar.getInstance();
		nowCal.setTime(new Date());
		currentWeek=PrefUtility.getInt(Constants.PREF_CURRENT_WEEK, 0);
		selectedWeek = PrefUtility.getInt(Constants.PREF_SELECTED_WEEK, 0);
		maxWeek = PrefUtility.getInt(Constants.PREF_MAX_WEEK, 0);
		Log.d(TAG, "currentWeek:" + currentWeek + ",selectedWeek:"
				+ selectedWeek + ",maxWeek:" + maxWeek);
		view = findViewById(R.id.subject);
		subjectFragment = (SubjectFragment) getSupportFragmentManager()
				.findFragmentById(R.id.subjectfragment);
		initTitle();
		
		if(!loadScheduleBg())
			view.setBackgroundResource(R.drawable.subject_bg);
		// 注册广播
		registerBoradcastReceiver();
		
	}
	@Override
	protected void onStart()
	{
		super.onStart();
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.cancel(2);
	}
	private void initTitle() {
		initlayout = (LinearLayout) findViewById(R.id.initlayout);
		layout_menu = (LinearLayout) findViewById(R.id.layout_back);
		LinearLayout layoutRefresh = (LinearLayout) findViewById(R.id.layout_goto);
		bn_menu = (Button) findViewById(R.id.btn_back);
		bn_refresh = (Button) findViewById(R.id.btn_goto);
		tv_title = (TextView) findViewById(R.id.tv_title);
		ImageButton imageButton_left=(ImageButton)findViewById(R.id.imageButton_left);
		ImageButton imageButton_right=(ImageButton)findViewById(R.id.imageButton_right);
		imageButton_left.setOnClickListener(new MyListener());
		imageButton_right.setOnClickListener(new MyListener());
		bn_menu.setBackgroundResource(R.drawable.bg_title_homepage_back);
		if(PrefUtility.get(Constants.PREF_CHECK_USERTYPE,"").equals("老师") && PrefUtility.get(Constants.PREF_CLASSES_BANZHUREN,"").length()>0) {
			bn_refresh.setBackgroundResource(R.drawable.dropdown);
			layoutRefresh.setOnClickListener(new MyListener());
			bn_refresh.setVisibility(View.VISIBLE);
		}
		else
			bn_refresh.setVisibility(View.INVISIBLE);
		tv_title.setOnClickListener(new MyListener());
		layout_menu.setOnClickListener(TabHostActivity.menuListener);
		
		isInitDate = PrefUtility.getBoolean(Constants.PREF_INIT_BASEDATE_FLAG, false);
		Log.d(TAG, "----------isInitDate"+isInitDate);
		if (isInitDate) {
			
				String lastdt=PrefUtility.get(Constants.PREF_INIT_BASEDATE_DATE, "");
				if(lastdt==null)
					regetKebiao();
				else
				{
					Date dt=DateHelper.getStringDate(lastdt, "yyyy-MM-dd");
					if(dt!=null)
					{
						int lastweek=DateHelper.getWeekIndexOfYear(dt);
						int nowweek=DateHelper.getWeekIndexOfYear(new Date());
						if(lastweek!=nowweek)
							regetKebiao();
					}
				}
				
				if (currentWeek == selectedWeek) {
					tv_title.setText("第" + selectedWeek + "周(本周)");
				} else {
					tv_title.setText("第" + selectedWeek + "周(非本周)");
				}
				
			
			
		}else{
			regetKebiao();
			/*
			Intent intent = new Intent(SubjectActivity.this,Alarmreceiver.class);
			String checkCode = PrefUtility.get(Constants.PREF_CHECK_CODE, "");
			intent.putExtra(Constants.PREF_CHECK_CODE, checkCode);
			intent.setAction("initBaseData");
			sendBroadcast(intent);
			*/
		}
	}
	private void regetKebiao()
	{
		//initlayout.setVisibility(View.VISIBLE);
		//tv_title.setVisibility(View.INVISIBLE);
		PrefUtility.put(Constants.PREF_SELECTED_WEEK, 0);
		String checkCode = PrefUtility.get(Constants.PREF_CHECK_CODE, "");
		InitData initData = new InitData(this,getHelper(), mLoadingDialog,ACTION_NAME,checkCode);
		initData.initAllInfo();
	}
	private class MyListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.tv_title:
				selectedWeeks();
				break;
			case R.id.layout_goto:

				PopupMenu popupMenu = new PopupMenu(SubjectActivity.this, v);
				android.view.Menu menu_more = popupMenu.getMenu();
				String banjistr=PrefUtility.get(Constants.PREF_CLASSES_BANZHUREN,"");
				final String banji[]=banjistr.split(",");
				menu_more.add(0, 0, 0, "我的课表");
				int selectindex=0;
				String banzhuren_view_banji=PrefUtility.get(Constants.PREF_CLASSES_BANZHUREN_VIEW,"");
				for (int i = 0; i < banji.length; i++) {
					if(banji[i]!=null && banji[i].length()>0) {
						menu_more.add(0, i + 1, i + 1, banji[i]);
						if(banzhuren_view_banji.equals(banji[i]))
							selectindex=i+1;
					}
				}
				menu_more.setGroupCheckable(0,true,true);
				menu_more.findItem(selectindex).setChecked(true);
				popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
					@Override
					public boolean onMenuItemClick(MenuItem menuItem) {
						String banzhuren_view_banji;
						if(menuItem.getItemId()==0) {
							banzhuren_view_banji="";
						}
						else
							banzhuren_view_banji=banji[menuItem.getItemId()-1];
						PrefUtility.put(Constants.PREF_CLASSES_BANZHUREN_VIEW,banzhuren_view_banji);
						regetKebiao();
						return true;
					}
				});
				popupMenu.show();
				break;
			case R.id.imageButton_left:
				if(selectedWeek>1)
				{
					selectedWeek--;
					PrefUtility.put(Constants.PREF_SELECTED_WEEK, selectedWeek);
					String checkCode = PrefUtility.get(Constants.PREF_CHECK_CODE,"");
					InitData initData = new InitData(SubjectActivity.this,
							getHelper(), mLoadingDialog, ACTION_NAME, checkCode);
					initData.initAllInfo();
				}
				break;
			case R.id.imageButton_right:
				if(selectedWeek<maxWeek)
				{
					selectedWeek++;
					PrefUtility.put(Constants.PREF_SELECTED_WEEK, selectedWeek);
					String checkCode = PrefUtility.get(Constants.PREF_CHECK_CODE,"");
					InitData initData = new InitData(SubjectActivity.this,
							getHelper(), mLoadingDialog, ACTION_NAME, checkCode);
					initData.initAllInfo();
				}
				break;

			}
		}
	}

	/**
	 * 功能描述:选择第几周的上课记录
	 * 
	 * @author shengguo 2014-5-15 下午5:13:06
	 * 
	 */
	/**
	 * 功能描述:
	 * 
	 * @author shengguo 2014-5-16 下午6:16:12
	 * 
	 */
	@SuppressWarnings("deprecation")
	public void selectedWeeks() {
		selectWeekView = getLayoutInflater()
				.inflate(R.layout.select_week, null);
		nPicker1 = (NumberPicker) selectWeekView
				.findViewById(R.id.numberPicker1);
		
		tvOk = (TextView) selectWeekView.findViewById(R.id.tv_ok);
		tvClose = (TextView) selectWeekView.findViewById(R.id.tv_close);
		tvRight = (TextView) selectWeekView.findViewById(R.id.tv_right);
		tvOk.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				selectedWeek = nPicker1.getValue();
				Log.d(TAG, "第" + selectedWeek + "周 ");
				PrefUtility.put(Constants.PREF_SELECTED_WEEK, selectedWeek);
				String checkCode = PrefUtility.get(Constants.PREF_CHECK_CODE,"");
				InitData initData = new InitData(SubjectActivity.this,
						getHelper(), mLoadingDialog, ACTION_NAME, checkCode);
				initData.initAllInfo();
				popupWindow.dismiss();
			}
		});
		tvClose.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				popupWindow.dismiss();
			}
		});
		if(selectedWeek>maxWeek)
			maxWeek=selectedWeek+1;
		nPicker1.setMaxValue(maxWeek);
		nPicker1.setMinValue(1);
		nPicker1.setValue(selectedWeek);
		nPicker1.setOnValueChangedListener(new OnValueChangeListener() {

			@Override
			public void onValueChange(NumberPicker picker, int oldVal,
					int newVal) {
				Log.d(TAG, "oldVal:" + oldVal + ",newVal:" + newVal);
				if (newVal == currentWeek) {
					tvRight.setText("周(本周)");
				} else {
					tvRight.setText("周");
				}
			}
		});

		popupWindow = new PopupWindow(selectWeekView, LayoutParams.FILL_PARENT,
				LayoutParams.WRAP_CONTENT);
		popupWindow.setFocusable(true);
		popupWindow.setAnimationStyle(R.style.popupAnimation);
		// 点击外部消失
		popupWindow.setOutsideTouchable(true);
		popupWindow.setBackgroundDrawable(new BitmapDrawable());
		popupWindow.showAtLocation(view.findViewById(R.id.subject),
				Gravity.RIGHT | Gravity.BOTTOM, 0, 0);

	}

	public void registerBoradcastReceiver() {
		IntentFilter myIntentFilter = new IntentFilter();
		myIntentFilter.addAction(ACTION_NAME);
		myIntentFilter.addAction("changeScheduleBg");
		// 注册广播
		registerReceiver(mBroadcastReceiver, myIntentFilter);
	}

	private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			Log.d(TAG, "----------->BroadcastReceiver：" + action);
			if (action.equals(ACTION_NAME)) {
				isInitDate = true;
				selectedWeek=PrefUtility.getInt(Constants.PREF_SELECTED_WEEK, 0);
				currentWeek=PrefUtility.getInt(Constants.PREF_CURRENT_WEEK, 0);
				maxWeek = PrefUtility.getInt(Constants.PREF_MAX_WEEK, 0);
				Log.d(TAG, "----BroadcastReceivercurrentWeek:" + currentWeek + ",selectedWeek:"
						+ selectedWeek + ",maxWeek:" + maxWeek);
				// 初始化成功重新加载课表数据
				subjectFragment.initTable();
				initlayout.setVisibility(View.INVISIBLE);
				tv_title.setVisibility(View.VISIBLE);
				if (currentWeek == selectedWeek) {
					tv_title.setText("第" + selectedWeek + "周(本周)");
				} else {
					tv_title.setText("第" + selectedWeek + "周(非本周)");
				}
			}
			if(action.equals("changeScheduleBg"))
			{
				if(loadScheduleBg())
					AppUtility.showToastMsg(SubjectActivity.this, "背景已修改");
				else
					AppUtility.showToastMsg(SubjectActivity.this, "背景修改失败，手机系统版本太低");
			}
		}
	};
	private boolean loadScheduleBg()
	{
		String bgname=PrefUtility.get("scheduleBg","default");
		BitmapDrawable drawable;
		Bitmap bitmap;
		if(bgname.equals("default"))
		{
			bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.subject_bg);  
			//bitmap = Bitmap.createBitmap(100, 20, Config.ARGB_8888);  
		}
		else
		{
			view.setBackground(Drawable.createFromPath(bgname));
			bitmap = BitmapFactory.decodeFile(bgname);  
		}
		if(bitmap!=null)
		{
			bitmap=ImageUtility.zoomBitmap(bitmap, this.getWindowManager().getDefaultDisplay().getWidth());
			drawable = new BitmapDrawable(this.getResources(),bitmap);  
			drawable.setTileModeXY(TileMode.REPEAT , TileMode.REPEAT );  
			drawable.setDither(true); 
			view.setBackground(drawable);
			return true;
		}
		return false;
		
	}
	@Override
	protected void onDestroy() {
		unregisterReceiver(mBroadcastReceiver);
		super.onDestroy();
	}

    private DatabaseHelper getHelper() {
		if (database == null) {
			database = OpenHelperManager.getHelper(SubjectActivity.this,
					DatabaseHelper.class);
		}
		return database;
	}

	public int getCurrentWeek() {
		return currentWeek;
	}

	public void setCurrentWeek(int currentWeek) {
		this.currentWeek = currentWeek;
	}

	public int getSelectedWeek() {
		return selectedWeek;
	}

	public void setSelectedWeek(int selectedWeek) {
		this.selectedWeek = selectedWeek;
	}

	public int getMaxWeek() {
		return maxWeek;
	}

	public void setMaxWeek(int maxWeek) {
		this.maxWeek = maxWeek;
	}
}
