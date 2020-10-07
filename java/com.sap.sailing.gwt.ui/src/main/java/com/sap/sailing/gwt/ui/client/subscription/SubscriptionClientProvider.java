package com.sap.sailing.gwt.ui.client.subscription;

/**
 * Interface for subscription service provider on client side. Each particular subscription service provider module has
 * to provide concrete implementation for this interface
 */
public interface SubscriptionClientProvider {

    /**
     * Initialize service on client
     */
    public void init();

    /**
     * Return unique name for this provider
     */
    public String getProviderName();

    /**
     * Return {@code SubscriptionService} for this provider
     */
    public SubscriptionServiceAsync<?, ?> getSubscriptionService();

    /**
     * Register GWT async service
     */
    public void registerAsyncService(String serviceBasePath);

    /**
     * Return {@code SubscriptionViewPresenter} for this provider
     */
    public SubscriptionViewPresenter getSubscriptionViewPresenter();
}
