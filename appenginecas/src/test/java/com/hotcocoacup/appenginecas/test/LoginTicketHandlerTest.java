package com.hotcocoacup.appenginecas.test;

import java.io.IOException;
import java.io.InputStream;

import junit.framework.Assert;

import org.junit.Test;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import com.hotcocoacup.appenginecas.LoginTicketHandler;


public class LoginTicketHandlerTest {

	@Test
	public void parseTest() throws IOException, SAXException {
		
		InputStream in = getClass().getClassLoader().getResourceAsStream("cas-simple.html");
		
		XMLReader reader = XMLReaderFactory.createXMLReader("org.ccil.cowan.tagsoup.Parser");

		// Create the handler, who will contain the loginTicket, if found
		LoginTicketHandler handler = new LoginTicketHandler();

		// Parse the inputStream
		reader.setContentHandler(handler);
		reader.parse(new InputSource(in));
		
		String execution = handler.getExecution();
		String lt = handler.getLt();
		
		Assert.assertEquals("LT-732298-TUWOcBPRsOcHZYZmb7BwYXHsQT9Rbi", lt);
		Assert.assertEquals("e2s1", execution);
	}

}
