package com.dandian.campus.xmjs.adapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.dandian.campus.xmjs.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ChildAdapter extends BaseAdapter {

	Context mContext;
	JSONArray mChildArr;// 子item标题数组

	/**
	 * 构造方法
	 * 
	 * @param context
	 */
	public ChildAdapter(Context context,JSONArray ChildArr) {
		mContext = context;
		mChildArr=ChildArr;
	}

	/**
	 * 为子ListVitem设置要显示的数据
	 * 
	 * @param childArr
	 */
	public void setChildData(JSONArray childArr) {
		this.mChildArr = childArr;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = LayoutInflater.from(mContext).inflate(
					R.layout.child_item_layout, null);
			holder.childText = (TextView) convertView
					.findViewById(R.id.child_textView);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		JSONObject obj = mChildArr.optJSONObject(position);
		if(obj!=null)
			holder.childText.setText(obj.optString("name")+"[￥"+obj.optString("price")+"]");
		
		return convertView;
	}

	static class ViewHolder {
		TextView childText;
	}

	/**
	 * 获取item总数
	 */
	@Override
	public int getCount() {
		if (mChildArr == null) {
			return 0;
		}
		return mChildArr.length();
	}

	/**
	 * 获取某一个Item的内容
	 */
	@Override
	public Object getItem(int position) {
		return mChildArr.optJSONObject(position);
	}

	/**
	 * 获取当前item的ID
	 */
	@Override
	public long getItemId(int position) {
		return position;
	}

}
