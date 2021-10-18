package com.sap.sailing.gwt.home.shared.places.subscription;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.IsWidget;
import com.sap.sailing.gwt.home.desktop.partials.subscription.SubscriptionCard;
import com.sap.sse.security.ui.shared.subscription.SubscriptionPlanDTO;

public interface SubscriptionView extends IsWidget {

    void addSubscriptionPlan(SubscriptionPlanDTO plan, final SubscriptionCard.Type type, final EventBus eventBus);

    public interface Presenter {

        void startSubscription(String planId);

        void manageSubscriptions();

        void toggleAuthenticationFlyout();
    }

}
