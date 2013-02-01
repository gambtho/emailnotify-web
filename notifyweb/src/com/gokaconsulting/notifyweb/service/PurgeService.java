package com.gokaconsulting.notifyweb.service;

import java.util.Calendar;
import java.util.Date;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.gokaconsulting.notifyweb.dao.PMF;
import com.gokaconsulting.notifyweb.model.Notification;

public class PurgeService {
	
	
	public PurgeService()
	{
		
	}
	
	public void purge()
	{
		PersistenceManager pm = PMF.get().getPersistenceManager();
		
		Date dateParam = new Date();
		Calendar c = Calendar.getInstance(); 
		c.setTime(dateParam); 
		c.add(Calendar.DATE, -1);
		dateParam = c.getTime();
		
		Query q = pm.newQuery(Notification.class);
		q.setFilter("sentDate < dateParam");
		q.declareParameters("date dateParam");
		q.deletePersistentAll(dateParam);
	}
}
