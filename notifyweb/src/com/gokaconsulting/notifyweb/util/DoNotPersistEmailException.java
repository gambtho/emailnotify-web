package com.gokaconsulting.notifyweb.util;

import com.gokaconsulting.notifyweb.model.Notification;

public class DoNotPersistEmailException extends Exception {

	private static final long serialVersionUID = 1L;

	private Notification notification;
	
	public DoNotPersistEmailException(Notification n)
	{
		notification = n;
	}
	
	public Notification getNotification() {
		return notification;
	}

	public void setNotification(Notification notification) {
		this.notification = notification;
	}

}
