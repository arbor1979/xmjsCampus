package com.dandian.campus.xmjs.entity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 *  #(c) ruanyun PocketCampus <br/>
 *
 *  版本说明: $id:$ <br/>
 *
 *  功能说明: 校内通知
 * 
 *  <br/>创建说明: 2014-4-16 下午2:05:53 shengguo  创建文件<br/>
 * 
 *  修改历史:<br/>
 *
 */
public class BlogsItem {
	private String TemplateName;
	private String title;
	private List<Blog> notices;
	private String rightButton;
	private String rightButtonUrl;
	public BlogsItem() {
		super();
	}
	
	public BlogsItem(JSONObject jo) {
		TemplateName = jo.optString("适用模板");
		title = jo.optString("标题显示");
		JSONArray joArr = jo.optJSONArray("通知项");
		rightButton=jo.optString("右上按钮");
		rightButtonUrl=jo.optString("右上按钮URL");
		notices = getNotices(joArr);
	}

	public String getRightButton() {
		return rightButton;
	}

	public void setRightButton(String rightButton) {
		this.rightButton = rightButton;
	}

	public String getRightButtonUrl() {
		return rightButtonUrl;
	}

	public void setRightButtonUrl(String rightButtonUrl) {
		this.rightButtonUrl = rightButtonUrl;
	}

	private List<Blog> getNotices(JSONArray joArr) {
		List<Blog> notices = new ArrayList<Blog>();
		for (int i = 0; i < joArr.length(); i++) {
			Blog notice = new Blog(joArr.optJSONObject(i));
			notices.add(notice);
		}
		return notices;
	}
	

	public String getTemplateName() {
		return TemplateName;
	}

	public void setTemplateName(String templateName) {
		TemplateName = templateName;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public List<Blog> getNotices() {
		return notices;
	}

	public void setNotices(List<Blog> notices) {
		this.notices = notices;
	}

}
