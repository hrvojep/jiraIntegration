package org.jasig.cas.client.integration.atlassian;

public class ISFJiraIdentifiers {

	private String upn;
	private String email;
	private String abn;

	public ISFJiraIdentifiers(String upn, String email, String abn) {
		this.upn = upn;
		this.email = email;
		this.abn=abn;
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
	
	@Override
	public String toString() {
		return "ISFJiraIdentifiers [upn=" + upn + ", email=" + email + ", abn=" + abn + "]";
	}

}
