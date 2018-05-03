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
import org.json.XML;

import com.appedo.manager.LogManager;
import com.appedo.wpt.scheduler.bean.SUMTestBean;
import com.appedo.wpt.scheduler.common.Constants;
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
	public RunTest(String strLocation, SUMTestBean testBean) throws Throwable {

		try {
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
		boolean isDowntime=false;
		try {
			sumManager = new SUMManager();
			// Pushing test to 70.70 server, get response 
			client = new HttpClient();
			// URLEncoder.encode(requestUrl,"UTF-8");
			LogManager.infoLog("Before Starting Test through runtest.php for TestId: "+testBean.getTestId());
			
			method = new PostMethod(Constants.WPT_LOCATION_SERVER+"runtest.php");
			method.addParameter("url", testBean.getURL());
			method.addParameter("label", testBean.getTestName());
			method.addParameter("location", strLocation);
			// method.addParameter("runs", ""+testBean.getRunEveryMinute()); -should be discussed
			method.addParameter("runs", "1");
			method.addParameter("fvonly", testBean.getRepeatView()); //Set 0 to get Repeatview and 1 for first view alone
			// method.addParameter("domelement", "null");
			method.addParameter("private", "0");
			method.addParameter("connections", "0");
			method.addParameter("web10", "0");
			// method.addParameter("block", "null");
			// method.addParameter("login", "null");
			// method.addParameter("password", "null");
			method.addParameter("authType", "0");
			method.addParameter("video", "1");
			method.addParameter("f", "json");
			// method.addParameter("r", "null");
			// method.addParameter("notify", "null");
			// method.addParameter("pingback", "null");
			method.addParameter("bwDown", testBean.getDownload());
			method.addParameter("bwUp", testBean.getUpload());
			method.addParameter("latency", testBean.getLatency());
			method.addParameter("plr", testBean.getPacketLoss());
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
			// method.addParameter("tsview_id", "null");
			// method.addParameter("custom", "null");
			// method.addParameter("tester", "null");
			// method.addParameter("affinity", "null");
			method.addParameter("timeline", "0");
			method.addParameter("timelineStack", "0");
			method.addParameter("ignoreSSL", "0");
			
			method.setRequestHeader("Connection", "close");
			int statusCode = client.executeMethod(method);
			LogManager.infoLog("statusCode: "+statusCode);
			
			String responseStream = method.getResponseBodyAsString().trim();
			// Sample responseStream = "{\"statusCode\":200,\"statusText\":\"Ok\",\"data\":{\"testId\":\"150813_FE_e5fd27139876bc8ed69ed3628ced3280\",\"ownerKey\":\"e9028f67d5a3dba9019b214267a555d422e43d1b\",\"jsonUrl\":\"http://www.webpagetest.org/jsonResult.php?test=150813_FE_e5fd27139876bc8ed69ed3628ced3280\",\"xmlUrl\":\"http://www.webpagetest.org/xmlResult/150813_FE_e5fd27139876bc8ed69ed3628ced3280/\",\"userUrl\":\"http://www.webpagetest.org/result/150813_FE_e5fd27139876bc8ed69ed3628ced3280/\",\"summaryCSV\":\"http://www.webpagetest.org/result/150813_FE_e5fd27139876bc8ed69ed3628ced3280/page_data.csv\",\"detailCSV\":\"http://www.webpagetest.org/result/150813_FE_e5fd27139876bc8ed69ed3628ced3280/requests.csv\"}}";
			LogManager.infoLog("Response from runtest.php for TestId: "+testBean.getTestId()+" <> response: "+responseStream);
			
			String runTestCode = null;
			if( ! responseStream.startsWith("{") || ! responseStream.endsWith("}")) {
				LogManager.errorLog("Response from runtest.php is not JSON for TestId: "+testBean.getTestId()+" <> response: "+responseStream);
			} else {
				JSONObject joResponse = JSONObject.fromObject(responseStream);
				if( ! joResponse.containsKey("data") ) {
					LogManager.errorLog("Response JSON from runtest.php does't ve `data` key, for TestId: "+testBean.getTestId()+" <> response: "+responseStream);
				} else {
					JSONObject joData = JSONObject.fromObject(joResponse.get("data"));
					runTestCode = joData.getString("testId");
				}
				
				// preparation of sum_har_results table
				long harId = sumManager.insertHarTable(testBean.getTestId(), joResponse.getInt("statusCode"), joResponse.getString("statusText"), runTestCode, testBean.getLocation());
				sumManager.updateMeasurementCntInUserMaster(testBean.getTestId());
				// sumManager.updateSumTestLastRunDetail(testBean.getTestId());
				
				if( runTestCode != null){
					int statusCheckStatus = 0;
					int cnt = 1;
					while(statusCheckStatus != 200){
						client = new HttpClient();
						// URLEncoder.encode(requestUrl,"UTF-8");
						method = new PostMethod(Constants.WPT_LOCATION_SERVER+"testStatus.php");
						method.addParameter("f", "json");
						method.addParameter("test", runTestCode);
						method.setRequestHeader("Connection", "close");
						statusCode = client.executeMethod(method);
						
						// responseStream = "{\"statusCode\":200,\"statusText\":\"Test Complete\",\"data\":{\"statusCode\":200,\"statusText\":\"Test Complete\",\"id\":\"150813_FE_e5fd27139876bc8ed69ed3628ced3280\",\"testInfo\":{\"url\":\"http://apm.appedo.com\",\"runs\":1,\"fvonly\":0,\"web10\":0,\"ignoreSSL\":0,\"video\":\"0\",\"label\":\"TEST\",\"priority\":5,\"block\":\"null\",\"location\":\"Dulles\",\"browser\":\"Chrome\",\"connectivity\":\"Cable\",\"bwIn\":5000,\"bwOut\":1000,\"latency\":28,\"plr\":\"0\",\"tcpdump\":0,\"timeline\":0,\"trace\":0,\"bodies\":0,\"netlog\":0,\"standards\":0,\"noscript\":0,\"pngss\":0,\"iq\":50,\"keepua\":0,\"mobile\":0,\"tsview_id\":\"null\",\"scripted\":0},\"testId\":\"150813_FE_e5fd27139876bc8ed69ed3628ced3280\",\"runs\":1,\"fvonly\":0,\"remote\":false,\"testsExpected\":1,\"location\":\"Dulles\",\"startTime\":\"08/13/15 8:36:57\",\"elapsed\":32,\"completeTime\":\"08/13/15 8:37:29\",\"testsCompleted\":1,\"fvRunsCompleted\":1,\"rvRunsCompleted\":1}}";
						responseStream = method.getResponseBodyAsString().trim();
						
						if( ! responseStream.startsWith("{") || ! responseStream.endsWith("}") ) {
							LogManager.errorLog("Response from testStatus.php is not JSON for TestId: "+testBean.getTestId()+" <> runTestCode: "+runTestCode+" <> response: "+responseStream);
						} else {
							joResponse = JSONObject.fromObject(responseStream);
							statusCheckStatus = joResponse.getInt("statusCode");
							
							// Log the exceptions
							if( statusCheckStatus != 200 ) {
								// Avoid printing 100 & 101 for first few iterations, as the test could be in WIP in WPT side.
								if ( cnt > 20 || ! ( statusCheckStatus == 100 || statusCheckStatus == 101 ) ) {
									// If status code is not `200` , make sleep it for 10 secs
									LogManager.errorLog("Status-Code from testStatus.php, for TestId: "+testBean.getTestId()+" <> runTestCode: "+runTestCode+" <> "+statusCheckStatus);
								}
								
								Thread.sleep(10*1000);
							}
							
							// To update har table status for showing in New SUM UI
							// sumManager.updateHarTable(testBean.getTestId(), joResponse.getInt("statusCode"), joResponse.getString("statusText"), runTestCode, 0, 0);
						}
						cnt++;
					}
					LogManager.infoLog("While loop count of testStatus.php: "+cnt+" TestId: "+testBean.getTestId()+" <> runTestCode: "+runTestCode);
					
					if( statusCheckStatus == 200 ){
						client = new HttpClient();
						// URLEncoder.encode(requestUrl,"UTF-8");
						LogManager.infoLog("Before jsonResult.php for TestId: "+testBean.getTestId()+" <> runTestCode: "+runTestCode);
						
						method = new PostMethod(Constants.WPT_LOCATION_SERVER+"xmlResult/"+runTestCode+"/");
						// method.addParameter("test", runTestCode);
						method.setRequestHeader("Connection", "close");
						statusCode = client.executeMethod(method);
						responseStream = method.getResponseBodyAsString();
						org.json.JSONObject xmlJSONObj = XML.toJSONObject(responseStream);
						
						// TODO what is the response for stopped servers; Send SLA
						
/*						if( xmlJSONObj.toString().startsWith("{") && xmlJSONObj.toString().endsWith("}")) {
							joResponse = JSONObject.fromObject(xmlJSONObj.toString());*/
							if ( ! xmlJSONObj.has("response") ) {
								LogManager.infoLog("xmlJSONObj does not contain response: TestId: "+testBean.getTestId()+" <> runTestCode: "+runTestCode);
							} else {
								org.json.JSONObject jores = xmlJSONObj.getJSONObject("response");
								if(jores.has("data")){
									org.json.JSONObject joData = jores.getJSONObject("data");

									if(joData.has("run")){
										org.json.JSONObject jorun = joData.getJSONObject("run");
										if(jorun.has("firstView")){
											org.json.JSONObject joFView = jorun.getJSONObject("firstView");
											
											if( joFView.has("status") && joFView.getString("status").length() > 0){
												LogManager.infoLog("Status of the url configured with the TestId: "+testBean.getTestId()+" <> runTestCode: "+runTestCode+" - "+joFView.getString("status"));
												isDowntime=true;
											}
										}
									}
									if( joData.has("average") ){
										org.json.JSONObject joAverage = joData.getJSONObject("average");
										double repeatLoadTime = 0, firstLoadTime = 0;
										if(joAverage.get("firstView") instanceof org.json.JSONObject){
											org.json.JSONObject joFirstView = joAverage.getJSONObject("firstView");
											if( joFirstView.has("loadTime") ){
												firstLoadTime = joFirstView.getInt("loadTime");
											}
										} 
										if(joAverage.has("repeatView") && joAverage.get("repeatView") instanceof org.json.JSONObject){
											org.json.JSONObject joRepeatView = joAverage.getJSONObject("repeatView");
											if( joRepeatView.has("loadTime") ){
												repeatLoadTime = joRepeatView.getInt("loadTime");
											}
										} 
										sumManager.updateHarTable(testBean.getTestId(), jores.getInt("statusCode"), jores.getString("statusText"), runTestCode, ((Double)firstLoadTime).intValue(), ((Double)repeatLoadTime).intValue() );
										
										// SLA
										JSONObject joSLA = new JSONObject();
										joSLA.put("sla_id", testBean.getSlaId());
										joSLA.put("userid", testBean.getUserId());
										joSLA.put("sla_sum_id", testBean.getSlaSumId());
										joSLA.put("sum_test_id", testBean.getTestId());
										joSLA.put("har_id", harId);
										joSLA.put("is_above", testBean.isAboveThreshold());
										joSLA.put("threshold_set_value", testBean.getThresholdValue());	
										joSLA.put("err_set_value", testBean.getErrorValue());
										joSLA.put("received_value", String.format( "%.2f", (firstLoadTime/1000)) );
										joSLA.put("appedoReceivedOn", new Date().getTime());
										joSLA.put("min_breach_count", testBean.getMinBreachCount());
										joSLA.put("location", strLocation.split(":")[0]);
										joSLA.put("is_Down", isDowntime);
										
										if( testBean.getThresholdValue() > 0 && firstLoadTime > (testBean.getThresholdValue()*1000) ) {
											joSLA.put("breached_severity", firstLoadTime > (testBean.getErrorValue()*1000)?"CRITICAL":"WARNING");
											
											LogManager.infoLog("json sla for SUM TestId: "+testBean.getTestId()+" <> runTestCode: "+runTestCode+" <> SLA Alert :: "+joSLA.toString());
											
											client = new HttpClient();
											// URLEncoder.encode(requestUrl,"UTF-8");
											method = new PostMethod(Constants.APPEDO_SLA_COLLECTOR);
											method.addParameter("command", "sumBreachCounterSet");
											method.addParameter("sumBreachCounterset", joSLA.toString());
//											method.setRequestHeader("Connection", "close");
											statusCode = client.executeMethod(method);
										}
										
/*										if( isDowntime && testBean.isDownTimeAlert()){
											joSLA.put("type", "Configured Site is Down");
											joSLA.put("is_Down", isDowntime);
											System.out.println("json sla for SUM Alert at Downtime:: "+joSLA.toString());
											client = new HttpClient();
											// URLEncoder.encode(requestUrl,"UTF-8");
											method = new PostMethod(Constants.APPEDO_SLA_COLLECTOR);
											method.addParameter("command", "sumDownTimeAlert");
											method.addParameter("sumBreachCounterset", joSLA.toString());
//											method.setRequestHeader("Connection", "close");
											statusCode = client.executeMethod(method);
										} */
									}
									
									// Insert Json into db
									sumManager.insertResultJson(joData, harId);
								}
							}
							
							LogManager.infoLog("Before export.php for TestId: "+testBean.getTestId()+" <> runTestCode: "+runTestCode);
							String fileURL = Constants.WPT_LOCATION_SERVER+"export.php?bodies=1&pretty=1&test="+runTestCode;
							String saveDir = Constants.HAR_PATH+testBean.getTestId();
							// Downloads HAR from WPT server and keep it in wpt_scheduler instance
							HttpDownloadUtility.downloadFile(fileURL, saveDir);
							
							try {
								File file = new File(saveDir);
								
								for(int i=0;i<file.listFiles().length;i++){
									File f = file.listFiles()[i];
									sumManager.updateHarFileNameInTable(testBean.getTestId(), runTestCode, f.getName());
									// Exports HAR files to the HAR repository
									JSONObject jo = exportHarFile(saveDir+"/"+f.getName(), f.getName(), ""+testBean.getTestId());
									if(jo.getBoolean("success")){
										deleteHar(saveDir+"/"+f.getName());
									}
								}
							} catch (Throwable th) {
								LogManager.errorLog(th);
							}
						//}
					}
				}
			}
			
			if (statusCode != HttpStatus.SC_OK) {
				LogManager.errorLog("Method failed: " + method.getStatusLine());
			}
			
		} catch (Throwable th) {
			LogManager.errorLog(th);
		} finally {
			System.out.println("Test Status: "+testBean.isStatus()+" TestId: "+testBean.getTestId()+" Thread Id: "+Thread.currentThread().getId());
		}
	}
	
	public boolean isStopRun() {
		return testBean.isStatus();
	}

	public void setStopRun(boolean stopRun) {
		this.testBean.setStatus(stopRun);
	}
	
	/**
	 * 
	 * @param filePath
	 * @param strTargetHarFile
	 * @param strTestId
	 * @return
	 * @throws Throwable
	 */
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

			String UPLOAD_URL = Constants.EXPORT_URL; //https://apm.appedo.com/AppedoEUM/importhar
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

			while ((bytesRead = inputStream.read(buffer)) != -1) {
				outputStream.write(buffer, 0, bytesRead);
			}

			// always check HTTP response code from server
			int responseCode = httpConn.getResponseCode();

			if (responseCode == HttpURLConnection.HTTP_OK) {
				// reads server's response
				reader = new BufferedReader(new InputStreamReader(httpConn.getInputStream()));
				String response = reader.readLine();
			} else {
				LogManager.errorLog("Server returned non-OK code: " + responseCode);
				joResponse.put("success", false);
			}
		} catch (Throwable t) {
			joResponse.put("success", true);
			throw t;
		} finally {
			LogManager.infoLog("Time Taken to export har file to server: " + (System.currentTimeMillis() - startTime) + " TestId: " + strTestId);
			outputStream.close();
			inputStream.close();
			reader.close();
		}
		 return joResponse;
	}
	
	/**
	 * 
	 * @param strHarPath
	 * @return
	 * @throws Throwable
	 */
	public JSONObject deleteHar(String strHarPath) throws Throwable {
		JSONObject joDeleteResponse = new JSONObject();
		long startTime = System.currentTimeMillis();
		File file = new File(strHarPath);
		try {
			joDeleteResponse.put("success", true);
			file.delete();
			LogManager.infoLog("Time taken to deleteHar: " + (System.currentTimeMillis() - startTime));
		} catch (Throwable t) {
			joDeleteResponse.put("success", false);
			throw t;
		}
		return joDeleteResponse;
	}
}