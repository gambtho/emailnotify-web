package com.gokaconsulting.notifyweb.service;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.MimeMessage;

import org.jsoup.Jsoup;

import com.gokaconsulting.notifyweb.dao.PMF;
import com.gokaconsulting.notifyweb.gateway.AlertGateway;
import com.gokaconsulting.notifyweb.model.Notification;
import com.gokaconsulting.notifyweb.model.User;
import com.gokaconsulting.notifyweb.web.MailHandlerServlet;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

public class MailService {

	public MailService() {

	}

	private static final String legalString = "The information in this Internet Email is confidential and may be legally privileged. It is intended solely for the addressee. Access to this Email by anyone else is unauthorized. If you are not the intended recipient, any disclosure, copying, distribution or any action taken or omitted to be taken in reliance on it, is prohibited and may be unlawful. When addressed to our clients any opinions or advice contained in this Email are subject to the terms and conditions expressed in any applicable governing The Home Depot terms of business or client engagement letter. The Home Depot disclaims all responsibility and liability for the accuracy and content of this attachment and for any damages or losses arising from any inaccuracies, errors, viruses, e.g., worms, trojan horses, etc., or other items of a destructive nature, which may be contained in this attachment and shall not be liable for direct, indirect, consequential or special damage...";
	
	// TODO: check environment if (SystemProperty.environment.value() ==
	// SystemProperty.Environment.Value.Production)

	private boolean textIsHtml = false;
	private final Logger logger = Logger.getLogger(MailHandlerServlet.class
			.getName());

	public Notification processEmail(MimeMessage message)
			throws MessagingException, IOException {

		Notification n = null;
		AlertGateway ag = new AlertGateway();
		
		try {
			n = parseMessage(message);
		} catch (UserDeleteException e) {
			deleteUser(e.getUser());
		} catch (DoNotPersistEmailException e) {
			ag.sendAlert(n);
			return n;
		}
		persistNotification(n);
		ag.sendAlert(n);
		return n;
	}

	public List<Notification> getNotifications(String userEmailAddress) {

		String user = userEmailAddress;

		logger.info("Returning all emails for: " + user);

		PersistenceManager pm = PMF.get().getPersistenceManager();

		Query q = pm.newQuery(Notification.class);
		q.setFilter("userEmail == user");
		q.setOrdering("sentDate desc");
		q.declareParameters("String user");
		
		@SuppressWarnings("unchecked")
		List<Notification> results = (List<Notification>) q.execute(user);
		logger.info("Count of emails found for user: " + user + " is: "
				+ results.size());
		if (!results.isEmpty()) {
			for (Notification n : results) {
				logger.info("Notification ID is: " + n.getId()
						+ " subject is: " + n.getSubject());
				if (n.getId() == null) {
					// TODO: Figure out why this is happening
					logger.severe("Task id for task: " + n.getKey().getId()
							+ " was 0");
					n.setId(n.getKey().getId());
				}
			}
		}
		pm.close();
		return results;
	}

	public void deleteNotification(String id) {

		PersistenceManager pm = PMF.get().getPersistenceManager();

		if (id != null && Integer.parseInt(id) > 0) {
			logger.info("Delete requested for notification: " + id);
			Key k = KeyFactory.createKey(Notification.class.getSimpleName(),
					Long.valueOf(id));
			Notification n = pm.getObjectById(Notification.class, k);

			pm.deletePersistent(n);
			logger.info("Delete succesful for Notification: " + id);

			pm.close();

		} else {
			logger.log(Level.WARNING,
					"Delete request without valid notification parameter");
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

	private void deleteUser(String userEmail) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		User u = pm.getObjectById(User.class, userEmail);
		pm.deletePersistent(u);
		logger.info("Delete succesful for user: " + userEmail);
		pm.close();
	}

	private void persistNotification(Notification n) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			logger.info("Saving message - ");
			pm.makePersistent(n);

			n.setId(n.getKey().getId());
		} catch (Exception e) {

		} finally {
			pm.close();
		}

	}
	
	private Notification parseMessage(MimeMessage message)
			throws MessagingException, IOException, UserDeleteException,
			DoNotPersistEmailException {

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

		messageBody = getText(message);

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
		messageBody = messageBody.replace("________________________________",
				" ");

		String delims = "[\\s\\n]+";
		String[] tokens = messageBody.split(delims);
		for (int i = 0; i < tokens.length; i++) {
			// logger.info("Token: " + i + " is: " + tokens[i]);
			if (tokens[i].equalsIgnoreCase("From:")) {
				logger.info("From address found: " + tokens[i + 1]);
				fromAddress = tokens[i + 1];
				String atSymbol = "@";
				if (!fromAddress.contains(atSymbol)) {
					logger.info("Adding to the from address" + tokens[i + 2]);
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

		if (userExists(userEmail)) {

			if (toAddress.contentEquals("reset")) {
				logger.info("Delete requested for user: " + fromAddress);
				throw new UserDeleteException(userEmail);
			} else {

				if (toAddress.contentEquals("donotsave")) {
					logger.info("Message received, but not persisted from: " + fromAddress);
					throw new DoNotPersistEmailException(n);
				}
			}
		}
		return n;
	}
}
