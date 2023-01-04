package com.sap.sailing.gwt.ui.client;

import static com.sap.sse.common.HttpRequestHeaderConstants.HEADER_FORWARD_TO_MASTER;
import static com.sap.sse.common.HttpRequestHeaderConstants.HEADER_FORWARD_TO_REPLICA;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.sap.sailing.landscape.common.RemoteServiceMappingConstants;
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
        return createSailingServiceInstance(null);
    }
    
    /**
     * Creates a new {@link SailingServiceWriteAsync} instance, should be used when code resides in the same bundle as the sailing service code.
     */
    public static SailingServiceWriteAsync createSailingServiceWriteInstance() {
        return createSailingServiceWriteInstance(/* no routing provider required, no sharding for write requests */ null);
    }
    
    /**
     * Creates a new {@link SailingServiceAsync} instance that uses a routing provider, for code in same bundle.
     */
    public static SailingServiceAsync createSailingServiceInstance(ServiceRoutingProvider routingProvider) {
        final SailingServiceAsync service = GWT.create(SailingService.class);
        final ServiceDefTarget serviceToRegister = (ServiceDefTarget) service;
        final StringBuilder servicePath = new StringBuilder(RemoteServiceMappingConstants.sailingServiceRemotePath);
        if (routingProvider != null) {
            servicePath.append(routingProvider.routingSuffixPath());
        }
        final String servicePathWithRoutingSuffix = servicePath.toString();
        EntryPointHelper.registerASyncService(serviceToRegister, servicePathWithRoutingSuffix, HEADER_FORWARD_TO_REPLICA);
        return service;
    }

    /**
     * Creates a new {@link SailingServiceWriteAsync} instance that uses a routing provider, for code in same bundle.
     */
    public static SailingServiceWriteAsync createSailingServiceWriteInstance(ServiceRoutingProvider routingProvider) {
        final SailingServiceWriteAsync service = GWT.create(SailingServiceWrite.class);
        final ServiceDefTarget serviceToRegister = (ServiceDefTarget) service;
        final StringBuilder servicePath = new StringBuilder(RemoteServiceMappingConstants.sailingServiceRemotePath);
        if (routingProvider != null) {
            servicePath.append(routingProvider.routingSuffixPath());
        }
        final String servicePathWithRoutingSuffix = servicePath.toString();
        EntryPointHelper.registerASyncService(serviceToRegister, servicePathWithRoutingSuffix, HEADER_FORWARD_TO_MASTER);
        return service;
    }
}
