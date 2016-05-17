package com.appedo.wpt.scheduler.timer;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TimerTask;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.util.HttpURLConnection;

import com.appedo.manager.LogManager;
import com.appedo.wpt.scheduler.common.Constants;
import com.appedo.wpt.scheduler.connect.DataBaseManager;
import com.appedo.wpt.scheduler.dbi.SUMDBI;

public class ScheduledLocationTracker extends TimerTask {

	Connection con = null;

	public ScheduledLocationTracker() {
		this.con = DataBaseManager.giveConnection();
		}

	public void run() {
		HttpClient client = null;
		PostMethod method = null;
		JSONObject joResponse = null;
		JSONObject joNodeAlert = null;
		JSONArray jaInActivenodes = null;
		SUMDBI sumdbi = null;
		Set<String> existingAgentsFromDb = null, activeAgentsFromDb = null, allActiveAgentsInApi = null, activeDesktopAgents = null, activeMobileAgents = null,
				desktopAgentsToInsert = null,mobileAgentsToInsert = null;

		try {
			allActiveAgentsInApi = new HashSet<String>();
			activeDesktopAgents = new HashSet<String>();
			activeMobileAgents = new HashSet<String>();
			desktopAgentsToInsert = new HashSet<String>();
			mobileAgentsToInsert = new HashSet<String>();

			sumdbi = new SUMDBI();
			joNodeAlert = new JSONObject();
			jaInActivenodes = new JSONArray();
			existingAgentsFromDb = sumdbi.extractExistingAgents(con);
			activeAgentsFromDb = sumdbi.extractActiveAgents(con);
			StringBuilder keyStrbuildr = null;
			client = new HttpClient();

			LogManager.infoLog(" frequent mail triggered in test environment : existingAgentsFromDb :"+existingAgentsFromDb);
			LogManager.infoLog(" frequent mail triggered in test environment : activeAgentsFromDb :"+activeAgentsFromDb);

			//method = new PostMethod("http://23.23.129.228/getLocations.php");
			method = new PostMethod(Constants.WPT_LOCATION_SERVER+"getLocations.php");
			method.addParameter("f", "json");
			method.setRequestHeader("Connection", "close");
			int statusCode = client.executeMethod(method);
			//System.out.println(statusCode);
			String responseStream = method.getResponseBodyAsString();

			if (statusCode == HttpURLConnection.HTTP_OK && responseStream.trim().startsWith("{") && responseStream.trim().endsWith("}")) {
				keyStrbuildr = new StringBuilder();
				joResponse = JSONObject.fromObject(responseStream);
				if (!joResponse.getString("data").equals("[]")) {
					JSONObject locationresp = (JSONObject) joResponse.get("data");
					for (Object key: locationresp.keySet()) {
						String keyStr = (String) key;
						if(keyStr.split(":").length==2){
							activeDesktopAgents.add(keyStr.split(":")[0]);	
						}else if(keyStr.split(":").length==1){
							activeMobileAgents.add(keyStr.split(":")[0]);
						}
						allActiveAgentsInApi.add(keyStr.split(":")[0]);
					}

					// To alert admin/devops when active Agents are inActive
					for (String strAgent: activeAgentsFromDb) {
						if (!allActiveAgentsInApi.contains(strAgent)) {
							jaInActivenodes.add(strAgent);
						}
					}
					if(jaInActivenodes.size() != 0 && jaInActivenodes !=null){
						joNodeAlert.put("inactive_nodes", jaInActivenodes.toString());
					}

					Iterator<String> itr= allActiveAgentsInApi.iterator();
					while(itr.hasNext()){
						String keyStr = (String) itr.next();
						keyStrbuildr.append("'")
							.append(keyStr.split(":")[0])
							.append("',");
					}
					keyStrbuildr.deleteCharAt(keyStrbuildr.lastIndexOf(","));
					String activeNodes = keyStrbuildr.toString();
						//update inactive locations in DB
						sumdbi.updateInactiveAgents(activeNodes, con);

						//Desktop
						for (String activeDeskloc: activeDesktopAgents) {
							if (!existingAgentsFromDb.contains(activeDeskloc)) {
								desktopAgentsToInsert.add(activeDeskloc);
							}
						}
						//Mobile
						for (String activeMobiloc: activeMobileAgents) {
							if (!existingAgentsFromDb.contains(activeMobiloc)) {
								mobileAgentsToInsert.add(activeMobiloc);
							}
						}
						// To insert new Desktop/Mobile Agents
							if (desktopAgentsToInsert.size() > 0) {
								LogManager.infoLog(" frequent mail triggered in test environment : desktopAgentsToInsert :"+desktopAgentsToInsert);
								boolean desktopAgentsAdded = false;
								desktopAgentsAdded = sumdbi.insertNewDesktopAgents(con, desktopAgentsToInsert);
								if(desktopAgentsAdded){
									joNodeAlert.put("new_desktop_agents", desktopAgentsToInsert.toString()+" - were updated in database");
								}else{
									joNodeAlert.put("new_desktop_agents", desktopAgentsToInsert.toString()+" - were not updated in database");
								}
								LogManager.infoLog("New Desktop Agents "+ desktopAgentsToInsert.toString());
							}
							if (mobileAgentsToInsert.size() > 0) {
								boolean mobileAgentsAdded = false;
								mobileAgentsAdded = sumdbi.insertNewMobileAgents(con, mobileAgentsToInsert);
								if(mobileAgentsAdded){
									joNodeAlert.put("new_mobile_agents", mobileAgentsToInsert.toString()+" - were updated in database");
								}else{
									joNodeAlert.put("new_mobile_agents", mobileAgentsToInsert.toString()+" - were not updated in database");
								}
								LogManager.infoLog("New Mobile Agents "+ mobileAgentsToInsert.toString());
							}
							//to alert admin/devops when active Agents are New /inactive
							if(joNodeAlert.size() > 0 && joNodeAlert != null){
								LogManager.infoLog("json with node names to Alert devops:: "+joNodeAlert.toString());
								client = new HttpClient();
								// URLEncoder.encode(requestUrl,"UTF-8");
								method = new PostMethod(Constants.APPEDO_SLA_COLLECTOR);
								method.addParameter("command", "inActiveLocations");
								method.addParameter("inActiveLocations", joNodeAlert.toString());
								//method.setRequestHeader("Connection", "close");
								statusCode = client.executeMethod(method);
								LogManager.infoLog("While Sending to sla_Collector :: "+statusCode);	
							}
				} else {
					sumdbi.updateAllAgentsInactive(con);
					LogManager.infoLog("No locations found in getLocations.php API");
				}
			} else {
				LogManager.infoLog("No response from wpt server. Response status code : "+ statusCode);
			}
		} catch (Throwable e) {
			LogManager.errorLog(e);
			try {
				if (!DataBaseManager.isConnectionExists(con)) {
					con = DataBaseManager.reEstablishConnection(con);
				}
			} catch (SQLException e1) {
				LogManager.errorLog(e);
			}
		} finally {
			try {
				existingAgentsFromDb = null;
				allActiveAgentsInApi = null;
				activeAgentsFromDb = null;
				activeDesktopAgents = null;
				activeMobileAgents = null;
				desktopAgentsToInsert = null;
				mobileAgentsToInsert = null;
			} catch (Exception e2) {
				LogManager.errorLog(e2);
			}
		}
	}

	protected void finalize() throws Throwable {
		LogManager.infoLog("Node inactiavting Thread is stopping");
		if (con != null) {
			DataBaseManager.close(con);
			con = null;
		}
		super.finalize();
	}
}