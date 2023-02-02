package com.sap.sse.security.ui.client.subscription;

import java.util.HashMap;
import java.util.Map;

import com.sap.sse.security.shared.subscription.InvalidSubscriptionProviderException;
import com.sap.sse.security.ui.client.subscription.chargebee.ChargebeeSubscriptionClientProvider;

/**
 * SubscriptionServiceFactory registers all payment provider services in the system and provides a way to access a
 * specific service. It also registers a default payment service for checkout handling. This class implements a
 * "singleton" pattern, and the single instance can be obtained using the {@link #getInstance()} class method. The
 * providers known are registered in {@link #registerProviders()}.
 */
public class SubscriptionServiceFactory {
    private static final String DEFAULT_PROVIDER_NAME = ChargebeeSubscriptionClientProvider.PROVIDER_NAME;
    private static SubscriptionServiceFactory instance;

    private Map<String, SubscriptionClientProvider> providers;
    private boolean inited;

    public static SubscriptionServiceFactory getInstance() {
        if (instance == null) {
            instance = new SubscriptionServiceFactory();
        }
        return instance;
    }

    private SubscriptionServiceFactory() {
        providers = new HashMap<String, SubscriptionClientProvider>();
        registerProviders();
    }

    public void initializeProviders() {
        if (!inited) {
            for (SubscriptionClientProvider p : providers.values()) {
                p.init();
            }
            inited = true;
        }
    }

    public void registerAsyncServices(String basePath) {
        for (SubscriptionClientProvider p : providers.values()) {
            p.registerAsyncService(basePath);
        }
    }

    public SubscriptionWriteServiceAsync<?, ?, ?> getWriteAsyncServiceByProvider(String providerName)
            throws InvalidSubscriptionProviderException {
        return getProvider(providerName).getSubscriptionWriteService();
    }

    public SubscriptionServiceAsync<?, ?> getAsyncServiceByProvider(String providerName)
            throws InvalidSubscriptionProviderException {
        return getProvider(providerName).getSubscriptionService();
    }

    /**
     * Return default provider
     */
    public SubscriptionClientProvider getDefaultProvider() throws InvalidSubscriptionProviderException {
        return getProvider(DEFAULT_PROVIDER_NAME);
    }

    /**
     * Return default subscription service
     */
    public SubscriptionServiceAsync<?, ?> getDefaultAsyncService() throws InvalidSubscriptionProviderException {
        return getAsyncServiceByProvider(DEFAULT_PROVIDER_NAME);
    }

    /**
     * Return default subscription write service
     */
    public SubscriptionWriteServiceAsync<?, ?, ?> getDefaultWriteAsyncService()
            throws InvalidSubscriptionProviderException {
        return getWriteAsyncServiceByProvider(DEFAULT_PROVIDER_NAME);
    }

    private SubscriptionClientProvider getProvider(String name) throws InvalidSubscriptionProviderException {
        SubscriptionClientProvider provider = providers.get(name);
        if (provider == null) {
            throw new InvalidSubscriptionProviderException(name);
        }
        return provider;
    }

    private void registerProviders() {
        // TODO turn this into an OSGi registry pattern; this would then allow us to add other providers also in other bundles
        registerSubscriptionProvider(new ChargebeeSubscriptionClientProvider());
    }

    private void registerSubscriptionProvider(SubscriptionClientProvider provider) {
        providers.put(provider.getProviderName(), provider);
    }
}
