package com.dandian.campus.xmjs.activity;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
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
import com.dandian.campus.xmjs.base.ExitApplication;
import com.dandian.campus.xmjs.db.DatabaseHelper;
import com.dandian.campus.xmjs.entity.Student;
import com.dandian.campus.xmjs.util.AppUtility;
import com.dandian.campus.xmjs.util.Base64;
import com.dandian.campus.xmjs.util.DialogUtility;
import com.dandian.campus.xmjs.util.PrefUtility;

public class EditStuInfoActivity extends Activity {
	Button back;
	TextView title, save;
	ImageView stu_photo;
	EditText et_stuinfo_stuname, et_stuinfo_stuphone, et_stuinfo_email,
			et_stuinfo_paname, et_stuinfo_paphone, et_stuinfo_address,
			et_stuinfo_remark;
	Spinner et_stuinfo_sex;
	Student studentInfo;
	
	DatabaseHelper database;
	String studentId;
	String userImage;
	AQuery aq;
	private Dialog mLoadingDialog;
	private static final String TAG = "EditStuInfoActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		ExitApplication.getInstance().addActivity(this);
		setContentView(R.layout.activity_stuinfo_edit);
		aq = new AQuery(this);
		stu_photo = (ImageView) findViewById(R.id.image_camera);
		et_stuinfo_stuname = (EditText) findViewById(R.id.et_stuinfo_stuname);
		et_stuinfo_stuphone = (EditText) findViewById(R.id.et_stuinfo_stuphone);
		et_stuinfo_sex = (Spinner) findViewById(R.id.et_stuinfo_sex);
		et_stuinfo_email = (EditText) findViewById(R.id.et_stuinfo_email);
		et_stuinfo_paname = (EditText) findViewById(R.id.et_stuinfo_paname);
		et_stuinfo_paphone = (EditText) findViewById(R.id.et_stuinfo_paphone);
		et_stuinfo_address = (EditText) findViewById(R.id.et_stuinfo_address);
		et_stuinfo_remark = (EditText) findViewById(R.id.et_stuinfo_remark);
		studentId = getIntent().getStringExtra("studentId");
		userImage = getIntent().getStringExtra("userImage");
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				this, R.array.Sex, R.layout.view_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		et_stuinfo_sex.setAdapter(adapter);
		initContent();
		initTitle();
	}

	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();
		initContent();
	}

	private void initContent() {
		try {
			studentInfo=null;
			Map<String,List<Student>> map=((CampusApplication)getApplicationContext()).getStudentDic();
			for (String key : map.keySet()) {
				List<Student> stuList=map.get(key);
				for(Student stu:stuList)
				{
					if(stu.getStudentID().equals(studentId))
					{
						studentInfo=stu;
						break;
					}
				}
				if(studentInfo!=null)
					break;
			}
			
			System.out.println("studentId:" + studentInfo.getName());
			if (studentInfo != null) {
				et_stuinfo_stuname.setText(studentInfo.getName());
				et_stuinfo_stuphone.setText(studentInfo.getPhone());
				et_stuinfo_email.setText(studentInfo.getEmail());
				et_stuinfo_paname.setText(studentInfo.getParentName());
				et_stuinfo_paphone.setText(studentInfo.getParentPhone());
				et_stuinfo_address.setText(studentInfo.getHomeAddress());
				et_stuinfo_remark.setText(studentInfo.getRemark());
				String sex = studentInfo.getGender();
				Log.d(TAG, "------------sex------------->" + sex);
				if (sex.equals("男")) {
					et_stuinfo_sex.setSelection(0);
				} else {
					et_stuinfo_sex.setSelection(1);
				}
			}

			ImageOptions options = new ImageOptions();
			options.round = 115;
			aq.id(stu_photo).image(studentInfo.getPicImage(), options);

		} catch (NumberFormatException e) {
			e.printStackTrace();
		} 
	}

	private void initTitle() {
		title = (TextView) findViewById(R.id.setting_tv_title);
		title.setText("学生详情修改");

		// go = (Button) findViewById(R.id.setting_btn_goto);
		back = (Button) findViewById(R.id.back);
		back.setText("取消");
		back.setVisibility(View.INVISIBLE);
		save = (TextView) findViewById(R.id.goto_text);

		save.setVisibility(View.VISIBLE);
		save.setText("保存");
		save.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String emailContent = et_stuinfo_email.getText().toString();
				String stuphoneContent = et_stuinfo_stuphone.getText()
						.toString();
				String paphoneContent = et_stuinfo_paphone.getText().toString();
				if (AppUtility.checkEmail(emailContent)
						&& AppUtility.checkPhone(stuphoneContent)
						&& AppUtility.checkPhone(paphoneContent)) {
					try {
						showDialog();
						studentInfo.setGender(et_stuinfo_sex.getSelectedItem().toString());
						studentInfo.setPhone(et_stuinfo_stuphone.getText().toString());
						studentInfo.setEmail(et_stuinfo_email.getText().toString());
						studentInfo.setParentName(et_stuinfo_paname.getText().toString());
						studentInfo.setParentPhone(et_stuinfo_paphone.getText().toString());
						studentInfo.setHomeAddress(et_stuinfo_address.getText().toString());
						studentInfo.setRemark(et_stuinfo_remark.getText().toString());
						
						/**
						 * 获取修改学生数据
						 */
						String studentJsonStr = getChangestudentinfo(studentInfo);
						Log.d(TAG, "studentJsonStr:" + studentJsonStr);
						if (!"".equals(studentJsonStr)
								&& studentJsonStr != null) {
							String studentBase64 = Base64.encode(studentJsonStr.getBytes());
							SubmitChangeinfo(studentBase64, "changestudentinfo");
						} else {
							closeDialog();
						}
					} catch (JSONException e) {
						e.printStackTrace();
						closeDialog();
					}
				} else {
					if (!AppUtility.checkEmail(emailContent)) {
						AppUtility.showToastMsg(EditStuInfoActivity.this, "请输入正确的邮箱格式");
					}
					if (!AppUtility.checkPhone(stuphoneContent)
							|| !AppUtility.checkPhone(paphoneContent)) {
						AppUtility.showToastMsg(EditStuInfoActivity.this, "请输入正确的手机号码");
					}
				}
			}
		});
	}

	

	/**
	 * 功能描述:加工需要修改的学生数据
	 * 
	 * @author yanzy 2013-12-5 上午10:17:27
	 * 
	 * @param studentList
	 * @return
	 * @throws JSONException
	 */
	public String getChangestudentinfo(Student student) throws JSONException {
		if (student != null) {
			JSONArray jsonArray = new JSONArray();
			JSONObject jo = new JSONObject();
			String checkCode = PrefUtility.get(Constants.PREF_CHECK_CODE, "");// 获取用户校验码
			jo.put("用户较验码", checkCode);
			jo.put("编号", student.getStudentID());
			jo.put("性别", student.getGender());
			jo.put("学生电话", student.getPhone());
			jo.put("学生邮箱", student.getEmail());
			jo.put("家长姓名", student.getParentName());
			jo.put("家长电话", student.getParentPhone());
			jo.put("家庭住址", student.getHomeAddress());
			jo.put("备注", student.getRemark());
			jsonArray.put(jo);
			return jsonArray.toString();
		}
		return null;
	}

	/**
	 * 功能描述:提交服务器
	 * 
	 * @author yanzy 2013-12-4 上午10:10:42
	 * 
	 * @param base64Str
	 */
	public void SubmitChangeinfo(String base64Str, final String action) {
		CampusParameters params = new CampusParameters();
		params.add("action", action);
		params.add(Constants.PARAMS_DATA, base64Str);
		CampusAPI.Changeinfo(params, new RequestListener() {

			@Override
			public void onIOException(IOException e) {
				// TODO Auto-generated method stub

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
				Bundle bundle = new Bundle();
				bundle.putString("action", action);
				bundle.putString("result", response.toString());
				Message msg = new Message();
				msg.what = 0;
				msg.obj = bundle;
				mHandler.sendMessage(msg);
			}
		});
	}

	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {

		public void handleMessage(Message msg) {
			Bundle bundle = new Bundle();
			switch (msg.what) {
			case -1:
				mLoadingDialog.dismiss();
				AppUtility.showErrorToast(EditStuInfoActivity.this,
						msg.obj.toString());
				break;
			case 0:
				bundle = (Bundle) msg.obj;
				String action = bundle.getString("action");
				String result = bundle.getString("result");
				String resultStr = "";
				try {
					resultStr = new String(Base64.decode(result.getBytes("GBK")));
				} catch (UnsupportedEncodingException e1) {
					e1.printStackTrace();
				}
				Log.d(TAG, "action:" + action);
				Log.d(TAG, "resultStr:" + resultStr);

				try {
					JSONObject jo = new JSONObject(resultStr);
					if ("1".equals(jo.optString("成功"))) {
						if (mLoadingDialog != null) {
							mLoadingDialog.dismiss();
						}
						DialogUtility.showMsg(EditStuInfoActivity.this, "保存成功！");
						Log.d(TAG, "----------------->结束保存数据："+ new Date());
						finish();
					} else {
						closeDialog();
					}
				} catch (Exception e) {
					e.printStackTrace();
					closeDialog();
				}
				break;
			}
		}
    };

	/**
	 * 功能描述:显示提示框
	 * 
	 * @author yanzy 2013-12-21 上午10:54:41
	 * 
	 */
	public void showDialog() {
		mLoadingDialog = DialogUtility.createLoadingDialog(
				EditStuInfoActivity.this, "数据保存中...");
		mLoadingDialog.show();
	}

	/**
	 * 功能描述:操作失败，提示
	 * 
	 * @author yanzy 2013-12-21 上午10:37:17
	 * 
	 */
	public void closeDialog() {
		if (mLoadingDialog != null) {
			mLoadingDialog.dismiss();
		}
		DialogUtility.showMsg(EditStuInfoActivity.this, "保存失败！");
	}
}
