package com.appedo.wpt.scheduler.timer;

import java.sql.Connection;
import java.util.TimerTask;

import com.appedo.manager.LogManager;
import com.appedo.wpt.scheduler.connect.DataBaseManager;
import com.appedo.wpt.scheduler.dbi.SUMDBI;

public class ResetMeasurementTimerTask extends TimerTask{
	
	Connection con = null;

	public ResetMeasurementTimerTask(){
		this.con = DataBaseManager.giveConnection();
	}
	
	@Override
	public void run() {
		SUMDBI sumdbi = null;
		try {
			if( ! DataBaseManager.isConnectionExists(con) ){
				con = DataBaseManager.reEstablishConnection(con);
			}
			sumdbi = new SUMDBI();
			sumdbi.resetMeasurements(con);
		} catch (Exception e) {
			LogManager.errorLog(e);
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
