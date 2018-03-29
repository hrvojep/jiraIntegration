package org.jasig.cas.client.integration.atlassian;

public class ISFJiraIdentifiers {

	private String upn;
	private String email;

	public ISFJiraIdentifiers(String upn, String email) {
		this.upn = upn;
		this.email = email;
	}

	public String getUpn() {
		return upn;
	}

	public String getEmail() {
		return email;
	}
	
	public String toString() {
		return "ISFJiraIdentifiers [upn=" + upn + ", email=" + email + "]";
	}

}
