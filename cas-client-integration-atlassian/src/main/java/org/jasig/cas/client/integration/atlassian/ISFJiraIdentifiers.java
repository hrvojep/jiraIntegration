package org.jasig.cas.client.integration.atlassian;

import org.springframework.util.StringUtils;

public class ISFJiraIdentifiers {

	private String upn;
	private String email;
	private String abn;
	private String givenName;
	private String familyName;
	private String abrPersonId;
	private String jiraUserName;

	public String getAbrPersonId() {
		return abrPersonId;
	}

	public String getUpn() {
		return upn;
	}

	public String getEmail() {
		return email;
	}
	
	public String getAbn(){
		return abn;
	}

	public String getGivenName() {
		return givenName;
	}

	
	public String getFamilyName() {
		return familyName;
	}

	public String getFullName(){
		return getGivenName() + " " + getFamilyName();
	}
	
	public String getJiraUserName(){
		return jiraUserName;
	}
	
	public ISFJiraIdentifiers(String upn, String email, String abn, String givenName, String familyName, String abrPersonId) {
		super();
		this.upn = StringUtils.trimAllWhitespace(upn);
		this.email = StringUtils.trimAllWhitespace(email);
		this.abn = StringUtils.trimAllWhitespace(abn);
		this.givenName = StringUtils.trimAllWhitespace(givenName); 
		this.familyName = StringUtils.trimAllWhitespace(familyName);
		this.abrPersonId= StringUtils.trimAllWhitespace(abrPersonId);
		this.jiraUserName = abrPersonId + "-" + abn;
	}

	@Override
	public String toString() {
		return "ISFJiraIdentifiers [upn=" + upn + ", email=" + email + ", abn=" + abn + ", givenName=" + givenName
				+ ", familyName=" + familyName + ", abrPersonId=" + abrPersonId + ", jiraUserName=" + jiraUserName
				+ "]";
	}


	

}
