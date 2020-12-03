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
    AnchorElement navigationAnchor;

    @UiField
    Element mobileMenu;

    @UiField
    FlowPanel desktopMenu, mobileMenuActions;

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

    public void addMenuItem(final String text, final ClickHandler clickHandler) {
        final Anchor desktopItem = new Anchor(text);
        desktopItem.addClickHandler(clickHandler);
        desktopMenu.add(desktopItem);

        final Anchor mobileItem = new Anchor(text);
        mobileItem.addClickHandler(event -> {
            Header.this.dropdownHandler.setVisible(false);
            clickHandler.onClick(event);
        });
        mobileMenuActions.add(mobileItem);
    }

}
