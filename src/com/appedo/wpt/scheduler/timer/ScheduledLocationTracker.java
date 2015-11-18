package com.appedo.wpt.scheduler.timer;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TimerTask;

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
		SUMDBI sumdbi = null;
		Set < String > existingAgents = null, allActiveAgents = null,activeDesktopAgents = null,activeMobileAgents = null,
				desktopAgentsToInsert = null,mobileAgentsToInsert = null;
		
		try {
			allActiveAgents = new HashSet < String > ();
			activeDesktopAgents = new HashSet < String > ();
			activeMobileAgents = new HashSet < String > ();
			desktopAgentsToInsert = new HashSet < String > ();
			mobileAgentsToInsert = new HashSet < String > ();
			
			sumdbi = new SUMDBI();
			existingAgents = sumdbi.extractexistingloc(con);
			client = new HttpClient();

			//method = new PostMethod("http://23.23.129.228/getLocations.php");
			method = new PostMethod(Constants.WPT_LOCATION_SERVER+"getLocations.php");
			method.addParameter("f", "json");
			method.setRequestHeader("Connection", "close");
			int statusCode = client.executeMethod(method);
			//System.out.println(statusCode);
			String responseStream = method.getResponseBodyAsString();

			if (statusCode == HttpURLConnection.HTTP_OK && responseStream.trim().startsWith("{") && responseStream.trim().endsWith("}")) {
				StringBuilder keyStrbuildr = new StringBuilder();
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
						allActiveAgents.add(keyStr.split(":")[0]);
					}
					Iterator<String> itr= allActiveAgents.iterator();
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
							if (!existingAgents.contains(activeDeskloc)) {
								desktopAgentsToInsert.add(activeDeskloc);
							}
						}
						//mobile
						for (String activeMobiloc: activeMobileAgents) {
							if (!existingAgents.contains(activeMobiloc)) {
								mobileAgentsToInsert.add(activeMobiloc);
							}
						}
						// To insert new Desktop/Mobile Agents
							if (desktopAgentsToInsert.size() > 0) {
								sumdbi.insertNewDesktopAgents(con, desktopAgentsToInsert);
							} else {LogManager.infoLog("No Desktop Locations to insert");}
							if(mobileAgentsToInsert.size() > 0){
								sumdbi.insertNewMobileAgents(con, mobileAgentsToInsert);
							}else{LogManager.infoLog("No Mobile Locations to insert");}
				} else {
					sumdbi.updateAllAgentsInactive(con);
					LogManager.infoLog("No locations found in getLocations.php API");
					}
			} else {
				LogManager.infoLog("No response from wpt server. Response status code : "+statusCode);
				}
		} catch(Throwable e) {
			LogManager.errorLog(e);	
			try {
				if( ! DataBaseManager.isConnectionExists(con) ){
					con = DataBaseManager.reEstablishConnection(con);
				}
			} catch (SQLException e1) {
				LogManager.errorLog(e);
				}
		}finally{
			try {
				existingAgents=null;
				allActiveAgents = null;
				activeDesktopAgents = null;
				activeMobileAgents = null;
				desktopAgentsToInsert=null;
				mobileAgentsToInsert=null;
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