package com.sap.sse.security.ui.client.subscription;

/**
 * Interface for subscription service provider view presenter where a particular provider has its own implementation for
 * handling UI regarding subscription actions
 */
public interface SubscriptionViewPresenter {

    /**
     * Start checkout process for a plan
     */
    public void startCheckout(String planId, SubscribeView view, Runnable runnable);
}
