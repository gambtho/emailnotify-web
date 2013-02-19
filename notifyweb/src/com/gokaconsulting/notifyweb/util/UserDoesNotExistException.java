package com.gokaconsulting.notifyweb.util;

public class UserDoesNotExistException extends Exception {

	private static final long serialVersionUID = 1L;
	
	private String user;
	
	
	public String getUser() {
		return user;
	}


	public void setUser(String user) {
		this.user = user;
	}


	public UserDoesNotExistException(String userEmail)
	{
		user = userEmail;
	}
}
