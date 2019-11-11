package com.dandian.campus.xmjs.adapter;


import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.dandian.campus.xmjs.R;

import java.util.ArrayList;
import java.util.HashMap;

public class ListViewImageAdapter extends BaseAdapter {
    private LayoutInflater mInflater;
    private ArrayList<HashMap<String, Object>> imageInfo;
    private AQuery aq;
    private Context mContext;
    public ListViewImageAdapter(Context context, ArrayList<HashMap<String, Object>> imageInfos) {
        mInflater = LayoutInflater.from(context);
        imageInfo = imageInfos;
        mContext=context;
        aq = new AQuery(context);
    }

    public int getCount() {
        return imageInfo.size();
    }

    public HashMap<String, Object> getItem(int position) {
        return imageInfo.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ListViewImageAdapter.ViewHolder holder;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.list_item_simple_image, null);
            holder = new ListViewImageAdapter.ViewHolder();
            holder.icon = (ImageView) convertView.findViewById(R.id.item_imageView);
            holder.path = (TextView) convertView.findViewById(R.id.item_textView);
            convertView.setTag(holder);
        } else {
            holder = (ListViewImageAdapter.ViewHolder) convertView.getTag();
        }
        if (imageInfo == null) {
            Log.i("ListViewAdapter", "imageInfo is null!");
            return convertView;
        }
        HashMap<String, Object> imgInfo=imageInfo.get(position);
        if (imgInfo == null) {
            Log.i("ListViewAdapter", "imageInfo.get(position) is null!");
            return convertView;
        }
        if(imgInfo.get("icon")!=null && imgInfo.get("icon").toString().length()>0)
        {
            holder.icon.setVisibility(View.VISIBLE);
            if(imgInfo.get("icon").toString().equals("add"))
                holder.icon.setImageResource(R.drawable.pic_add_more);
            else
                aq.id(holder.icon).progress(R.id.progressBar1).image(imgInfo.get("icon").toString(),false,true);
        }
        else
        {
            holder.icon.setVisibility(View.GONE);
        }
        holder.path.setText(imgInfo.get("name").toString());
        return convertView;
    }

    /* class ViewHolder */
    private class ViewHolder {
        TextView path;
        ImageView icon;
    }
}
