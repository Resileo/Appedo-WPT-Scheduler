package com.appedo.wpt.scheduler.timer;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.TimerTask;
import java.util.concurrent.PriorityBlockingQueue;

import com.appedo.manager.LogManager;
import com.appedo.wpt.scheduler.common.Constants;
import com.appedo.wpt.scheduler.connect.DataBaseManager;
import com.appedo.wpt.scheduler.manager.NodeManager;

public class AgentLogTimerTask extends TimerTask{
	
	// Index for this task object
	int nAgentTimerIndex = 0;
	String strAgentDetails = null;
	
	private Connection con = null;
	
	public AgentLogTimerTask(int nLogTimerIndex) throws Exception {
		try {
			this.nAgentTimerIndex = nLogTimerIndex;
			
			// Initialize Database connection
			DataBaseManager.doConnectionSetupIfRequired(Constants.APPEDO_CONFIG_FILE_PATH);
			this.con = DataBaseManager.giveConnection();
		} catch (Exception e) {
			LogManager.errorLog(e);
			throw e;
		}
	}
	
	@Override
	public void run() {
		Statement stmt = null;
		PriorityBlockingQueue<String> pqAgentDetails = null;
		
		try {
			pqAgentDetails = NodeManager.alAgentDetailsQueues.get(nAgentTimerIndex);
			while( !pqAgentDetails.isEmpty() ){
				strAgentDetails = pqAgentDetails.poll();
				if( strAgentDetails != null ){
					stmt = con.createStatement();
					stmt.executeUpdate(strAgentDetails);
				}
			}
		} catch (Throwable th) {
			LogManager.errorLog(th);
			
			// re-establish the connection if it is disconnected.
			// this will keep on waiting for the Connection to get established.
			try {
				if( ! DataBaseManager.isConnectionExists(con) ){
					pqAgentDetails.add(strAgentDetails);
					con = DataBaseManager.reEstablishConnection(con);
				}
			} catch (SQLException e) {
				LogManager.errorLog(e);
			}
			
		} finally {
			DataBaseManager.close(stmt);
			stmt = null;
		}
	}
	
	@Override
	protected void finalize() throws Throwable {
		DataBaseManager.close(con);
		con = null;
		
		super.finalize();
	}
}
