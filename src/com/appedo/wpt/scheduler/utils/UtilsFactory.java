package com.appedo.wpt.scheduler.utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.appedo.wpt.scheduler.connect.DataBaseManager;
import com.appedo.wpt.scheduler.manager.LogManager;

/**
 * Class which do some utilization operation.
 * 
 * @author Ramkumar R
 *
 */
public class UtilsFactory {
	
	public final static long FREQUENCY_ONE_DAY = 1000 * 60 * 60 * 24;
	public final static long FREQUENCY_ONE_HOUR = 1000 * 60 * 60;
	public final static long FREQUENCY_TEN_MINUTES = 1000 * 60 * 10;
	public final static long FREQUENCY_FOUR_HOUR = 1000 * 60 * 60 * 4;
	
	public static HashMap<String, JSONObject> HM_VENDOR_COUNTERS = new HashMap<String, JSONObject>();
	
	/**
	 * Returns the given yyyy-MM-dd HH:mm:ss format date-time in long value.
	 * 
	 * @param strTimeStamp
	 * @return
	 */
	public static Long formatTimestampToLong(String strTimeStamp){
		Long opDate = 0l;
		try{
			DateFormat ipFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
			opDate = ipFormatter.parse(strTimeStamp).getTime();
		}catch(Exception e){
			LogManager.errorLog(e);
		}
		return opDate;
	}
	
	/**
	 * Returns the given date-time in yyyy-MM-dd HH:mm:ss format.
	 * 
	 * @param lTime
	 * @return
	 */
	public static String formatDateToTimestamp(Long lTime){
		String opDate = "";
		try{
			DateFormat opFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
			opDate = opFormatter.format(new Date(lTime));
		}catch(Exception e){
			LogManager.errorLog(e);
		}
		return opDate;
	}
	
	/**
	 * Returns 'val2' if 'val1' is null; else return 'val1'
	 * 
	 * @param val1 
	 * @param val2 
	 * @return String
	 */
	public static String replaceNull(Object val1, String val2) {
		if (val1 == null)
			return val2;
		else
			return val1.toString();
	}
	
	/**
	 * Returns 'val2' if 'val1' is null or if blank; else return 'val1'
	 * 
	 * @param val1
	 * @param val2
	 * @return
	 */
	public static String replaceNullBlank(String val1, String val2) {
		if ( val1 == null || val1.length() == 0 )
			return val2;
		else
			return val1.toString();
	}
	
	/**
	 * Returns a JSONObject which can be used for a request's successful response, with the given message.
	 * 
	 * @param strMessage
	 * @return
	 */
	public static JSONObject getJSONSuccessReturn( String strMessage ){
		JSONObject joReturn = new JSONObject();
		
		joReturn.put("success", true);
		joReturn.put("failure", false);
		joReturn.put("message", strMessage);
		
		return joReturn;
	}
	
	/**
	 * Returns a JSONObject which can be used for a request's successful response, with the given message & successful db inserted counts
	 * 
	 * @param strMessage
	 * @param nInsertedCount
	 * @return
	 */
	public static JSONObject getJSONSuccessReturn( String strMessage, int nInsertedCount ){
		JSONObject joReturn = new JSONObject();
		
		joReturn.put("success", true);
		joReturn.put("failure", false);
		
		joReturn.put("message", strMessage);
		joReturn.put("inserted", nInsertedCount);
		
		return joReturn;
	}
	
	/**
	 * Returns a JSONObject which can be used for a request's successful response, with the given key-value pairs as message.
	 * 
	 * @param jo
	 * @return
	 */
	public static JSONObject getJSONSuccessReturn( JSONObject jo ){
		JSONObject joReturn = new JSONObject();
		
		joReturn.put("success", true);
		joReturn.put("failure", false);
		joReturn.put("message", jo);
		
		return joReturn;
	}
	
	/**
	 * Returns a JSONObject which can be used for a request's successful response, with the given JSONArray as message.
	 * 
	 * @param ja
	 * @return
	 */
	public static JSONObject getJSONSuccessReturn( JSONArray ja ){
		JSONObject joReturn = new JSONObject();
		
		joReturn.put("success", true);
		joReturn.put("failure", false);
		joReturn.put("message", ja);
		
		return joReturn;
	}
	
	/**
	 * Returns a JSONObject which can be used for a request's failure response with a message.
	 * 
	 * @param strMessage
	 * @return
	 */
	public static JSONObject getJSONFailureReturn( String strMessage ){
		JSONObject joReturn = new JSONObject(); 
		
		joReturn.put("success", false);
		joReturn.put("failure", true);
		joReturn.put("errorMessage", (""+strMessage).replaceAll("\"","\\\""));
		
		return joReturn;
	}
	
	/**
	 * Returns a JSONObject which can be used for a request's failure response with a message, and next focus-to field.
	 * 
	 * @param strMessage
	 * @param strFocusTo
	 * @return
	 */
	public static JSONObject getJSONFailureReturn( String strMessage, String strFocusTo){
		JSONObject joReturn = new JSONObject(); 
		
		joReturn.put("success", false);
		joReturn.put("failure", true);
		joReturn.put("errorMessage", ""+strMessage.replaceAll("\"","\\\""));
		joReturn.put("focusTo", strFocusTo);
		
		return joReturn;
	}
	
	/**
	 * Returns the input string as comfortable for SQL operations.
	 * 
	 * @param str
	 * @return
	 */
	public static String makeValidVarchar(String str) {
		StringBuilder sbValue = new StringBuilder();
		
		if( str == null )
			sbValue.append("null");
		else
			sbValue.append("'").append(str.replaceAll("'","''")).append("'");
		
		return sbValue.toString();
	}
	
	/**
	 * Close the given InputStream
	 * 
	 * @param is
	 * @return
	 */
	public static boolean close(InputStream is){
		try{
			if(is != null){
				is.close();
			}
		}catch(Exception e){
			LogManager.errorLog(e);
			return false;
		}
		return true;
	}
	
	/**
	 * Close the given InputStreamReader
	 * 
	 * @param isr
	 * @return
	 */
	public static boolean close(InputStreamReader isr){
		try{
			if(isr != null){
				isr.close();
			}
		}catch(Exception e){
			LogManager.errorLog(e);
			return false;
		}
		return true;
	}
	
	/**
	 * Close the given BufferedReader
	 * 
	 * @param reader
	 * @return
	 */
	public static boolean close(BufferedReader reader){
		try{
			if(reader != null){
				reader.close();
			}
		}catch(Exception e){
			LogManager.errorLog(e);
			return false;
		}
		return true;
	}
	

	/**
	 * Closes the nested collection variable.
	 * 
	 * @param objCollection
	 */
	public static void clearCollectionHieracy(Object objCollection){
		try{
			if( objCollection == null ){
				
			}else if( objCollection instanceof JSONObject ) {
				JSONObject joCollection = (JSONObject)objCollection;
				Iterator it = joCollection.keySet().iterator();
				while( it.hasNext() ){
					Object str = it.next();
					clearCollectionHieracy( joCollection.get(str) );
				}
				joCollection.clear();
				joCollection = null;
			}else if( objCollection instanceof JSONArray ) {
				JSONArray jaCollection = (JSONArray)objCollection;
				for( int i=0; i < jaCollection.size(); i++ ){
					clearCollectionHieracy( jaCollection.get(i) );
				}
				jaCollection.clear();
				jaCollection = null;
			}else if( objCollection instanceof Map ) {
				Map mapCollection = (Map)objCollection;
				Iterator it = mapCollection.keySet().iterator();
				while( it.hasNext() ){
					Object str = it.next();
					clearCollectionHieracy( mapCollection.get(str) );
				}
				mapCollection.clear();
				mapCollection = null;
			}else if( objCollection instanceof List ) {
				List listCollection = (List)objCollection;
				for( int i=0; i < listCollection.size(); i++ ){
					clearCollectionHieracy( listCollection.get(i) );
				}
				listCollection.clear();
				listCollection = null;
			}else if( objCollection instanceof StringBuilder ) {
				StringBuilder sbCollection = (StringBuilder)objCollection;
				sbCollection.setLength(0);
			}else if( objCollection instanceof StringBuffer ) {
				StringBuffer sbCollection = (StringBuffer)objCollection;
				sbCollection.setLength(0);
			}else if( objCollection instanceof Set ) {
				Set setCollection = (Set)objCollection;
				Object[] objSetCollections = setCollection.toArray();
				for( int i = 0; i < objSetCollections.length; i++ ){
					clearCollectionHieracy( objSetCollections[i] );
				}
				setCollection.clear();
				setCollection = null;
			}
			
			objCollection = null;
		}catch(Throwable t){
			LogManager.errorLog(t);
		}
	}
	
	/**
	 * Return 'val2' if 'val1' is null; else return 'val1' 
	 * This function should be used when db_column definition is "NOT NULL DEFAULT ''"
	 * 
	 * @param val1 
	 * @param val2 
	 * @return String
	 */
	public static String replaceNullWithQuote(Object val1, String val2) {
		if (val1 == null)
			return "'" + val2 +"'";
		else
			return "'" + val1.toString() + "'";
	}
	
	/**
	 * Returns the value inside quotes or as NULL to use in SQL query.
	 * 
	 * @param objValue
	 * @return
	 */
	public static StringBuilder coverWithSQLQuote(Object objValue) {
		StringBuilder sbValue = new StringBuilder();
		
		if( objValue == null)
			sbValue.append("null");
		else
			sbValue.append("'").append(objValue).append("'");
		
		return sbValue;
	}
	
	/**
	 * Paramterized method to sort Map e.g. HashMap or Hashtable in Java throw
	 * NullPointerException if Map contains null key
	 * 
	 * @param <K>
	 * @param <V>
	 * @param map
	 * @return
	 */
	public static <K extends Comparable, V extends Comparable> Map<K, V> sortByKeys(Map<K, V> map) {
		List<K> keys = new LinkedList<K>(map.keySet());
		Collections.sort(keys);

		// LinkedHashMap will keep the keys in the order they are inserted
		// which is currently sorted on natural ordering
		Map<K, V> sortedMap = new LinkedHashMap<K, V>();
		for (K key : keys) {
			sortedMap.put(key, map.get(key));
		}

		return sortedMap;
	}
	
	
	/**
	 * Gets available agents for the module (APPLICATION/SERVER/DATABASES) 
	 * 
	 * @param con
	 * @param strModuleName
	 * @return
	 * @throws Exception
	 */
	public static JSONArray getModuleAgents(Connection con, String strModuleName) throws Exception {
		PreparedStatement pstmt = null;
		ResultSet rst = null;
		
		StringBuilder sbQuery = new StringBuilder();
		
		JSONArray jaAgents = new JSONArray();
		JSONObject joAgent = null;
		
		try {
			sbQuery.append("SELECT * from agentversionmaster WHERE module_code = ? ");
			
			pstmt = con.prepareStatement(sbQuery.toString());
			pstmt.setString(1, strModuleName);
			rst = pstmt.executeQuery();
			while(rst.next()) {
				joAgent = new JSONObject();
				joAgent.put("agentVersionId", rst.getString("agent_version_id"));
				joAgent.put("moduleCode", rst.getString("module_code"));
				joAgent.put("agentName", rst.getString("agent_name"));
				joAgent.put("familyName", rst.getString("family_name"));
				
				jaAgents.add(joAgent);
			}
			
		} catch (Exception e) {
			LogManager.errorLog(e);
			
			throw e;
		} finally {
			DataBaseManager.close(rst);
			rst = null;
			DataBaseManager.close(pstmt);
			pstmt = null;
		}
		
		return jaAgents;
	}
}
