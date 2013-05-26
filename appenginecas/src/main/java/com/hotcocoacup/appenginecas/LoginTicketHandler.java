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

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * SAX ContentHandler for extracting a CAS login ticket from an HTML page
 *
 */
public class LoginTicketHandler extends DefaultHandler {

	public static class LoginTicketResponse {
		public String lt;
		public String execution;
	}
	
	private static final String TAG_INPUT = "input";
	private static final String TAG_INPUT_NAME = "name";

	private static final String TAG_LT = "lt";
	private static final String TAG_LT_VALUE = "value";

	private static final String TAG_EXECUTION = "execution";
	private static final String TAG_EXECUTION_VALUE = "value";
	
	private String mLt;
	private String mExecution;
	
	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		
		// Searching for any input tag
		if (TAG_INPUT.equals(localName)) {
			String inputName = attributes.getValue(TAG_INPUT_NAME);
			
			// Extract the "name" attribute and test its value.
			// If the "name" matched, extract its "value" attribute
			if (TAG_LT.equals(inputName)) {
				mLt = attributes.getValue(TAG_LT_VALUE);

			} else if (TAG_EXECUTION.equals(inputName)) {
				mExecution = attributes
						.getValue(TAG_EXECUTION_VALUE);
			}
		}
	}
	
	/**
	 * Getter for the Login ticket
	 * 
	 * @return if found, returns the login ticket, else returns null.
	 */
	public String getLt() {
		return mLt;
	}
	
	/**
	 * Getter for the execution field.
	 * Note that normally, only the Login Ticket is required
	 * 
	 * @return if found, returns the execution field, else returns null.
	 * @
	 */
	public String getExecution() {
		return mExecution;
	}
	
}
