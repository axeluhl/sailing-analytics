package com.sap.sailing.gwt.home.client.app;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.app.aboutus.AboutUsClientFactory;
import com.sap.sailing.gwt.home.client.app.contact.ContactClientFactory;
import com.sap.sailing.gwt.home.client.app.event.EventClientFactory;
import com.sap.sailing.gwt.home.client.app.events.EventsClientFactory;
import com.sap.sailing.gwt.home.client.app.start.StartClientFactory;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;

public interface ApplicationClientFactory extends AboutUsClientFactory, ContactClientFactory, EventClientFactory, EventsClientFactory, StartClientFactory {
    Widget getRoot();
    
    AcceptsOneWidget getStage();

    Place getDefaultPlace();
    
    SailingServiceAsync getSailingService();
}
