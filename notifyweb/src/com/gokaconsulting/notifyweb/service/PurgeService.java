package com.gokaconsulting.notifyweb.service;

import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.gokaconsulting.notifyweb.dao.PMF;
import com.gokaconsulting.notifyweb.model.Notification;

public class PurgeService {

	private static final Logger logger = Logger.getLogger(PurgeService.class.getName());
	
	public PurgeService() {

	}

	public void purge() {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		logger.info("Beginning purge process");
		try {
			Date dateParam = new Date();
			Calendar c = Calendar.getInstance();
			c.setTime(dateParam);
			c.add(Calendar.DATE, -1);
			dateParam = c.getTime();

			Query q = pm.newQuery(Notification.class);
			q.setFilter("sentDate < dateParam");
			q.declareImports("import java.util.Date");
			q.declareParameters("Date dateParam");
			q.deletePersistentAll(dateParam);
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Error purging old notifications", e);
		} finally {
			logger.warning("Purge process completed");
			pm.close();
		}
	}
}
