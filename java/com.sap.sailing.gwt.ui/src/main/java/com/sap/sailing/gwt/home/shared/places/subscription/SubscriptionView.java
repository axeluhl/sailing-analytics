package com.sap.sailing.gwt.home.shared.places.subscription;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.IsWidget;
import com.sap.sailing.gwt.home.desktop.partials.subscription.SubscriptionCard;
import com.sap.sse.security.ui.authentication.app.AuthenticationContext;
import com.sap.sse.security.ui.client.subscription.SubscribeView;
import com.sap.sse.security.ui.shared.subscription.SubscriptionPlanDTO;

public interface SubscriptionView extends SubscribeView, IsWidget {

    void addSubscriptionPlan(SubscriptionPlanDTO plan, final SubscriptionCard.Type type, final EventBus eventBus);
    
    void resetSubscriptions();

    void setPresenter(SubscriptionView.Presenter presenter);

    public interface Presenter {

        void startSubscription(String priceId);

        void manageSubscriptions();

        void toggleAuthenticationFlyout();

        AuthenticationContext getAuthenticationContext();

    }

}
