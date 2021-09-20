package com.sap.sailing.gwt.home.shared.places.subscription;

import com.google.gwt.user.client.ui.IsWidget;
import com.sap.sailing.gwt.home.desktop.partials.subscription.Subscription;
import com.sap.sse.security.ui.shared.subscription.SubscriptionPlanDTO;

public interface SubscriptionView extends IsWidget {

    void addSubscriptionPlan(SubscriptionPlanDTO plan, final Subscription.Type type);

    public interface Presenter {

        void startSubscription(String planId);

        void manageSubscriptions();

    }

}
