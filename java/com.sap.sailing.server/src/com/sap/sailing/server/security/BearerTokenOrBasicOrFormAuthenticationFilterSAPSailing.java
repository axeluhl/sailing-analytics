package com.sap.sailing.server.security;

import com.sap.sse.security.BearerTokenOrBasicOrFormAuthenticationFilter;

public class BearerTokenOrBasicOrFormAuthenticationFilterSAPSailing extends
        BearerTokenOrBasicOrFormAuthenticationFilter {
    public BearerTokenOrBasicOrFormAuthenticationFilterSAPSailing() {
        super(/* application name */ System.getProperty("com.sap.sailing.server.name", "SAP Sailing Analytics"));
    }
}
