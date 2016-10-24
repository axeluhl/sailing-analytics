package com.sap.sse.security.ui.authentication.decorator;

import com.google.web.bindery.event.shared.EventBus;

/**
 * {@link AuthorizedContentDecorator} that uses {@link FlyoutNotLoggedInPresenter} internally to handle the sign in
 * case.
 *
 */
public class FlyoutBasedAuthorizedContentDecorator extends AuthorizedContentDecorator {

    public FlyoutBasedAuthorizedContentDecorator(EventBus eventBus, NotLoggedInView notLoggedInView) {
        super(new FlyoutNotLoggedInPresenter(eventBus), notLoggedInView);
    }
}
