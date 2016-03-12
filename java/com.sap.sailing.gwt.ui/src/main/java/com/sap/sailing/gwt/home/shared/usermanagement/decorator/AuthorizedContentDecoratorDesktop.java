package com.sap.sailing.gwt.home.shared.usermanagement.decorator;

import com.sap.sse.security.ui.authentication.decorator.AuthorizedContentDecorator;
import com.sap.sse.security.ui.authentication.decorator.NotLoggedInPresenter;


public class AuthorizedContentDecoratorDesktop extends AuthorizedContentDecorator {
    
    public AuthorizedContentDecoratorDesktop(NotLoggedInPresenter presenter) {
        super(presenter, new NotLoggedInViewDesktopImpl());
    }

}
