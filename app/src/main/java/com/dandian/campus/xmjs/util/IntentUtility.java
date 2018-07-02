package com.dandian.campus.xmjs.util;

import java.io.File;
import java.util.List;

import com.dandian.campus.xmjs.activity.CourseClassActivity;
import com.dandian.campus.xmjs.activity.TabHostActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.FileProvider;

@SuppressLint("DefaultLocale")
public class IntentUtility {

	
	public static Intent openUrl(Context context,String url) {
		/* 取得扩展名 */
		String end = url.substring(url.lastIndexOf(".") + 1,url.length()).toLowerCase();
		if (end==null || end.length()==0)
			return null;
		String fileType=null;
		/* 依扩展名的类型决定MimeType */
		if (end.equals("m4a") || end.equals("mp3") || end.equals("mid")
				|| end.equals("xmf") || end.equals("ogg") || end.equals("wav")) {
			fileType="audio/*";
		} else if (end.equals("3gp") || end.equals("mp4") || end.equals("mov") || end.equals("avi") 
				|| end.equals("flv") || end.equals("f4v") || end.equals("wmv") || end.equals("rm") 
				|| end.equals("asf") || end.equals("mpg") || end.equals("mpeg") || end.equals("rmvb") 
				|| end.equals("ogm")) {
			fileType="video/*";

		} else if (end.equals("jpg") || end.equals("gif") || end.equals("png")
				|| end.equals("jpeg") || end.equals("bmp")) {
			fileType="image/*";
		} else if (end.equals("apk")) {
			fileType="application/vnd.android.package-archive";
		} else if (end.equals("ppt")) {
			fileType="application/vnd.ms-powerpoint";
		} else if (end.equals("xls") || end.equals("xlsx")) {
			fileType="application/vnd.ms-excel";
		} else if (end.equals("doc") || (end.equals("docx"))) {
			fileType="application/msword";
		} else if (end.equals("pdf")) {
			fileType="application/pdf";
		} else if (end.equals("chm")) {
			fileType="application/x-chm";
		} else if (end.equals("txt")) {
			fileType="text/plain";
		} else if(end.equals("rar")){
			fileType="application/x-rar-compressed";
		}else if(end.equals("zip")){
			fileType="application/zip";
		}else if(end.equals("swf")){
			fileType="application/x-shockwave-flash";
		}
		if(fileType!=null)
			return getFileIntentByType(context,url,fileType);
		else
			return null;
	}

	// Android获取一个用于打开APK文件的intent
	public static Intent getAllIntent(Context context,String param) {

		Intent intent = new Intent();
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setAction(android.content.Intent.ACTION_VIEW);
		Uri uri;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
			uri = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".fileProvider", new File(param));
		else
			uri = Uri.fromFile(new File(param));
		intent.setDataAndType(uri, "*/*");
		return intent;
	}



	// Android获取一个用于打开Html文件的intent
	public static Intent getHtmlFileIntent(String param) {

		Uri uri = Uri.parse(param).buildUpon()
				.encodedAuthority("com.android.htmlfileprovider")
				.scheme("content").encodedPath(param).build();
		Intent intent = new Intent("android.intent.action.VIEW");
		intent.setDataAndType(uri, "text/html");
		return intent;
	}

	// Android获取一个用于打开图片文件的intent
	public static Intent getImageFileIntent(String param) {

		Intent intent = new Intent("android.intent.action.VIEW");
		intent.addCategory("android.intent.category.DEFAULT");
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		Uri uri;
		if(param.startsWith("http:"))
			uri=Uri.parse(param);
		else
			uri = Uri.fromFile(new File(param));
		intent.setDataAndType(uri, "image/*");
		return intent;
	}


	public static Intent getFileIntentByType(Context context,String param,String fileType) {

		Intent intent = new Intent("android.intent.action.VIEW");
		//intent.addCategory("android.intent.category.DEFAULT");
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		Uri uri;
		if(param.startsWith("http:"))
			uri=Uri.parse(param);
		else {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
				uri = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".fileProvider", new File(param));
				intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
			}
			else
				uri = Uri.fromFile(new File(param));
		}
		intent.setDataAndType(uri, fileType);
		return intent;
	}



	public static boolean openIntent(Context context,Intent intent,boolean iftip)
	{
		if(hasApplication(context,intent))
		{
			context.startActivity(intent);
			return true;
		}
		else
		{
			if(iftip)
				AppUtility.showToastMsg(context, "没有可以打开此文件的程序");
			return false;
		}
	}
	private static boolean hasApplication(Context context,Intent intent){    
        android.content.pm.PackageManager packageManager = context.getPackageManager();    
        //查询是否有该Intent的Activity    
        List<android.content.pm.ResolveInfo> activities = packageManager.queryIntentActivities(intent, 0);    
        //activities里面不为空就有，否则就没有    
        return activities.size() > 0;
	}
	public static void checkAppByPackName(final Context context,final Intent intent,String[] packName,String tip,final String url){    
	
        android.content.pm.PackageManager packageManager = context.getPackageManager();    
            
        ApplicationInfo appinfo = null;
        for(int i=0;i<packName.length;i++)
        {
			try {
				appinfo = packageManager.getApplicationInfo(packName[i], 0);
			} catch (NameNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(appinfo!=null) break;
        }
        if(appinfo==null)
        {
        	AlertDialog.Builder builder = new AlertDialog.Builder(context);
        	builder.setMessage(tip)
        	       .setCancelable(false)
        	       .setPositiveButton("安装", new DialogInterface.OnClickListener() {
        	           public void onClick(DialogInterface dialog, int id) {
        	        	   
        	        	   AppUtility.downloadUrl(url, null, context);
        	        	   dialog.dismiss();
        	           }
        	       })
        	       .setNegativeButton("取消", new DialogInterface.OnClickListener() {
        	           public void onClick(DialogInterface dialog, int id) {
        	        	   openIntent(context,intent,false);
        	                dialog.cancel();
        	           }
        	       });
        	AlertDialog alert = builder.create();
        	alert.show();
        }
        else
        	openIntent(context,intent,true);
    
	}
}
