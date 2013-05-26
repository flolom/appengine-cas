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

package com.hotcocoacup.appenginecas.sample;

import java.io.IOException;
import java.net.URL;

import org.xml.sax.SAXException;

import com.google.appengine.api.urlfetch.HTTPHeader;
import com.google.appengine.api.urlfetch.HTTPMethod;
import com.google.appengine.api.urlfetch.HTTPRequest;
import com.google.appengine.api.urlfetch.HTTPResponse;
import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;
import com.hotcocoacup.appenginecas.CASAuthenticator;

public class MoodleCASAuthenticator extends CASAuthenticator {

	private String mMoodleUrl;
	
	private static final String TEST_SESSION = "MoodleSessionTest=";
	private static final String COOKIE_SESSION = "MoodleSession=";

	public MoodleCASAuthenticator(String moodleLoginUrl, String casLoginUrl) {
		super(casLoginUrl);

		mMoodleUrl = moodleLoginUrl;
	}

	/**
	 * Authenticate a user to the Moodle platform, with a CAS authentication 
	 * 
	 * @param email email of the user who whant to be logged in
	 * @param password raw password of the user
	 * @return null is the credentials 
	 * @throws IllegalStateException
	 * @throws IOException
	 * @throws SAXException
	 * @see {@link CASAuthenticator#authenticate(String, String, String, URLFetchService)}
	 */
	public MoodleSession authenticateToMoodle(String email, String password)
			throws IllegalStateException, IOException, SAXException {

		URLFetchService urlFetch = URLFetchServiceFactory.getURLFetchService();

		// Authenticate to the CAS
		String urlWithToken = authenticate(mMoodleUrl, email, password,
				urlFetch);

		// if login is successful
		if (urlWithToken != null) {

			// First connection to moodle
			// The purpose is to give the ticket and wait for a redirection
			HTTPRequest ticketECampus = new HTTPRequest(new URL(urlWithToken),
					HTTPMethod.GET, FETCH_OPTION);

			HTTPResponse res = urlFetch.fetch(ticketECampus);

			// We must receive two cookies (because it's the first connection)
			// and one redirection (=login successful)
			String moodleSession = null;
			String moodleSessionTest = null;
			String secondPageMoodle = null;

			// We are searching for the some headers
			for (HTTPHeader header : res.getHeadersUncombined()) {

				if (header.getName().equals("Set-Cookie")) {
					String cookieFound = cookieFromHeader(header);
					if (cookieFound.startsWith(COOKIE_SESSION)) {
						moodleSession = cookieFound;
					} else if (cookieFound.startsWith(TEST_SESSION)) {
						moodleSessionTest = cookieFound;
					}
				} else if (header.getName().equals("Location")) {
					secondPageMoodle = header.getValue();
				}
			}

			// We test if all the data needed were found
			if (moodleSession == null) {
				throw new IllegalStateException(
						"Impossible to find the MoodleSession cookie, after a sucessful login to CAS");
			} else if (moodleSessionTest == null) {
				throw new IllegalStateException(
						"Impossible to find the MoodleSessionTest cookie, after a sucessful login to CAS");
			} else if (secondPageMoodle == null) {
				throw new IllegalStateException("No redirection found");
			}

			sLogger.fine("Tmp session: "+moodleSession);
			sLogger.fine("test cookie: "+moodleSessionTest);
			sLogger.fine("redirection: "+secondPageMoodle);
			
			// Final call, to get the real session cookie
			HTTPRequest requestSessionCookies = new HTTPRequest(new URL(
					secondPageMoodle), HTTPMethod.GET, FETCH_OPTION);

			requestSessionCookies.setHeader(new HTTPHeader("Cookie",
					moodleSession + "; "+moodleSessionTest));

			res = urlFetch.fetch(requestSessionCookies);
			String validSessionCookie = null;

			// extract the new MoodleSession (user is logged with this session)
			for (HTTPHeader header : res.getHeadersUncombined()) {
				if (header.getName().equals("Set-Cookie")) {
					String cookieFound = cookieFromHeader(header);

					if (cookieFound.startsWith(COOKIE_SESSION)) {
						validSessionCookie = cookieFound.substring(COOKIE_SESSION.length());
						break;
					}
				}
			}

			if (validSessionCookie == null ) {
				throw new IllegalStateException(
						"Impossible to find the MoodleSession cookie, during the second redirection");
			}

			return new MoodleSession(validSessionCookie, moodleSessionTest.substring(TEST_SESSION.length()));
		}

		return null;
	}

}
