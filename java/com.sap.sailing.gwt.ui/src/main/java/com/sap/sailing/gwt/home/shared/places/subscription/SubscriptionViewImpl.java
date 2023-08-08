package com.sap.sailing.gwt.home.shared.places.subscription;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.Composite;
import com.sap.sailing.gwt.home.desktop.partials.subscription.SubscriptionCard;
import com.sap.sailing.gwt.home.desktop.partials.subscription.SubscriptionCardContainer;
import com.sap.sailing.gwt.home.desktop.partials.subscription.SubscriptionCardResources;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.Notification;
import com.sap.sse.gwt.client.Notification.NotificationType;
import com.sap.sse.gwt.client.dialog.ConfirmationDialog;
import com.sap.sse.security.shared.subscription.SubscriptionPrice;
import com.sap.sse.security.ui.authentication.app.AuthenticationContext;

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
    public void addSubscriptionGroup(final SubscriptionGroupDTO group, final SubscriptionCard.Type type,
            final EventBus eventBus) {
        final AuthenticationContext authenticationContext = presenter.getAuthenticationContext();
        switch (type) {
        case HIGHLIGHT:
        case DEFAULT:
            container.addSubscription(new SubscriptionCard(group, type, (price) -> {
                if (!authenticationContext.isLoggedIn()) {
                    onOpenCheckoutError(StringMessages.INSTANCE.notLoggedIn());
                    presenter.toggleAuthenticationFlyout();
                } else if (!this.isEmailValidated(authenticationContext)) {
                    onOpenCheckoutError(StringMessages.INSTANCE.mailNotValidated());
                    presenter.toggleAuthenticationFlyout();
                } else {
                    if (price != null) {
                        presenter.startSubscription(price.getPriceId());
                    }
                }
            }, eventBus, presenter.getAuthenticationContext().isLoggedIn(), this.isEmailValidated(authenticationContext)));
            break;
        case UPGRADE:
            container.addSubscription(new SubscriptionCard(group, type, (price) -> {
                if (!authenticationContext.isLoggedIn()) {
                    onOpenCheckoutError(StringMessages.INSTANCE.notLoggedIn());
                    presenter.toggleAuthenticationFlyout();
                } else if (!this.isEmailValidated(authenticationContext)) {
                    onOpenCheckoutError(StringMessages.INSTANCE.mailNotValidated());
                    presenter.toggleAuthenticationFlyout();
                } else {
                    if (price != null) {
                        openConfirmationDialog(price);
                    }
                }
            }, eventBus, presenter.getAuthenticationContext().isLoggedIn(), this.isEmailValidated(authenticationContext)));
            break;
        case OWNER:
            container.addSubscription(new SubscriptionCard(group, type, price -> presenter.manageSubscriptions(),
                    eventBus, presenter.getAuthenticationContext().isLoggedIn(), this.isEmailValidated(authenticationContext)));
            break;
        case ONETIMELOCK:
            container.addSubscription(new SubscriptionCard(group, type, price -> {},
                    eventBus, presenter.getAuthenticationContext().isLoggedIn(), this.isEmailValidated(authenticationContext)));
            break;
        case FREE:
            container.addSubscription(new SubscriptionCard(group, type, price -> presenter.toggleAuthenticationFlyout(),
                    eventBus, presenter.getAuthenticationContext().isLoggedIn(), this.isEmailValidated(authenticationContext)));
            break;
        default:
            break;
        }
    }
    
    private void openConfirmationDialog(SubscriptionPrice price) {
        final StringMessages i18n = StringMessages.INSTANCE;
        ConfirmationDialog upgradeDialog = ConfirmationDialog.create(i18n.upgrade(),
                i18n.upgradeInfo(), i18n.subscribe(), i18n.cancel(),
                () -> presenter.startSubscription(price.getPriceId()));
        upgradeDialog.setAnimationEnabled(true);
        upgradeDialog.setGlassEnabled(true);
        upgradeDialog.setModal(true);
        upgradeDialog.setStyleName(SubscriptionCardResources.INSTANCE.css().confirmationDialog());
        upgradeDialog.setGlassStyleName(SubscriptionCardResources.INSTANCE.css().popupGlass());
        upgradeDialog.center();
    }
    
    private boolean isEmailValidated(final AuthenticationContext authenticationContext) {
        return !presenter.isMailVerificationRequired();
    }

    @Override
    public void onCloseCheckoutModal() {
        GWT.log("checkout module is closed");
    }

    @Override
    public void onOpenCheckoutError(final String error) {
        Notification.notify(error, NotificationType.ERROR);
    }
    
    @Override
    public void onFinishedPayment(final String message) {
        Notification.notify(message, NotificationType.SUCCESS);
    }
    
    @Override
    public void onUnfinishedPayment(final String message) {
        Notification.notify(message, NotificationType.WARNING);
    }
    @Override
    public void resetSubscriptions() {
        container.resetSubscriptions();
    }

}
