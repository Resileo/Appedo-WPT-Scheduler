package com.appedo.wpt.scheduler.bean;

import net.sf.json.JSONObject;


/**
 * Bean replica for the database table `eum_node_details`
 * This keeps the parameters like Node id, mac_address, IP-address and etc...
 * 
 * @author Navin
 *
 */
public class SUMNodeBean {
	
	private Long lNodeId;
	private Long lUserId;
	private String strMACAddress;
	private String strAgentType;
	private String strIPAddress;
	private String strCity, strState, strCountry;
	private Double dLongitude;
	private Double dLatitude;
	private String strNodeStatus;
	
	public long getNodeId() {
		return lNodeId;
	}
	public void setNodeId(long nodeId) {
		this.lNodeId = nodeId;
	}
	
	public Long getUserId() {
		return lUserId;
	}
	public void setUserId(long userId) {
		lUserId = userId;
	}
	
	public String getIPAddresses() {
		return strIPAddress;
	}
	public void setIPAddresses(String strIPAddresses) {
		this.strIPAddress = strIPAddresses;
	}
	
	public String getCity() {
		return strCity;
	}
	public void setCity(String strCity) {
		this.strCity = strCity;
	}
	
	public String getCountry() {
		return strCountry;
	}
	public void setCountry(String strCountry) {
		this.strCountry = strCountry;
	}
	
	public String getMacAddress() {
		return strMACAddress;
	}

	public void setMacAddress(String macAddress) {
		this.strMACAddress = macAddress;
	}

	public Double getLongitude() {
		return dLongitude;
	}

	public void setLongitude(Double longitude) {
		this.dLongitude = longitude;
	}
	
	public Double getLatitude() {
		return dLatitude;
	}
	public void setLatitude(Double latitude) {
		this.dLatitude = latitude;
	}
	
	public String getAgentType() {
		return strAgentType;
	}
	public void setAgentType(String agentType) {
		this.strAgentType = agentType;
	}
	
	public String getNodeStatus() {
		return strNodeStatus;
	}
	public void setNodeStatus(String strNodeStatus) {
		this.strNodeStatus = strNodeStatus;
	}
	
	public JSONObject toJSON() {
		JSONObject joBean = new JSONObject();
		
		joBean.put("node_id", lNodeId);
		joBean.put("user_id", lUserId);
		joBean.put("mac_address", strMACAddress);
		joBean.put("agent_type", strAgentType);
		joBean.put("ip_address", strIPAddress);
		joBean.put("city", strCity);
		joBean.put("state", strState);
		joBean.put("country", strCountry);
		joBean.put("longitude", dLongitude);
		joBean.put("latitude", dLatitude);
		joBean.put("node_status", strNodeStatus);
		
		return joBean;
	}
}
