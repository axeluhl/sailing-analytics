package com.sap.sailing.gwt.ui.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.sap.sse.gwt.client.EntryPointHelper;
import com.sap.sse.gwt.client.ServiceRoutingProvider;

/**
 * Set of convenience factory methods that create a {@link SailingServiceAsync} instance. 
 */
public abstract class SailingServiceHelper {
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
        final SailingServiceAsync service = GWT.create(SailingService.class);
        final ServiceDefTarget serviceToRegister = (ServiceDefTarget) service;
        
        final StringBuilder servicePath = new StringBuilder(RemoteServiceMappingConstants.sailingServiceRemotePath);
        if (routingProvider != null) {
            servicePath.append(routingProvider.routingSuffixPath());
        }
        
        final String servicePathWithRoutingSuffix = servicePath.toString();
        if (sameBundle) {
            EntryPointHelper.registerASyncService(serviceToRegister, servicePathWithRoutingSuffix);
        } else {
            EntryPointHelper.registerASyncService(serviceToRegister, RemoteServiceMappingConstants.WEB_CONTEXT_PATH, servicePathWithRoutingSuffix);
        }
        return service;
    }
}
