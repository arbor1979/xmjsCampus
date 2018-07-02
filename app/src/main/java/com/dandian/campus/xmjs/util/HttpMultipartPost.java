package com.dandian.campus.xmjs.util;

import java.io.File;
import java.nio.charset.Charset;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.AsyncTask;

import com.dandian.campus.xmjs.api.CampusParameters;
import com.dandian.campus.xmjs.api.HttpManager;
import com.dandian.campus.xmjs.entity.CustomMultipartEntity;
import com.dandian.campus.xmjs.entity.CustomMultipartEntity.ProgressListener;


public class HttpMultipartPost extends AsyncTask<String, Integer, String> {
	private Context context;  
    public ProgressDialog pd;  
    private long totalSize;
    private CampusParameters myParams;
    HttpClient httpClient;
    public HttpMultipartPost(Context context, CampusParameters params) {  
        this.context = context;  
        this.myParams = params;  
    }  
  
    
    @Override  
    protected void onPreExecute() {  
        pd = new ProgressDialog(context);
        pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);  
        pd.setMessage("正在上传...");  
        pd.setCancelable(true);  
        pd.show();  
        pd.setOnCancelListener(new OnCancelListener() {                
        
			@Override
			public void onCancel(DialogInterface dialog) {
				if(httpClient!=null)
	    			httpClient.getConnectionManager().shutdown();
				
			}
        	  }); 
        
    }  
  
    @Override  
    protected String doInBackground(String... params) {  
        String serverResponse = null;  
  
        httpClient =HttpManager.getNewHttpClient();
        HttpContext httpContext = new BasicHttpContext();  
        HttpPost httpPost = new HttpPost("http://laoshi.dandian.net/upload.php");  
        
        
        try {  
            CustomMultipartEntity multipartContent = new CustomMultipartEntity(
            		new ProgressListener() {  
                        @Override  
                        public void transferred(long num) {  
                            publishProgress((int) ((num / (float) totalSize) * 100));  
                        }  
                    });  
            // We use FileBody to transfer an image  
            
            for(int i=0;i<myParams.size();i++)
            {
            	String key=myParams.getKey(i);
            	String value=myParams.getValue(key);
            	multipartContent.addPart(key, new StringBody(value, Charset.forName("UTF-8")));   
            	
            }  
            multipartContent.addPart("filename", new FileBody(new File(  
            		myParams.getValue("pic"))));  
           
            totalSize = multipartContent.getContentLength();  
  
            // Send it  
            httpPost.setEntity(multipartContent);  
            
            HttpResponse response = httpClient.execute(httpPost, httpContext);  
            serverResponse = EntityUtils.toString(response.getEntity());  
              
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
  
        return serverResponse;  
    }  
  
    @Override  
    protected void onProgressUpdate(Integer... progress) {  
        pd.setProgress(progress[0]);
        if(pd.getProgress()==100)
        	pd.setMessage("上传完毕，等待返回结果..");
    }  
  
      
  
    @Override  
    protected void onCancelled() {  
        System.out.println("cancle");  
        pd.dismiss(); 
    } 
}
