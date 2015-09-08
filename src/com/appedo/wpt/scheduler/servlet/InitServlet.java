package com.appedo.wpt.scheduler.servlet;

import java.io.IOException;
import java.sql.Connection;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.appedo.manager.LogManager;
import com.appedo.wpt.scheduler.common.Constants;
import com.appedo.wpt.scheduler.connect.DataBaseManager;
import com.appedo.wpt.scheduler.manager.NodeManager;
import com.appedo.wpt.scheduler.timer.AgentLogTimerTask;
import com.appedo.wpt.scheduler.timer.ResetMeasurementTimerTask;
import com.appedo.wpt.scheduler.timer.SUMAuditLogTimerTask;
import com.appedo.wpt.scheduler.timer.SUMSchedulerTimerTask;

/**
 * Servlet to handle one operation for the whole application
 * @author navin
 *
 */
public class InitServlet extends HttpServlet {
	// set log access
	
	private static final long serialVersionUID = 1L;
	public static String realPath = null;
	public static TimerTask timerTaskNodeInactive = null, timerTaskSUMScheduler = null, resetMeasurementCount = null;
	public static Timer timerNodeInactive = new Timer(), timerSUMScheduler = new Timer(), timerAuditLog = new Timer(), timerAgentDetails = new Timer(), timerMeasurementCount = new Timer();
	
	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public void init() {
		//super();
		
		// declare servlet context
		ServletContext context = getServletContext();
		
		realPath = context.getRealPath("//");
		
		Connection con = null;
		
		try {
			String strConstantsFilePath = context.getInitParameter("CONSTANTS_PROPERTIES_FILE_PATH");
			String strLog4jFilePath = context.getInitParameter("LOG4J_PROPERTIES_FILE_PATH");
			Constants.CONSTANTS_FILE_PATH = InitServlet.realPath+strConstantsFilePath;
			Constants.LOG4J_PROPERTIES_FILE = InitServlet.realPath+strLog4jFilePath;
			// Loads log4j configuration properties
			LogManager.initializePropertyConfigurator(Constants.LOG4J_PROPERTIES_FILE);
			// Loads Constant properties 
			Constants.loadConstantsProperties(Constants.CONSTANTS_FILE_PATH);
			
			Constants.loadAppedoConfigProperties(Constants.APPEDO_CONFIG_FILE_PATH);
			
			// Loads db config
			DataBaseManager.doConnectionSetupIfRequired(Constants.APPEDO_CONFIG_FILE_PATH);
			
//			timerTaskNodeInactive = new NodeTimerTask();
//			timerNodeInactive.schedule(timerTaskNodeInactive, 500, Constants.TIMER_PERIOD * 1000);
        	
			timerTaskSUMScheduler = new SUMSchedulerTimerTask();
			timerSUMScheduler.schedule(timerTaskSUMScheduler, 150, Constants.SCHEDULE_INTERVAL);
			
			// SUM Execution Audit log queues
			NodeManager.createSUMAuditLogQueues();
			
			// SUM Execution Audit log inserting Threads
			for(int nLogTimerIndex=0; nLogTimerIndex<10; nLogTimerIndex++){
				timerAuditLog.schedule(new SUMAuditLogTimerTask(nLogTimerIndex), 500, 1000);
			}
			
			// SUM  Agent Details queues
			NodeManager.createAgentDetailsQueues();
			
			// SUM Agent Details update Threads
			for(int nAgentTimerIndex = 0; nAgentTimerIndex < 10; nAgentTimerIndex ++){
				timerAgentDetails.schedule(new AgentLogTimerTask(nAgentTimerIndex), 500, 1000);
			}
			
			resetMeasurementCount = new ResetMeasurementTimerTask();
			timerMeasurementCount.schedule(resetMeasurementCount, getNext1200AM(), 1000*60*60*24);
			
		} catch(Throwable th) {
        	System.out.println("Exception in InitServlet.init: "+th.getMessage());
        	th.printStackTrace();
        	
			LogManager.errorLog(th);
		} finally {
			DataBaseManager.close(con);
			con = null;
		}
	}
	
	private static Date getNext1200AM(){
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		calendar.add(Calendar.DATE, 1);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		LogManager.infoLog("TIME::: "+calendar.getTime());
		return calendar.getTime();
      }
	
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
