package com.dandian.campus.xmjs.activity;

import java.io.IOException;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.dandian.campus.xmjs.R;
import com.dandian.campus.xmjs.api.CampusAPI;
import com.dandian.campus.xmjs.api.CampusException;
import com.dandian.campus.xmjs.api.CampusParameters;
import com.dandian.campus.xmjs.api.RequestListener;
import com.dandian.campus.xmjs.base.Constants;
import com.dandian.campus.xmjs.base.ExitApplication;
import com.dandian.campus.xmjs.util.AppUtility;
import com.dandian.campus.xmjs.util.Base64;
import com.dandian.campus.xmjs.util.DialogUtility;
import com.dandian.campus.xmjs.util.PrefUtility;


public class FeedbackActivity extends Activity implements OnClickListener {
	private String TAG = "FeedbackActivity";
	private Button back,send;
	private EditText content;
	private TextView title;
	private InputMethodManager inputManager;
	private Dialog mLoadingDialog;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ExitApplication.getInstance().addActivity(this);

		setContentView(R.layout.activity_feedback);
		inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		initTitle();
		initContent();
		listener();
	}

	private void initTitle() {
		back = (Button) findViewById(R.id.back);
		title = (TextView) findViewById(R.id.setting_tv_title);
		title.setText("意见反馈");
	}

	private void initContent() {
		send = (Button) findViewById(R.id.send);
		content = (EditText) findViewById(R.id.suggest);
	}

	private void listener() {
		back.setOnClickListener(this);
		send.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.back:
			inputManager.hideSoftInputFromWindow(content.getWindowToken(), 0);
			finish();
			break;
		case R.id.send:
			String suggest = content.getText().toString();

			if (suggest != null && !suggest.trim().toString().equals("")) {
				showDialog();

				try {
					String suggJsonString = getChangeSuggestInfo(suggest);
					if (!"".equals(suggJsonString) && suggJsonString != null) {
						String SuggBase64 = Base64.encode(suggJsonString
								.getBytes());
						SubmitFeedback(SuggBase64, "changeSuggestion");
					}else{
						closeDialog();
					}
				} catch (JSONException e) {
					e.printStackTrace();
					closeDialog();
				}
				
			} else {
				AppUtility.showToastMsg(this, "提交意见不能为空！");
			}

			break;
		default:
			break;
		}
	}
	
	/***
	 * 功能描述:加工需要修改的意见反馈信息
	 *
	 * @author linrr  2013-12-16 上午11:14:57
	 * 
	 * @param userInfo
	 * @return
	 * @throws JSONException 
	 */
	public String getChangeSuggestInfo(String suggest) throws JSONException {
		if (AppUtility.isNotEmpty(suggest)) {
			JSONObject jo = new JSONObject();
			String checkCode = PrefUtility.get(Constants.PREF_CHECK_CODE, "");// 获取用户校验码
			jo.put("用户较验码", checkCode);
			jo.put("action",  "DataDeal");
			jo.put("CONTENT", suggest);
			jo.put("DATETIME", String.valueOf(new Date().getTime()));
			return jo.toString();
		}
		return null;
	}
	
	/**
	 * 功能描述:功能描述:提交服务器
	 *
	 * @author linrr  2013-12-16 下午2:18:41
	 * 
	 * @param base64Str
	 * @param action
	 */
	public void SubmitFeedback(String base64Str,final String action){
		CampusParameters params = new CampusParameters();
		params.add(Constants.PARAMS_DATA, base64Str);
		CampusAPI.feedback(params, new RequestListener(){

			@Override
			public void onComplete(String response) {
				Bundle bundle = new Bundle();
				bundle.putString("action", action);
				bundle.putString("result", response.toString());
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
	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {

		public void handleMessage(Message msg) {
			Bundle bundle = new Bundle();
			switch (msg.what) {
			case 0:
				bundle = (Bundle) msg.obj;
				String action = bundle.getString("action");
				String result = bundle.getString("result");
				Log.d(TAG, "------action：" + action + "------result：" + result);
				if (mLoadingDialog != null) {
					mLoadingDialog.dismiss();
				}
				DialogUtility.showMsg(FeedbackActivity.this, "提交成功！");
				Log.d(TAG, "----------------->结束保存数据："+new Date());
				finish();
				break;
			case 1:
				mLoadingDialog.dismiss();
				AppUtility.showErrorToast(FeedbackActivity.this, msg.obj.toString());
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
		mLoadingDialog = DialogUtility.createLoadingDialog(FeedbackActivity.this, "数据提交中...");
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
		DialogUtility.showMsg(FeedbackActivity.this, "保存失败！");
	}
}
