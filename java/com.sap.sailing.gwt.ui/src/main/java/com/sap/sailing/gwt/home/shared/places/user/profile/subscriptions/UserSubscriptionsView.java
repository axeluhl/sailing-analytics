package com.sap.sailing.gwt.home.shared.places.user.profile.subscriptions;

import java.util.concurrent.CompletableFuture;

import com.google.gwt.user.client.ui.IsWidget;
import com.sap.sse.security.ui.client.subscription.SubscriptionsView;
import com.sap.sse.security.ui.shared.subscription.SubscriptionPlanDTO;

/**
 * View to display an overview of user subscriptions including information like plan, subscription status, etc. as well
 * as the possibility to cancel currently active subscription if desired.
 *
 * @author Tu Tran
 */
public interface UserSubscriptionsView extends SubscriptionsView, IsWidget {

    /**
     * Presenter for {@link UserSubscriptionsView}
     */
    public interface Presenter {

        void init();

        void loadSubscription();

        void setView(UserSubscriptionsView view);

        void cancelSubscription(String planId, String providerName);

        CompletableFuture<SubscriptionPlanDTO> getPlanById(String planId);

        void navigateToSubscribe();
    }
}
