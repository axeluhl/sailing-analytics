package com.sap.sailing.shared.server.security;

import com.sap.sse.ServerInfo;
import com.sap.sse.security.BearerTokenOrBasicOrFormAuthenticationFilter;

public class BearerTokenOrBasicOrFormAuthenticationFilterSAPSailing extends
        BearerTokenOrBasicOrFormAuthenticationFilter {

    protected static final String SAP_SAILING_ANALYTICS_APPLICATION_NAME = "SAP Sailing Analytics (" + /* application name */ ServerInfo.getName() + ")";

    public BearerTokenOrBasicOrFormAuthenticationFilterSAPSailing() {
        super(SAP_SAILING_ANALYTICS_APPLICATION_NAME);
    }
}
