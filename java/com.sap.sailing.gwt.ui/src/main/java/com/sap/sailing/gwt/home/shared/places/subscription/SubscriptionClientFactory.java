package com.sap.sailing.gwt.home.shared.places.subscription;

import com.sap.sailing.gwt.ui.client.refresh.ErrorAndBusyClientFactory;
import com.sap.sse.security.ui.authentication.WithAuthenticationManager;
import com.sap.sse.security.ui.client.WithSecurity;

public interface SubscriptionClientFactory extends ErrorAndBusyClientFactory, WithSecurity, WithAuthenticationManager {
    SubscriptionView createSubscriptionsView();
}
