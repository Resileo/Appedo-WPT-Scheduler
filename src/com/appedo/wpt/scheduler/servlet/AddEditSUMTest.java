package com.appedo.wpt.scheduler.servlet;

import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.appedo.wpt.scheduler.bean.SUMTestBean;
import com.appedo.wpt.scheduler.connect.DataBaseManager;
import com.appedo.wpt.scheduler.dbi.SUMDBI;
import com.appedo.wpt.scheduler.manager.LogManager;
import com.appedo.wpt.scheduler.manager.SUMManager;

public class AddEditSUMTest extends HttpServlet {

	Connection con = null;
	public AddEditSUMTest() {
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
		try {
			con = DataBaseManager.giveConnection();
			SUMDBI sumdbi = new SUMDBI();
			SUMManager manager = new SUMManager();
			long test_id = Long.valueOf(request.getParameter("testid"));
			String status = request.getParameter("status");
			String delStatus = request.getParameter("delStatus");
			LogManager.infoLog("STATUS:::: "+status);
			
			ArrayList<SUMTestBean> alSUMTestBeans = new ArrayList<SUMTestBean>();
			if(delStatus != null){
				if (SUMManager.hmThreads.containsKey(test_id)) {
					for(int i = 0; i < SUMManager.hmThreads.get(test_id).size(); i++){
						SUMManager.hmThreads.get(test_id).get(i).setStopRun(false);
					}
				}
			}
			else{
				alSUMTestBeans = sumdbi.createNewThreadForTest(con, test_id, Boolean.valueOf(status));
			}
			
			for(int i=0; i<alSUMTestBeans.size(); i++){
				try {
					manager.runSUMTests((SUMTestBean) alSUMTestBeans.get(i).clone());
				} catch (Throwable e1) {
					LogManager.errorLog(e1);
				}
			}
		} catch (Throwable th) {
			LogManager.errorLog(th);
		} finally {
			DataBaseManager.close(con);
			con = null;
		}
	}

}
