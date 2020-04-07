package com.sap.sse;

public interface ServerStartupConstants {
    final static String SERVER_NAME = System.getProperty("com.sap.sailing.server.name", "unknown server name");
    final static String JETTY_HOME = System.getProperty("jetty.home");
    final static String MANAGE_EVENTS_URL = System.getProperty("com.sap.sailing.eventmanagement.url", "https://my.sapsailing.com");
}
