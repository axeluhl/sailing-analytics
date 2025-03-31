package com.sap.sse.security.impl;

import com.sap.sse.security.SecurityUrlPathProvider;

public class SecurityUrlPathProviderDefaultImpl implements SecurityUrlPathProvider{
    private static final String SECURITY_UI_URL_PATH = "/security/ui/";
    private static final String SSE_RESETPASSWORD = "EditProfile.html";

    @Override
    public String getPasswordResetUrlPath() {
        return SECURITY_UI_URL_PATH + SSE_RESETPASSWORD;
    }
}
