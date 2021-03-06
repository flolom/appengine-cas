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

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class MoodleCASLoginServlet extends HttpServlet {
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {

		MoodleCASAuthenticator auth = new MoodleCASAuthenticator(req.getParameter("serviceurl"), req.getParameter("casurl"));

		String response;
		try {
			MoodleSession authResult = auth.authenticateToMoodle(
					req.getParameter("email"), req.getParameter("password")
					);
			
			if (authResult == null) {
				response = "The email/password does not match";
			} else {
				response = "Cookie to reinject during the session = " + authResult.toString();
			}

		} catch (Exception e) {
			response = e.getMessage();
		}

		resp.setContentType("text/plain");
		resp.getWriter().println(response);
	}
}
