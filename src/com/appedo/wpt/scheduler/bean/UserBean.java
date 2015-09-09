package com.appedo.wpt.scheduler.bean;

/**
 * Userbean is for replica of usermaster table
 * @author navin
 *
 */
public class UserBean {

	private int nUserId;
	private String strEmailId;
	private String strPassword;
	private int nEnterpriseId;
	private String strFirstName;
	private String strLastName;
	private String strMobileNo;
	private boolean bActiveFlag;
	public boolean isActiveFlag() {
		return bActiveFlag;
	}
	public void setActiveFlag(boolean activeFlag) {
		bActiveFlag = activeFlag;
	}
	public int getEnterpriseId() {
		return nEnterpriseId;
	}
	public void setEnterpriseId(int enterpriseId) {
		nEnterpriseId = enterpriseId;
	}
	public String getEmailId() {
		return strEmailId;
	}
	public void setEmailId(String strEmailId) {
		this.strEmailId = strEmailId;
	}
	public String getFirstName() {
		return strFirstName;
	}
	public void setFirstName(String strFirstName) {
		this.strFirstName = strFirstName;
	}
	public String getLastName() {
		return strLastName;
	}
	public void setLastName(String strLastName) {
		this.strLastName = strLastName;
	}
	public String getMobileNo() {
		return strMobileNo;
	}
	public void setMobileNo(String strMobileNo) {
		this.strMobileNo = strMobileNo;
	}
	public String getPassword() {
		return strPassword;
	}
	public void setPassword(String strPassword) {
		this.strPassword = strPassword;
	}
	public int getUserId() {
		return nUserId;
	}
	public void setUserId(int userId) {
		nUserId = userId;
	}

}
