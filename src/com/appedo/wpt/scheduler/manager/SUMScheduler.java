package com.appedo.wpt.scheduler.manager;

import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashSet;

import com.appedo.manager.LogManager;
import com.appedo.wpt.scheduler.bean.SUMTestBean;

public class SUMScheduler {
	
	private static Hashtable<String, LinkedHashSet<SUMTestBean>> htSUMLocationTestQueues = new Hashtable<String, LinkedHashSet<SUMTestBean>>();
	
	
	public static boolean queueSUMTest(String strLocation, SUMTestBean testBean) throws Exception {
		LinkedHashSet<SUMTestBean> setSUMLoc = null;
		Iterator<SUMTestBean> iterSUMTests = null;
		SUMTestBean sumTestBean = null;
		
		boolean bReturn = false;
		
		try {
			// Get the HashSet of the location
			synchronized (htSUMLocationTestQueues) {
				if (htSUMLocationTestQueues.containsKey(strLocation)) {
					setSUMLoc = htSUMLocationTestQueues.get(strLocation);
				} else {
					setSUMLoc = new LinkedHashSet<SUMTestBean>();
					htSUMLocationTestQueues.put(strLocation, setSUMLoc);
				}
			}
			
			// Queue the TestBean
			synchronized (setSUMLoc) {
				testBean.setQueuedOn(new Date());
				
				// verify whether the Test is already Queued, within last 2 minutes
				iterSUMTests = setSUMLoc.iterator();
				while( iterSUMTests.hasNext() ) {
					sumTestBean = iterSUMTests.next();
					
					if( testBean.getTestId() == sumTestBean.getTestId() 			// Check whether same Test is available in the Queue
							&& testBean.getQueuedOn().getTime() > (sumTestBean.getQueuedOn().getTime() + 120000l) ) {	// Check for the Queued time.
						bReturn = setSUMLoc.add(testBean);
					}
				}
			}
		} catch (Exception e) {
			LogManager.errorLog(e);
			throw e;
		}
		
		return bReturn;
	}
	
	public static SUMTestBean pollSUMTest(String strLocation) throws Exception {
		LinkedHashSet<SUMTestBean> setSUMLoc = null;
		SUMTestBean sumTestBean = null;
		try{
			// a client is available in a location. Add a queue for it; if not available
			if (htSUMLocationTestQueues.containsKey(strLocation)) {
				setSUMLoc = htSUMLocationTestQueues.get(strLocation);
			} else {
				setSUMLoc = new LinkedHashSet<SUMTestBean>();
				htSUMLocationTestQueues.put(strLocation, setSUMLoc);
			}
			
			synchronized ( setSUMLoc ) {
				sumTestBean = setSUMLoc.iterator().next();
				//setSUMLoc.iterator().remove();
				setSUMLoc.remove(sumTestBean);
				if( sumTestBean != null ){
				}
				return sumTestBean;
			}
		} catch (Exception e) {
			LogManager.errorLog(e);
			throw e;
		}
	}
	
	public static ArrayList<SUMTestBean> drainSUMTest(String strLocation) throws Exception {
		LinkedHashSet<SUMTestBean> setSUMLoc = null;
		try{
			// a client is available in a location. Add a queue for it; if not available
			if (htSUMLocationTestQueues.containsKey(strLocation)) {
				setSUMLoc = htSUMLocationTestQueues.get(strLocation);
			} else {
				setSUMLoc = new LinkedHashSet<SUMTestBean>();
				htSUMLocationTestQueues.put(strLocation, setSUMLoc);
			}

			synchronized ( setSUMLoc ) {
				ArrayList<SUMTestBean> sumTestBeans = new ArrayList<SUMTestBean>(setSUMLoc);
				setSUMLoc.clear();
				for(int i=0; i<sumTestBeans.size(); i++){
				}
				return sumTestBeans;
			}
		} catch (Exception e) {
			LogManager.errorLog(e);
			throw e;
		}
	}
}
