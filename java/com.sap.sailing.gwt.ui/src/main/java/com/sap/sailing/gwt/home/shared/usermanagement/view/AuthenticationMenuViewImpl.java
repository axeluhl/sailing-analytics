package com.sap.sailing.gwt.home.shared.usermanagement.view;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Widget;

public class AuthenticationMenuViewImpl implements AuthenticationMenuView {
    
    private Presenter presenter;
    private final Anchor anchor;
    private final String loggedInStyle;

    public AuthenticationMenuViewImpl(Anchor anchor, String loggedInStyle) {
        this.anchor = anchor;
        this.loggedInStyle = loggedInStyle;
        anchor.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                presenter.toggleFlyout();
            }
        });
    }

    @Override
    public Widget asWidget() {
        return anchor;
    }

    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void setAuthenticated(boolean authenticated) {
        anchor.setStyleName(loggedInStyle, authenticated);
    }

}
