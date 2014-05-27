package com.sap.sailing.gwt.home.client.shared.header;

import java.util.Arrays;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.shared.mainmenu.MainMenuNavigator;

public class Header extends Composite {
    @UiField
    Anchor startPageLink;

    @UiField
    Anchor eventsPageLink;

    @UiField
    Anchor aboutUsPageLink;

    @UiField
    Anchor contactPageLink;

    private final List<Anchor> links;

    private final MainMenuNavigator navigator;

    interface HeaderUiBinder extends UiBinder<Widget, Header> {
    }
    
    private static HeaderUiBinder uiBinder = GWT.create(HeaderUiBinder.class);

    public Header(MainMenuNavigator navigator) {
        this.navigator = navigator;
        HeaderResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
        links = Arrays.asList(new Anchor[] { startPageLink, eventsPageLink, aboutUsPageLink, contactPageLink });
    }

    @UiHandler("startPageLink")
    public void goToHome(ClickEvent e) {
        navigator.goToHome();
        setActiveLink(startPageLink);
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

    private void setActiveLink(Anchor link) {
        final String activeStyle = HeaderResources.INSTANCE.css().sitenavigation_active();
        for (Anchor l : links) {
            if (l == link) {
                l.addStyleName(activeStyle);
            } else {
                l.removeStyleName(activeStyle);
            }
        }
    }

}
