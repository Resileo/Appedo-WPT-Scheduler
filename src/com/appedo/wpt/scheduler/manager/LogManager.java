package com.appedo.wpt.scheduler.manager;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.appedo.wpt.scheduler.common.Constants;

public class LogManager {
	static Logger errorLog = null;
	static Logger infoLog = null;
	
	
	public static void initializePropertyConfigurator() {

		PropertyConfigurator.configure(Constants.LOG4J_PROPERTIES_FILE);
		errorLog = Logger.getLogger("errorLogger");
		infoLog = Logger.getLogger("infoLogger");
	}
	/**
	 * to log error's into error.log
	 * @param e
	 */
	public static void errorLog(Throwable th) {
		
		StringWriter stack = new StringWriter();
		PrintWriter pw = new PrintWriter(stack);
		
		try {
			th.printStackTrace(pw);
			errorLog.error(stack.toString());
		}finally {
			pw = null;
			stack = null;
		}
	}
	
	public static void infoLog(String strInfo) {
		infoLog.info(strInfo);
		
	}
	
	
}
