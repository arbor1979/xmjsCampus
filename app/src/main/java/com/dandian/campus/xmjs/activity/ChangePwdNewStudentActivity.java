package com.dandian.campus.xmjs.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.dandian.campus.xmjs.R;
import com.dandian.campus.xmjs.api.CampusAPI;
import com.dandian.campus.xmjs.api.CampusException;
import com.dandian.campus.xmjs.api.CampusParameters;
import com.dandian.campus.xmjs.api.RequestListener;
import com.dandian.campus.xmjs.base.Constants;
import com.dandian.campus.xmjs.util.AppUtility;
import com.dandian.campus.xmjs.util.Base64;
import com.dandian.campus.xmjs.util.DialogUtility;
import com.dandian.campus.xmjs.util.PrefUtility;
import com.dandian.campus.xmjs.util.TimeUtility;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;

public class ChangePwdNewStudentActivity extends Activity implements OnClickListener{

	private Dialog mLoadingDialog;
	InputMethodManager inputManager;
	TextView title;
	private Button back,save;
	String oldpwd;
	AQuery aq=new AQuery(this);
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_change_newstudent_pwd);
		inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		
		initTitle();
	}
	private void initTitle() {
		back = (Button) findViewById(R.id.back);
		title = (TextView) findViewById(R.id.setting_tv_title);
		title.setText(R.string.settings_changePwd);
		save= (Button) findViewById(R.id.uploading);
		save.setCompoundDrawables(null, null, null, null); 
		save.setVisibility(View.VISIBLE);
		save.setText(R.string.save);
		//aq.id(R.id.setting_layout_goto).visibility(View.VISIBLE);
		
		back.setOnClickListener(this);
		save.setOnClickListener(this);
		EditText et=(EditText)findViewById(R.id.editText1);
		TimeUtility.popSoftKeyBoard(this, et);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.back:
			//inputManager.hideSoftInputFromWindow(content.getWindowToken(), 0);
			finish();
			break;
		case R.id.uploading:
			String oldpwd=aq.id(R.id.editText1).getText().toString();
			String pwd =aq.id(R.id.editText2).getText().toString();
			String pwd1 =aq.id(R.id.editText3).getText().toString();
			
			if(pwd.trim().length()==0)
			{
				AppUtility.showToastMsg(this, getString(R.string.pwdisempty));
				break;
			}
			if(!pwd.equals(pwd1)){
				AppUtility.showToastMsg(this, getString(R.string.pwdisdifferent));
				break;
			}
			
			SubmitFeedback(oldpwd,pwd);

			break;
		default:
			break;
		}
	}

	public void SubmitFeedback(String oldpwd,String newpwd){
		showDialog();
		JSONObject jo = new JSONObject();
		String checkCode = PrefUtility.get(Constants.PREF_CHECK_CODE, "");// 获取用户校验码
		try {
			jo.put("用户较验码", checkCode);
			jo.put("action",  "changepwd");
			jo.put("密码", newpwd);
			jo.put("旧密码", oldpwd);
			jo.put("DATETIME", String.valueOf(new Date().getTime()));
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		String SuggBase64 = Base64.encode(jo.toString().getBytes());
		CampusParameters params = new CampusParameters();
		params.add(Constants.PARAMS_DATA, SuggBase64);
		CampusAPI.loginCheckNewStudent(params, new RequestListener(){

			@Override
			public void onComplete(String response) {
				
				Message msg = new Message();
				msg.what = 0;
				msg.obj = response;
				mHandler.sendMessage(msg);	
				
			}

			@Override
			public void onIOException(IOException e) {
				// TODO Auto-generated method stub
				Message msg = new Message();
				msg.what = 1;
				msg.obj = e.getMessage();
				mHandler.sendMessage(msg);
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
			
			switch (msg.what) {
			case 0:
				
				if (mLoadingDialog != null) {
					mLoadingDialog.dismiss();
				}
				String result = msg.obj.toString();
				String resultStr = "";
				boolean flag=false;
				String errMsg="";
				if (AppUtility.isNotEmpty(result)) {
					try {
						resultStr = new String(Base64.decode(result.getBytes("GBK")));
						
						
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
					JSONObject jo = null;
					try {
						jo = new JSONObject(resultStr);
						
					} catch (JSONException e) {
						e.printStackTrace();
					}
					if(jo!=null){
						String tips = jo.optString("结果");
						if(tips.equals("成功"))
						{
							flag=true;
							JSONObject userInfo=jo.optJSONObject("用户资料");
							PrefUtility.put(Constants.PREF_CHECK_CODE,userInfo.optString("用户较验码"));
							PrefUtility.put(Constants.PREF_LOGIN_PASS, jo.optString("明文密码"));
						}
						else
							errMsg=tips;
					}
				}	
				if(flag)
				{
					
					DialogUtility.showMsg(ChangePwdNewStudentActivity.this, getString(R.string.savesuc));
					finish();
				}
				else
					DialogUtility.showMsg(ChangePwdNewStudentActivity.this, getString(R.string.failed)+errMsg);
				break;
			case 1:
				mLoadingDialog.dismiss();
				AppUtility.showErrorToast(ChangePwdNewStudentActivity.this, msg.obj.toString());
			break;
			}
		};
	};
	public void showDialog(){
		mLoadingDialog = DialogUtility.createLoadingDialog(this, getString(R.string.saving));
		mLoadingDialog.show();
	}
	
	public void closeDialog(){
		if (mLoadingDialog != null) {
			mLoadingDialog.dismiss();
		}
		DialogUtility.showMsg(this, "保存失败！");
	}
}

