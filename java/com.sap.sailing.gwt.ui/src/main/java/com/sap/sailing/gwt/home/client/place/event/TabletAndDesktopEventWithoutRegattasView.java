package com.sap.sailing.gwt.home.client.place.event;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.app.HomePlacesNavigator;
import com.sap.sailing.gwt.home.client.place.event.header.EventHeader;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.shared.EventDTO;

public class TabletAndDesktopEventWithoutRegattasView extends Composite implements EventWithoutRegattasView {
    private static EventViewUiBinder uiBinder = GWT.create(EventViewUiBinder.class);

    interface EventViewUiBinder extends UiBinder<Widget, TabletAndDesktopEventWithoutRegattasView> {
    }

    @UiField(provided=true) EventHeader eventHeader;
    
    public TabletAndDesktopEventWithoutRegattasView(SailingServiceAsync sailingService, EventDTO event, HomePlacesNavigator placeNavigator) {
        eventHeader = new EventHeader(event, placeNavigator);

        initWidget(uiBinder.createAndBindUi(this));
    }
}
