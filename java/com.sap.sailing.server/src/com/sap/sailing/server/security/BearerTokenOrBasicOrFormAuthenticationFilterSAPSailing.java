package com.sap.sailing.server.security;

import com.sap.sse.ServerStartupConstants;
import com.sap.sse.security.BearerTokenOrBasicOrFormAuthenticationFilter;

public class BearerTokenOrBasicOrFormAuthenticationFilterSAPSailing extends
        BearerTokenOrBasicOrFormAuthenticationFilter {
    public BearerTokenOrBasicOrFormAuthenticationFilterSAPSailing() {
        super("SAP Sailing Analytics ("+/* application name */ ServerStartupConstants.SERVER_NAME+")");
    }
}
