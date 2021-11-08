package com.sap.sailing.shared.server.security;

import com.sap.sse.security.BearerTokenOrBasicOrFormOrAnonymousAuthenticationFilter;

public class BearerTokenOrBasicOrFormOrAnonymousAuthenticationFilterSAPSailing
        extends BearerTokenOrBasicOrFormOrAnonymousAuthenticationFilter {
    public BearerTokenOrBasicOrFormOrAnonymousAuthenticationFilterSAPSailing() {
        super(BearerTokenOrBasicOrFormAuthenticationFilterSAPSailing.SAP_SAILING_ANALYTICS_APPLICATION_NAME);
    }
}
