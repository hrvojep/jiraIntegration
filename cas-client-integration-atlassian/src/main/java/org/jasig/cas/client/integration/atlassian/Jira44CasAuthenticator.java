/*
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.cas.client.integration.atlassian;

import java.io.FileInputStream;
import java.security.Principal;
import java.util.Base64;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Properties;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

//import org.json.JSONException;
//import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.atlassian.jira.security.login.JiraSeraphAuthenticator;
import com.atlassian.jira.util.json.JSONException;
import com.atlassian.jira.util.json.JSONObject;
import com.atlassian.seraph.auth.AuthenticationContextAwareAuthenticator;
import com.atlassian.seraph.auth.AuthenticatorException;


/**
 * Extension of JiraSeraphAuthenticator to allow people to configure JIRA 4.4
 * and above to authenticate via Jasig CAS
 *
 */
@AuthenticationContextAwareAuthenticator
public final class Jira44CasAuthenticator extends JiraSeraphAuthenticator {

	/** Jira43CasAuthenticator.java */
	private static final long serialVersionUID = 3852011252741183166L;

	private static final Logger LOGGER = LoggerFactory.getLogger(Jira44CasAuthenticator.class);
	public static Properties appProps = new Properties();		
	static {		
		//String propertiesLocation="/tmp/jira/jira.properties";
		String propertiesLocation="/opt/atlassian/jira/conf/jira.properties";
		try{
			appProps.load(new FileInputStream(propertiesLocation));			
			LOGGER.error("Read the properties from:" + propertiesLocation + " v1");
			LOGGER.error("properties values:" + appProps.toString());
		}catch (Exception e){
			LOGGER.error("Cloud not read properties from:" + propertiesLocation);
			LOGGER.error(e.getMessage(),e);
		}
	}
	

	public Principal getUser(final HttpServletRequest request, final HttpServletResponse response) {
		// First, check to see if this session has already been authenticated
		// during a previous request.

		//LOGGER.error("---> We are here!");

		logSessionAndCookies(request);
		
		Principal existingUser = getUserFromSession(request);
		if (existingUser != null) {
			LOGGER.error("Session found; user already logged in. User name:" + existingUser.getName());
			return existingUser;
		}
		
		Enumeration<String> headerNames =request.getHeaderNames();
		while (headerNames.hasMoreElements()){			
			String headerName = headerNames.nextElement();
			if (headerName.equals("isf8uid")){
				LOGGER.error("->"+ headerName + ":-->"+request.getHeader(headerName));
			}
		}

		// final HttpSession session = request.getSession();
		// final Assertion assertion = (Assertion)
		// session.getAttribute(AbstractCasFilter.CONST_CAS_ASSERTION);
		//
		// if (assertion != null) {
		// final String username = assertion.getPrincipal().getName();
		// final Principal user = getUser(username);
		//
		// if (user != null) {
		// putPrincipalInSessionContext(request, user);
		// getElevatedSecurityGuard().onSuccessfulLoginAttempt(request,
		// username);
		// LoginReason.OK.stampRequestResponse(request, response);
		// LOGGER.debug("Logging in [{}] from CAS.", username);
		// } else {
		// LOGGER.debug("Failed logging [{}] from CAS.", username);
		// getElevatedSecurityGuard().onFailedLoginAttempt(request, username);
		// }
		// return user;
		// }
		
		String uidAsUrlParam = request.getParameter("isf8uid");


		//do we have ISF UID header, if we do, do stuff with it
		String uid=request.getHeader("isf8uid");
		if (uid !=null || uidAsUrlParam !=null){
			if (uid ==null){
				uid=uidAsUrlParam;
			}
			LOGGER.error("Got UID:" + uid);
			ISFJiraIdentifiers isfIdentifiers=null;
			try {
				isfIdentifiers = ISFUtils.getISFIdentifiers(ISFUtils.getXMLFromBase64String(uid));
			} catch (Exception e) {
				//LOGGER.error("Unable to extract ISF uid" + e);
				LOGGER.error("Unable to extract ISF uid:" + uid + " " + e);
			}
			
			String upn = isfIdentifiers.getUpn().trim();
			String email = isfIdentifiers.getEmail().trim();
			String abn = isfIdentifiers.getAbn().trim();
			String fullName = isfIdentifiers.getFullName();
			
			LOGGER.error("ISF identifiers: "  + uid + " upn='" + upn +"' email='" + email + "'");
			
			if (upn == null || email ==null){
				LOGGER.error("ISF uid or email null. ISF uid:"  + uid + " upn='" + upn +"' email='" + email + "'");
			//do the authentication or reqistration
			}else{
				//check if user exists, if it does, log them in and return. Happy days.
				try {
					boolean userExists= JiraAuthAndRegistrationUtils.userExistsInJira(email);
					LOGGER.error("Does user exist:" + email + ":" + userExists);
				if (userExists){
					return logInExistingUser(email,request);
				}
				//user does not exist. Register them and log them in on the fly.
				else{
					 LOGGER.error("Starting a workflow to create a new user in JIRA upn:'" + upn +"' email:'" + email + "'" );
					 
//					 LOGGER.error("**-->Start createNewJiraUser:" + upn + " :"+ email);
//					 JiraAuthAndRegistrationUtils.createNewJiraUser(upn,email);
//					 LOGGER.error("**-->Finish createNewJiraUser:" + upn + " :"+ email);
					 

//					 LOGGER.error("**-->Start addUserToExternalUsersGroup:" + upn + " :"+ email);
//					 JiraAuthAndRegistrationUtils.addUserToExternalUsersGroup(upn);
//					 LOGGER.error("**-->Finish addUserToExternalUsersGroup:" + upn + " :"+ email);

					 LOGGER.error("**-->Start createOrganisationinServiceDesk:" + abn);
					 String newOrgId = JiraAuthAndRegistrationUtils.createOrganisationinServiceDesk(abn);

					 LOGGER.error("**-->Start addOrganisationToServiceDesk:" + newOrgId);
					 JiraAuthAndRegistrationUtils.addOrganisationToServiceDesk(newOrgId);

					 LOGGER.error("**-->Start createCustomerInServiceDesk:" + upn + " :"+ email);
					 JiraAuthAndRegistrationUtils.createCustomerInServiceDesk(email,fullName);
					 
					 LOGGER.error("**-->Start addCustomerToOrganisation:" + upn + " :"+ email);
					 JiraAuthAndRegistrationUtils.addCustomerToOrganisation(newOrgId, email);

					 LOGGER.error("**-->Start addUidPropertyToUser:" + upn + " :"+ email);
					 JiraAuthAndRegistrationUtils.addUidPropertyToUser(email);
					 
					 LOGGER.error("Finished workflow to create a new user in JIRA upn:'" + upn +"' email:'" + email + "'" );
					 
					 //final step, log in new user
					 return logInExistingUser(email,request);

				}
				} catch (Exception e) {
					LOGGER.error("Something went wrong with the workflow:" + upn + " "+ e);
				    LOGGER.error(e.getMessage());
				}
				
			}
			
		}
		//LOGGER.error("---> Calling super");
		return super.getUser(request, response);
	}
	
	private void logSessionAndCookies(final HttpServletRequest request){
		LOGGER.error("------>Logging session and cookie info<----");
		LOGGER.error("sessionId:"+request.getRequestedSessionId());
		Enumeration<String> attributeNames = request.getSession().getAttributeNames();
		while(attributeNames.hasMoreElements()){
			String sessionAtributeName= attributeNames.nextElement();
			LOGGER.error(sessionAtributeName + ":" + request.getSession().getAttribute(sessionAtributeName).toString());
		}
		LOGGER.error("cookieInfo"+request.getRequestedSessionId());
		Cookie[] cookies = request.getCookies();
		if (cookies!=null){
			for (int i = 0; i < cookies.length; i++) {
			  String name = cookies[i].getName();
			  String value = cookies[i].getValue();
			  LOGGER.error(name + ":" + value);
			}
		}
		LOGGER.error("------>Finished Logging session and cookie info<----");
		LOGGER.error("");

	}
	
	
	private Principal logInExistingUser(String upn, final HttpServletRequest request){
		LOGGER.error("---> creating principal:" + upn);

		final String username = upn;
		final Principal principal = new Principal() {
			@Override
			public String getName() {
				return username;
			}
		};
		LOGGER.error("---> login existing user:" + upn);
	    putPrincipalInSessionContext(request, principal);
		getElevatedSecurityGuard().onSuccessfulLoginAttempt(request,username);
		return principal;
	}

	public boolean logout(final HttpServletRequest request, final HttpServletResponse response)
			throws AuthenticatorException {
		final HttpSession session = request.getSession();
		final Principal p = (Principal) session.getAttribute(LOGGED_IN_KEY);

		if (p != null) {
			LOGGER.error("Logging out from Jira principal:", p.getName());
		} else {
			LOGGER.error("Logging out from Jira null principal");
		}
		removePrincipalFromSessionContext(request);
		// session.setAttribute(AbstractCasFilter.CONST_CAS_ASSERTION, null);
		super.logout(request, response);
		return true;
	}
	
	
//	public static void getJiraUser(String username) throws JSONException{
//		
//		 String url = "http://localhost:8080/rest/api/2/user?username="+username;
//		 
//		 	LOGGER.error("calling getJiraUser with username:" + username);
//			String plainCreds = "admin:admin";
//			byte[] plainCredsBytes = plainCreds.getBytes();
//			byte[] base64CredsBytes = Base64.getEncoder().encode(plainCredsBytes);
//			String base64Creds = new String(base64CredsBytes);
//
//			HttpHeaders headers = new HttpHeaders();
//			headers.add("Authorization", "Basic " + base64Creds);
//			headers.setContentType(MediaType.APPLICATION_JSON);
//			headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
//
//			RestTemplate restTemplate = new RestTemplate();
//			HttpEntity<String> request = new HttpEntity<String>(headers);
//			LOGGER.error(request.toString());
//			LOGGER.error(request.getHeaders().toString());
//			ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);
//			String stringResponse = response.getBody();
//			LOGGER.error(stringResponse);
//			JSONObject user = new JSONObject(stringResponse);
//			LOGGER.error("End "+user.getString("key") + " " + user.getString("name") + " " + user.getString("emailAddress")); 
//	}

	
	



	
	
}
