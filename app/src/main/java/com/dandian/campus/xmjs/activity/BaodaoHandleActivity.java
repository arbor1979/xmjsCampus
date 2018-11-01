package com.dandian.campus.xmjs.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
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
import com.dandian.campus.xmjs.entity.User;
import com.dandian.campus.xmjs.util.AppUtility;
import com.dandian.campus.xmjs.util.Base64;
import com.dandian.campus.xmjs.util.DateHelper;
import com.dandian.campus.xmjs.util.DialogUtility;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BaodaoHandleActivity extends Activity {

	AQuery aq;
	private LinearLayout ll_studentinfo,loadingLayout;
	private JSONObject userObject,completeResult,otherObject;
	MyAdapter adapter;
	private List<String> groupkey=new ArrayList<String>(); 
	private List<String> aList = new ArrayList<String>();  
	private String ID;
	private ProgressDialog mypDialog;
	private User user;
	private boolean changed=false;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_baodao_handle);
		ll_studentinfo=(LinearLayout)this.findViewById(R.id.ll_studentinfo);
		loadingLayout = (LinearLayout) findViewById(R.id.data_load);
		loadingLayout.setVisibility(View.GONE);
		
		aq = new AQuery(this);
		
		aq.id(R.id.back).clicked(new OnClickListener(){

			@Override
			public void onClick(View v) {
				if(changed)
				{
					Intent resultIntent = new Intent();
					setResult(RESULT_OK, resultIntent);
				}
				finish();
			}
			
		});
		user=((CampusApplication)getApplicationContext()).getLoginUserObj();
		aq.id(R.id.setting_tv_title).text("新生报到");		
		ID=getIntent().getStringExtra("ID");
		if(ID!=null || ID.length()>0)
			getUserInfo();
		else
			AppUtility.showErrorToast(this,"身份证号不能为空");
	}
	private void getUserInfo()
	{
		showProgress(true);
		String dataResult = "";
		Locale locale = getResources().getConfiguration().locale;
	    String language = locale.getCountry();
		try {
			JSONObject jsonObj = new JSONObject();
			jsonObj.put("action", "getinfo");
			jsonObj.put("编号", ID);
			jsonObj.put("userRole", user.getsStatus());
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
	private void showProgress(boolean progress) {
		if (progress) {
			loadingLayout.setVisibility(View.VISIBLE);
			ll_studentinfo.setVisibility(View.GONE);
		} else {
			loadingLayout.setVisibility(View.GONE);
			ll_studentinfo.setVisibility(View.VISIBLE);
		}
	}
	
	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {

		public void handleMessage(Message msg) {
			switch (msg.what) {
			case -1:
				showProgress(false);
				showProgress1(false);
				AppUtility.showErrorToast(BaodaoHandleActivity.this,
						msg.obj.toString());
				break;
			case 0:
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
						AppUtility.showToastMsg(BaodaoHandleActivity.this, loginStatus,1);
					} else 
					{
						
						userObject=jo.optJSONObject("用户信息");
						String luqustr=jo.optString("表格分组");
						completeResult=jo.optJSONObject("完成情况");
						otherObject=jo.optJSONObject("其他数组");
						groupkey.clear();
						aList.clear();
						String[] headstr=luqustr.split(",");
						for(int i=0;i<headstr.length;i++)
						{
							groupkey.add(headstr[i]);
							aList.add(headstr[i]);
							String[] fields=jo.optString(headstr[i]).split(",");
							for(int j=0;j<fields.length;j++)
							{
								aList.add(fields[j]);
								
							}
						}
						initContent();
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
						AppUtility.showToastMsg(BaodaoHandleActivity.this, loginStatus,1);
					} else 
					{
						changed=true;
						String action=jo.optString("action");
						if(jo.optString("完成情况")!=null && jo.optString("完成情况").length()>0)
							completeResult.put(action, jo.optInt("完成情况"));
						userObject.put(action, jo.optString("显示值"));
						if(action.equals("分配宿舍") && jo.optInt("完成情况")==0)
						{
							userObject.put("学生宿舍","");
							userObject.put("床位号",0);
						}
							
					}
					adapter.notifyDataSetChanged();
				}
				catch (Exception e) 
				{
					e.printStackTrace();
				}
				break;
		
			}
		};
	};
	private void initContent() {
		
		ImageOptions options = new ImageOptions();
		//options.round=40;
		options.memCache=true;
		options.fileCache=true;
		
		String userImage=userObject.optString("照片");
		if(userImage!=null && userImage.length()>0)
		{
			aq.id(R.id.iv_pic).image(userImage,options);
		}
		
		
		aq.id(R.id.tv_name).text(userObject.optString("姓名"));
		aq.id(R.id.user_type).text(userObject.optString("学生状态"));
		
		aq.id(R.id.iv_pic).clicked(new OnClickListener(){

			@Override
			public void onClick(View v) {
				DialogUtility.showImageDialog(BaodaoHandleActivity.this,userObject.optString("照片"));
				
			}
			
		});
		
		aq.id(R.id.btn_msg).clicked(new OnClickListener(){

			@Override
			public void onClick(View v) {
				
				Intent intent = new Intent(BaodaoHandleActivity.this, ChatMsgActivity.class);
				intent.putExtra("toid", userObject.optString("用户唯一码"));
				intent.putExtra("toname", userObject.optString("姓名"));
				intent.putExtra("type", "txt");
				intent.putExtra("userImage", userObject.optString("照片"));
				startActivity(intent);
			}
		});
		aq.id(R.id.btn_call).clicked(new OnClickListener(){

			@Override
			public void onClick(View v) {
				ArrayList<String> userphones=new ArrayList<String>();
				userphones.add("学生电话:"+userObject.optString("学生电话"));
				userphones.add("监护人电话 :"+userObject.optString("监护人手机号码"));
				
				final String []  userStr=new String[userphones.size()];
				for(int i=0;i<userphones.size();i++)
				{
					userStr[i]=userphones.get(i);
				}
				new Builder(BaodaoHandleActivity.this).setTitle("拨打给")
				.setIcon(android.R.drawable.ic_dialog_info)
				.setSingleChoiceItems(userStr, 0, new DialogInterface.OnClickListener() 
				{ 
					public void onClick(DialogInterface dialog, int which) 
					{ 
						String[]telStr=userStr[which].split(":");
						if(telStr.length==2 && telStr[1]!=null)
						{
							String tel=telStr[1];
							if(AppUtility.checkPhone(tel))
							{
                                startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"+tel)));
							}
						}
						dialog.dismiss();
					} 
				} 
				).setNegativeButton("取消", null) .show();
			}
			
		});
		adapter=new MyAdapter(this);
		aq.id(R.id.listView1).adapter(adapter);
		
	}
	public class MyAdapter extends BaseAdapter{
		 
        private LayoutInflater mInflater;
        public MyAdapter(Context context){
            this.mInflater = LayoutInflater.from(context);
          
        }
        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return aList.size();
        }
 
        @Override
        public Object getItem(int arg0) {
            // TODO Auto-generated method stub
        	return aList.get(arg0); 
        }
 
        @Override
        public long getItemId(int arg0) {
            // TODO Auto-generated method stub
            return arg0;
        }
        @Override  
        public boolean isEnabled(int position) {  
            // TODO Auto-generated method stub  
             if(groupkey.contains(getItem(position))){  
                 return false;  
             }  
             return super.isEnabled(position);  
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
             
        	View view=convertView;  
        	final String key=aList.get(position);
            if(groupkey.contains(getItem(position))){  
                view=mInflater.inflate(R.layout.addexam_list_item_tag, null);  
                TextView text=(TextView) view.findViewById(R.id.addexam_list_item_text); 
                text.setText(key);
            }else{  
                view=mInflater.inflate(R.layout.list_left_right_image_check, null);  
                TextView title=(TextView)view.findViewById(R.id.left_title);
                TextView detail=(TextView)view.findViewById(R.id.right_detail);
                RadioGroup rg_jiudufangshi=(RadioGroup)view.findViewById(R.id.rg_jiudufangshi);
                rg_jiudufangshi.setVisibility(View.GONE);
                
                LinearLayout ll_checkbox=(LinearLayout)view.findViewById(R.id.ll_checkbox);
                CheckBox checkBox1=(CheckBox)view.findViewById(R.id.checkBox1);
                CheckBox checkBox2=(CheckBox)view.findViewById(R.id.checkBox2);
                CheckBox checkBox3=(CheckBox)view.findViewById(R.id.checkBox3);
                CheckBox checkBox4=(CheckBox)view.findViewById(R.id.checkBox4);
                CheckBox checkBox5=(CheckBox)view.findViewById(R.id.checkBox5);
                CheckBox checkBox6=(CheckBox)view.findViewById(R.id.checkBox6);
                checkBox1.setEnabled(false);
                checkBox2.setEnabled(false);
                checkBox3.setEnabled(false);
                checkBox4.setEnabled(false);
                checkBox5.setEnabled(false);
                checkBox6.setEnabled(false);
                checkBox1.setVisibility(View.GONE);
            	checkBox2.setVisibility(View.GONE);
            	checkBox3.setVisibility(View.GONE);
            	checkBox4.setVisibility(View.GONE);
            	checkBox5.setVisibility(View.GONE);
            	checkBox6.setVisibility(View.GONE);
                ImageView iv_complete=(ImageView)view.findViewById(R.id.iv_complete);
                title.setText(key);
                //detail.setText(userObject.optString(key));
                Button changeBtn=(Button)view.findViewById(R.id.bt_changeNumber);
                if(key.equals("身份验证"))
                {
                	detail.setVisibility(View.GONE);
                	ll_checkbox.setVisibility(View.VISIBLE);
                	checkBox1.setVisibility(View.VISIBLE);
                	if(user.getsStatus().equals("班主任"))
                	{
                		checkBox1.setEnabled(true);
                	}
                	checkBox1.setText(userObject.optString(key));
                	if(userObject.optString(key).equals("已验证"))
                		checkBox1.setChecked(true);
                	else
                		checkBox1.setChecked(false);
                	checkBox1.setTag(userObject.optString(key));
                	checkBox1.setOnClickListener(new OnClickListener(){
						@Override
						 public void onClick(final View v) {
			                final boolean checked = ((CheckBox) v).isChecked();
			                if(!checked)
			                {
			                	if(completeResult.optInt("领取校园卡")==1 || completeResult.optInt("分配宿舍")==1 || completeResult.optInt("领宿舍钥匙")==1 )
			                	{
			                		AppUtility.showErrorToast(BaodaoHandleActivity.this,
			            					"已有后续步骤已完成，本步骤无法取消");
			                		((CheckBox) v).setChecked(true);
			                		return;
			                	}
			                	new Builder(BaodaoHandleActivity.this).setTitle("确认撤销吗？") 
			                    .setIcon(android.R.drawable.ic_menu_info_details) 
			                    .setCancelable(false)
			                    .setPositiveButton("是", new DialogInterface.OnClickListener() { 
			                        @Override 
			                        public void onClick(DialogInterface dialog, int which) { 
			                        	updateBaodao(key,checked,null);
			                        } 
			                    }) 
			                    .setNegativeButton("否", new DialogInterface.OnClickListener() { 
			                        @Override 
			                        public void onClick(DialogInterface dialog, int which) { 
			                        	((CheckBox) v).setChecked(true);
			                        } 
			                    })
			                    .show(); 
			                }
			                else
			                	updateBaodao(key,checked,null);

						}
                	});
                }
                else if(key.equals("收取材料"))
                {
                	detail.setVisibility(View.GONE);
                	ll_checkbox.setVisibility(View.VISIBLE);
                	String[] ziliaoArray=userObject.optString(key).split("\n");
                	for(int i=0;i<ziliaoArray.length;i++)
                	{
                		CheckBox cb=(CheckBox)ll_checkbox.getChildAt(i);
                		cb.setVisibility(View.VISIBLE);
                		if(user.getsStatus().equals("班主任"))
                			cb.setEnabled(true);
                		final String[] itemArray=ziliaoArray[i].split(":");
                		cb.setText(itemArray[0]);
                		if(itemArray.length==2 && itemArray[1]!=null && itemArray[1].equals("已提交"))
                			cb.setChecked(true);
                    	else
                    		cb.setChecked(false);
                		cb.setOnClickListener(new OnClickListener(){
    						@Override
    						 public void onClick(final View v) {
    			                final boolean checked = ((CheckBox) v).isChecked();
    			                if(checked && conditionVerify())
    			                {
    			                	((CheckBox) v).setChecked(false);
    			                	return;
    			                }
    			                if(!checked)
    			                {
    			                	new Builder(BaodaoHandleActivity.this).setTitle("确认撤销吗？") 
    			                    .setIcon(android.R.drawable.ic_menu_info_details) 
    			                    .setCancelable(false)
    			                    .setPositiveButton("是", new DialogInterface.OnClickListener() { 
    			                        @Override 
    			                        public void onClick(DialogInterface dialog, int which) { 
    			                        	updateBaodao(itemArray[0],checked,null);
    			                        } 
    			                    }) 
    			                    .setNegativeButton("否", new DialogInterface.OnClickListener() { 
    			                        @Override 
    			                        public void onClick(DialogInterface dialog, int which) { 
    			                        	((CheckBox) v).setChecked(true);
    			                        } 
    			                    })
    			                    .show(); 
    			                }
    			                else
    			                	updateBaodao(itemArray[0],checked,null);
    			                	
    						}
                    	});
                	}
                }
                else  if(key.equals("领取校园卡"))
                {
                	detail.setVisibility(View.GONE);
                	ll_checkbox.setVisibility(View.VISIBLE);
                	checkBox1.setVisibility(View.VISIBLE);
                	if(user.getsStatus().equals("班主任"))
                	{
                		checkBox1.setEnabled(true);
                	}
                	checkBox1.setText(userObject.optString(key));
                	if(userObject.optString(key).equals("未领取"))
                		checkBox1.setChecked(false);
                	else
                		checkBox1.setChecked(true);
                	checkBox1.setTag(userObject.optString(key));
                	checkBox1.setOnClickListener(new OnClickListener(){
						@Override
						 public void onClick(final View v) {
			                final boolean checked = ((CheckBox) v).isChecked();
			                if(checked && conditionVerify())
			                {
			                	((CheckBox) v).setChecked(false);
			                	return;
			                }
			                if(!checked)
			                {
			                	new Builder(BaodaoHandleActivity.this).setTitle("确认撤销吗？") 
			                    .setIcon(android.R.drawable.ic_menu_info_details) 
			                    .setCancelable(false)
			                    .setPositiveButton("是", new DialogInterface.OnClickListener() { 
			                        @Override 
			                        public void onClick(DialogInterface dialog, int which) { 
			                        	updateBaodao(key,checked,null);
			                        } 
			                    }) 
			                    .setNegativeButton("否", new DialogInterface.OnClickListener() { 
			                        @Override 
			                        public void onClick(DialogInterface dialog, int which) { 
			                        	((CheckBox) v).setChecked(true);
			                        } 
			                    })
			                    .show(); 
			                }
			                else
			                	updateBaodao(key,checked,null);
			                	
						}
                	});
                }
                else  if(key.equals("分配宿舍"))
                {
                	detail.setVisibility(View.GONE);
                	ll_checkbox.setVisibility(View.VISIBLE);
                	checkBox1.setVisibility(View.VISIBLE);
                	if(user.getsStatus().equals("班主任"))
                	{
                		checkBox1.setEnabled(true);
                	}
                	checkBox1.setText(userObject.optString(key));
                	if(userObject.optString(key).equals("未分配"))
                		checkBox1.setChecked(false);
                	else
                		checkBox1.setChecked(true);
                	checkBox1.setTag(userObject.optString(key));
                	checkBox1.setOnClickListener(new OnClickListener(){
						@Override
						 public void onClick(final View v) {
			                final boolean checked = ((CheckBox) v).isChecked();
			                if(checked && conditionVerify())
			                {
			                	((CheckBox) v).setChecked(false);
			                	return;
			                }
			                if(!checked)
			                {
			                	if(completeResult.optInt("领宿舍钥匙")==1)
			                	{
			                		AppUtility.showErrorToast(BaodaoHandleActivity.this,
			            					"已有后续步骤已完成，本步骤无法取消");
			                		((CheckBox) v).setChecked(true);
			                		return;
			                	}
			                }
			                else
			                {
			                	if(userObject.optString("就读方式").equals("走读"))
			                	{
			                		AppUtility.showErrorToast(BaodaoHandleActivity.this,
			            					"走读生无需分配宿舍");
			                		((CheckBox) v).setChecked(false);
			                		return;
			                	}
			                }
			                if(!checked)
			                {
			                	new Builder(BaodaoHandleActivity.this).setTitle("确认撤销吗？") 
			                    .setIcon(android.R.drawable.ic_menu_info_details) 
			                    .setCancelable(false)
			                    .setPositiveButton("是", new DialogInterface.OnClickListener() { 
			                        @Override 
			                        public void onClick(DialogInterface dialog, int which) { 
			                        	updateBaodao(key,checked,null);
			                        } 
			                    }) 
			                    .setNegativeButton("否", new DialogInterface.OnClickListener() { 
			                        @Override 
			                        public void onClick(DialogInterface dialog, int which) { 
			                        	((CheckBox) v).setChecked(true);
			                        } 
			                    })
			                    .show(); 
			                }
			                else
			                {
			                	Intent intent=new Intent(BaodaoHandleActivity.this,DormBedActivity.class);
			                	intent.putExtra("编号", ID);
			                	intent.putExtra("性别", userObject.optString("性别"));
			                	startActivityForResult(intent,101);
			                }
			                
			                
			                	
						}
                	});
                }
                else  if(key.equals("领宿舍钥匙"))
                {
                	detail.setVisibility(View.GONE);
                	ll_checkbox.setVisibility(View.VISIBLE);
                	checkBox1.setVisibility(View.VISIBLE);
                	if(user.getsStatus().equals("宿舍管理员"))
                		checkBox1.setEnabled(true);
                	checkBox1.setText(userObject.optString(key));
                	if(userObject.optString(key).equals("未领取"))
                		checkBox1.setChecked(false);
                	else
                		checkBox1.setChecked(true);
                	checkBox1.setTag(userObject.optString(key));
                	checkBox1.setOnClickListener(new OnClickListener(){
						@Override
						 public void onClick(final View v) {
			                final boolean checked = ((CheckBox) v).isChecked();
			                if(checked && conditionVerify())
			                {
			                	((CheckBox) v).setChecked(false);
			                	return;
			                }
			                if(checked && completeResult.optInt("分配宿舍")==0)
			                {
			                	AppUtility.showErrorToast(BaodaoHandleActivity.this,
		            					"请先分配宿舍");
		                		((CheckBox) v).setChecked(false);
		                		return;
			                }
			                if(!checked)
			                {
			                	new Builder(BaodaoHandleActivity.this).setTitle("确认撤销吗？") 
			                    .setIcon(android.R.drawable.ic_menu_info_details) 
			                    .setCancelable(false)
			                    .setPositiveButton("是", new DialogInterface.OnClickListener() { 
			                        @Override 
			                        public void onClick(DialogInterface dialog, int which) { 
			                        	updateBaodao(key,checked,null);
			                        } 
			                    }) 
			                    .setNegativeButton("否", new DialogInterface.OnClickListener() { 
			                        @Override 
			                        public void onClick(DialogInterface dialog, int which) { 
			                        	((CheckBox) v).setChecked(true);
			                        } 
			                    })
			                    .show(); 
			                }
			                else
			                	updateBaodao(key,checked,null);
			                
			                	
						}
                	});
                }
                else  if(key.equals("就读方式"))
                {
                	detail.setVisibility(View.GONE);
                	ll_checkbox.setVisibility(View.GONE);
                	if(user.getsStatus().equals("班主任"))
                	{
                		rg_jiudufangshi.setVisibility(View.VISIBLE);
                		
                		for(int i=0;i<rg_jiudufangshi.getChildCount();i++)
                		{
                			RadioButton item=(RadioButton)rg_jiudufangshi.getChildAt(i);
                			if(item.getText().equals(userObject.optString(key)))
                			{
                				item.setChecked(true);
                				break;
                			}
                		}

                		rg_jiudufangshi.setTag(userObject.optString(key));
                		rg_jiudufangshi.setOnCheckedChangeListener(new OnCheckedChangeListener(){
    						@Override
    						public void onCheckedChanged(RadioGroup group, int checkedId){
    							
    							RadioButton selItem=(RadioButton)group.findViewById(checkedId);
    							if(selItem.isPressed())
        						{
	    							if(checkedId==R.id.radio2)
	    							{
	    								if(completeResult.optInt("分配宿舍")==1 || completeResult.optInt("领宿舍钥匙")==1)
	    								{
	    									AppUtility.showErrorToast(BaodaoHandleActivity.this,
	        		            					"已分配宿舍或已领宿舍钥匙，请先撤销");
	    									for(int i=0;i<group.getChildCount();i++)
	    			                		{
	    			                			RadioButton item=(RadioButton)group.getChildAt(i);
	    			                			if(item.getText().equals(userObject.optString(key)))
	    			                			{
	    			                				item.setChecked(true);
	    			                				break;
	    			                			}
	    			                		}
	        		                		return;
	    								}
	    								
	    							}
	    							
	    			                updateBaodao(selItem.getText().toString(),true,null);
    							}
    			                	
    						}
                    	});
                	}
                	else
                	{
                		detail.setVisibility(View.VISIBLE);
                		detail.setText(userObject.optString(key));
                	}
                	
                }
                else  if(key.equals("缴费"))
                {
                	detail.setVisibility(View.GONE);
                	ll_checkbox.setVisibility(View.VISIBLE);
					checkBox1.setVisibility(View.VISIBLE);
					if(user.getsStatus().equals("班主任"))
					{
						checkBox1.setEnabled(true);
					}
					checkBox1.setText(userObject.optString(key));
					if(userObject.optString(key).equals("未缴费"))
						checkBox1.setChecked(false);
					else
						checkBox1.setChecked(true);
					checkBox1.setTag(userObject.optString(key));
					checkBox1.setOnClickListener(new OnClickListener(){
						@Override
						public void onClick(final View v) {
							final boolean checked = ((CheckBox) v).isChecked();
							if (checked && conditionVerify()) {
								((CheckBox) v).setChecked(false);
								return;
							}
							if (!checked) {
								new Builder(BaodaoHandleActivity.this).setTitle("确认撤销吗？")
										.setIcon(android.R.drawable.ic_menu_info_details)
										.setCancelable(false)
										.setPositiveButton("是", new DialogInterface.OnClickListener() {
											@Override
											public void onClick(DialogInterface dialog, int which) {
												updateBaodao(key, checked, null);
											}
										})
										.setNegativeButton("否", new DialogInterface.OnClickListener() {
											@Override
											public void onClick(DialogInterface dialog, int which) {
												((CheckBox) v).setChecked(true);
											}
										})
										.show();
							} else
								updateBaodao(key, checked, null);
						}
					});
                }
                else
                {
                	detail.setVisibility(View.VISIBLE);
                	ll_checkbox.setVisibility(View.GONE);
                	detail.setText(userObject.optString(key));
                }
                	
                if(completeResult!=null && completeResult.optString(key)!=null && completeResult.optString(key).length()>0)
                {
                	iv_complete.setVisibility(View.VISIBLE);
                	if(completeResult.optInt(key)==1)
                		iv_complete.setImageResource(R.drawable.complete);
                	else
                		iv_complete.setImageResource(R.drawable.uncomplete);
                }
                else
                	iv_complete.setVisibility(View.GONE);
                	
            } 
            
            return view;
        }
       
         
    }
	private boolean conditionVerify()
	{
    	if(completeResult.optInt("身份验证")==0)
    	{
    		AppUtility.showErrorToast(BaodaoHandleActivity.this,
					"请先进行身份验证");
    		return true;
    	}
    	return false;
	}
	private void  updateBaodao(String key, boolean checked,JSONObject otherParams)
	{
		showProgress1(true);
		String dataResult = "";
		Locale locale = getResources().getConfiguration().locale;
	    String language = locale.getCountry();
		try {
			JSONObject jsonObj = new JSONObject();
			jsonObj.put("action", key);
			jsonObj.put("编号", ID);
			jsonObj.put("checked", checked);
			jsonObj.put("userid", user.getUsername());
			jsonObj.put("language", language);
			jsonObj.put("client", "Android");
			jsonObj.put("otherParams",otherParams);
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
	private void popPayDlg(final String key,final View v)
	{
		String title="正常交费";
		LayoutInflater inflater = LayoutInflater.from(this);
		final View textEntryView = inflater.inflate(R.layout.dialog_green_channel,
			    null);
		textEntryView.setOnTouchListener(touchListener);
		TextView tv_totalmoney=(TextView)textEntryView.findViewById(R.id.tv_totalmoney);
		tv_totalmoney.setText("应交学费："+userObject.optString("收费标准")+"元");
		final RadioGroup rg_ifpay=(RadioGroup)textEntryView.findViewById(R.id.rg_ifpay);
		rg_ifpay.setVisibility(View.GONE);
		final LinearLayout ll_delay=(LinearLayout)textEntryView.findViewById(R.id.ll_delay);
		final LinearLayout ll_reduce=(LinearLayout)textEntryView.findViewById(R.id.ll_reduce);
		ll_delay.setVisibility(View.GONE);
		ll_reduce.setVisibility(View.GONE);
		final EditText ed_fapiao=(EditText)textEntryView.findViewById(R.id.ed_fapiao);
		
        Builder builder = new Builder(this);
        builder.setTitle(title).setView(textEntryView)
		.setPositiveButton("确定", new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				JSONObject obj=new JSONObject();
				try {
						obj.put("交费类型","正常交费");
						obj.put("发票号",ed_fapiao.getText().toString());
						
				} catch (Exception e) {
					// TODO Auto-generated catch block
					AppUtility.showToastMsg(BaodaoHandleActivity.this, e.getMessage());
					((CheckBox) v).setChecked(false);
					return;
				}
				updateBaodao(key,true,obj);
			}
			
		}).setNegativeButton("取消", new DialogInterface.OnClickListener(){

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				((CheckBox) v).setChecked(false);
			}
			
		});
        Dialog searchDialog=builder.create();
        searchDialog.show();
	}
	private void popGreenDlg(final String key,final View v)
	{
		String title="绿色通道";
		LayoutInflater inflater = LayoutInflater.from(this);
		final View textEntryView = inflater.inflate(R.layout.dialog_green_channel,
			    null);
		textEntryView.setOnTouchListener(touchListener);
		TextView tv_totalmoney=(TextView)textEntryView.findViewById(R.id.tv_totalmoney);
		tv_totalmoney.setText("应交学费："+userObject.optString("收费标准")+"元");
		final RadioGroup rg_ifpay=(RadioGroup)textEntryView.findViewById(R.id.rg_ifpay);
		final LinearLayout ll_normal=(LinearLayout)textEntryView.findViewById(R.id.ll_normal);
		ll_normal.setVisibility(View.GONE);
		final LinearLayout ll_delay=(LinearLayout)textEntryView.findViewById(R.id.ll_delay);
		ll_delay.setVisibility(View.VISIBLE);
		final LinearLayout ll_reduce=(LinearLayout)textEntryView.findViewById(R.id.ll_reduce);
		ll_reduce.setVisibility(View.GONE);
		final EditText ed_other=(EditText)textEntryView.findViewById(R.id.ed_other);
		final EditText ed_cutoffdate=(EditText)textEntryView.findViewById(R.id.ed_cutoffdate);
		ed_other.setVisibility(View.GONE);
		final EditText ed_reduce_reason=(EditText)textEntryView.findViewById(R.id.ed_reduce_reason);
		
		// 初始化控件
		final Spinner spinner1 = (Spinner) textEntryView.findViewById(R.id.sp_delay_item);
		final Spinner spinner2 = (Spinner) textEntryView.findViewById(R.id.sp_delay_reason);
		// 建立数据源
		
		String[] mItems;
		final String[] mReasons;
		try {
			JSONArray delayItems=otherObject.getJSONArray("缓交项目");
			JSONArray delayReasons=otherObject.getJSONArray("缓交原因");
			if(delayItems==null || delayItems.length()==0)
			{
				AppUtility.showToastMsg(this, "缓交项目为空");
				return;
			}
			if(delayReasons==null || delayReasons.length()==0)
			{
				AppUtility.showToastMsg(this, "缓交原因为空");
				return;
			}
			mItems = new String[delayItems.length()];
			for(int i=0;i<delayItems.length();i++)
			{
				mItems[i]=delayItems.getString(i);
			}
			ed_cutoffdate.setText(otherObject.getString("缓交截止日期"));
			ArrayAdapter<String> adapter=new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, mItems);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			//绑定 Adapter到控件
			spinner1.setAdapter(adapter);
			mReasons=new String[delayReasons.length()];
			for(int i=0;i<delayReasons.length();i++)
			{
				mReasons[i]=delayReasons.getString(i);
			}
			ArrayAdapter<String> adapter2=new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, mReasons);
			adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			//绑定 Adapter到控件
			spinner2.setAdapter(adapter2);
			spinner2.setOnItemSelectedListener(new OnItemSelectedListener(){
				@Override
				public void onItemSelected(AdapterView<?> parent, View view,
						int position, long id) {
					if(mReasons[position].equals("其他"))
						ed_other.setVisibility(View.VISIBLE);
					else
						ed_other.setVisibility(View.GONE);
					
				}
				@Override
				public void onNothingSelected(AdapterView<?> parent) {
					// TODO Auto-generated method stub
					
				}
				
			});
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			AppUtility.showToastMsg(this, e.getLocalizedMessage());
			return;
		}
		
		final EditText ed_reduce_money=(EditText)textEntryView.findViewById(R.id.ed_reduce_money);
		final TextView tv_real_money=(TextView)textEntryView.findViewById(R.id.tv_real_money);
		ed_reduce_money.setOnFocusChangeListener(new OnFocusChangeListener(){
		       
	        @Override
	        public void onFocusChange(View arg0, boolean arg1) {
	            EditText et = (EditText) arg0;
	            if(arg1) {
	                //Log.e("", "获得焦点"+detailItem.getId());
	            } else {
	            	updateYingJiao(et,tv_real_money);
	                //Log.e("", "失去焦点"+detailItem.getId());
	                
	            }
	        }
	         
	    });
		
		ed_reduce_money.setOnEditorActionListener(new EditText.OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE) { 
					updateYingJiao(ed_reduce_money,tv_real_money);
				}
				return false;
			}  
			  
		  
		      
		});
		rg_ifpay.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            
            @Override
            public void onCheckedChanged(RadioGroup arg0, int arg1) {
            	if(arg1==R.id.rb_ifpay1)
            	{
            		ll_delay.setVisibility(View.VISIBLE);
            		ll_reduce.setVisibility(View.GONE);
            	}
            	else
            	{
            		ll_delay.setVisibility(View.GONE);
            		ll_reduce.setVisibility(View.VISIBLE);
            	}
            }
        });
        Builder builder = new Builder(this);
        builder.setTitle(title).setView(textEntryView)
		.setPositiveButton("确定", new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				JSONObject obj=new JSONObject();
				try {
					
					if(rg_ifpay.getCheckedRadioButtonId()==R.id.rb_ifpay2)
					{
						int jianmian=0;
						if(ed_reduce_money.getText().toString()!=null && ed_reduce_money.getText().toString().length()>0)
							jianmian=Integer.parseInt(ed_reduce_money.getText().toString());
						if(jianmian<=0)
						{
							throw new Exception("减免金额必须大于0");
						}
						int yingjiao=userObject.optInt("收费标准");
						if(jianmian>yingjiao)
						{
							throw new Exception("减免金额不能大于应交金额");
						}
						obj.put("交费类型","减免");
						obj.put("减免金额", jianmian);
						obj.put("减免原因", ed_reduce_reason.getText().toString());
					}
					else if(rg_ifpay.getCheckedRadioButtonId()==R.id.rb_ifpay1)
					{
						if(!DateHelper.valid(ed_cutoffdate.getText().toString()))
						{
							throw new Exception("日期格式应为:2018-01-01");
						}
						obj.put("交费类型","缓交");
						obj.put("缓交项目", spinner1.getSelectedItem());
						if(ed_other.getVisibility()==View.VISIBLE && ed_other.getText().toString()!=null && ed_other.getText().toString().length()>0)
							obj.put("缓交原因", ed_other.getText().toString());
						else
							obj.put("缓交原因", spinner2.getSelectedItem());
						obj.put("缓交截止日期", ed_cutoffdate.getText().toString());
					}
					
						
				} catch (Exception e) {
					// TODO Auto-generated catch block
					AppUtility.showToastMsg(BaodaoHandleActivity.this, e.getMessage());
					((CheckBox) v).setChecked(false);
					return;
				}
				updateBaodao(key,true,obj);
			}
			
		}).setNegativeButton("取消", new DialogInterface.OnClickListener(){

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				((CheckBox) v).setChecked(false);
			}
			
		});
        Dialog searchDialog=builder.create();
        searchDialog.show();
		
	}
	private void updateYingJiao(EditText et,TextView tv)
	{
		int jianmian=0;
    	int yingjiao=0;
    	if(et.getText().toString().length()>0)
    	{
	    	try
	        {
	    		jianmian=Integer.parseInt(et.getText().toString());
	    		yingjiao=userObject.optInt("收费标准");
	        }
	        catch(NumberFormatException e)
	        {
	        	AppUtility.showToastMsg(BaodaoHandleActivity.this, "请输入数字");
	        	et.setText("");
	        	return;
	        }
    	}
    	tv.setText("应交："+(yingjiao-jianmian));
	}
	private OnTouchListener touchListener= new OnTouchListener(){

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			v.setFocusable(true);
			v.setFocusableInTouchMode(true);
			v.requestFocus();
			closeInputMethod(v);
			return false;
		}
		
	};
	private void closeInputMethod(View v) {
	    InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
	    boolean isOpen = imm.isActive();
	    if (isOpen) {
	        // imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);//没有显示则显示
	    	imm.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
	    }
	}
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (resultCode) { //resultCode为回传的标记，我在B中回传的是RESULT_OK
		   case RESULT_OK:
		    Bundle b=data.getExtras(); //data为B中回传的Intent
		    if(b!=null)
		    {
		    	String action=b.getString("action");//str即为回传的值
			    try {
					completeResult.put(action, b.getInt("完成情况"));
					userObject.put(action, b.getString("显示值"));
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		    }
		    adapter.notifyDataSetChanged();
			
		    break;
		default:
		    break;
		    }
		}
}
