package com.appedo.wpt.scheduler.sum;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;

import net.sf.json.JSONObject;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;

import com.appedo.wpt.scheduler.bean.SUMTestBean;
import com.appedo.wpt.scheduler.common.Constants;
import com.appedo.wpt.scheduler.manager.LogManager;
import com.appedo.wpt.scheduler.manager.SUMManager;

/**
 * Runs the Test by creating a new Thread everytime for given URL.
 * 
 * @author Ameen
 * 
 */
public class RunTest extends Thread {
	
	
	String strLocation;
	public SUMTestBean testBean;
	Date expireDate;
	HttpClient client = null;
	PostMethod method = null;
	
	/**
	 * Starts Test in scheduled time for the given test_id and selecting the
	 * available nodes using the cluster_id
	 * 
	 * @param clusterId
	 * @param id
	 * @param intDuration
	 * @param strUrl
	 * @throws Throwable
	 */
	public RunTest(String strLocation, SUMTestBean testBean, Date date) throws Throwable {

		try {
			this.expireDate = date;
			this.testBean = testBean;
			this.strLocation = strLocation;
			
			// setDefaultFirefoxPreferences(id);
		} catch (Exception e) {
			LogManager.errorLog(e);
		}

	}

	/**
	 * Starts the test in scheduled time for the given test_id
	 */
	public void run() {
		SUMManager sumManager = null;
		try {
			sumManager = new SUMManager();
			// Pushing test to 70.70 server, get response 
			client = new HttpClient();
			// URLEncoder.encode(requestUrl,"UTF-8");
			method = new PostMethod("http://54.237.70.70/runtest.php");
			method.addParameter("url", testBean.getURL());
			method.addParameter("label", testBean.getTestName());
			method.addParameter("location", strLocation);
			// method.addParameter("runs", ""+testBean.getRunEveryMinute()); -should be discussed
			method.addParameter("runs", "1");
			method.addParameter("fvonly", "0");
			method.addParameter("domelement", "null");
			method.addParameter("private", "0");
			method.addParameter("connections", "0");
			method.addParameter("web10", "0");
			method.addParameter("block", "null");
			method.addParameter("login", "null");
			method.addParameter("password", "null");
			method.addParameter("authType", "0");
			method.addParameter("video", "0");
			method.addParameter("f", "json");
			method.addParameter("r", "null");
			method.addParameter("notify", "null");
			method.addParameter("pingback", "null");
			method.addParameter("bwDown", "null");
			method.addParameter("bwUp", "null");
			method.addParameter("latency", "null");
			method.addParameter("plr", "null");
			// method.addParameter("k", "A.8d4413bd13dc1798f286c896d25be969");
			method.addParameter("k", "33f6b472561edfcf6130b2a65b687104f9ed5d62");
			method.addParameter("tcpdump", "0");
			method.addParameter("noopt", "0");
			method.addParameter("noimages", "0");
			method.addParameter("noheaders", "0");
			method.addParameter("pngss", "0");
			method.addParameter("iq", "30"); /*30-100*/
			method.addParameter("noscript", "0");
			method.addParameter("clearcerts", "0");
			method.addParameter("mobile", "0");
			method.addParameter("mv", "0");
			method.addParameter("htmlbody", "0");
			method.addParameter("tsview_id", "null");
			method.addParameter("custom", "null");
			method.addParameter("tester", "null");
			method.addParameter("affinity", "null");
			method.addParameter("timeline", "0");
			method.addParameter("timelineStack", "0");
			method.addParameter("ignoreSSL", "0");
			
			method.setRequestHeader("Connection", "close");
			int statusCode = client.executeMethod(method);
			LogManager.infoLog("statusCode: "+statusCode);
			
			String responseStream = method.getResponseBodyAsString();
			// String responseStream = "{\"statusCode\":200,\"statusText\":\"Ok\",\"data\":{\"testId\":\"150813_FE_e5fd27139876bc8ed69ed3628ced3280\",\"ownerKey\":\"e9028f67d5a3dba9019b214267a555d422e43d1b\",\"jsonUrl\":\"http://www.webpagetest.org/jsonResult.php?test=150813_FE_e5fd27139876bc8ed69ed3628ced3280\",\"xmlUrl\":\"http://www.webpagetest.org/xmlResult/150813_FE_e5fd27139876bc8ed69ed3628ced3280/\",\"userUrl\":\"http://www.webpagetest.org/result/150813_FE_e5fd27139876bc8ed69ed3628ced3280/\",\"summaryCSV\":\"http://www.webpagetest.org/result/150813_FE_e5fd27139876bc8ed69ed3628ced3280/page_data.csv\",\"detailCSV\":\"http://www.webpagetest.org/result/150813_FE_e5fd27139876bc8ed69ed3628ced3280/requests.csv\"}}";
			System.out.println("RESPONSE:: "+responseStream);
			
			String runTestCode = null;
			if( responseStream.trim().startsWith("{") && responseStream.trim().endsWith("}")) {
				JSONObject joResponse = JSONObject.fromObject(responseStream);
				if(joResponse.containsKey("data")){
					JSONObject joData = JSONObject.fromObject(joResponse.get("data"));
					runTestCode = joData.getString("testId");
				}
				
				
				// preparation of sum_har_results table
				sumManager.insertHarTable(testBean.getTestId(), joResponse.getInt("statusCode"), joResponse.getString("statusText"), runTestCode, testBean.getLocation());
				sumManager.updateSumTestLastRunDetail(testBean.getTestId());
				
				if( runTestCode != null){
					int statusCheckStatus = 0;
					while(statusCheckStatus != 200){
						client = new HttpClient();
						// URLEncoder.encode(requestUrl,"UTF-8");
						method = new PostMethod("http://54.237.70.70/testStatus.php");
						method.addParameter("f", "json");
						method.addParameter("test", runTestCode);
						method.setRequestHeader("Connection", "close");
						statusCode = client.executeMethod(method);
						
						// responseStream = "{\"statusCode\":200,\"statusText\":\"Test Complete\",\"data\":{\"statusCode\":200,\"statusText\":\"Test Complete\",\"id\":\"150813_FE_e5fd27139876bc8ed69ed3628ced3280\",\"testInfo\":{\"url\":\"http://apm.appedo.com\",\"runs\":1,\"fvonly\":0,\"web10\":0,\"ignoreSSL\":0,\"video\":\"0\",\"label\":\"TEST\",\"priority\":5,\"block\":\"null\",\"location\":\"Dulles\",\"browser\":\"Chrome\",\"connectivity\":\"Cable\",\"bwIn\":5000,\"bwOut\":1000,\"latency\":28,\"plr\":\"0\",\"tcpdump\":0,\"timeline\":0,\"trace\":0,\"bodies\":0,\"netlog\":0,\"standards\":0,\"noscript\":0,\"pngss\":0,\"iq\":50,\"keepua\":0,\"mobile\":0,\"tsview_id\":\"null\",\"scripted\":0},\"testId\":\"150813_FE_e5fd27139876bc8ed69ed3628ced3280\",\"runs\":1,\"fvonly\":0,\"remote\":false,\"testsExpected\":1,\"location\":\"Dulles\",\"startTime\":\"08/13/15 8:36:57\",\"elapsed\":32,\"completeTime\":\"08/13/15 8:37:29\",\"testsCompleted\":1,\"fvRunsCompleted\":1,\"rvRunsCompleted\":1}}";
						responseStream = method.getResponseBodyAsString();
						if( responseStream.trim().startsWith("{") && responseStream.trim().endsWith("}")) {
							joResponse = JSONObject.fromObject(responseStream);
							statusCheckStatus = joResponse.getInt("statusCode");
							sumManager.updateHarTable(testBean.getTestId(), joResponse.getInt("statusCode"), joResponse.getString("statusText"), runTestCode, 0, 0);
						}
					}
					
					if( statusCheckStatus == 200 ){
						client = new HttpClient();
						// URLEncoder.encode(requestUrl,"UTF-8");
						method = new PostMethod("http://54.237.70.70/jsonResult.php");
						method.addParameter("test", runTestCode);
						method.setRequestHeader("Connection", "close");
						statusCode = client.executeMethod(method);
						responseStream = method.getResponseBodyAsString();
						if( responseStream.trim().startsWith("{") && responseStream.trim().endsWith("}")) {
							joResponse = JSONObject.fromObject(responseStream);
							if(joResponse.containsKey("data")){
								JSONObject joData = JSONObject.fromObject(joResponse.get("data"));
								if( joData.containsKey("average") ){
									JSONObject joAverage = JSONObject.fromObject(joData.get("average"));
									JSONObject joFirstView = JSONObject.fromObject(joAverage.get("firstView"));
									JSONObject joRepeatView = JSONObject.fromObject(joAverage.get("repeatView"));
									sumManager.updateHarTable(testBean.getTestId(), joResponse.getInt("statusCode"), joResponse.getString("statusText"), runTestCode, joFirstView.getInt("loadTime"), joRepeatView.getInt("loadTime") );
								}
							}
						}
						String fileURL = "http://54.237.70.70/export.php?bodies=1&pretty=1&test="+runTestCode;
						String saveDir = Constants.HAR_PATH+testBean.getTestId();
						HttpDownloadUtility.downloadFile(fileURL, saveDir);
						
						try {
							File file = new File(saveDir);
							for(int i=0;i<file.listFiles().length;i++){
								File f = file.listFiles()[i];
								sumManager.updateHarFileNameInTable(testBean.getTestId(), runTestCode, f.getName());
								JSONObject jo = exportHarFile(saveDir+"/"+f.getName(), f.getName(), ""+testBean.getTestId());
								if(jo.getBoolean("success")){
									deleteHar(saveDir+"/"+f.getName());
								}
							}
						} catch (Throwable e) {
							LogManager.errorLog(e);
						}
					}
					
				}
				
			}	
			
			if (statusCode != HttpStatus.SC_OK) {
				LogManager.infoLog("Method failed: " + method.getStatusLine());
			}
			
			// insert data into sum_har_result_table using get response value -- Done
			// Loop check status and upadate sum_har_result_table for that run_test_code -- Done
			// Once status is completed, then get har if result code is either 0 or 99999 (For other result code we need to check the existence of the har) -- Getting value/Unable to get response val
			// Push har file to har repository as it is done by sum agent before
			// update sum_har_result table with this filename and location
			// update loadTime (get using xmlResult API) -- Done
			
			
		} catch (Exception ee) {
			LogManager.errorLog(ee);
		} finally {
			LogManager.infoLog("Test Status: "+testBean.isStatus()+" Test ID: "+testBean.getTestId()+" Thread Id: "+Thread.currentThread().getId());
		}
	}
	
	public boolean isStopRun() {
		return testBean.isStatus();
	}

	public void setStopRun(boolean stopRun) {
		this.testBean.setStatus(stopRun);
	}
	
	
	public JSONObject exportHarFile(String filePath, String strTargetHarFile, String strTestId) throws Throwable {
		int BUFFER_SIZE = 4096;
		FileInputStream inputStream = null;
		OutputStream outputStream = null;
		BufferedReader reader = null;
		JSONObject joResponse = new JSONObject();
		 
		 try {
			joResponse.put("success", true);
			// takes file path from input parameter
			File uploadFile = new File(filePath);
			 
			//System.out.println("File to upload: " + filePath);
			
		   String UPLOAD_URL = Constants.EXPORT_URL;
			// creates a HTTP connection
	       URL url = new URL(UPLOAD_URL);
	       HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
	       httpConn.setUseCaches(false);
	       httpConn.setDoOutput(true);
	       httpConn.setRequestMethod("POST");
	       // sets file name as a HTTP header
	       httpConn.setRequestProperty("har_file_Name", strTargetHarFile);
	       httpConn.setRequestProperty("command", "UPLOAD");
	       httpConn.setRequestProperty("test_id", strTestId);
	       
	       // opens output stream of the HTTP connection for writing data
	       outputStream = httpConn.getOutputStream();
	       
	       // Opens input stream of the file for reading data
	       inputStream = new FileInputStream(uploadFile);
	       
	       byte[] buffer = new byte[BUFFER_SIZE];
	       int bytesRead = -1;
	       
	       System.out.println("Upload started...");
	       
	       while ((bytesRead = inputStream.read(buffer)) != -1) {
		            outputStream.write(buffer, 0, bytesRead);
		        }
	       
	       System.out.println("Upload succeded.");
	       
	       // always check HTTP response code from server
	       int responseCode = httpConn.getResponseCode();
	       
	       if (responseCode == HttpURLConnection.HTTP_OK) {
	       	// reads server's response
	       	reader = new BufferedReader(new InputStreamReader(
		                    httpConn.getInputStream()));
	       	String response = reader.readLine();
	       	System.out.println("Server's response: " + response);
		            
		    } else {
		            System.out.println("Server returned non-OK code: " + responseCode);
		            joResponse.put("success", false);
		    }
		 }catch(Throwable t) {
			 System.out.println("Exception in ExportHarFile() :"+t.getMessage());
			 t.printStackTrace();
			 joResponse.put("success", true);
			 throw t;
			 
		 }finally {
			 outputStream.close();
			 inputStream.close();
			 reader.close();
		 }
		 return joResponse;
	}
	
	public JSONObject deleteHar(String strHarPath) throws Throwable {
		JSONObject joDeleteResponse = new JSONObject();
		File file = new File(strHarPath);
		try {
			joDeleteResponse.put("success", true);
			file.delete();
		}catch(Throwable t) {
			System.out.println("Exception in deleteHar" + t.getMessage());
			joDeleteResponse.put("success", false);
			throw t;
		}
		return joDeleteResponse;
	}
}