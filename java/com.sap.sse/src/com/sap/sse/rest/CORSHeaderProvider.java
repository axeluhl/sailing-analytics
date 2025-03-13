package com.sap.sse.rest;

import java.util.HashMap;
import java.util.Map;

import com.sap.sse.impl.Activator;

public interface CORSHeaderProvider {
    default Map<String, String> getCORSHeaders(String origin) {
        final Map<String, String> responseCORSHeaders = new HashMap<>();
        final CORSFilterConfiguration corsFilterConfiguration = Activator.getInstance().getCORSFilterConfiguration();
        final String allowOrigin;
        if (corsFilterConfiguration.isWildcard()) {
            allowOrigin = "*";
        } else {
            if (corsFilterConfiguration.contains(origin)) {
                allowOrigin = origin;
            } else {
                allowOrigin = null;
            }
        }
        if (allowOrigin != null) {
            responseCORSHeaders.put("Access-Control-Allow-Origin", allowOrigin);
            responseCORSHeaders.put("Vary", "Origin"); // don't cache this; it may vary depending on origin
        }
        responseCORSHeaders.put("Access-Control-Allow-Credentials", "true");
        responseCORSHeaders.put("Access-Control-Allow-Headers", "Authorization, Origin, X-Requested-With, Content-Type");
        responseCORSHeaders.put("Access-Control-Expose-Headers", "Location, Content-Disposition");
        responseCORSHeaders.put("Access-Control-Allow-Methods", "POST, PUT, GET, DELETE, HEAD, OPTIONS");
        return responseCORSHeaders;
    }
}
