package com.sap.sailing.gwt.home.mobile.partials.header;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.place.events.EventsPlace;
import com.sap.sailing.gwt.home.client.place.start.StartPlace;
import com.sap.sailing.gwt.home.mobile.app.ApplicationContext;

public class Header extends Composite {

    // @UiField TextBox searchText;
    // @UiField Button searchButton;

    

    interface HeaderUiBinder extends UiBinder<Widget, Header> {
    }
    
    private static HeaderUiBinder uiBinder = GWT.create(HeaderUiBinder.class);
    private ApplicationContext appContext;

    
    public Header(ApplicationContext appContext) {


        this.appContext = appContext;
        HeaderResources.INSTANCE.css().ensureInjected();

        initWidget(uiBinder.createAndBindUi(this));

    }

    @UiHandler("homeLinkUi")
    public void goToHome(ClickEvent e) {
        appContext.goToPlace(new StartPlace());
    }

    @UiHandler("eventsLinkUi")
    public void goToEvents(ClickEvent e) {
        appContext.goToPlace(new EventsPlace());
    }

}
