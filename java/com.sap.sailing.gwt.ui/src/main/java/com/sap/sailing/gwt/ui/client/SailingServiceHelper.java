package com.sap.sailing.gwt.ui.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.sap.sse.gwt.client.ServiceRoutingProvider;

public abstract class SailingServiceHelper {
    private final static String moduleBaseURL = GWT.getModuleBaseURL();

    private SailingServiceHelper() {
    }

    public static SailingServiceAsync createSailingServiceInstance() {
        return createSailingServiceInstance(true, null);
    }

    public static SailingServiceAsync createSailingServiceInstance(boolean sameBundle) {
        return createSailingServiceInstance(sameBundle, null);
    }

    public static SailingServiceAsync createSailingServiceInstance(ServiceRoutingProvider routingProvider) {
        return createSailingServiceInstance(true, routingProvider);
    }
    
  
    private static SailingServiceAsync createSailingServiceInstance(boolean sameBundle, ServiceRoutingProvider routingProvider) {
        SailingServiceAsync service = GWT.create(SailingService.class);
        ServiceDefTarget serviceToRegister = (ServiceDefTarget) service;
        StringBuilder baseURL = new StringBuilder();
        baseURL.append(moduleBaseURL.substring(0, moduleBaseURL.lastIndexOf('/', moduleBaseURL.length() - 2) + 1));
        if (!sameBundle) {
            baseURL.append(RemoteServiceMappingConstants.WEB_CONTEXT_PATH).append("/");
        }
        baseURL.append(RemoteServiceMappingConstants.sailingServiceRemotePath);
        if (routingProvider != null) {
            baseURL.append("/").append(routingProvider.routingSuffixPath());
        }
        serviceToRegister.setServiceEntryPoint(baseURL.toString());
        return service;
    }
}
