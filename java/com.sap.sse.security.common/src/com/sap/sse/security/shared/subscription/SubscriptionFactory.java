package com.sap.sse.security.shared.subscription;

import java.util.HashMap;
import java.util.Map;

import com.sap.sse.security.shared.subscription.chargebee.ChargebeeSubscriptionProvider;

/**
 * Provide access to concrete subscription provider implementation
 */
public class SubscriptionFactory {
    private static SubscriptionFactory instance;

    public static SubscriptionFactory getInstance() {
        if (instance == null) {
            instance = new SubscriptionFactory();
        }
        return instance;
    }

    private Map<String, SubscriptionProvider> subscriptionProviders = new HashMap<String, SubscriptionProvider>();

    private SubscriptionFactory() {
        registerSubscriptionProvider(ChargebeeSubscriptionProvider.getInstance());
    }

    public Iterable<SubscriptionProvider> getProviders() {
        return subscriptionProviders.values();
    }

    public SubscriptionProvider getSubscriptionProvider(String providerName)
            throws InvalidSubscriptionProviderException {
        if (!subscriptionProviders.containsKey(providerName)) {
            throw new InvalidSubscriptionProviderException("Invalid subscription provider " + providerName);
        }
        return subscriptionProviders.get(providerName);
    }

    private void registerSubscriptionProvider(SubscriptionProvider provider) {
        subscriptionProviders.put(provider.getProviderName(), provider);
    }
}
