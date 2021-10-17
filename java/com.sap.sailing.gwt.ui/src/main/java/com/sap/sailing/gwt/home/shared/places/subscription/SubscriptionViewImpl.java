package com.sap.sailing.gwt.home.shared.places.subscription;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.sap.sailing.gwt.home.desktop.partials.subscription.SubscriptionCard;
import com.sap.sailing.gwt.home.desktop.partials.subscription.SubscriptionCardContainer;
import com.sap.sse.security.ui.shared.subscription.SubscriptionPlanDTO;

public class SubscriptionViewImpl extends Composite implements SubscriptionView {

    private final SubscriptionCardContainer container = new SubscriptionCardContainer();
    private final Presenter presenter;

    public SubscriptionViewImpl(Presenter presenter) {
        initWidget(container);
        this.presenter = presenter;
    }

    @Override
    public void addSubscriptionPlan(final SubscriptionPlanDTO plan, final SubscriptionCard.Type type) {
        switch (type) {
        case HIGHLIGHT:
            container.addSubscription(new SubscriptionCard(plan, type, (price) -> {
                if (price != null) {
                    presenter.startSubscription(price.getPriceId());
                }
            }));
            break;
        case DEFAULT:
            container.addSubscription(new SubscriptionCard(plan, type, (price) -> {
                if (price != null) {
                    presenter.startSubscription(price.getPriceId());
                }
            }));
            break;
        case OWNER:
            container.addSubscription(new SubscriptionCard(plan, type, (price) -> {
                presenter.manageSubscriptions();
            }));
            break;
        case INDIVIDUAL:
            container.addSubscription(new SubscriptionCard(plan, type, (price) -> {
                Window.Location.assign("mailto:info@sapsailing.com");
            }));
            break;
        case FREE:
            container.addSubscription(new SubscriptionCard(plan, type, (price) -> {
                GWT.log("TODO: sign up dialog");
            }));
            break;
        default:
            break;
        }
    }

}
