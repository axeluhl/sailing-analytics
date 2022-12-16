package com.sap.sailing.gwt.home.desktop.places.subscription;

import com.google.web.bindery.event.shared.EventBus;
import com.sap.sailing.gwt.home.desktop.app.DesktopClientFactory;
import com.sap.sailing.gwt.home.desktop.app.DesktopPlacesNavigator;
import com.sap.sailing.gwt.home.shared.places.subscription.AbstractSubscriptionActivity;
import com.sap.sailing.gwt.home.shared.places.subscription.SubscriptionPlace;
import com.sap.sailing.gwt.home.shared.places.subscription.SubscriptionView;
import com.sap.sse.security.ui.authentication.AuthenticationPlaces;
import com.sap.sse.security.ui.authentication.AuthenticationRequestEvent;

public class SubscriptionActivity extends AbstractSubscriptionActivity implements SubscriptionView.Presenter {

    private final DesktopPlacesNavigator navigator;
    private final EventBus eventBus;

    public SubscriptionActivity(final SubscriptionPlace place, final DesktopClientFactory clientFactory) {
        super(place, clientFactory);
        this.navigator = clientFactory.getHomePlacesNavigator();
        this.eventBus = clientFactory.getEventBus();
    }

    @Override
    public void manageSubscriptions() {
        navigator.goToPlace(navigator.getUserSubscriptionsNavigation());
    }
    @Override
    public void toggleAuthenticationFlyout() {
        eventBus.fireEvent(new AuthenticationRequestEvent(AuthenticationPlaces.SIGN_IN));
    }

}
