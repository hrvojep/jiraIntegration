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
	private static String jiraUser=Jira44CasAuthenticator.appProps.getProperty("adminUser");
	private static String jiraPassword = Jira44CasAuthenticator.appProps.getProperty("adminpassword");
	private static String plainCreds = jiraUser+jiraPassword;

	public static boolean userExistsInJira(String userUpn) throws JSONException {
		
		boolean userFound=false;
		LOGGER.error("checking if user exists:" + userUpn);
		// String url = "http://localhost:8080/rest/api/2/user?username=h2";

		String getUserUrlEndpoint = "/rest/api/2/user?username=";
		String fullUrl = localJiraUrl + getUserUrlEndpoint + userUpn;

		String plainCreds = jiraUser+jiraPassword;
		byte[] plainCredsBytes = plainCreds.getBytes();
		byte[] base64CredsBytes = Base64.getEncoder().encode(plainCredsBytes);
		String base64Creds = new String(base64CredsBytes);

		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Basic " + base64Creds);
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

		RestTemplate restTemplate = new RestTemplate();
		HttpEntity<String> request = new HttpEntity<String>(headers);
		LOGGER.error("making request with url:" + fullUrl);		
		try {
		ResponseEntity<String> response = restTemplate.exchange(fullUrl, HttpMethod.GET, request, String.class);
		String stringResponse = response.getBody();


		JSONObject user = new JSONObject(stringResponse);
		String userKey = user.getString("key");
		if (userKey != null && userKey.trim().length() > 0) {
			LOGGER.error("User:'" + userUpn + "' exists");
			userFound= true;
		} else{
			LOGGER.error("User:'" + userUpn + "' does not exist!");
			userFound = false;
		}
		} catch (org.springframework.web.client.RestClientException e){
			LOGGER.error("User:'" + userUpn + "' not found");
		}
		return userFound;
	}
	
	
	
	public static String createNewJiraUser(String upn, String email) throws JSONException{
		
		String url = "http://localhost:8080/rest/api/2/user";
		//String url = "https://hookb.in/v0PPBPX0";
		
		LOGGER.error("registering new user upn:'" + upn +" ' email:'" + email + "'");
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

	//task2
	public static void addUidPropertyToUser(String upn) throws Exception{
   		//sample request via curl    
		//curl -i -X PUT http://localhost:8080/rest/api/2/user/properties/uid?username=h2 -H'Content-type: application/json' -H'Accept: application/json' -uadmin:admin -d'"xxxxh2"'
			
			LOGGER.error("calling addUidPropertyToUser with upn:" + upn);
			String url = "http://localhost:8080/rest/api/2/user/properties/uid?username="+upn;
			JSONObject json = new JSONObject();
			json.put("",upn);
			String requestBody = json.toString();
			HttpEntity<String> request = getRequest(requestBody,false);

			RestTemplate restTemplate = new RestTemplate();
			ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT, request, String.class);
			String stringResponse = response.getBody();
			LOGGER.error("finished with addUidPropertyToUser with upn:" + upn);
//			JSONObject user = new JSONObject(stringResponse);
//			System.out.println("--++>>"+user.getString("key") + " " + user.getString("name") + " " + user.getString("emailAddress")); 
			
	}

	//task3
	public static void addUserToExternalUsersGroup(String upn) throws Exception{
//		curl  -i -u admin:admin -X POST --data "{\"name\": \"h3\"}" -H "Content-Type: application/json" http://localhost:8080/rest/api/2/group/user?groupname=jira-software-users

		LOGGER.error("calling addUserToExternalUsersGroup with upn:" + upn);
		String groupName="group-h1";
		String url = "http://localhost:8080/rest/api/2/group/user?groupname="+groupName;
		JSONObject json = new JSONObject();
		json.put("name",upn);
		String requestBody = json.toString();
		HttpEntity<String> request = getRequest(requestBody,false);

		RestTemplate restTemplate = new RestTemplate();
		System.out.println(request.toString());
		System.out.println(request.getHeaders().toString());
		ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
		String stringResponse = response.getBody();
		System.out.println(stringResponse);
		LOGGER.error("finished addUserToExternalUsersGroup with upn:" + upn);
	}

	
	//task4
	//ISSUE: ORG not visible in serviceDesk Web, but is via API, can add it manually in browser
	public static String createOrganisationinServiceDesk(String abn) throws Exception{
		//create organization
		//curl -v -i -X POST http://localhost:8080/rest/servicedeskapi/organization -H'Content-type: application/json' -H'Accept: application/json' 
		//-H'X-ExperimentalApi: opt-in' -uadmin:admin -d'{"name": "test organization 2"}'

		LOGGER.error("calling createOrganisationinServiceDesk with ABN:" + abn);
		String url = "http://localhost:8080/rest/servicedeskapi/organization";
		JSONObject json = new JSONObject();
		json.put("name",abn);
		String requestBody = json.toString();
		HttpEntity<String> request = getRequest(requestBody,true);
		RestTemplate restTemplate = new RestTemplate();
		System.out.println(request.toString());
		System.out.println(request.getHeaders().toString());
		ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
		JSONObject createOrgResponse = new JSONObject(response.getBody());
		LOGGER.error("finished createOrganisationinServiceDesk with ABN:" + abn + " id:" + createOrgResponse.getString("id"));
		return createOrgResponse.getString("id");

	}

	
	//task5
	//TODO add name instead of email to fullName
	public static void createCustomerInServiceDesk(String email) throws Exception{
//		curl -v -i -X POST http://localhost:8080/rest/servicedeskapi/customer -H'Content-type: application/json' -H'Accept: application/json' -H'X-ExperimentalApi: opt-in' -uadmin:admin -d'
//		{
//		    "email": "fred@example.com",
//		    "fullName": "Fred F. User"
//		}'
		LOGGER.error("calling createCustomerInServiceDesk with email:" + email);
		String url = "http://localhost:8080/rest/servicedeskapi/customer";
		JSONObject json = new JSONObject();
		json.put("email", email);
		json.put("fullName",email);
		String requestBody = json.toString();
		HttpEntity<String> request = getRequest(requestBody,true);

		RestTemplate restTemplate = new RestTemplate();
		System.out.println(request.toString());
		System.out.println(request.getHeaders().toString());
		ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
		String stringResponse = response.getBody();
		System.out.println(stringResponse);
		LOGGER.error("finished createCustomerInServiceDesk with email:" + email);
	}

	
	
	//task6
	public static void addCustomerToOrganisation(String organisationId, String userName) throws Exception{
//		#add user to organisation 
//		curl -v -i -X POST http://localhost:8080/rest/servicedeskapi/organization/2/user -H'Content-type: application/json' -H'Accept: application/json' -H'X-ExperimentalApi: opt-in' -uadmin:admin -d'
//		{
//		    "usernames": [
//		        "fred@example.com"        
//		    ]
//		}'
		LOGGER.error("calling addCustomerToOrganisation with organisationId:" + organisationId + " userName:" + userName);
		String url = "http://localhost:8080/rest/servicedeskapi/organization/"+ organisationId +"/user";
		String requestBody="{\"usernames\": [\""+ userName +"\"]}";
		System.out.println("Body:" + requestBody);
		HttpEntity<String> request = getRequest(requestBody,true);
		RestTemplate restTemplate = new RestTemplate();
		System.out.println(request.toString());
		System.out.println(request.getHeaders().toString());
		ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
		String stringResponse = response.getBody();
		System.out.println(stringResponse);
		LOGGER.error("finished addCustomerToOrganisation with organisationId:" + organisationId + " userName:" + userName);
	}

	
	
	private static HttpEntity<String>  getRequest(String body, boolean addExperimentalHeader){
		byte[] plainCredsBytes = plainCreds.getBytes();
		byte[] base64CredsBytes = Base64.getEncoder().encode(plainCredsBytes);
		String base64Creds = new String(base64CredsBytes);

		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Basic " + base64Creds);
		if (addExperimentalHeader){
			headers.add("X-ExperimentalApi", "opt-in");			
		}
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
		HttpEntity<String> request;
		
		if (body == null){
			request = new HttpEntity<String>(headers);
		}else{
			request = new HttpEntity<String>(body,headers);
		}
		return request;
	} 


	
	public static String generateRandomId(){
		long millis = System.currentTimeMillis() % 1000;
		return "ucxek" + millis;
	}


}
