package com.appedo.wpt.scheduler.listener;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.appedo.wpt.scheduler.servlet.InitServlet;

/**
 * This class will handle the operations required to be done when the application is started or stopped in Tomcat.
 * 
 * @author Ramkumar R
 *
 */
public class AppedoSUMSchedulerServletContext implements ServletContextListener	{

	private ServletContext context = null;
	
	/**
	 * This method is invoked when the Web Application
	 * is ready to service requests
	 */
	public void contextInitialized(ServletContextEvent event) {
		this.context = event.getServletContext();
		
		//Output a simple message to the server's console
		System.out.println("The SSProfiler Web App is Ready in "+context.getServerInfo());
	}
	
	/**
	 * Stop all the timers when this project is stopped or tomcat is stopped
	 */
	public void contextDestroyed(ServletContextEvent event) {
		
		/**
		 * TODO cancel, purge the Application,Server & Database Timer & TimerTask
		try{
			InitServlet.ttApplication.cancel();
			InitServlet.timerMYSQLWriter.cancel();
			InitServlet.timerMYSQLWriter.purge();
		} catch (Exception e) {
			System.out.println("Exception in MySQL timer stop: "+e.getMessage());
		}
		
		try{
			InitServlet.ttTomcat.cancel();
			InitServlet.timerTomcatWriter.cancel();
			InitServlet.timerTomcatWriter.purge();
		} catch (Exception e) {
			System.out.println("Exception in Tomcat timer stop: "+e.getMessage());
		}
		
		try{
			InitServlet.ttLinux.cancel();
			InitServlet.timerLinuxWriter.cancel();
			InitServlet.timerLinuxWriter.purge();
		} catch (Exception e) {
			System.out.println("Exception in Linux timer stop: "+e.getMessage());
		}
		*/
		
		try{
			InitServlet.timerTaskNodeInactive.cancel();
			InitServlet.timerNodeInactive.cancel();
			InitServlet.timerNodeInactive.purge();
		} catch (Exception e) {
			System.out.println("Exception in Java timer stop: "+e.getMessage());
		}
		
		try{
			InitServlet.timerTaskSUMScheduler.cancel();
			InitServlet.timerSUMScheduler.cancel();
			InitServlet.timerSUMScheduler.purge();
		} catch (Exception e) {
			System.out.println("Exception in .Net timer stop: "+e.getMessage());
		}
		
		System.out.println("The Appedo Collector Web App has Been Removed.");
		this.context = null;
	}
}
