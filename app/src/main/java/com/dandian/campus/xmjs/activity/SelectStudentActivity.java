package com.dandian.campus.xmjs.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.androidquery.callback.ImageOptions;
import com.dandian.campus.xmjs.CampusApplication;
import com.dandian.campus.xmjs.R;
import com.dandian.campus.xmjs.api.CampusAPI;
import com.dandian.campus.xmjs.api.CampusException;
import com.dandian.campus.xmjs.api.CampusParameters;
import com.dandian.campus.xmjs.api.RequestListener;
import com.dandian.campus.xmjs.base.Constants;
import com.dandian.campus.xmjs.entity.DormEntity;
import com.dandian.campus.xmjs.entity.NewStudent;
import com.dandian.campus.xmjs.entity.User;
import com.dandian.campus.xmjs.util.AppUtility;
import com.dandian.campus.xmjs.util.Base64;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


@SuppressLint({ "NewApi", "HandlerLeak" })
public class SelectStudentActivity extends Activity {

	private ExpandableListView expandableListView;
	public ExpandableAdapter expandableAdapter;
	public List<String> groupList;
	public JSONObject baodaoNumObj;
	public List<List<NewStudent>> childList;
	private LinearLayout initLayout;
	private AQuery aq;
	private Dialog userTypeDialog;
	private ProgressDialog mypDialog;
	private User user;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		user=((CampusApplication)getApplicationContext()).getLoginUserObj();
		setContentView(R.layout.view_dorms);
		expandableListView = (ExpandableListView)findViewById(R.id.contacts);
		initLayout = (LinearLayout) findViewById(R.id.initlayout);
		expandableListView.setVisibility(View.GONE);
		initLayout.setVisibility(View.VISIBLE);
		aq = new AQuery(this);
		aq.id(R.id.back).clicked(new OnClickListener(){

			@Override
			public void onClick(View v) {
				finish();
			}
			
		});
		aq.id(R.id.setting_tv_title).text("选择学生");	
		groupList=new ArrayList<String>();
		childList=new ArrayList<List<NewStudent>>();
		getStudentList();
	}
	private void showProgress(boolean progress) {
		if (progress) {
			initLayout.setVisibility(View.VISIBLE);
			expandableListView.setVisibility(View.GONE);
		} else {
			initLayout.setVisibility(View.GONE);
			expandableListView.setVisibility(View.VISIBLE);
		}
	}
	private void getStudentList()
	{
		showProgress(true);
		String dataResult = "";
		Locale locale = getResources().getConfiguration().locale;
	    String language = locale.getCountry();
		try {
			JSONObject jsonObj = new JSONObject();
			jsonObj.put("action", "getStudentList");
			jsonObj.put("userid", user.getUsername());
			jsonObj.put("language", language);
			dataResult = Base64.encode(jsonObj.toString().getBytes());
		} catch (JSONException e1) {
			e1.printStackTrace();
		}

		CampusParameters params = new CampusParameters();
		params.add(Constants.PARAMS_DATA, dataResult);
		CampusAPI.baodaoHandle(params, new RequestListener() {

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
				msg.what = 0;
				msg.obj = response;
				mHandler.sendMessage(msg);
			}
		});
	}
	@SuppressLint("HandlerLeak")
	public Handler mHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			switch(msg.what){
			case -1:
				showProgress(false);
				showProgress1(false);
				AppUtility.showErrorToast(SelectStudentActivity.this,
						msg.obj.toString());
			case 0 :
				showProgress(false);
				String result = msg.obj.toString();
				try {
					result = new String(Base64.decode(result.getBytes("GBK")));
				} catch (UnsupportedEncodingException e1) {
					e1.printStackTrace();
				}
				try 
				{
					JSONObject jo = new JSONObject(result);
					String loginStatus = jo.optString("结果");
					
					if (!loginStatus.equals("成功")) {
						AppUtility.showToastMsg(SelectStudentActivity.this, loginStatus,1);
					} else 
					{
						baodaoNumObj=jo.optJSONObject("报到完成人数");
						JSONObject dormObject=jo.optJSONObject("班级人员列表");
						String buildingStr=jo.optString("班级字符串");
						String []buildingArray=buildingStr.split(",");
						for(int i=0;i<buildingArray.length;i++)
						{
							String key=buildingArray[i];
							groupList.add(key);
				            List<NewStudent> itemList=new ArrayList<NewStudent>();
				            JSONArray ja=(JSONArray) dormObject.opt(key);
				            if(ja!=null)
				            {
					            for(int j=0;j<ja.length();j++)
					            {
					            	JSONObject item=ja.getJSONObject(j);
					            	NewStudent dorm=new NewStudent(item);
					            	itemList.add(dorm);
					            }
				            }
				            childList.add(itemList);
						}
						
				        expandableAdapter = new ExpandableAdapter(groupList, childList);
						expandableListView.setAdapter(expandableAdapter);
						if(groupList.size()==1)
							expandableListView.expandGroup(0);
					}
					
				} catch (Exception e) {
					
					e.printStackTrace();
				}
				
				break;
			case 1:
				showProgress1(false);
				
				result = msg.obj.toString();
				try {
					result = new String(Base64.decode(result.getBytes("GBK")));
				} catch (UnsupportedEncodingException e1) {
					e1.printStackTrace();
				}
				try 
				{
					JSONObject jo = new JSONObject(result);
					String loginStatus = jo.optString("结果");
					
					if (!loginStatus.equals("成功")) {
						AppUtility.showToastMsg(SelectStudentActivity.this, loginStatus,1);
					} else 
					{
						
						JSONArray bedArray=jo.optJSONArray("床位列表");
						String dormName=jo.optString("dormName");
						ArrayList<DormEntity> bedList=new ArrayList<DormEntity>();
						for(int i=0;i<bedArray.length();i++)
						{
							DormEntity item=new DormEntity(bedArray.getJSONObject(i));
							if(item!=null)
								bedList.add(item);
						}
						DialogAdapter dialogAdapter = new DialogAdapter(bedList);
						if(userTypeDialog==null)
						{
							userTypeDialog = new Dialog(SelectStudentActivity.this, R.style.dialog);
							View view = LayoutInflater.from(getBaseContext()).inflate(
									R.layout.view_bed_select_dialog, null);
							ListView mList = (ListView) view.findViewById(R.id.list);
							
							mList.setAdapter(dialogAdapter);
							Window window = userTypeDialog.getWindow();
							window.setWindowAnimations(R.style.CustomDialog);
							window.setGravity(Gravity.CENTER);
							userTypeDialog.setContentView(view);
						}
						else
						{
							ListView mList = (ListView) userTypeDialog.findViewById(R.id.list);
							mList.setAdapter(dialogAdapter);
						}
						TextView title = (TextView) userTypeDialog.findViewById(R.id.titleText);
						title.setText(dormName+" 中选择一个床位");
						userTypeDialog.show();
					}
					
				} catch (Exception e) {
					
					e.printStackTrace();
				}
				
				
				break;
			case 2:
				showProgress1(false);
				
				result = msg.obj.toString();
				try {
					result = new String(Base64.decode(result.getBytes("GBK")));
				} catch (UnsupportedEncodingException e1) {
					e1.printStackTrace();
				}
				try 
				{
					JSONObject jo = new JSONObject(result);
					String loginStatus = jo.optString("结果");
					
					if (!loginStatus.equals("成功")) {
						AppUtility.showToastMsg(SelectStudentActivity.this, loginStatus,1);
					} else 
					{
						
						Intent it = new Intent();  
						it.putExtra("action", "分配宿舍");
						it.putExtra("完成情况", jo.optInt("完成情况"));
						it.putExtra("显示值", jo.optString("显示值"));
		                setResult(Activity.RESULT_OK, it);  
						finish();
					}
					
				} catch (Exception e) {
					
					e.printStackTrace();
				}
				
				
				break;
			default:
					break;
			}
		}
		
	};
	public class ExpandableAdapter extends BaseExpandableListAdapter {
		List<String> groupList = new ArrayList<String>();
		List<List<NewStudent>> childList = new ArrayList<List<NewStudent>>();
		
		
		public ExpandableAdapter(List<String> group,
				List<List<NewStudent>> child) {
			this.groupList = group;
			this.childList = child;
		}

		public void refresh(List<String> group,
				List<List<NewStudent>> child){
			this.groupList = group;
			this.childList = child;
			notifyDataSetChanged();
		}
		@Override
		public Object getChild(int groupPosition, int childPosition) {
			return this.childList.get(groupPosition).get(childPosition);
		}

		@Override
		public long getChildId(int groupPosition, int childPosition) {
			return childPosition;
		}

		@Override
		public View getChildView(int groupPosition, int childPosition,
				boolean isLastChild, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = LayoutInflater.from(SelectStudentActivity.this)
						.inflate(R.layout.view_expandablelist_child_student, null);
				holder.group = (LinearLayout)convertView.findViewById(R.id.contacts_info1);
				holder.photo = (ImageView) convertView.findViewById(R.id.photo);
				holder.name = (TextView) convertView.findViewById(R.id.child);
				holder.tv_step1=(TextView) convertView.findViewById(R.id.tv_step1);
				holder.tv_step2=(TextView) convertView.findViewById(R.id.tv_step2);
				holder.tv_step3=(TextView) convertView.findViewById(R.id.tv_step3);
				holder.tv_step4=(TextView) convertView.findViewById(R.id.tv_step4);
				holder.iv_step1=(ImageView) convertView.findViewById(R.id.iv_step1);
				holder.iv_step2=(ImageView) convertView.findViewById(R.id.iv_step2);
				holder.iv_step3=(ImageView) convertView.findViewById(R.id.iv_step3);
				holder.iv_step4=(ImageView) convertView.findViewById(R.id.iv_step4);
				holder.tv_idcard=(TextView)convertView.findViewById(R.id.tv_idcard);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			final NewStudent contactsMember = childList.get(groupPosition)
					.get(childPosition);
			
			if (contactsMember != null) {
				
				String url=contactsMember.getPicImage();
				//Log.d(TAG,"---------------------->contactsMember.getUserImage():"+url);
				ImageOptions options = new ImageOptions();
				options.round = 80;
				options.fallback = R.drawable.man;
				aq.id(holder.photo).image(url, options);
				holder.name.setText(contactsMember.getName());
				if(contactsMember.getStudentID().length()>0)
					holder.tv_idcard.setText(contactsMember.getStudentID());
				else
					holder.tv_idcard.setText("");
				if(contactsMember.getStatus().length()>0)
				{
					holder.tv_step1.setVisibility(View.VISIBLE);
					holder.iv_step1.setVisibility(View.VISIBLE);
					holder.tv_step1.setText("身份验证");
					if(contactsMember.getStatus().equals("1"))
						holder.iv_step1.setImageResource(R.drawable.rightmark);
					else
						holder.iv_step1.setImageResource(R.drawable.login_delete_bg_sel);
				}
				else
				{
					holder.tv_step1.setVisibility(View.GONE);
					holder.iv_step1.setVisibility(View.GONE);
				}
				if(contactsMember.getPayment().length()>0)
				{
					holder.tv_step2.setVisibility(View.VISIBLE);
					holder.iv_step2.setVisibility(View.VISIBLE);
					holder.tv_step2.setText("缴费");
					if(contactsMember.getPayment().equals("1"))
						holder.iv_step2.setImageResource(R.drawable.rightmark);
					else
						holder.iv_step2.setImageResource(R.drawable.login_delete_bg_sel);
				}
				else
				{
					holder.tv_step2.setVisibility(View.GONE);
					holder.iv_step2.setVisibility(View.GONE);
				}
				if(contactsMember.getCollect().length()>0)
				{
					holder.tv_step3.setVisibility(View.VISIBLE);
					holder.iv_step3.setVisibility(View.VISIBLE);
					holder.tv_step3.setText("收取材料");
					if(contactsMember.getCollect().equals("1"))
						holder.iv_step3.setImageResource(R.drawable.rightmark);
					else
						holder.iv_step3.setImageResource(R.drawable.login_delete_bg_sel);
				}
				else
				{
					holder.tv_step3.setVisibility(View.GONE);
					holder.iv_step3.setVisibility(View.GONE);
				}
				if(contactsMember.getOnecard().length()>0)
				{
					holder.tv_step4.setVisibility(View.VISIBLE);
					holder.iv_step4.setVisibility(View.VISIBLE);
					holder.tv_step4.setText("一卡通");
					if(contactsMember.getOnecard().equals("1"))
						holder.iv_step4.setImageResource(R.drawable.rightmark);
					else
						holder.iv_step4.setImageResource(R.drawable.login_delete_bg_sel);
				}
				else
				{
					holder.tv_step4.setVisibility(View.GONE);
					holder.iv_step4.setVisibility(View.GONE);
				}
				holder.group.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) 
					{
						/*
						Intent resultIntent = new Intent();
						Bundle bundle = new Bundle();
						bundle.putString("result", contactsMember.getId());
						resultIntent.putExtras(bundle);
						setResult(RESULT_OK, resultIntent);
						finish();
						*/
						Intent intent=new Intent(SelectStudentActivity.this,BaodaoHandleActivity.class);
						intent.putExtra("ID", contactsMember.getId());
						startActivityForResult(intent,101);
					}
				});
				
			}
			return convertView;
		}

		@Override
		public int getChildrenCount(int groupPosition) {
			return this.childList.get(groupPosition).size();
		}

		@Override
		public Object getGroup(int groupPosition) {
			return this.groupList.get(groupPosition);
		}

		@Override
		public int getGroupCount() {
			return this.groupList.size();
		}

		@Override
		public long getGroupId(int groupPosition) {
			return groupPosition;
		}

		@Override
		public View getGroupView(final int groupPosition, final boolean isExpanded,
				View convertView, ViewGroup parent) {
			ViewHolder holder;
			if(convertView == null){
				holder = new ViewHolder();
				convertView = LayoutInflater.from(SelectStudentActivity.this).inflate(
						R.layout.view_expandablelist_group, null);
				holder.groupTV = (TextView) convertView
						.findViewById(R.id.group_name);
				holder.countTV = (TextView) convertView
						.findViewById(R.id.group_count);
				holder.groupIV = (ImageView) convertView.findViewById(R.id.group_image);
				holder.showMemberBT = (TextView)convertView.findViewById(R.id.show_member);
				
				convertView.setTag(holder);
			}else{
				holder = (ViewHolder) convertView.getTag();
			}
			
				holder.showMemberBT.setVisibility(View.GONE);
				holder.groupIV.setVisibility(View.GONE);
			
			holder.groupTV.setText(this.groupList.get(groupPosition));
			String num=baodaoNumObj.optString(this.groupList.get(groupPosition));
			if(num.length()==0)
				num="0";
			holder.countTV.setText("报到情况:"+num+"/"+String.valueOf(this.childList.get(groupPosition)
					.size()));
			return convertView;
		}

		@Override
		public boolean hasStableIds() {
			return false;
		}

		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			return false;
		}

	}
	
	class ViewHolder {
		LinearLayout group;
		ImageView photo,groupIV,callIV,iv_step1,iv_step2,iv_step3,iv_step4;
		TextView name,groupTV,countTV,lastContentTV;
		TextView showMemberBT,tv_step1,tv_step2,tv_step3,tv_idcard,tv_step4;
	}
	private void showProgress1(final boolean show) {
		
		if(show)
		{
		if(mypDialog==null)
			mypDialog=new ProgressDialog(this);
        //实例化
        mypDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        //设置ProgressDialog 标题
        mypDialog.setMessage("处理中..");
        //设置ProgressDialog 提示信息
        //设置ProgressDialog 的一个Button
        mypDialog.setIndeterminate(false);
        //设置ProgressDialog 的进度条是否不明确
        mypDialog.setCancelable(false);
        //设置ProgressDialog 是否可以按退回按键取消
        mypDialog.show();
		}
		else
		{
			if(mypDialog!=null)
				mypDialog.cancel();
		}
	}
	private void getBedList(String dormName)
	{
		showProgress1(true);
		String dataResult = "";
		Locale locale = getResources().getConfiguration().locale;
	    String language = locale.getCountry();
		try {
			JSONObject jsonObj = new JSONObject();
			jsonObj.put("action", "getBedList");
			jsonObj.put("dormName", dormName);
			jsonObj.put("language", language);
			dataResult = Base64.encode(jsonObj.toString().getBytes());
		} catch (JSONException e1) {
			e1.printStackTrace();
		}

		CampusParameters params = new CampusParameters();
		params.add(Constants.PARAMS_DATA, dataResult);
		CampusAPI.baodaoHandle(params, new RequestListener() {

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
				msg.what = 1;
				msg.obj = response;
				mHandler.sendMessage(msg);
			}
		});
		
	}
	public class DialogAdapter extends BaseAdapter {
		ArrayList<DormEntity> arrayData;

		public DialogAdapter(ArrayList<DormEntity> array) {
			this.arrayData = array;
		}

		@Override
		public int getCount() {
			return arrayData == null ? 0 : arrayData.size();
		}

		@Override
		public Object getItem(int position) {
			return arrayData.get(position);
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
						R.layout.view_expandablelist_child, null);
				holder.group = (LinearLayout)convertView.findViewById(R.id.contacts_info1);
				holder.photo = (ImageView) convertView.findViewById(R.id.photo);
				holder.name = (TextView) convertView.findViewById(R.id.child);
				holder.lastContentTV = (TextView)convertView.findViewById(R.id.signature);
				holder.callIV = (ImageView) convertView.findViewById(R.id.callIV);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			
			final DormEntity contactsMember = arrayData.get(position);
			if (contactsMember != null) 
			{
				
				holder.lastContentTV.setVisibility(View.GONE);
				holder.lastContentTV.setText("");
				holder.callIV.setVisibility(View.GONE);
				String url=contactsMember.getUrl();
				//Log.d(TAG,"---------------------->contactsMember.getUserImage():"+url);
				ImageOptions options = new ImageOptions();
				options.round = 80;
				options.fallback = R.drawable.botton_school_sel;
				aq.id(holder.photo).image(url, options);
				holder.name.setText(contactsMember.getRoomName()+" "+contactsMember.getBanji());
				holder.group.setOnClickListener(new OnClickListener()
				{
					@Override
					public void onClick(View v) {
						if(contactsMember.getBanji().equals("[空闲]"))
						{
							
							userTypeDialog.dismiss();
						}
						else
							AppUtility.showErrorToast(SelectStudentActivity.this,"此床位已被占用");
					}
				});
			}
			return convertView;
			
		}

	}
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
		case 101:
			if(resultCode == RESULT_OK){
				groupList.clear();
				childList.clear();
				getStudentList();
			}
			break;
		}
    }
   
}
