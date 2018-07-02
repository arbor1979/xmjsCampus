package com.dandian.campus.xmjs.entity;

import org.json.JSONObject;

import java.io.Serializable;

public class DormEntity implements Serializable,Cloneable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private int id;

	private String buildingName;
	
	private String roomName;
	
	private int price;
	
	private int bedsNum;
	private int alreadyIn;
	private String sex;
	
	private String banji;
	
	private String buildingNo;
	private String url;
	
	
	public DormEntity(JSONObject jo){
		id=jo.optInt("编号");
		buildingName=jo.optString("宿舍楼");
		roomName=jo.optString("房间名称");
		price=jo.optInt("房间价格标准");
		bedsNum=jo.optInt("房间床位数");
		sex=jo.optString("性别");
		banji=jo.optString("所属班级");
		buildingNo=jo.optString("楼栋号");
		url=jo.optString("url");
		alreadyIn=jo.optInt("已住人数");
	}



	public int getAlreadyIn() {
		return alreadyIn;
	}



	public void setAlreadyIn(int alreadyIn) {
		this.alreadyIn = alreadyIn;
	}



	public String getUrl() {
		return url;
	}



	public void setUrl(String url) {
		this.url = url;
	}



	public int getId() {
		return id;
	}



	public void setId(int id) {
		this.id = id;
	}



	public String getBuildingName() {
		return buildingName;
	}



	public void setBuildingName(String buildingName) {
		this.buildingName = buildingName;
	}



	public String getRoomName() {
		return roomName;
	}



	public void setRoomName(String roomName) {
		this.roomName = roomName;
	}



	public int getPrice() {
		return price;
	}



	public void setPrice(int price) {
		this.price = price;
	}



	public int getBedsNum() {
		return bedsNum;
	}



	public void setBedsNum(int bedsNum) {
		this.bedsNum = bedsNum;
	}



	public String getSex() {
		return sex;
	}



	public void setSex(String sex) {
		this.sex = sex;
	}



	public String getBanji() {
		return banji;
	}



	public void setBanji(String banji) {
		this.banji = banji;
	}



	public String getBuildingNo() {
		return buildingNo;
	}



	public void setBuildingNo(String buildingNo) {
		this.buildingNo = buildingNo;
	}
	
	
}
