package com.appedo.wpt.scheduler.manager;

import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.concurrent.PriorityBlockingQueue;

import com.appedo.manager.LogManager;
import com.appedo.wpt.scheduler.bean.SUMTestBean;

public class SUMScheduler {
	
	private static Hashtable<String, PriorityBlockingQueue<SUMTestBean>> htSUMLocationTestQueues = new Hashtable<String, PriorityBlockingQueue<SUMTestBean>>();
	
	public static SUMTestBean queueSUMTest(String strLocation, SUMTestBean testBean) throws Exception {
		PriorityBlockingQueue<SUMTestBean> pqSUMLocation = null;
		
		try {
			if (htSUMLocationTestQueues.containsKey(strLocation)) {
				pqSUMLocation = htSUMLocationTestQueues.get(strLocation);
			} else {
				pqSUMLocation = new PriorityBlockingQueue<SUMTestBean>();
				htSUMLocationTestQueues.put(strLocation, pqSUMLocation);
			}
			synchronized ( pqSUMLocation ) {
				// queue needs to compare this bean with others to find priority
				testBean.setQueuedOn(new Date());
				pqSUMLocation.add(testBean);
				return testBean;
			}
		} catch (Exception e) {
			LogManager.errorLog(e);
			throw e;
		}
	}
	
	public static SUMTestBean pollSUMTest(String strLocation) throws Exception {
		PriorityBlockingQueue<SUMTestBean> pqSUMLocation = null;
		SUMTestBean sumTestBean = null;
		
		try{
			// a client is available in a location. Add a queue for it; if not available
			if (htSUMLocationTestQueues.containsKey(strLocation)) {
				pqSUMLocation = htSUMLocationTestQueues.get(strLocation);
//				NodeManager nodeManager = new NodeManager();
//				nodeManager.updateSumlog(pqSUMLocation.element(),strLocation);
			} else {
				pqSUMLocation = new PriorityBlockingQueue<SUMTestBean>();
				htSUMLocationTestQueues.put(strLocation, pqSUMLocation);
			}
			
			synchronized ( pqSUMLocation ) {
				sumTestBean = pqSUMLocation.poll();
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
		PriorityBlockingQueue<SUMTestBean> pqSUMLocation = null;
		
		try{
			// a client is available in a location. Add a queue for it; if not available
			if (htSUMLocationTestQueues.containsKey(strLocation)) {
				pqSUMLocation = htSUMLocationTestQueues.get(strLocation);
			} else {
				pqSUMLocation = new PriorityBlockingQueue<SUMTestBean>();
				htSUMLocationTestQueues.put(strLocation, pqSUMLocation);
			}
			
			synchronized ( pqSUMLocation ) {
				ArrayList<SUMTestBean> sumTestBeans = new ArrayList<SUMTestBean>();
				pqSUMLocation.drainTo(sumTestBeans);
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
