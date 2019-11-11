package com.dandian.campus.xmjs.entity;

import org.json.JSONObject;

//校内item
public class SchoolWorkItem {
	private String workPicPath;// 事物图标
	private String workText;// 事物标题
	private String interfaceName;// 接口名称
	private String TemplateName;// 模板名称
	private String groupName;// 分组
	private String transIcon;// 透明图标
	private int unread;//未读
	private String ifShowCount;//是否显示角标
	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public String getTransIcon() {
		return transIcon;
	}

	public void setTransIcon(String transIcon) {
		this.transIcon = transIcon;
	}

	public SchoolWorkItem() {

	}

	public String getIfShowCount() {
		return ifShowCount;
	}

	public SchoolWorkItem(JSONObject jo) {
		workPicPath = jo.optString("图标");
		workText = jo.optString("文字");
		interfaceName = jo.optString("接口地址");
		TemplateName = jo.optString("模板名称");
		groupName=jo.optString("分组");
		transIcon=jo.optString("透明图标");
		ifShowCount=jo.optString("是否有角标");
		if(transIcon==null || transIcon.length()==0)
			transIcon=workPicPath;
		unread=0;
	}

	public int getUnread() {
		return unread;
	}

	public void setUnread(int unread) {
		this.unread = unread;
	}

	public String getWorkPicPath() {
		return workPicPath;
	}

	public void setWorkPicPath(String workPicPath) {
		this.workPicPath = workPicPath;
	}

	public String getWorkText() {
		return workText;
	}

	public void setWorkText(String workText) {
		this.workText = workText;
	}

	public String getInterfaceName() {
		return interfaceName;
	}

	public void setInterfaceName(String interfaceName) {
		this.interfaceName = interfaceName;
	}

	public String getTemplateName() {
		return TemplateName;
	}

	public void setTemplateName(String templateName) {
		TemplateName = templateName;
	}
}
