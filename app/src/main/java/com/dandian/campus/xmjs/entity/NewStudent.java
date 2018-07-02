package com.dandian.campus.xmjs.entity;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import org.json.JSONObject;

import java.io.Serializable;

@DatabaseTable(tableName="Student")
public class NewStudent implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@DatabaseField(id = true)
	private String id;
	@DatabaseField
	private String studentID;
	@DatabaseField
	private String password;
	@DatabaseField
	private String name;
	@DatabaseField
	private String className;
	@DatabaseField
	private String gender;
	@DatabaseField
	private String phone;
	@DatabaseField
	private String email;
	@DatabaseField
	private String dormitory;
	@DatabaseField
	private String parentName;
	@DatabaseField
	private String parentPhone;
	@DatabaseField
	private String homeAddress;
	@DatabaseField
	private String remark;
	@DatabaseField
	private String status;
	@DatabaseField
	private int isModify=0;

	private String stuLetter;
	private String onecard;
	private String collect;
	private String payment;
	@DatabaseField
	private String picImage;
	public NewStudent() {

	}
	
	
	public NewStudent(JSONObject jo) {
		id = jo.optString("编号");
		studentID = jo.optString("身份证号");
		name = jo.optString("姓名");
		className = jo.optString("班号");
		gender = jo.optString("性别");
		picImage = jo.optString("照片");
		status= jo.optString("是否报到");
		dormitory= jo.optString("学生宿舍");
		onecard= jo.optString("一卡通卡号");
		collect= jo.optString("收取材料");
		payment= jo.optString("预交费");
	}
	
	
	public String getPayment() {
		return payment;
	}


	public void setPayment(String payment) {
		this.payment = payment;
	}


	public String getOnecard() {
		return onecard;
	}


	public void setOnecard(String onecard) {
		this.onecard = onecard;
	}


	public String getCollect() {
		return collect;
	}


	public void setCollect(String collect) {
		this.collect = collect;
	}


	/**
	 * 缂栧彿
	 * 
	 * @return
	 */
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	/**
	 * 瀛﹀彿
	 * 
	 * @return
	 */
	public String getStudentID() {
		return studentID;
	}

	public void setStudentID(String studentID) {
		this.studentID = studentID;
	}

	/**
	 * 瀵嗙爜
	 * 
	 * @return
	 */
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * 濮撳悕
	 * 
	 * @return
	 */
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * 鐝骇
	 * 
	 * @return
	 */
	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	/**
	 * 鎬у埆
	 * 
	 * @return
	 */
	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	/**
	 * 瀛︾敓鐢佃瘽
	 * 
	 * @return
	 */
	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	/**
	 * 瀛︾敓閭
	 * 
	 * @return
	 */
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	/**
	 * 瀛︾敓瀹胯垗
	 * 
	 * @return
	 */
	public String getDormitory() {
		return dormitory;
	}

	public void setDormitory(String dormitory) {
		this.dormitory = dormitory;
	}

	/**
	 * 瀹堕暱濮撳悕
	 * 
	 * @return
	 */
	public String getParentName() {
		return parentName;
	}

	public void setParentName(String parentName) {
		if(parentName!=null)
			this.parentName = parentName;
	}

	/**
	 * 瀹堕暱鐢佃瘽
	 * 
	 * @return
	 */
	public String getParentPhone() {
		return parentPhone;
	}

	public void setParentPhone(String parentPhone) {
		if(parentPhone!=null)
			this.parentPhone = parentPhone;
	}

	/**
	 * 瀹跺涵浣忓潃
	 * 
	 * @return
	 */
	public String getHomeAddress() {
		return homeAddress;
	}

	public void setHomeAddress(String homeAddress) {
		if(homeAddress!=null)
			this.homeAddress = homeAddress;
	}

	/**
	 * 澶囨敞
	 * 
	 * @return
	 */
	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	/**
	 * 瀛︾敓鐘舵??
	 * 
	 * @return
	 */
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	/**
	 * 鏄惁淇敼
	 * 
	 * @return
	 */
	public int getIsModify() {
		return isModify;
	}

	public void setIsModify(int isModify) {
		this.isModify = isModify;
	}
	
	public String getStuLetter() {
		return stuLetter;
	}
	public void setStuLetter(String stuLetter) {
		this.stuLetter = stuLetter;
	}
	
	public String getPicImage() {
		return picImage;
	}
	public void setPicImage(String picImage) {
		this.picImage = picImage;
	}

	
}
