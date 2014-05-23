package com.sap.sailing.gwt.home.client.shared;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

/**
 * The main menu for the homepage.
 */
public class MainMenu extends Composite {
    interface MainMenuUiBinder extends UiBinder<Widget, MainMenu> {
    }

    private static MainMenuUiBinder uiBinder = GWT.create(MainMenuUiBinder.class);
    
    private final MainMenuNavigator navigator;
    
    @UiField
    Anchor startPageLink;

    @UiField
    Anchor eventsPageLink;

    @UiField
    Anchor aboutUsPageLink;

    @UiField
    Anchor contactPageLink;

    MainMenu(MainMenuNavigator navigator) {
        initWidget(uiBinder.createAndBindUi(this));
        this.navigator = navigator;
    }
    
    @UiHandler("startPageLink")
    public void goToHome(ClickEvent e) {
        navigator.goToHome();
    }

    @UiHandler("eventsPageLink")
    public void goToEvents(ClickEvent e) {
        navigator.goToEvents();
    }

    @UiHandler("aboutUsPageLink")
    public void goToAboutUs(ClickEvent e) {
        navigator.goToAboutUs();
    }

    @UiHandler("contactPageLink")
    public void goToContact(ClickEvent e) {
        navigator.goToContact();
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
