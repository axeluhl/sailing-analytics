package com.sap.sse.security.ui.client.subscription;

import com.sap.sse.security.ui.shared.subscription.SubscriptionDTO;
import com.sap.sse.security.ui.shared.subscription.SubscriptionPlanDTO;

/**
 * View for displaying user subscription information like plan, subscription status...In this view user is able to
 * subscribe to a plan or cancel current subscription.
 * 
 * @author Tu Tran
 */
public interface BaseUserSubscriptionView {
    /**
     * Update the view with subscription data returned from back-end
     */
    public void updateView(SubscriptionDTO subscription, Iterable<SubscriptionPlanDTO> planList);

    /**
     * Called on checkout modal is closed
     */
    public void onCloseCheckoutModal();

    /**
     * Called on checkout modal has errors
     */
    public void onOpenCheckoutError(String error);
}
