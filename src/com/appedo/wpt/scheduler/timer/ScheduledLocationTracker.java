package com.appedo.wpt.scheduler.timer;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashSet;
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
		
	}

	public void run() {
		con = DataBaseManager.giveConnection();
		SUMDBI sumdbi = new SUMDBI();
		HashSet < String > existingloc = sumdbi.extractexistingloc(con);
		HashSet < String > locToInsert = new HashSet < String > ();
		// Create Repetitively task for every 1 secs
		HttpClient client = null;
		PostMethod method = null;
		JSONObject joResponse = null;

		try {
			client = new HttpClient();

			//method = new PostMethod("http://23.23.129.228/getLocations.php");
			method = new PostMethod(Constants.WPT_LOCATION_SERVER+"getLocations.php");
			method.addParameter("f", "json");
			method.setRequestHeader("Connection", "close");
			int statusCode = client.executeMethod(method);
			//System.out.println(statusCode);
			String responseStream = method.getResponseBodyAsString();
			HashSet < String > activeLocations = new HashSet < String > ();

			if (statusCode == HttpURLConnection.HTTP_OK && responseStream.trim().startsWith("{") && responseStream.trim().endsWith("}")) {
				StringBuilder keyStrbuildr = new StringBuilder();
				joResponse = JSONObject.fromObject(responseStream);
				if (!joResponse.getString("data").equals("[]")) {
					JSONObject locationresp = (JSONObject) joResponse.get("data");
					for (Object key: locationresp.keySet()) {

						String keyStr = (String) key;
						activeLocations.add(keyStr.split(":")[0]);
						keyStrbuildr.append("'")
							.append(keyStr.split(":")[0])
							.append("',");
					}
					keyStrbuildr.deleteCharAt(keyStrbuildr.lastIndexOf(","));
					String activeNodes = keyStrbuildr.toString();

					for (String activeloc: activeLocations) {
						if (!existingloc.contains(activeloc)) {
							locToInsert.add(activeloc);
						}
					}
					// insert new locations
					if (locToInsert.size() > 0) {
						sumdbi.insertNewLocation(con, locToInsert);
					} else {
						LogManager.infoLog("No Locations to insert");
					}
					//update inactive locations
					sumdbi.updateInactiveLocation(activeNodes, con);

				} else {
					LogManager.infoLog("No locations found in getLocations.php API");
				}
			} else {
				LogManager.infoLog("No response from wpt server. Response status code : "+statusCode);
			}
		} catch (Throwable e) {
			LogManager.errorLog(e);
			
		}finally{
			try {
				if (con != null) {
					DataBaseManager.close(con);
				}
			} catch (Exception e1) {
				LogManager.errorLog(e1);
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