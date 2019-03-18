package com.dandian.campus.xmjs.util;

import java.io.File;
import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.conn.util.InetAddressUtils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.DownloadManager.Request;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Application;
import android.app.DownloadManager;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.provider.Browser;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Toast;

import com.dandian.campus.xmjs.BuildConfig;
import com.dandian.campus.xmjs.R;
import com.dandian.campus.xmjs.activity.TabHostActivity;
import com.dandian.campus.xmjs.api.CampusAPI;
import com.dandian.campus.xmjs.api.CampusException;
import com.dandian.campus.xmjs.api.CampusParameters;
import com.dandian.campus.xmjs.api.RequestListener;
import com.dandian.campus.xmjs.base.Constants;
import com.dandian.campus.xmjs.entity.DownloadSubject;
import com.dandian.campus.xmjs.service.Alarmreceiver;
import com.dandian.campus.xmjs.widget.XListView;

import static android.content.Context.ALARM_SERVICE;

public class AppUtility {
	private static final String TAG = "AppUtility";
	private static Toast mToast;
	
	
	public static void setViewHeightBasedOnChildren(ExpandableListView listView) {
		// 获取ListView对应的Adapter
		ListAdapter listAdapter = listView.getAdapter();
		if (listAdapter == null) {
			// pre-condition
			return;
		}

		int totalHeight = 0;
		for (int i = 0, len = listAdapter.getCount(); i < len; i++) { // listAdapter.getCount()返回数据项的数目
			View listItem = listAdapter.getView(i, null, listView);
			listItem.measure(0, 0); // 计算子项View 的宽高
			totalHeight += listItem.getMeasuredHeight(); // 统计所有子项的总高度
		}

		ViewGroup.LayoutParams params = listView.getLayoutParams();
		params.height = totalHeight
				+ (listView.getDividerHeight() * (listAdapter.getCount() - 1));
		// listView.getDividerHeight()获取子项间分隔符占用的高度
		// params.height最后得到整个ListView完整显示需要的高度
		listView.setLayoutParams(params);
	}

	public static void setListViewHeightBasedOnChildren(
			ExpandableListView listView) {
		// ListAdapter listAdapter = listView.getAdapter();
		ExpandableListAdapter listAdapter = listView.getExpandableListAdapter();
		if (listAdapter == null) {
			return;
		}
		int totalHeight = 0;
		for (int i = 0; i < listAdapter.getGroupCount(); i++) {

			for (int j = 0; j < listAdapter.getChildrenCount(i); j++) {
				View listItem = listAdapter.getGroupView(i, true, null,
						listView);
				// getView(i, null, listView);
				listItem.measure(0, 0);
				totalHeight += listItem.getMeasuredHeight();
				System.out.println("---------totalheight----------"
						+ totalHeight);
			}
		}
		ViewGroup.LayoutParams params = listView.getLayoutParams();
		params.height = totalHeight;
//				+ (listView.getChildCount() * (listAdapter.getGroupCount() - 1));
		// params.height =totalHeight
		// + (listView.getDividerHeight() * (listAdapter.getGroupCount() - 1));
		System.out.println("----------height----------" + params.height);
		listView.setLayoutParams(params);
	}

	/**
	 * 功能描述:检查邮箱格式
	 * 
	 * @author zhuliang 2013-12-11 下午7:26:42
	 * 
	 * @param email
	 * @return
	 */
	public static final boolean checkEmail(String email) {
		Pattern pattern = Pattern
				.compile("^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$");
		Matcher matcher = pattern.matcher(email);
		return matcher.matches();
	}
	/**
	 * 功能描述:获取整个文件夹的大小
	 *
	 * @author linrr  2014-1-25 上午11:48:24
	 * 
	 * @param f
	 * @return
	 * @throws Exception
	 */
	public static long getFileSize(File f) throws Exception {
		long size = 0;
		File flist[] = f.listFiles();
		for (int i = 0; i < flist.length; i++) {
			if (flist[i].isDirectory()) {
				size = size + getFileSize(flist[i]);
			} else {
				size = size + flist[i].length();
			}
		}
		return size;
	}
	/**
	 * 功能描述:快速取消Toast提示
	 *
	 * @author linrr  2014-1-25 上午11:32:21
	 * 
	 * @param context
	 * @param msg
	 * @param duration
	 */
	public static void showToast(Context context, String msg, int duration) {
        // if (mToast != null) {
        // mToast.cancel();
        // }
        // mToast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
        if (mToast == null) {
                mToast = Toast.makeText(context, msg, duration);
        } else {
                mToast.setText(msg);
        }
        mToast.show();
}
   public static void cancelToast()
   {
	   if(mToast!=null)
		   mToast.cancel();
   }
	/**
	 * 功能描述:检查手机号码格式
	 * 
	 * @author zhuliang 2013-12-11 下午7:40:38
	 * 
	 * @param phone
	 * @return
	 */
	public static final boolean checkPhone(String phone) {
		//Pattern pattern = Pattern.compile("^((13[0-9])|(15[0-9])|(18[0-9])|(17[0-9])|(147))\\d{8}$");
		Pattern pattern = Pattern.compile("^(1\\d{10})$");
		Matcher matcher = pattern.matcher(phone);

		if (matcher.matches()) {
			return true;
		}
		return false;
	}

	public static void report(Throwable e) {
		if (e == null)
			return;
		try {
			Log.d("reporting", Log.getStackTraceString(e));

			if (eh != null) {
				eh.uncaughtException(Thread.currentThread(), e);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private static UncaughtExceptionHandler eh;

	public static void setExceptionHandler(UncaughtExceptionHandler handler) {
		eh = handler;
	}

	private static Context context;

	public static void setContext(Application app) {
		context = app.getApplicationContext();
	}

	public static Context getContext() {
		if (context == null) {
			Log.w(TAG, "getContext with null");
			Log.d(TAG, "debug", new IllegalStateException());
		}
		return context;
	}

	/**
	 * 获取sd卡的路径
	 * 
	 * @return
	 */
	@SuppressLint("SdCardPath")
	public static String getSDPath() {
		File sdDir = null;
		boolean sdCardExist = Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED); // 判断sd卡是否存在
		if (sdCardExist) {
			sdDir = Environment.getExternalStorageDirectory();// 获取跟目录
		}
		return sdDir.toString();
	}

	/**
	 * Show a prompt message
	 * 
	 * @param msg
	 *            message content
	 */
	public static void showToastMsg(Context context, String msg) {
		Toast toast=Toast.makeText(context, msg, Toast.LENGTH_SHORT);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
	}
	public static void showToastMsg(Context context, String msg,int duration) {
		if(context!=null && msg!=null)
		{
			Toast toast=Toast.makeText(context, msg, duration);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
		}
	}

	/**
	 * 生成UUID
	 * 
	 * @return
	 */
	public static String UUIDGenerator() {
		return UUID.randomUUID().toString().replaceAll("-", "");
	}

	/**
	 * MD5加密
	 * 
	 * @param str
	 * @return
	 */
	public static String MD5Encode(String str) {
		MessageDigest md5 = null;
		try {
			md5 = MessageDigest.getInstance("MD5");
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}

		char[] charArray = str.toCharArray();
		byte[] byteArray = new byte[charArray.length];

		for (int i = 0; i < charArray.length; i++) {
			byteArray[i] = (byte) charArray[i];
		}
		byte[] md5Bytes = md5.digest(byteArray);

		StringBuffer hexValue = new StringBuffer();
		for (int i = 0; i < md5Bytes.length; i++) {
			int val = ((int) md5Bytes[i]) & 0xff;
			if (val < 16) {
				hexValue.append("0");
			}
			hexValue.append(Integer.toHexString(val));
		}
		return hexValue.toString().substring(8, 24);
	}

	/**
	 * 判断字符串是否为数字
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isNumeric(String str) {
		Pattern pattern = Pattern.compile("[0-9]*");
		return pattern.matcher(str).matches();
	}
	/**
	 * 功能描述:将文件大小转换成MB,KB,GB形式
	 *
	 * @author linrr  2014-1-23 下午3:41:07
	 * 
	 * @param size
	 * @return
	 */
	public static int formetFileSize(long size){
		String length_display = null;
		int b_size = 0;
			if(size < 1024){
				length_display = size + "B";
			}else if(size < 1048576 && size >= 1024){
				if((size/1024+"").contains(".") && (size/1024+"").substring((size/1024+"").lastIndexOf(".")).length() > 3){
					length_display = (size/1024+"").substring(0,(size/1024+"").lastIndexOf(".")+3)  + "KB";
					b_size = Integer.parseInt(length_display.substring(0, length_display.indexOf("B")*1024));
				}else{
					length_display = size/1024*1024 + "B";
				}
			}else if(size <= 1073741824 && size >= 1048576){
				if((size/1048576+"").contains(".") && (size/1048576+"").substring((size/1048576+"").lastIndexOf(".")).length() > 3){
					length_display = (size/1048576+"").substring(0,(size/1048576+"").lastIndexOf(".")+3) + "MB";
				}else{
					length_display = size/1048576*1024*1024 + "B";
				}
				
			}else{
				if((size/1073741824+"").contains(".") && (size/1073741824+"").substring((size/1073741824+"").lastIndexOf(".")).length() > 3){
					length_display = (size/1073741824+"").substring(0,(size/1073741824+"").lastIndexOf(".")+3) + "GB";
				}else{
					length_display = size/1073741824 + "GB";
				}
				
			}
			b_size = Integer.parseInt(length_display.substring(0, length_display.indexOf("B")));
			return b_size;
		}

	/**
	 * Check if the network is available. <br/>
	 * need <uses-permission
	 * android:name="android.permission.ACCESS_NETWORK_STATE" />
	 * 
	 * @param context
	 *            The current context.
	 * @return True if the network is available,false otherwise.
	 */
	public static boolean getNetworkIsAvailable(Context context) {
		ConnectivityManager manager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = manager.getActiveNetworkInfo();
		if (info == null || !info.isConnected()) {
			return false;
		}
		if (info.isRoaming()) {
			return true;
		}
		return true;
	}

	/**
	 * Check if the SD card is available.Display an alert if not.
	 * 
	 * @param context
	 *            The current context.
	 * @param showMessage
	 *            If true, will display a message for the user.
	 * @return True if the SD Card is available, false otherwise.
	 */
	public static boolean checkCardState(Context context, boolean showMessage) {
		// Check to see if we have an SDCard.
		String status = Environment.getExternalStorageState();
		if (!status.equals(Environment.MEDIA_MOUNTED)) {
			int messageId;
			// Check to see if the SDCard is busy,same as the music app.
			if (status.equals(Environment.MEDIA_SHARED)) {
				messageId = R.string.Commons_SDCardErrorSDUnavailable;
			} else {
				messageId = R.string.Commons_SDCardErrorNoSDMsg;
			}
			if (showMessage) {
				AppUtility.showErrorDialog(context,
						R.string.Commons_SDCardErrorTitle, messageId);
			}
			return false;
		}
		return true;
	}

	/**
	 * Show an error dialog.
	 * 
	 * @param context
	 *            The current context.
	 * @param title
	 *            The title string id.
	 * @param message
	 *            The message string id.
	 */
	public static void showErrorDialog(Context context, int title, int message) {
		new AlertDialog.Builder(context).setTitle(title)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setMessage(message)
				.setPositiveButton(R.string.Commons_Ok, null).show();
	}

	/**
	 * 检查IP地址是否正确
	 * 
	 * @param ip
	 * @return
	 */
	public static boolean checkIPAddress(String ip) {
		Pattern pattern = Pattern
				.compile("\\b((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\.((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\.((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\.((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\b");
		Matcher matcher = pattern.matcher(ip);
		return matcher.matches();
	}

	/**
	 * 功能描述：字符串转换int
	 * 
	 * @author yanzy 2013-11-26 下午12:02:05
	 * 
	 * @param str
	 * @return
	 */
	public static int parseInt(String str) {
		if (!"".equals(str) && !"null".equals(str) && str != null
				&& isNumeric(str)) {
			return Integer.parseInt(str);
		} else {
			return -1;
		}
	}

	public static float parseFloat(String str) {
		if (!"".equals(str) && !"null".equals(str) && str != null
				&& isNumeric(str)) {
			return Float.parseFloat(str);
		} else {
			return (float) 0.0;
		}
	}

	/**
	 * 功能描述:判断字符串不为空
	 * 
	 * @author yanzy 2013-11-26 下午1:02:37
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isNotEmpty(String str) {
		return str != null && !"".equals(str) && !"null".equals(str);
	}

	/**
	 * get the IP address of the device
	 * 
	 * @return IP address add by zhuliang copy from wgz's AfterSale
	 */
	public static String getLocalIpAddress() {
		String deviceIp = null;
		boolean keepLookupOn = true;

		try {
			Enumeration<NetworkInterface> availableNetwork = NetworkInterface
					.getNetworkInterfaces();

			while (availableNetwork.hasMoreElements() && keepLookupOn) {
				NetworkInterface intf = availableNetwork.nextElement();
				Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses();

				while (enumIpAddr.hasMoreElements()) {
					InetAddress inetAddress = enumIpAddr.nextElement();

					deviceIp = inetAddress.getHostAddress().toString();

					if (!inetAddress.isLoopbackAddress()
							&& InetAddressUtils.isIPv4Address(deviceIp)) {
						keepLookupOn = false;
						break;
					}
				}
			}
		} catch (SocketException ex) {
			ex.printStackTrace();
		}
		return deviceIp;
	}

	public static String getStudentPicPath(String className, String studentId) {
		return "PocketCampus/" + className + "/" + studentId + "_230230.jpg";
	}
	
	public static String getFileSize(long filesize){
		float a = 0;
		String text="";
		if (filesize > 1024*1024) {
			a = (float)filesize/1024/1024;
			text = String.format("%.1f", a)+"M";
		}
		if (1024 <= filesize && filesize < 1024*1024) {
			a = (float)filesize/1024;
			text = String.format("%.1f", a)+"K";
		}
		if (0<= filesize && filesize < 1024) {
			text = String.format("%.1f", a)+"b";
		}
		return text;
	}
	
	/**
	 * 功能描述: 判断是否初始化基础数据
	 *
	 * @author yanzy  2013-12-26 下午12:12:28
	 * 
	 * @return
	 */
	public static boolean isInitBaseData(){
//		String init_flag = PrefUtility.get(Constants.PREF_INIT_BASE_FLAG, "");
//		String userNumber = PrefUtility.get(Constants.PREF_USER_NUNMBER, "");
//		System.out.println("-------------------->init_flag:"+init_flag);
//		System.out.println("-------------------->userNumber:"+userNumber);
//		boolean init = false;
//		if (init_flag.equals(userNumber)) {
//			init = true;
//		}
//		return init;
		return PrefUtility.getBoolean(Constants.PREF_INIT_BASEDATE_FLAG, false);
	}
	
	/**
	 * 功能描述: 判断是否初始化联系人
	 *
	 * @author yanzy  2013-12-26 下午12:12:28
	 * 
	 * @return
	 */
	public static boolean isInitContactData(){
//		String init_flag = PrefUtility.get(Constants.PREF_INIT_CONTACT_FLAG, "");
//		String userNumber = PrefUtility.get(Constants.PREF_USER_NUNMBER, "");
//		System.out.println("-------------------->init_flag:"+init_flag);
//		System.out.println("-------------------->userNumber:"+userNumber);
//		boolean init = false;
//		if (init_flag.equals(userNumber)) {
//			init = true;
//		}
//		return init;
		
		Log.d("TAG", "--->  "+PrefUtility.getBoolean(Constants.PREF_INIT_CONTACT_FLAG, false));
		
		return PrefUtility.getBoolean(Constants.PREF_INIT_CONTACT_FLAG, false);
	}
	
	/**
	 * 功能描述:获取日期和星期
	 *
	 * @author shengguo  2014-5-10 下午1:52:11
	 * 
	 * @param date
	 * @return
	 */
	public static String getWeekAndDate(Date date){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.SIMPLIFIED_CHINESE);
		String time = sdf.format(date);
		String[] weekDays = {"星期日","星期一","星期二","星期三","星期四","星期五","星期六"};
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)-1;
		//int day = calendar.DAY_OF_WEEK - 1;
		if(dayOfWeek < 0){
			dayOfWeek = 0;
		}
		return time +" "+ weekDays[dayOfWeek];
	}
	
	/**
	 * 功能描述:获取垂直文本
	 *
	 * @author zhuliang  2014-1-11 下午3:34:58
	 * 
	 * @param text
	 * @return
	 */
	public static String getVerticalText(String text){
		StringBuffer stringBuffer = new StringBuffer();  
        if (text != null && text.length() > 0) {  
            int length = text.length();  
            for (int i = 0; i < length; i++)  
                stringBuffer.append(text.charAt(i) + "\n");
        }
        stringBuffer.deleteCharAt(stringBuffer.lastIndexOf("\n"));
	    return stringBuffer.toString();
	}
	
	/**
	 * 功能描述:打印错误提示
	 *
	 * @author shengguo  2014-4-28 下午2:51:46
	 * 
	 * @param context
	 * @param exception
	 */
	public static void showErrorToast(Context context,String exception){
		String string = exception;
		Log.d(TAG, "e----->" + exception);
		if (exception.indexOf(":") != -1) {
			exception = exception.substring(0, exception.indexOf(":"));
		}
		exception = exception.substring(exception.lastIndexOf(".") + 1,
				exception.length());
		if (exception.equals("ConnectTimeoutException")) {
			string = "连接超时!";
		}
		if (exception.equals("IllegalArgumentException")) {
			string = "服务器地址错误!";
		}
		if (exception.equals("SocketTimeoutException")) {
			string = "服务器未响应!";
		}
		if (exception.equals("HttpHostConnectException")) {
			string = "请检查网络连接!";
		}
		if (exception.equals("UnknownHostException")) {
			string = "服务器无法访问，请检查网络连接";
		}
		AppUtility.showToastMsg(context, string,1);
	}
	//程序是否进入后台
	public static boolean isApplicationBroughtToBackground(final Context context) { 

	    ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE); 

	    List<RunningTaskInfo> tasks = am.getRunningTasks(1); 

	    if (!tasks.isEmpty()) { 

	        ComponentName topActivity = tasks.get(0).topActivity; 

	        if (!topActivity.getPackageName().equals(context.getPackageName())) { 

	            return true; 

	        } 

	    } 

	    return false; 

	}
	//是否处于锁屏
	public static boolean isLockScreen(final Context context) { 
		KeyguardManager kgm = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
		return kgm.inKeyguardRestrictedInputMode();
	}
	public static void playSounds(int sound, final Context context)
	{
		
	  final SoundPool sp = new SoundPool(1,AudioManager.STREAM_MUSIC,0);
	  sp.load(context, sound, 1);
	  sp.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener(){
		  
		  public void onLoadComplete(SoundPool soundPool, int sampleId, int status)
		  {
			  sp.play(sampleId,     //声音资源
			        1,         //左声道
			        1,         //右声道
			        0,             //优先级，0最低
			        0,         //循环次数，0是不循环，-1是永远循环
			        1);            //回放速度，0.5-2.0之间。1为正常速度
			  
		  }
		  
	  } );
	  
	  
	}
	public static int getPixByDip(Context context,int dip)
	{
		float scale = context.getResources().getDisplayMetrics().density;  
        return (int)(dip * scale); 
	}
	public static float getDipByPix(Context context,int Pix)
	{
		float scale = context.getResources().getDisplayMetrics().density;  
        return Pix/scale; 
	}
	/**
	 * 功能描述:开启课程提醒
	 * 
	 * @author shengguo 2014-5-24 下午5:17:18
	 * 
	 */
	
	public static void beginReminder(Context ct) {

		if(context!=null)
			ct=context;
		/*
		Intent intent = new Intent(ct, Alarmreceiver.class);
		intent.setAction("reminderMyClass");
		PendingIntent sender = PendingIntent.getBroadcast(ct,
				0, intent, 0);
		AlarmManager am = (AlarmManager) ct.getSystemService(Activity.ALARM_SERVICE);// 24小时一个周期，不停的发送广播
		am.cancel(sender);
		*/


		String remindClassTime = PrefUtility.get("remindClassTime", "前一天 20:00");
		String[] arrayStr=remindClassTime.split(" ");
		arrayStr=arrayStr[1].split(":");

		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis());
		calendar.set(Calendar.HOUR_OF_DAY, Integer.valueOf(arrayStr[0]));
		calendar.set(Calendar.MINUTE, Integer.valueOf(arrayStr[1]));
		calendar.set(Calendar.SECOND,0);
		if(calendar.getTime().before(new Date()))
		{
			calendar.add(Calendar.DAY_OF_MONTH, 1);
		}

		// 开始时间
		//invokeTimerPOIService(ct,firstime);
		Intent intent = new Intent(context, Alarmreceiver.class);
		intent.setAction("reminderMyClass");
		PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, 	PendingIntent.FLAG_UPDATE_CURRENT);

		AlarmManager manager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
		if(PrefUtility.getBoolean("booleanReminderDayClass", true))
		{
			manager.cancel(pi);
			manager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),	24*60*60*1000,pi);
		}
		else
			manager.cancel(pi);
		
	}
	public static void beginGPS(Context ct,String userType) {
		if(context!=null)
			ct=context;
		Intent intent = new Intent(ct, Alarmreceiver.class);
		intent.setAction("reportLocation");
		PendingIntent sender = PendingIntent.getBroadcast(ct,
				0, intent, 0);
		AlarmManager am = (AlarmManager) ct.getSystemService(ALARM_SERVICE);
		am.cancel(sender);
		if(userType.equals("学生"))
		{
			String dtStr=DateHelper.getToday("yyyy-MM-dd HH:00:00");
			Date dt=DateHelper.getStringDate(dtStr,null);
			am.setRepeating(AlarmManager.RTC_WAKEUP, dt.getTime(),60*60*1000,sender);
		}
		
	}	
	public static boolean checkPermission(final Activity act,final int MY_PERMISSIONS_REQUEST_Code,final String permission)
	{

		if (ContextCompat.checkSelfPermission(act,permission)
		!= PackageManager.PERMISSION_GRANTED)
		{
			/*
			if (ActivityCompat.shouldShowRequestPermissionRationale(act,permission)) 
			{
                new AlertDialog.Builder(act)
                        .setMessage("申请相机权限才能拍照")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //申请相机权限
                            	ActivityCompat.requestPermissions(act,new String[]{permission}, MY_PERMISSIONS_REQUEST_Camera);
                            }
                        })
                        .show();
                return false;
			}
            else
            {
            */
            	ActivityCompat.requestPermissions(act,new String[]{permission},MY_PERMISSIONS_REQUEST_Code);
            	return false;
            //}
		}else {
			return true;
		}

	}
	
	public static void permissionResult(int requestCode,int[] grantResults,Activity act,CallBackInterface callBack)
	{
		if (requestCode == 5 || requestCode == 11) //11为精确定位
		{
			if (grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
			{
				callBack.getLocation1(requestCode);
				//Toast.makeText(act, "授权成功", Toast.LENGTH_SHORT).show();
	
			} else
			{
				// Permission Denied
				Toast.makeText(act, "权限被拒绝", Toast.LENGTH_SHORT).show();
				goIntentSetting();
			}
	
		}
		if (requestCode == 6 || requestCode == 12) //12为扫描二维码
		{
			if (grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
			{
				callBack.getPictureByCamera1(requestCode);
				//Toast.makeText(context, "授权成功", Toast.LENGTH_SHORT).show();
	
			} else
			{
				// Permission Denied
				if (!ActivityCompat.shouldShowRequestPermissionRationale(act,Manifest.permission.CAMERA)) {
	                Toast.makeText(context, "相机权限已被禁止,请在设置-授权管理-应用权限管理中修改", Toast.LENGTH_SHORT).show();
	            }
	
			}
	
		}
	
		if (requestCode == 7)
		{
			if (grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
			{
				callBack.getPictureFromLocation1();
				//Toast.makeText(act, "授权成功", Toast.LENGTH_SHORT).show();
		
			} else
			{
				// Permission Denied
				if (!ActivityCompat.shouldShowRequestPermissionRationale(act,Manifest.permission.READ_EXTERNAL_STORAGE)) {
	                Toast.makeText(context, "读写存储权限已被禁止,请在设置-授权管理-应用权限管理中修改", Toast.LENGTH_SHORT).show();
	            }
		
			}
	
		}
		/*
		if (requestCode == 8)
		{
			if (grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
			{
				callBack.sendCall1();
				//Toast.makeText(act, "授权成功", Toast.LENGTH_SHORT).show();
		
			} else
			{
				// Permission Denied
				if (!ActivityCompat.shouldShowRequestPermissionRationale(act,Manifest.permission.CALL_PHONE)) {
	                Toast.makeText(context, "呼叫权限已被禁止,请在设置-授权管理-应用权限管理中修改", Toast.LENGTH_SHORT).show();
	            }
		
			}
	
		}
		if (requestCode == 9)
		{
			if (grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
			{
				callBack.sendMsg1();
				//Toast.makeText(act, "授权成功", Toast.LENGTH_SHORT).show();
		
			} else
			{
				// Permission Denied
				if (!ActivityCompat.shouldShowRequestPermissionRationale(act,Manifest.permission.SEND_SMS)) {
	                Toast.makeText(context, "短信权限已被禁止,请在设置-授权管理-应用权限管理中修改", Toast.LENGTH_SHORT).show();
	            }
		
			}
	
		}
		*/
		if (requestCode == 10)
		{
			if (grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
			{
				callBack.getFujian1();
				//Toast.makeText(act, "授权成功", Toast.LENGTH_SHORT).show();

			} else
			{
				// Permission Denied
				if (!ActivityCompat.shouldShowRequestPermissionRationale(act,Manifest.permission.READ_EXTERNAL_STORAGE)) {
					Toast.makeText(context, "读写存储权限已被禁止,请在设置-授权管理-应用权限管理中修改", Toast.LENGTH_SHORT).show();
				}

			}

		}
	}
	public interface CallBackInterface {

		void getLocation1(int rqcode);
		void getPictureByCamera1(int rqcode);
	    void getPictureFromLocation1();
	    void sendCall1();
	    void sendMsg1();
	    void getFujian1();
	}    
	public static void downloadUrl(String url,File file,Context context)
	{
		if(file==null)
		{
			String path=FileUtility.creatSDDir("download");
			String fileName=FileUtility.getUrlRealName(url);
			String filePath=path+fileName;
			file = new File(filePath);  
		}
		DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);  
	   	 Uri uri = Uri.parse(url);  
	   	 Request request = new Request(uri); 
	   	 if(file.getAbsolutePath().startsWith("/storage/sdcard") || file.getAbsolutePath().startsWith("/storage/emulated"))
	   		 request.setDestinationUri(Uri.fromFile(file));
	   	 //request.setDestinationInExternalFilesDir(context, "", file.getAbsolutePath());
	   	 request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE|DownloadManager.Request.NETWORK_WIFI);
	   	 //request.setDestinationInExternalFilesDir(WebSiteActivity.this, null, "PacketCampus");
	   	 downloadManager.enqueue(request);
	   	 AppUtility.showToastMsg(context, "已开始后台下载..");
	}
	public static boolean isServiceRunning(Context context, String className) {
        boolean isRunning = false;
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> serviceInfos = activityManager.getRunningServices(50);

        if(null == serviceInfos || serviceInfos.size() < 1) {
            return false;
        }

        for(int i = 0; i < serviceInfos.size(); i++) {
            if(serviceInfos.get(i).service.getClassName().contains(className)) {
                isRunning = true;
                break;
            }
        }
        return isRunning;
    }

	public static void downloadAndOpenFile(String mUrl,View widget)
	{
		String path=FileUtility.creatSDDir("download");
		String fileName=FileUtility.getUrlRealName(mUrl);
		String filePath=path+fileName;
		//FileUtility.deleteFile(filePath);
		File file = new File(filePath);  
		Intent intent;
		Context context = widget.getContext();
        if(file.exists() && file.isFile())
        {
        	intent=IntentUtility.openUrl(context,filePath);
        	IntentUtility.openIntent(widget.getContext(), intent,true);
        }
        else
        {
        	intent=IntentUtility.openUrl(context,mUrl);
        	if(intent==null)
        	{
	    		Uri uri = Uri.parse(mUrl);

		        intent = new Intent(Intent.ACTION_VIEW, uri);
		        intent.putExtra(Browser.EXTRA_APPLICATION_ID, context.getPackageName());
		        context.startActivity(intent);
	    	}
	    	else
	    	{
	    		downloadUrl(mUrl, file, widget.getContext());
	    	}
        }
	}
	//是否浮点型
	public static boolean isDecimal(String str) {
		if(str==null || "".equals(str))
			return false;
		java.util.regex.Pattern pattern = Pattern.compile("[0-9]*(\\.?)[0-9]*");
		return pattern.matcher(str).matches();
	}
	//是否整形
	public static boolean isInteger(String str){
		if(str==null )
			return false;
		Pattern pattern = Pattern.compile("[0-9]+");
		return pattern.matcher(str).matches();
	}

	public static void setWindowState(Activity atx)
	{
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			Window window = atx.getWindow();
			window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
					| WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
			window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
					| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION  //该参数指布局能延伸到navigationbar，我们场景中不应加这个参数
					| View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
			window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
			window.setStatusBarColor(Color.TRANSPARENT);
			window.setNavigationBarColor(Color.TRANSPARENT); //设置navigationbar颜色为透明
		}
	}

	public static void statusBarTintColor(Activity activity, int color) {
		// 代表 5.0 及以上
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			activity.getWindow().setStatusBarColor(color);
			return;
		}

		// versionCode > 4.4  and versionCode < 5.0
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
			//透明状态栏
			activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
			ViewGroup androidContainer = (ViewGroup) activity.findViewById(android.R.id.content);
			// 留出高度 setFitsSystemWindows  true代表会调整布局，会把状态栏的高度留出来
			View contentView = androidContainer.getChildAt(0);
			if (contentView != null) {
				contentView.setFitsSystemWindows(true);
			}
			// 在原来的位置上添加一个状态栏
			View statusBarView = createStatusBarView(activity);
			androidContainer.addView(statusBarView, 0);
			statusBarView.setBackgroundColor(color);
		}
	}

	/**
	 * 创建一个需要填充statusBarView
	 */
	private static View createStatusBarView(Activity activity) {
		View statusBarView = new View(activity);
		ViewGroup.LayoutParams statusBarParams = new ViewGroup.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT, getStatusBarHeight(activity));
		statusBarView.setLayoutParams(statusBarParams);
		return statusBarView;
	}

	/**
	 * 获取状态栏的高度
	 */
	public static int getStatusBarHeight(Context context) {
		int result = 0;
		int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
		if (resourceId > 0) {
			result = context.getResources().getDimensionPixelSize(resourceId);
		}
		return result;
	}
	public static View getRootView(Activity context)
	{
		return ((ViewGroup)context.findViewById(android.R.id.content)).getChildAt(0);
	}
    public static void setRootViewPadding(View view)
    {
        int statusBarHeight=getStatusBarHeight(view.getContext());
        view.setPadding(0, statusBarHeight, 0, 0);
		List<View> viewList=getAllChildViews(view);
		for(View subview : viewList)
		{

			if(subview instanceof ListView || subview instanceof XListView)
			{
				ListView mainview=(ListView)subview;
				if(subview.getLayoutParams().height== -1)
				{
					int navBarHeight=getDaoHangHeight(subview.getContext());
					if(navBarHeight>0) {
						mainview.setClipToPadding(false);
						mainview.setPadding(0, 0, 0, navBarHeight);
					}
                    break;
				}
			}
			if(subview instanceof ScrollView){
				ScrollView mainview=(ScrollView)subview;
				if(subview.getLayoutParams().height== -1)
				{
					int navBarHeight=getDaoHangHeight(subview.getContext());
					if(navBarHeight>0) {
						mainview.setClipToPadding(false);
						mainview.setPadding(0, 0, 0, navBarHeight);
					}
                    break;
				}
			}
		}

    }
	public static int getDaoHangHeight(Context context) {
		int result = 0;
		int resourceId=0;
		int rid = context.getResources().getIdentifier("config_showNavigationBar", "bool", "android");
		if (rid!=0){
			resourceId = context.getResources().getIdentifier("navigation_bar_height", "dimen", "android");
			return context.getResources().getDimensionPixelSize(resourceId);
		}else
			return 0;
	}
	public static List<View> getAllChildViews(View view) {
		List<View> allchildren = new ArrayList<View>();
		if (view instanceof ViewGroup) {
			ViewGroup vp = (ViewGroup) view;
			for (int i = 0; i < vp.getChildCount(); i++) {
				View viewchild = vp.getChildAt(i);
				allchildren.add(viewchild);
				//再次 调用本身（递归）
				allchildren.addAll(getAllChildViews(viewchild));
			}
		}
		return allchildren;
	}
	public static int getAndroiodScreenProperty(Context context) {
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		DisplayMetrics dm = new DisplayMetrics();
		wm.getDefaultDisplay().getMetrics(dm);
		int width = dm.widthPixels;         // 屏幕宽度（像素）
		int height = dm.heightPixels;       // 屏幕高度（像素）
		float density = dm.density;         // 屏幕密度（0.75 / 1.0 / 1.5）
		int densityDpi = dm.densityDpi;     // 屏幕密度dpi（120 / 160 / 240）
		// 屏幕宽度算法:屏幕宽度（像素）/屏幕密度
		int screenWidth = (int) (width / density);  // 屏幕宽度(dp)
		return screenWidth;
	}
	public static synchronized String getPictureByCamera(Activity theAct,int REQUEST_CODE_TAKE_CAMERA) {
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);// 调用android自带的照相机
		String sdStatus = Environment.getExternalStorageState();
		if (!sdStatus.equals(Environment.MEDIA_MOUNTED)) { // 检测sd是否可用
			AppUtility.showToastMsg(context, "没有安装SD卡，无法使用相机功能");
			return "";
		}
		String picturePath = FileUtility.getRandomSDFileName("jpg");

		File mCurrentPhotoFile = new File(picturePath);

		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
			intent.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(theAct, BuildConfig.APPLICATION_ID + ".fileProvider", mCurrentPhotoFile)); //Uri.fromFile(tempFile)
		else {
			Uri uri = Uri.fromFile(mCurrentPhotoFile);
			intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
		}
		theAct.startActivityForResult(intent, REQUEST_CODE_TAKE_CAMERA);
		return picturePath;
	}
	public static void fileUploadWay(File file,String imagetype, Handler handler) {
		if(!file.exists()) return;
		if(AppUtility.formetFileSize(file.length()) > 5242880*2){
			AppUtility.showToastMsg(context, "对不起，您上传的文件太大了，请选择小于10M的文件！");
		}else{

			ImageUtility.rotatingImageIfNeed(file.getAbsolutePath());
			DownloadSubject downloadSubject = new DownloadSubject();
			String filebase64Str = FileUtility.fileupload(file);
			downloadSubject.setFilecontent(filebase64Str);
			String filename = file.getName();
			downloadSubject.setFileName(filename);
			downloadSubject.setLocalfile(file.getAbsolutePath());
			downloadSubject.setFilesize(file.length());
			uploadFile(downloadSubject,imagetype,handler);

		}
	}

	public static void uploadFile(DownloadSubject downloadSubject, String imagetype,final Handler mHandler) {
		final CampusParameters params = new CampusParameters();
		String checkCode = PrefUtility.get(Constants.PREF_CHECK_CODE, "");// 获取用户校验码
		params.add("用户较验码", checkCode);
		params.add("课程名称", imagetype);
		params.add("老师上课记录编号", "0");
		params.add("图片类别", "问卷调查");
		params.add("文件名称", downloadSubject.getFileName());
		params.add("文件内容", downloadSubject.getFilecontent());
		CampusAPI.uploadFiles(params, new RequestListener() {

			@Override
			public void onComplete(String response) {
				Log.d(TAG, "------------------response" + response);

				Message msg = new Message();
				msg.what = 3;
				msg.obj = response;
				Bundle data=new Bundle();
				data.putString("oldFileName", params.getValue("文件名称"));
				msg.setData(data);
				mHandler.sendMessage(msg);

			}

			@Override
			public void onIOException(IOException e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onError(CampusException e) {
				Log.d(TAG, "图片上传失败");
				Message msg = new Message();
				msg.what = -1;
				msg.obj = e.getMessage();
				mHandler.sendMessage(msg);
			}
		});
	}
	public static void goIntentSetting() {
		Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
		Uri uri = Uri.fromParts("package", context.getPackageName(), null);
		intent.setData(uri);
		try {
			context.startActivity(intent);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static int chineseLength(String value) {
		int valueLength = 0;
		String chinese = "[\u0391-\uFFE5]";
		/* 获取字段值的长度，如果含中文字符，则每个中文字符长度为2，否则为1 */
		for (int i = 0; i < value.length(); i++) {
			/* 获取一个字符 */
			String temp = value.substring(i, i + 1);
			/* 判断是否为中文字符 */
			if (temp.matches(chinese)) {
				/* 中文字符长度为2 */
				valueLength += 2;
			} else {
				/* 其他字符长度为1 */
				valueLength += 1;
			}
		}
		return valueLength;
	}
	public static String cutStringToLength(String s,int length)
	{
		while(chineseLength(s)>length)
			s=s.substring(0,s.length()-1);
		return s;
	}
}
