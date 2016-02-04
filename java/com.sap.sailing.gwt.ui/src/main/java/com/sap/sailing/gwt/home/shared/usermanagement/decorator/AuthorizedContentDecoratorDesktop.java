package com.sap.sailing.gwt.home.shared.usermanagement.decorator;


public class AuthorizedContentDecoratorDesktop extends AuthorizedContentDecorator {
    
    public AuthorizedContentDecoratorDesktop(NotLoggedInPresenter presenter) {
        super(presenter, new NotLoggedInViewDesktopImpl());
    }

}
