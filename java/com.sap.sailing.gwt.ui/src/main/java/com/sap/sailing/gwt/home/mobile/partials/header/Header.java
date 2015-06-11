package com.sap.sailing.gwt.home.mobile.partials.header;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.mobile.app.MobileApplicationClientFactory;

public class Header extends Composite {

    // @UiField TextBox searchText;
    // @UiField Button searchButton;

    @UiField
    DivElement locationTitleUi;

    interface HeaderUiBinder extends UiBinder<Widget, Header> {
    }
    
    private static HeaderUiBinder uiBinder = GWT.create(HeaderUiBinder.class);
    private MobileApplicationClientFactory appContext;

    
    public Header(MobileApplicationClientFactory appContext) {


        this.appContext = appContext;
        HeaderResources.INSTANCE.css().ensureInjected();

        initWidget(uiBinder.createAndBindUi(this));

    }

    @UiHandler("homeLinkUi")
    public void goToHome(ClickEvent e) {
        appContext //
                .getNavigator() //
                .getHomeNavigation()//
                .goToPlace();
    }

    @UiHandler("eventsLinkUi")
    public void goToEvents(ClickEvent e) {
        appContext //
                .getNavigator() //
                .getEventsNavigation()//
                .goToPlace();
    }

    public void setLocationTitle(String locationTitle) {
        locationTitleUi.setInnerText(locationTitle);
    }
}
