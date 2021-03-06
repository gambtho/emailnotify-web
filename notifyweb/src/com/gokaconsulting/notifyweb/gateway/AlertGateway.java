package com.gokaconsulting.notifyweb.gateway;

import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;

import org.apache.commons.codec.binary.Base64;

import com.gokaconsulting.notifyweb.dao.PMF;
import com.gokaconsulting.notifyweb.model.Notification;
import com.gokaconsulting.notifyweb.model.PushNotification;
import com.gokaconsulting.notifyweb.model.User;
import com.gokaconsulting.notifyweb.util.Constants;
import com.google.gson.Gson;

public class AlertGateway {

	private static final Logger logger = Logger.getLogger(AlertGateway.class
			.getName());

	public AlertGateway() {

	}

	private String getAuthString(Boolean isValidated, String userName) {
		if (isValidated == null || !isValidated) {
			logger.warning("Sending alert to non-validated user: " + userName);
			return Constants.getDevAuthString();
		} else {
			return Constants.getAuthString();
		}
	}

	public void sendAlert(Notification n, int unRead) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			if (unRead < 50) {

				User user = pm.getObjectById(User.class, n.getUserEmail());
				if (user != null) {

					URL url = new URL(Constants.URBAN_AIRSHIP_API);
					HttpURLConnection connection = (HttpURLConnection) url
							.openConnection();
					connection.setRequestMethod("POST");
					connection.setDoOutput(true);

					String authString = this.getAuthString(user.isValidated(),
							user.getUserAddress());

					String authStringBase64 = Base64
							.encodeBase64String(authString.getBytes());
					authStringBase64 = authStringBase64.trim();

					connection.setRequestProperty("Content-type",
							"application/json");
					connection.setRequestProperty("Authorization", "Basic "
							+ authStringBase64);

					Gson gson = new Gson();
					OutputStreamWriter osw = new OutputStreamWriter(
							connection.getOutputStream());
					gson.toJson(
							new PushNotification(n.getUserEmail(), n
									.getFromAddress() + ": " + n.getSubject(),
									"default", unRead), osw);
					// osw.write("{\"aps\":{\"alert\":\"New Mail!\", \"sound\": \"default\"}, \"aliases\": [\"thomas_gamble@homedepot.com\"]}");
					osw.close();

					int responseCode = connection.getResponseCode();
					logger.info("Notification submitted with response code: "
							+ responseCode);
					// logger.info(authString);
					logger.info(gson.toJson(new PushNotification(n
							.getUserEmail(), n.getFromAddress() + ": "
							+ n.getSubject(), "default", unRead)));
				} else {
					logger.info("User passed to notification was null");
				}
			} else {
				logger.warning(n.getUserEmail() + " has " + unRead
						+ " unread messages, notification not sent");
			}
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Error sending notification", e);
		}
	}

}
