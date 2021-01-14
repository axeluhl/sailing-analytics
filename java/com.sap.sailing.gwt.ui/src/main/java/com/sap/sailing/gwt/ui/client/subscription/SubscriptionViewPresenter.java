package com.sap.sailing.gwt.ui.client.subscription;

import com.sap.sailing.gwt.home.shared.places.user.profile.subscription.UserSubscriptionView;

/**
 * Interface for subscription service provider view presenter where a particular provider has its own implementation for
 * handling UI regarding subscription actions
 */
public interface SubscriptionViewPresenter {
    /**
     * Start checkout process for a plan
     */
    public void startCheckout(String planId, UserSubscriptionView view);
}
