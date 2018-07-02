package com.dandian.campus.xmjs.fragment;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.CategorySeries;
import org.achartengine.model.SeriesSelection;
import org.achartengine.renderer.DefaultRenderer;
import org.achartengine.renderer.SimpleSeriesRenderer;
import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dandian.campus.xmjs.R;
import com.dandian.campus.xmjs.api.CampusAPI;
import com.dandian.campus.xmjs.api.CampusException;
import com.dandian.campus.xmjs.api.CampusParameters;
import com.dandian.campus.xmjs.api.RequestListener;
import com.dandian.campus.xmjs.base.Constants;
import com.dandian.campus.xmjs.db.DatabaseHelper;
import com.dandian.campus.xmjs.util.AppUtility;
import com.dandian.campus.xmjs.util.Base64;
import com.dandian.campus.xmjs.util.PrefUtility;

public class StudentAttenceFragment extends Fragment {
	LinearLayout ll_pie_chart;
	DatabaseHelper database;
	
	private LinearLayout loadingLayout;
	private LinearLayout contentLayout;
	private LinearLayout failedLayout;
	TextView stuinfo_stuname;
	TextView stuinfo_attendanceRate;
	TextView stuinfo_rate;
	private GraphicalView mChartView;
	private DefaultRenderer mRenderer = new DefaultRenderer();
	private CategorySeries mSeries = new CategorySeries("");
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View localView = inflater.inflate(R.layout.view_stuinfo2, container,
				false);
		ll_pie_chart = (LinearLayout) localView.findViewById(R.id.ll_pie_chart);
		loadingLayout = (LinearLayout) localView.findViewById(R.id.data_load);
		contentLayout = (LinearLayout) localView.findViewById(R.id.content_layout);
		failedLayout = (LinearLayout) localView.findViewById(R.id.empty_error);
		stuinfo_stuname = (TextView) localView.findViewById(R.id.stuinfo_stuname);
		stuinfo_rate = (TextView) localView.findViewById(R.id.stuinfo_rate);
		stuinfo_attendanceRate = (TextView) localView.findViewById(R.id.stuinfo_attendanceRate);
		mChartView = ChartFactory.getPieChartView(getActivity(), mSeries, mRenderer);
	    mRenderer.setClickEnabled(true);
	    mRenderer.setStartAngle(180);
	    mRenderer.setDisplayValues(true);
	    mRenderer.setAntialiasing(true);
	    mRenderer.setInScroll(false);
	    mRenderer.setLabelsTextSize(28);
	    mRenderer.setLabelsColor(Color.BLACK);
	    mRenderer.setLegendTextSize(28);// 图标字体大小
	    mRenderer.setPanEnabled(false);//图表是否可以移动
	    mRenderer.setZoomEnabled(false);//图表是否可以缩放
        
	    ll_pie_chart.addView(mChartView, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT));
	    mChartView.setOnClickListener(new View.OnClickListener() {
	        @Override
	        public void onClick(View v) {
	          SeriesSelection seriesSelection = mChartView.getCurrentSeriesAndPoint();
	          if (seriesSelection != null) {
	            
	            for (int i = 0; i < mSeries.getItemCount(); i++) {
	              mRenderer.getSeriesRendererAt(i).setHighlighted(i == seriesSelection.getPointIndex());
	            }
	            mChartView.repaint();
	                 
	          }
	        }
	      });
	    
		Bundle bundle = getArguments();
		String studentId = bundle.getString("studentId");
		
		getAttenceData(studentId);
		
		
		return localView;
	}
	private void showProgress(boolean progress) {
		if (progress) {
			loadingLayout.setVisibility(View.VISIBLE);
			contentLayout.setVisibility(View.GONE);
			failedLayout.setVisibility(View.GONE);
		} else {
			loadingLayout.setVisibility(View.GONE);
			contentLayout.setVisibility(View.VISIBLE);
			failedLayout.setVisibility(View.GONE);
		}
	}
	public void getAttenceData(String studentId) {
		showProgress(true);
		
		long datatime = System.currentTimeMillis();
		String checkCode = PrefUtility.get(Constants.PREF_CHECK_CODE, "");
		JSONObject jo = new JSONObject();
		try {
			jo.put("用户较验码", checkCode);
			jo.put("DATETIME", datatime);
			jo.put("studentId", studentId);
			jo.put("当前学期",  PrefUtility.get(Constants.PREF_CUR_XUEQI, ""));
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		String base64Str = Base64.encode(jo.toString().getBytes());
		CampusParameters params = new CampusParameters();
		params.add(Constants.PARAMS_DATA, base64Str);
		CampusAPI.getSchoolItem(params, "XUESHENG-KAOQIN-Student.php", new RequestListener() {

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
				msg.what = 0;
				msg.obj = response;
				mHandler.sendMessage(msg);
			}
		});
	}
	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			String result = msg.obj.toString();
			String resultStr = "";
			switch (msg.what) {
			case -1:
				showProgress(false);
				AppUtility.showErrorToast(getActivity(), msg.obj.toString());
				break;
			case 0:
				showProgress(false);
				if (AppUtility.isNotEmpty(result)) {
					try {
						resultStr = new String(Base64.decode(result
								.getBytes("GBK")));
						
					} catch (UnsupportedEncodingException e) {
						
						e.printStackTrace();
					}
				}

				if (AppUtility.isNotEmpty(resultStr)) {
					try {
						JSONObject jo = new JSONObject(resultStr);
						String res = jo.optString("结果");
						
						if (AppUtility.isNotEmpty(res)) {
							AppUtility.showToastMsg(getActivity(), res);
						} else {
							
							initDate(jo);
						}
					} catch (JSONException e) {
						
						e.printStackTrace();
					}
				}
				break;
			
			}
		}
	};
	private void initDate(JSONObject jo)
	{
		
		stuinfo_stuname.setText(jo.optString("用户姓名")+"的课堂出勤情况");
		stuinfo_attendanceRate.setText("("+jo.optString("当前学期")+")");
		JSONObject json = jo.optJSONObject("考勤数值");
		stuinfo_rate.setText("出勤率："+jo.optString("出勤率"));
		try {
			Iterator<String> keyIter=json.keys();
	
			while(keyIter.hasNext())
			{
				String key = keyIter.next();
				
				double value = Double.parseDouble(json.getJSONObject(key).optString("值"));
				if(value==0) continue;
				 mSeries.add(key, value);
			     SimpleSeriesRenderer renderer = new SimpleSeriesRenderer();
			     renderer.setColor(Color.parseColor(json.getJSONObject(key).optString("颜色")));
			     mRenderer.addSeriesRenderer(renderer);
			}
			mChartView.repaint();
				
		
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	/*
	 * private void initView(View view){ ll_pie_chart =
	 * (LinearLayout)view.findViewById(R.id.ll_pie_chart);
	 * 
	 * int[] colors = new int[] { Color.RED, Color.YELLOW, Color.BLUE };
	 * //DefaultRenderer renderer = buildCategoryRenderer(colors);
	 * CategorySeries categorySeries = new CategorySeries("Vehicles Chart");
	 * categorySeries.add("cars ", 30); categorySeries.add("trucks", 20);
	 * categorySeries.add("bikes ", 60);
	 * 
	 * 
	 * DefaultRenderer renderer = new DefaultRenderer(); for (int color :
	 * colors) { SimpleSeriesRenderer r = new SimpleSeriesRenderer();
	 * r.setColor(color); renderer.addSeriesRenderer(r);
	 * renderer.setBackgroundColor(Color.WHITE); } View view1 =
	 * ChartFactory.getPieChartView(getActivity(), categorySeries, renderer);
	 * ll_pie_chart.addView(view1);
	 * 
	 * 
	 * }
	 */
	
}
