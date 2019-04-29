package com.sap.sailing.server.security;

import com.sap.sse.ServerInfo;
import com.sap.sse.security.BearerTokenOrBasicOrFormAuthenticationFilter;

public class BearerTokenOrBasicOrFormAuthenticationFilterSAPSailing extends
        BearerTokenOrBasicOrFormAuthenticationFilter {

    public BearerTokenOrBasicOrFormAuthenticationFilterSAPSailing() {
        super("SAP Sailing Analytics (" + /* application name */ ServerInfo.getName() + ")");
    }
}
