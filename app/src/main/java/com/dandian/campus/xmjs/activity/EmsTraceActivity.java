package com.dandian.campus.xmjs.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.dandian.campus.xmjs.R;
import com.dandian.campus.xmjs.api.CampusAPI;
import com.dandian.campus.xmjs.api.CampusException;
import com.dandian.campus.xmjs.api.CampusParameters;
import com.dandian.campus.xmjs.api.RequestListener;
import com.dandian.campus.xmjs.entity.EMSEntry;
import com.dandian.campus.xmjs.entity.EMSEntry.EMSItemEntry;
import com.dandian.campus.xmjs.util.AppUtility;

import org.json.JSONObject;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.Random;


public class EmsTraceActivity extends Activity {
	
	AQuery aq;
	MyAdapter adapter;
	TextView chat_msg_none;
	ListView listview;
	String emsno;
	EMSEntry emsEntry;
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ems_trace);
		chat_msg_none = (TextView) findViewById(R.id.chat_msg_none);
		listview=(ListView)findViewById(R.id.message_list);
		emsno=getIntent().getStringExtra("emsno");
		
		aq = new AQuery(this);
		
		aq.id(R.id.back).clicked(new OnClickListener(){

			@Override
			public void onClick(View v) {
				finish();
			}
			
		});
		
		getStatus();
		
	}
	
	private void getStatus() {
		Date dt=new Date();
		Random generator = new Random(); 
		DecimalFormat fmt2 = new DecimalFormat ("###"); 
		int num2 = generator.nextInt(999); 
		String timestamp=String.valueOf(dt.getTime())+fmt2.format(num2);
		String url="https://open.onebox.so.com/api/getkuaidi?com=ems&nu="+emsno+"&_="+timestamp;

		CampusParameters params = new CampusParameters();
		CampusAPI.getUrl(url,params, new RequestListener() {

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
	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {

		public void handleMessage(Message msg) {
			switch (msg.what) {
			case -1:
				
				AppUtility.showErrorToast(EmsTraceActivity.this,
						msg.obj.toString());
				break;
			case 0:
				
				String result = msg.obj.toString();
				
				try 
				{
					JSONObject jo = new JSONObject(result);
					String loginStatus = jo.optString("errcode");
					
					if (!loginStatus.equals("0")) {
						AppUtility.showToastMsg(EmsTraceActivity.this, jo.optString("errmsg"),1);
					} else 
					{
						
						emsEntry=new EMSEntry(jo.optJSONObject("data"));
						adapter=new MyAdapter(EmsTraceActivity.this);
						listview.setAdapter(adapter);
					}
					
				} catch (Exception e) {
					
					e.printStackTrace();
				}
				
				break;
		
			}
		};
	};
	
	
	public class MyAdapter extends BaseAdapter{
		 
        private LayoutInflater mInflater;
        public MyAdapter(Context context){
            this.mInflater = LayoutInflater.from(context);
          
        }
        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return emsEntry.getData().size();
        }
 
        @Override
        public Object getItem(int arg0) {
            // TODO Auto-generated method stub
        	
			return emsEntry.getData().get(arg0);
			
        	
        }
 
        @Override
        public long getItemId(int arg0) {
            // TODO Auto-generated method stub
            return arg0;
        }
        @Override  
        public boolean isEnabled(int position) {   
           return false;   
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
             
        	View view=convertView;  
        	view=mInflater.inflate(R.layout.list_ems_item, null);  
        	EMSItemEntry item=emsEntry.getData().get(position);
        	TextView cornerTv=(TextView)view.findViewById(R.id.cornerTv);
            TextView detail=(TextView)view.findViewById(R.id.item_textView);
            detail.setText(Html.fromHtml(item.getTime()+"<br>"+item.getContext()));
            detail.setMovementMethod(LinkMovementMethod.getInstance());
            cornerTv.setBackgroundResource(R.drawable.corner_view_gray);
            if(emsEntry.getSuccess() && position==0)
            {
            	cornerTv.setBackgroundResource(R.drawable.corner_view_green);
            }
            
            return view;
        }
       
         
    }
	

	
}
