package com.sap.sailing.gwt.managementconsole.partials.authentication.decorator;

import com.sap.sailing.gwt.managementconsole.partials.authentication.AuthenticationResources;
import com.sap.sse.security.ui.authentication.decorator.AuthorizedContentDecorator;
import com.sap.sse.security.ui.authentication.decorator.NotLoggedInPresenter;

public class AuthorizedDecorator extends AuthorizedContentDecorator {

    public AuthorizedDecorator(final NotLoggedInPresenter presenter) {
        super(presenter, new NotLoggedInViewImpl());
        AuthenticationResources.INSTANCE.style().ensureInjected();
    }

}
