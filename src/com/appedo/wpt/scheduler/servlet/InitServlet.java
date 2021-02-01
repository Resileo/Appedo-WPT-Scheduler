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

import com.appedo.commons.connect.DataBaseManager;
import com.appedo.manager.LogManager;
import com.appedo.wpt.scheduler.common.Constants;
import com.appedo.wpt.scheduler.timer.ResetMeasurementTimerTask;
import com.appedo.wpt.scheduler.timer.SUMSchedulerTimerTask;
import com.appedo.wpt.scheduler.timer.ScheduledLocationTracker;

/**
 * Servlet to handle one operation for the whole application
 * @author navin
 *
 */
public class InitServlet extends HttpServlet {
	// set log access
	
	private static final long serialVersionUID = 1L;
	public static String realPath = null;
	public static TimerTask timerTaskNodeInactive = null, timerTaskNodeactive = null,timerTaskSUMScheduler = null, resetMeasurementCount = null;
	public static Timer timerNodeInactive = new Timer(), timerNodeActive = new Timer(),timerSUMScheduler = new Timer(), timerAuditLog = new Timer(), timerAgentDetails = new Timer(), timerMeasurementCount = new Timer();
	
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
			
			// Loads log4j configuration properties
			Constants.LOG4J_PROPERTIES_FILE = InitServlet.realPath+strLog4jFilePath;
			LogManager.initializePropertyConfigurator(Constants.LOG4J_PROPERTIES_FILE);
			
			// Loads Constant properties 
			Constants.loadConstantsProperties(Constants.CONSTANTS_FILE_PATH);
			
			// Loads db config
			DataBaseManager.doConnectionSetupIfRequired("Appedo-WPT-Scheduler", Constants.APPEDO_CONFIG_FILE_PATH, true);
			
			con = DataBaseManager.giveConnection();
			
			// loads Appedo constants: WhiteLabels, Config-Properties
			Constants.loadAppedoConstants(con);
			
			// Loads Appedo config properties from DB (or) the system path
			Constants.loadAppedoConfigProperties(Constants.APPEDO_CONFIG_FILE_PATH);
			
			
//			timerTaskNodeInactive = new NodeTimerTask();
//			timerNodeInactive.schedule(timerTaskNodeInactive, 500, Constants.TIMER_PERIOD * 1000);
			
			timerTaskNodeactive = new ScheduledLocationTracker();
			timerNodeActive.schedule(timerTaskNodeactive, 300, 1000 * 60 * 3);
			
			timerTaskSUMScheduler = new SUMSchedulerTimerTask();
			timerSUMScheduler.schedule(timerTaskSUMScheduler, 150, Constants.SCHEDULE_INTERVAL);
			
			// SUM Execution Audit log queues
			//NodeManager.createSUMAuditLogQueues();
			
			// SUM Execution Audit log inserting Threads
			//for(int nLogTimerIndex=0; nLogTimerIndex<10; nLogTimerIndex++){
			//	timerAuditLog.schedule(new SUMAuditLogTimerTask(nLogTimerIndex), 500, 1000);
			//}
			
			// SUM  Agent Details queues
			//NodeManager.createAgentDetailsQueues();
			
			// SUM Agent Details update Threads
			/*for(int nAgentTimerIndex = 0; nAgentTimerIndex < 10; nAgentTimerIndex ++){
				timerAgentDetails.schedule(new AgentLogTimerTask(nAgentTimerIndex), 500, 1000);
			}*/
			
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
		doAction(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doAction(request, response);
	}
	
	/**
	 * Accessed in both GET and POSTrequests for the operations below, 
	 * 1. Loads agents latest build version
	 * 
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	public void doAction(HttpServletRequest request, HttpServletResponse response) throws ServletException,IOException {
		Connection con = null;
		
		response.setContentType("text/html");
		String strActionCommand = request.getRequestURI();
		
		if(strActionCommand.endsWith("/init/reloadConfigProperties")) {
			// to reload config and appedo_config properties 
			
			try {
				// Loads Constant properties
				Constants.loadConstantsProperties(Constants.CONSTANTS_FILE_PATH);
				
				con = DataBaseManager.giveConnection();
				
				// loads Appedo constants; say loads appedoWhiteLabels, 
				Constants.loadAppedoConstants(con);
				
				// Loads Appedo config properties from the system path
				Constants.loadAppedoConfigProperties(Constants.APPEDO_CONFIG_FILE_PATH);
				
				response.getWriter().write("Loaded <B>Appedo-WPT-Scheduler</B>, config, appedo_config & appedo whitelabels.");
			} catch (Throwable th) {
				LogManager.errorLog(th);
				response.getWriter().write("<B style=\"color: red; \">Exception occurred Appedo-WPT-Scheduler: "+th.getMessage()+"</B>");
			} finally {
				DataBaseManager.close(con);
				con = null;
			}
		}
	}
}
