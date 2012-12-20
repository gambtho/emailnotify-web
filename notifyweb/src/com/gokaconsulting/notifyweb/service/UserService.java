package com.gokaconsulting.notifyweb.service;

import java.io.IOException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;

import com.gokaconsulting.notifyweb.dao.PMF;
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

	public boolean checkUserPassword(String user, String token) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			logger.info("Checking password");
			User tempUser = pm.getObjectById(User.class, user);
			return tempUser.checkPassword(token);
		} catch (Exception e) {
			return false;
		} finally {
			pm.close();
		}
	}
}
