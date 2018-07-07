package com.dandian.campus.xmjs.fragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.androidquery.callback.ImageOptions;
import com.dandian.campus.xmjs.CampusApplication;
import com.dandian.campus.xmjs.R;
import com.dandian.campus.xmjs.activity.SchoolDetailActivity;
import com.dandian.campus.xmjs.api.CampusAPI;
import com.dandian.campus.xmjs.api.CampusException;
import com.dandian.campus.xmjs.api.CampusParameters;
import com.dandian.campus.xmjs.api.RequestListener;
import com.dandian.campus.xmjs.base.Constants;
import com.dandian.campus.xmjs.entity.Blog;
import com.dandian.campus.xmjs.entity.BlogsItem;
import com.dandian.campus.xmjs.entity.User;
import com.dandian.campus.xmjs.util.AppUtility;
import com.dandian.campus.xmjs.util.Base64;
import com.dandian.campus.xmjs.util.PrefUtility;
import com.dandian.campus.xmjs.widget.SegmentedGroup;
import com.dandian.campus.xmjs.widget.XListView;
import com.dandian.campus.xmjs.widget.XListView.IXListViewListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 通知
 */
public class SchoolBlogFragment extends Fragment implements IXListViewListener,RadioGroup.OnCheckedChangeListener {
	private String TAG = "SchoolNoticeFragment";
	private XListView myListview;
	private Button btnLeft;
	private TextView tvTitle;
	private LinearLayout lyLeft;
	private LinearLayout loadingLayout;
	private LinearLayout contentLayout;
	private LinearLayout failedLayout;
	private LinearLayout emptyLayout;
	
	private String interfaceName,title;
	private LayoutInflater inflater;
	private NoticeAdapter adapter;
	private List<Blog> noticesList=new ArrayList<Blog>();
	RadioButton btn21,btn23;
	private int pagesize=20;
	private boolean isLoading=false;
	AQuery aq;
	Button btn_right;
	LinearLayout lyRight;
	private User user;
	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case -1:
				showProgress(false);
				showFetchFailedView();
				AppUtility.showErrorToast(getActivity(), msg.obj.toString());
				break;

			case 0:
				showProgress(false);
				
				String result = msg.obj.toString();
				String resultStr=null;
				//byte[] contact64byte = null;
				if (AppUtility.isNotEmpty(result)) {
					try {
						resultStr = new String(Base64.decode(result
								.getBytes("GBK")));
					} catch (UnsupportedEncodingException e) {
						showFetchFailedView();
						e.printStackTrace();
					}
				}else{
					showFetchFailedView();
				}
				//resultStr=ZLibUtils.decompress(contact64byte);
				if (AppUtility.isNotEmpty(resultStr)) {
					try {
						JSONObject jo = new JSONObject(resultStr);
						String res = jo.optString("结果");
						if(AppUtility.isNotEmpty(res)){
							AppUtility.showToastMsg(getActivity(), res);
						}else{
							final BlogsItem noticesItem = new BlogsItem(jo);
							Log.d(TAG, "--------noticesItem.getNotices().size():"
									+ noticesItem.getNotices().size());
							List<Blog> notices = noticesItem.getNotices();
							
							myListview.stopLoadMore();
							if(noticesItem.getTitle()!=null && noticesItem.getTitle().length()>0)
								tvTitle.setText(noticesItem.getTitle());
							for(Blog item:notices)
							{
								noticesList.add(item);
							}
							//if(notices.size()>0)
							adapter.notifyDataSetChanged();
							if(notices.size()<pagesize)
							{
								myListview.setPullLoadEnable(false);
							}
							else
								myListview.setPullLoadEnable(true);
							if(noticesItem.getRightButton()!=null && noticesItem.getRightButton().length()>0)
							{
								btn_right.setVisibility(View.VISIBLE);
								btn_right.setTextSize(13);
								btn_right.setText(noticesItem.getRightButton());
								lyRight.setOnClickListener(new OnClickListener(){
	
									@Override
									public void onClick(View v) {
										Intent intent =new Intent(getActivity(),SchoolDetailActivity.class);
										intent.putExtra("templateName", "调查问卷");
										int pos=interfaceName.indexOf("?");
										String preUrl=interfaceName;
										if(pos>-1)
											preUrl=interfaceName.substring(0, pos);
										intent.putExtra("interfaceName", preUrl+noticesItem.getRightButtonUrl());
										intent.putExtra("title", title);
										intent.putExtra("status", "进行中");
										intent.putExtra("autoClose", "是");
										startActivityForResult(intent, 101);
									}
								
								});
							}
							
						}
					} catch (JSONException e) {
						showFetchFailedView();
						e.printStackTrace();
					} 
				}else{
					showFetchFailedView();
				}
				break;
			case 1:
				result = msg.obj.toString();
				resultStr=null;
				//byte[] contact64byte = null;
				if (AppUtility.isNotEmpty(result)) {
					try {
						resultStr = new String(Base64.decode(result
								.getBytes("GBK")));
					} catch (UnsupportedEncodingException e) {
						showFetchFailedView();
						e.printStackTrace();
					}
				}
				//resultStr=ZLibUtils.decompress(contact64byte);
				if (AppUtility.isNotEmpty(resultStr)) 
				{
					try {
						JSONObject jo = new JSONObject(resultStr);
						String res = jo.optString("结果");
						if(AppUtility.isNotEmpty(res))
						{
							AppUtility.showToastMsg(getActivity(), res);
						}
						else
						{
							for(Blog item:noticesList)
							{
								if(item.getId()==jo.optInt("编号"))
								{
									noticesList.remove(item);
									adapter.notifyDataSetChanged();
									break;
								}
							}
						}	
						
					}
					catch (JSONException e) {
						e.printStackTrace();
					} 
				}
			break;
		}
		}
	};
	public SchoolBlogFragment() {
		
	}
	public static final Fragment newInstance(String title, String interfaceName){
    	Fragment fragment = new SchoolBlogFragment();
    	Bundle bundle = new Bundle();
    	bundle.putString("title", title);
    	bundle.putString("interfaceName", interfaceName);
    	fragment.setArguments(bundle);
    	return fragment;
    }


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		title=getArguments().getString("title");

		interfaceName=getArguments().getString("interfaceName");
		this.inflater = inflater;
		View view = inflater.inflate(R.layout.school_xlistview_fragment,
				container, false);
		user=((CampusApplication)getActivity().getApplicationContext()).getLoginUserObj();
		aq= new AQuery(view);
		myListview = (XListView) view.findViewById(R.id.my_listview);
		myListview.setDivider(getResources().getDrawable(R.color.transparent));
		btnLeft = (Button) view.findViewById(R.id.btn_left);
		lyLeft = (LinearLayout) view.findViewById(R.id.layout_btn_left);
		tvTitle=(TextView) view.findViewById(R.id.tv_title);
		loadingLayout = (LinearLayout) view.findViewById(R.id.data_load);
		contentLayout = (LinearLayout) view.findViewById(R.id.content_layout);
		failedLayout = (LinearLayout) view.findViewById(R.id.empty_error);
		emptyLayout = (LinearLayout) view.findViewById(R.id.empty);
		myListview.setEmptyView(emptyLayout);
		myListview.setPullRefreshEnable(false);
		myListview.setPullLoadEnable(true);
		myListview.setXListViewListener(this);
		btnLeft.setVisibility(View.VISIBLE);
		btnLeft.setCompoundDrawablesWithIntrinsicBounds(
				R.drawable.bg_btn_left_nor, 0, 0, 0);
		adapter = new NoticeAdapter();
		tvTitle.setText(title);
		myListview.setAdapter(adapter);
		lyLeft.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				getActivity().finish();
			}
		});
		//重新加载
		failedLayout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				getNoticesList(true);
			}
		});
		btn_right=(Button)view.findViewById(R.id.btn_right);
		lyRight = (LinearLayout) view.findViewById(R.id.layout_btn_right);
		
		SegmentedGroup segmented2 = (SegmentedGroup) view.findViewById(R.id.segmentedGroup3);
		segmented2.setTintColor(Color.DKGRAY);
		segmented2.setVisibility(View.VISIBLE);
		tvTitle.setVisibility(View.GONE);
		segmented2.setOnCheckedChangeListener(this);
		btn21 = (RadioButton) view.findViewById(R.id.button21);
		btn23 = (RadioButton) view.findViewById(R.id.button23);
		if(!btn21.isChecked() && !btn23.isChecked())
			btn21.setChecked(true);
		return view;
	}


	
	private void getNoticesList(boolean flag)
	{
		showProgress(flag);
		isLoading=true;
		long datatime = System.currentTimeMillis();
		String checkCode = PrefUtility.get(Constants.PREF_CHECK_CODE, "");
		String user_id=PrefUtility.get(Constants.PREF_LOGIN_NAME, "");
		JSONObject jo = new JSONObject();
		Locale locale = getResources().getConfiguration().locale;
	    String language = locale.getCountry();
	    String fanwei="全部";
	    if(btn23.isChecked())
	    	fanwei="我的";
		try {
			jo.put("用户较验码", checkCode);
			jo.put("DATETIME", datatime);
			jo.put("language", language);
			jo.put("start", noticesList.size());
			jo.put("pagesize", pagesize);
			jo.put("fanwei", fanwei);
			jo.put("userId", user_id);
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
				isLoading=false;
			}

			@Override
			public void onError(CampusException e) {
				Log.d(TAG, "----response" + e.getMessage());
				isLoading=false;
			}

			@Override
			public void onComplete(String response) {
				Log.d(TAG, "----response" + response);
				isLoading=false;
				Message msg = new Message();
				msg.what = 0;
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
	
	class NoticeAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return noticesList.size();
		}

		@Override
		public Object getItem(int position) {
			return noticesList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;

			if (null == convertView) {
				convertView = inflater.inflate(R.layout.school_liuyan_item,
						parent, false);
				holder = new ViewHolder();

				holder.time = (TextView) convertView.findViewById(R.id.tv_time);
				holder.headImage = (ImageView) convertView.findViewById(R.id.iv_head);
				holder.content = (TextView) convertView.findViewById(R.id.tv_content);
				holder.name = (TextView) convertView.findViewById(R.id.tv_name);
				holder.ib_delete=(ImageButton)convertView.findViewById(R.id.ib_delete);
				if(user.getUserType().equals("老师"))
				{
					holder.ib_delete.setImageResource(R.drawable.reply);
				}
				else
					holder.ib_delete.setImageResource(R.drawable.delete);
				holder.time_answer = (TextView) convertView.findViewById(R.id.tv_time_answer);
				holder.headImage_answer = (ImageView) convertView.findViewById(R.id.iv_head_answer);
				holder.content_answer = (TextView) convertView.findViewById(R.id.tv_content_answer);
				holder.name_answer = (TextView) convertView.findViewById(R.id.tv_name_answer);
				
				
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			final Blog notice = (Blog) getItem(position);
			
			//holder.openDetail.setBackgroundColor(Color.WHITE);
			ImageOptions option=new ImageOptions();
			option.targetWidth=230;
			option.round=115;
			aq.id(holder.headImage).image(notice.getAvater(),option);
			holder.name.setText(notice.getPoster());
			holder.content.setText(notice.getContent());
			holder.time.setText(notice.getPosttime());
			
			String userid=PrefUtility.get(Constants.PREF_LOGIN_NAME, "");
			if(user.getUserType().equals("老师"))
			{
				if(notice.getAnswerContent()==null || notice.getAnswerContent().length()==0)
				{
					if(user.getsStatus().equals("班主任") || user.getsStatus().equals("迎新管理员"))
						holder.ib_delete.setVisibility(View.VISIBLE);
					else
						holder.ib_delete.setVisibility(View.GONE);
				}
				else
					holder.ib_delete.setVisibility(View.GONE);
			}
			else
			{
				if(notice.getPosterId().equals(userid))
					holder.ib_delete.setVisibility(View.VISIBLE);
				else
					holder.ib_delete.setVisibility(View.GONE);
			}
			holder.ib_delete.setTag(notice.getId());
			holder.ib_delete.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(final View v) {
					if(user.getUserType().equals("老师"))
					{
						Intent intent =new Intent(getActivity(),SchoolDetailActivity.class);
						intent.putExtra("templateName", "调查问卷");
						int pos=interfaceName.indexOf("?");
						String preUrl=interfaceName;
						if(pos>-1)
							preUrl=interfaceName.substring(0, pos);
						intent.putExtra("interfaceName", preUrl+"?ID="+v.getTag().toString()+"&action=reply");
						intent.putExtra("title", title);
						intent.putExtra("status", "进行中");
						intent.putExtra("autoClose", "是");
						startActivityForResult(intent, 101);
					}
					else
					{
						new AlertDialog.Builder(getActivity()) 
						.setTitle(R.string.deleteconfirm)
						.setMessage(R.string.ifconfirmdelete)
						.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener(){
	
							@Override
							public void onClick(DialogInterface dialog, int which) {
								// TODO Auto-generated method stub
								deleteLiuyan(v.getTag().toString());
							}
							
						})
						.setNegativeButton(R.string.no, null)
						.show();
					}
				}
				
			});

			LinearLayout layout=(LinearLayout)convertView.findViewById(R.id.ly_answer);
			if(notice.getAnswerContent()==null || notice.getAnswerContent().length()==0)
			{
				layout.setVisibility(View.GONE);
			}
			else
			{
				layout.setVisibility(View.VISIBLE);
				aq.id(holder.headImage_answer).image(notice.getAnswerAvater(),option);
				holder.name_answer.setText(notice.getAnswer());
				holder.time_answer.setText(notice.getAnswerTime());
			}
			holder.content_answer.setText(notice.getAnswerContent());
			
			return convertView;
		}

		class ViewHolder {
			
			TextView time;
			ImageView headImage;
			TextView content;
			TextView name;
			TextView time_answer;
			ImageView headImage_answer;
			TextView content_answer;
			TextView name_answer;
			LinearLayout openDetail;
			ImageButton ib_delete;
		}
	}
	private void deleteLiuyan(String id)
	{
		
		long datatime = System.currentTimeMillis();
		String checkCode = PrefUtility.get(Constants.PREF_CHECK_CODE, "");
		JSONObject jo = new JSONObject();
		Locale locale = getResources().getConfiguration().locale;
	    String language = locale.getCountry();
		try {
			jo.put("用户较验码", checkCode);
			jo.put("DATETIME", datatime);
			jo.put("language", language);
			
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		String base64Str = Base64.encode(jo.toString().getBytes());
		Log.d(TAG, "------->base64Str:" + base64Str);
		CampusParameters params = new CampusParameters();
		params.add(Constants.PARAMS_DATA, base64Str);
		CampusAPI.getSchoolItem(params, interfaceName+"?action=delBlog&ID="+id, new RequestListener() {

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
				msg.what = 1;
				msg.obj = response;
				mHandler.sendMessage(msg);
			}
		});
	}
	@Override
	public void onLoadMore() {
		// TODO Auto-generated method stub
		if(!isLoading)
			getNoticesList(false);
	}
	@Override
	public void onRefresh() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (resultCode) { //resultCode为回传的标记，我在B中回传的是RESULT_OK
		case 1:
			noticesList.clear();
			getNoticesList(true);
		    break;
		default:
		    break;
		}
	}
	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		// TODO Auto-generated method stub
		noticesList.clear();
		 switch (checkedId) {
         case R.id.button21:
        	 noticesList.clear();
        	 adapter.notifyDataSetChanged();
        	 getNoticesList(true);
             return;
         case R.id.button23:
        	 noticesList.clear();
        	 adapter.notifyDataSetChanged();
        	 getNoticesList(true);
             return;
		 }
	}
	
}
