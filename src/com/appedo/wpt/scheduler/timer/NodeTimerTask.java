package com.appedo.wpt.scheduler.timer;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.TimerTask;

import com.appedo.wpt.scheduler.connect.DataBaseManager;
import com.appedo.wpt.scheduler.manager.LogManager;
import com.appedo.wpt.scheduler.manager.NodeManager;

public class NodeTimerTask extends TimerTask {
	private Connection con = null;
	
	public NodeTimerTask(){
		this.con = DataBaseManager.giveConnection();
	}
	
	@Override
	public void run() {
		try{
			// System.out.println("Starting timer to update in active nodes: "+(new Date()));
			new NodeManager().updateInActiveNodes(con);
		} catch(Throwable e) {
			LogManager.errorLog(e);
			
			try {
				if( ! DataBaseManager.isConnectionExists(con) ){
					con = DataBaseManager.reEstablishConnection(con);
				}
			} catch (SQLException e1) {
				LogManager.errorLog(e);
			}
		}
	}
	
	@Override
	protected void finalize() throws Throwable {
		//System.out.println("Node inactiavting Thread is stopping");
		DataBaseManager.close(con);
		con = null;
		
		super.finalize();
	}

}
