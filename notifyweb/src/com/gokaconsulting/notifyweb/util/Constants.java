package com.gokaconsulting.notifyweb.util;

import java.util.logging.Logger;

import com.google.appengine.api.utils.SystemProperty;

public class Constants {

	private final static Logger logger = Logger.getLogger(Constants.class.getName());
	
	private static String AIRSHIP_ID = null;
	private static String AIRSHIP_SECRET = null;
	
	private static Boolean isProd = null;
	
	private static final String PR_AIRSHIP_ID = "jr-hH4hkSQO2mWDNA6e8Ag";
	private static final String PR_AIRSHIP_SECRET = "lOZCrOuvR92P_wSpWgkgFQ";	
	
	private static final String DEV_AIRSHIP_ID = "Bcwsh30hSEm7rUgIw7Z4pQ";
	private static final String DEV_AIRSHIP_SECRET = "qKk0-kkTTz6AvMp4oJzXpQ";
	
	public static final String URBAN_AIRSHIP_API = "https://go.urbanairship.com/api/push/";
	
	private Constants()
	{
		
	}
	
	public static boolean isProd()
	{
		if (isProd == null) {
			if ((SystemProperty.environment.value() == SystemProperty.Environment.Value.Production)) {
				isProd = true;
			}
			else
			{
				isProd = false;
			}	
			logger.warning("System environment is " + SystemProperty.environment.value());
		}	
		return isProd;
	}
	
	public static String getAuthString()
	{
		if(AIRSHIP_ID == null || AIRSHIP_SECRET == null)
		{	
			if(isProd())
			{
				//TODO: switch this to PR
				logger.info("Using PR Airship credentials (current same as dev)");
				AIRSHIP_ID = PR_AIRSHIP_ID;
				AIRSHIP_SECRET = PR_AIRSHIP_SECRET;
			}
			else
			{
				AIRSHIP_ID = DEV_AIRSHIP_ID;
				AIRSHIP_SECRET = DEV_AIRSHIP_SECRET;
				logger.info("Using Dev Airship credentials");
			}
		}
		return AIRSHIP_ID + ":" + AIRSHIP_SECRET;
	}
	
	public static String getDevAuthString()
	{
		return DEV_AIRSHIP_ID + ":" + DEV_AIRSHIP_SECRET;
	}
}
