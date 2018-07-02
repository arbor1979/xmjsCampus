package com.dandian.campus.xmjs.adapter;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.dandian.campus.xmjs.R;
import com.dandian.campus.xmjs.entity.QuestionnaireList.Question;
import com.dandian.campus.xmjs.fragment.SchoolQuestionnaireDetailFragment;
import com.dandian.campus.xmjs.util.AppUtility;

/**
 * 向picturechooseFragment listView添加数据
 * 
 * @author shengguo
 * 
 */
public class ListOfBillAdapter extends BaseAdapter{
	private LayoutInflater mInflater;
	private List<HashMap<String, Object>> imageInfo;
	private Context mContext;
	private JSONObject mProductObj;
	private ListView mList1,mList2;
	private GroupAdapter groupAdapter;
	private ChildAdapter childAdapter;
	private Dialog userTypeDialog;
	private Question mQuestion;
	private int mPosition;
	int mFocusPosition = -1;
	
	private class OnFocusChangeListenerImpl implements OnFocusChangeListener {
        private int position;
        public OnFocusChangeListenerImpl(int position) {
            this.position = position;
            
        }
        @Override
        public void onFocusChange(View arg0, boolean arg1) {
            EditText et = (EditText) arg0;
            HashMap<String, Object> item=imageInfo.get(position);
            
            if(arg1) {
                //Log.e("", "获得焦点"+detailItem.getId());
            	mFocusPosition = position;
            } else {
            	
                //Log.e("", "失去焦点"+detailItem.getId());
                try
                {
                	int num=Integer.parseInt(et.getText().toString());
                	if(Integer.valueOf(String.valueOf(item.get("num")))!=num)
                	{
                		JSONObject jo=new JSONObject();
                		jo.put("num", num);
                		updateList(jo,"update",position);
                	}
                }
                catch(NumberFormatException e)
                {
                	AppUtility.showToastMsg(mContext, "请输入整型数字");
                	et.setText(String.valueOf(item.get("num")));
                } catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
                
            }
        }
         
    }
	
	public ListOfBillAdapter(Context context, List<HashMap<String, Object>> imageInfos,Question question,int position) {
		mInflater = LayoutInflater.from(context);
		imageInfo = imageInfos;
		mContext=context;
		mProductObj=question.getFilterObj();
		mQuestion=question;
		mPosition=position;
		
	}

	public int getCount() {
		return imageInfo.size();
	}

	public Object getItem(int position) {
		return imageInfo.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder holder;

		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.list_item_bill, null);
			holder = new ViewHolder();

			holder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
			holder.tv_price = (TextView) convertView.findViewById(R.id.tv_price);
			holder.et_num = (EditText) convertView.findViewById(R.id.et_num);
			holder.tv_jine = (TextView) convertView.findViewById(R.id.tv_jine);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		final HashMap<String, Object> imgInfo=imageInfo.get(position);
		if(imgInfo.get("id").equals(0))
		{
			holder.tv_price.setVisibility(View.GONE);
			holder.et_num.setVisibility(View.GONE);
		}
		else
		{
			holder.tv_price.setVisibility(View.VISIBLE);
			holder.et_num.setVisibility(View.VISIBLE);
		}
		holder.tv_name.setText((String) imgInfo.get("name"));
		holder.tv_price.setText(String.valueOf(imgInfo.get("price")));
		holder.et_num.setText(String.valueOf(imgInfo.get("num")));
		holder.et_num.setOnFocusChangeListener(new OnFocusChangeListenerImpl(position));
		
		if (mFocusPosition == position) {
			holder.et_num.requestFocus();
        } 
        
		holder.tv_jine.setText(String.valueOf(imgInfo.get("jine")));
		holder.tv_name.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				if(imgInfo.get("id").equals(0))
				{
					userTypeDialog = new Dialog(mContext, R.style.dialog);
					View view = mInflater.inflate(
							R.layout.view_bill_itemlist_dialog, null);
					mList1 = (ListView) view.findViewById(R.id.listView1);
					mList2 = (ListView) view.findViewById(R.id.listView2);
					Button cancel= (Button) view.findViewById(R.id.cancel);
					cancel.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							userTypeDialog.dismiss();
						}
					});
					if(mProductObj!=null && mProductObj.length()>0)
					{
						String []groupNameArray=new String[mProductObj.length()];
						Iterator<String> keyIter=mProductObj.keys();
						int i=0;
						while (keyIter.hasNext()) 
						{ 
					        String key = keyIter.next();
					        groupNameArray[i]=key;
					        i++;
						}
						groupAdapter = new GroupAdapter(mContext,groupNameArray);
						mList1.setAdapter(groupAdapter);
						try {
							JSONArray ja=mProductObj.getJSONArray(groupNameArray[0]);
							childAdapter = new ChildAdapter(mContext,ja);
							mList2.setAdapter(childAdapter);
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						mList1.setOnItemClickListener(new MyGroupItemClick());
						mList2.setOnItemClickListener(new MyChildItemClick());						
					}
					Window window = userTypeDialog.getWindow();
					window.setGravity(Gravity.BOTTOM);// 在底部弹出
					window.setWindowAnimations(R.style.CustomDialog);
					userTypeDialog.setContentView(view);
					userTypeDialog.show();
				}
				else
				{
					new AlertDialog.Builder(mContext)
				    .setMessage("是否删除此行?")
				    .setPositiveButton("是", new DialogInterface.OnClickListener()
				    {
				    @Override
				    public void onClick(DialogInterface dialog, int which) {
				      
				    	updateList(null,"del",position);
				    }})
				    .setNegativeButton("否", null)
				    .show();
				}
					
			}
			
		});
		return convertView;
	}

	/* class ViewHolder */
	private class ViewHolder {
		TextView tv_name;
		TextView tv_price;
		EditText et_num;
		TextView tv_jine;
	}
	class MyGroupItemClick implements OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			
			groupAdapter.setSelectedPosition(position);
			groupAdapter.notifyDataSetChanged();
			String key=(String) groupAdapter.getItem(position);
			try {
				JSONArray ja=mProductObj.getJSONArray(key);
				childAdapter.setChildData(ja);
				childAdapter.notifyDataSetChanged();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}
	class MyChildItemClick implements OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			HashMap<String, Object> tempHashMap=imageInfo.get(imageInfo.size()-1);
			if(tempHashMap.get("id").equals(0))
				imageInfo.remove(tempHashMap);
			JSONObject item=(JSONObject) childAdapter.getItem(position);
			userTypeDialog.dismiss();
			updateList(item,"add",0);
			
		}

	}
	
	@SuppressLint("NewApi")
	private void updateList(JSONObject item,String action,int index)
	{
		FragmentActivity parentAct=(FragmentActivity)mContext;
		SchoolQuestionnaireDetailFragment fragment=(SchoolQuestionnaireDetailFragment) parentAct.getSupportFragmentManager().findFragmentById(
				android.R.id.content);
		if(action.equals("add"))
		{
			boolean flag=false;
			for(int i=0;i<mQuestion.getFujianArray().length();i++)
			{
				JSONObject jo;
				try {
					jo = mQuestion.getFujianArray().getJSONObject(i);
					if(jo!=null)
					{
						if(jo.optInt("id")==item.optInt("id"))
						{
							jo.put("num", jo.optInt("num")+1);
							flag=true;
							break;
						}
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if(!flag)
				mQuestion.getFujianArray().put(item);
		}
		else if(action.equals("update"))
		{
			JSONObject jo;
			try {
				jo = mQuestion.getFujianArray().getJSONObject(index);
				jo.put("num", item.optInt("num"));
				jo.put("jine", item.optInt("num")*jo.optDouble("price"));
				mQuestion.getFujianArray().put(index, jo);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		else if(action.equals("del"))
			mQuestion.getFujianArray().remove(index);
		fragment.updateQuestions(mQuestion, mPosition);
	}
	
}
