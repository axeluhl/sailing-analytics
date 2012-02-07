package com.sap.sailing.gwt.ui.client;

public interface UserAgentChecker {
    
    public enum UserAgentTypes{ DESKTOP, MOBILE }
    
    public final UserAgentCheckerImpl INSTANCE = new UserAgentCheckerImpl();
    
    public UserAgentTypes checkUserAgent(String userAgent);

}
