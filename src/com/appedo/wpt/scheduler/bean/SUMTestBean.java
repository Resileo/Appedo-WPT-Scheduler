package com.appedo.wpt.scheduler.bean;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;

import net.sf.json.JSONObject;

import com.appedo.wpt.scheduler.utils.UtilsFactory;

/**
 * Bean replica for the database table `eum_test_master`
 * This keeps the parameters like Test id, name, URL/Transaction to be tested and etc...
 * 
 * @author Navin
 *
 */
public class SUMTestBean implements Comparable<SUMTestBean>, Cloneable {
	
	private long lTestId;
	private Long lUserId;
	private String strTestName;
	private String strTestType;
	private String strURL;
	private String strTransaction;
	private int nRunEveryMinute;
	private boolean bStatus;
	private String startDate;
	private String endDate;
	private String strTestClassName;
	private String agentType;
	private String strLocation;
	private String connectionName;
	private String download;
	private String upload;
	private String latency;
	private String packetLoss;
	private String repeatView;
	private long slaId;
	private long slaSumId;
	private boolean aboveThreshold;
	private int thresholdValue;
	private int errorValue;
	private int minBreachCount;
		
	private Date dateQueuedOn = null;
	
	private HashSet<String> hsTargetLocations = null;
	
	public SUMTestBean() {
		hsTargetLocations = new HashSet<String>();
	}
	
	public String getStartDate() {
		return startDate;
	}
	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}
	
	public String getEndDate() {
		return endDate;
	}
	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}
	
	public long getTestId() {
		return lTestId;
	}
	public void setTestId(long testId) {
		lTestId = testId;
	}
	
	public long getUserId() {
		return lUserId;
	}
	public void setUserId(long userId) {
		lUserId = userId;
	}
	
	public int getRunEveryMinute() {
		return nRunEveryMinute;
	}
	public void setRunEveryMinute(int nRunEveryMin) {
		this.nRunEveryMinute = nRunEveryMin;
	}
	
	public String getTestName() {
		return strTestName;
	}
	public void setTestName(String strTestName) {
		this.strTestName = strTestName;
	}
	
	public String getURL() {
		return strURL;
	}
	public void setURL(String strURL) {
		this.strURL = strURL;
	}
	
	public boolean isStatus() {
		return bStatus;
	}
	public void setStatus(boolean status) {
		bStatus = status;
	}
	
	public String getTransaction() {
		return strTransaction;
	}
	public void setTransaction(String strTransaction) {
		this.strTransaction = strTransaction;
	}
	
	public String getTestType() {
		return strTestType;
	}
	public void setTestType(String strTestType) {
		this.strTestType = strTestType;
	}
	
	public String getTestClassName() {
		return strTestClassName;
	}
	
	public void setTestClassName(String strTestClassName) {
		this.strTestClassName = strTestClassName;
	}
	
	public HashSet<String> getTargetLocations() {
		return hsTargetLocations;
	}
	public ArrayList<String> getTargetLocationsArrayList() {
		ArrayList<String> alTargetLocations = new ArrayList<String>();
		
		Iterator<String> iter = hsTargetLocations.iterator();
		while(iter.hasNext()) {
			alTargetLocations.add(iter.next());
		}
		return alTargetLocations;
	}
	public void setTargetLocations(HashSet<String> hsLocations) {
		UtilsFactory.clearCollectionHieracy(this.hsTargetLocations);
		this.hsTargetLocations = hsLocations;
	}
	public void addTargetLocation(String strTargetLocation) {
		this.hsTargetLocations.add(strTargetLocation);
	}
	
	public Date getQueuedOn() {
		return dateQueuedOn;
	}
	public void setQueuedOn(Date dateQueuedOn) {
		this.dateQueuedOn = dateQueuedOn;
	}
	
	public JSONObject toJSON() {
		JSONObject joBean = new JSONObject();
		
		joBean.put("test_id", lTestId);
		joBean.put("user_id", lUserId);
		joBean.put("test_name", strTestName);
		joBean.put("test_type", strTestType);
		joBean.put("url", strURL);
		joBean.put("transaction", strTransaction);
		joBean.put("run_every_minute", nRunEveryMinute);
		joBean.put("status", bStatus);
		joBean.put("start_date", startDate);
		joBean.put("end_date", endDate);
		joBean.put("dateQueuedOn", dateQueuedOn);
		
		return joBean;
	}
	
	@Override
	public String toString() {
		return Long.toString(lTestId);
	}
	
	@Override
	public int compareTo(SUMTestBean another) {
		// compareTo should return < 0 if this is supposed to be
        // less than other, > 0 if this is supposed to be greater than 
        // other and 0 if they are supposed to be equal
    	
    	return ((int) (dateQueuedOn.getTime() - another.getQueuedOn().getTime()));
	}
	
	public String getAgentType() {
		return agentType;
	}
	public void setAgentType(String agentType) {
		this.agentType = agentType;
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	public String getLocation() {
		return strLocation;
	}

	public void setLocation(String strLocation) {
		this.strLocation = strLocation;
	}

	public String getConnectionName() {
		return connectionName;
	}

	public void setConnectionName(String connectionName) {
		this.connectionName = connectionName;
	}

	public String getDownload() {
		return download;
	}

	public void setDownload(String download) {
		this.download = download;
	}

	public String getUpload() {
		return upload;
	}

	public void setUpload(String upload) {
		this.upload = upload;
	}

	public String getLatency() {
		return latency;
	}

	public void setLatency(String latency) {
		this.latency = latency;
	}

	public String getPacketLoss() {
		return packetLoss;
	}

	public void setPacketLoss(String packetLoss) {
		this.packetLoss = packetLoss;
	}

	public String getRepeatView() {
		return repeatView;
	}

	public void setRepeatView(String repeatView) {
		this.repeatView = repeatView;
	}

	public long getSlaId() {
		return slaId;
	}

	public void setSlaId(long slaId) {
		this.slaId = slaId;
	}

	public long getSlaSumId() {
		return slaSumId;
	}

	public void setSlaSumId(long slaSumId) {
		this.slaSumId = slaSumId;
	}

	public boolean isAboveThreshold() {
		return aboveThreshold;
	}

	public void setAboveThreshold(boolean aboveThreshold) {
		this.aboveThreshold = aboveThreshold;
	}

	public int getThresholdValue() {
		return thresholdValue;
	}

	public void setThresholdValue(int thresholdValue) {
		this.thresholdValue = thresholdValue;
	}

	public int getErrorValue() {
		return errorValue;
	}

	public void setErrorValue(int errorValue) {
		this.errorValue = errorValue;
	}

	public int getMinBreachCount() {
		return minBreachCount;
	}

	public void setMinBreachCount(int minBreachCount) {
		this.minBreachCount = minBreachCount;
	}
}
