package com.appedo.wpt.scheduler.common;

import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.util.Properties;

import com.appedo.manager.AppedoConstants;
import com.appedo.manager.LogManager;
import com.appedo.wpt.scheduler.utils.UtilsFactory;

/**
 * This class holds the application level variables which required through the application.
 * 
 * @author navin
 *
 */
public class Constants {
	
	public static boolean DEV_ENVIRONMENT = false;
	
	public static String CONSTANTS_FILE_PATH = "";
	
	public static String RESOURCE_PATH = "";
	
	public static String APPEDO_CONFIG_FILE_PATH = "";
	
	public static String PATH = ""; 
	public static int INTERVAL;
	public static long TIMER_PERIOD, SCHEDULE_INTERVAL;
//	public static String LOCATION_EXPORT_URL; 
	public static String SELENIUM_SCRIPT_CLASS_FILE_PATH = "";
	
	//log4j properties file path
	public static String LOG4J_PROPERTIES_FILE = "";
	public static String EXPORT_URL;
	public static String HAR_PATH, APPEDO_SLA_COLLECTOR, WPT_LOCATION_SERVER;
	
	
	/**
	 * Loads constants properties 
	 * 
	 * @param srtConstantsPath
	 */
	public static void loadConstantsProperties(String srtConstantsPath) throws Throwable {
		Properties prop = new Properties();
		InputStream is = null;
		
		try {
			is = new FileInputStream(srtConstantsPath);
			prop.load(is);
			
			// Appedo application's resource directory path
			RESOURCE_PATH = prop.getProperty("RESOURCE_PATH");
			
			APPEDO_CONFIG_FILE_PATH = RESOURCE_PATH+prop.getProperty("APPEDO_CONFIG_FILE_PATH");
			
			PATH = RESOURCE_PATH+prop.getProperty("Path");
			
			INTERVAL = Integer.parseInt(prop.getProperty("queryinterval"));
			
			TIMER_PERIOD = Long.parseLong(prop.getProperty("node_status__check_period_in_sec"));
			
			SCHEDULE_INTERVAL = Long.parseLong(prop.getProperty("scheduleinterval"));
			
			// LOCATION_EXPORT_URL = prop.getProperty("location_export_url");
			
			Constants.SELENIUM_SCRIPT_CLASS_FILE_PATH = Constants.RESOURCE_PATH+prop.getProperty("sumtransactionfilepath");
			HAR_PATH = Constants.RESOURCE_PATH+prop.getProperty("harfilepath");
			
			LOG4J_PROPERTIES_FILE = RESOURCE_PATH+prop.getProperty("LOG4J_CONFIG_FILE_PATH");
		} catch(Throwable th) {
			System.out.println("Exception in loadConstantsProperties: "+th.getMessage());
			th.printStackTrace();
			LogManager.errorLog(th);
			
			throw th;
		} finally {
			UtilsFactory.close(is);
			is = null;
		}	
	}


	public static void loadAppedoConfigProperties(String strAppedoConfigPath) throws Throwable {
		Properties prop = new Properties();
		InputStream is = null;
		
		try {
			is = new FileInputStream(strAppedoConfigPath);
			prop.load(is);
			
			if( prop.getProperty("ENVIRONMENT") != null && prop.getProperty("ENVIRONMENT").equals("DEVELOPMENT") 
					&& AppedoConstants.getAppedoConfigProperty("ENVIRONMENT") != null && AppedoConstants.getAppedoConfigProperty("ENVIRONMENT").equals("DEVELOPMENT") ) 
			{
				DEV_ENVIRONMENT = true;
			}
			
			// Url to delete har files from har repository
			EXPORT_URL = getProperty("URL_TO_EXPORT_HAR", prop);
			APPEDO_SLA_COLLECTOR = getProperty("APPEDO_SLA_COLLECTOR", prop);
			WPT_LOCATION_SERVER = getProperty("WPT_LOCATION_SERVER", prop);
			
		} catch(Exception e) {
			LogManager.errorLog(e);
			throw e;
		} finally {
			UtilsFactory.close(is);
			is = null;
		}	
	}
	
	/**
	 * loads AppedoConstants, 
	 * of loads Appedo whitelabels, replacement of word `Appedo` as configured in DB
	 * 
	 * @param strAppedoConfigPath
	 * @throws Exception
	 */
	public static void loadAppedoConstants(Connection con) throws Throwable {
		
		try {
			AppedoConstants.getAppedoConstants().loadAppedoConstants(con);
		} catch (Throwable th) {
			throw th;
		}
	}
	
	/**
	 * Get the property's value from appedo_config.properties, if it is DEV environment;
	 * Otherwise get the property's value from DB.
	 * 
	 * @param strPropertyName
	 * @param prop
	 * @return
	 */
	private static String getProperty(String strPropertyName, Properties prop) throws Throwable {
		if( DEV_ENVIRONMENT && prop.getProperty(strPropertyName) != null )
			return prop.getProperty(strPropertyName);
		else
			return AppedoConstants.getAppedoConfigProperty(strPropertyName);
	}
}