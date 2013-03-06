package com.gokaconsulting.notifyweb.web;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.gokaconsulting.notifyweb.service.PurgeService;


public class PurgeServlet extends HttpServlet {
	private static final long serialVersionUID = 6815532253864651508L;
	private static final Logger logger = Logger.getLogger(PurgeServlet.class.getName());

	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		logger.warning("Beginning purge process");
		PurgeService purgeService = new PurgeService();
		String days = req.getParameter("days");
		if(days!=null){
			purgeService.purge(Integer.parseInt(days));
		}
		else {
			logger.severe("Purging all messages");
			purgeService.purge();
		}	
	}
}