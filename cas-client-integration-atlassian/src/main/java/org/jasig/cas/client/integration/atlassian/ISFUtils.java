package org.jasig.cas.client.integration.atlassian;

import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.Base64;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class ISFUtils {

	public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException {
//		System.out.println("Starting");
//		String input = "PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0idXMtYXNjaWkiPz48dWlkIHZlcnNpb249IjMuMiIgeG1sbnM9InVybjp4bWwtZ292LWF1OmF0bzp1aWQtaXNmOjMuMiI+PGF1dGhlbnRpY2F0aW9uVHlwZT4yPC9hdXRoZW50aWNhdGlvblR5cGU+PHVwbj5QQThNSkxtQGdydW1weS5hdG9kbmV0Lmdvdi5hdTwvdXBuPjxpc2ZIb3BzPjEwLjE5LjIwMC4xMDIsMTAuMTk2LjY2LjI1NTozNDAwLDEwLjE5Ni42Ny4xMzM6ODEsMTI3LjAuMC4xOjM1MDA8L2lzZkhvcHM+PHNlc3Npb25JZD44ZmNlZDQyY2RiZGY0YzgyODgwZmFkMDk1ZjRiNmQyMzwvc2Vzc2lvbklkPjxyZXF1ZXN0SWQ+MGEwOTVkYWZhZDYwNDljODkwZmNlOWE3Zjc5ZGM2Njg8L3JlcXVlc3RJZD48Z2F0ZXdheT41PC9nYXRld2F5Pjx0aW1lU3RhbXA+MjAxOC0wMy0xNlQxMDoyOTowOCsxMTowMDwvdGltZVN0YW1wPjxhYm4+MjExNzM3MTk5NzE8L2Fibj48c3ViamVjdEROPkNOPVBFUkYgVVNFUiA5MDIxMCwgTz0yMTE3MzcxOTk3MSwgZG5RPTkwMjEyPC9zdWJqZWN0RE4+PGNyZWRlbnRpYWxUeXBlPjE8L2NyZWRlbnRpYWxUeXBlPjxlbWFpbD5hQGIuY29tPC9lbWFpbD48b2FtQ2FjaGUgLz48Y2xhaW1zPjxjbGFpbT48Y2xhaW1UeXBlPnVybjovL2F1Lmdvdi5hdG8vYXV0aGVudGljYXRpb24vTGFzdExvZ2dlZEluRGF0ZVRpbWU8L2NsYWltVHlwZT48dmFsdWU+MDAxNy0wNi0wNVQwODoxMjoxMFo8L3ZhbHVlPjwvY2xhaW0+PGNsYWltPjxjbGFpbVR5cGU+dXJuOi8vYXUuZ292LmF0by9hdXRoZW50aWNhdGlvbi9EaXNwbGF5R2l2ZW5OYW1lPC9jbGFpbVR5cGU+PHZhbHVlPkpvZTwvdmFsdWU+PC9jbGFpbT48Y2xhaW0+PGNsYWltVHlwZT51cm46Ly9hdS5nb3YuYXRvL2F1dGhlbnRpY2F0aW9uL0Rpc3BsYXlGYW1pbHlOYW1lPC9jbGFpbVR5cGU+PHZhbHVlPkJsb2dnczwvdmFsdWU+PC9jbGFpbT48Y2xhaW0+PGNsYWltVHlwZT51cm46Ly9hdS5nb3YuYXRvL2F1dGhlbnRpY2F0aW9uL0J1c2luZXNzQ2xpZW50Q291bnQ8L2NsYWltVHlwZT48dmFsdWU+MDwvdmFsdWU+PC9jbGFpbT48Y2xhaW0+PGNsYWltVHlwZT51cm46Ly9hdS5nb3YuYXRvL2F1dGhlbnRpY2F0aW9uL09yaWdpbmFsTG9naW5DaGFubmVsPC9jbGFpbVR5cGU+PHZhbHVlPkJyb3dzZXJTYW1sPC92YWx1ZT48L2NsYWltPjxjbGFpbT48Y2xhaW1UeXBlPnVybjovL2F1Lmdvdi5hdG8vYXV0aGVudGljYXRpb24vQ2VydGlmaWNhdGVGaW5nZXJwcmludDwvY2xhaW1UeXBlPjx2YWx1ZT4yMTE3MzcxOTk3MTkwMjEyMjExNzM3MTk5NzE5MDIxMjk3MTkwMjEyPC92YWx1ZT48L2NsYWltPjxjbGFpbT48Y2xhaW1UeXBlPnVybjovL2F1Lmdvdi5hdG8vYXV0aGVudGljYXRpb24vQWJyUGVyc29uSWQ8L2NsYWltVHlwZT48dmFsdWU+OTAyMTI8L3ZhbHVlPjwvY2xhaW0+PGNsYWltPjxjbGFpbVR5cGU+dXJuOi8vYXUuZ292LmF0by9hdXRoZW50aWNhdGlvbi9DdXJyZW50SWRlbnRpdHlQcm92aWRlcjwvY2xhaW1UeXBlPjx2YWx1ZT5WYW5HdWFyZFNhbWxCaW5kaW5nPC92YWx1ZT48L2NsYWltPjwvY2xhaW1zPjwvdWlkPg==";
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
		String upn = doc.getElementsByTagName("upn").item(0).getTextContent();
		String email = doc.getElementsByTagName("email").item(0).getTextContent();
		String abn = doc.getElementsByTagName("abn").item(0).getTextContent();
		return new ISFJiraIdentifiers(upn, email, abn);
	}
	
	public static String encodeToBase64(){
		String upn ="alex2";
		String email="alex2@ato.gov.au";
		String abn="11111111115";
		String plainTextGID="<?xml version=\"1.0\" encoding=\"us-ascii\"?><uid version=\"3.2\" xmlns=\"urn:xml-gov-au:ato:uid-isf:3.2\">"
				+ "<authenticationType>2</authenticationType><upn>"+upn+"</upn><isfHops>10.19.200.102,10.196.66.255:3400,10.196.67.133:81,127.0.0.1:3500</isfHops><sessionId>8fced42cdbdf4c82880fad095f4b6d23</sessionId><requestId>0a095dafad6049c890fce9a7f79dc668</requestId><gateway>5</gateway><timeStamp>2018-03-16T10:29:08+11:00</timeStamp><abn>"+abn+"</abn><subjectDN>CN=PERF USER 90210, O=21173719971, dnQ=90212</subjectDN><credentialType>1</credentialType><email>"+email+"</email><oamCache /><claims><claim><claimType>urn://au.gov.ato/authentication/LastLoggedInDateTime</claimType><value>0017-06-05T08:12:10Z</value></claim><claim><claimType>urn://au.gov.ato/authentication/DisplayGivenName</claimType><value>Joe</value></claim><claim><claimType>urn://au.gov.ato/authentication/DisplayFamilyName</claimType><value>Bloggs</value></claim><claim><claimType>urn://au.gov.ato/authentication/BusinessClientCount</claimType><value>0</value></claim><claim><claimType>urn://au.gov.ato/authentication/OriginalLoginChannel</claimType><value>BrowserSaml</value></claim><claim><claimType>urn://au.gov.ato/authentication/CertificateFingerprint</claimType><value>2117371997190212211737199719021297190212</value></claim><claim><claimType>urn://au.gov.ato/authentication/AbrPersonId</claimType><value>90212</value></claim><claim><claimType>urn://au.gov.ato/authentication/CurrentIdentityProvider</claimType><value>VanGuardSamlBinding</value></claim></claims></uid>";
		String base64UID = new String(Base64.getEncoder().encode(plainTextGID.getBytes()));
		System.out.println(base64UID);
		return base64UID;
	}

	public static void printIdentifiers(String base64String ) throws ParserConfigurationException, SAXException, IOException{
		ISFJiraIdentifiers identifiers=getISFIdentifiers(getXMLFromBase64String(base64String));
		System.out.println("ISF Identifiers:" + identifiers.toString());
	}
}
