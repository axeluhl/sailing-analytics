package com.sap.sailing.gwt.home.shared.places.subscription;

import com.google.gwt.user.client.ui.Composite;
import com.sap.sailing.gwt.home.desktop.partials.subscription.Subscription;
import com.sap.sailing.gwt.home.desktop.partials.subscription.SubscriptionContainer;
import com.sap.sse.security.ui.shared.subscription.SubscriptionPlanDTO;

public class SubscriptionViewImpl extends Composite implements SubscriptionView {

    private final SubscriptionContainer container = new SubscriptionContainer();

    public SubscriptionViewImpl() {
        initWidget(container);
    }

    @Override
    public void addSubscriptionPlan(final SubscriptionPlanDTO plan, final boolean highlight) {
        container.addSubscription(new Subscription(plan, highlight));
        container.addSubscription(new Subscription(plan, false));
        container.addSubscription(new Subscription(plan, highlight));
        container.addSubscription(new Subscription(plan, false));
        container.addSubscription(new Subscription(plan, false));
    }

}
