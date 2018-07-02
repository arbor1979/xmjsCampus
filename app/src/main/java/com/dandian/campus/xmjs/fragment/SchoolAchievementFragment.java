package com.dandian.campus.xmjs.fragment;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.dandian.campus.xmjs.R;
import com.dandian.campus.xmjs.R.drawable;
import com.dandian.campus.xmjs.activity.SchoolActivity;
import com.dandian.campus.xmjs.activity.SchoolDetailActivity;
import com.dandian.campus.xmjs.activity.WebSiteActivity;
import com.dandian.campus.xmjs.api.CampusAPI;
import com.dandian.campus.xmjs.api.CampusException;
import com.dandian.campus.xmjs.api.CampusParameters;
import com.dandian.campus.xmjs.api.RequestListener;
import com.dandian.campus.xmjs.base.Constants;
import com.dandian.campus.xmjs.entity.AchievementItem;
import com.dandian.campus.xmjs.entity.AchievementItem.Achievement;
import com.dandian.campus.xmjs.util.AppUtility;
import com.dandian.campus.xmjs.util.Base64;
import com.dandian.campus.xmjs.util.DialogUtility;
import com.dandian.campus.xmjs.util.PrefUtility;


/**
 * 成绩
 */
public class SchoolAchievementFragment extends Fragment {
	private String TAG = "SchoolAchievementFragment";
	private ListView myListview;
	private Button btnLeft;
	private TextView tvTitle,tvRight;
	private LinearLayout lyLeft,lyRight;
	private LinearLayout loadingLayout;
	private LinearLayout contentLayout;
	private LinearLayout failedLayout;
	private LinearLayout emptyLayout;
	private AchievementItem achievementItem;
	private String interfaceName,title;
	private LayoutInflater inflater;
	private AchieveAdapter adapter;
	private List<Achievement> achievements = new ArrayList<Achievement>();
	private Dialog dialog;
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
						JSONObject jo = new JSONObject(resultStr);
						String res = jo.optString("结果");
						if(AppUtility.isNotEmpty(res)){
							AppUtility.showToastMsg(getActivity(), res);
						}
						else{
							achievementItem = new AchievementItem(jo);
							Log.d(TAG, "--------noticesItem.getNotices().size():"
									+ achievementItem.getAchievements().size());
							achievements = achievementItem.getAchievements();
							adapter.notifyDataSetChanged();
							tvTitle.setText(achievementItem.getTitle());
							if(achievementItem.getRightButton()!=null && achievementItem.getRightButton().length()>0)
							{
								tvRight.setText(achievementItem.getRightButton());
								tvRight.setVisibility(View.VISIBLE);
								lyRight.setOnClickListener(new OnClickListener() {

									@Override
									public void onClick(View v) {
										if(achievementItem.getSubmitTarget().equals("是"))
										{
											submitButtonClick(achievementItem.getRightButtonURL());
										}
										else
										{
											Intent intent =new Intent(getActivity(),SchoolDetailActivity.class);
											intent.putExtra("templateName", "调查问卷");
											int pos=interfaceName.indexOf("?");
											String preUrl=interfaceName;
											if(pos>-1)
												preUrl=interfaceName.substring(0, pos);
											intent.putExtra("interfaceName", preUrl+achievementItem.getRightButtonURL());
											intent.putExtra("title", title);
											intent.putExtra("status", "进行中");
											intent.putExtra("autoClose", "是");
											startActivityForResult(intent,101);
										}
									}
								});
							}
							else
							{
								tvRight.setVisibility(View.GONE);
								lyRight.setOnClickListener(null);
							}
								
						}
					} 
					catch (UnsupportedEncodingException e) {

						e.printStackTrace();
						AppUtility.showErrorToast(getActivity(),e.getLocalizedMessage());
					}
					catch (JSONException e) {
						
						e.printStackTrace();
						AppUtility.showErrorToast(getActivity(), e.getLocalizedMessage());
					}
				}else{
					showFetchFailedView();
					
				}

				
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
	public static final SchoolAchievementFragment newInstance(String title,String interfaceName)
	{
		SchoolAchievementFragment fragment = new SchoolAchievementFragment();
		Bundle bundle = new Bundle();
		bundle.putString("title",title);
		bundle.putString("interfaceName", interfaceName);
		fragment.setArguments(bundle);

		return fragment ;
	}
	@Override
	public void onCreate( Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		interfaceName=getArguments().getString("interfaceName");
		title=getArguments().getString("title");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		this.inflater = inflater;
		View view = inflater.inflate(R.layout.school_listview_fragment,
				container, false);
		myListview = (ListView) view.findViewById(R.id.my_listview);
		btnLeft = (Button) view.findViewById(R.id.btn_left);
		tvTitle = (TextView) view.findViewById(R.id.tv_title);
		tvRight = (TextView) view.findViewById(R.id.tv_right);
		lyRight = (LinearLayout) view.findViewById(R.id.layout_btn_right);
		lyLeft = (LinearLayout) view.findViewById(R.id.layout_btn_left);
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
				Message msg = new Message();
				msg.what = -1;
				msg.obj = e.getMessage();
				mHandler.sendMessage(msg);
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
	
	@SuppressLint({ "DefaultLocale", "NewApi" })
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
						R.layout.school_achievement_or_question_item, parent,
						false);
				holder = new ViewHolder();

				holder.icon = (ImageView) convertView
						.findViewById(R.id.iv_icon);
				holder.title = (TextView) convertView
						.findViewById(R.id.tv_title);
				holder.total = (TextView) convertView
						.findViewById(R.id.thieDescription);
				holder.rank = (TextView) convertView
						.findViewById(R.id.tv_right);
				
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			final Achievement achievement = (Achievement) getItem(position);
			AQuery aq = new AQuery(convertView);
			String imagurl = achievement.getIcon();
			Log.d(TAG, "----imagurl:" + imagurl);
			if (imagurl != null && !imagurl.equals("")) {
				aq.id(holder.icon).image(imagurl);
			}
	
			holder.title.setText(achievement.getTitle());
			holder.total.setText(achievement.getTotal());
			holder.rank.setText(achievement.getRank());
			if(achievement.getThecolor()!=null && achievement.getThecolor().length()>0)
			{
				if(achievement.getThecolor().toLowerCase().equals("red"))
					holder.total.setBackground(getResources().getDrawable(R.drawable.school_achievement_red));
				else if(achievement.getThecolor().toLowerCase().equals("blue"))
					holder.total.setBackground(getResources().getDrawable(R.drawable.school_achievement_blue));
				else if(achievement.getThecolor().toLowerCase().equals("brown"))
					holder.total.setBackground(getResources().getDrawable(R.drawable.school_achievement_brown));
				else if(achievement.getThecolor().toLowerCase().equals("pink"))
					holder.total.setBackground(getResources().getDrawable(R.drawable.school_achievement_pink));
				else if(achievement.getThecolor().toLowerCase().equals("goldenrod"))
					holder.total.setBackground(getResources().getDrawable(R.drawable.school_achievement_goldenrod));
				else if(achievement.getThecolor().toLowerCase().equals("blueviolet"))
					holder.total.setBackground(getResources().getDrawable(R.drawable.school_achievement_blueviolet));
				else
					holder.total.setBackground(getResources().getDrawable(R.drawable.school_achievement_bg));
			}
			convertView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					String DetailUrl = achievement.getDetailUrl();
					if (AppUtility.isNotEmpty(DetailUrl)) {
						Log.d(TAG,"----notice.getEndUrl():"+ achievement.getDetailUrl());
						
						if(DetailUrl.length()>0 && !DetailUrl.equals("null"))
						{
							Intent intent =null;
							if(achievement.getTemplateName()==null || achievement.getTemplateName().length()==0)
							{
								intent=new Intent(getActivity(),SchoolDetailActivity.class);
								intent.putExtra("templateName", "成绩");
							}
							else
							{
								if(achievement.getTemplateGrade().equals("main"))
									intent=new Intent(getActivity(),SchoolActivity.class);
								else
									intent=new Intent(getActivity(),SchoolDetailActivity.class);
								intent.putExtra("templateName", achievement.getTemplateName());
							}
							int pos=interfaceName.indexOf("?");
							String preUrl=interfaceName;
							if(pos>-1)
								preUrl=interfaceName.substring(0, pos);
							intent.putExtra("interfaceName", preUrl+DetailUrl);
							intent.putExtra("title", title);
							startActivityForResult(intent,101);
						}
					}
				}
			});
	
			return convertView;
		}

		class ViewHolder {
			ImageView icon;
			TextView title;
			TextView total;
			TextView rank;
		}
		
	}
}
