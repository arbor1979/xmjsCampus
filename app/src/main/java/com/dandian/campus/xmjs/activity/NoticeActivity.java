
package com.dandian.campus.xmjs.activity;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.PreparedUpdate;
import com.dandian.campus.xmjs.R;
import com.dandian.campus.xmjs.api.CampusAPI;
import com.dandian.campus.xmjs.api.CampusException;
import com.dandian.campus.xmjs.api.CampusParameters;
import com.dandian.campus.xmjs.api.RequestListener;
import com.dandian.campus.xmjs.base.Constants;
import com.dandian.campus.xmjs.base.ExitApplication;
import com.dandian.campus.xmjs.db.DatabaseHelper;
import com.dandian.campus.xmjs.entity.NoticeClass;
import com.dandian.campus.xmjs.entity.User;
import com.dandian.campus.xmjs.util.AppUtility;
import com.dandian.campus.xmjs.util.Base64;
import com.dandian.campus.xmjs.util.DialogUtility;
import com.dandian.campus.xmjs.util.PrefUtility;
/**
 * 
 *  #(c) ruanyun PocketCampus <br/>
 *
 *  版本说明: $id:$ <br/>
 *
 *  功能说明: 班级通知
 * 
 *  <br/>创建说明: 2013-12-18 下午4:49:21 zhuliang  创建文件<br/>
 * 
 *  修改历史:<br/>
 *
 */
@SuppressLint("NewApi")
public class NoticeActivity extends Activity implements OnClickListener,android.content.DialogInterface.OnDismissListener{
	private String TAG = "NoticeActivity";
	private Button back,sendNotice;
	private EditText choose_class;
	private ImageView addClass;
	private CheckBox allChoose;
	private TextView tv_title;
	private ListView listView;
	private Dao<User, Integer> userDao;
	private DatabaseHelper database;
	private List<JSONObject> listData;
	private EditText content;
	private NoticeClass notices;
	private List<NoticeClass> list;
	private Dao<NoticeClass, Integer> noticeDao;
	private ClassAdapter classAdapter;
	private Dialog dialog;
	private Dialog mLoadingDialog;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.view_chat_notice);
		ExitApplication.getInstance().addActivity(this);
		listData = new ArrayList<JSONObject>();
		notices = new NoticeClass();
		dialog = new Dialog(this, R.style.dialog);
		dialog.setOnDismissListener(this);
		query();
		initTitle();
		initContent();	
	}
	/**
	 * 功能描述:加工需要修改的班级通知信息
	 *
	 * @author linrr  2013-12-16 下午5:57:23
	 * 
	 * @param suggestion
	 * @return
	 */
	public String getChangeNoticeClassInfo(NoticeClass notices) {
		if (notices != null) {
			JSONObject jo = new JSONObject();
			String checkCode = PrefUtility.get(Constants.PREF_CHECK_CODE, "");
			try {
				jo.put("用户较验码", checkCode);
				jo.put("action",  "DataDeal");
				jo.put("CONTENT", notices.getNotice());
				jo.put("DATETIME", String.valueOf(new Date().getTime()));
				jo.put("班级名称",notices.getClassName());//找到班级。。。。。。
				
				return jo.toString();
			} catch (JSONException e) {
				e.printStackTrace();
				return null;
			}
		}
		return null;
		
	}
	/**
	 * 
	 * 功能描述:提交班级通知
	 *
	 * @author linrr  2013-12-16 下午5:52:47
	 * 
	 * @param base64Str
	 * @param action
	 */
	public void SubmitNoticeClass(String base64Str,final String action){
		CampusParameters params = new CampusParameters();
		params.add(Constants.PARAMS_DATA, base64Str);
		CampusAPI.noticeClass(params, new RequestListener(){

			@Override
			public void onComplete(String response) {
				Bundle bundle = new Bundle();
				bundle.putString("action", action);
				bundle.putString("result", response.toString());
				System.out.println("response.toString()"+response.toString());
				Message msg = new Message();
				msg.what = 0;
				msg.obj = bundle;
				mHandler.sendMessage(msg);	
				
			}
			

			@Override
			public void onIOException(IOException e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onError(CampusException e) {
				Message msg = new Message();
				msg.what = 1;
				msg.obj = e.getMessage();
				mHandler.sendMessage(msg);
			}
			
		});
	}
	
	private void query(){
		try {
			userDao = getHelper().getUserDao();
			User user = userDao.queryBuilder().queryForFirst();
			String withClass = user.getWithClass();
			String[] str = withClass.split(",");
			for (String string : str) {
				JSONObject jo = new JSONObject();
				jo.put("isChecked", false);
				jo.put("text", string);
				listData.add(jo);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	private void initTitle(){
		
		back = (Button) findViewById(R.id.back);
		tv_title = (TextView)findViewById(R.id.setting_tv_title);
		tv_title.setText("班级通知");
		back.setOnClickListener(this);
	}

	private void initContent(){
		try {
			noticeDao = getHelper().getNoticeClassDao();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		content = (EditText) findViewById(R.id.send_content);
		sendNotice = (Button) findViewById(R.id.send);
		addClass = (ImageView)findViewById(R.id.bn_choose);
		choose_class = (EditText)findViewById(R.id.choose_class);
		if(listData != null && listData.size() == 1){
			JSONObject jo = listData.get(0);
			choose_class.setText(jo.optString("text"));
		}
		sendNotice.setOnClickListener(this);
		addClass.setOnClickListener(this);
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		dialogSure();
	}
	
	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.back :
			finish();
			break;
		case R.id.send :
			if (!choose_class.getText().toString().equals("")) {
				sendNotice();
			} else {
				AppUtility.showToastMsg(NoticeActivity.this, "您还没有选择班级，请选择！");
			}
			break;
		case R.id.bn_choose :
			bnChoose();
			break;
		case R.id.add_class:
			dialogSure();
			dialog.dismiss();
			break;
		case R.id.all_choose:
			allChoose(allChoose.isChecked());
			break;
			default :
				break;
		}
	}
	/**
	 * 功能描述:发送通知
	 *
	 * @author zhuliang  2013-12-21 上午10:33:42
	 *
	 */
	private void sendNotice(){
		String noticeStr = content.getText().toString();
		notices = new NoticeClass();
		try {
			if (noticeStr != null && !noticeStr.trim().equals("")) {
				notices.setNotice(noticeStr);
				notices.setIsModify(1);
				noticeDao.create(notices);
				notices.getNotice();
				showDialog();
				String noticeJsonString = getChangeNoticeClassInfo(notices);
				if (!"".equals(noticeJsonString) && noticeJsonString != null) {
					String NoticeBase64 = Base64.encode(noticeJsonString
							.getBytes());
					SubmitNoticeClass(NoticeBase64, "changeNoticeClass");
				}
				
			} else {
				DialogUtility.showMsg(getApplicationContext(), "班级通知不能为空！");
			}
			list = noticeDao.queryForAll();
			if (list != null && list.size() > 0) {
				for (NoticeClass notice : list) {
					Log.d(TAG, "--------------notice---------> "
									+ notice.getId() + "/"
									+ notice.getNotice());
				}
				Log.d(TAG, "-----------notice--------->"
						+ list.size());
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}
	/**
	 * 功能描述:确定选中
	 *
	 * @author zhuliang  2013-12-21 上午10:32:06
	 *
	 */
	private void dialogSure(){
		StringBuffer buffer = new StringBuffer();
		
		for (JSONObject jo : listData) {
			if (jo.optBoolean("isChecked")) {
				String classname = jo.optString("text");
				buffer.append(classname);
				buffer.append(",");
			}
			
		}
		if(buffer != null && !"".equals(buffer.toString().trim())){
			buffer.deleteCharAt(buffer.lastIndexOf(","));
		}
		choose_class.setText(buffer.toString());

	}
	
	/**
	 * 功能描述:添加班级
	 *
	 * @author zhuliang  2013-12-21 上午10:32:21
	 *
	 */
	private void bnChoose(){
		View localView = getLayoutInflater().inflate(R.layout.view_notice_add, null);
		allChoose = (CheckBox)localView.findViewById(R.id.all_choose);
		listView = (ListView)localView.findViewById(R.id.list);
		Button bn_sure = (Button)localView.findViewById(R.id.add_class);
		classAdapter = new ClassAdapter(listData);
		listView.setAdapter(classAdapter);
		String text = choose_class.getText().toString();
		String[] textStr;
		if(text != null && !"".equals(text.trim())){
			textStr = text.split(",");
			if(textStr.length == listData.size()){
				allChoose.setChecked(true);
				for(int i = 0; i < listData.size();i++){
					View view = classAdapter.getView(i, null, listView);
					if(view instanceof CheckBox){
						((CheckBox) view).setChecked(true);
					}
				}
			}
		}
		allChoose.setOnClickListener(this);
		dialog.setContentView(localView);
		dialog.show();
		dialog.getWindow().setGravity(Gravity.BOTTOM);
		bn_sure.setOnClickListener(this);
	}
	
	/**
	 * 功能描述:全选
	 *
	 * @author zhuliang  2013-12-21 下午1:01:18
	 * 
	 * @param isChecked
	 */
	private void allChoose(boolean isChecked) {
		for(int i = 0; i < listData.size(); i++){
			
			try {
				JSONObject jo = listData.get(i);
				jo.put("isChecked", isChecked);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		classAdapter.notifyDataSetChanged();
	}
	
	class ClassAdapter extends BaseAdapter{
		private List<JSONObject> list = new ArrayList<JSONObject>();
		
		public ClassAdapter(List<JSONObject> list){
			this.list = list;
		}
		@Override
		public int getCount() {
			return this.list == null ? 0 : this.list.size();
		}

		@Override
		public Object getItem(int position) {
			return list.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			convertView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.notice_choose, null);
			final CheckBox box = (CheckBox) convertView.findViewById(R.id.checkbox);
			final JSONObject joItem = (JSONObject) getItem(position);
			box.setText(joItem.optString("text"));
			box.setChecked(joItem.optBoolean("isChecked"));
			
			box.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					try {
						joItem.put("isChecked", isChecked);
					} catch (JSONException e) {
						e.printStackTrace();
					}
					if(!isChecked){
						allChoose.setChecked(false);
					}
				}
			});
			return convertView;
		}
	}

	private DatabaseHelper getHelper() {
		if (database == null) {
			database = OpenHelperManager.getHelper(this, DatabaseHelper.class);

		}
		return database;
	}

	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {

		public void handleMessage(Message msg) {
			Bundle bundle = new Bundle();
			switch (msg.what) {
			case 0:
				bundle = (Bundle) msg.obj;
				String action2 = bundle.getString("action");
				String result2 = bundle.getString("result");
				Log.d(TAG, "-----action2：" + action2 + "-----result2：" + result2);
				if(result2!=null){
					try {
						if (mLoadingDialog != null) {
							mLoadingDialog.dismiss();
						}
						DialogUtility.showMsg(NoticeActivity.this, "发送成功！");
						Log.d(TAG, "----------------->结束保存数据："+new Date());
						finish();
						@SuppressWarnings("unchecked")
						PreparedUpdate<NoticeClass> preparedUpdateNoticeClass = (PreparedUpdate<NoticeClass>) noticeDao.updateBuilder().updateColumnValue("isModify", 0).where().eq("isModify", 1).prepare();
						noticeDao.update(preparedUpdateNoticeClass);
					} catch (SQLException e1) {
						e1.printStackTrace();
					}
				}
				break;
			case 1:
				mLoadingDialog.dismiss();
				AppUtility.showErrorToast(NoticeActivity.this, msg.obj.toString());
				break;
			}
		}
    };
	

	/**
	 * 功能描述:显示提示框
	 *
	 * @author yanzy  2013-12-21 上午10:54:41
	 *
	 */
	public void showDialog(){
		mLoadingDialog = DialogUtility.createLoadingDialog(NoticeActivity.this, "数据提交中...");
		mLoadingDialog.show();
	}
	/**
	 * 功能描述:操作失败，提示
	 *
	 * @author yanzy  2013-12-21 上午10:37:17
	 *
	 */
	public void closeDialog(){
		if (mLoadingDialog != null) {
			mLoadingDialog.dismiss();
		}
		DialogUtility.showMsg(NoticeActivity.this, "保存失败！");
	}
}
