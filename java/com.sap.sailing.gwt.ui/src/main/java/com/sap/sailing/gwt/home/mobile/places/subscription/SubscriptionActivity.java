package com.sap.sailing.gwt.home.mobile.places.subscription;

import com.sap.sailing.gwt.home.mobile.app.MobileApplicationClientFactory;
import com.sap.sailing.gwt.home.mobile.app.MobilePlacesNavigator;
import com.sap.sailing.gwt.home.shared.places.subscription.AbstractSubscriptionActivity;
import com.sap.sailing.gwt.home.shared.places.subscription.SubscriptionPlace;
import com.sap.sailing.gwt.home.shared.places.subscription.SubscriptionView;

public class SubscriptionActivity extends AbstractSubscriptionActivity implements SubscriptionView.Presenter {

    private final MobilePlacesNavigator navigator;

    public SubscriptionActivity(final SubscriptionPlace place, final MobileApplicationClientFactory clientFactory) {
        super(place, clientFactory);
        this.navigator = clientFactory.getNavigator();
    }

    @Override
    public void manageSubscriptions() {
        navigator.goToPlace(navigator.getUserSubscriptionsNavigation());
    }

    @Override
    public void toggleAuthenticationFlyout() {
        navigator.goToPlace(navigator.getSignInNavigation());
    }

}
