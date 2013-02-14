package com.gokaconsulting.notifyweb.service;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.gokaconsulting.notifyweb.dao.PMF;
import com.gokaconsulting.notifyweb.gateway.Constants;
import com.gokaconsulting.notifyweb.model.Notification;
import com.gokaconsulting.notifyweb.model.User;

public class UserService {

	private final Logger logger = Logger.getLogger(UserService.class.getName());

	public UserService() {

	}

	public User validateUser(String user, String token) throws IOException {

		PersistenceManager pm = PMF.get().getPersistenceManager();
		User u = null;
		try {

			try {
				User tempUser = pm.getObjectById(User.class, user);
				if (tempUser != null) {
					u = tempUser;
					logger.info("Checking password for user: " + user);
					if (u.checkPassword(token)) {
						logger.info("Valid password for user: " + user);
						u.setLastLogin(new Date());
					} else {
						logger.warning("Invalid password submitted for user: "
								+ user);
						u = null;
					}
				}
			} catch (JDOObjectNotFoundException e) {
				logger.info("Creating user: " + user);
				u = new User(user, token, new Date());
				pm.makePersistent(u);
			}
			if (u != null) {
				return u;
			} else {
				throw new IOException();
			}
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Unable to validate user: ", e);
			throw new IOException();
		} finally {
			pm.close();
		}
	}

	public boolean checkUserPasswordforGet(String user, String token) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			logger.info("Checking password");
			User tempUser = pm.getObjectById(User.class, user);
			if (tempUser.checkPassword(token))
			{
				logger.info("Password validated");
				tempUser.setUnRead(0);
				return true;
			}
			else
			{
				logger.info("Incorrect password");
				return false;
			}
		} catch (Exception e) {
			return false;
		} finally {
			pm.close();
		}
	}
	
	public void deleteAllMessages(String user) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		
		Query q = pm.newQuery(Notification.class);
		q.setFilter("userEmail == user");
		q.setOrdering("sentDate desc");
		q.declareParameters("String user");

		@SuppressWarnings("unchecked")
		List<Notification> results = (List<Notification>) q.execute(user);
		logger.info("Count of emails found for user: " + user + " to be deleted is: "
				+ results.size());
		if (!results.isEmpty()) {
			for (Notification n : results) {
				logger.info("Deleting notification");
				pm.deletePersistent(n);
			}
		}
		pm.close();
		
		if(!Constants.isProd())
		{
			PersistenceManager mp = PMF.get().getPersistenceManager();
			
			Query u = mp.newQuery(Notification.class);
			u.setFilter("userEmail == user");
			u.setOrdering("sentDate desc");
			u.declareParameters("String user");

			@SuppressWarnings("unchecked")
			List<Notification> afterResults = (List<Notification>) u.execute(user);
			logger.info("Count of emails found for user: " + user + " after delete is: "
					+ afterResults.size());
			mp.close();
		}	
		
	}
}
