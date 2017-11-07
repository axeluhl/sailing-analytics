package com.sap.sse.security.ui.authentication.decorator;

import com.google.web.bindery.event.shared.EventBus;
import com.sap.sse.security.ui.authentication.AuthenticationPlaces;
import com.sap.sse.security.ui.authentication.AuthenticationRequestEvent;

/**
 * {@link NotLoggedInPresenter} implementation that fires an {@link AuthenticationRequestEvent} which is used by
 * authentication UI implementations using a flyout to host the authentication UI. This event triggers the flyout to be
 * shown.
 *
 */
public class FlyoutNotLoggedInPresenter implements NotLoggedInPresenter {

    private final EventBus eventBus;

    public FlyoutNotLoggedInPresenter(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    @Override
    public void doTriggerLoginForm() {
        eventBus.fireEvent(new AuthenticationRequestEvent(AuthenticationPlaces.SIGN_IN));
    }
}
