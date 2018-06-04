package org.jasig.cas.client.integration.atlassian;

import java.util.Base64;
import java.util.Collections;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.atlassian.jira.util.json.JSONArray;
import com.atlassian.jira.util.json.JSONException;
import com.atlassian.jira.util.json.JSONObject;

import abn.lookup.AbnSearchResult;
import abn.lookup.AbnSearchWSHttpGet;


//import reactor.core.publisher.Mono;

public class JiraAuthAndRegistrationUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(JiraAuthAndRegistrationUtils.class);

//	private static String localJiraUrl = "http://localhost:8080";
	//localJiraRestUrl should be someting like http://localhost:8080/jira/rest
	
	private static Properties properties = Jira44CasAuthenticator.appProps;
	
	private static String localJiraRestUrl = properties.getProperty("localJiraRestUrl");
	private static String jiraUser=properties.getProperty("adminuser");
	private static String jiraPassword = properties.getProperty("adminpassword");
	private static String externalUserGroupName= properties.getProperty("externalUserGroupName");
	private static String serviceDeskId= properties.getProperty("serviceDeskId");
	public static String logSessionAndCookieInfo = properties.getProperty("logSessionAndCookieInfo");
	private static String abnLookupAuthGuid = properties.getProperty("abnLookupAuthGuid");
	private static String resolveAbnToBusinessName = properties.getProperty("resolveAbnToBusinessName");
	
	private static String objectTypeIdOrg = properties.getProperty("objectTypeIdOrg");
	private static String objectTypeAttributeIdOrgName = properties.getProperty("objectTypeAttributeIdOrgName");
	private static String objectTypeAttributeIdOrgAbn = properties.getProperty("objectTypeAttributeIdOrgAbn");
	
	private static String objectTypeIdContact = properties.getProperty("objectTypeIdContact");
	private static String objectTypeAttributeIdContactEmail = properties.getProperty("objectTypeAttributeIdContactEmail");
	private static String objectTypeAttributeIdContactName = properties.getProperty("objectTypeAttributeIdContactName");
	private static String objectTypeAttributeIdContactCompanyId = properties.getProperty("objectTypeAttributeIdContactCompanyId");
	
	private static String dspObjectSchemaId = properties.getProperty("dspObjectSchemaId");
	
	
	
	private static String plainCreds = jiraUser+":" +jiraPassword;

	
	
	public static boolean userExistsInJira(String email) throws JSONException {
		
		boolean userFound=false;
		log("checking if user exists:" + email);
		// String url = "http://localhost:8080/rest/api/2/user?username=h2";
		//String url = localJiraRestUrl + 
		//String getUserUrlEndpoint = "/rest/api/2/user?username=";
		String fullUrl = localJiraRestUrl + "/api/2/user?username=" + email;
		//String fullUrl = localJiraUrl + getUserUrlEndpoint + userUpn;

		RestTemplate restTemplate = new RestTemplate();
		HttpEntity<String> request = getRequest(null,false);
		
		log("making request with url:" + fullUrl + " requestBody:" + request.getBody() + " requestToString:" + request.toString());		
		try {
		ResponseEntity<String> response = restTemplate.exchange(fullUrl, HttpMethod.GET, request, String.class);
		String stringResponse = response.getBody();

		JSONObject user = new JSONObject(stringResponse);
		String userKey = user.getString("key");
		if (userKey != null && userKey.trim().length() > 0) {
			log("User:'" + email + "' exists");
			userFound= true;
		} else{
			log("User:'" + email + "' does not exist!");
			userFound = false;
		}
		} catch (HttpClientErrorException e){
			logErrorAndRethrowException(e,false);
		}
		return userFound;
	}
	
	
//	public static void getUserAsync() throws Exception {
//		 String url = "http://localhost:8080/rest/api/2/user?username=h2";
//		 System.out.println("Starting");
//
//		 Mono<ClientResponse> result = WebClient.create().get()
//		        .uri(url)
//		        .header(HttpHeaders.AUTHORIZATION, "Basic " + getCreds())
//		        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
//		        .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
//		        .exchange();
//	 
//		 result.flatMap(res2 -> res2.bodyToMono(String.class)).subscribe(JiraAuthAndRegistrationUtils::handleResponse);
//		 System.out.println("Finished");
//	}

//	 private static void handleResponse(String s) {
//	      System.out.println("handle response:" + s);
//	  }

	
	private static void logErrorAndRethrowException(HttpClientErrorException e,boolean throwException  ){
	    log(e.getMessage());
	    log(e.getResponseBodyAsString());
	    log(e.getStatusCode().toString());
	    if (throwException){
	    	throw e;
	    }
	}
	
	
	private static void logErrorAndRethrowException(RestClientException e){
	    log(e.getMessage(), e);
		log(((HttpClientErrorException) e).getResponseBodyAsString());
		log(((HttpClientErrorException) e).getStatusCode().toString());
		throw e;
}

	
	public static String createNewJiraUser(String upn, String email) throws JSONException{
		
		//String url = "http://localhost:8080/rest/api/2/user";
		String url = localJiraRestUrl + "/api/2/user";
		//String url = "https://hookb.in/v0PPBPX0";
		
		log("registering new user upn:'" + upn +"' email:'" + email + "'" + " with url:" + url);
		
		RestTemplate restTemplate = new RestTemplate();
		JSONObject json = new JSONObject();
		json.put("name", upn);
		json.put("emailAddress", email);
		json.put("displayName", email);
		
		String requestBody = json.toString();
		HttpEntity<String> request = getRequest(requestBody,false);
		ResponseEntity<String> responseEntity=null;
		String response=null;
		try{
	    	responseEntity = restTemplate.exchange( url,HttpMethod.POST, request , String.class );
			System.out.println(responseEntity.toString());
			response = responseEntity.getBody();
		} catch (HttpClientErrorException e){
			logErrorAndRethrowException(e,true);
		}
		JSONObject user = new JSONObject(response);
		log("Created new jira user:"+user.getString("key") + " " + user.getString("name") + " " + user.getString("emailAddress")); 
		return user.getString("key");
	}

	//task2
	public static void addUidPropertyToUser(String email) throws Exception{
   		//sample request via curl    
		//curl -i -X PUT http://localhost:8080/rest/api/2/user/properties/uid?username=h2 -H'Content-type: application/json' -H'Accept: application/json' -uadmin:admin -d'"xxxxh2"'
			
			//String url = "http://localhost:8080/rest/api/2/user/properties/uid?username="+upn;
			String url = localJiraRestUrl +"/api/2/user/properties/uid?username="+email;
			
			log("calling addUidPropertyToUser with upn:" + email + " with url:" + url);
			JSONObject json = new JSONObject();
			json.put("",email);
			String requestBody = json.toString();
			HttpEntity<String> request = getRequest(requestBody,false);

			RestTemplate restTemplate = new RestTemplate();
			try{
				restTemplate.exchange(url, HttpMethod.PUT, request, String.class);
			} catch (HttpClientErrorException e){
				logErrorAndRethrowException(e,true);
			}
			log("finished with addUidPropertyToUser with upn:" + email);
	}

	//task3
	public static void addUserToExternalUsersGroup(String upn) throws Exception{
//		curl  -i -u admin:admin -X POST --data "{\"name\": \"h3\"}" -H "Content-Type: application/json" http://localhost:8080/rest/api/2/group/user?groupname=jira-software-users

		//String url = "http://localhost:8080/rest/api/2/group/user?groupname="+externalUserGroupName;
		String url = localJiraRestUrl + "/api/2/group/user?groupname="+externalUserGroupName;
		log("calling addUserToExternalUsersGroup with upn:" + upn + " :url:" + url);
		JSONObject json = new JSONObject();
		json.put("name",upn);
		String requestBody = json.toString();
		HttpEntity<String> request = getRequest(requestBody,false);

		RestTemplate restTemplate = new RestTemplate();
		System.out.println(request.toString());
		System.out.println(request.getHeaders().toString());
		try{
		ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
		} catch (HttpClientErrorException e){
			logErrorAndRethrowException(e,true);
		}
		log("finished addUserToExternalUsersGroup with upn:" + upn);
	}

	
	
	public static String resolveAbnToBusinessName(String abn) throws Exception {		
		String resolvedBusinessName="";
		if (resolveAbnToBusinessName!=null && resolveAbnToBusinessName.equalsIgnoreCase("true")){
			AbnSearchResult result = AbnSearchWSHttpGet.searchByABN(abnLookupAuthGuid, abn, false);
			if (result.isException()){
				log("ABN search for ABN [" + abn + "] returned exception [" + result.getExceptionDescription() + "]");
				throw new Exception("Could not lookup ABN:" + abn + " " + result.getExceptionDescription());
			}
			resolvedBusinessName =  result.getOrganisationName();
		} else{
			//we can't resolve abn to business name, just return abn
			resolvedBusinessName = abn;
		}
		return resolvedBusinessName;
	}
	
	
	//task4
	//ISSUE: ORG not visible in serviceDesk Web, but is via API, can add it manually in browser
	public static String createOrganisationinServiceDesk(String businessName,String abn) throws Exception{
		//create organization
		//curl -v -i -X POST http://localhost:8080/rest/servicedeskapi/organization -H'Content-type: application/json' -H'Accept: application/json' 
		//-H'X-ExperimentalApi: opt-in' -uadmin:admin -d'{"name": "test organization 2"}'

		//String url = "http://localhost:8080/rest/servicedeskapi/organization";
		
		
		String url = localJiraRestUrl + "/servicedeskapi/organization";
		log("calling createOrganisationinServiceDesk with ABN:" + abn + " businessName:" + businessName + " with url:" + url);
		JSONObject json = new JSONObject();
		json.put("name",businessName);
		String requestBody = json.toString();
		HttpEntity<String> request = getRequest(requestBody,true);
		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<String> response=null;
		try{
			response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
		} catch (HttpClientErrorException e){
			logErrorAndRethrowException(e,true);
		}
		JSONObject createOrgResponse = new JSONObject(response.getBody());
		log("finished createOrganisationinServiceDesk with ABN:" + abn + " id:" + createOrgResponse.getString("id"));
		return createOrgResponse.getString("id");
	}

	
	public static void addOrganisationToServiceDesk(String orgId) throws Exception{
	//		curl -u hrvoje2:hrvoje2 -X POST  -H "X-ExperimentalApi: opt-in" -H "Content-Type: application/json" http://localhost:8080/rest/servicedeskapi/servicedesk/1/organization -d'
	//		{
	//		    "organizationId": 9
	//		}'
		 
		//String url = "http://localhost:8080/rest/servicedeskapi/servicedesk/"+ serviceDeskId +"/organization";
		String url = localJiraRestUrl + "/servicedeskapi/servicedesk/"+ serviceDeskId +"/organization";

		log("calling addOrganisationToServiceDesk with orgID:" + orgId + " with url:" + url);
		JSONObject json = new JSONObject();
		json.put("organizationId",orgId);
		String requestBody = json.toString();
		HttpEntity<String> request = getRequest(requestBody,true);
		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<String> response=null;
		try{
			response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
		} catch (RestClientException e){
			logErrorAndRethrowException(e);
		}
		log("finished addOrganisationToServiceDesk with orgID:" + orgId + " reponseCode:" + response.getStatusCode() );
	}
	
	
	//task5
	//TODO add name instead of email to fullName
	public static void createCustomerInServiceDesk(String email, String fullName) throws Exception{
//		curl -v -i -X POST http://localhost:8080/rest/servicedeskapi/customer -H'Content-type: application/json' -H'Accept: application/json' -H'X-ExperimentalApi: opt-in' -uadmin:admin -d'
//		{
//		    "email": "fred@example.com",
//		    "fullName": "Fred F. User"
//		}'
		//String url = "http://localhost:8080/rest/servicedeskapi/customer";
		String url = localJiraRestUrl + "/servicedeskapi/customer";
		log("calling createCustomerInServiceDesk with email:" + email + " with url:" + url);
		JSONObject json = new JSONObject();
		json.put("email", email);
		json.put("fullName",fullName);
		String requestBody = json.toString();
		HttpEntity<String> request = getRequest(requestBody,true);

		RestTemplate restTemplate = new RestTemplate();
		System.out.println(request.toString());
		System.out.println(request.getHeaders().toString());
		try{
			ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
		} catch (HttpClientErrorException e){
			logErrorAndRethrowException(e,true);
		}
		log("finished createCustomerInServiceDesk with email:" + email);
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
		//String url = "http://localhost:8080/rest/servicedeskapi/organization/"+ organisationId +"/user";
		String url = localJiraRestUrl + "/servicedeskapi/organization/"+ organisationId +"/user";
		log("calling addCustomerToOrganisation with organisationId:" + organisationId + " userName:" + userName + " with url: " + url);		
		String requestBody="{\"usernames\": [\""+ userName +"\"]}";
		HttpEntity<String> request = getRequest(requestBody,true);
		RestTemplate restTemplate = new RestTemplate();
		try{
			ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
		} catch (HttpClientErrorException e){
			logErrorAndRethrowException(e,true);
		}
		log("finished addCustomerToOrganisation with organisationId:" + organisationId + " userName:" + userName);
	}

	
	
	private static HttpEntity<String>  getRequest(String body, boolean addExperimentalHeader){
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Basic " + getCreds());
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
	
	
	
	public static void createDspAndContactInInsight(String abn, String businessName, String contactName, String contactEmail) throws Exception{
		String dspCompanyId = createOrganisationInInsight(abn,businessName);
		createContactInInsight(dspCompanyId,contactName,contactEmail);
	}
	
	private static String createOrganisationInInsight(String abn,String businessName) throws Exception{
//		curl -v -i -X POST http://localhost:8080/jira/rest/insight/1.0/object/create -H'Content-type: application/json' -H'Accept: application/json' -H'X-ExperimentalApi: opt-in' -uadmin:admin -d'
//		{
//		    "objectTypeId": 3,
//		    "attributes": [{
//		        "objectTypeAttributeId": 45,
//		        "objectAttributeValues": [{
//		            "value": "H enterprise 3"
//		        }]
//		    },
//		    {
//		        "objectTypeAttributeId": 49,
//		        "objectAttributeValues": [{
//		            "value": "1213546546"
//		        }]
//		    },
//		    {
//		        "objectTypeAttributeId": 52,
//		        "objectAttributeValues": [{
//		            "value": "470"
//		        }]
//
//		    }
//		    ]
//		}'
		String insightDSPid=null;

		//check if DSP exists in Insight
		insightDSPid = getRegisteredDSPidFromInsight(abn);

		// if DSP doesn't exist register it
		if (insightDSPid == null){
			log("calling createOrganisationInInsight with businessName:" + businessName + " abn:" + abn);
			String requestBody="{\"objectTypeId\": "+ objectTypeIdOrg +", \"attributes\": [{\"objectTypeAttributeId\": "+objectTypeAttributeIdOrgName+", \"objectAttributeValues\": [{\"value\": \""+businessName+"\"}] }, {\"objectTypeAttributeId\": "+objectTypeAttributeIdOrgAbn+", \"objectAttributeValues\": [{\"value\": \""+abn+"\"}] } ] }'";
			String url = localJiraRestUrl + "/insight/1.0/object/create";
			HttpEntity<String> request = getRequest(requestBody,true);
			RestTemplate restTemplate = new RestTemplate();
			ResponseEntity<String> response=null;
			try{
				response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);		
			} catch (HttpClientErrorException e){
				logErrorAndRethrowException(e);
			}
			String stringResponse = response.getBody();
			JSONObject org = new JSONObject(stringResponse);
			int orgId=org.getInt("id");		
			log("finished createOrganisationInInsight with businessName:" + businessName + " abn:" + abn + " .Create DSP with id:" + orgId);
			insightDSPid = String.valueOf(orgId);
		}
		return insightDSPid;
		
	}
	
	
	private static String getRegisteredDSPidFromInsight(String abn) throws Exception {
		//curl  -X GET http://localhost:8080/jira/rest/insight/1.0/iql/objects?objectSchemaId=2&iql=objecttypeid=3%20AND%20ABN=51824753554 -H'Content-type: application/json' -H'Accept: application/json' -H'X-ExperimentalApi: opt-in' -uadmin:admin | jq '.'

		log("Strarting getRegisteredDSPidFromInsight with abn:" + abn);
		String registeredDSPidFromInsight=null;
		String url = localJiraRestUrl + "/insight/1.0/iql/objects?objectSchemaId="+dspObjectSchemaId+"&iql=objecttypeid="+objectTypeIdOrg+" AND ABN="+ abn;

		HttpEntity<String> request = getRequest(null,true);
		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<String> response=null;
		try{
			response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);
		} catch (HttpClientErrorException e){
			logErrorAndRethrowException(e);
		}

		String stringResponse = response.getBody();
		//you get a pile of JSON back, assumpiton is if abn appears mulitple times then the DSP is already registered. 
		int countOfAppearances = StringUtils.countOccurrencesOf(stringResponse, abn);
		if (countOfAppearances > 1){
			JSONObject org = new JSONObject(stringResponse);
			JSONArray arrObj = org.getJSONArray("objectEntries");
			JSONObject firstElement = (JSONObject)arrObj.get(0);
			Integer orgId = firstElement.getInt("id");
			registeredDSPidFromInsight=String.valueOf(orgId);
		}
		log("finished getRegisteredDSPidFromInsight with abn:" + abn + " registeredDSPidFromInsight:" + registeredDSPidFromInsight + " countOfAppearances:" + countOfAppearances);
		return registeredDSPidFromInsight;
	}
	
	

	
	private static void createContactInInsight(String contactCompanyId, String contactName, String contactEmail) throws Exception {
//		#create contact object for DSP object, last field is the company id (481) from Elle.contact.json, this is the id: you get from create company.
//		curl -v -i -X POST http://localhost:8080/jira/rest/insight/1.0/object/create -H'Content-type: application/json' -H'Accept: application/json' -H'X-ExperimentalApi: opt-in' -uadmin:admin -d'
//		{
//		    "objectTypeId": 7,
//		    "attributes": [{
//		        "objectTypeAttributeId": 100,
//		        "objectAttributeValues": [{
//		            "value": "test2@test.com"
//		        }]
//		    },
//		    {
//		        "objectTypeAttributeId": 26,
//		        "objectAttributeValues": [{
//		            "value": "test three"
//		        }]
//		    },
//		    {
//		        "objectTypeAttributeId": 96,
//		        "objectAttributeValues": [{
//		            "value": "481"
//		        }]
//		    }
//		    ]
//		}'	
		
		log("calling createContactInInsight with companyId:" + contactCompanyId + " contactname:" + contactName + " contactEmail:" + contactEmail);

		//with companyID
		//String requestBody="{\"objectTypeId\": "+objectTypeIdContact+", \"attributes\": [{\"objectTypeAttributeId\": "+objectTypeAttributeIdContactEmail+", \"objectAttributeValues\": [{\"value\": \""+contactEmail+"\"}] }, {\"objectTypeAttributeId\": "+objectTypeAttributeIdContactName+", \"objectAttributeValues\": [{\"value\": \""+contactName+"\"}] }, {\"objectTypeAttributeId\": "+objectTypeAttributeIdContactCompanyId+", \"objectAttributeValues\": [{\"value\": \""+contactCompanyId+"\"}] } ] }'";

		//without companyID
		String requestBody="{\"objectTypeId\": "+objectTypeIdContact+", \"attributes\": [{\"objectTypeAttributeId\": "+objectTypeAttributeIdContactEmail+", \"objectAttributeValues\": [{\"value\": \""+contactEmail+"\"}] }, {\"objectTypeAttributeId\": "+objectTypeAttributeIdContactName+", \"objectAttributeValues\": [{\"value\": \""+contactName+"\"}] }, {\"objectTypeAttributeId\": "+objectTypeAttributeIdContactCompanyId+", \"objectAttributeValues\": [{\"value\": \""+contactCompanyId+"\"}] } ] }'";
		//without companyID
		//String requestBody="{\"objectTypeId\": "+objectTypeIdContact+", \"attributes\": [{\"objectTypeAttributeId\": "+objectTypeAttributeIdContactEmail+", \"objectAttributeValues\": [{\"value\": \""+contactEmail+"\"}] }, {\"objectTypeAttributeId\": "+objectTypeAttributeIdContactName+", \"objectAttributeValues\": [{\"value\": \""+contactName+"\"}] }] }'";
		String url = localJiraRestUrl + "/insight/1.0/object/create";
		HttpEntity<String> request = getRequest(requestBody,true);

		RestTemplate restTemplate = new RestTemplate();
		log(request.toString());
		log(request.getHeaders().toString());
		ResponseEntity<String> response=null;
		try{
			response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
		} catch (HttpClientErrorException e){
			logErrorAndRethrowException(e);
		}
		
		String stringResponse = response.getBody();
		JSONObject org = new JSONObject(stringResponse);
		int contactId=org.getInt("id");
		log("finished createContactInInsight with companyId:" + contactCompanyId + " contactname:" + contactName + " contactEmail:" + contactEmail + " contactId:" + contactId);
	}	
	
	

	private static void log(String logMessage){
		LOGGER.error(logMessage);
	}
	
	private static void log(String logMessage, Exception ex){
		LOGGER.error(logMessage, ex);
	}

	private static String getCreds(){
		byte[] plainCredsBytes = plainCreds.getBytes();
		byte[] base64CredsBytes = Base64.getEncoder().encode(plainCredsBytes);
		String base64Creds = new String(base64CredsBytes);
		return base64Creds;
		
	}
}
