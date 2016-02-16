package com.sap.sailing.gwt.home.shared.usermanagement.decorator;


public class AuthorizedContentDecoratorMobile extends AuthorizedContentDecorator {
    
    public AuthorizedContentDecoratorMobile(NotLoggedInPresenter presenter) {
        super(presenter, new NotLoggedInViewMobileImpl());
    }

}
