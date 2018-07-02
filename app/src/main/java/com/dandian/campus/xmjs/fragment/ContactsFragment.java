package com.dandian.campus.xmjs.fragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.SpannableString;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnGroupExpandListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.androidquery.callback.ImageOptions;
import com.dandian.campus.xmjs.CampusApplication;
import com.dandian.campus.xmjs.R;
import com.dandian.campus.xmjs.activity.ChatMsgActivity;
import com.dandian.campus.xmjs.activity.ShowPersonInfo;
import com.dandian.campus.xmjs.activity.StudentInfoActivity;
import com.dandian.campus.xmjs.entity.ContactsFriends;
import com.dandian.campus.xmjs.entity.ContactsMember;
import com.dandian.campus.xmjs.entity.User;
import com.dandian.campus.xmjs.util.AppUtility;
import com.dandian.campus.xmjs.util.ExpressionUtil;
import com.dandian.campus.xmjs.util.AppUtility.CallBackInterface;
/**
 * 
 *  #(c) ruanyun PocketCampus <br/>
 *
 *  版本说明: $id:$ <br/>
 *
 *  功能说明: 联系人详细信息
 * 
 *  <br/>创建说明: 2013-12-16 下午5:45:42 zhuliang  创建文件<br/>
 * 
 *  修改历史:<br/>
 *
 */
public class ContactsFragment extends Fragment {
	
	
	private ExpandableListView expandableListView;
	public ExpandableAdapter expandableAdapter;
	public List<String> groupList;
	public List<List<ContactsMember>> childList;
	private LinearLayout initLayout;
	private AQuery aq;
	private static final String TAG = "ContactsFragment";
	
	private PinyinComparator pinyinComparator;
	public List<ContactsMember> memberList;
	public Map<String,String> chatFriendMap;
	static Dialog mLoadingDialog = null;
	
	private User user;
	private String curPhone;
	@SuppressLint("HandlerLeak")
	public Handler mHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			switch(msg.what){
			case 0 :
				initLayout.setVisibility(View.GONE);
				expandableListView.setVisibility(View.VISIBLE);
				initContent();
				
				
				break;
				default:
					break;
			}
		}
		
	};
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		user=((CampusApplication)getActivity().getApplicationContext()).getLoginUserObj();
		Log.d(TAG, "----------------onCreate is running------------");
		

	}
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.d(TAG, "--------------refresh is running--------------");
		View localView = inflater.inflate(R.layout.view_contacts, container, false);
		expandableListView = (ExpandableListView)localView.findViewById(R.id.contacts);
		initLayout = (LinearLayout) localView.findViewById(R.id.initlayout);
		
		expandableListView.setVisibility(View.GONE);
		initLayout.setVisibility(View.VISIBLE);
		
		
		Thread thread = new Thread(){

			@Override
			public void run() {
				query();
				Message msg = new Message();
				msg.what = 0;
				
				mHandler.sendMessage(msg);
			}
			
		};
		thread.start();
		return localView;
	}

	/**
	 * 功能描述:  	查询联系人数据
	 *
	 * @author zhuliang  2013-12-13 下午5:01:06
	 *
	 */
	private void query() {
		Log.d(TAG, "------------------query refresh is running----------------");
		groupList = new ArrayList<String>();
		childList = new ArrayList<List<ContactsMember>>();
		memberList = new ArrayList<ContactsMember>();
		String userNumber = user.getUserNumber();
		try {
		
			//searchChatContent();
			
				//List<ContactsFriends> friendsList = contactsFriendsDao.queryForAll();
				List<ContactsFriends> friendsList=((CampusApplication)getActivity().getApplicationContext()).getLinkGroupList();
				for (ContactsFriends contactsFriends : friendsList) {
					String friendsName = contactsFriends.getFriendsName();
					String friendsMember = contactsFriends.getFriendsMember();
					JSONArray jaFriends = new JSONArray(friendsMember);
					List<ContactsMember> listMember = new ArrayList<ContactsMember>();
					if (jaFriends != null && jaFriends.length() > 0) {
						for (int i = 0; i < jaFriends.length(); i++) {
							String str = jaFriends.optString(i);
							if (!str.equals(userNumber)) {
								
								ContactsMember contactsMember=((CampusApplication)getActivity().getApplicationContext()).getLinkManDic().get(str);
								listMember.add(contactsMember);
								memberList.add(contactsMember);
								
							}
						}
					}
					groupList.add(friendsName);
					//pinyinComparator = new PinyinComparator();
					//Collections.sort(listMember, pinyinComparator);
					childList.add(listMember);
				}
			
			

		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	public class PinyinComparator implements Comparator<ContactsMember> {

		public int compare(ContactsMember o1, ContactsMember o2) {
			// 这里主要是用来对ListView里面的数据根据ABCDEFG...来排序
				String o1Name=o1.getXingMing().trim().substring(0,1)+o1.getName().trim();
				String o2Name=o2.getXingMing().trim().substring(0,1)+o2.getName().trim();
				return o1Name.compareTo(o2Name);
			
		}

	
	}
	private void sendCall(String phone)
	{
		Intent phoneIntent = new Intent("android.intent.action.CALL",
		Uri.parse("tel:" + phone));
		startActivity(phoneIntent);
	}
	private void initContent(){
		Log.d(TAG, "--------------initContent is rinning-------------");
		System.out.println(groupList.size() + "/" + childList.size());
		
		expandableAdapter = new ExpandableAdapter(groupList, childList,chatFriendMap);
		expandableListView.setAdapter(expandableAdapter);
		if( groupList.size() == 1){
			expandableListView.expandGroup(0);
		}
		expandableListView.setOnGroupExpandListener(new OnGroupExpandListener() {
			
			@Override
			public void onGroupExpand(int groupPosition) {
				for(int i = 0; i < expandableAdapter.getGroupCount(); i++){
					if(groupPosition != i && expandableListView.isGroupExpanded(i)){
						expandableListView.collapseGroup(i);
					}
				}
			}
		});
		
	}
	
	// 联系人数据适配器
		public class ExpandableAdapter extends BaseExpandableListAdapter {
			List<String> groupList = new ArrayList<String>();
			List<List<ContactsMember>> childList = new ArrayList<List<ContactsMember>>();
			Map<String,String> map = new HashMap<String, String>();
			
			public ExpandableAdapter(List<String> group,
					List<List<ContactsMember>> child,Map<String,String> map) {
				this.groupList = group;
				this.childList = child;
				this.map = map;
			}

			public void refresh(List<String> group,
					List<List<ContactsMember>> child,Map<String,String> map){
				this.groupList = group;
				this.childList = child;
				this.map = map;
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
					convertView = LayoutInflater.from(getActivity())
							.inflate(R.layout.view_expandablelist_child, null);
					holder.group = (LinearLayout)convertView.findViewById(R.id.contacts_info1);
					holder.photo = (ImageView) convertView.findViewById(R.id.photo);
					holder.name = (TextView) convertView.findViewById(R.id.child);
					holder.lastContentTV = (TextView)convertView.findViewById(R.id.signature);
					holder.callIV = (ImageView) convertView.findViewById(R.id.callIV);
					
					convertView.setTag(holder);
				} else {
					holder = (ViewHolder) convertView.getTag();
				}

				final ContactsMember contactsMember = childList.get(groupPosition)
						.get(childPosition);
				aq = new AQuery(getActivity());
				if (contactsMember != null) {
					//String toid = contactsMember.getUserNumber();
					String url = contactsMember.getUserImage();
					/*
					if(toid != null && !toid.trim().equals("") && map!=null && map.containsKey(toid)){
						holder.lastContentTV.setVisibility(View.VISIBLE);
						String msgContent = map.get(toid);
						SpannableString spannableString = ExpressionUtil
								.getExpressionString(getActivity(), msgContent);
						holder.lastContentTV.setText(spannableString);
						
					}else{
						holder.lastContentTV.setVisibility(View.GONE);
						holder.lastContentTV.setText("");
					}
					*/
					holder.callIV.setVisibility(View.GONE);
					//老师可以拨打电话
					final String userType = user.getUserType();

					final String phone=contactsMember.getStuPhone();
					if(AppUtility.isNotEmpty(phone) && phone.length()==11)
					{
						holder.callIV.setVisibility(View.VISIBLE);
						holder.callIV.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View v) {
								startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"+phone)));
								/*
								if (Build.VERSION.SDK_INT >= 23)
								{
									if(AppUtility.checkPermission(getActivity().getParent(), 8,Manifest.permission.CALL_PHONE))
										sendCall(phone);
									else
										curPhone=phone;
								}
								else
									sendCall(phone);
								*/

							}

						});
					}
					if(contactsMember.getUserType().endsWith("学生") && AppUtility.isNotEmpty(contactsMember.getSeatNumber()))
					{
						holder.lastContentTV.setVisibility(View.VISIBLE);
						holder.lastContentTV.setText("座号:"+contactsMember.getSeatNumber());
					}
					else
						holder.lastContentTV.setVisibility(View.GONE);

					
					//Log.d(TAG,"---------------------->contactsMember.getUserImage():"+url);
					ImageOptions options = new ImageOptions();
					options.memCache=false;
					options.round = 20;
					options.fallback = R.drawable.ic_launcher;
					aq.id(holder.photo).image(url, options);
					holder.name.setText(contactsMember.getName().trim());
					holder.group.setOnClickListener(new OnClickListener() {
	
						@Override
						public void onClick(View v) {
							Intent intent = new Intent(getActivity(),ChatMsgActivity.class);
							intent.putExtra("toid", contactsMember.getUserNumber());
							intent.putExtra("type", "消息");
							intent.putExtra("toname", contactsMember.getName());
							intent.putExtra("userImage", contactsMember.getUserImage());
							getActivity().startActivity(intent);
						}
					});
					holder.photo.setOnClickListener(new OnClickListener(){

						@Override
						public void onClick(View v) {
							
							if(userType.equals("老师") && contactsMember.getUserType().equals("学生"))
							{
								Intent intent = new Intent(getActivity(),
										StudentInfoActivity.class);
								intent.putExtra("studentId", contactsMember.getStudentID());
								intent.putExtra("userImage", contactsMember.getUserImage());
								startActivity(intent);
							}
							else
							{
								Intent intent = new Intent(getActivity(),
										ShowPersonInfo.class);
								intent.putExtra("studentId", contactsMember.getUserNumber());
								intent.putExtra("userImage", contactsMember.getUserImage());
								startActivity(intent);
							}
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
					convertView = LayoutInflater.from(getActivity()).inflate(
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
				holder.countTV.setText(String.valueOf(this.childList.get(groupPosition)
						.size()) + "人");
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
			ImageView photo,groupIV,callIV;
			TextView name,groupTV,countTV,lastContentTV;
			TextView showMemberBT;
		}
		
		/*
		private void searchChatContent(){
			chatFriendMap = new ConcurrentHashMap<String, ChatFriend>();
			try {
				chatFriendDao = getHelper().getChatFriendDao();
				List<ChatFriend> chatFriendList = chatFriendDao.queryForAll();
				ChatFriend chatFriend;
				String toid;
				for(int i = 0; i < chatFriendList.size(); i++){
					chatFriend = chatFriendList.get(i);
					toid = chatFriend.getToid();
					chatFriendMap.put(toid, chatFriend);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		public CallBackInterface callBack=new CallBackInterface()
		{

			@Override
			public void getLocation1() {
				// TODO Auto-generated method stub
			
			}

			@Override
			public void getPictureByCamera1() {

				
			}

			@Override
			public void getPictureFromLocation1() {

				
			}

			@Override
			public void sendCall1() {

				if(curPhone!=null && curPhone.length()>0)
					sendCall(curPhone);
			}

			@Override
			public void sendMsg1() {

				
			}
			
		};
		*/
}
