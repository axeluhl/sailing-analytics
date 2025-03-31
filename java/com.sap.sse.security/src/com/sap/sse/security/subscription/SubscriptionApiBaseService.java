package com.sap.sse.security.subscription;

import com.sap.sse.common.Duration;
import com.sap.sse.security.shared.SubscriptionPlanProvider;

/**
 * Service interface for provider API requests
 */
public interface SubscriptionApiBaseService {
    /**
     * The OSGi registry property name under which to look up a specific implementation of this API.
     */
    String PROVIDER_NAME_OSGI_REGISTRY_KEY = "provider-name";

    /**
     * The name used for the {@link #PROVIDER_NAME_OSGI_REGISTRY_KEY} key during registration of this service with the
     * OSGi service registry.
     */
    String getProviderName();
    
    /**
     * A subscription provider may have API rate limits. This is the minimum time between starting two subsequent
     * requests to the same subscription provider API.
     */
    Duration getTimeBetweenApiRequestStart();
    
    /**
     * The delay after which to re-schedule a request that failed for having exceeded the service's rate limit
     */
    Duration getLimitReachedResumeDelay();
    
    SubscriptionPlanProvider getSubscriptionPlanProvider();
}
