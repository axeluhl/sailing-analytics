package com.sap.sailing.gwt.managementconsole.partials.header;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.common.client.DropdownHandler;

public class Header extends Composite {

    interface HeaderUiBinder extends UiBinder<Widget, Header> {
    }

    private static HeaderUiBinder uiBinder = GWT.create(HeaderUiBinder.class);

    @UiField
    HeaderResources local_res;

    @UiField
    AnchorElement logoAnchor, navigationAnchor;

    @UiField
    Element mobileMenu;

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

    public void addMenuItem(final String text, final ClickHandler handler) {
        final Anchor desktopItem = new Anchor(text);
        desktopItem.addClickHandler(handler);
        desktopMenu.add(desktopItem);

        final Anchor mobileItem = new Anchor(text);
        mobileItem.addClickHandler(event -> {
            Header.this.dropdownHandler.setVisible(false);
            handler.onClick(event);
        });
        mobileMenuActions.add(mobileItem);
    }

    public void setUserDetailsHandler(final ClickHandler handler) {
        userDetails.addClickHandler(handler);
        userDetailsMobile.addClickHandler(handler);
    }

    public void setSignOutHandler(final ClickHandler handler) {
        signOutMobile.addClickHandler(handler);
    }


}
