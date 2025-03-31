package com.sap.sse.gwt.client.useragent;

public interface UserAgentChecker {
    
    public final UserAgentChecker INSTANCE = new UserAgentCheckerImpl();

    /**
	 * Returns false if the given userAgent is not supported.
	 * 
     * @param userAgent the user agent string as provided by Window.Navigator.getUserAgent()
     * @return true if the agent is supported
     */
    public boolean isUserAgentSupported(UserAgentDetails details);

}
