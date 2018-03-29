package org.jasig.cas.client.integration.atlassian;

import java.util.Base64;
import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.atlassian.jira.util.json.JSONException;
import com.atlassian.jira.util.json.JSONObject;

public class JiraAuthAndRegistrationUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(JiraAuthAndRegistrationUtils.class);

	private static String localJiraUrl = "http://localhost:8080";

	public static boolean userExistsInJira(String userUpn) throws JSONException {
		
		LOGGER.error("checking if user exists" + userUpn);
		// String url = "http://localhost:8080/rest/api/2/user?username=h2";

		String getUserUrlEndpoint = "/rest/api/2/user?username=";
		String fullUrl = localJiraUrl + getUserUrlEndpoint + userUpn;

		String plainCreds = "admin:admin";
		byte[] plainCredsBytes = plainCreds.getBytes();
		byte[] base64CredsBytes = Base64.getEncoder().encode(plainCredsBytes);
		String base64Creds = new String(base64CredsBytes);

		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Basic " + base64Creds);
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

		RestTemplate restTemplate = new RestTemplate();
		HttpEntity<String> request = new HttpEntity<String>(headers);
		ResponseEntity<String> response = restTemplate.exchange(fullUrl, HttpMethod.GET, request, String.class);
		String stringResponse = response.getBody();

		JSONObject user = new JSONObject(stringResponse);
		String userKey = user.getString("key");
		if (userKey != null && userKey.trim().length() > 0) {
			LOGGER.error("User:'" + userUpn + "' exists");
			return true;
		} else
			LOGGER.error("User:'" + userUpn + "' does not exist!");
			return false;
	}
	
	
	
	public static String createNewJiraUser(String upn, String email) throws JSONException{
		
		String url = "http://localhost:8080/rest/api/2/user";
		//String url = "https://hookb.in/v0PPBPX0";
		
		LOGGER.error("registering new user" + upn);
		String plainCreds = "admin:admin";
		byte[] plainCredsBytes = plainCreds.getBytes();
		byte[] base64CredsBytes =Base64.getEncoder().encode(plainCredsBytes);
		String base64Creds = new String(base64CredsBytes);

		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Basic " + base64Creds);
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
		
		RestTemplate restTemplate = new RestTemplate();
		JSONObject json = new JSONObject();
		json.put("name", upn);
		//json.put("password", "h"+randomId);
		json.put("emailAddress", email);
		json.put("displayName", email);
		
		String jsonString = json.toString();
		HttpEntity<String> request = new HttpEntity<String>(jsonString, headers);
		
//		System.out.println(request.toString());
//		System.out.println(request.getHeaders());
		ResponseEntity<String> responseEntity=null;
    	responseEntity = restTemplate.exchange( url,HttpMethod.POST, request , String.class );
		System.out.println(responseEntity.toString());
		String response = responseEntity.getBody();
		
		JSONObject user = new JSONObject(response);
		LOGGER.error("Created new jira user:"+user.getString("key") + " " + user.getString("name") + " " + user.getString("emailAddress")); 
		return user.getString("key");
	}
	
	public static String generateRandomId(){
		long millis = System.currentTimeMillis() % 1000;
		return "ucxek" + millis;
	}


}
