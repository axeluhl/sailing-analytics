package com.sap.sailing.gwt.home.client.shared;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.InlineHyperlink;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.proxy.PlaceManager;

/**
 * The main menu for the homepage.
 */
public class MainMenu extends Composite {
    interface MainMenuUiBinder extends UiBinder<Widget, MainMenu> {
    }

    @UiField
    InlineHyperlink startPageLink;

    @UiField
    InlineHyperlink eventsPageLink;

    @UiField
    InlineHyperlink aboutUsPageLink;

    @UiField
    InlineHyperlink contactPageLink;

    private final PlaceManager placeManager;

    @Inject
    MainMenu(MainMenuUiBinder binder, PlaceManager placeManager) {
        this.placeManager = placeManager;
        initWidget(binder.createAndBindUi(this));
    }

    public void voidSetActiveLink(String pageNameToken) {
        String activeLinkStyle = "active";
        switch (pageNameToken) {
        case PageNameConstants.aboutUsPage:
            aboutUsPageLink.addStyleName(activeLinkStyle);
            break;
        case PageNameConstants.contactPage:
            contactPageLink.addStyleName(activeLinkStyle);
            break;
        case PageNameConstants.startPage:
            startPageLink.addStyleName(activeLinkStyle);
            break;
        case PageNameConstants.eventsPage:
            eventsPageLink.addStyleName(activeLinkStyle);
            break;
        }
    }

}
