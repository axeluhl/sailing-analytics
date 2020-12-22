package com.sap.sailing.gwt.managementconsole.partials.header;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasVisibility;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.common.client.DropdownHandler;
import com.sap.sse.security.ui.authentication.app.AuthenticationContext;
import com.sap.sse.security.ui.authentication.app.NeedsAuthenticationContext;

public class Header extends Composite implements NeedsAuthenticationContext {

    interface HeaderUiBinder extends UiBinder<Widget, Header> {
    }

    private static HeaderUiBinder uiBinder = GWT.create(HeaderUiBinder.class);

    @UiField
    HeaderResources local_res;

    @UiField
    AnchorElement logoAnchor, navigationAnchor;

    @UiField
    Element actions, mobileMenu;

    @UiField
    FlowPanel desktopMenu, mobileMenuActions;

    @UiField
    Anchor userDetails, userDetailsMobile, signOutMobile;

    private final DropdownHandler dropdownHandler;

    public Header() {
        initWidget(uiBinder.createAndBindUi(this));
        local_res.style().ensureInjected();

        this.dropdownHandler = new DropdownHandler(navigationAnchor, mobileMenu) {
            @Override
            protected void dropdownStateChanged(final boolean dropdownShown) {
                setStyleName(mobileMenu, local_res.style().active(), dropdownShown);
            }
        };
    }

    @Override
    public void setAuthenticationContext(final AuthenticationContext authenticationContext) {
        setVisible(actions, authenticationContext.isLoggedIn());
    }

    public HasVisibility addMenuItem(final String text, final ClickHandler handler) {
        final Anchor desktopItem = new Anchor(text);
        desktopItem.addClickHandler(handler);
        desktopMenu.add(desktopItem);

        final Anchor mobileItem = new Anchor(text);
        mobileItem.addClickHandler(new CloseMenuClickHandler(handler));
        mobileMenuActions.add(mobileItem);

        return new MenuItem(desktopItem, mobileItem);
    }

    public HasVisibility initUserDetailsItem(final ClickHandler handler) {
        userDetails.addClickHandler(handler);
        userDetailsMobile.addClickHandler(new CloseMenuClickHandler(handler));
        return new MenuItem(userDetails, userDetailsMobile);
    }

    public HasVisibility initSignOutItem(final ClickHandler handler) {
        signOutMobile.addClickHandler(new CloseMenuClickHandler(handler));
        return new MenuItem(signOutMobile);
    }

    private class CloseMenuClickHandler implements ClickHandler {

        private final ClickHandler handler;

        private CloseMenuClickHandler(final ClickHandler handler) {
            this.handler = handler;
        }

        @Override
        public void onClick(final ClickEvent event) {
            Header.this.dropdownHandler.setVisible(false);
            handler.onClick(event);
        }
    }

    private class MenuItem implements HasVisibility {

        private final Set<HasVisibility> items = new HashSet<>();

        private MenuItem(final HasVisibility... anchors) {
            this.items.addAll(Arrays.asList(anchors));
        }

        @Override
        public boolean isVisible() {
            return items.stream().anyMatch(HasVisibility::isVisible);
        }

        @Override
        public void setVisible(final boolean visible) {
            items.forEach(item -> item.setVisible(visible));
        }

    }

}
