package com.gokaconsulting.notifyweb;

import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Date;

import javax.jdo.PersistenceManager;
import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.gokaconsulting.notifyweb.PMF;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class MailHandlerServlet extends HttpServlet {
	private static final long serialVersionUID = 6815532253864651508L;
	private final Logger logger = Logger.getLogger(MailHandlerServlet.class
			.getName());

	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		Properties props = new Properties();
		Session session = Session.getDefaultInstance(props, null);
		try {
			MimeMessage message = new MimeMessage(session, req.getInputStream());

			String subject = message.getSubject();
			String userEmail = " ";
			Date sentDate = message.getSentDate();;
//			Boolean highImportance;

			Address[] from = message.getFrom();
			String fromAddress = "";
			if (from.length > 0) {
				userEmail = from[0].toString();
			}

			logger.info("Receieved message from " + userEmail + " subject "
					+ subject);

			// get body and attachment
			// from
			// http://jeremyblythe.blogspot.com/2009/12/gae-128-fixes-mail-but-not-jaxb.html
			Object content = message.getContent();

			String messageBody = "";
//			byte[] imageData = null;
			if (content instanceof String) {
				messageBody = (String) content;
			} else if (content instanceof Multipart) {
				Multipart multipart = (Multipart) content;
				Part part = multipart.getBodyPart(0);
				Object partContent = part.getContent();
				if (partContent instanceof String) {
					messageBody = (String) partContent;
				}
				// extract attached image if any
//				imageData = getMailAttachmentBytes(multipart);
			}
			
			Notification n = new Notification(fromAddress, userEmail, sentDate, 
					messageBody, subject);
			PersistenceManager pm = PMF.get().getPersistenceManager();

			try {
				pm.makePersistent(n);

				n.setId(n.getKey().getId());

				Gson gson = new GsonBuilder()
						.excludeFieldsWithoutExposeAnnotation().create();

				gson.toJson(n, resp.getWriter());
				resp.setContentType("application/json");

				if (n.getId() == null || n.getId() == 0) {
					logger.severe("Invalid notification id created for notification with subject: "
							+ n.getSubject());
				}
			} catch (Exception e) {
				logger.log(Level.SEVERE, "Unable to complete email post ", e);
				resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
						"Email post failed");
			} finally {
				pm.close();
			}
			
		} catch (MessagingException e) {
			logger.log(Level.SEVERE, "Error: ", e);
		}
	}
/*
	/**
	 * From http://java.sun.com/developer/onlineTraining/JavaMail/contents.html#
	 * JavaMailMessage
	 * 
	 * @param attachmentInputStream
	 * @param mimeMultipart
	 * @return image data from attachment or null if there is none
	 * @throws MessagingException
	 * @throws IOException
	 
	private byte[] getMailAttachmentBytes(Multipart mimeMultipart)
			throws MessagingException, IOException {
		InputStream attachmentInputStream = null;
		try {
			for (int i = 0, n = mimeMultipart.getCount(); i < n; i++) {
				String disposition = mimeMultipart.getBodyPart(i)
						.getDisposition();
				if (disposition == null) {
					continue;
				}
				if ((disposition.equals(Part.ATTACHMENT) || (disposition
						.equals(Part.INLINE)))) {
					attachmentInputStream = mimeMultipart.getBodyPart(i)
							.getInputStream();
					byte[] imageData = getImageDataFromInputStream(attachmentInputStream);
					return imageData;
				}
			}
		} finally {
			try {
				if (attachmentInputStream != null)
					attachmentInputStream.close();
			} catch (Exception e) {
			}
		}
		return null;
	}

	public byte[] getImageDataFromInputStream(InputStream inputStream) {
		BufferedInputStream bis = null;
		ByteArrayOutputStream bos = null;
		try {
			bis = new BufferedInputStream(inputStream);
			// write it to a byte[] using a buffer since we don't know the exact
			// image size
			byte[] buffer = new byte[1024];
			bos = new ByteArrayOutputStream();
			int i = 0;
			while (-1 != (i = bis.read(buffer))) {
				bos.write(buffer, 0, i);
			}
			byte[] imageData = bos.toByteArray();
			if (imageData.length > 1000000) {
				// throw new ImageTooLargeException("from email", 1000000);
			}
			return imageData;
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			try {
				if (bis != null)
					bis.close();
				if (bos != null)
					bos.close();
			} catch (IOException e) {
				// ignore
			}
		}
	}

	private String getText(Part p) throws MessagingException, IOException {

		boolean textIsHtml = false;

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
	*/
}