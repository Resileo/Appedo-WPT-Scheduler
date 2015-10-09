package com.appedo.wpt.scheduler.servlet;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.json.XML;

import com.appedo.manager.LogManager;
import com.appedo.wpt.scheduler.common.Constants;
import com.appedo.wpt.scheduler.connect.DataBaseManager;
import com.appedo.wpt.scheduler.manager.SUMManager;
import com.appedo.wpt.scheduler.sum.HttpDownloadUtility;

/*
 * Get the url from wpt server to process the result.
 */
public class TransactionResult extends HttpServlet {

	Connection con = null;
	public TransactionResult() {
		super();
	}

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doAction(request, response);
	}

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doAction(request, response);
	}

	private void doAction(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpClient client = null;
		PostMethod method = null;
		int statusCode = -1;
		String responseStream = "";
		JSONObject joResponse = null;
		try {
			con = DataBaseManager.giveConnection();
			long test_id = Long.valueOf(request.getParameter("testid"));
			String wpt_test_code = request.getParameter("testcode");
			//statusCode = Integer.valueOf(request.getParameter("statuscode"));
			//String statusText = request.getParameter("statustext");
			String location = request.getParameter("location");
			SUMManager sumManager = new SUMManager();
			//update the details in db.
			LogManager.infoLog("Transaction update started for test id :"+test_id+" and location :"+location);
			long harId = sumManager.insertHarTable(test_id, -1,"", wpt_test_code,location+":CHROME.Native");

			if( wpt_test_code != null){
				int statusCheckStatus = 0;
				int cnt = 1;
				while(statusCheckStatus != 200){
					client = new HttpClient();
					// URLEncoder.encode(requestUrl,"UTF-8");
					method = new PostMethod(Constants.WPT_LOCATION_SERVER+"testStatus.php");
					method.addParameter("f", "json");
					method.addParameter("test", wpt_test_code);
					method.setRequestHeader("Connection", "close");
					statusCode = client.executeMethod(method);
					
					// responseStream = "{\"statusCode\":200,\"statusText\":\"Test Complete\",\"data\":{\"statusCode\":200,\"statusText\":\"Test Complete\",\"id\":\"150813_FE_e5fd27139876bc8ed69ed3628ced3280\",\"testInfo\":{\"url\":\"http://apm.appedo.com\",\"runs\":1,\"fvonly\":0,\"web10\":0,\"ignoreSSL\":0,\"video\":\"0\",\"label\":\"TEST\",\"priority\":5,\"block\":\"null\",\"location\":\"Dulles\",\"browser\":\"Chrome\",\"connectivity\":\"Cable\",\"bwIn\":5000,\"bwOut\":1000,\"latency\":28,\"plr\":\"0\",\"tcpdump\":0,\"timeline\":0,\"trace\":0,\"bodies\":0,\"netlog\":0,\"standards\":0,\"noscript\":0,\"pngss\":0,\"iq\":50,\"keepua\":0,\"mobile\":0,\"tsview_id\":\"null\",\"scripted\":0},\"testId\":\"150813_FE_e5fd27139876bc8ed69ed3628ced3280\",\"runs\":1,\"fvonly\":0,\"remote\":false,\"testsExpected\":1,\"location\":\"Dulles\",\"startTime\":\"08/13/15 8:36:57\",\"elapsed\":32,\"completeTime\":\"08/13/15 8:37:29\",\"testsCompleted\":1,\"fvRunsCompleted\":1,\"rvRunsCompleted\":1}}";
					responseStream = method.getResponseBodyAsString();
					if( responseStream.trim().startsWith("{") && responseStream.trim().endsWith("}")) {
						joResponse = JSONObject.fromObject(responseStream);
						statusCheckStatus = joResponse.getInt("statusCode");
						//sumManager.updateHarTable(test_id, statusCode,statusText, wpt_test_code, 0, 0);
						sumManager.updateHarTable(test_id, joResponse.getInt("statusCode"), joResponse.getString("statusText"),wpt_test_code, 0, 0);
					}
					cnt++;
				}
				LogManager.infoLog("While loop count of testStatus.php: "+cnt+" TestId: "+test_id);
			

			if( statusCheckStatus == 200 ){
				client = new HttpClient();
				// URLEncoder.encode(requestUrl,"UTF-8");
				LogManager.infoLog("Before jsonResult.php for TestId: "+test_id);
				method = new PostMethod(Constants.WPT_LOCATION_SERVER+"xmlResult/"+wpt_test_code+"/");
				// method.addParameter("test", wpt_test_code);
				method.setRequestHeader("Connection", "close");
				statusCode = client.executeMethod(method);
				responseStream = method.getResponseBodyAsString();
				org.json.JSONObject xmlJSONObj = XML.toJSONObject(responseStream);
				if( xmlJSONObj.toString().startsWith("{") && xmlJSONObj.toString().endsWith("}")) {
					joResponse = JSONObject.fromObject(xmlJSONObj.toString());
					if (joResponse.containsKey("response")) {
						JSONObject jores = JSONObject.fromObject(joResponse.get("response"));
						if(jores.containsKey("data")){
							JSONObject joData = JSONObject.fromObject(jores.get("data"));
							if( joData.containsKey("average") ){
								JSONObject joAverage = JSONObject.fromObject(joData.get("average"));
								double repeatLoadTime = 0, firstLoadTime = 0;
								if(joAverage.get("firstView") instanceof JSONObject){
									JSONObject joFirstView = JSONObject.fromObject(joAverage.get("firstView"));
									firstLoadTime = joFirstView.getInt("loadTime");
								} 
								if(joAverage.get("repeatView") instanceof JSONObject){
									JSONObject joRepeatView = JSONObject.fromObject(joAverage.get("repeatView"));
									repeatLoadTime = joRepeatView.getInt("loadTime");
								} 
								sumManager.updateHarTable(test_id, jores.getInt("statusCode"), jores.getString("statusText"), wpt_test_code, ((Double)firstLoadTime).intValue(), ((Double)repeatLoadTime).intValue() );
								/*
								// SLA 
								JSONObject joSLA = new JSONObject();
								if( testBean.getThreasholdValue()> 0 && firstLoadTime > (testBean.getThreasholdValue()*1000) ){
									joSLA.put("sla_id", testBean.getSlaId());
									joSLA.put("userid", testBean.getUserId());
									joSLA.put("sla_sum_id", testBean.getSlaSumId());
									joSLA.put("sum_test_id", test_id);
									joSLA.put("har_id", harId);
									joSLA.put("is_above", testBean.isAboveThreashold());
									joSLA.put("threshold_set_value", testBean.getThreasholdValue());	
									joSLA.put("err_set_value", testBean.getErrorValue());
									joSLA.put("received_value", String.format( "%.2f", (firstLoadTime/1000)) );
									joSLA.put("min_breach_count", testBean.getMinBreachCount());
									joSLA.put("location", strLocation.split(":")[0]);
									System.out.println("json sla :: "+joSLA.toString());
									client = new HttpClient();
									// URLEncoder.encode(requestUrl,"UTF-8");
									method = new PostMethod(Constants.APPEDO_SLA_COLLECTOR);
									method.addParameter("command", "sumBreachCounterSet");
									method.addParameter("sumBreachCounterset", joSLA.toString());
//									method.setRequestHeader("Connection", "close");
									statusCode = client.executeMethod(method);
								}*/
							}
							// Insert Json into db
							sumManager.insertResultJson(joData, harId);
						}
					}
				}
				LogManager.infoLog("Before export.php for TestId: "+test_id);
				String fileURL = Constants.WPT_LOCATION_SERVER+"export.php?bodies=1&pretty=1&test="+wpt_test_code;
				String saveDir = Constants.HAR_PATH+test_id;
				HttpDownloadUtility.downloadFile(fileURL, saveDir);
				
				try {
					File file = new File(saveDir);
					for(int i=0;i<file.listFiles().length;i++){
						File f = file.listFiles()[i];
						sumManager.updateHarFileNameInTable(test_id, wpt_test_code, f.getName());
						JSONObject jo = exportHarFile(saveDir+"/"+f.getName(), f.getName(), ""+test_id);
						if(jo.getBoolean("success")){
							deleteHar(saveDir+"/"+f.getName());
						}
					}
				} catch (Throwable e) {
					LogManager.errorLog(e);
				}
			}
				
	
				if (statusCode != HttpStatus.SC_OK) {
					LogManager.infoLog("Method failed: " + method.getStatusLine());
				}
			}
	    } catch (Throwable th) {
			LogManager.errorLog(th);
	    } finally {
	    	DataBaseManager.close(con);
	    	con = null;
	    }
	}
	

	public JSONObject exportHarFile(String filePath, String strTargetHarFile, String strTestId) throws Throwable {
		int BUFFER_SIZE = 4096;
		FileInputStream inputStream = null;
		OutputStream outputStream = null;
		BufferedReader reader = null;
		JSONObject joResponse = new JSONObject();
		long startTime = System.currentTimeMillis();
		 
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
	       
	       LogManager.infoLog("Upload started...");
	       
	       while ((bytesRead = inputStream.read(buffer)) != -1) {
		            outputStream.write(buffer, 0, bytesRead);
		        }
	       
	       LogManager.infoLog("Upload succeded.");
	       
	       // always check HTTP response code from server
	       int responseCode = httpConn.getResponseCode();
	       
	       if (responseCode == HttpURLConnection.HTTP_OK) {
	       	// reads server's response
	       	reader = new BufferedReader(new InputStreamReader(
		                    httpConn.getInputStream()));
	       	String response = reader.readLine();
	       	LogManager.infoLog("Server's response: " + response);
		            
		    } else {
		    	LogManager.infoLog("Server returned non-OK code: " + responseCode);
		            joResponse.put("success", false);
		    }
		 }catch(Throwable t) {
			 LogManager.errorLog("Exception in ExportHarFile() :"+t.getMessage());
			 t.printStackTrace();
			 joResponse.put("success", true);
			 throw t;
			 
		 }finally {
			 LogManager.infoLog("Time Taken to export har file to server: "+(System.currentTimeMillis() - startTime)+" TestId: "+strTestId);
			 outputStream.close();
			 inputStream.close();
			 reader.close();
		 }
		 return joResponse;
	}
	
	public JSONObject deleteHar(String strHarPath) throws Throwable {
		JSONObject joDeleteResponse = new JSONObject();
		long startTime = System.currentTimeMillis();
		File file = new File(strHarPath);
		try {
			joDeleteResponse.put("success", true);
			file.delete();
			LogManager.infoLog("Time taken to deleteHar: "+(System.currentTimeMillis() - startTime));
		}catch(Throwable t) {
			LogManager.errorLog("Exception in deleteHar" + t.getMessage());
			joDeleteResponse.put("success", false);
			throw t;
		}
		return joDeleteResponse;
	}

		
		

}