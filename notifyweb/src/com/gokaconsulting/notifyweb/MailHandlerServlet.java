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

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.gokaconsulting.notifyweb.PMF;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class MailHandlerServlet extends HttpServlet {
	private static final long serialVersionUID = 6815532253864651508L;
	private static final String legalString =  "The information in this Internet Email is confidential and may be legally privileged. It is intended solely for the addressee. Access to this Email by anyone else is unauthorized. If you are not the intended recipient, any disclosure, copying, distribution or any action taken or omitted to be taken in reliance on it, is prohibited and may be unlawful. When addressed to our clients any opinions or advice contained in this Email are subject to the terms and conditions expressed in any applicable governing The Home Depot terms of business or client engagement letter. The Home Depot disclaims all responsibility and liability for the accuracy and content of this attachment and for any damages or losses arising from any inaccuracies, errors, viruses, e.g., worms, trojan horses, etc., or other items of a destructive nature, which may be contained in this attachment and shall not be liable for direct, indirect, consequential or special damage...";
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
			String userEmail = " ";
			Date sentDate = message.getSentDate();
			String messageBody = "";
			
//			Boolean highImportance;

			Address[] from = message.getFrom();
			String fromAddress = "";
			if (from.length > 0) {
				userEmail = from[0].toString();
			}

			logger.info("Receieved message from " + userEmail + " subject "
					+ subject);
			try 
			{
				messageBody = getText(message);
			}
			catch (Exception e)
			{
				logger.log(Level.SEVERE, "Error getting message text: ", e);
			}
/*			
			// get body and attachment
			// from
			// http://jeremyblythe.blogspot.com/2009/12/gae-128-fixes-mail-but-not-jaxb.html
			Object content = message.getContent();

			
//			byte[] imageData = null;
			if (content instanceof String) {
				messageBody = (String) content;
			} else if (content instanceof Multipart) {
				Multipart multipart = (Multipart) content;
				int partCount = multipart.getCount();
				
				logger.info("Number of parts: " + partCount);
				
				Part part = multipart.getBodyPart(0);
				Part part2 = multipart.getBodyPart(1);
				Object partContent = part.getContent();
				if (partContent instanceof String) {
					messageBody = (String) partContent;
					logger.info("In part 1: " + messageBody);
				}
				Object partContent2 = part2.getContent();
				if(partContent instanceof String) {
					logger.info("In part 2: " + (String)partContent2);
				}
				// extract attached image if any
//				imageData = getMailAttachmentBytes(multipart);
			}
*/			
			if(textIsHtml)
			{
				if(messageBody!=null)
				{
					logger.info("Message before jsoup: " + messageBody);
					
				    messageBody = Jsoup.parse(messageBody.replaceAll("(?i)<br[^>]*>", "br2nl").replaceAll("\n", "br2nl")).text();
				    messageBody = messageBody.replaceAll("br2nl ", "\n").replaceAll("br2nl", "\n").trim();
					
					logger.info("Message after jsoup: " + messageBody);
				}
				else 
				{
					logger.info("Html with null message body");
				}
			}
			
			messageBody = messageBody.replace(legalString, " ");
			messageBody = messageBody.replace("________________________________", " ");
			
			String delims = "[ ]+";
			String[] tokens = messageBody.split(delims);
			for (int i = 0; i < tokens.length; i++)
			{
//			    logger.info("Token: " + i + " is: " + tokens[i]);
			    if(tokens[i].equalsIgnoreCase("From:"))
			    {
			    	logger.info("From address found: " + tokens[i+1]);
			    	fromAddress = tokens[i+1];
			    }
			    else if(tokens[i].contains("From:"))
			    {
			    	logger.info("Found from in token: " + tokens[i]);
			    	fromAddress = tokens[i+1];
			    }
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
				logger.log(Level.SEVERE, "Unable to complete email save: ", e);
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
*/
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
}