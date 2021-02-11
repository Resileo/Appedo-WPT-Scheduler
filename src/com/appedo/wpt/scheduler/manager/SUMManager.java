package com.appedo.wpt.scheduler.manager;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;

import net.sf.json.JSONObject;

import com.appedo.commons.connect.DataBaseManager;
import com.appedo.manager.LogManager;
import com.appedo.wpt.scheduler.bean.SUMTestBean;
import com.appedo.wpt.scheduler.common.Constants;
import com.appedo.wpt.scheduler.dbi.SUMDBI;
import com.appedo.wpt.scheduler.sum.RunTest;
import com.appedo.wpt.scheduler.utils.UtilsFactory;

/**
 * This manager will do the operations(add,modify & delete) on SUM Nodes and also it add sum test
 * 
 */
public class SUMManager {

	public static HashMap<Long, ArrayList<RunTest>> hmThreads = new HashMap<Long, ArrayList<RunTest>>();
	
	private ArrayList<RunTest> sumTestIdList = new ArrayList<RunTest>();
	
	public void runSUMTests(SUMTestBean testBean) throws Throwable {
		
		try {
			RunTest t;
			
			// To check expiry and MaxMeasurement Count
			if ( !getTestStatus(testBean) ) {
				// check for test type
				if(testBean.getTestType().equalsIgnoreCase("transaction")){
					SUMScheduler.queueSUMTest(testBean.getLocation(), testBean);
				} else {
					t = new RunTest(testBean.getLocation(), testBean);
					sumTestIdList.add(t);
					hmThreads.put(testBean.getTestId(), sumTestIdList);
					t.start();	
				}
			}
		} catch (Exception e) {
			LogManager.errorLog(e);
		} 
	}
	
	public Object[] getSUMTestForLocation(String strLocation, String mac) {
		SUMTestBean sumTestBean = null;
		Connection con = null;
		try {
			con = DataBaseManager.giveConnection();
			sumTestBean = SUMScheduler.pollSUMTest(strLocation);
		} catch (Throwable th) {
			LogManager.errorLog(th);
		} finally {
			DataBaseManager.close(con);
			con = null;
		}
		return new Object[]{sumTestBean};
	}
	
	/**
	 * Check User Expiry and MaxMeasurement Count
	 * 
	 * @param testBean
	 * @return
	 */
	public boolean getTestStatus(SUMTestBean testBean){
		Connection con = null;
		SUMDBI sumdbi = null;
		JSONObject jsonObject = null;
		boolean status = false;
		try {
			con = DataBaseManager.giveConnection();
			sumdbi = new SUMDBI();
			jsonObject = sumdbi.getUserDetails(con, testBean.getUserId());

			if( !jsonObject.containsKey("start_date") ){
				sumdbi.deactivateTest(con, testBean.getUserId());
				status = true;
			} else {
				int maxNodeCount = sumdbi.getMaxMeasurementPerMonth(con, testBean.getUserId(), jsonObject);
				if ( maxNodeCount >= jsonObject.getInt("max_measurement_per_day") && jsonObject.getInt("max_measurement_per_day") != -1 ){ 
					LogManager.infoLog("Max Measurement Reached for the day: "+testBean.getUserId());
					status = true;
				} else{
					status = false;
				}
			}
		} catch (Exception e) {
			LogManager.errorLog(e);

		} finally {
			DataBaseManager.close(con);
			con = null;
			sumdbi = null;
			UtilsFactory.clearCollectionHieracy( jsonObject );
		}
		return status;
	}


	public void sendSlaAlert(long test_id, boolean isDowntime, double firstLoadTime, String location, long har_id) throws Exception {
		Connection con = null;
		SUMDBI sumdbi = null;
		HttpClient client = null;
		PostMethod method = null;
		int statusCode;
		try {
			con = DataBaseManager.giveConnection();
			sumdbi = new SUMDBI();
			JSONObject joSLA = sumdbi.getTestIdDetails(con, test_id);
			
			if( isDowntime || (joSLA.getInt("threshold_set_value") > 0 && firstLoadTime > (joSLA.getInt("threshold_set_value")*1000)) ) {
				joSLA.put("appedoReceivedOn", new Date().getTime());
				joSLA.put("received_value", String.format( "%.2f", (firstLoadTime/1000)) );
				joSLA.put("breached_severity", firstLoadTime > (joSLA.getInt("err_set_value"))?"CRITICAL":"WARNING");
				joSLA.put("location", location);
				joSLA.put("is_Down", isDowntime);
				joSLA.put("har_id", har_id);
				
				LogManager.infoLog("json sla for SUM TestId: "+joSLA.getString("userid")+" <> SLA Alert :: "+joSLA.toString());
				
				client = new HttpClient();
				method = new PostMethod(Constants.APPEDO_SLA_COLLECTOR);
				method.addParameter("command", "sumBreachCounterSet");
				method.addParameter("sumBreachCounterset", joSLA.toString());
				statusCode = client.executeMethod(method);
				if (statusCode != HttpStatus.SC_OK) {
					LogManager.errorLog("Method failed: " + method.getStatusLine());
				}
			}
		}catch (Exception e) {
			LogManager.errorLog(e);
		}
	}
	
	public void sendCustomDownAlert(long test_id, String custom_key, String location) throws Exception {
		Connection con = null;
		SUMDBI sumdbi = null;
		HttpClient client = null;
		PostMethod method = null;
		int statusCode;
		try {
			con = DataBaseManager.giveConnection();
			sumdbi = new SUMDBI();
			JSONObject joSLA = sumdbi.getTestIdDetails(con, test_id);		
			joSLA.put("location", location);
			joSLA.put("type", "Configured Site is Down");
			joSLA.put("is_Down", true);
			joSLA.put("custom_key", custom_key);
			joSLA.put("appedoReceivedOn", new Date().getTime());
			
			LogManager.infoLog("json sla for SUM TestId: "+joSLA.getString("userid")+" <> SLA Alert :: "+joSLA.toString());
			
			client = new HttpClient();
			method = new PostMethod(Constants.APPEDO_SLA_COLLECTOR);
			method.addParameter("command", "sumCustomDownTimeAlert");
			method.addParameter("sumBreachCounterset", joSLA.toString());
			statusCode = client.executeMethod(method);
			if (statusCode != HttpStatus.SC_OK) {
				LogManager.errorLog("Method failed: " + method.getStatusLine());
			}
		
		}catch (Exception e) {
			LogManager.errorLog(e);
		}
	}
	
	public long insertHarTable(long testId, int statusCode, String statusText, String runTestCode, String location, String testUrl) {
		Connection con = null;
		SUMDBI sumdbi = null;
		long harId = 0;
		try {
			con = DataBaseManager.giveConnection();
			sumdbi = new SUMDBI();
			harId = sumdbi.insertHarTable(con, testId, statusCode, statusText, runTestCode, location, testUrl);
		} catch (Exception e) {
			LogManager.errorLog(e);
		} finally {
			DataBaseManager.close(con);
			con = null;
			sumdbi = null;
		}
		return harId;
	}

	public void updateHarTable(long testId, int statusCode, String statusText, String runTestCode, int loadTime, int repeatLoadTime) {
		Connection con = null;
		SUMDBI sumdbi = null;
		try {
			con = DataBaseManager.giveConnection();
			sumdbi = new SUMDBI();
			sumdbi.updateHarTable(con, testId, statusCode, statusText, runTestCode, loadTime, repeatLoadTime);
		} catch (Exception e) {
			LogManager.errorLog(e);
		} finally {
			DataBaseManager.close(con);
			con = null;
			sumdbi = null;
		}
	}
	
	public void updateHarTable(long testId, int statusCode, String statusText, String runTestCode, int loadTime, int repeatLoadTime, JSONObject JoTestResValue) {
		Connection con = null;
		SUMDBI sumdbi = null;
		try {
			con = DataBaseManager.giveConnection();
			sumdbi = new SUMDBI();
			sumdbi.updateHarTable(con, testId, statusCode, statusText, runTestCode, loadTime, repeatLoadTime, JoTestResValue);
		} catch (Exception e) {
			LogManager.errorLog(e);
		} finally {
			DataBaseManager.close(con);
			con = null;
			sumdbi = null;
		}
	}
	
	public void updateHarFileNameInTable(long testId, String runTestCode, String harFileName) {
		Connection con = null;
		SUMDBI sumdbi = null;
		try {
			con = DataBaseManager.giveConnection();
			sumdbi = new SUMDBI();
			sumdbi.updateHarFileNameInTable(con, testId, runTestCode, harFileName);
		} catch (Exception e) {
			LogManager.errorLog(e);
		} finally {
			DataBaseManager.close(con);
			con = null;
			sumdbi = null;
		}
	}
	
	public void updateSumTestLastRunDetail(long testId) {
		Connection con = null;
		SUMDBI sumdbi = null;
		try {
			con = DataBaseManager.giveConnection();
			sumdbi = new SUMDBI();
			sumdbi.updateSumTestLastRunDetail(con, testId);
		} catch (Exception e) {
			LogManager.errorLog(e);
		} finally {
			DataBaseManager.close(con);
			con = null;
			sumdbi = null;
		}
	}

	public void updateMeasurementCntInUserMaster(long testId) {
		Connection con = null;
		SUMDBI sumdbi = null;
		try {
			con = DataBaseManager.giveConnection();
			sumdbi = new SUMDBI();
			sumdbi.updateMeasurementCntInUserMaster(con, testId);
		} catch (Exception e) {
			LogManager.errorLog(e);
		} finally {
			DataBaseManager.close(con);
			con = null;
			sumdbi = null;
		}
	}

	public void insertResultJson(org.json.JSONObject joData, long harId) {
		Connection con = null;
		SUMDBI sumdbi = null;
		try {
			con = DataBaseManager.giveConnection();
			sumdbi = new SUMDBI();
			sumdbi.insertResultJson(con, joData, harId);
		} catch (Exception e) {
			LogManager.errorLog(e);
		} finally {
			DataBaseManager.close(con);
			con = null;
			sumdbi = null;
		}
	}
}
