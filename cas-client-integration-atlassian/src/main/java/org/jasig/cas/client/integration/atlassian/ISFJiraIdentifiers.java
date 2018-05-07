package org.jasig.cas.client.integration.atlassian;

public class ISFJiraIdentifiers {

	private String upn;
	private String email;
	private String abn;
	private String givenName;
	private String familyName;


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
	
	public ISFJiraIdentifiers(String upn, String email, String abn, String givenName, String familyName) {
		super();
		this.upn = upn;
		this.email = email;
		this.abn = abn;
		this.givenName = givenName;
		this.familyName = familyName;
	}

	@Override
	public String toString() {
		return "ISFJiraIdentifiers [upn=" + upn + ", email=" + email + ", abn=" + abn + ", givenName=" + givenName
				+ ", familyName=" + familyName + "]";
	}

}
