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
		LinkedHashSet<SUMTestBean> hsSUMLocation = null;
		Iterator<SUMTestBean> iterSUMTests = null;
		SUMTestBean sumTestBeanItr = null;
		
		boolean bReturn = false, bAdd = false, bTestPresent = false;
		
		try {
			// Get the HashSet of the location
			synchronized (htSUMLocationTestQueues) {
				if (htSUMLocationTestQueues.containsKey(strLocation)) {
					hsSUMLocation = htSUMLocationTestQueues.get(strLocation);
				} else {
					hsSUMLocation = new LinkedHashSet<SUMTestBean>();
					htSUMLocationTestQueues.put(strLocation, hsSUMLocation);
				}
			}
			
			// Queue the TestBean when,
			// 1. Location HashSet is blank.
			// 2. Location HashSet does't have this Test-Id
			// 3. Location HashSet has the same Test-Id, but added 2 minutes back.
			synchronized (hsSUMLocation) {
				testBean.setQueuedOn(new Date());
				
				if( hsSUMLocation.size() == 0 ) {
					// 1. Location HashSet is blank.
					bAdd = true;
				} else {
					// verify whether the Test is already Queued, within last 2 minutes
					iterSUMTests = hsSUMLocation.iterator();
					
					while( iterSUMTests.hasNext() ) {
						sumTestBeanItr = iterSUMTests.next();
						
						if ( testBean.getTestId() == sumTestBeanItr.getTestId() ) {			// Check whether same Test is available in the Queue
							if (testBean.getQueuedOn().getTime() > (sumTestBeanItr.getQueuedOn().getTime() + 120000l) ) {	// Check for the Queued time.
								// 3. Location HashSet has the same Test-Id, but added 2 minutes back.
								bAdd = true;
							}
							
							// 2. Location HashSet does't have this Test-Id
							bTestPresent = true;
							
							break;
						}
					}
				}
				
				// Queue the TestBean when one of the above condition is satisfied
				if( bAdd || !bTestPresent ) {
					bReturn = hsSUMLocation.add(testBean);
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
				if(setSUMLoc.iterator().hasNext()){
					sumTestBean = setSUMLoc.iterator().next();
					//setSUMLoc.iterator().remove();
					setSUMLoc.remove(sumTestBean);
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
