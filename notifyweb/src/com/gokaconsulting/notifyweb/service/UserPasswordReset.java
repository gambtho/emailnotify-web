package com.gokaconsulting.notifyweb.service;

public class UserPasswordReset extends Exception {

	private static final long serialVersionUID = 1L;
	private String user;
	
	public UserPasswordReset(String u)
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
