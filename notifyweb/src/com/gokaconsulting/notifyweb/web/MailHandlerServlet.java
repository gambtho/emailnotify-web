package com.gokaconsulting.notifyweb.web;

import java.io.IOException;

import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.lang.reflect.Type;

import javax.mail.Session;
import javax.mail.internet.MimeMessage;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.StringUtils;

import com.gokaconsulting.notifyweb.model.Notification;
import com.gokaconsulting.notifyweb.service.MailService;
import com.gokaconsulting.notifyweb.service.UserService;

import com.google.appengine.api.datastore.Text;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class MailHandlerServlet extends HttpServlet {
	private static final long serialVersionUID = 6815532253864651508L;

	private final Logger logger = Logger.getLogger(MailHandlerServlet.class
			.getName());

	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		Properties props = new Properties();
		Session session = Session.getDefaultInstance(props, null);
		MailService mailService = new MailService();

		try {

			MimeMessage message = new MimeMessage(session, req.getInputStream());

			Notification n = mailService.processEmail(message);

			Gson gson = new GsonBuilder()
					.excludeFieldsWithoutExposeAnnotation().create();
			gson.toJson(n, resp.getWriter());

		} catch (Exception e) {
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
					"Email post failed");
			logger.log(Level.SEVERE, "Error: processing email post", e);
		}
		resp.setContentType("application/json");
	}

	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		logger.info("In doGet");
		Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation()
				.registerTypeAdapter(Text.class, new TextSerializer()).create();
		MailService mailService = new MailService();
		UserService userService = new UserService();

		String user = req.getParameter("user");
		String token = req.getParameter("token");

		if (user != null && token != null) {

			if (userService.checkUserPassword(user, token)) {
				List<Notification> results = mailService.getNotifications(user
						.toLowerCase());
				gson.toJson(results, resp.getWriter());
				resp.setContentType("application/json");
			} else {
				logger.warning("Get called with invalid password for " + user);
				resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
						"Incorrect password");
			}
		} else {
			logger.warning("Get called without user and token");
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
					"Invalid parameters provided");
		}

	}

	public void doDelete(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {

		MailService mailService = new MailService();
		String id = req.getParameter("id");
		// String userID = req.getParameter("user");

		if (id != null && Integer.parseInt(id) > 0) {
			mailService.deleteNotification(id);
		} else {
			logger.log(Level.WARNING,
					"Delete request without valid notification parameter");
		}
		resp.setContentType("application/json");
	}

	private class TextSerializer implements JsonSerializer<Text> {
		public JsonElement serialize(Text src, Type typeOfSrc,
				JsonSerializationContext context) {
			return new JsonPrimitive(StringUtils.newStringUtf8(Base64
					.decodeBase64(src.getValue())));
		}
	}
}