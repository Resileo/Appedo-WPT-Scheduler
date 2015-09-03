package com.appedo.wpt.scheduler.servlet;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import com.appedo.wpt.scheduler.connect.DataBaseManager;
import com.appedo.wpt.scheduler.dbi.SUMDBI;
import com.appedo.wpt.scheduler.manager.LogManager;
import com.appedo.wpt.scheduler.manager.NodeManager;
import com.appedo.wpt.scheduler.utils.UtilsFactory;

/**
 * Servlet implementation class UpdateAgentDetails
 */
//@WebServlet("/UpdateAgentDetails")
public class UpdateAgentDetails extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	Connection con = null;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public UpdateAgentDetails() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		JSONObject joPostResponse = new JSONObject() ;
		
		try {
			joPostResponse =doAction(request,response);
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
			joPostResponse.put("success", false);
			joPostResponse.put("error", e.getMessage());
			
		}
		
		response.getWriter().print(joPostResponse.toString());
		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		JSONObject joPostResponse = new JSONObject() ;
		
		try {
			synchronized ( joPostResponse ) {
				joPostResponse = doAction(request,response);	
			}
			
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
			joPostResponse.put("success", false);
			joPostResponse.put("error", e.getMessage());
			
		}
		
		response.getWriter().print(joPostResponse.toString());
	}
	
	/**
	 * to handle the incoming messages which is coming from the sum agent
	 * @param request
	 * @param response
	 * @throws Throwable
	 */
	private JSONObject doAction(HttpServletRequest request, HttpServletResponse response) throws Throwable {
		
		JSONObject joNodeSummary = new JSONObject() ;
		JSONObject joActionResponse = new JSONObject() ;
		
		String strCommand = null;
		String strMsg = null;
		
		SUMDBI sumDBI = new SUMDBI();
		
		try {
			joActionResponse.put("success", true);
			
			
				strCommand = request.getHeader("command").trim().toUpperCase();
				strMsg = request.getHeader("msg").trim();
				strMsg = JSONObject.fromObject(strMsg).toString().trim();				
				joNodeSummary = JSONObject.fromObject(strMsg);
				String ipAddress = request.getHeader("X-FORWARDED-FOR");  
			    if (ipAddress == null) {
			    	ipAddress = request.getRemoteAddr();
			    }
			       
			     System.out.println("IPADDRESS::: "+ipAddress); 

				switch(strCommand) {
					case "ISVALIDUSER" : {
						int nSumUserId = 0;
						try {
							con = DataBaseManager.giveConnection();
							nSumUserId = getUserId(con, joNodeSummary.getString("nodeuserid"));
							if(nSumUserId>0)
								joActionResponse.put("isvaliduser", true);
							else
								joActionResponse.put("isvaliduser", false);
							
						}catch(Exception e) {
							LogManager.errorLog(e);
						}
						break;
					}
					
					case "ISADMIN" : {						
						try {
							con = DataBaseManager.giveConnection();
							int nSumUserId = getUserId(con, joNodeSummary.getString("nodeuserid"));
							if(nSumUserId==1)
								joActionResponse.put("isadmin", "true");
							else
								joActionResponse.put("isadmin", "false");
							
						}catch(Exception e) {
							LogManager.errorLog(e);
						}
						break;
					}
					
					case "AGENT" : {
						updateNodeStatus(joNodeSummary);
						break;
					}
					
					case "ND" : {
						if(joNodeSummary.getBoolean("success")) {
							con = DataBaseManager.giveConnection();
							int nUserId = getUserId(con, joNodeSummary.getString("NodeUserId"));
							
							// if Node already exists then update its new details
							// otherwise add the Node
							Boolean bIsNodeexist = isNodeExist(con, nUserId, joNodeSummary.getString("Mac") );
							
							if(bIsNodeexist) {										// update node details
								updateNodeDetails(nUserId, joNodeSummary);
							}else {													// insert node details
								addNodeDetails(nUserId, joNodeSummary);
							}
						}
						break;
					}
					
					case "STATUS" : {
						updateNodeStatus(joNodeSummary);
						break;
					}
					
					case "LOCATION" : {
						if( joNodeSummary.getBoolean("success") ) {
							// update node details table
							updateNodeLocation(joNodeSummary);
						}
						break;
					}
					
					case "HAR" : {
						con = DataBaseManager.giveConnection();
						Long testid = joNodeSummary.getLong("test_id");
						
						if( joNodeSummary.getString("test_type").equals("TRANSACTION") ) {
							sumDBI.insertHarFileTableForScript(joNodeSummary, testid, joNodeSummary.getString("mac"));
						} else if( joNodeSummary.getString("test_type").equals("URL") ) {
							sumDBI.insertHarFileTableForURL(joNodeSummary, testid, joNodeSummary.getString("mac"));
						}
						sumDBI.updateMeasurementCntInUserMaster(con, testid);
						break;
					}
					
					case "TEST_STATUS" : {
						sumDBI.updateSUMLog(joNodeSummary);
						break;
					}
					
					case "WPT_AGENT" : {
						con = DataBaseManager.giveConnection();
						
						System.out.println("joNodeSummary:: "+joNodeSummary);
						break;
					}
					
				}
			} catch(Throwable t) {
				LogManager.errorLog(t);
				joActionResponse.put("success", false);
				joActionResponse.put("error", t.getMessage());
				throw t;
			} finally {
				DataBaseManager.close(con);
				con = null;				
				sumDBI = null;
				strCommand = null;
				strMsg = null;
			}
		return joActionResponse;
	}
	
	public Boolean isNodeExist(Connection con, int strSumUserId, String strMac) throws Exception {
		PreparedStatement pstmt = null;
		ResultSet rst = null;
		
		Boolean bool = false;
		String strQry = "select sum_user_id from sum_node_details where mac_address='"+strMac+"'";
		try {
			
			pstmt = con.prepareStatement(strQry);
			rst = pstmt.executeQuery();
			while (rst.next()) {
				
				if(rst.getString(1)!=null)
					bool = true;
				else
					bool = false;
			}
		}catch (Exception e) {
			LogManager.infoLog("sbQuery : " + strQry);
			LogManager.errorLog(e);
			throw e;
		} finally {
			DataBaseManager.close(rst);
			rst = null;
			DataBaseManager.close(pstmt);
			pstmt = null;
			
			strQry = null;
		}
		
		return bool;
	}
	
	/***
	 * to get node user id 
	 * @param strEnyUserId
	 * @return
	 * @throws Exception
	 */
	public int getUserId(Connection con, String strEnyUserId) throws Exception {
		PreparedStatement pstmt = null;
		ResultSet rst = null;
		
		int nUserId = 0;
		String strQry = "select  sum_user_id  from sum_node_registration where encrypted_sum_user_id='"+strEnyUserId+"'";
		
		try {
			pstmt = con.prepareStatement(strQry);
			rst = pstmt.executeQuery();
			while (rst.next()) {
				if( rst.getString(1) !=null )
					nUserId = Integer.parseInt(rst.getString(1));
				else
					nUserId = 0;
			}
		} catch (Exception e) {
			LogManager.infoLog("sbQuery : " + strQry);
			LogManager.errorLog(e);
			throw e;
		} finally {
			DataBaseManager.close(rst);
			rst = null;
			DataBaseManager.close(pstmt);
			pstmt = null;
			
			strQry = null;
		}
		
		return nUserId;
	}
	
	/**
	 * to add details of node to node_details table
	 * @param strQry
	 * @throws Throwable
	 */
	public void addNodeDetails(int nUserId, JSONObject joNodeSummary) throws Throwable {
		StringBuilder sbQuery = new StringBuilder();
		try {
			sbQuery	.append("insert into sum_node_details(sum_user_id,sum_node_status,sum_agent_version,mac_address,agent_type,ipaddress,city,state,country,latitude,longitude,selenium_webdriver_version,")
					.append("jre_version,firebug_version,netexport_version,os_type,operating_system,os_version,chrome_version,remarks,created_by,created_on) values (")
					.append(nUserId).append(",")
					.append(UtilsFactory.makeValidVarchar(joNodeSummary.getString("Status"))).append(",")
					.append(UtilsFactory.makeValidVarchar(joNodeSummary.getString("agent_version"))).append(",")
					.append(UtilsFactory.makeValidVarchar(joNodeSummary.getString("Mac"))).append(",")
					.append(UtilsFactory.makeValidVarchar(joNodeSummary.getString("AgentType"))).append(",")
					.append(UtilsFactory.makeValidVarchar(joNodeSummary.getString("IpAddress"))).append(",")
					.append(UtilsFactory.makeValidVarchar(joNodeSummary.getString("City"))).append(",")
					.append(UtilsFactory.makeValidVarchar(joNodeSummary.getString("State"))).append(",")
					.append(UtilsFactory.makeValidVarchar(joNodeSummary.getString("Country"))).append(",")
					.append(UtilsFactory.makeValidVarchar(joNodeSummary.getString("Lat"))).append(",")
					.append(UtilsFactory.makeValidVarchar(joNodeSummary.getString("Lon"))).append(",")
					.append(UtilsFactory.makeValidVarchar(joNodeSummary.getString("Selenium_version"))).append(",")
					.append(UtilsFactory.makeValidVarchar(joNodeSummary.getString("Jre_version"))).append(",")
					.append(UtilsFactory.makeValidVarchar(joNodeSummary.getString("Firebug_version"))).append(",")
					.append(UtilsFactory.makeValidVarchar(joNodeSummary.getString("Netexport_version"))).append(",")
					.append(UtilsFactory.makeValidVarchar(joNodeSummary.getString("Os_type"))).append(",")
					.append(UtilsFactory.makeValidVarchar(joNodeSummary.getString("Operating_system"))).append(",")
					.append(UtilsFactory.makeValidVarchar(joNodeSummary.getString("Os_version"))).append(",")
					.append(UtilsFactory.makeValidVarchar(joNodeSummary.getString("Chrome_version"))).append(",")
					//.append(joNodeSummary.getString("Is_active")).append(",'")
					.append(UtilsFactory.makeValidVarchar(joNodeSummary.getString("remarks")))
					.append(", -1, now() )");
			
			NodeManager.agentLogQueue(sbQuery.toString());
			
		} catch(Throwable t) {
			LogManager.errorLog(t);
			
			throw t;
		} finally {
			UtilsFactory.clearCollectionHieracy( sbQuery );
		}
	}
	
	/***
	 * to update node details
	 * @param joUpdateSummary
	 * @param sbQuery
	 * @throws Exception
	 */
	
	public void updateNodeDetails(int nUserId, JSONObject joUpdateSummary) throws Exception {
		
		StringBuilder sbQuery = new StringBuilder();
		try {
			//sb.append("update sum_node_details set AgentType = ? , IpAddress=? , City=?, State=?, Country=?, Lat=?, Lon=?, Selenium_version=?,")
			//.append("Jre_version=? , Firebug_version=? , Netexport_version=? , Os_type=? , operating_system=? , os_version=?, chrome_version=? , is_active=? ,remarks=? ,modified_by=? , modified_on=now()" );
			sbQuery	.append("UPDATE sum_node_details SET agent_type = ")
					.append(UtilsFactory.makeValidVarchar(joUpdateSummary.getString("AgentType"))).append(", IpAddress = ")
					.append(UtilsFactory.makeValidVarchar(joUpdateSummary.getString("IpAddress"))).append(", City = ")
					.append(UtilsFactory.makeValidVarchar(joUpdateSummary.getString("City"))).append(", State = ")
					.append(UtilsFactory.makeValidVarchar(joUpdateSummary.getString("State"))).append(", Country = ")
					.append(UtilsFactory.makeValidVarchar(joUpdateSummary.getString("Country"))).append(", latitude = ")
					.append(UtilsFactory.makeValidVarchar(joUpdateSummary.getString("Lat"))).append(", longitude = ")
					.append(UtilsFactory.makeValidVarchar(joUpdateSummary.getString("Lon"))).append(", selenium_webdriver_version = ")
					.append(UtilsFactory.makeValidVarchar(joUpdateSummary.getString("Selenium_version"))).append(", Jre_version = ")
					.append(UtilsFactory.makeValidVarchar(joUpdateSummary.getString("Jre_version"))).append(", Firebug_version = ")
					.append(UtilsFactory.makeValidVarchar(joUpdateSummary.getString("Firebug_version"))).append(", Netexport_version = ")
					.append(UtilsFactory.makeValidVarchar(joUpdateSummary.getString("Netexport_version"))).append(", Os_type = ")
					.append(UtilsFactory.makeValidVarchar(joUpdateSummary.getString("Os_type"))).append(", operating_system = ")
					.append(UtilsFactory.makeValidVarchar(joUpdateSummary.getString("Operating_system"))).append(", os_version = ")
					.append(UtilsFactory.makeValidVarchar(joUpdateSummary.getString("Os_version"))).append(", chrome_version = ")
					.append(UtilsFactory.makeValidVarchar(joUpdateSummary.getString("Chrome_version"))).append(", remarks = ")
					.append(UtilsFactory.makeValidVarchar(joUpdateSummary.getString("remarks"))).append(", modified_by = ")
					.append(nUserId).append(", modified_on = now() WHERE mac_address =")
					.append(UtilsFactory.makeValidVarchar(joUpdateSummary.getString("Mac"))).append(" AND sum_user_id = ")
					.append(nUserId);
			
			NodeManager.agentLogQueue(sbQuery.toString());
			
		} catch (Exception e) {
			LogManager.errorLog(e);
			
			throw e;
		} finally {
			UtilsFactory.clearCollectionHieracy( sbQuery );
		}
	}
	/**
	 * to update agent status
	 * @param joNodeStatus
	 * @throws Exception
	 */
	public void updateNodeStatus(JSONObject joNodeStatus) throws Throwable {
		StringBuilder sbQuery = new StringBuilder();
		
		try {
			// System.out.println(" test :"+ joNodeStatus.toString());
			if(joNodeStatus.getString("status").equalsIgnoreCase("error")) {
				sbQuery	.append("update sum_node_details SET sum_node_status = ")
				        .append(UtilsFactory.makeValidVarchar(joNodeStatus.getString("status")))
				        .append(", last_error_on=now(), remarks = ")
				        .append(UtilsFactory.makeValidVarchar(joNodeStatus.getString("remarks")))
				        .append(" , modified_on=now() ")
				        .append("where mac_address = ").append(UtilsFactory.makeValidVarchar(joNodeStatus.getString("Mac")));
			}else {
				sbQuery	.append("update sum_node_details SET sum_node_status = ")
						.append(UtilsFactory.makeValidVarchar(joNodeStatus.getString("status")))
						.append(", modified_on = now() ")
						.append("where mac_address = ").append(UtilsFactory.makeValidVarchar(joNodeStatus.getString("Mac")));
			}

			NodeManager.agentLogQueue(sbQuery.toString());
			
		} catch (Throwable e) {
			LogManager.errorLog(e);
			throw e;
		} finally {
			UtilsFactory.clearCollectionHieracy( sbQuery );
		}
	}
	
	/**
	 * to update node location 
	 * @param joLocation
	 * @throws Exception
	 */
	public void updateNodeLocation(JSONObject joLocation ) throws Exception {
		StringBuilder sbQuery = new StringBuilder();
		
		try {
			sbQuery	.append("UPDATE sum_node_details SET country = ")
					.append(UtilsFactory.makeValidVarchar(joLocation.getString("Country"))).append(", state = ")
					.append(UtilsFactory.makeValidVarchar(joLocation.getString("State"))).append(", city = ")
					.append(UtilsFactory.makeValidVarchar(joLocation.getString("City"))).append(", latitude = ")
					.append(UtilsFactory.makeValidVarchar(joLocation.getString("Lat"))).append(", longitude = ")
					.append(UtilsFactory.makeValidVarchar(joLocation.getString("Lon"))).append(", modified_on = now() ")
					.append("WHERE mac_address = ").append(UtilsFactory.makeValidVarchar(joLocation.getString("Mac")));
			
			NodeManager.agentLogQueue(sbQuery.toString());

		} catch (Exception e) {
			LogManager.errorLog(e);
			
			throw e;
		} finally {
			UtilsFactory.clearCollectionHieracy( sbQuery );
		}
	}
	
	protected void finalize() throws Throwable {	  
	    // clean up code for this class here
		if (con != null) {
	    	con.close();
	    	con = null;
	    }
		super.finalize();	    
	}
}
