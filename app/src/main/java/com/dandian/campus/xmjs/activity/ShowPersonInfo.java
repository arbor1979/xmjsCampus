package com.dandian.campus.xmjs.activity;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.text.InputType;
import android.text.util.Linkify;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxStatus;
import com.androidquery.callback.BitmapAjaxCallback;
import com.androidquery.callback.ImageOptions;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.dandian.campus.xmjs.BuildConfig;
import com.dandian.campus.xmjs.CampusApplication;
import com.dandian.campus.xmjs.R;
import com.dandian.campus.xmjs.api.CampusAPI;
import com.dandian.campus.xmjs.api.CampusException;
import com.dandian.campus.xmjs.api.CampusParameters;
import com.dandian.campus.xmjs.api.RequestListener;
import com.dandian.campus.xmjs.base.Constants;
import com.dandian.campus.xmjs.base.ExitApplication;
import com.dandian.campus.xmjs.db.DatabaseHelper;
import com.dandian.campus.xmjs.entity.AlbumImageInfo;
import com.dandian.campus.xmjs.entity.ContactsMember;
import com.dandian.campus.xmjs.entity.User;
import com.dandian.campus.xmjs.util.AppUtility;
import com.dandian.campus.xmjs.util.Base64;
import com.dandian.campus.xmjs.util.DialogUtility;
import com.dandian.campus.xmjs.util.FileUtility;
import com.dandian.campus.xmjs.util.HttpMultipartPost;
import com.dandian.campus.xmjs.util.ImageUtility;
import com.dandian.campus.xmjs.util.PrefUtility;
import com.dandian.campus.xmjs.util.TimeUtility;
import com.dandian.campus.xmjs.util.AppUtility.CallBackInterface;

public class ShowPersonInfo extends Activity {

	public static final int REQUEST_CODE_TAKE_PICTURE = 2;// //设置图片操作的标志
	public static final int REQUEST_CODE_TAKE_CAMERA = 1;// //设置拍照操作的标志
	
	private String picturePath;
	private String studentId;
	private String userImage;
	private int picCount=0;
	private ArrayList<AlbumImageInfo> picList=new ArrayList<AlbumImageInfo>();
	AQuery aq;
	ContactsMember memberInfo;
	DatabaseHelper database;
	List<Map<String, Object>> list;
	User user;
	MyAdapter adapter;
	Button changeheader;
	private Dao<User, Integer> userDao;
	ImageView headImgView;
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_person_info);
        headImgView=(ImageView)findViewById(R.id.iv_pic);
		ExitApplication.getInstance().addActivity(this);
		studentId = getIntent().getStringExtra("studentId");
		userImage = getIntent().getStringExtra("userImage");
		try {
			userDao = getHelper().getUserDao();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		user=((CampusApplication)getApplicationContext()).getLoginUserObj();
		
		if(studentId.equals(user.getUserNumber()))
		{
			changeheader= (Button) findViewById(R.id.bt_changeHeader);
			changeheader.setVisibility(View.VISIBLE);
			changeheader.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
					showGetPictureDiaLog();
				}
				
			});
		}
		
		aq = new AQuery(this);
		query();
		getPrivateAlbum();
		initContent();
	}
	private DatabaseHelper getHelper() {
		if (database == null) {
			database = OpenHelperManager.getHelper(this, DatabaseHelper.class);

		}
		return database;
	}
	private void query() {
	
		if(memberInfo==null)
		{
			memberInfo=new ContactsMember();
			if(studentId.equals(user.getUserNumber()))
			{
				
				memberInfo.setName(user.getName());
				memberInfo.setGender(user.getGender());
				memberInfo.setStudentID(studentId.split("_")[2]);
				if(user.getUserType().equals("老师"))
				{
					memberInfo.setClassName(user.getDepartment());
					memberInfo.setChargeClass(user.getWithClass());
					memberInfo.setChargeKeCheng(user.getWithCourse());
					memberInfo.setStuPhone(user.getPhone());
				}
				else
				{
					memberInfo.setClassName(user.getsClass());
					memberInfo.setStuPhone(user.getsPhone());
				}
				
				memberInfo.setLoginTime(user.getLoginTime());
				memberInfo.setUserType(user.getUserType());
				memberInfo.setSchoolName(user.getCompanyName());
				memberInfo.setUserImage(user.getUserImage());
			}
			else
			{
				//memberInfo=((CampusApplication)getApplicationContext()).getLinkManDic().get(studentId);
				//AppUtility.showToastMsg(this,"正在刷新个人资料");
				memberInfo.setUserType("");
			}
		}
		else
			memberInfo.setSchoolName(user.getCompanyName());
	}
	
	private void initContent() {
		
		
		if(userImage==null || userImage.equals("null"))
			userImage=memberInfo.getUserImage();

        ImageOptions options = new ImageOptions();
        options.memCache=false;
        options.fileCache=true;

		options.targetWidth=200;
		options.round = 100;
		aq.id(R.id.iv_pic).image(userImage,options);
        /*
		final Bitmap bmold=aq.getCachedImage(userImage);
		if(flag && bmold!=null)
		{


		}
		else
		{
			BitmapAjaxCallback cb = new BitmapAjaxCallback(){
				 public void callback(String url, ImageView iv, Bitmap bm, AjaxStatus status){
		                if(bm==null) {
		                	bm=bmold;
		                } 
		                super.callback(url, iv, bm,status);
	                    Log.d("ShowPersonInfo", "status:"+status.getCode()+"url:"+url);
		            }       
	                
	        };
	        cb.url(userImage).memCache(flag).fileCache(flag).targetWidth(800).fallback(0).animation(0).round(400).timeout(30000);
	        aq.id(R.id.iv_pic).image(cb);
		}
		*/
        //ImageLoader.getInstance().displayImage(userImage,headImgView,TabHostActivity.headOptions);

		//aq.id(R.id.iv_pic).image(userImage,flag,flag,800,R.drawable.ic_launcher);
		aq.id(R.id.tv_name).text(memberInfo.getName());
		aq.id(R.id.user_type).text(memberInfo.getUserType());
		aq.id(R.id.setting_tv_title).text("用户信息");
		aq.id(R.id.back).clicked(new OnClickListener(){

			@Override
			public void onClick(View v) {
				finish();
			}
			
		});
		aq.id(R.id.iv_pic).clicked(new OnClickListener(){

			@Override
			public void onClick(View v) {
				DialogUtility.showImageDialog(ShowPersonInfo.this,userImage);
				
			}
			
		});
		String userType =user.getUserType();
		list = new ArrayList<Map<String, Object>>();
		if(memberInfo.getUserType().equals("老师"))
		{
			
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("title", "性别");
			map.put("info", memberInfo.getGender());
			list.add(map);
			if(userType.equals("老师"))
			{
				map = new HashMap<String, Object>();
				map.put("title", "手机");
				map.put("info", memberInfo.getStuPhone());
				list.add(map);
			}
			map = new HashMap<String, Object>();
			map.put("title", "电邮");
			map.put("info", memberInfo.getStuEmail());
			list.add(map);
			
			map = new HashMap<String, Object>();
			map.put("title", "单位");
			map.put("info", memberInfo.getSchoolName());
			list.add(map);
			
			map = new HashMap<String, Object>();
			map.put("title", "部门");
			map.put("info", memberInfo.getClassName());
			list.add(map);
			
			map = new HashMap<String, Object>();
			map.put("title", "所带班级");
			map.put("info", memberInfo.getChargeClass());
			list.add(map);
			
			map = new HashMap<String, Object>();
			map.put("title", "所带课程");
			map.put("info", memberInfo.getChargeKeCheng());
			list.add(map);
			
			map = new HashMap<String, Object>();
			map.put("title", "登录时间");
			map.put("info", memberInfo.getLoginTime());
			list.add(map);
			
		}
		else
		{
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("title", "性别");
			map.put("info", memberInfo.getGender());
			list.add(map);
			
			if(studentId.equals(user.getUserNumber()))
			{
				map = new HashMap<String, Object>();
				map.put("title", "手机");
				map.put("info", memberInfo.getStuPhone());
				list.add(map);
			}
			map = new HashMap<String, Object>();
			map.put("title", "单位");
			map.put("info", memberInfo.getSchoolName());
			list.add(map);
			
			map = new HashMap<String, Object>();
			map.put("title", "学号");
			map.put("info", memberInfo.getStudentID());
			list.add(map);
			
			map = new HashMap<String, Object>();
			map.put("title", "班级");
			map.put("info", memberInfo.getClassName());
			list.add(map);
			
			map = new HashMap<String, Object>();
			map.put("title", "登录时间");
			map.put("info", memberInfo.getLoginTime());
			list.add(map);
			
		}
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("title", "个人相册");
		map.put("info", "");
		list.add(map);
		/*
		SimpleAdapter adapter = new SimpleAdapter(this,list,R.layout.list_left_right,
				new String[]{"title","info"},
				new int[]{R.id.left_title,R.id.right_detail});
		*/
		adapter=new MyAdapter(this);
		aq.id(R.id.listView1).adapter(adapter);
		
	}
	
	public class MyAdapter extends BaseAdapter{
		 
        private LayoutInflater mInflater;
        private Context context;
        public MyAdapter(Context context){
            this.mInflater = LayoutInflater.from(context);
           this.context=context;
        }
        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return list.size();
        }
 
        @Override
        public Object getItem(int arg0) {
            // TODO Auto-generated method stub
            return null;
        }
 
        @Override
        public long getItemId(int arg0) {
            // TODO Auto-generated method stub
            return 0;
        }
 
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
             
            ViewHolder holder = null;
            
            if (convertView == null) {
                 
                holder=new ViewHolder();  
                 
                convertView = mInflater.inflate(R.layout.list_left_right, null);
                holder.title = (TextView)convertView.findViewById(R.id.left_title);
                holder.info = (TextView)convertView.findViewById(R.id.right_detail);
                holder.private_album = (LinearLayout)convertView.findViewById(R.id.private_album);
                holder.imageViews=new ImageView[4];
                holder.imageViews[0]= (ImageView)convertView.findViewById(R.id.theImage);
                holder.imageViews[1] = (ImageView)convertView.findViewById(R.id.imageView2);
                holder.imageViews[2] = (ImageView)convertView.findViewById(R.id.imageView3);
                holder.imageViews[3] = (ImageView)convertView.findViewById(R.id.imageView4);
                holder.bt_changeNumber= (Button)convertView.findViewById(R.id.bt_changeNumber);
                convertView.setTag(holder);
                 
            }else {
                 
                holder = (ViewHolder)convertView.getTag();
            }
            
            holder.title.setText((String)list.get(position).get("title"));
            holder.info.setText((String)list.get(position).get("info"));
            
            if(holder.title.getText().equals("手机") && studentId.equals(user.getUserNumber()))
            {
            	holder.bt_changeNumber.setVisibility(View.VISIBLE);
            	holder.bt_changeNumber.setOnClickListener(new OnClickListener(){

        			@Override
        			public void onClick(View v) {
        				final EditText et=new EditText(ShowPersonInfo.this);
        				et.setInputType(InputType.TYPE_CLASS_PHONE);
        				new AlertDialog.Builder(ShowPersonInfo.this).setTitle("请输入新的联系方式").setView(et)
        				.setPositiveButton("确定", new DialogInterface.OnClickListener()
        				{

        					@Override
        					public void onClick(DialogInterface dialog, int which) {
        						// TODO Auto-generated method stub
        						String newphone=et.getText().toString().trim();
        						if(et.length()!=11)
        						{
        							AppUtility.showToastMsg(ShowPersonInfo.this, "要求11位手机号码！");
        						}
        						else
        							updateUserPhone(newphone);
        					}
        					
        				}).setNegativeButton("取消", null).show();
        				TimeUtility.popSoftKeyBoard(ShowPersonInfo.this,et);
        			}
        			
        		});
            }
            else {
				holder.bt_changeNumber.setVisibility(View.GONE);
				Pattern pattern = Pattern.compile("^((13[0-9])|(15[^4])|(18[0,2,3,5-9])|(17[0-8])|(147))\\d{8}$");
				Linkify.addLinks(holder.info, pattern, "tel:", new Linkify.MatchFilter() {
					public final boolean acceptMatch(CharSequence s, int start, int end) {
						int digitCount = 0;

						for (int i = start; i < end; i++) {
							if (Character.isDigit(s.charAt(i))) {
								digitCount++;
								if (digitCount == 11) {
									return true;
								}
							}
						}
						return false;
					}
				}, Linkify.sPhoneNumberTransformFilter);
			}
            	
            if(holder.title.getText().equals("个人相册"))
            {
            	if(picCount==0)
            	{
            		holder.info.setText("暂时没有上传照片");
            		holder.private_album.setVisibility(View.GONE);
            	}
            	else
            	{
            		holder.info.setText("已上传了"+picCount+"张照片");
            		holder.private_album.setVisibility(View.VISIBLE);
            		AQuery aq = new AQuery(convertView);
            		for(int i=0;i<picList.size();i++)
            		{
            			AlbumImageInfo image=picList.get(i);
            			//aq.id(holder.imageViews[i]).image(image.getUrl(),false,true,120,R.drawable.empty_photo);
                        ImageLoader.getInstance().displayImage(image.getUrl(),holder.imageViews[i]);
            			holder.imageViews[i].setOnClickListener(new View.OnClickListener() {
                            
                            @Override
                            public void onClick(View v) {
                                Intent intent=new Intent(context,AlbumPersonalActivity.class);
                                intent.putExtra("hostId", studentId);
                                intent.putExtra("hostName", memberInfo.getName());
                                context.startActivity(intent);
                            }
                        });
            		}
            	}
            }
            else
            	holder.private_album.setVisibility(View.GONE);
            
             
             
            return convertView;
        }
        public final class ViewHolder{
          
            public TextView title;
            public TextView info;
            public LinearLayout private_album;
            public ImageView[] imageViews;
            public Button bt_changeNumber;
        }
         
    }
	private void updateUserPhone(String newphone)
	{
		long datatime = System.currentTimeMillis();
		String checkCode = PrefUtility.get(Constants.PREF_CHECK_CODE, "");
		JSONObject jo = new JSONObject();
		try {
			jo.put("action", "更新联系方式");
			jo.put("新号码", newphone);
			jo.put("用户较验码", checkCode);
			jo.put("DATETIME", datatime);
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		String base64Str = Base64.encode(jo.toString().getBytes());
		CampusParameters params = new CampusParameters();
		params.add(Constants.PARAMS_DATA, base64Str);
		CampusAPI.getDownloadSubject(params, "AlbumPraise.php", new RequestListener() {

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
				Message msg = new Message();
				msg.what = 2;
				msg.obj = response;
				mHandler.sendMessage(msg);
				
			}
		});
	}
	private void getPrivateAlbum() {
		

		long datatime = System.currentTimeMillis();
		String checkCode = PrefUtility.get(Constants.PREF_CHECK_CODE, "");
		JSONObject jo = new JSONObject();
		try {
			jo.put("action", "个人相册简介");
			jo.put("hostId", studentId);
			jo.put("用户较验码", checkCode);
			jo.put("DATETIME", datatime);
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		String base64Str = Base64.encode(jo.toString().getBytes());
		CampusParameters params = new CampusParameters();
		params.add(Constants.PARAMS_DATA, base64Str);
		CampusAPI.getDownloadSubject(params, "AlbumPraise.php", new RequestListener() {

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
				Message msg = new Message();
				msg.what = 1;
				msg.obj = response;
				mHandler.sendMessage(msg);
				
			}
		});
	}
	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {

		@SuppressLint("SimpleDateFormat")
		@Override
		public void handleMessage(Message msg) {
			
			String result = "";
			String resultStr = "";
			switch (msg.what) {
			case -1:// 请求失败
				
				AppUtility.showErrorToast(ShowPersonInfo.this,
						msg.obj.toString());
				break;
			case 1:
				
				result = msg.obj.toString();
				resultStr = "";
				if (AppUtility.isNotEmpty(result)) {
					try {
						resultStr = new String(Base64.decode(result
								.getBytes("GBK")));
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
				}
				try {
					JSONObject jo = new JSONObject(resultStr);
					picCount=jo.optInt("总数");
					JSONArray ja=jo.getJSONArray("最近");
					if(ja!=null)
					{
						picList.clear();
						for(int i=0;i<ja.length();i++)
						{
							AlbumImageInfo image=new AlbumImageInfo((JSONObject)ja.get(i));
							if(image!=null)
								picList.add(image);
						}
					}
					
						memberInfo=new ContactsMember(jo.getJSONObject("个人资料"));
						userImage=memberInfo.getUserImage();
						initContent();

						
					
				} catch (JSONException e) {
					e.printStackTrace();
				}
				break;
			case 2:
				result = msg.obj.toString();
				resultStr = "";
				if (AppUtility.isNotEmpty(result)) {
					try {
						resultStr = new String(Base64.decode(result
								.getBytes("GBK")));
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
				}
				try {
					JSONObject jo = new JSONObject(resultStr);
					if(jo.optString("结果").equals("成功"))
					{
						AppUtility.showToastMsg(ShowPersonInfo.this, "更新成功！");
						memberInfo.setStuPhone(jo.optString("新号码"));
						if(user.getUserType().equals("老师"))
							user.setPhone(memberInfo.getStuPhone());
						else
							user.setsPhone(memberInfo.getStuPhone());
						try {
							userDao.update(user);
						} catch (SQLException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						initContent();
					}
					else
						AppUtility.showToastMsg(ShowPersonInfo.this, "更新失败:"+jo.optString("结果"));
				} catch (JSONException e) {
					e.printStackTrace();
				}
				break;
			case 5:
				
				Bundle	upLoadbundle = (Bundle) msg.obj;
				result = upLoadbundle.getString("result");
				
				try {
					resultStr = new String(Base64.decode(result.getBytes("GBK")));
				} catch (UnsupportedEncodingException e1) {
					e1.printStackTrace();
				}
				
				try {
					JSONObject jo = new JSONObject(resultStr);
					
					if("OK".equals(jo.optString("STATUS"))){
						DialogUtility.showMsg(ShowPersonInfo.this, "上传成功！");
						userImage=jo.optString("新头像");
						user.setUserImage(userImage);
						userDao.update(user);
						initContent();
						Intent intent = new Intent("ChangeHead");
						intent.putExtra("newhead", userImage);
						sendBroadcast(intent);
						
					}else{
						DialogUtility.showMsg(ShowPersonInfo.this, "上传失败:"+jo.optString("STATUS"));
					}
				}catch (Exception e) {
					AppUtility.showToastMsg(ShowPersonInfo.this, e.getMessage());
					e.printStackTrace();
				}	
				break;
			}
		}
	};
	private void showGetPictureDiaLog() {
		View view = getLayoutInflater()
				.inflate(R.layout.view_get_picture, null);
		Button cancel = (Button) view.findViewById(R.id.cancel);
		TextView byCamera = (TextView) view.findViewById(R.id.tv_by_camera);
		TextView byLocation = (TextView) view.findViewById(R.id.tv_by_location);

		final AlertDialog ad = new AlertDialog.Builder(this).setView(view)
				.create();

		Window window = ad.getWindow();
		window.setGravity(Gravity.BOTTOM);// 在底部弹出
		window.setWindowAnimations(R.style.CustomDialog);
		ad.show();
		cancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ad.dismiss();
			}
		});
		byCamera.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (Build.VERSION.SDK_INT >= 23) 
				{
					if(AppUtility.checkPermission(ShowPersonInfo.this, 6,Manifest.permission.CAMERA))
						getPictureByCamera();
				}
				else
					getPictureByCamera();
				ad.dismiss();
			}
		});
		byLocation.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (Build.VERSION.SDK_INT >= 23) 
				{
					if(AppUtility.checkPermission(ShowPersonInfo.this,7,Manifest.permission.READ_EXTERNAL_STORAGE))
						getPictureFromLocation();
				}
				else
					getPictureFromLocation();
				ad.dismiss();
			}
		});
	}
	
	private synchronized void getPictureByCamera() {
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);// 调用android自带的照相机
		String sdStatus = Environment.getExternalStorageState();
		if (!sdStatus.equals(Environment.MEDIA_MOUNTED)) { // 检测sd是否可用
			AppUtility.showToastMsg(this, "没有安装SD卡，无法使用相机功能");
			return;
		}
		picturePath = FileUtility.getRandomSDFileName("jpg");
		
		File mCurrentPhotoFile = new File(picturePath);

		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
			intent.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".fileProvider", mCurrentPhotoFile)); //Uri.fromFile(tempFile)
		else {
			Uri uri = Uri.fromFile(mCurrentPhotoFile);
			intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
		}
		startActivityForResult(intent, REQUEST_CODE_TAKE_CAMERA);
	}
	
	public void getPictureFromLocation() {
		String status = Environment.getExternalStorageState();
		if (status.equals(Environment.MEDIA_MOUNTED)) {// 判断是否有SD卡
			/*
			Intent intent = new Intent();
			intent.setType("image/*");
			intent.setAction(Intent.ACTION_GET_CONTENT);
			startActivityForResult(intent, REQUEST_CODE_TAKE_PICTURE);
			*/
			Intent intent; 
			intent = new Intent(Intent.ACTION_PICK, 
			                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI); 
			startActivityForResult(intent, REQUEST_CODE_TAKE_PICTURE);
		} else {
			AppUtility.showToastMsg(this, "SD卡不可用");
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_CODE_TAKE_CAMERA: // 拍照返回
			if (resultCode == RESULT_OK) {
				rotateAndCutImage(new File(picturePath));
			}
			break;
		case REQUEST_CODE_TAKE_PICTURE:
			if (data != null) {
				
				//picturePath = data.getStringExtra("filepath");
				//String myImageUrl = data.getDataString();
				//Uri uri = Uri.parse(myImageUrl);
				Uri uri = data.getData();
				String[] pojo  = { MediaStore.Images.Media.DATA };
				CursorLoader cursorLoader = new CursorLoader(this, uri, pojo, null,null, null); 
				Cursor cursor = cursorLoader.loadInBackground();
				if(cursor!=null)
				{
					cursor.moveToFirst(); 
					picturePath = cursor.getString(cursor.getColumnIndex(pojo[0]));
				}
				else
				{
					if(uri.toString().startsWith("file://"))
					{
						picturePath=uri.toString().replace("file://", "");
					}
					else
					{
						AppUtility.showErrorToast(this, "获取相册图片失败");
						return;
					}
				}
			     
				
				String tempPath =FileUtility.getRandomSDFileName("jpg");
				if(FileUtility.copyFile(picturePath,tempPath))
					rotateAndCutImage(new File(tempPath));
				else
					AppUtility.showErrorToast(this, "向SD卡复制文件出错");
			}
			break;
		case 3:
			if (resultCode == 200 && data != null) {
				
				String picPath = data.getStringExtra("picPath");
				SubmitUploadFile(picPath);
			}
		default:
			break;
		}
	}
	private void rotateAndCutImage(final File file) {
		if(!file.exists()) return;
		if(AppUtility.formetFileSize(file.length()) > 5242880*2){
			AppUtility.showToastMsg(this, "对不起，您上传的文件太大了，请选择小于10M的文件！");
		}else{
			
			ImageUtility.rotatingImageIfNeed(file.getAbsolutePath());
			Intent intent=new Intent(this,CutImageActivity.class);
			intent.putExtra("picPath", file.getAbsolutePath());
			startActivityForResult(intent,3);
		}
	}
	
	public void SubmitUploadFile(String picPath){
		CampusParameters params = new CampusParameters();
		String checkCode = PrefUtility.get(Constants.PREF_CHECK_CODE, "");// 获取用户校验码
		/*
		params.add("用户较验码", checkCode);
		params.add("课程名称", downloadSubject.getCourseName());
		params.add("老师姓名", downloadSubject.getUserName());
		params.add("文件名", downloadSubject.getFileName());
		*/
		params.add("JiaoYanMa", checkCode);
		params.add("pic", picPath);
		params.add("TuPianLeiBie", "头像");
		HttpMultipartPost post = new HttpMultipartPost(this, params){
			@Override  
		    protected void onPostExecute(String result) {  
				Bundle bundle = new Bundle();
				bundle.putString("result", result);
				Message msg = new Message();
				msg.what = 5;
				msg.obj = bundle; 
				mHandler.sendMessage(msg);	
				this.pd.dismiss();
		    }
		};  
        post.execute();
	}
	@Override
    public void onSaveInstanceState(Bundle savedInstanceState){
		super.onSaveInstanceState(savedInstanceState);
		savedInstanceState.putSerializable("memberInfo",memberInfo);
		savedInstanceState.putString("picturePath", picturePath);
		
		
	}
	@Override
    public void onRestoreInstanceState(Bundle savedInstanceState){
        super.onRestoreInstanceState(savedInstanceState);
        memberInfo = (ContactsMember) savedInstanceState.getSerializable("memberInfo");
        picturePath=savedInstanceState.getString("picturePath");
    }
	@TargetApi(23)
	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
	{
		AppUtility.permissionResult(requestCode,grantResults,this,callBack);
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
	}
	public CallBackInterface callBack=new CallBackInterface()
	{

		@Override
		public void getLocation1() {
			// TODO Auto-generated method stub
		
		}

		@Override
		public void getPictureByCamera1() {
			// TODO Auto-generated method stub
			getPictureByCamera();
		}

		@Override
		public void getPictureFromLocation1() {
			// TODO Auto-generated method stub
			getPictureFromLocation();
		}

		@Override
		public void sendCall1() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void sendMsg1() {
			// TODO Auto-generated method stub
			
		}
		@Override
		public void getFujian1() {
			// TODO Auto-generated method stub
		}
		
	};
}
