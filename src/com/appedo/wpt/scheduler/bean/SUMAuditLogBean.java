package com.appedo.wpt.scheduler.bean;

public class SUMAuditLogBean implements Comparable<SUMAuditLogBean>{

	private long auditLogId;
	private long nodeId;
	private long nodeUserId;
	private String agentType;
	private long sumTestId;
	private String sumTestName;
	private long appedoUserId;
	private int appedoEnterpriseId;
	private int executionDurationInMin;
	private boolean executionStatus;
	private int executionTime;
	private String timestampOffsetValue;
	private String location;
	private Double latitude;
	private Double longitude;
	private String ipAddress;
	private String macAddress;
	private String errorMsg;
	private String remarks;
	private String createdOn;
	
	public long getAuditLogId() {
		return auditLogId;
	}
	public void setAuditLogId(long auditLogId) {
		this.auditLogId = auditLogId;
	}
	
	public long getNodeId() {
		return nodeId;
	}
	public void setNodeId(long nodeId) {
		this.nodeId = nodeId;
	}
	
	public long getNodeUserId() {
		return nodeUserId;
	}
	public void setNodeUserId(long nodeUserId) {
		this.nodeUserId = nodeUserId;
	}
	
	public String getAgentType() {
		return agentType;
	}
	public void setAgentType(String agentType) {
		this.agentType = agentType;
	}
	
	public long getSumTestId() {
		return sumTestId;
	}
	public void setSumTestId(long sumTestId) {
		this.sumTestId = sumTestId;
	}
	
	public String getSumTestName() {
		return sumTestName;
	}
	public void setSumTestName(String sumTestName) {
		this.sumTestName = sumTestName;
	}
	
	public long getAppedoUserId() {
		return appedoUserId;
	}
	public void setAppedoUserId(long appedoUserId) {
		this.appedoUserId = appedoUserId;
	}
	
	public int getAppedoEnterpriseId() {
		return appedoEnterpriseId;
	}
	public void setAppedoEnterpriseId(int appedoEnterpriseId) {
		this.appedoEnterpriseId = appedoEnterpriseId;
	}
	
	public int getExecutionDurationInMin() {
		return executionDurationInMin;
	}
	public void setExecutionDurationInMin(int executionDurationInMin) {
		this.executionDurationInMin = executionDurationInMin;
	}
	
	public boolean isExecutionStatus() {
		return executionStatus;
	}
	public void setExecutionStatus(boolean executionStatus) {
		this.executionStatus = executionStatus;
	}
	
	public int getExecutionTime() {
		return executionTime;
	}
	public void setExecutionTime(int executionTime) {
		this.executionTime = executionTime;
	}
	
	public String getTimestampOffsetValue() {
		return timestampOffsetValue;
	}
	public void setTimestampOffsetValue(String timestampOffsetValue) {
		this.timestampOffsetValue = timestampOffsetValue;
	}
	
	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
	}
	
	public Double getLatitude() {
		return latitude;
	}
	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}
	
	public Double getLongitude() {
		return longitude;
	}
	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}
	
	public String getIpAddress() {
		return ipAddress;
	}
	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}
	
	public String getMacAddress() {
		return macAddress;
	}
	public void setMacAddress(String macAddress) {
		this.macAddress = macAddress;
	}
	
	public String getErrorMsg() {
		return errorMsg;
	}
	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}
	
	public String getRemarks() {
		return remarks;
	}
	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}
	
	public String getCreatedOn() {
		return createdOn;
	}
	public void setCreatedOn(String createdOn) {
		this.createdOn = createdOn;
	}
	
	@Override
	public int compareTo(SUMAuditLogBean auditLogBean) {
		return (int) auditLogBean.getSumTestId();
	}
}
