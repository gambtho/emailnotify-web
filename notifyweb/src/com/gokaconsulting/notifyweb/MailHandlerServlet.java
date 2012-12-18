package com.gokaconsulting.notifyweb;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Date;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.jsoup.Jsoup;

import com.gokaconsulting.notifyweb.PMF;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Text;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class MailHandlerServlet extends HttpServlet {
	private static final long serialVersionUID = 6815532253864651508L;
	private static final String legalString = "The information in this Internet Email is confidential and may be legally privileged. It is intended solely for the addressee. Access to this Email by anyone else is unauthorized. If you are not the intended recipient, any disclosure, copying, distribution or any action taken or omitted to be taken in reliance on it, is prohibited and may be unlawful. When addressed to our clients any opinions or advice contained in this Email are subject to the terms and conditions expressed in any applicable governing The Home Depot terms of business or client engagement letter. The Home Depot disclaims all responsibility and liability for the accuracy and content of this attachment and for any damages or losses arising from any inaccuracies, errors, viruses, e.g., worms, trojan horses, etc., or other items of a destructive nature, which may be contained in this attachment and shall not be liable for direct, indirect, consequential or special damage...";
	private static final String URBAN_AIRSHIP_API = "https://go.urbanairship.com/api/push/";

	// TODO: check environment if (SystemProperty.environment.value() ==
	// SystemProperty.Environment.Value.Production)
	private static final String AIRSHIP_ID = "Bcwsh30hSEm7rUgIw7Z4pQ";
	private static final String AIRSHIP_SECRET = "qKk0-kkTTz6AvMp4oJzXpQ";
	private boolean textIsHtml = false;
	private final Logger logger = Logger.getLogger(MailHandlerServlet.class
			.getName());

	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		Properties props = new Properties();
		Session session = Session.getDefaultInstance(props, null);
		try {
			MimeMessage message = new MimeMessage(session, req.getInputStream());

			String subject = message.getSubject();
			subject = subject.replaceFirst("FW:", "");
			String userEmail = " ";
			Date sentDate = message.getSentDate();
			String messageBody = "";

			Address[] toAddArray = message.getAllRecipients();
			String toAddress = null;
			if (toAddArray.length > 0) {
				toAddress = toAddArray[0].toString().toLowerCase();
			}

			logger.info("Before conversion to address: " + toAddress);

			int start = toAddress.indexOf('@');
			toAddress = toAddress.substring(0, start).trim().toLowerCase();

			// Boolean highImportance;

			Address[] from = message.getFrom();
			String fromAddress = null;
			if (from.length > 0) {
				userEmail = from[0].toString().toLowerCase();
			}

			logger.info("Receieved message from " + userEmail + " subject "
					+ subject + " sent to: " + toAddress);
			try {
				messageBody = getText(message);
			} catch (Exception e) {
				logger.log(Level.SEVERE, "Error getting message text: ", e);
			}

			if (textIsHtml) {
				if (messageBody != null) {
					logger.info("Message before jsoup: " + messageBody);

					messageBody = Jsoup.parse(
							messageBody.replaceAll("(?i)<br[^>]*>", "br2nl")
									.replaceAll("\n", "br2nl")).text();
					messageBody = messageBody.replaceAll("br2nl ", "\n")
							.replaceAll("br2nl", "\n").trim();

					logger.info("Message after jsoup: " + messageBody);
				} else {
					logger.info("Html with null message body");
				}
			}

			messageBody = messageBody.replaceAll(legalString, " ");
			messageBody = messageBody.replace(
					"________________________________", " ");

			String delims = "[\\s\\n]+";
			String[] tokens = messageBody.split(delims);
			for (int i = 0; i < tokens.length; i++) {
				// logger.info("Token: " + i + " is: " + tokens[i]);
				if (tokens[i].equalsIgnoreCase("From:")) {
					logger.info("From address found: " + tokens[i + 1]);
					fromAddress = tokens[i + 1];
					String atSymbol = "@";
					if (!fromAddress.contains(atSymbol)) {
						logger.info("Adding to the from address"
								+ tokens[i + 2]);
						fromAddress += " " + tokens[i + 2];
					}
					break;
				} else if (tokens[i].contains("From:")) {
					logger.info("Found from in token: " + tokens[i]);
					fromAddress = tokens[i + 1];
					String atSymbol = "@";
					if (!fromAddress.contains(atSymbol)) {
						fromAddress += " " + tokens[i + 2];
					}
					break;
				}
			}

			if (fromAddress != null) {
				fromAddress = fromAddress.replaceAll("<", "");
				fromAddress = fromAddress.replaceAll(">To:", "");
				fromAddress = fromAddress.trim();
			} else {
				fromAddress = userEmail;
			}
			Notification n = new Notification(fromAddress, userEmail, sentDate,
					messageBody, subject);
			PersistenceManager pm = PMF.get().getPersistenceManager();

			if (userExists(userEmail)) {

				if (toAddress.contentEquals("reset")) {
					logger.info("Delete requested for user: " + fromAddress);
					try {
						User u = pm.getObjectById(User.class, fromAddress);
						pm.deletePersistent(u);
						logger.info("Delete succesful for user: " + fromAddress);
					} catch (Exception e) {
						logger.log(Level.SEVERE, "Unable to delete user: "
								+ fromAddress, e);
						resp.sendError(
								HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
								"User: " + fromAddress
										+ " not found for deletion");
					} finally {
						pm.close();
					}
				} else {

					sendAlert(n);
					
					if (!toAddress.contentEquals("donotsave")) {
						logger.info("Saving message - " + toAddress);
						try {
							pm.makePersistent(n);

							n.setId(n.getKey().getId());

							Gson gson = new GsonBuilder()
									.excludeFieldsWithoutExposeAnnotation()
									.create();

							gson.toJson(n, resp.getWriter());
							resp.setContentType("application/json");

							if (n.getId() == null || n.getId() == 0) {
								logger.severe("Invalid notification id created for notification with subject: "
										+ n.getSubject());
							}
						} catch (Exception e) {
							logger.log(Level.SEVERE,
									"Unable to complete email save: ", e);
							resp.sendError(
									HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
									"Email post failed");
						} finally {
							pm.close();
						}
					}
					else
					{
						logger.info("Message not saved, but alert sent");
						pm.close();
					}
				}
			} else {
				resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
						"Email post failed");
			}

		} catch (MessagingException e) {
			logger.log(Level.SEVERE, "Error: ", e);
		}
	}

	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		logger.info("In doGet");
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation()
				.registerTypeAdapter(Text.class, new TextSerializer()).create();

		String user = req.getParameter("user").toLowerCase();

		if (user != null) {
			logger.info("Returning all emails for: " + user);

			Query q = pm.newQuery(Notification.class);
			q.setFilter("userEmail == user");
			q.setOrdering("sentDate desc");
			q.declareParameters("String user");

			try {
				@SuppressWarnings("unchecked")
				List<Notification> results = (List<Notification>) q
						.execute(user);
				logger.info("Count of emails found for user: " + user + " is: "
						+ results.size());
				if (!results.isEmpty()) {
					for (Notification n : results) {
						logger.info("Notification ID is: " + n.getId()
								+ " subject is: " + n.getSubject());
						if (n.getId() == null) {
							// TODO: Figure out why this is happening
							logger.severe("Task id for task: "
									+ n.getKey().getId() + " was 0");
							n.setId(n.getKey().getId());
						}
					}
				}
				gson.toJson(results, resp.getWriter());
				resp.setContentType("application/json");
			} catch (Exception e) {
				logger.log(Level.SEVERE, "Error: ", e);
			} finally {
				pm.close();
			}
		} else {
			logger.warning("Get called without user");
		}

	}

	public void doDelete(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {

		PersistenceManager pm = PMF.get().getPersistenceManager();

		String id = req.getParameter("id");
		logger.info("NotificationId is: " + id);
		// String userID = req.getParameter("user");
		if (id != null && Integer.parseInt(id) > 0) {
			logger.info("Delete requested for notification: " + id);
			try {
				Key k = KeyFactory.createKey(
						Notification.class.getSimpleName(), Long.valueOf(id));
				Notification n = pm.getObjectById(Notification.class, k);

				pm.deletePersistent(n);
				logger.info("Delete succesful for Notification: " + id);
			} catch (Exception e) {
				logger.log(Level.SEVERE,
						"Unable to delete Notification: " + id, e);
				resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
						"Notification: " + id + " not found for deletion");
			} finally {
				pm.close();
			}
		} else {
			logger.log(Level.WARNING,
					"Delete request without valid notification parameter");
		}
		resp.setContentType("application/json");
	}

	private void sendAlert(Notification n) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			User user = pm.getObjectById(User.class, n.getUserEmail());
			if (user != null) {

				URL url = new URL(URBAN_AIRSHIP_API);
				HttpURLConnection connection = (HttpURLConnection) url
						.openConnection();
				connection.setRequestMethod("POST");
				connection.setDoOutput(true);

				String authString = AIRSHIP_ID + ":" + AIRSHIP_SECRET;
				String authStringBase64 = Base64.encodeBase64String(authString
						.getBytes());
				authStringBase64 = authStringBase64.trim();

				connection.setRequestProperty("Content-type",
						"application/json");
				connection.setRequestProperty("Authorization", "Basic "
						+ authStringBase64);

				Gson gson = new Gson();
				OutputStreamWriter osw = new OutputStreamWriter(
						connection.getOutputStream());
				gson.toJson(
						new PushNotificationObj(n.getUserEmail(), n
								.getFromAddress() + ": " + n.getSubject(),
								"default"), osw);
				// osw.write("{\"aps\":{\"alert\":\"New Mail!\", \"sound\": \"default\"}, \"aliases\": [\"thomas_gamble@homedepot.com\"]}");
				osw.close();

				int responseCode = connection.getResponseCode();
				logger.info("Notification submitted with response code: "
						+ responseCode);
				logger.info(authString);
				logger.info(gson.toJson(new PushNotificationObj(n
						.getUserEmail(), "New Email: " + n.getSubject(),
						"default")));
			}
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Error sending notification", e);
		}
	}

	private String getText(Part p) throws MessagingException, IOException {
		if (p.isMimeType("text/*")) {
			String s = (String) p.getContent();
			textIsHtml = p.isMimeType("text/html");
			return s;
		}

		if (p.isMimeType("multipart/alternative")) {
			// prefer html text over plain text
			Multipart mp = (Multipart) p.getContent();
			String text = null;
			for (int i = 0; i < mp.getCount(); i++) {
				Part bp = mp.getBodyPart(i);
				if (bp.isMimeType("text/plain")) {
					if (text == null)
						text = getText(bp);
					continue;
				} else if (bp.isMimeType("text/html")) {
					String s = getText(bp);
					if (s != null)
						return s;
				} else {
					return getText(bp);
				}
			}
			return text;
		} else if (p.isMimeType("multipart/*")) {
			Multipart mp = (Multipart) p.getContent();
			for (int i = 0; i < mp.getCount(); i++) {
				String s = getText(mp.getBodyPart(i));
				if (s != null)
					return s;
			}
		}
		return null;
	}

	private class TextSerializer implements JsonSerializer<Text> {
		public JsonElement serialize(Text src, Type typeOfSrc,
				JsonSerializationContext context) {
			return new JsonPrimitive(src.getValue());
		}
	}

	private boolean userExists(String user) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			User tempUser = pm.getObjectById(User.class, user);
			if (tempUser != null) {
				return true;
			}
		} catch (Exception e) {
			logger.log(Level.WARNING,
					"Error retrieving user for received mail", e);
		}
		return false;
	}
}