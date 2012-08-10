package com.gokaconsulting.notifyweb;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;

public class ImageServlet extends HttpServlet {
	private static final long serialVersionUID = 7335699430753606809L;
	private final Logger logger = Logger.getLogger(ImageServlet.class.getName());
	private BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
	
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		BlobKey blobKey = new BlobKey(req.getParameter("photoID"));
		blobstoreService.serve(blobKey, resp);
		logger.info("Returned image for key: " + blobKey.toString());
	}
}
