package com.sap.sailing.gwt.ui.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.sap.sse.gwt.client.ServiceRoutingProvider;

/**
 * Set of convenience factory methods that create a {@link SailingServiceAsync} instance. 
 */
public abstract class SailingServiceHelper {
    private final static String moduleBaseURL = GWT.getModuleBaseURL();

    private SailingServiceHelper() {
    }

    /**
     * Creates a new {@link SailingServiceAsync} instance, should be used when code resides in the same bundle as the sailing service code.
     */
    public static SailingServiceAsync createSailingServiceInstance() {
        return createSailingServiceInstance(true, null);
    }
    /**
     * Creates a new {@link SailingServiceAsync} instance.
     * 
     * @param sameBundle {@code false} if using {@link SailingService} from a different OSGi bundle (e.g. dashboards), {@code true} otherwise
     */
    public static SailingServiceAsync createSailingServiceInstance(boolean sameBundle) {
        return createSailingServiceInstance(sameBundle, null);
    }

    /**
     * Creates a new {@link SailingServiceAsync} instance that uses a routing provider, for code in same bundle.
     */
    public static SailingServiceAsync createSailingServiceInstance(ServiceRoutingProvider routingProvider) {
        return createSailingServiceInstance(true, routingProvider);
    }

    /**
     * Crates a new {@link SailingServiceAsync} instance.
     * 
     * @param sameBundle {@code false} if using {@link SailingService} from a different OSGi bundle (e.g. dashboards), {@code true} otherwise
     */
    public static SailingServiceAsync createSailingServiceInstance(boolean sameBundle, ServiceRoutingProvider routingProvider) {
        SailingServiceAsync service = GWT.create(SailingService.class);
        ServiceDefTarget serviceToRegister = (ServiceDefTarget) service;
        StringBuilder baseURL = new StringBuilder();
        baseURL.append(moduleBaseURL.substring(0, moduleBaseURL.lastIndexOf('/', moduleBaseURL.length() - 2) + 1));
        if (!sameBundle) {
            baseURL.append(RemoteServiceMappingConstants.WEB_CONTEXT_PATH).append("/");
        }
        
        baseURL.append(RemoteServiceMappingConstants.sailingServiceRemotePath);
        
        if (routingProvider != null) {
            baseURL.append(routingProvider.routingSuffixPath());
        }
        serviceToRegister.setServiceEntryPoint(baseURL.toString());
        return service;
    }
}
