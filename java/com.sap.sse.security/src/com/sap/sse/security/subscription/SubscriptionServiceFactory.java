package com.sap.sse.security.subscription;

import java.util.HashMap;
import java.util.Map;

import com.sap.sse.security.shared.subscription.chargebee.ChargebeeSubscriptionProvider;
import com.sap.sse.security.subscription.chargebee.ChargebeeApiService;

/**
 * Register subscription services for payment providers, and allow to access to the services
 */
public class SubscriptionServiceFactory {
    private static SubscriptionServiceFactory instance;

    public static SubscriptionServiceFactory getInstance() {
        if (instance == null) {
            instance = new SubscriptionServiceFactory();
        }
        return instance;
    }

    private Map<String, SubscriptionApiService> subscriptionApiServices = new HashMap<String, SubscriptionApiService>();

    public SubscriptionServiceFactory() {
        registerApiServices();
    }

    /**
     * Return implementation of {@code SubscriptionApiService} for a subscription provider
     */
    public SubscriptionApiService getApiService(String providerName) {
        return subscriptionApiServices.get(providerName);
    }

    private void registerApiServices() {
        subscriptionApiServices.put(ChargebeeSubscriptionProvider.getInstance().getProviderName(),
                ChargebeeApiService.getInstance());
    }
}
