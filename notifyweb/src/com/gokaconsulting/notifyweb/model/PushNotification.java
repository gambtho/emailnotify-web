package com.gokaconsulting.notifyweb.model;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class PushNotification {

	
	public PushNotification(String user, String alert, String sound, int badge)
	{
		this.aliases = Arrays.asList(user);		
		this.aps = new HashMap<String, String>();
	
		aps.put("sound", sound);
		aps.put("alert", alert);
		aps.put("badge", String.valueOf(badge));
	}
	
	private HashMap<String, String> aps;
	private List<String> aliases;
	
	public List<String> getAliases() {
		return aliases;
	}
	
	public void setAliases(List<String> aliases) {
		this.aliases = aliases;
	}

}
