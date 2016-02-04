package com.sap.sse.security.ui.authentication.decorator;

import com.google.web.bindery.event.shared.EventBus;
import com.sap.sse.security.ui.authentication.AuthenticationRequestEvent;

public class FlyoutNotLoggedInPresenter implements NotLoggedInPresenter {
    
    private final EventBus eventBus;

    public FlyoutNotLoggedInPresenter(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    @Override
    public void doTriggerLoginForm() {
        eventBus.fireEvent(new AuthenticationRequestEvent());
    }
}
