package com.sap.sse.security.ui.client.subscription;

/**
 * Interface for subscription service provider on client side. Each particular subscription service provider module has
 * to provide concrete implementation for this interface
 */
public interface SubscriptionClientProvider {

    /**
     * Initialize service on client
     */
    void init();

    /**
     * Return unique name for this provider
     */
    String getProviderName();
    
    /**
     * Return {@code SubscriptionService} for this provider, running reading requests that also replicas may handle
     */
    SubscriptionServiceAsync<?, ?> getSubscriptionService();

    /**
     * Return {@code SubscriptionWriteService} for this provider; use this at least for all updating requests that a
     * master node must handle.
     */
    SubscriptionWriteServiceAsync<?, ?, ?> getSubscriptionWriteService();

    /**
     * Register GWT async service
     */
    void registerAsyncService(String serviceBasePath);

    /**
     * Return {@code SubscriptionViewPresenter} for this provider
     */
    SubscriptionViewPresenter getSubscriptionViewPresenter();
}
