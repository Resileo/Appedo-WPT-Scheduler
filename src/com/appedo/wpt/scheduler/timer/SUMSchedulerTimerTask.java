package com.appedo.wpt.scheduler.timer;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.TimerTask;

import com.appedo.commons.connect.DataBaseManager;
import com.appedo.manager.LogManager;
import com.appedo.wpt.scheduler.bean.SUMTestBean;
import com.appedo.wpt.scheduler.dbi.SUMDBI;
import com.appedo.wpt.scheduler.manager.SUMManager;

public class SUMSchedulerTimerTask extends TimerTask{
	
	Connection con = null;

	public SUMSchedulerTimerTask(){
		this.con = DataBaseManager.giveConnection();
	}
	
	@Override
	public void run() {
		
		/*
		 * Before self killing Thread, reference should be cleared.
		 * 
		 */
		SUMManager.hmThreads.clear();
		SUMManager manager = new SUMManager();
		SUMDBI sumdbi = new SUMDBI();
		ArrayList<SUMTestBean> alSUMTestBeans = null;
		
		while(alSUMTestBeans == null){
			try {
				con = DataBaseManager.reEstablishConnection(con);
				alSUMTestBeans = sumdbi.getTestIdDetails(con);
			} catch (Throwable e) {
				LogManager.errorLog(e);
				if( ! ( e.getMessage().toLowerCase().contains("connection") && e.getMessage().toLowerCase().contains("closed") ) ) {
					break;
				}
			}
		}
		
		if( alSUMTestBeans != null ) {
			for(int i=0; i<alSUMTestBeans.size(); i++){
				try {
					manager.runSUMTests(alSUMTestBeans.get(i));
				} catch (Throwable e1) {
					LogManager.errorLog(e1);
				}
			}
		}
	}

	@Override
	protected void finalize() throws Throwable {
		LogManager.infoLog("Node inactiavting Thread is stopping");
		DataBaseManager.close(con);
		con = null;
		super.finalize();
	}
}
