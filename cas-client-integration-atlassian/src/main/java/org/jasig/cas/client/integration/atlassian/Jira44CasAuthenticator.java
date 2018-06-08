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

import com.atlassian.jira.security.login.JiraSeraphAuthenticator;
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
	private static final String ISF_HEADER = "isf8uid";

	private static final Logger LOGGER = LoggerFactory.getLogger(Jira44CasAuthenticator.class);
	

	public Principal getUser(final HttpServletRequest request, final HttpServletResponse response) {
		// First, check to see if this session has already been authenticated
		// during a previous request.

		if ( JiraConfluenceAuthAndRegistrationUtils.logSessionAndCookieInfo != null && "true".equals(JiraConfluenceAuthAndRegistrationUtils.logSessionAndCookieInfo)){
			JiraConfluenceUtils.logSessionAndCookies(request);
		}
		
		Principal existingUser = getUserFromSession(request);
		if (existingUser != null) {
			log("Session found; user already logged in. User name:" + existingUser.getName());
			return existingUser;
		}
		
		Enumeration<String> headerNames =request.getHeaderNames();
		while (headerNames.hasMoreElements()){			
			String headerName = headerNames.nextElement();
			if (headerName.equals(ISF_HEADER)){
				log("->"+ headerName + ":-->"+request.getHeader(headerName));
			}
		}
		String uidAsUrlParam = request.getParameter(ISF_HEADER);


		//do we have ISF UID header, if we do, do stuff with it
		String uid=request.getHeader(ISF_HEADER);
		if (uid !=null || uidAsUrlParam !=null){
			if (uid ==null){
				uid=uidAsUrlParam;
			}
			log("Got UID:" + uid);
			ISFJiraIdentifiers isfIdentifiers=null;
			try {
				isfIdentifiers = ISFUtils.getISFIdentifiers(ISFUtils.getXMLFromBase64String(uid));
			} catch (Exception e) {
				log("Unable to extract ISF uid:" + uid + " " + e);
			}
			
			String email = isfIdentifiers.getEmail().trim();
			String abn = isfIdentifiers.getAbn().trim();
			String fullName = isfIdentifiers.getFullName();
			String jiraUserName = isfIdentifiers.getJiraUserName();
			
			log("ISF identifiers: " + isfIdentifiers.toString());
			
			if (jiraUserName == null){
				log("ISF jiraUserName is null." + isfIdentifiers.toString());
			//do the authentication or reqistration
			}else{
				//check if user exists, if it does, log them in and return. Happy days.
				try {
					boolean userExists= JiraConfluenceAuthAndRegistrationUtils.userExistsInJira(jiraUserName);
					log("Does user exist:" + email + ":" + userExists);
				if (userExists){
					return logInExistingUser(jiraUserName,request);
				}
				//user does not exist. Register them and log them in on the fly.
				else{
					 log("Starting a workflow to create a new user in JIRA:" + isfIdentifiers.toString());
					 

					 log("**-->Start createNewJiraUser jiraUserName:" + jiraUserName + " email:"+ email);
					 JiraConfluenceAuthAndRegistrationUtils.createNewJiraUser(jiraUserName,email, fullName);
					 
					 log("**-->Resolving abn to business name:" + abn);
					 String businessName = JiraConfluenceAuthAndRegistrationUtils.resolveAbnToBusinessName(abn);
					 
					 log("**-->Start createOrganisationinServiceDesk:" + abn);
					 String newOrgId = JiraConfluenceAuthAndRegistrationUtils.createOrganisationinServiceDesk(businessName);

					 log("**-->Start addOrganisationToServiceDesk:" + newOrgId);
					 JiraConfluenceAuthAndRegistrationUtils.addOrganisationToServiceDesk(newOrgId);

//					 log("**-->Start createCustomerInServiceDesk:" + jiraUserName);
//					 JiraAuthAndRegistrationUtils.createCustomerInServiceDesk(email,fullName);
					 
					 log("**-->Start addUserToExternalUsersGroup:" + jiraUserName);
					 JiraConfluenceAuthAndRegistrationUtils.addUserToExternalUsersGroup(jiraUserName);
					 
					 log("**-->Start addCustomerToOrganisation:" + jiraUserName);
					 JiraConfluenceAuthAndRegistrationUtils.addCustomerToOrganisation(newOrgId, jiraUserName);

					 log("**-->Start addUidPropertyToUser:"+ jiraUserName);
					 JiraConfluenceAuthAndRegistrationUtils.addUidPropertyToUser(jiraUserName);
					 
					 log("**-->Start register compnay and user in insight businessName:" + businessName + " jiraUserName:"+ jiraUserName);
					 JiraConfluenceAuthAndRegistrationUtils.createDspAndContactInInsight(abn,businessName,fullName,email,jiraUserName);

					 log("Finished workflow to create a new user in JIRA jiraUserName:'" + jiraUserName);
					 
					 //final step, log in new user
					 return logInExistingUser(jiraUserName,request);

				}
				} catch (Exception e) {
					log("Something went wrong with the workflow jiraUserName:" + jiraUserName + " "+ e);
				    log(e.getMessage());
				}
			}
		}
		return super.getUser(request, response);
	}
	
	
	
	private Principal logInExistingUser(String jiraUserName, final HttpServletRequest request){
		log("---> creating principal:" + jiraUserName);

		final String username = jiraUserName;
		final Principal principal = new Principal() {
			@Override
			public String getName() {
				return username;
			}
		};
		log("---> login existing user:" + jiraUserName);
	    putPrincipalInSessionContext(request, principal);
		getElevatedSecurityGuard().onSuccessfulLoginAttempt(request,username);
		return principal;
	}

	public boolean logout(final HttpServletRequest request, final HttpServletResponse response)
			throws AuthenticatorException {
		final HttpSession session = request.getSession();
		final Principal p = (Principal) session.getAttribute(LOGGED_IN_KEY);

		if (p != null) {
			log("Logging out from Jira principal:" + p.getName());
		} else {
			log("Logging out from Jira null principal");
		}
		removePrincipalFromSessionContext(request);
		// session.setAttribute(AbstractCasFilter.CONST_CAS_ASSERTION, null);
		super.logout(request, response);
		return true;
	}
	
	private static void log(String logMessage){
		LOGGER.error(logMessage);
	}
	
	private static void log(String logMessage, Exception ex){
		LOGGER.error(logMessage, ex);
	}
	
}
