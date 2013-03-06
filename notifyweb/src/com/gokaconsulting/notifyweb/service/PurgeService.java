package com.gokaconsulting.notifyweb.service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.gokaconsulting.notifyweb.dao.PMF;
import com.gokaconsulting.notifyweb.model.Notification;
import com.gokaconsulting.notifyweb.model.User;

public class PurgeService {

	private static final Logger logger = Logger.getLogger(PurgeService.class
			.getName());

	public PurgeService() {

	}

	public void purge() {
		logger.info("Purging all messages");
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			Date dateParam = new Date();
			Calendar c = Calendar.getInstance();
			c.setTime(dateParam);
			c.add(Calendar.DATE, 0);
			dateParam = c.getTime();

			Query q = pm.newQuery(Notification.class);
			q.setFilter("sentDate < dateParam");
			q.declareImports("import java.util.Date");
			q.declareParameters("Date dateParam");
			q.deletePersistentAll(dateParam);
			resetUnread();
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Error purging old notifications", e);
		} finally {
			logger.warning("Purge process completed");
			pm.close();
		}
	}

	public void purge(int daysOld) {
		logger.info("Purging all messages more than " + daysOld + " days old");
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			Date dateParam = new Date();
			Calendar c = Calendar.getInstance();
			c.setTime(dateParam);
			c.add(Calendar.DATE, daysOld);
			dateParam = c.getTime();

			Query q = pm.newQuery(Notification.class);
			q.setFilter("sentDate < dateParam");
			q.declareImports("import java.util.Date");
			q.declareParameters("Date dateParam");
			q.deletePersistentAll(dateParam);
			resetUnread();
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Error purging old notifications", e);
		} finally {
			logger.warning("Purge process completed");
			pm.close();
		}
	}

	private void resetUnread() {
		PersistenceManager pm = PMF.get().getPersistenceManager();

		try {
			Query q = pm.newQuery(User.class);
			q.setFilter("unRead > 0");

			@SuppressWarnings("unchecked")
			List<User> results = (List<User>) q.execute();
			logger.info("Number of users with unread messages is: "
					+ results.size());
			if (!results.isEmpty()) {
				for (User u : results) {
					u.setUnRead(0);
					pm.makePersistent(u);
				}
			}
		} catch (Exception e) {
			logger.severe("Error resetting unRead during purge" + e);
		} finally {
			pm.close();
		}
	}

}
