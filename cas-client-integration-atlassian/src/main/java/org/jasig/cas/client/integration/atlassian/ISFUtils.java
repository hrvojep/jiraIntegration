package org.jasig.cas.client.integration.atlassian;

import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Base64;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class ISFUtils {

	public static void main(String[] args) throws Exception {
		System.out.println("Starting");
//		String input = "PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0idXMtYXNjaWkiPz48dWlkIHZlcnNpb249IjMuMiIgeG1sbnM9InVybjp4bWwtZ292LWF1OmF0bzp1aWQtaXNmOjMuMiI+PGF1dGhlbnRpY2F0aW9uVHlwZT4yPC9hdXRoZW50aWNhdGlvblR5cGU+PHVwbj5QQkZRVll3QGluZmFjY20uYXRvZG5ldC5nb3YuYXU8L3Vwbj48aXNmSG9wcz4xMC43LjI1NC4xMzQsMTAuMTkyLjIyLjE4Mzo2NTAwLDEwLjE5Mi4xOC4yMzg6ODEsMTI3LjAuMC4xOjY2MDA8L2lzZkhvcHM+PHNlc3Npb25JZD5kYjJlNzJlNGU4MTk0MzNjYTkxY2I0MDdmMGE0YWMzNDwvc2Vzc2lvbklkPjxyZXF1ZXN0SWQ+MmEwZjdkMTkyNmNmNDY5NjkyYWVkODQ1NjJkMTI1NDY8L3JlcXVlc3RJZD48Z2F0ZXdheT41PC9nYXRld2F5Pjx0aW1lU3RhbXA+MjAxOC0wNC0yN1QxMjozNToxMisxMDowMDwvdGltZVN0YW1wPjxhYm4+MjExNzM3MTk5NzE8L2Fibj48c3ViamVjdEROPkNOPVBFUkYgVVNFUiA5MDIxMCwgTz0yMTE3MzcxOTk3MSwgZG5RPTkwMjEyPC9zdWJqZWN0RE4+PGNyZWRlbnRpYWxUeXBlPjE8L2NyZWRlbnRpYWxUeXBlPjxlbWFpbD5hQGIuY29tPC9lbWFpbD48b2FtQ2FjaGUgLz48Y2xhaW1zPjxjbGFpbT48Y2xhaW1UeXBlPnVybjovL2F1Lmdvdi5hdG8vYXV0aGVudGljYXRpb24vTGFzdExvZ2dlZEluRGF0ZVRpbWU8L2NsYWltVHlwZT48dmFsdWU+MjAxOC0wNC0yN1QwMjozMzo1Mlo8L3ZhbHVlPjwvY2xhaW0+PGNsYWltPjxjbGFpbVR5cGU+dXJuOi8vYXUuZ292LmF0by9hdXRoZW50aWNhdGlvbi9EaXNwbGF5R2l2ZW5OYW1lPC9jbGFpbVR5cGU+PHZhbHVlPkpvZTwvdmFsdWU+PC9jbGFpbT48Y2xhaW0+PGNsYWltVHlwZT51cm46Ly9hdS5nb3YuYXRvL2F1dGhlbnRpY2F0aW9uL0Rpc3BsYXlGYW1pbHlOYW1lPC9jbGFpbVR5cGU+PHZhbHVlPkJsb2dnczwvdmFsdWU+PC9jbGFpbT48Y2xhaW0+PGNsYWltVHlwZT51cm46Ly9hdS5nb3YuYXRvL2F1dGhlbnRpY2F0aW9uL0J1c2luZXNzQ2xpZW50Q291bnQ8L2NsYWltVHlwZT48dmFsdWU+MTwvdmFsdWU+PC9jbGFpbT48Y2xhaW0+PGNsYWltVHlwZT51cm46Ly9hdS5nb3YuYXRvL2F1dGhlbnRpY2F0aW9uL0NsaWVudDwvY2xhaW1UeXBlPjx2YWx1ZT4mbHQ7Y2xpZW50Jmd0OyZsdDtpZGVudGl0eVR5cGUmZ3Q7YnVzaW5lc3MmbHQ7L2lkZW50aXR5VHlwZSZndDsmbHQ7aW50ZXJuYWxJZCZndDsyMDAwMDAwMDAwMDMwJmx0Oy9pbnRlcm5hbElkJmd0OyZsdDtleHRlcm5hbElkcyZndDsmbHQ7ZXh0ZXJuYWxJZCZndDsmbHQ7dHlwZSZndDsxMCZsdDsvdHlwZSZndDsmbHQ7dmFsdWUmZ3Q7MjExNzM3MTk5NzEmbHQ7L3ZhbHVlJmd0OyZsdDsvZXh0ZXJuYWxJZCZndDsmbHQ7L2V4dGVybmFsSWRzJmd0OyZsdDsvY2xpZW50Jmd0OzwvdmFsdWU+PC9jbGFpbT48Y2xhaW0+PGNsYWltVHlwZT51cm46Ly9hdS5nb3YuYXRvL2F1dGhlbnRpY2F0aW9uL09yaWdpbmFsTG9naW5DaGFubmVsPC9jbGFpbVR5cGU+PHZhbHVlPkJyb3dzZXJTYW1sPC92YWx1ZT48L2NsYWltPjxjbGFpbT48Y2xhaW1UeXBlPnVybjovL2F1Lmdvdi5hdG8vYXV0aGVudGljYXRpb24vQ3VycmVudElkZW50aXR5UHJvdmlkZXI8L2NsYWltVHlwZT48dmFsdWU+VmFuR3VhcmRTYW1sQmluZGluZzwvdmFsdWU+PC9jbGFpbT48L2NsYWltcz48L3VpZD4=";		
//		System.out.println(URLEncoder.encode(input,"UTF-8"));
		
//		input = URLDecoder.decode(input, "UTF-8");		
//		String ISFtokenAsXML = ISFUtils.getXMLFromBase64String(input.trim());
//		System.out.println("UID as XML:" + ISFtokenAsXML );
//		System.out.println(ISFUtils.getISFIdentifiers(ISFtokenAsXML).toString());
		
		printIdentifiers(encodeToBase64());
	}

	public static String getXMLFromBase64String(String base64Uid) throws UnsupportedEncodingException {
		return new String(Base64.getDecoder().decode(base64Uid));
	}

	
	public static ISFJiraIdentifiers getISFIdentifiers(String xmlString)
			throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		InputSource src = new InputSource(new StringReader(xmlString));
		Document doc = builder.parse(src);
		String upn = doc.getElementsByTagName("upn").item(0).getTextContent().trim();
		String email = doc.getElementsByTagName("email").item(0).getTextContent().trim();
		String abn = doc.getElementsByTagName("abn").item(0).getTextContent().trim();
	    String givenName="";
	    String familyName="";
	    String abrPersonId="";

	    NodeList nodeList = doc.getElementsByTagName("claims").item(0).getChildNodes();
	    int claimSize =  nodeList.getLength();
	    
	    for (int i=0; i<claimSize;i++){
			Node claimNode =  nodeList.item(i);
			
			String claimType = claimNode.getChildNodes().item(0).getTextContent();
			String claimValue = claimNode.getChildNodes().item(1).getTextContent();
			
			if (claimType.contains("DisplayGivenName")){
				givenName = claimValue;
			}
			if (claimType.contains("DisplayFamilyName")){
				familyName = claimValue;
			}
			if (claimType.contains("AbrPersonId")){
				abrPersonId = claimValue;
			}
			
			
		}
		return new ISFJiraIdentifiers(upn, email, abn,givenName,familyName,abrPersonId);
	}

	
	
	public static String encodeToBase64(){
		String upn ="user17@test.com.au";
		String email="user17@test.com.au";
		String abn="1117";
		String name="seventeen";
		String surname="test";
		String abrPersonId="2217";
		
		String org = StringUtils.reverse(("ua"));
		String org1 = StringUtils.reverse("vog");
		String org2 = StringUtils.reverse("ota");
		
		String org3 = org+"."+org1+"."+org2;
		
		String plainTextGID="<?xml version=\"1.0\" encoding=\"us-ascii\"?><uid version=\"3.2\" >"
				+ "<authenticationType>2</authenticationType><upn>"+upn+"</upn><sessionId>8fced42cdbdf4c82880fad095f4b6d23</sessionId><requestId>0a095dafad6049c890fce9a7f79dc668</requestId><gateway>5</gateway><timeStamp>2018-03-16T10:29:08+11:00</timeStamp><abn>"+abn+"</abn><subjectDN>CN=PERF USER 90210, O=21173719971, dnQ=90212</subjectDN><credentialType>1</credentialType><email>"+email+"</email><oamCache /><claims><claim><claimType>urn://"+org3+"/authentication/LastLoggedInDateTime</claimType><value>0017-06-05T08:12:10Z</value></claim><claim><claimType>urn://"+org3+"/authentication/DisplayGivenName</claimType><value>"+name+"</value></claim><claim><claimType>urn://"+org3+"/authentication/DisplayFamilyName</claimType><value>"+surname+"</value></claim><claim><claimType>urn://"+org3+"/authentication/BusinessClientCount</claimType><value>0</value></claim><claim><claimType>urn://"+org3+"/authentication/OriginalLoginChannel</claimType><value>BrowserSaml</value></claim><claim><claimType>urn://"+org3+"/authentication/CertificateFingerprint</claimType><value>2117371997190212211737199719021297190212</value></claim><claim><claimType>urn://"+org3+"/authentication/AbrPersonId</claimType><value>"+abrPersonId+"</value></claim><claim><claimType>urn://"+org3+"/authentication/CurrentIdentityProvider</claimType><value>VanGuardSamlBinding</value></claim></claims></uid>";
		String base64UID = new String(Base64.getEncoder().encode(plainTextGID.getBytes()));
		
		System.out.println(base64UID);
	    System.out.println();
	    try {
			System.out.println(URLEncoder.encode(base64UID, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return base64UID;
	}

	public static void printIdentifiers(String base64String ) throws Exception{
		String isfTokenAsXMLString = getXMLFromBase64String(base64String);
		//System.out.println(isfTokenAsXMLString);
		System.out.println();
		//extractISFUserName(isfTokenAsXMLString);
		ISFJiraIdentifiers identifiers=getISFIdentifiers(isfTokenAsXMLString);
		System.out.println("ISF Identifiers:" + identifiers.toString());
	}
}
