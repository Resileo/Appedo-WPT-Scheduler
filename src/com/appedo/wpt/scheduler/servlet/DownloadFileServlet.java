package com.appedo.wpt.scheduler.servlet;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.appedo.manager.LogManager;
import com.appedo.wpt.scheduler.bean.SUMAuditLogBean;
import com.appedo.wpt.scheduler.bean.SUMTestBean;
import com.appedo.wpt.scheduler.common.Constants;
import com.appedo.wpt.scheduler.connect.CryptManager;
import com.appedo.wpt.scheduler.manager.SUMManager;

/**
 * Servlet implementation class DownloadFileServlet
 */
//@WebServlet("/DownloadFileServlet")
public class DownloadFileServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	static final int BUFFER_SIZE = 4096;
	StringBuilder sb=new StringBuilder();
	FileInputStream inputStream = null;
	OutputStream outputStream = null;
	BufferedReader reader = null;
         
    /**
     * @see HttpServlet#HttpServlet()
     */
    public DownloadFileServlet() {
        super();
        // TODO Auto-generated constructor stub
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
		String strCommand = request.getHeader("command");
		if(strCommand != null && strCommand.trim().equalsIgnoreCase("UPLOAD")) {
			try {
				doImport(request,response);
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}else {
			doAction(request, response);
		}
	}
	
	protected void doAction(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try{
			// TODO Hardcoded browser and connection details. Need to remove this. 
			//String strLocation = (request.getParameter("country")+"-"+request.getParameter("state")+"-"+request.getParameter("city")).toUpperCase();
			String strLocation = (request.getParameter("country")+"-"+"-"+request.getParameter("city"))+":CHROME.Native";
			String mac = request.getParameter("mac");
			//String userId = CryptManager.decodeDecryptURL(request.getParameter("uid"));
			Object[] oaReturn = (new SUMManager()).getSUMTestForLocation(strLocation, mac);
			SUMTestBean sumTestBean = (SUMTestBean)oaReturn[0];
			// long lLogId = 0;
			String logId = "0";
			 if(oaReturn[1] != null){
				 logId = ((SUMAuditLogBean)oaReturn[1]).getCreatedOn();
			 }
			// no Test is available in queue. So ask agent to wait for some more time.
			if( sumTestBean == null ){
				response.setHeader("test_id", "-1" );
				 response.setHeader("log_id", "0");
			} else {
		        // obtains ServletContext
		        ServletContext context = getServletContext();
		        response.setHeader("test_id", Long.toString(sumTestBean.getTestId()) );
		        response.setHeader("url", sumTestBean.getURL());
		        response.setHeader("test_type", sumTestBean.getTestType());
		        response.setHeader("log_id", logId);
		        
		        if( sumTestBean.getTestType().toUpperCase().equals("TRANSACTION") ){
		        	
			        response.setHeader("class_name", sumTestBean.getTestClassName());
			        
					// reads input file from an absolute path
					String filePath = Constants.SELENIUM_SCRIPT_CLASS_FILE_PATH;
			        File downloadFile = new File( filePath+File.separator+sumTestBean.getTestClassName()+".class" );
//			        System.out.println("downloadFile: "+downloadFile.getAbsolutePath());
			        FileInputStream inStream = new FileInputStream(downloadFile);
			        // gets MIME type of the file
			        String mimeType = context.getMimeType(filePath);
			        if (mimeType == null) {
			            // set to binary type if MIME mapping not found
			            mimeType = "application/octet-stream";
			        }
			        
			        // modifies response
			        response.setContentType(mimeType);
			        response.setContentLength((int) downloadFile.length());
			        
			        // obtains response's output stream
			        OutputStream outStream = response.getOutputStream();
			        
			        byte[] buffer = new byte[4096];
			        int bytesRead = -1;
			        while ((bytesRead = inStream.read(buffer)) != -1) {
			            outStream.write(buffer, 0, bytesRead);
			        }
			       inStream.close();
			       outStream.close();
		        }
		    }
		} catch( Exception ex ){
			LogManager.errorLog(ex);
		}
	}
	
	public void doImport(HttpServletRequest request, HttpServletResponse response) throws Throwable {
		
		try {
			
			// Get class file name
			String strClaassFile = request.getHeader("class-name");
			
				File saveFile = new File(Constants.SELENIUM_SCRIPT_CLASS_FILE_PATH+strClaassFile);
				InputStream inputStream = request.getInputStream();
				// opens an output stream for writing file
				FileOutputStream outputStream = new FileOutputStream(saveFile);
				
				byte[] buffer = new byte[BUFFER_SIZE];
				int bytesRead = -1;
//				System.out.println("Receiving data...");
				while ((bytesRead = inputStream.read(buffer)) != -1) {
					outputStream.write(buffer, 0, bytesRead);
				}
				outputStream.close();
				inputStream.close();
				LogManager.infoLog("File written to: " + saveFile.getAbsolutePath());
				// sends response to client
				response.getWriter().print("success");
			
		}catch(Throwable t) {
			LogManager.errorLog(t);
			throw t;
		}
	}
}