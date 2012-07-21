package com.sap.sailing.gwt.ui.client;

public interface UserAgentChecker {
    
    public enum UserAgentTypes{ DESKTOP, MOBILE, UNKNOWN }
    
    public final UserAgentCheckerImpl INSTANCE = new UserAgentCheckerImpl();
    
    public UserAgentTypes checkUserAgent(String userAgent);
    
    public boolean isUserAgentSupported(String userAgent);

}
