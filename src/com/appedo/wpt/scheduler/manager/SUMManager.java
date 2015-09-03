package com.appedo.wpt.scheduler.manager;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;

import net.sf.json.JSONObject;

import com.appedo.wpt.scheduler.bean.SUMAuditLogBean;
import com.appedo.wpt.scheduler.bean.SUMNodeBean;
import com.appedo.wpt.scheduler.bean.SUMTestBean;
import com.appedo.wpt.scheduler.connect.DataBaseManager;
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
			// ArrayList<String> alLocations = testBean.getTargetLocationsArrayList();
			
			// To check expiry and MaxMeasurement Count
			if ( !getTestStatus(testBean) ) {
				//for (int i = 0; i < alLocations.size(); i++) {
					t = new RunTest(testBean.getLocation(), testBean);
					sumTestIdList.add(t);
					hmThreads.put(testBean.getTestId(), sumTestIdList);
					t.start();
				// }
			}
		} catch (Exception e) {
			LogManager.errorLog(e);
		} 
	}
	
	public Object[] getSUMTestForLocation(String strLocation, String mac, String userId) {
		SUMTestBean sumTestBean = null;
		SUMNodeBean sumNodeBean = null;
		NodeManager manager = null;
		SUMAuditLogBean auditLogBean = null;
		Connection con = null;
		
		try {
			con = DataBaseManager.giveConnection();
			manager = new NodeManager();
			sumTestBean = SUMScheduler.pollSUMTest(strLocation);
			
			if( sumTestBean != null ) {
				sumNodeBean = manager.getNodeDetails(con, mac);
				sumTestBean.setUserId(Integer.parseInt(userId));
				auditLogBean = manager.insertSUMlog(sumTestBean, sumNodeBean, strLocation, "Agent polled bean from queue");
			}
		} catch (Throwable th) {
			LogManager.errorLog(th);
		} finally {
			DataBaseManager.close(con);
			con = null;
		}
		
		return new Object[]{sumTestBean, auditLogBean};
	}
	
	public boolean getTestStatus(SUMTestBean testBean){
		Connection con = null;
		SUMDBI sumdbi = null;
		JSONObject jsonObject = null;
		boolean status = false;
		try {
			
			con = DataBaseManager.giveConnection();
			sumdbi = new SUMDBI();
			jsonObject = sumdbi.getUserDetails(con, testBean.getUserId());

			LogManager.infoLog("Json:: "+jsonObject.toString());
			if( !jsonObject.containsKey("start_date") ){
				sumdbi.deactivateTest(con, testBean.getUserId());
				status = true;
			} else {
				int maxNodeCount = sumdbi.getMaxMeasurementPerMonth(con, testBean.getUserId(), jsonObject);
				if ( maxNodeCount >= jsonObject.getInt("max_measurement_per_day") && jsonObject.getInt("max_measurement_per_day") != -1 ){ 
					sumdbi.deactivateTest(con, testBean.getUserId());
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

	public void insertHarTable(long testId, int statusCode, String statusText, String runTestCode, String location) {
		Connection con = null;
		SUMDBI sumdbi = null;
		try {
			con = DataBaseManager.giveConnection();
			sumdbi = new SUMDBI();
			sumdbi.insertHarTable(con, testId, statusCode, statusText, runTestCode, location);
		} catch (Exception e) {
			LogManager.errorLog(e);
		} finally {
			DataBaseManager.close(con);
			con = null;
			sumdbi = null;
		}
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
}
