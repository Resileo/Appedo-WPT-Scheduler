package com.appedo.wpt.scheduler.common;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import com.appedo.wpt.scheduler.manager.LogManager;
import com.appedo.wpt.scheduler.utils.UtilsFactory;

/**
 * This class holds the application level variables which required through the application.
 * 
 * @author navin
 *
 */
public class Constants {
	
	//public final static String CONFIGFILEPATH = InitServlet.realPath+"/WEB-INF/classes/com/softsmith/floodgates/resource/config.properties";
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
	public static String HAR_PATH;
	
	
	/**
	 * Loads constants properties 
	 * 
	 * @param srtConstantsPath
	 */
	public static void loadConstantsProperties(String srtConstantsPath) throws Exception {
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


	public static void loadAppedoConfigProperties(String strAppedoConfigPath) throws Exception {
    	Properties prop = new Properties();
    	InputStream is = null;
    	
        try {
    		is = new FileInputStream(strAppedoConfigPath);
    		prop.load(is);
     		
     		// Url to delete har files from har repository
    		EXPORT_URL = prop.getProperty("URL_TO_EXPORT_HAR");	
     		
     		// Webservice collector
        } catch(Exception e) {
        	LogManager.errorLog(e);
        	throw e;
        } finally {
        	UtilsFactory.close(is);
        	is = null;
        }	
	}
	
}