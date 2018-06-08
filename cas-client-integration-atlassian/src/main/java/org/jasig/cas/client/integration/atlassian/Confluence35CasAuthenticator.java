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

import com.atlassian.confluence.event.events.security.LoginEvent;
import com.atlassian.confluence.event.events.security.LoginFailedEvent;
import com.atlassian.confluence.user.ConfluenceAuthenticator;
import com.atlassian.seraph.auth.AuthenticatorException;
import com.atlassian.seraph.auth.LoginReason;

import java.io.FileInputStream;
import java.security.Principal;
import java.util.Enumeration;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.jasig.cas.client.util.AbstractCasFilter;
import org.jasig.cas.client.validation.Assertion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extension of ConfluenceAuthenticator to allow people to configure Confluence 3.5+ to authenticate
 * via CAS.
 *
 * Based on https://bitbucket.org/jaysee00/example-confluence-sso-authenticator
 *
 * @author Scott Battaglia
 * @author John Watson
 * @author Jozef Kotlar
 * @version $Revision$ $Date$
 * @since 3.3.0
 */
public final class Confluence35CasAuthenticator extends ConfluenceAuthenticator {
    private static final long serialVersionUID = -6097438206488390678L;

    private static final Logger LOGGER = LoggerFactory.getLogger(Confluence35CasAuthenticator.class);
	private static final String ISF_HEADER = "isf8uid";
	
	
	public static Properties confluenceAppProps = new Properties();		
	
	static {		
		String propertiesLocation="/opt/atlassian/confluence/conf/confluence.properties";
		try{
			confluenceAppProps.load(new FileInputStream(propertiesLocation));
			String printProperties = confluenceAppProps.getProperty("printProperties");
			if (printProperties !=null && printProperties.equalsIgnoreCase("true")){
				log("Read the properties from:" + propertiesLocation );
				log("properties values:" + confluenceAppProps.toString());
			}
		}catch (Exception e){
			log("Cloud not read properties from:" + propertiesLocation);
			log(e.getMessage(),e);
		}
	}



/*    public Principal getUser(final HttpServletRequest request, final HttpServletResponse response) {
        Principal existingUser = getUserFromSession(request);
        if (existingUser != null) {
            LOGGER.debug("Session found; user already logged in.");
            LoginReason.OK.stampRequestResponse(request, response);
            return existingUser;
        }

        final HttpSession session = request.getSession();
        final Assertion assertion = (Assertion) session.getAttribute(AbstractCasFilter.CONST_CAS_ASSERTION);

        if (assertion != null) {
            final String username = assertion.getPrincipal().getName();
            final Principal user = getUser(username);
            final String remoteIP = request.getRemoteAddr();
            final String remoteHost = request.getRemoteHost();

            if (user != null) {
                putPrincipalInSessionContext(request, user);
                getElevatedSecurityGuard().onSuccessfulLoginAttempt(request, username);
                // Firing this event is necessary to ensure the user's personal information is initialised correctly.
                getEventPublisher().publish(
                        new LoginEvent(this, username, request.getSession().getId(), remoteHost, remoteIP));
                LoginReason.OK.stampRequestResponse(request, response);
                LOGGER.debug("Logging in [{}] from CAS.", username);
            } else {
                LOGGER.debug("Failed logging [{}] from CAS.", username);
                getElevatedSecurityGuard().onFailedLoginAttempt(request, username);
                getEventPublisher().publish(
                        new LoginFailedEvent(this, username, request.getSession().getId(), remoteHost, remoteIP));
            }
            return user;
        }

        return super.getUser(request, response);
    }
*/
    
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
			
//			String email = isfIdentifiers.getEmail().trim();
//			String abn = isfIdentifiers.getAbn().trim();
//			String fullName = isfIdentifiers.getFullName();
			String jiraUserName = isfIdentifiers.getJiraUserName();
			
			log("ISF identifiers: " + isfIdentifiers.toString());
			
			if (jiraUserName == null){
				log("ISF jiraUserName is null." + isfIdentifiers.toString());
			//do the authentication or reqistration
			}else{
				//check if user exists, if it does, log them in and return. Happy days.
				try {
					boolean userExists= JiraConfluenceAuthAndRegistrationUtils.userExistsInConfluence(jiraUserName);
					log("Does user exist:" + jiraUserName + ":" + userExists);
				if (userExists){
					return logInExistingUser(jiraUserName,request);
				}
				//user does not exist. Register them and log them in on the fly.
				
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
	
	
	private static void log(String logMessage){
		LOGGER.error(logMessage);
	}
	
	private static void log(String logMessage, Exception ex){
		LOGGER.error(logMessage, ex);
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

 
}
