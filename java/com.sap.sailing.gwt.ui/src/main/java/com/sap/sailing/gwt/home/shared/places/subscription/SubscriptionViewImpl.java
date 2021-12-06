package com.sap.sailing.gwt.home.shared.places.subscription;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.sap.sailing.gwt.home.desktop.partials.subscription.SubscriptionCard;
import com.sap.sailing.gwt.home.desktop.partials.subscription.SubscriptionCardContainer;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.Notification;
import com.sap.sse.gwt.client.Notification.NotificationType;
import com.sap.sse.security.ui.shared.subscription.SubscriptionPlanDTO;

public class SubscriptionViewImpl extends Composite implements SubscriptionView {

    private final SubscriptionCardContainer container = new SubscriptionCardContainer();
    private Presenter presenter;

    public SubscriptionViewImpl() {
        initWidget(container);
    }

    @Override
    public void setPresenter(final Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void addSubscriptionPlan(final SubscriptionPlanDTO plan, final SubscriptionCard.Type type,
            final EventBus eventBus) {
        switch (type) {
        case HIGHLIGHT:
        case DEFAULT:
            container.addSubscription(new SubscriptionCard(plan, type, (price) -> {
                if (presenter.getAuthenticationContext().isLoggedIn()) {
                    if (price != null) {
                        presenter.startSubscription(price.getPriceId());
                    }
                } else {
                    presenter.getClientFactory().createErrorView(StringMessages.INSTANCE.notLoggedIn(),
                            new RuntimeException("User not logged in."));
                    presenter.toggleAuthenticationFlyout();
                }
            }, eventBus, presenter.getAuthenticationContext().isLoggedIn()));
            break;
        case OWNER:
            container.addSubscription(new SubscriptionCard(plan, type, price -> presenter.manageSubscriptions(),
                    eventBus, presenter.getAuthenticationContext().isLoggedIn()));
            break;
        case INDIVIDUAL:
            container.addSubscription(
                    new SubscriptionCard(plan, type, price -> Window.Location.assign("mailto:info@sapsailing.com"),
                            eventBus, presenter.getAuthenticationContext().isLoggedIn()));
            break;
        case FREE:
            container.addSubscription(new SubscriptionCard(plan, type, price -> presenter.toggleAuthenticationFlyout(),
                    eventBus, presenter.getAuthenticationContext().isLoggedIn()));
            break;
        default:
            break;
        }
    }

    @Override
    public void onCloseCheckoutModal() {
        // FIXME Is any action required in this case?
    }

    @Override
    public void onOpenCheckoutError(final String error) {
        Notification.notify(error, NotificationType.ERROR);
    }

}
