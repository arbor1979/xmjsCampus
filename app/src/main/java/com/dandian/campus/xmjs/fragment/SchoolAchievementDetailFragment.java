package com.dandian.campus.xmjs.fragment;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.R.color;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Browser;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.text.util.Linkify;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.TextView;

import com.alipay.sdk.app.PayTask;
import com.dandian.campus.xmjs.activity.ClassDetailActivity;
import com.dandian.campus.xmjs.entity.PayResult;
import com.dandian.campus.xmjs.entity.TeacherInfo;
import com.dandian.campus.xmjs.util.SignUtils;
import com.androidquery.AQuery;
import com.dandian.campus.xmjs.R;
import com.dandian.campus.xmjs.activity.SchoolDetailActivity;
import com.dandian.campus.xmjs.activity.SysSettingActivity;
import com.dandian.campus.xmjs.activity.WebSiteActivity;
import com.dandian.campus.xmjs.adapter.MyPictureAdapter;
import com.dandian.campus.xmjs.api.CampusAPI;
import com.dandian.campus.xmjs.api.CampusException;
import com.dandian.campus.xmjs.api.CampusParameters;
import com.dandian.campus.xmjs.api.RequestListener;
import com.dandian.campus.xmjs.base.Constants;
import com.dandian.campus.xmjs.entity.AchievementDetail;
import com.dandian.campus.xmjs.entity.AchievementDetail.Achievement;
import com.dandian.campus.xmjs.util.AppUtility;
import com.dandian.campus.xmjs.util.Base64;
import com.dandian.campus.xmjs.util.DialogUtility;
import com.dandian.campus.xmjs.util.FileUtility;
import com.dandian.campus.xmjs.util.IntentUtility;
import com.dandian.campus.xmjs.util.PrefUtility;
import com.dandian.campus.xmjs.widget.NonScrollableGridView;
import com.dandian.campus.xmjs.widget.NonScrollableListView;

/**
 * 成绩详情
 */
public class SchoolAchievementDetailFragment extends Fragment {
	private String TAG = "SchoolAchievementDetailFragment";
	private ListView myListview;
	private Button btnLeft;
	private TextView tvTitle,tvRight;
	private LinearLayout lyLeft,lyRight;
	private LinearLayout loadingLayout;
	private LinearLayout contentLayout;
	private LinearLayout failedLayout;
	private LinearLayout emptyLayout;
	private AchievementDetail achievementDetail;
	private String title, interfaceName;
	private LayoutInflater inflater;
	private AchieveAdapter adapter;
	private LayoutParams leftParams, rightParams;
	private Dialog dialog;
	private List<Achievement> achievements = new ArrayList<Achievement>();
	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case -1:
				showFetchFailedView();
				AppUtility.showErrorToast(getActivity(), msg.obj.toString());
				break;
			case 0:
				showProgress(false);
				String result = msg.obj.toString();
				String resultStr = "";
				if (AppUtility.isNotEmpty(result)) {
					try {
						resultStr = new String(Base64.decode(result
								.getBytes("GBK")));
						Log.d(TAG, resultStr);
					} catch (UnsupportedEncodingException e) {
						showFetchFailedView();
						e.printStackTrace();
					}
				}else{
					showFetchFailedView();
				}

				if (AppUtility.isNotEmpty(resultStr)) {
					try {
						JSONObject jo = new JSONObject(resultStr);
						String res = jo.optString("结果");
						if(AppUtility.isNotEmpty(res)){
							AppUtility.showToastMsg(getActivity(), res);
						}else{
							achievementDetail = new AchievementDetail(jo);
							Log.d(TAG, "--------noticesItem.getNotices().size():"
									+ achievementDetail.getAchievements().size());
							initDate();
						}
					} catch (JSONException e) {
						//showFetchFailedView();
						e.printStackTrace();
					}
				}else{
					//showFetchFailedView();
				}
				break;
			case 1:
				result = msg.obj.toString();
				resultStr = "";
				if (AppUtility.isNotEmpty(result)) {
					try {
						resultStr = new String(Base64.decode(result
								.getBytes("GBK")));
						Log.d(TAG, resultStr);
					} catch (UnsupportedEncodingException e) {
						showFetchFailedView();
						e.printStackTrace();
					}
				}else{
					showFetchFailedView();
				}

				if (AppUtility.isNotEmpty(resultStr)) {
					try {
						JSONObject jo = new JSONObject(resultStr);
						String res = jo.optString("结果");
						AppUtility.showToastMsg(getActivity(), "操作"+res);
						if(res.equals("成功"))
							getAchievesItem();
						
					} catch (JSONException e) {
						showFetchFailedView();
						e.printStackTrace();
					}
				}else{
					showFetchFailedView();
				}
				break;
			case 2:
				LinearLayout v=(LinearLayout)msg.obj;
				v.setVisibility(View.GONE);
				break;
			case 3:
				result = msg.obj.toString();
				resultStr = "";
				if (AppUtility.isNotEmpty(result)) {
					try {
						resultStr = new String(Base64.decode(result
								.getBytes("GBK")));
						Log.d(TAG, resultStr);
					} catch (UnsupportedEncodingException e) {
						showFetchFailedView();
						e.printStackTrace();
					}
				}else{
					showFetchFailedView();
				}

				if (AppUtility.isNotEmpty(resultStr)) {
					try {
						JSONObject jo = new JSONObject(resultStr);
						String res = jo.optString("结果");
						
						if(res.equals("成功"))
						{
							AppUtility.showToastMsg(getActivity(), "操作成功!");
							String autoClose=jo.optString("自动关闭");
							if(autoClose!=null && autoClose.equals("是"))
							{
								Intent aintent = new Intent();
								getActivity().setResult(1,aintent); 
								getActivity().finish();
							}
							else
								getAchievesItem();
						}
						else
							AppUtility.showErrorToast(getActivity(), "操作失败:"+res);
						
					} catch (JSONException e) {
						showFetchFailedView();
						e.printStackTrace();
					}
				}else{
					showFetchFailedView();
				}
				break;
			case 4: 
				PayResult payResult = new PayResult((String) msg.obj);
				
				// 支付宝返回此次支付结果及加签，建议对支付宝签名信息拿签约时支付宝提供的公钥做验签
				String resultInfo = payResult.getResult();
				
				String resultStatus = payResult.getResultStatus();

				// 判断resultStatus 为“9000”则代表支付成功，具体状态码代表含义可参考接口文档
				if (TextUtils.equals(resultStatus, "9000")) {
					Toast.makeText(getActivity(), "支付成功",
							Toast.LENGTH_SHORT).show();
					getAchievesItem();
					Intent aintent = new Intent();
					getActivity().setResult(1,aintent); 
				} else {
					// 判断resultStatus 为非“9000”则代表可能支付失败
					// “8000”代表支付结果因为支付渠道原因或者系统原因还在等待支付结果确认，最终交易是否成功以服务端异步通知为准（小概率状态）
					if (TextUtils.equals(resultStatus, "8000")) {
						Toast.makeText(getActivity(), "支付结果确认中",
								Toast.LENGTH_SHORT).show();

					} else {
						// 其他值就可以判断为支付失败，包括用户主动取消支付，或者系统返回的错误
						Toast.makeText(getActivity(), "支付失败",
								Toast.LENGTH_SHORT).show();

					}
				}
				break;
			}
			
				
		}
	};

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (resultCode) { //resultCode为回传的标记，我在B中回传的是RESULT_OK
			case 1:
				getAchievesItem();
				break;
			default:
				break;
		}
	}
	public static final SchoolAchievementDetailFragment newInstance(String title, String interfaceName)
	{
		SchoolAchievementDetailFragment fragment = new SchoolAchievementDetailFragment();
		Bundle bundle = new Bundle();
		bundle.putString("title",title);
		bundle.putString("interfaceName", interfaceName);
		fragment.setArguments(bundle);
		return fragment ;
	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		title=getArguments().getString("title");
		interfaceName=getArguments().getString("interfaceName");
		leftParams = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT, 1.0f);
		rightParams = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT, 1.0f);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		this.inflater = inflater;
		View view = inflater.inflate(R.layout.school_listview_fragment,
				container, false);
		myListview = (ListView) view.findViewById(R.id.my_listview);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
			AppUtility.setRootViewPadding(view);
		btnLeft = (Button) view.findViewById(R.id.btn_left);
		tvRight = (TextView) view.findViewById(R.id.tv_right);
		tvTitle = (TextView) view.findViewById(R.id.tv_title);
		lyLeft = (LinearLayout) view.findViewById(R.id.layout_btn_left);
		lyRight = (LinearLayout) view.findViewById(R.id.layout_btn_right);
		loadingLayout = (LinearLayout) view.findViewById(R.id.data_load);
		contentLayout = (LinearLayout) view.findViewById(R.id.content_layout);
		failedLayout = (LinearLayout) view.findViewById(R.id.empty_error);
		emptyLayout = (LinearLayout) view.findViewById(R.id.empty);
		myListview.setEmptyView(emptyLayout);
		btnLeft.setVisibility(View.VISIBLE);
		btnLeft.setCompoundDrawablesWithIntrinsicBounds(
				R.drawable.bg_btn_left_nor, 0, 0, 0);
		tvTitle.setText(title);
		adapter = new AchieveAdapter();
		myListview.setAdapter(adapter);
		lyLeft.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				getActivity().finish();
			}
		});
		// 重新加载
		failedLayout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				getAchievesItem();
			}
		});
		getAchievesItem();
		return view;
	}

	/**
	 * 功能描述:初始化数据
	 * 
	 * @author shengguo 2014-4-17 下午5:18:06
	 * 
	 */
	private void initDate() {
		tvTitle.setText(achievementDetail.getTitle());
		achievements = achievementDetail.getAchievements();
		//设置Weight值
		float leftWeight = achievementDetail.getLeftWeight() / 10.0f;
		float rightWeight = achievementDetail.getRightWeight() / 10.0f;
		Log.d(TAG, "leftWeight:" + leftWeight + ",rightWeight:" + rightWeight);
		leftParams = new LayoutParams(0,LayoutParams.WRAP_CONTENT, leftWeight);
		rightParams = new LayoutParams(0,LayoutParams.WRAP_CONTENT, rightWeight);
		if(achievementDetail.getSubmitBtn()!=null && achievementDetail.getSubmitBtn().length()>0)
		{
			tvRight.setText(achievementDetail.getSubmitBtn());
			tvRight.setVisibility(View.VISIBLE);
			lyRight.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if(achievementDetail.getSubmitTarget().equals("是"))
					{
						submitButtonClick(achievementDetail.getSubmitBtnUrl());
					}
					else
					{
						Intent intent =new Intent(getActivity(),SchoolDetailActivity.class);
						intent.putExtra("templateName", "调查问卷");
						int pos=interfaceName.indexOf("?");
						String preUrl=interfaceName;
						if(pos>-1)
							preUrl=interfaceName.substring(0, pos);
						intent.putExtra("interfaceName", preUrl+achievementDetail.getSubmitBtnUrl());
						intent.putExtra("title", title);
						intent.putExtra("status", "进行中");
						intent.putExtra("autoClose", "是");
						startActivityForResult(intent, 101);
					}
				}
			});
		}
		else
		{
			tvRight.setVisibility(View.GONE);
			lyRight.setOnClickListener(null);
		}
		adapter.notifyDataSetChanged();
	}
	//submit按钮
	private void submitButtonClick(String url) {

		long datatime = System.currentTimeMillis();
		String checkCode = PrefUtility.get(Constants.PREF_CHECK_CODE, "");

		JSONObject jo = new JSONObject();
		try {
			jo.put("用户较验码", checkCode);
			jo.put("DATETIME", datatime);
		
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		dialog = DialogUtility.createLoadingDialog(getActivity(),
				"数据处理中...");
		dialog.show();

		String base64Str = Base64.encode(jo.toString().getBytes());
	
		CampusParameters params = new CampusParameters();
		params.add(Constants.PARAMS_DATA, base64Str);
		int pos=interfaceName.indexOf("?");
		String preUrl=interfaceName;
		if(pos>-1)
			preUrl=interfaceName.substring(0, pos);
		CampusAPI.getSchoolItem(params,
				preUrl + url,
				new RequestListener() {

					@Override
					public void onIOException(IOException e) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onError(CampusException e) {
						Log.d(TAG, "----response" + e.getMessage());
						if(dialog != null){
							dialog.dismiss();
						}
						Message msg = new Message();
						msg.what = -1;
						msg.obj = e.getMessage();
						mHandler.sendMessage(msg);
					}

					@Override
					public void onComplete(String response) {
						Log.d(TAG, "----response" + response);
						if(dialog != null){
							dialog.dismiss();
						}
						Message msg = new Message();
						msg.what = 3;
						msg.obj = response;
						mHandler.sendMessage(msg);
					}
				});
	}
	/**
	 * 显示加载失败提示页
	 */
	private void showFetchFailedView() {
		loadingLayout.setVisibility(View.GONE);
		contentLayout.setVisibility(View.GONE);
		failedLayout.setVisibility(View.VISIBLE);
	}

	private void showProgress(boolean progress) {
		if (progress) {
			loadingLayout.setVisibility(View.VISIBLE);
			contentLayout.setVisibility(View.GONE);
			failedLayout.setVisibility(View.GONE);
		} else {
			loadingLayout.setVisibility(View.GONE);
			contentLayout.setVisibility(View.VISIBLE);
			failedLayout.setVisibility(View.GONE);
		}
	}

	/**
	 * 功能描述:获取通知内容
	 * 
	 * @author shengguo 2014-4-16 上午11:12:43
	 * 
	 */
	public void getAchievesItem() {
		showProgress(true);
		Log.d(TAG, "--------" + String.valueOf(new Date().getTime()));
		long datatime = System.currentTimeMillis();
		String checkCode = PrefUtility.get(Constants.PREF_CHECK_CODE, "");
		Log.d(TAG, "----------datatime:" + datatime);
		Log.d(TAG, "----------checkCode:" + checkCode + "++");
		JSONObject jo = new JSONObject();
		try {
			jo.put("用户较验码", checkCode);
			jo.put("DATETIME", datatime);
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		String base64Str = Base64.encode(jo.toString().getBytes());
		Log.d(TAG, "------->base64Str:" + base64Str);
		CampusParameters params = new CampusParameters();
		params.add(Constants.PARAMS_DATA, base64Str);
		CampusAPI.getSchoolItem(params, interfaceName, new RequestListener() {

			@Override
			public void onIOException(IOException e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onError(CampusException e) {
				Log.d(TAG, "----response" + e.getMessage());

			}

			@Override
			public void onComplete(String response) {
				Log.d(TAG, "----response" + response);
				Message msg = new Message();
				msg.what = 0;
				msg.obj = response;
				mHandler.sendMessage(msg);
			}
		});
	}

	@SuppressLint("NewApi")
	class AchieveAdapter extends BaseAdapter {

		
		@Override
		public int getCount() {
			return achievements.size();
		}

		@Override
		public Object getItem(int position) {
			return achievements.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;

			if (null == convertView) {
				convertView = inflater.inflate(
						R.layout.school_achievement_detail_item, parent, false);
				holder = new ViewHolder();
				holder.celllayout=(LinearLayout)convertView.findViewById(R.id.cell_layout);
				holder.left = (TextView) convertView.findViewById(R.id.thieDescription);
				holder.right = (TextView) convertView.findViewById(R.id.tv_right);
				holder.hiddenBtn=(ImageView)convertView.findViewById(R.id.hiddenBtn);
				holder.ly_hidden=(LinearLayout)convertView.findViewById(R.id.ly_hidden);
				holder.grid_picture=(NonScrollableGridView)convertView.findViewById(R.id.grid_picture);
				holder.list_fujian=(NonScrollableListView)convertView.findViewById(R.id.list_fujian);
				
				holder.right_layout=(LinearLayout)convertView.findViewById(R.id.right_layout);
				convertView.setTag(holder);
				
				
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			AQuery aq = new AQuery(convertView);
			final Achievement achievement = (Achievement) getItem(position);
			holder.left.setText(achievement.getSubject());
			holder.right.setText(achievement.getFraction());
			holder.right.setVisibility(View.VISIBLE);
			
			Pattern pattern = Pattern.compile("^((13[0-9])|(15[^4])|(18[0,2,3,5-9])|(17[0-8])|(147))\\d{8}$");
			Linkify.addLinks(holder.right, pattern, "tel:", new Linkify.MatchFilter() {
			     public final boolean acceptMatch(CharSequence s, int start, int end) {
			       int digitCount = 0;

			       for (int i = start; i < end; i++) {
			         if (Character.isDigit(s.charAt(i))) {
			           digitCount++;
			           if (digitCount == 11 ) {
			             return true;
			         }
			       }
			     }
			      return false;
			    }
			  }, Linkify.sPhoneNumberTransformFilter);
			
			if(achievement.getHiddenBtn()!=null && achievement.getHiddenBtn().length()>0)
			{
				
				aq.id(holder.hiddenBtn).image(achievement.getHiddenBtn(),false,true);
				holder.ly_hidden.setOnClickListener(new OnClickListener(){

					@Override
					public void onClick(View v) {
						doRequestUrl(achievement.getHiddenBtnURL());
					}
					
				});
			}
			holder.left.setLayoutParams(leftParams);
			holder.right_layout.setLayoutParams(rightParams);
			if(achievement.getImageList()!=null && achievement.getImageList().size()>0)
			{
				holder.grid_picture.setVisibility(View.VISIBLE);
				if(holder.right.getText().length()==0)
					holder.right.setVisibility(View.GONE);
				if(holder.grid_picture.getAdapter()==null)
				{
					MyPictureAdapter myPictureAdapter = new MyPictureAdapter(getActivity(),
						false, achievement.getImageList(), 10,"课堂笔记");
					holder.grid_picture.setAdapter(myPictureAdapter);
				}
				else
				{
					MyPictureAdapter myPictureAdapter=(MyPictureAdapter) holder.grid_picture.getAdapter();
					myPictureAdapter.setPicPaths(achievement.getImageList());
				}
			}
			else
				holder.grid_picture.setVisibility(View.GONE);
			if(achievement.getFujianList()!=null && achievement.getFujianList().length()>0)
			{
				holder.list_fujian.setVisibility(View.VISIBLE);
				if(holder.right.getText().length()==0)
					holder.right.setVisibility(View.GONE);
				ArrayList<HashMap<String, Object>> arrayList = new ArrayList<HashMap<String,Object>>();  
		        for(int i=0;i<achievement.getFujianList().length();i++){  
		        	JSONObject item = null;
					try {
						item = (JSONObject) achievement.getFujianList().get(i);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if(item!=null)
					{
			            HashMap<String, Object> tempHashMap = new HashMap<String, Object>();  
			            tempHashMap.put("name", item.optString("name"));
			            tempHashMap.put("url", item.optString("url"));
			            arrayList.add(tempHashMap);  
					}
		              
		        } 
				SimpleAdapter adapter = new SimpleAdapter(getActivity(), arrayList, R.layout.list_item_simple,  
		                new String[]{"name"}, new int[]{R.id.item_textView});  
				holder.list_fujian.setAdapter(adapter);  
				holder.list_fujian.setOnItemClickListener(new OnItemClickListener(){  
			
					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						
						JSONObject item = null;
						try {
							item = (JSONObject) achievement.getFujianList().get(position);
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						if(item!=null)
						{
							AppUtility.downloadAndOpenFile(item.optString("url"),view);
						}
					}     
				});
			}
			else
			{
				holder.list_fujian.setVisibility(View.GONE);
			}
			convertView.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
					if(achievement.getHiddenBtn()!=null && achievement.getHiddenBtn().length()>0)
					{
						ViewHolder holder = (ViewHolder) v.getTag();
						if(holder.ly_hidden.getVisibility()==View.GONE)
						{
							holder.ly_hidden.setVisibility(View.VISIBLE);
							Timer timer = new Timer(); 
							timer.schedule(new Task(holder), 3 * 1000);
						}
					}
					
					if(achievement.getLat()!=0 && !String.valueOf(achievement.getLat()).equals("NaN"))
					{
						Intent contractIntent = new Intent(getActivity(),WebSiteActivity.class);
						String address=achievement.getFraction().split("\n")[0];
						String url=String.format("http://mo.amap.com/?q=%.10f,%.10f&name=%s&dev=1", achievement.getLat(),achievement.getLon(),address);
						contractIntent.putExtra("url",url);
						contractIntent.putExtra("title", achievement.getSubject());
						startActivity(contractIntent);
					}
					if(achievement.getHtmlText()!=null && achievement.getHtmlText().length()>0 && !achievement.getHtmlText().equals("null"))
					{
						Intent contractIntent = new Intent(getActivity(),WebSiteActivity.class);
						contractIntent.putExtra("htmlText",achievement.getHtmlText());
						contractIntent.putExtra("title", achievementDetail.getTitle());
						contractIntent.putExtra("loginUrl", achievementDetail.getLoginUrl());
						startActivity(contractIntent);
					}
					
					if(achievement.getUrl()!=null && achievement.getUrl().length()>0 && !achievement.getUrl().equals("null"))
					{
						Intent intent =new Intent(getActivity(),SchoolDetailActivity.class);
						intent.putExtra("templateName", "成绩");
						int pos=interfaceName.indexOf("?");
						String preUrl=interfaceName;
						if(pos>-1)
							preUrl=interfaceName.substring(0, pos);
						intent.putExtra("interfaceName", preUrl+achievement.getUrl());
						intent.putExtra("title", achievement.getFraction());
						startActivityForResult(intent,101);
					}
					if(achievement.getKechengId()!=null && achievement.getKechengId().length()>0)
					{
						TeacherInfo ti = new TeacherInfo();
						ti.setId(achievement.getKechengId());
						ti.setUsername(achievement.getTeacherUsername());
						ti.setCourseName(achievementDetail.getTitle());
						// Intent intent = new
						// Intent(getActivity(),ClassRoomActivity.class);
						Intent intent = new Intent(getActivity(),
								ClassDetailActivity.class);
						intent.putExtra("teacherInfo", ti);
						startActivity(intent);
					}
					
				}
				
			});
			
			if(achievement.getFraction().indexOf("|||")>0)
			{
				holder.left.setText("");
				holder.right.setText(achievement.getSubject());
				
				LayoutParams leftParams1 = new LayoutParams(0,LayoutParams.WRAP_CONTENT, 0.0f);
				LayoutParams rightParams1 = new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT, 0.0f);
				holder.left.setLayoutParams(leftParams1);
				holder.right.setLayoutParams(rightParams1);
				holder.right.setGravity(Gravity.CENTER);
				holder.right.setTextColor(Color.WHITE);
				final String[] params=achievement.getFraction().split("\\|\\|\\|");
				holder.celllayout.setBackgroundResource(R.color.moban_color_pink);
				convertView.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(final View v) {
						if(params.length==8)
						{
							pay(params[0],params[1],params[2],params[3],params[4],params[5],params[6],params[7]);
							v.setClickable(false);
							new Handler().postDelayed(new Runnable(){  
							    public void run() {  
							    	v.setClickable(true);
							    }  
							 }, 5000);  
						}
					}
				});
			}
			
			return convertView;
		}
		class Task extends TimerTask {
			private ViewHolder holder;
			public Task(ViewHolder h)
			{
				holder=h;
			}
			public void run()
			{
				Message msg = new Message();
				msg.what = 2;
				msg.obj = holder.ly_hidden;
				mHandler.sendMessage(msg);   
			}
		}
		class ViewHolder {
			LinearLayout celllayout;
			TextView left;
			TextView right;
			ImageView hiddenBtn;
			LinearLayout ly_hidden;
			NonScrollableGridView grid_picture;
			NonScrollableListView list_fujian;
			LinearLayout right_layout;
		}
	}
	private void doRequestUrl(String url)
	{
		dialog = DialogUtility.createLoadingDialog(getActivity(),
				"数据处理中...");
		dialog.show();
		long datatime = System.currentTimeMillis();
		String checkCode = PrefUtility.get(Constants.PREF_CHECK_CODE, "");
		JSONObject jo = new JSONObject();
		try {
			jo.put("用户较验码", checkCode);
			jo.put("DATETIME", datatime);
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		String base64Str = Base64.encode(jo.toString().getBytes());
		CampusParameters params = new CampusParameters();
		params.add(Constants.PARAMS_DATA, base64Str);
		int pos=interfaceName.indexOf("?");
		String preUrl=interfaceName;
		if(pos>-1)
			preUrl=interfaceName.substring(0, pos);
		CampusAPI.getSchoolItem(params,preUrl+ url,new RequestListener() {

			@Override
			public void onIOException(IOException e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onError(CampusException e) {
				Log.d(TAG, "----response" + e.getMessage());
				if(dialog != null){
					dialog.dismiss();
				}
				Message msg = new Message();
				msg.what = -1;
				msg.obj = e.getMessage();
				mHandler.sendMessage(msg);
			}

			@Override
			public void onComplete(String response) {
				Log.d(TAG, "----response" + response);
				if(dialog != null){
					dialog.dismiss();
				}
				Message msg = new Message();
				msg.what = 1;
				msg.obj = response;
				mHandler.sendMessage(msg);
			}
		});
	}
	public void pay(String tradeid,String name,String price,String description,String partner,String seller_id,String rsa_private,String notify_url) {
		// 订单
		String orderInfo = getOrderInfo(tradeid,name, description, price,partner,seller_id,notify_url);

		// 对订单做RSA 签名
		String sign = sign(orderInfo,rsa_private);
		try {
			// 仅需对sign 做URL编码
			sign = URLEncoder.encode(sign, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		// 完整的符合支付宝参数规范的订单信息
		final String payInfo = orderInfo + "&sign=\"" + sign + "\"&"
				+ getSignType();

		Runnable payRunnable = new Runnable() {

			@Override
			public void run() {
				// 构造PayTask 对象
				PayTask alipay = new PayTask(getActivity());
				// 调用支付接口，获取支付结果
				String result = alipay.pay(payInfo);

				Message msg = new Message();
				msg.what = 4;
				msg.obj = result;
				mHandler.sendMessage(msg);
			}
		};

		// 必须异步调用
		Thread payThread = new Thread(payRunnable);
		payThread.start();
	}
	public String getOrderInfo(String tradeid,String subject, String body, String price,String partner,String seller_id,String notify_url) {
		// 签约合作者身份ID
		String orderInfo = "partner=" + "\"" + partner + "\"";

		// 签约卖家支付宝账号
		orderInfo += "&seller_id=" + "\"" + seller_id + "\"";

		// 商户网站唯一订单号
		orderInfo += "&out_trade_no=" + "\"" + tradeid + "\"";

		// 商品名称
		orderInfo += "&subject=" + "\"" + subject + "\"";

		// 商品详情
		orderInfo += "&body=" + "\"" + body + "\"";

		// 商品金额
		orderInfo += "&total_fee=" + "\"" + price + "\"";

		// 服务器异步通知页面路径
		orderInfo += "&notify_url=" + "\"" + notify_url
				+ "\"";

		// 服务接口名称， 固定值
		orderInfo += "&service=\"mobile.securitypay.pay\"";

		// 支付类型， 固定值
		orderInfo += "&payment_type=\"1\"";

		// 参数编码， 固定值
		orderInfo += "&_input_charset=\"utf-8\"";

		// 设置未付款交易的超时时间
		// 默认30分钟，一旦超时，该笔交易就会自动被关闭。
		// 取值范围：1m～15d。
		// m-分钟，h-小时，d-天，1c-当天（无论交易何时创建，都在0点关闭）。
		// 该参数数值不接受小数点，如1.5h，可转换为90m。
		orderInfo += "&it_b_pay=\"30m\"";

		// extern_token为经过快登授权获取到的alipay_open_id,带上此参数用户将使用授权的账户进行支付
		// orderInfo += "&extern_token=" + "\"" + extern_token + "\"";

		// 支付宝处理完请求后，当前页面跳转到商户指定页面的路径，可空
		orderInfo += "&return_url=\"m.alipay.com\"";

		// 调用银行卡支付，需配置此参数，参与签名， 固定值 （需要签约《无线银行卡快捷支付》才能使用）
		// orderInfo += "&paymethod=\"expressGateway\"";

		return orderInfo;
	}
	public String sign(String content,String rsa_private) {
		return SignUtils.sign(content, rsa_private);
	}
	public String getSignType() {
		return "sign_type=\"RSA\"";
	}
}
