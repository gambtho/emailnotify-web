package com.gokaconsulting.notifyweb;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Date;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.gokaconsulting.notifyweb.PMF;
import com.gokaconsulting.notifyweb.User;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class UserServlet extends HttpServlet {
	private static final long serialVersionUID = 6815532253864651508L;
	private final Logger logger = Logger.getLogger(UserServlet.class
			.getName());

	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {

		String user = req.getParameter("user");
		String token = req.getParameter("token");

		if (user == null || token == null) {
			logger.severe("User servlet accessed without user or token");
			resp.sendError(
					HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
					"User login failed");
		}

		else {
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
					Gson gson = new GsonBuilder()
							.excludeFieldsWithoutExposeAnnotation().create();

					gson.toJson(u, resp.getWriter());
					resp.setContentType("application/json");
				} else {
					resp.sendError(
							HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
							"User login failed");
				}
			} catch (Exception e) {
				logger.log(Level.SEVERE, "Unable to validate user: ", e);
				resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
						"User login failed");
			} finally {
				pm.close();
			}
		}
	}

}