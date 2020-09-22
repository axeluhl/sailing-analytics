package com.sap.sailing.server.impl;

import com.sap.sse.security.SecurityUrlPathProvider;

public class SecurityUrlPathProviderSailingImpl implements SecurityUrlPathProvider{
    public static final String APPLICATION = "sailing";
    
    private static final String SAILING_GWT_UI_PATH = "/gwt";
    private static final String HOME_HTML = "/Home.html";
    private static final String PASSWORD_RESET_PLACE_PATH = "#/user/passwordreset/:";

    @Override
    public String getPasswordResetUrlPath() {
        return SAILING_GWT_UI_PATH + HOME_HTML + PASSWORD_RESET_PLACE_PATH;
    }

}
