package com.dandian.campus.xmjs.activity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.dandian.campus.xmjs.CampusApplication;
import com.dandian.campus.xmjs.R;
import com.dandian.campus.xmjs.base.Constants;
import com.dandian.campus.xmjs.entity.ContactsMember;
import com.dandian.campus.xmjs.fragment.ContactsSelectSearchFragment;
import com.dandian.campus.xmjs.fragment.StudentSelectFragment;
import com.dandian.campus.xmjs.util.DialogUtility;
import com.dandian.campus.xmjs.util.PrefUtility;
import com.dandian.campus.xmjs.util.SearchParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 
 * #(c) ruanyun PocketCampus <br/>
 * 
 * 版本说明: $id:$ <br/>
 * 
 * 功能说明: 联系人界面
 * 
 * <br/>
 * 创建说明: 2014-07-15 15:14:26 QiaoLin 创建文件<br/>
 * 
 * 
 */
public class StudentSelectActivity extends FragmentActivity implements ContactsSelectSearchFragment.MyListener {
	static Button menu;
	static LinearLayout layout_menu;
	public static LinearLayout layout_refresh;
	private TextView cancel;
	private ViewGroup search_head;

	public static EditText search;
	private LinearLayout contacts;

	public static int STATUS = 0;
	private static final String TAG = "ContactsActivity";
	public static MyHandler mHandler;
	static ContactsSelectSearchFragment contactsSearchFragment;
	private DisplayMetrics dm;
	public static Dialog mLoadingDialog;
	StudentSelectFragment mContactsFragment;
	RelativeLayout contactlayout;
	LinearLayout initlayout;
	Button selectOk;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "----------------onCreate-----------------------");
		dm = getResources().getDisplayMetrics();
		setContentView(R.layout.activity_student_select);
		
		contacts = (LinearLayout) findViewById(R.id.content);
		search = (EditText) findViewById(R.id.edit_search);
		mContactsFragment=(StudentSelectFragment)getSupportFragmentManager().findFragmentById(R.id.contacts_list);
		mLoadingDialog = DialogUtility.createLoadingDialog(StudentSelectActivity.this, "正在获取数据...");
		String[] groups= getIntent().getStringArrayExtra("选项");
		JSONObject subOptions=null;
		JSONArray answers=null;
		try {
			subOptions=new JSONObject(getIntent().getStringExtra("子选项"));
			answers=new JSONArray(getIntent().getStringExtra("用户答案"));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		List<String> grouplist=new ArrayList<String>();
		List<List<ContactsMember>> childList=new ArrayList<List<ContactsMember>>() ;
		List<List<ContactsMember>> childSelectedList=new ArrayList<List<ContactsMember>>() ;
		List<ContactsMember> memberList = new ArrayList<ContactsMember>();
		String[] weiyimaarr= PrefUtility.get(Constants.PREF_CHECK_HOSTID,"1").split("_");
		PinyinComparator pinyinComparator = new PinyinComparator();
		for(String item :groups) {
			if(item.equals("请选择"))
				continue;
			grouplist.add(item);
			try {
				JSONArray ja=subOptions.getJSONArray(item);
				if(ja==null)
					ja=new JSONArray();
				List<ContactsMember> groupitem=new ArrayList<ContactsMember>();
				List<ContactsMember> selectitem=new ArrayList<ContactsMember>();
				for(int i=0;i<ja.length();i++)
				{
					JSONObject jo=ja.getJSONObject(i);
					ContactsMember person=new ContactsMember();
					if(jo!=null)
					{
						person.setClassName(item);
						person.setStudentID(jo.optString("id"));
						person.setName(jo.optString("name"));
						person.setXingMing(SearchParser.getPinYinHeadChar(person.getName())+person.getName());
						person.setUserImage(jo.optString("icon"));
						person.setGender(jo.optString("sex"));
						if(jo.optString("usertype").length()==0)
							person.setUserType("学生");
						else
							person.setUserType(jo.optString("usertype"));
						String weiyima="用户_"+person.getUserType()+"_"+person.getStudentID()+"____"+weiyimaarr[weiyimaarr.length-1];
						person.setUserNumber(weiyima);
						if(person.getUserImage()==null || person.getUserImage().length()==0)
						{

							ContactsMember contactsMember=((CampusApplication)getApplicationContext()).getLinkManDic().get(weiyima);
							if(contactsMember!=null && contactsMember.getUserImage().length()>0)
								person.setUserImage(contactsMember.getUserImage());
						}
						groupitem.add(person);
						for(int j=0;j<answers.length();j++)
						{
							JSONObject answerItem=answers.getJSONObject(j);
							if(answerItem.optString("id").equals(person.getStudentID())) {
								selectitem.add(person);
								break;
							}
						}
						memberList.add(person);
					}
				}

				Collections.sort(groupitem, pinyinComparator);
				childList.add(groupitem);
				childSelectedList.add(selectitem);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		mContactsFragment.groupList=grouplist;
		mContactsFragment.childList=childList;
		mContactsFragment.childSelectedList=childSelectedList;
		mContactsFragment.memberList=memberList;
		mContactsFragment.initContent();
		mHandler = new MyHandler();
		initViews();
		initSearch();

	
	}


	public class PinyinComparator implements Comparator<ContactsMember> {

		public int compare(ContactsMember o1, ContactsMember o2) {
			// 这里主要是用来对ListView里面的数据根据ABCDEFG...来排序
			String o1Name=o1.getXingMing().trim().substring(0,1)+o1.getName().trim();
			String o2Name=o2.getXingMing().trim().substring(0,1)+o2.getName().trim();
			return o1Name.compareTo(o2Name);

		}


	}

	
	/**
	 * 功能描述: 搜索框处理
	 *
	 * @author zhuliang  2013-12-13 下午5:03:12
	 *
	 */
	@SuppressLint("NewApi")
	private void initSearch() {
		search.setFocusable(false);
		search.setFocusableInTouchMode(false);
		search.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//界面上移动画
				AnimationSet animationSet = new AnimationSet(true);
				TranslateAnimation translateAnimation = new TranslateAnimation(
						0, 0, contacts.getY(), contacts.getY() - 44 * dm.densityDpi/160);
				animationSet.addAnimation(translateAnimation);
				animationSet.setDuration(300);
				animationSet.setFillAfter(true);
				animationSet.setFillBefore(false);
				contacts.startAnimation(animationSet);
				
				List<ContactsMember> list=new ArrayList<ContactsMember>();
				for(List<ContactsMember> sublist:mContactsFragment.childSelectedList)
				{
					for(ContactsMember item:sublist)
						list.add(item);
				}
				contactsSearchFragment = ContactsSelectSearchFragment.newInstance(2, mContactsFragment.memberList,list);
				
				Message msg = new Message();
				msg.what = 0;
				mHandler.sendMessageDelayed(msg, 300);
			}
		});
	}
	
	// 初始化Views
	private void initViews() {
		
		search_head = (ViewGroup) findViewById(R.id.search_head);
		
		search_head.getBackground().setAlpha(50);
		cancel = (TextView) findViewById(R.id.chat_btn_cancel);
		cancel.setVisibility(View.GONE);
		
		TextView tv_title = (TextView) findViewById(R.id.setting_tv_title);
		final Button bn_back = (Button) findViewById(R.id.back);
		tv_title.setText("搜索多选");
		
		bn_back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				
				InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(bn_back.getWindowToken(), 0);
				finish();
			}
		});
		selectOk=(Button)findViewById(R.id.confirm_sel);
		selectOk.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				if(mContactsFragment.selectedlist!=null)
				{
					JSONArray returnJson=new JSONArray();
					for(ContactsMember item:mContactsFragment.selectedlist)
					{
						if(item.getStudentID()!=null && item.getStudentID().length()>0) {
							JSONObject jo = new JSONObject();
							try {
								jo.put("id", item.getStudentID());
								jo.put("name", item.getName());
								jo.put("icon", item.getUserImage());
							} catch (JSONException e) {
								e.printStackTrace();
							}
							returnJson.put(jo);
						}
					}
					Intent aintent = new Intent();
					aintent.putExtra("returnJson", returnJson.toString());
					int curIndex=getIntent().getIntExtra("curIndex",-1);
					aintent.putExtra("curIndex",curIndex);
					setResult(1,aintent);
					finish();
				}
			}
			
		});
	}

	//	消息处理
	@SuppressLint("HandlerLeak")
	public class MyHandler extends Handler {
		@SuppressWarnings("deprecation")
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case 0:
				//弹出搜索窗口
				Log.d(TAG, "-------isAdded----------" + contactsSearchFragment.isAdded());
				if (!contactsSearchFragment.isAdded()) {
					contactsSearchFragment.show(getSupportFragmentManager(),
							"search");
					getSupportFragmentManager().executePendingTransactions();
					Dialog dialog = contactsSearchFragment.getDialog();
					WindowManager wm = getWindowManager();
					Display display = wm.getDefaultDisplay();
					LayoutParams lp = dialog.getWindow().getAttributes();
					dialog.getWindow().setWindowAnimations(R.style.dialogWindowAnim);
					lp.width = display.getWidth();
					Log.d(TAG, "----------height----------" + lp.height);
					dialog.getWindow().setGravity(Gravity.TOP);
					dialog.getWindow().setAttributes(lp);
					
					//点击search时，不弹出输入键盘
					
					InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(search.getWindowToken(), 0);
					
					search.setInputType(0);
				}
				break;
			case 1:
				//界面下移
				Log.d(TAG, "--->  执行界面隐藏方法...");
				
				AnimationSet animationSet1 = new AnimationSet(true);
				TranslateAnimation translateAnimation1 = new TranslateAnimation(
						0, 0, contacts.getY() + 44 * dm.densityDpi/160, contacts.getY());
				animationSet1.addAnimation(translateAnimation1);
				animationSet1.setFillAfter(true);
				animationSet1.setFillBefore(false);
				contacts.startAnimation(animationSet1);
				
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(search.getWindowToken(), 0); //强制隐藏键盘  
				
				break;
			
			default:
				break;
			}
		}

	}

	
	class ViewHolder {
		ImageView photo;
		TextView name;
	}


	@Override
	public void updateSelectedList(List<ContactsMember> selectList) {
		// TODO Auto-generated method stub
		for(int i=0;i<mContactsFragment.childSelectedList.size();i++)
			mContactsFragment.childSelectedList.get(i).clear();
		for(ContactsMember item:selectList)
		{
			String group=item.getVirtualClass();
			if(group==null)
				group=item.getClassName();
			for(int i=0;i<mContactsFragment.groupList.size();i++)
			{
				if(mContactsFragment.groupList.get(i).equals(group))
				{
					List<ContactsMember> list=mContactsFragment.childSelectedList.get(i);
					if(!list.contains(item))
						list.add(item);
					break;
				}
			}
		}
		mContactsFragment.updateViewBySelected();
	}
	
	
}
