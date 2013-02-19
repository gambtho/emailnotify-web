package com.gokaconsulting.notifyweb.util;

public class UserPasswordResetException extends Exception {

	private static final long serialVersionUID = 1L;
	private String user;
	
	public UserPasswordResetException(String u)
	{
		this.user = u;
	}
	
	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}
	
}
