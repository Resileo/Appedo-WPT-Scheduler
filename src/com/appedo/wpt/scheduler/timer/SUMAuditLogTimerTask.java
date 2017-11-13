package com.appedo.wpt.scheduler.timer;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.TimerTask;
import java.util.concurrent.PriorityBlockingQueue;

import com.appedo.commons.connect.DataBaseManager;
import com.appedo.manager.LogManager;
import com.appedo.wpt.scheduler.bean.SUMAuditLogBean;
import com.appedo.wpt.scheduler.common.Constants;
import com.appedo.wpt.scheduler.dbi.SUMDBI;
import com.appedo.wpt.scheduler.manager.NodeManager;

public class SUMAuditLogTimerTask extends TimerTask{
	
	SUMAuditLogBean sumAuditLogBean = null;
	
	int nLogTimerIndex = 0;
	
	private Connection con = null;
	
	public SUMAuditLogTimerTask(int nLogTimerIndex) {
		try {
			this.nLogTimerIndex = nLogTimerIndex;
			DataBaseManager.doConnectionSetupIfRequired("", Constants.APPEDO_CONFIG_FILE_PATH, true);
			this.con = DataBaseManager.giveConnection();
		} catch (Exception e) {
			LogManager.errorLog(e);
		}
	}
	
	@Override
	public void run() {
		PriorityBlockingQueue<SUMAuditLogBean> pqAuditLogBeans = null;
		
		try {
			SUMDBI sumdbi = null;
			pqAuditLogBeans = NodeManager.alAuditLogQueues.get(nLogTimerIndex);
			
			if( pqAuditLogBeans != null ){
				sumdbi = new SUMDBI();
				while( ! pqAuditLogBeans.isEmpty() ){
					sumAuditLogBean = pqAuditLogBeans.poll();
					if( sumAuditLogBean != null ){
						sumdbi.insertSUMlog(con, sumAuditLogBean);
					}
				}
			}
		} catch (Throwable e) {
			LogManager.errorLog(e);
			// re-establish the connection if it is disconnected.
			// this will keep on waiting for the Connection to get established.
			try {
				if( ! DataBaseManager.isConnectionExists(con) ){
					pqAuditLogBeans.add(sumAuditLogBean);
					con = DataBaseManager.reEstablishConnection(con);
				}
			} catch (SQLException e1) {
				LogManager.errorLog(e);
			}
			
		}
	}
	
	@Override
	protected void finalize() throws Throwable {
		DataBaseManager.close(con);
		con = null;
		
		super.finalize();
	}
}
