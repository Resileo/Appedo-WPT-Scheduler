package com.appedo.wpt.scheduler.manager;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.PriorityBlockingQueue;

import com.appedo.manager.LogManager;
import com.appedo.wpt.scheduler.bean.SUMAuditLogBean;
import com.appedo.wpt.scheduler.bean.SUMNodeBean;
import com.appedo.wpt.scheduler.bean.SUMTestBean;
import com.appedo.wpt.scheduler.connect.DataBaseManager;
import com.appedo.wpt.scheduler.dbi.SUMDBI;
import com.appedo.wpt.scheduler.utils.UtilsFactory;

public class NodeManager {
	
	// Connection con = null;
	public static ArrayList< PriorityBlockingQueue<SUMAuditLogBean> > alAuditLogQueues = null;
	public static ArrayList< PriorityBlockingQueue<String> > alAgentDetailsQueues = null;
	//public static PriorityBlockingQueue<EUMAuditLogBean> pqAuditLogBeans = null;
	
	
	public static void createSUMAuditLogQueues() {
		PriorityBlockingQueue<SUMAuditLogBean> pqAuditLogBeans = null;
		alAuditLogQueues = new ArrayList< PriorityBlockingQueue<SUMAuditLogBean> >();
		
		for(int nLogTimerIndex=0; nLogTimerIndex<10; nLogTimerIndex++){
			pqAuditLogBeans = new PriorityBlockingQueue<SUMAuditLogBean>();
			alAuditLogQueues.add(pqAuditLogBeans);
		}
	}
	
	public static void createAgentDetailsQueues() {
		PriorityBlockingQueue<String> pqAgentDetails = null;
		alAgentDetailsQueues = new ArrayList< PriorityBlockingQueue<String> >();
		
		for(int nLogTimerIndex=0; nLogTimerIndex<10; nLogTimerIndex++){
			pqAgentDetails = new PriorityBlockingQueue<String>();
			alAgentDetailsQueues.add(pqAgentDetails);
		}
	}
	
	public static void auditLogQueue(SUMAuditLogBean auditLogBean){
		
		PriorityBlockingQueue<SUMAuditLogBean> pqAuditLogBeans = alAuditLogQueues.get( (int)Thread.currentThread().getId()%10 );
		synchronized ( pqAuditLogBeans ) {
			pqAuditLogBeans.add(auditLogBean);	
		}
	}
	
	public static void agentLogQueue(String strAgentDetails){
		
		PriorityBlockingQueue<String> pqAgentDetails = alAgentDetailsQueues.get( (int)Thread.currentThread().getId()%10 );
		synchronized ( pqAgentDetails ) {
			if( strAgentDetails != null ){
				pqAgentDetails.add(strAgentDetails);
			}
		}
	}
	
	/**
	 * this is to update the status of  node details table  
	 * where the status not received from the agent 
	 * @throws Throwable
	 */
	
	public void updateInActiveNodes(Connection con) throws Throwable {
		ResultSet rst = null;
		
		ArrayList<SUMTestBean> alSUMTestBeans = null;
		SUMDBI sumDBI = null;
		PreparedStatement pstmt = null;
		
		try {
	     	sumDBI = new SUMDBI();
	     	
	     	sumDBI.updateNodeStatus(con);
			
			rst = getNonActiveAgentLocations(con, pstmt);
			while (rst.next()) {
				if(Integer.parseInt(rst.getString(2)) == 0){
					alSUMTestBeans = SUMScheduler.drainSUMTest(rst.getString(1).toUpperCase());
					for(int i=0; i<alSUMTestBeans.size(); i++){
						insertSUMlog(alSUMTestBeans.get(i), null, rst.getString(1).toUpperCase(), "No agent is active. So drained the queue.");
					}
				}
			}
			
		}catch(Throwable t) {
			LogManager.errorLog(t);
			// throw t;

			// re-establish the connection if it is disconnected.
			// this will keep on waiting for the Connection to get established.
			con = DataBaseManager.reEstablishConnection(con);
		}finally {
			DataBaseManager.close(rst);
			rst = null;
			DataBaseManager.close(pstmt);
			pstmt = null;
		}
		
		
	}
	/**
	 * to get the inactive locations 
	 * @return
	 * @throws Throwable
	 */
	public ResultSet getNonActiveAgentLocations(Connection con, PreparedStatement pstmt) throws Throwable {
		
		ResultSet rst = null;
		
		String strQry = "select a.country||'-'||'-'||a.city as loc, SUM( CASE WHEN sum_node_status = 'active' THEN 1 ELSE 0 END ) AS active_count from sum_node_details  a group by a.country, a.city";
		
		try {
			pstmt = con.prepareStatement(strQry);
			rst = pstmt.executeQuery();
		} catch (Throwable t) {
			LogManager.infoLog("strQry : " + strQry);
			LogManager.errorLog(t);
			throw t;
		} finally {
			UtilsFactory.clearCollectionHieracy( strQry );
		}
		
		return rst;
	}
	
	/**
	 * 
	 * @throws Throwable
	 */
	public void sendLocationToSch(String strLocation) throws Throwable {
		
		BufferedReader reader = null;
		try {
			
			String UPLOAD_URL = null;
			
			// creates a HTTP connection
			URL url = new URL(UPLOAD_URL);
			HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
			httpConn.setUseCaches(false);
			httpConn.setDoOutput(true);
			httpConn.setRequestMethod("POST");
			// sets file name as a HTTP header
			httpConn.setRequestProperty("location", "India--Chennai");
			httpConn.connect();
	       
			// always check HTTP response code from server
			int responseCode = httpConn.getResponseCode();
			
			if (responseCode == HttpURLConnection.HTTP_OK) {
				// reads server's response
				reader = new BufferedReader(new InputStreamReader(httpConn.getInputStream()));
				String response = reader.readLine();
				//System.out.println("Server's response: " + response);
			} else {
				LogManager.infoLog("Server returned non-OK code: " + responseCode);
		    }
		 } catch (Throwable t) {
			 LogManager.errorLog(t);
			 throw t;
		 } finally {
			 if(reader!=null)
				 reader.close();
		 }
	}
	
	public SUMAuditLogBean insertSUMlog(SUMTestBean testBean, SUMNodeBean sumNodeBean, String strLocation, String strRemarks) throws Throwable {
		SUMAuditLogBean auditLogBean = null;
		
		try {
			auditLogBean = new SUMAuditLogBean();
			auditLogBean.setNodeId(sumNodeBean == null ? -1 : sumNodeBean.getNodeId());
			auditLogBean.setNodeUserId(sumNodeBean == null ? -1 : sumNodeBean.getUserId());
			auditLogBean.setAgentType(testBean.getAgentType());
			auditLogBean.setSumTestId(testBean.getTestId());
			auditLogBean.setSumTestName(testBean.getTestName());
			auditLogBean.setAppedoUserId(testBean.getUserId());
			auditLogBean.setExecutionTime(testBean.getRunEveryMinute());
			auditLogBean.setLocation(strLocation);
			auditLogBean.setLatitude(sumNodeBean == null ? -1 : sumNodeBean.getLatitude());
			auditLogBean.setLongitude(sumNodeBean == null ? -1 : sumNodeBean.getLongitude());
			auditLogBean.setIpAddress(sumNodeBean == null ? null : sumNodeBean.getIPAddresses());
			auditLogBean.setMacAddress(sumNodeBean == null ? null : sumNodeBean.getMacAddress());
			auditLogBean.setRemarks(strRemarks);
			auditLogBean.setCreatedOn(String.valueOf(new Date().getTime()));
			
			auditLogQueue(auditLogBean);
			
		} catch (Exception e) {
			LogManager.errorLog(e);
		}
		return auditLogBean;
	}
	
	public SUMNodeBean getNodeDetails(Connection con, String mac) throws Throwable {
		SUMNodeBean sumNodeBean = null;
		SUMDBI sumdbi = null;
		
		try {
			sumdbi = new SUMDBI();
			sumNodeBean = sumdbi.getNodeDetails(con, mac);
		} catch (Exception e) {
			LogManager.errorLog(e);
		}
		return sumNodeBean;
	}
}
