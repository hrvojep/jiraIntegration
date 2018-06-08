package org.jasig.cas.client.integration.atlassian;

import java.util.Enumeration;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JiraConfluenceUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(Jira44CasAuthenticator.class);

	
	public static void logSessionAndCookies(final HttpServletRequest request){
		log("------>Logging session and cookie info<----");
		log("sessionId:"+request.getRequestedSessionId());
		Enumeration<String> attributeNames = request.getSession().getAttributeNames();
		while(attributeNames.hasMoreElements()){
			String sessionAtributeName= attributeNames.nextElement();
			log(sessionAtributeName + ":" + request.getSession().getAttribute(sessionAtributeName).toString());
		}
		log("cookieInfo"+request.getRequestedSessionId());
		Cookie[] cookies = request.getCookies();
		if (cookies!=null){
			for (int i = 0; i < cookies.length; i++) {
			  String name = cookies[i].getName();
			  String value = cookies[i].getValue();
			  log(name + ":" + value);
			}
		}
		log("------>Finished Logging session and cookie info<----");
		log("");

	}
	
	
	private static void log(String logMessage){
		LOGGER.error(logMessage);
	}
	
	private static void log(String logMessage, Exception ex){
		LOGGER.error(logMessage, ex);
	}


	
}
