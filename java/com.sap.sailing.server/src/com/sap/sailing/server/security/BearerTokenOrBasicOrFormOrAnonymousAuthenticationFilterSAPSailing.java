package com.sap.sailing.server.security;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import com.sap.sse.ServerInfo;
import com.sap.sse.security.BearerTokenOrBasicOrFormAuthenticationFilter;

public class BearerTokenOrBasicOrFormOrAnonymousAuthenticationFilterSAPSailing
        extends BearerTokenOrBasicOrFormAuthenticationFilter {

    public BearerTokenOrBasicOrFormOrAnonymousAuthenticationFilterSAPSailing() {
        super("SAP Sailing Analytics (" + /* application name */ ServerInfo.getName() + ")");
    }

    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
        super.onAccessDenied(request, response);
        return true;
    }

}
