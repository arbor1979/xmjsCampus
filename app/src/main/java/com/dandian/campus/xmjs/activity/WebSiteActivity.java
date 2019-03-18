package com.dandian.campus.xmjs.activity;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.http.util.EncodingUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.DownloadManager;
import android.app.DownloadManager.Request;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.webkit.ConsoleMessage;
import android.webkit.CookieSyncManager;
import android.webkit.DownloadListener;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.dandian.campus.xmjs.CampusApplication;
import com.dandian.campus.xmjs.entity.ImageItem;
import com.dandian.campus.xmjs.entity.User;
import com.dandian.campus.xmjs.service.Alarmreceiver;
import com.dandian.campus.xmjs.util.Base64;
import com.dandian.campus.xmjs.util.DateHelper;
import com.dandian.campus.xmjs.activity.TabHostActivity;
import com.dandian.campus.xmjs.activity.WebSiteActivity;
import com.dandian.campus.xmjs.util.FileUtility;
import com.dandian.campus.xmjs.R;
import com.dandian.campus.xmjs.base.Constants;
import com.dandian.campus.xmjs.base.ExitApplication;
import com.dandian.campus.xmjs.util.AppUtility;
import com.dandian.campus.xmjs.util.IntentUtility;
import com.dandian.campus.xmjs.util.PrefUtility;
import com.dandian.campus.xmjs.util.AppUtility.CallBackInterface;


/**
 * 信息
 * 
 * @Title NoticeAcitivity.java
 * @Description: TODO
 * 
 * @author Zecker
 * @date 2013-10-23 下午4:47:47
 * @version V1.0
 * 
 */

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
public class WebSiteActivity extends Activity implements Alarmreceiver.BRInteraction{
	private static final String TAG = "WebSiteActivity";
	public static Date loginDate;
	private WebView mWebView; 
	private WebChromeClient chromeClient = null;
	private View myView = null;
	private WebChromeClient.CustomViewCallback myCallBack = null;
	private LinearLayout frameLayout = null,webNavBar;
	private FrameLayout loading=null;
	private RelativeLayout layoutHead;
	private Button btnLeft;
	private ImageButton btn_close;
	private String needLoadHtml="";
	private String moodleText="";
	private String downloadUrl;
	private File downloadFile;
	private static boolean bOfficeInstall=false,bZipInstall=false;
	private ValueCallback<Uri> uploadMessage;
	private ValueCallback<Uri[]> uploadMessageAboveL;
	private final static int FILE_CHOOSER_RESULT_CODE = 10000;
	private final static int REQUEST_CODE_TAKE_CAMERA = 1;// //设置拍照操作的标志
	private final static int SCANNIN_GREQUEST_CODE = 2;
	private String cameraPicPath,cameraPicType;
	@SuppressLint({ "SetJavaScriptEnabled", "CutPasteId" })

	@Override
	public void callbackGPSXY(Location loc) {
		double lat=0;
		double lon=0;
		String datestr="";
		double accu=0;
		if(loc!=null)
		{
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			datestr=format.format(loc.getTime());
			lat=loc.getLatitude();
			lon=loc.getLongitude();
			accu=loc.getAccuracy();
		}
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			mWebView.evaluateJavascript("javascript:callbackGPSXY("+lat+","+lon+",'"+datestr+"',"+accu+")",null);
		}
		else
			mWebView.loadUrl("javascript:callbackGPSXY("+lat+","+lon+",'"+datestr+"',"+accu+")");
		Log.d(TAG,"lat="+lat+",lon="+lon);
	}
	public void callbackRealAddress(String realAddress)
	{
		realAddress=realAddress.replace("\n","\\n");
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			mWebView.evaluateJavascript("javascript:callbackRealAddress('"+realAddress+"')",null);
		}
		else
			mWebView.loadUrl("javascript:callbackRealAddress('\"+realAddress+\"')");
		Alarmreceiver.brInteraction=null;
		Log.d(TAG,realAddress);
	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.web_site);

		ExitApplication.getInstance().addActivity(this);
		frameLayout = (LinearLayout)findViewById(R.id.mainLayout);
		btnLeft = (Button) findViewById(R.id.btn_left);
		btnLeft.setVisibility(View.VISIBLE);
		btnLeft.setCompoundDrawablesWithIntrinsicBounds(
				R.drawable.bg_btn_left_nor, 0, 0, 0);
		loading=(FrameLayout)findViewById(R.id.loading);
		mWebView = (WebView) findViewById(R.id.website);
		layoutHead = (RelativeLayout) findViewById(R.id.headerlayout);
		//webNavBar=(LinearLayout)findViewById(R.id.webNavBar);

		mWebView.getSettings().setAllowFileAccess(true);
		mWebView.getSettings().setJavaScriptEnabled(true);   
		mWebView.getSettings().setDomStorageEnabled(true);
		mWebView.getSettings().setBuiltInZoomControls(false);
		mWebView.getSettings().setSupportZoom(false);  
		mWebView.getSettings().setDisplayZoomControls(false);  
		//mWebView.getSettings().setMediaPlaybackRequiresUserGesture(false);
		mWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
		mWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);   
		//mWebView.getSettings().setPluginState(PluginState.ON);
		mWebView.getSettings().setDefaultTextEncodingName("GBK");
		btn_close=(ImageButton) findViewById(R.id.btn_close);
		/*
		mWebView.getSettings().setDatabaseEnabled(true);
		mWebView.getSettings().setAppCacheEnabled(true);
		mWebView.getSettings().setAppCachePath(FileUtility.creatSDDir("webCache"));
		*/
		String url = getIntent().getStringExtra("url");
		moodleText = getIntent().getStringExtra("htmlText");
		String title = getIntent().getStringExtra("title");
		Log.d(TAG, "url：" + url);
		Log.d(TAG, "title：" + title);
		TextView tv_title = (TextView) findViewById(R.id.tv_title);
		if(title.length()>10)
		{
			tv_title.setTextSize(18);
			if(title.length()>12)
			title=title.substring(0, 12)+"..";
		}
		tv_title.setText(title);
		mWebView.setWebViewClient(new MyWebClient());
		//mWebView.setWebChromeClient(new WebChromeClient());
		mWebView.setVisibility(View.GONE);
		mWebView.setDownloadListener(new MyWebViewDownLoadListener());
		chromeClient = new MyChromeClient();
		mWebView.addJavascriptInterface(chromeClient, "VideoComplete");
		mWebView.setWebChromeClient(chromeClient);
		
		CookieSyncManager.createInstance(this); 
		
		if(url!=null && url.length()>0)
			mWebView.loadUrl(url);
		else if(moodleText!=null && moodleText.length()>0)
		{
			Date now=new Date();
			String loginUrl =  getIntent().getStringExtra("loginUrl");
			if(loginUrl!=null && (loginDate==null || DateHelper.getMinutesDiff(now,loginDate)>20))
			{
				String username=PrefUtility.get(Constants.PREF_LOGIN_NAME, "");
				username=username.split("@")[0];
				String password=PrefUtility.get(Constants.PREF_LOGIN_PASS, "");
				String postDate = "username="+username+"&password="+password;
				mWebView.postUrl(loginUrl, EncodingUtils.getBytes(postDate, "BASE64"));
				needLoadHtml=moodleText;
			}
			else
			{
				mWebView.getSettings().setDefaultTextEncodingName("UTF-8");//设置默认为utf-8
				mWebView.loadData(moodleText, "text/html; charset=UTF-8", null);
			}
			//webNavBar.setVisibility(View.VISIBLE);
		}
		LinearLayout lyLeft = (LinearLayout) findViewById(R.id.layout_btn_left);
		lyLeft.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(mWebView.canGoBack())
					mWebView.goBack();
				else
					finish();
			}
		});
		btn_close.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}

	public class MyChromeClient extends WebChromeClient implements OnCompletionListener {
		
		@Override
		public void onShowCustomView(View view, CustomViewCallback callback) {
			if(myView != null){
				callback.onCustomViewHidden();
				return;
			}
			WebSiteActivity.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
			//frameLayout.removeView(mWebView);
			mWebView.setVisibility(View.GONE);
			layoutHead.setVisibility(View.GONE);
			webNavBar.setVisibility(View.GONE);
			frameLayout.setBackgroundColor(Color.BLACK);
			frameLayout.addView(view);
			myView = view;
			myCallBack = callback;
			chromeClient=this;
			
			FrameLayout frameLayout = (FrameLayout) view; 
            View focusedChild = frameLayout.getFocusedChild();
            
            if (focusedChild instanceof VideoView) 
            { 
                // VideoView (typically API level <11) 
                VideoView videoView = (VideoView) focusedChild; 
                // Handle all the required events 
                videoView.setOnCompletionListener(this); 
                
            }
            else // Usually android.webkit.HTML5VideoFullScreen$VideoSurfaceView, sometimes android.webkit.HTML5VideoFullScreen$VideoTextureView 
            { 
                // HTML5VideoFullScreen (typically API level 11+) 
                // Handle HTML5 video ended event 
                    String js = "javascript:"; 
                    js += "_ytrp_html5_video = document.getElementsByTagName('video')[0];"; 
                    js += "if (_ytrp_html5_video !== undefined) {"; 
                    { 
                        js += "function _ytrp_html5_video_ended() {"; 
                        { 
                            js += "_ytrp_html5_video.removeEventListener('ended', _ytrp_html5_video_ended);"; 
                            js += "VideoComplete.playVideoEnd();"; // Must match Javascript interface name and method of VideoEnableWebView 
                        } 
                        js += "}"; 
                        js += "_ytrp_html5_video.addEventListener('ended', _ytrp_html5_video_ended);"; 
                    } 
                    js += "if(_ytrp_html5_video.paused) _ytrp_html5_video.play();}"; 
                    mWebView.loadUrl(js); 
            	
            } 
		}
		public void playVideoEnd() 
		{
			WebSiteActivity.this.frameLayout.post(new Runnable() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
						onHideCustomView();
					}
				});
			}
		@Override 
		public void onCompletion(MediaPlayer mp) // Video finished playing, only called in the case of VideoView (typically API level <11) 
		{ 
		        onHideCustomView(); 
		} 
		@Override
		public void onHideCustomView() {
			if(myView == null){
				return;
			}
			WebSiteActivity.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
			frameLayout.removeView(myView);
			layoutHead.setVisibility(View.VISIBLE);
			frameLayout.setBackgroundColor(Color.WHITE);
			myView = null;
			mWebView.setVisibility(View.VISIBLE);
			if(moodleText!=null && moodleText.length()>0)
				webNavBar.setVisibility(View.VISIBLE);
			myCallBack.onCustomViewHidden();
		}
		
		@Override
		public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
			// TODO Auto-generated method stub
			Log.d("ZR", consoleMessage.message()+" at "+consoleMessage.sourceId()+":"+consoleMessage.lineNumber());
			return super.onConsoleMessage(consoleMessage);
		}
		// For Android < 3.0
		public void openFileChooser(ValueCallback<Uri> valueCallback) {
			uploadMessage = valueCallback;
			openImageChooserActivity();
		}

		// For Android  >= 3.0
		public void openFileChooser(ValueCallback valueCallback, String acceptType) {
			uploadMessage = valueCallback;
			openImageChooserActivity();
		}

		//For Android  >= 4.1
		public void openFileChooser(ValueCallback<Uri> valueCallback, String acceptType, String capture) {
			uploadMessage = valueCallback;
			openImageChooserActivity();
		}

		// For Android >= 5.0
		@Override
		public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams) {
			uploadMessageAboveL = filePathCallback;
			openImageChooserActivity();
			return true;
		}
		@JavascriptInterface
		public void GetScanCode() {
			if (Build.VERSION.SDK_INT >= 23) {
				if (AppUtility.checkPermission(WebSiteActivity.this, 12, Manifest.permission.CAMERA))
					openScanCode();
			} else
				openScanCode();
		}
		@JavascriptInterface
		public void GetCamera(String type) {
			cameraPicType=type;
			if (Build.VERSION.SDK_INT >= 23) {
				if (AppUtility.checkPermission(WebSiteActivity.this, 6, Manifest.permission.CAMERA))
					cameraPicPath=AppUtility.getPictureByCamera(WebSiteActivity.this,REQUEST_CODE_TAKE_CAMERA);
			} else
				cameraPicPath=AppUtility.getPictureByCamera(WebSiteActivity.this,REQUEST_CODE_TAKE_CAMERA);
		}
		@JavascriptInterface
		public void GetGPSNew(int mode) {
			int code=5;
			if(mode==1)
				code=11;
			if (Build.VERSION.SDK_INT >= 23) {
				if (AppUtility.checkPermission(WebSiteActivity.this, code, Manifest.permission.ACCESS_FINE_LOCATION))
					getLocation(mode);
			} else
				getLocation(mode);
		}
		@JavascriptInterface
		public String GetGPS() {

			if (Build.VERSION.SDK_INT >= 23) {
				if (AppUtility.checkPermission(WebSiteActivity.this, 5, Manifest.permission.ACCESS_FINE_LOCATION))
					getLocation(0);
			} else
				getLocation(0);
			User user = ((CampusApplication) getApplicationContext()).getLoginUserObj();
			if(user.getLatestAddress()==null || user.getLatestAddress().length()==0)
				return "";
			else
				return user.getLatestGps()+"\n"+user.getLatestAddress();
		}

	}
	private void getLocation(int mode)
	{
		Intent intent = new Intent(WebSiteActivity.this, Alarmreceiver.class);
		if(mode==1)
			intent.setAction("reportGPSLocation");
		else
			intent.setAction("reportLocation");
		Alarmreceiver.brInteraction=this;
		sendBroadcast(intent);
	}
	private void openImageChooserActivity() {
		Intent i = new Intent(Intent.ACTION_GET_CONTENT);
		i.addCategory(Intent.CATEGORY_OPENABLE);
		i.setType("*/*");
		startActivityForResult(Intent.createChooser(i, "文件选择"), FILE_CHOOSER_RESULT_CODE);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == FILE_CHOOSER_RESULT_CODE) {
			if (null == uploadMessage && null == uploadMessageAboveL) return;
			Uri result = data == null || resultCode != RESULT_OK ? null : data.getData();
			if (uploadMessageAboveL != null) {
				onActivityResultAboveL(requestCode, resultCode, data);
			} else if (uploadMessage != null) {
				uploadMessage.onReceiveValue(result);
				uploadMessage = null;
			}
		}
		else if (requestCode ==  REQUEST_CODE_TAKE_CAMERA) {// 拍照返回
			if (resultCode == RESULT_OK && cameraPicPath!=null && cameraPicPath.length()>0) {
				AppUtility.fileUploadWay(new File(cameraPicPath),cameraPicType,this.mHandler);
			}
		}
		else if (requestCode ==  SCANNIN_GREQUEST_CODE){
			if(resultCode == RESULT_OK){
				Bundle bundle = data.getExtras();
				String result = bundle.getString("result");
				/*
				try {
					result = new String(Base64.decode(result.getBytes("GBK")));
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				*/
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
					mWebView.evaluateJavascript("javascript:callbackScanCode('"+result+"')",null);
				}
				else
					mWebView.loadUrl("javascript:callbackScanCode('"+result+"')");
				Log.d(TAG,result);
			}
		}
	}
	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	private void onActivityResultAboveL(int requestCode, int resultCode, Intent intent) {
		if (requestCode != FILE_CHOOSER_RESULT_CODE || uploadMessageAboveL == null)
			return;
		Uri[] results = null;
		if (resultCode == Activity.RESULT_OK) {
			if (intent != null) {
				String dataString = intent.getDataString();
				ClipData clipData = intent.getClipData();
				if (clipData != null) {
					results = new Uri[clipData.getItemCount()];
					for (int i = 0; i < clipData.getItemCount(); i++) {
						ClipData.Item item = clipData.getItemAt(i);
						results[i] = item.getUri();
					}
				}
				if (dataString != null)
					results = new Uri[]{Uri.parse(dataString)};
			}
		}
		uploadMessageAboveL.onReceiveValue(results);
		uploadMessageAboveL = null;
	}

	private class MyWebClient extends WebViewClient
	{
		 @Override  
	    public void doUpdateVisitedHistory(WebView view, String url, boolean isReload) {  

	    } 
		public void onPageFinished(WebView view, String url) {
			super.onPageFinished(view, url);
			if(moodleText!=null && moodleText.length()>0)
			{
				view.loadUrl("javascript:if(document.getElementById('region-main')) "
						+ "{document.body.innerHTML=document.getElementById('region-main').innerHTML.replace(new RegExp('pluginfile.php','gm'),'pluginfile_dandian.php?');"
						+ "var tags=document.getElementsByTagName('a');"
						+ "for(var i=0;i<tags.length;i++)"
						+ "{tags[i].innerHTML=decodeURIComponent(tags[i].innerHTML);}"
						+ "setTimeout('',100);}");
			}
			mWebView.setVisibility(View.VISIBLE);
			loading.setVisibility(View.GONE);
			
			if(needLoadHtml.length()>0)
			{
				needLoadHtml=needLoadHtml.replaceAll("pluginfile.php","pluginfile_dandian.php?");
				needLoadHtml="<head><meta name='viewport' content='width=device-width, initial-scale=1.0'></head>"+needLoadHtml;
				/*
				needLoadHtml=needLoadHtml.replaceAll("pluginfile.php","pluginfile_dandian.php?");
				needLoadHtml+="<script language=javascript>window.onload=function(){"
				+ "var tables=document.getElementsByTagName('table');"
						+ "for(var i=0;i<tables.length;i++)"
						+ "{tables[i].style.width='';}}</script>";
				*/
				mWebView.getSettings().setDefaultTextEncodingName("UTF-8"); //设置默认为utf-8
				mWebView.loadData(needLoadHtml, "text/html; charset=UTF-8", null);
				needLoadHtml="";
				loginDate=new Date();
			}
			if(mWebView.canGoBack()) {
				btn_close.setVisibility(View.VISIBLE);
			}
			else {
				btn_close.setVisibility(View.GONE);
			}
			/*
			int navBarHeight=AppUtility.getDaoHangHeight(mWebView.getContext());
			if(navBarHeight>0) {
				mWebView.loadUrl("javascript:document.body.style.paddingBottom='"+navBarHeight+"px'; void 0");
			}
			*/
			
	    }
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
	        super.onPageStarted(view, url, favicon);
	        mWebView.setVisibility(View.GONE);
			loading.setVisibility(View.VISIBLE);
	    }  
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			Uri uri = Uri.parse(url);
		    if (url.startsWith("tel:")) {
		        startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse(url)));
		        return true;
		    } else if (url.startsWith("mailto:")) {
		        url = url.replaceFirst("mailto:", "");
		        url = url.trim();
		        Intent i = new Intent(Intent.ACTION_SEND);
		        i.setType("plain/text").putExtra(Intent.EXTRA_EMAIL, new String[]{url});
		        startActivity(i);
		        return true;
		    } else if (url.startsWith("geo:")) {
		        Intent searchAddress = new Intent(Intent.ACTION_VIEW, Uri.parse(url)); 
		        startActivity(searchAddress); 
		        return true;
		    }
			else if ( uri.getScheme().equals("js")) {
				if (uri.getAuthority().equals("PersonInfo")) {
					Intent intent = new Intent(WebSiteActivity.this,
							ShowPersonInfo.class);
					intent.putExtra("studentId", uri.getQueryParameter("weiyima"));
					startActivity(intent);
				}
				else if(uri.getAuthority().equals("OpenTemplateMain"))
				{
					Intent intent = new Intent(WebSiteActivity.this, SchoolActivity.class);
					intent.putExtra("title", uri.getQueryParameter("title"));
					intent.putExtra("interfaceName",uri.getQueryParameter("interfaceName"));
					intent.putExtra("templateName",uri.getQueryParameter("templateName"));
					startActivity(intent);
				}
				else if(uri.getAuthority().equals("OpenTemplateDetail"))
				{
					Intent intent = new Intent(WebSiteActivity.this, SchoolDetailActivity.class);
					intent.putExtra("title", uri.getQueryParameter("title"));
					intent.putExtra("interfaceName",uri.getQueryParameter("interfaceName"));
					intent.putExtra("templateName",uri.getQueryParameter("templateName"));
					startActivity(intent);
				}
				return true;
			}
		    /*
		    else if(url.toLowerCase(Locale.getDefault()).endsWith(".swf"))
		    {
		    	
		    	if(!checkFlashplayer())
		    	{
		    		AppUtility.showToastMsg(WebSiteActivity.this, "请先安装flash插件");
		    		Intent installIntent = new Intent(  
                            "android.intent.action.VIEW");  
                    installIntent.setData(Uri  
                            .parse("market://details?id=com.adobe.flashplayer"));  
                    startActivity(installIntent);  
                    return true;
		    	}
		    	
		    	AppUtility.showToastMsg(WebSiteActivity.this,"由于安卓对swf格式文件支持不佳，建议在电脑上打开swf文件",1);
		    	//view.loadUrl(url);
		    	return true;

		    }
		    */
		    else
		    {
		    	//if(moodleText!=null && moodleText.length()>0)
		    	//	url=url.replace("pluginfile.php", "pluginfile_dandian.php?");
		    	url=url.replace("pluginfile.php", "pluginfile_dandian.php?");
		    	String[] tempArray=url.split("\\?");
		    	if(tempArray.length>2)
		    		url=tempArray[0]+"?"+tempArray[1];
				String path=FileUtility.creatSDDir("download");
				String fileName=FileUtility.getUrlRealName(url);
				String filePath=path+fileName;
				//FileUtility.deleteFile(filePath);
				File file = new File(filePath);  
				Intent intent;
		        if(file.exists() && file.isFile())
		        {
		        	intent=IntentUtility.openUrl(WebSiteActivity.this,filePath);
		        	if(intent==null)
						view.loadUrl(url);
		        	else
		        		IntentUtility.openIntent(WebSiteActivity.this, intent,true);
		        }
		        else
		        {
		        	
		        	intent=IntentUtility.openUrl(WebSiteActivity.this,url);
		        	if(intent==null)
		        	{
		        		view.loadUrl(url);
			    	}
			    	else
			    	{
			    		ArrayList<String> tempList =new ArrayList<String>();
			    		tempList.add("audio/*");
			    		tempList.add("video/*");
			    		tempList.add("image/*");
			    		if(tempList.contains(intent.getType()))
			    		{
			    			if(!IntentUtility.openIntent(WebSiteActivity.this, intent,false))
			    			{
			    				downloadFile(url,file);
			    			}
			    		}
			    		else
			    			downloadFile(url,file);
			    		
			    	}
			    	
		        	 
		        }
		        return true;
		    }
		   
		    
		}
		
	}
	private void downloadFile(String url,File file)
	{
		if (Build.VERSION.SDK_INT >= 23) 
		{
			if(AppUtility.checkPermission(WebSiteActivity.this, 7,Manifest.permission.WRITE_EXTERNAL_STORAGE))
				beginDownload(url,file);
			else
			{
				downloadFile=file;
				downloadUrl=url;
			}
		}
		else
			beginDownload(url,file);
		
		 
	}
	private void beginDownload(String url,File file)
	{
		AppUtility.downloadUrl(url, file, WebSiteActivity.this);

	}
	private class MyWebViewDownLoadListener implements DownloadListener {

        @Override
        public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype,
                                    long contentLength) {
        	Intent intent=IntentUtility.openUrl(WebSiteActivity.this,url);
        	if(intent==null)
	    	{
        		Uri uri = Uri.parse(url);
        		intent = new Intent(Intent.ACTION_VIEW, uri);
        		startActivity(intent);
	    	}
        	else
        		IntentUtility.openIntent(WebSiteActivity.this,intent,true);
        }

    }
	@Override
	public void onPause()
	{
		super.onPause();
		mWebView.onPause();
		mWebView.loadUrl("javascript:var v=document.getElementById('video1');if(v) v.pause();");
		CookieSyncManager.getInstance().stopSync();
	}
	@Override
	public void onResume()
	{
		super.onResume();
		mWebView.onResume();
		CookieSyncManager.getInstance().startSync();
	}
	 @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // TODO Auto-generated method stub
        super.onConfigurationChanged(newConfig);
        if(this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE )
        {
            Toast.makeText(getApplicationContext(), "开启全屏", Toast.LENGTH_SHORT).show();
        }else if(this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
        {
            Toast.makeText(getApplicationContext(), "退出全屏", Toast.LENGTH_SHORT).show();
        }
    }
	 @Override 
	public boolean onKeyDown(int keyCode, KeyEvent event) {  
		 if(keyCode == KeyEvent.KEYCODE_BACK)
		 {
			 if(myView!=null)
			 {
				 chromeClient.onHideCustomView();
				 return true;
			 }
			 else
			 {
				 if(mWebView.canGoBack()){
					 mWebView.goBack();
					 return true;
				 }
				 else {
					 finish();
					 return true;
				 }
			 }
		 }
		 else
			 return super.onKeyDown(keyCode, event); 
	 }
	 /*
	 private boolean checkFlashplayer() {
			PackageManager pm = getPackageManager();
			List<PackageInfo> infoList = pm
					.getInstalledPackages(PackageManager.GET_SERVICES);
			for (PackageInfo info : infoList) {
				if ("com.adobe.flashplayer".equals(info.packageName)) {
					return true;
				}
			}
			return false;
	}*/
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
		public void getLocation1(int rqcode)
		{
			if(rqcode==11)
				getLocation(1);
			else
				getLocation(0);
		}

		@Override
		public void getPictureByCamera1(int rqcode) {
			if(rqcode==12)
				openScanCode();
			else
				cameraPicPath=AppUtility.getPictureByCamera(WebSiteActivity.this,REQUEST_CODE_TAKE_CAMERA);
		}

		@Override
		public void getPictureFromLocation1() {
			// TODO Auto-generated method stub
			beginDownload(downloadUrl,downloadFile);
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
	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			String result = "";
			String resultStr = "";
			switch (msg.what) {
				case -1:
					AppUtility.showErrorToast(WebSiteActivity.this,
							msg.obj.toString());
					break;
				case 3:// 图片上传
					result = msg.obj.toString();
					resultStr = "";
					Bundle data = msg.getData();
					String oldFileName = data.getString("oldFileName");
					if (AppUtility.isNotEmpty(result)) {
						try {
							resultStr = new String(Base64.decode(result
									.getBytes("GBK")));
							Log.d(TAG, resultStr);
						} catch (UnsupportedEncodingException e) {
							e.printStackTrace();
						}
					}
					try {
						JSONObject jo = new JSONObject(resultStr);
						if ("OK".equals(jo.optString("STATUS"))) {
							String newFileName = jo.getString("文件名");
							FileUtility.fileRename(oldFileName, newFileName);
							ImageItem ds = new ImageItem(jo);
							if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
								mWebView.evaluateJavascript("javascript:callbackCamera('" + ds.getDownAddress() + "')", null);
							}
							else
								mWebView.loadUrl("javascript:callbackCamera('" + ds.getDownAddress() + "')");
							Log.d(TAG, ds.getDownAddress());
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
					break;
			}
		}
	};
	private void openScanCode()
	{
		Intent intent = new Intent();
		intent.setClass(WebSiteActivity.this, CaptureActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivityForResult(intent, SCANNIN_GREQUEST_CODE);
	}
}

