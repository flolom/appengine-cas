/* Copyright 2013 François Lolom
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package com.hotcocoacup.appenginecas;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import com.google.appengine.api.urlfetch.FetchOptions;
import com.google.appengine.api.urlfetch.HTTPHeader;
import com.google.appengine.api.urlfetch.HTTPMethod;
import com.google.appengine.api.urlfetch.HTTPRequest;
import com.google.appengine.api.urlfetch.HTTPResponse;
import com.google.appengine.api.urlfetch.URLFetchService;
import com.hotcocoacup.appenginecas.LoginTicketHandler.LoginTicketResponse;

/**
 * A client implementation of the CAS login for Google app engine
 * 
 */
public class CASAuthenticator {

	public static final boolean DEBUG_MODE = true;
	
	/**
	 * Parser used to parse the CAS HTML. You can change this with any SAX
	 * Parser implementation that supports HTML instead of XML
	 */
	private static final String SAX_PARSER = "org.ccil.cowan.tagsoup.Parser";

	private static final String FORMAT_CAS_URL = "%s?service=%s";
	private static final String FORMAT_LOGIN_PAYLOAD = "username=%s&password=%s&lt=%s&execution=%s&_eventId=submit";

	protected static final FetchOptions FETCH_OPTION = FetchOptions.Builder
			.withDefaults().doNotFollowRedirects();
	
	private static final HTTPHeader HEADER_URL_ENCODED = new HTTPHeader("Content-Type", "application/x-www-form-urlencoded");
	
	protected static final Logger sLogger = Logger.getLogger(CASAuthenticator.class.getSimpleName());
	static {
		sLogger.setLevel(DEBUG_MODE ? Level.ALL : Level.WARNING);
	}

	/**
	 * The url on which
	 */
	final protected String mCASBaseUrl;

	/**
	 * Create a {@link CASAuthenticator} an which you can authenticate different
	 * user on different services
	 * 
	 * @param casLoginUrl
	 *            URL of CAS login page
	 */
	public CASAuthenticator(String casLoginUrl) {
		mCASBaseUrl = casLoginUrl;
	}

	/**
	 * <p>
	 * This method tries to authenticate the following user to a service,
	 * through the CAS
	 * </p>
	 * 
	 * @param serviceUrl
	 *            the url which request a login authentication
	 * @param username
	 *            the name of the user who is trying to authenticate to this
	 *            service
	 * @param password
	 *            the raw password of the user
	 * @param urlFetch
	 *            an instance of the {@link URLFetchService} which will perform
	 *            the networks operations
	 * @return null if the login fails because the credentials provided are not
	 *         known by the CAS server, else, returns the {@link URL} that you
	 *         must call to continue the login process (on the service side this
	 *         time)
	 * @throws IOException
	 *             If the remote service could not be contacted or the URL could
	 *             not be fetched.
	 * @throws SAXException
	 *             If an error occurred while parsing (or trying to parse) the
	 *             HTML
	 * @throws IllegalStateException
	 *             Exception that should not happen if you are calling a proper
	 *             CAS Server. This exception may be triggered if you are not
	 *             calling
	 */
	public String authenticate(String serviceUrl, String username,
			String password, URLFetchService urlFetch) throws IOException,
			SAXException, IllegalStateException {

		String finalUrl = String.format(FORMAT_CAS_URL, mCASBaseUrl,
				URLEncoder.encode(serviceUrl, "UTF-8"));
		
		sLogger.fine("calling url: "+finalUrl);
		
		final URL loginUrl = new URL(finalUrl);
		HTTPRequest requestLoginTicket = new HTTPRequest(loginUrl,
				HTTPMethod.GET, FETCH_OPTION);

		HTTPResponse res = urlFetch.fetch(requestLoginTicket);

		// Process the server response
		// extracting the JSESSIONID
		String jsessionCookie = extractJessionId(res);
		sLogger.fine("jsess:"+jsessionCookie);
		
		// extracting the LoginTicket
		InputStream loginTicketStream = new ByteArrayInputStream(
				res.getContent());
		LoginTicketResponse loginTicket = extractLoginTicket(loginTicketStream);
		sLogger.fine("loginticket:"+loginTicket.lt);
		sLogger.fine("execution:"+loginTicket.execution);
		// performing the login request
		HTTPRequest requestLogin = buildLoginRequest(loginUrl, username,
				password, loginTicket, jsessionCookie);
		res = urlFetch.fetch(requestLogin);
		// check and returns the login result
		String redirection = extractRedirection(res);
		sLogger.fine("result is: "+redirection);
		return redirection;
	}

	private static String extractJessionId(HTTPResponse res)
			throws IllegalStateException {

		// Look for the JSESSIONID
		String jsessionCookie = null;
		for (HTTPHeader header : res.getHeadersUncombined()) {
			if (header.getName().equals("Set-Cookie")) {
				jsessionCookie = cookieFromHeader(header);
				break;
			}
		}

		// The server did not return the cookie. This should not happen...
		if (jsessionCookie == null) {
			throw new IllegalStateException(
					"JSESSIONID cookie not found in the server response (Header)");
		}
		return jsessionCookie;
	}

	private static LoginTicketResponse extractLoginTicket(InputStream serverResponse)
			throws IOException, SAXException, IllegalStateException {

		XMLReader reader = XMLReaderFactory.createXMLReader(SAX_PARSER);

		// Create the handler, who will contain the loginTicket, if found
		LoginTicketHandler handler = new LoginTicketHandler();

		// Parse the inputStream
		reader.setContentHandler(handler);
		reader.parse(new InputSource(serverResponse));

		// retrieve the loginTicket from the handler
		LoginTicketResponse res = new LoginTicketResponse();
		res.lt = handler.getLt();
		res.execution = handler.getExecution();

		// The server did not respond
		if (res.lt == null) {
			throw new IllegalStateException(
					"LoginTicket field not found in the server response (Content)");
		}
		
		if (res.execution == null) {
			throw new IllegalStateException(
					"Execution field not found in the server response (Content)");
		}
		
		return res;
	}

	private static HTTPRequest buildLoginRequest(URL loginUrl, String username,
			String password, LoginTicketResponse loginTicket, String sessionCookie) {
		try {
			HTTPRequest requestLogin = new HTTPRequest(loginUrl,
					HTTPMethod.POST, FETCH_OPTION);
			
			sLogger.fine("calling :"+loginUrl);

			String payload = String.format(FORMAT_LOGIN_PAYLOAD,
					URLEncoder.encode(username, "UTF-8"),
					URLEncoder.encode(password, "UTF-8"),
					URLEncoder.encode(loginTicket.lt, "UTF-8"),
					URLEncoder.encode(loginTicket.execution, "UTF-8"));

			requestLogin.setPayload(payload.getBytes());
			requestLogin.setHeader(HEADER_URL_ENCODED);
			requestLogin.setHeader(new HTTPHeader("Cookie", sessionCookie));

			return requestLogin;

		} catch (UnsupportedEncodingException e) {
			// Cannot happen...
			throw new IllegalStateException("UTF-8 not supported", e);
		}
	}

	private static String extractRedirection(HTTPResponse res) {
		// Look for the Location header if any.
		// If the Location is present, it means that the login is successful
		for (HTTPHeader header : res.getHeadersUncombined()) {
			if (header.getName().equals("Location")) {
				return header.getValue();
			}
		}
		return null;
	}

	/**
	 * Extract a Cookie string from an {@link HTTPHeader} object, with just its key-value, on this format :
	 * 
	 * <pre>
	 * key : value
	 * </pre>
	 * 
	 * @param header
	 *            an HTTPHeader, containing ONLY ONE cookie on its value
	 *            attribute
	 * @return the Cookie extracted
	 * @see {@link HTTPResponse#getHeadersUncombined()} to extract only one
	 *      Header at a time. This method will create only one
	 *      {@link HTTPHeader} per cookie (if several found)
	 */
	protected final static String cookieFromHeader(HTTPHeader header) {

		if (!header.getName().equals("Set-Cookie")) {
			throw new IllegalArgumentException(
					"The HTTPHeader provided must be a 'Set-Cookie' header");
		}
		String headerValue = header.getValue().trim();

		// Extract only the raw cookie, without any information, like the
		// expiration date, etc.
		int separator = headerValue.indexOf(';');
		String cookieString = separator != -1 ? headerValue.substring(0,
				separator) : headerValue;

		return cookieString;
	}

}