package com.dandian.campus.xmjs.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
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
import android.widget.ListView;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.dandian.campus.xmjs.CampusApplication;
import com.dandian.campus.xmjs.R;
import com.dandian.campus.xmjs.api.CampusAPI;
import com.dandian.campus.xmjs.api.CampusException;
import com.dandian.campus.xmjs.api.CampusParameters;
import com.dandian.campus.xmjs.api.RequestListener;
import com.dandian.campus.xmjs.base.Constants;
import com.dandian.campus.xmjs.util.AppUtility;
import com.dandian.campus.xmjs.util.Base64;
import com.dandian.campus.xmjs.util.PrefUtility;
import com.dandian.campus.xmjs.widget.HorizontalProgressBarWithNumber;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Locale;

/**
 * 
 * #(c) ruanyun PocketCampus <br/>
 * 
 * 版本说明: $id:$ <br/>
 * 
 * 功能说明: 消息列表界面
 * 
 * <br/>
 * 创建说明: 2013-12-9 下午12:51:50 zhuliang 创建文件<br/>
 * 
 * 修改历史:<br/>
 * 
 */
public class SubmitDataActivity extends Activity implements OnItemClickListener {
	private String TAG= "ChatFriendActivity";
	private ListView mList;
	private MessageAdapter mAdapter;
	static Button menu;
	static LinearLayout layout_menu;
	private JSONArray listData;
	public static boolean isruning = false;
	AQuery aq;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "----------------onCreate-----------------------");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_submit);
		TextView title = (TextView) findViewById(R.id.tv_title);
		title.setVisibility(View.VISIBLE);
		title.setText(getString(R.string.submitInfo));
		mList=(ListView)findViewById(R.id.message_list);
		mList.setOnItemClickListener(this);
		listData=new JSONArray();
		mAdapter=new MessageAdapter(this);
		mList.setAdapter(mAdapter);
		Button btn_back= (Button) findViewById(R.id.btn_back);
		btn_back.setBackgroundResource(R.drawable.relogin);
		LinearLayout relogin = (LinearLayout) findViewById(R.id.layout_back);
		relogin.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				((CampusApplication)getApplicationContext()).reLogin_newStudent();
			}
			
		});
		getListData();
	
	}
	
	public void getListData() {
		
		Log.d(TAG, "--------"+String.valueOf(new Date().getTime()));
		long datatime =System.currentTimeMillis();
		String checkCode=PrefUtility.get(Constants.PREF_CHECK_CODE, "");
		Log.d(TAG, "----------datatime:"+datatime);
		Log.d(TAG, "----------checkCode:"+checkCode+"++");
		JSONObject jo = new JSONObject();
		Locale locale = getResources().getConfiguration().locale;
	    String language = locale.getCountry();
	    String thisVersion = CampusApplication.getVersion();
		try {
			jo.put("用户较验码", checkCode);
			jo.put("DATETIME", datatime);
			jo.put("language", language);
			jo.put("当前版本", thisVersion);
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		String base64Str = Base64.encode(jo.toString().getBytes());
		Log.d(TAG, "---------------->base64Str:" + base64Str);
		CampusParameters params = new CampusParameters();
		params.add(Constants.PARAMS_DATA, base64Str);
		CampusAPI.getNeedSubmit(params, new RequestListener() {
			
			@Override
			public void onIOException(IOException e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onError(CampusException e) {
				Log.d(TAG, "----response"+e.getMessage());
				Message msg=new Message();
				msg.what=-1;
				msg.obj= e.getMessage();
				mHandler.sendMessage(msg);
			}
			
			@Override
			public void onComplete(String response) {
				Log.d(TAG, "----response"+response);
				
				Message msg=new Message();
				msg.what=0;
				msg.obj= response;
				mHandler.sendMessage(msg);
			}
		});
	}
	
	

	/**
	 * 
	 * #(c) ruanyun PocketCampus <br/>
	 * 
	 * 版本说明: $id:$ <br/>
	 * 
	 * 功能说明: 消息list适配器
	 * 
	 * <br/>
	 * 创建说明: 2013-12-9 下午1:10:31 zhuliang 创建文件<br/>
	 * 
	 * 修改历史:<br/>
	 * 
	 */
	class MessageAdapter extends BaseAdapter {
		
		private Context context;
		private LayoutInflater mInflater;

		public MessageAdapter(Context context) {
			this.context = context;
			
			this.mInflater = LayoutInflater.from(this.context);
		}

		@Override
		public int getCount() {
			return listData.length();
		}

		@Override
		public Object getItem(int position) {
			JSONObject obj=null;
			try {
				obj=(JSONObject) listData.get(position);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return obj;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = mInflater.inflate(R.layout.view_submit_list, null);
				holder.photo = (ImageView) convertView.findViewById(R.id.photo);
				holder.name = (TextView) convertView.findViewById(R.id.name);
				holder.tv_finish_persent = (TextView) convertView
						.findViewById(R.id.tv_finish_persent);
				holder.pb_finish_persent = (HorizontalProgressBarWithNumber) convertView.findViewById(R.id.pb_finish_persent);
				holder.iv_shenhe=(ImageView) convertView.findViewById(R.id.iv_shenhe);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			JSONObject obj = (JSONObject) getItem(position);
			holder.name.setText(obj.optString("标题"));
			holder.pb_finish_persent.setProgress(obj.optInt("完成度"));
			holder.tv_finish_persent.setText(obj.optString("完成度文字"));
			aq = new AQuery(convertView);
			
			aq.id(holder.photo).image(obj.optString("图标"),true,true,0,R.drawable.ic_launcher);
			if(obj.optString("审核状态")!=null && obj.optString("审核状态").length()>0)
			{
				holder.iv_shenhe.setVisibility(View.VISIBLE);
				if(obj.optString("审核状态").equals("已审核"))
					holder.iv_shenhe.setImageResource(R.drawable.needsubmit_hasreview);
				else if(obj.optString("审核状态").equals("待审核"))
					holder.iv_shenhe.setImageResource(R.drawable.needsubmit_waitreview);
				else if(obj.optString("审核状态").equals("未完成") || obj.optString("审核状态").equals("被拒绝"))
					holder.iv_shenhe.setImageResource(R.drawable.needsubmit_unfinish);
				else
					holder.iv_shenhe.setVisibility(View.GONE);
			}
			
			return convertView;
		}
	}

	class ViewHolder {
		ImageView photo;
		TextView name;
		TextView tv_finish_persent;
		HorizontalProgressBarWithNumber pb_finish_persent;
		ImageView iv_shenhe;
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View view, int position,
			long arg3) {
		JSONObject obj = (JSONObject) mList.getItemAtPosition(position);
		Intent intent = new Intent(this, SchoolDetailActivity.class);
		intent.putExtra("templateName", "调查问卷");
		intent.putExtra("interfaceName", obj.optString("接口地址"));
		intent.putExtra("title", obj.optString("标题"));
		intent.putExtra("display", obj.optString("标题"));
		intent.putExtra("autoClose", "是");
		intent.putExtra("status", "进行中");
		startActivityForResult(intent, 101);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (resultCode) { //resultCode为回传的标记，我在B中回传的是RESULT_OK
		case 1:
			getListData();
		    break;
		default:
		    break;
		}
	}
	
	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case -1:
				
				AppUtility.showErrorToast(SubmitDataActivity.this, msg.obj.toString());
				break;
			case 0:

				String result = msg.obj.toString();
				String resultStr = "";
				if (AppUtility.isNotEmpty(result)) {
					try {
						resultStr = new String(Base64.decode(result.getBytes("GBK")));
						Log.d(TAG, "----resultStr:"+resultStr);
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
				}

				if (AppUtility.isNotEmpty(resultStr)) {
					
					try {
						listData = new JSONArray(resultStr);

					} catch (JSONException e) {
	
						e.printStackTrace();
					}
					mAdapter.notifyDataSetChanged();
				}
				break;
			}
		}
	};
	
}
