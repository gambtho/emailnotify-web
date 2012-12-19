package com.gokaconsulting.notifyweb.web;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.gokaconsulting.notifyweb.model.User;
import com.gokaconsulting.notifyweb.service.UserService;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class UserServlet extends HttpServlet {
	private static final long serialVersionUID = 6815532253864651508L;
	private final Logger logger = Logger.getLogger(UserServlet.class.getName());

	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {

		String user = req.getParameter("user").toLowerCase();
		String token = req.getParameter("token");

		if (user == null || token == null) {
			logger.severe("User servlet accessed without user or token");
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
					"User login failed");
		}

		else {

			User u = null;
			UserService userService = new UserService();
			try {
				u = userService.validateUser(user, token);
				Gson gson = new GsonBuilder()
						.excludeFieldsWithoutExposeAnnotation().create();

				gson.toJson(u, resp.getWriter());
				resp.setContentType("application/json");
			} catch (Exception e) {
				resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
						"User login failed");
			}
		}
	}

}