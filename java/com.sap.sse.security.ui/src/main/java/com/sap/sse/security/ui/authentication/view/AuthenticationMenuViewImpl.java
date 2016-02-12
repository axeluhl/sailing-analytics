package com.sap.sse.security.ui.authentication.view;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Widget;

/**
 * Default implementation of {@link AuthenticationMenuView} based on an {@link Anchor} widget.
 */
public class AuthenticationMenuViewImpl implements AuthenticationMenuView {
    
    private Presenter presenter;
    private final Anchor anchor;
    private final String loggedInStyle;
    private final String openStyle;

    /**
     * Create a new {@link AuthenticationMenuViewImpl} instance with the given parameters.
     * 
     * @param anchor
     *            the {@link Anchor} widget representing the menu item
     * @param loggedInStyle
     *            the style name to add to the widget, if there is an authenticated user
     * @param openStyle
     *            the style name to add to the widget, if the {@link FlyoutAuthenticationView} is open
     */
    public AuthenticationMenuViewImpl(Anchor anchor, String loggedInStyle, String openStyle) {
        this.anchor = anchor;
        this.loggedInStyle = loggedInStyle;
        this.openStyle = openStyle;
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
    
    @Override
    public void setOpen(boolean open) {
        anchor.setStyleName(openStyle, open);
    }

}
