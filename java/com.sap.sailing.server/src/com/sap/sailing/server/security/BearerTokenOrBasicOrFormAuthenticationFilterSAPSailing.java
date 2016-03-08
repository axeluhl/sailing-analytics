package com.sap.sailing.server.security;

import com.sap.sse.security.BearerTokenOrBasicOrFormAuthenticationFilter;

public class BearerTokenOrBasicOrFormAuthenticationFilterSAPSailing extends
        BearerTokenOrBasicOrFormAuthenticationFilter {
    public BearerTokenOrBasicOrFormAuthenticationFilterSAPSailing() {
        super(/* application name */ "SAP Sailing Analytics");
    }
}
