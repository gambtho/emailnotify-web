package com.gokaconsulting.notifyweb;

import java.util.HashMap;

public class PushNotificationObj {

	
	public PushNotificationObj(String user, String alert, String sound)
	{
		this.aliases = user;
		this.aps = new HashMap<String, String>();
		aps.put("alert", alert);
		aps.put("sound", sound);
	}
	
	private String aliases;
	private HashMap<String, String> aps;
	
	public String getAliases() {
		return aliases;
	}
	public void setAliases(String aliases) {
		this.aliases = aliases;
	}

}
