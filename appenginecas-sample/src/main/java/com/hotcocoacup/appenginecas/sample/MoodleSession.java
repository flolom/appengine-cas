package com.hotcocoacup.appenginecas.sample;

import java.io.Serializable;

public class MoodleSession implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String COOKIE_SESSION_NAME = "MoodleSession";
	public static final String COOKIE_TEST_NAME = "MoodleSessionTest";

	private String cookieSession;
	private String cookieTest;

	public MoodleSession() {
	}

	public MoodleSession(String cookieSession, String cookieTest) {
		this.cookieSession = cookieSession;
		this.cookieTest = cookieTest;
	}

	public String getCookieSession() {
		return cookieSession;
	}

	public String getCookieTest() {
		return cookieTest;
	}

	@Override
	public String toString() {
		return COOKIE_SESSION_NAME + "=" + cookieSession + "; "
				+ COOKIE_TEST_NAME + "=" + cookieTest;
	}
}
