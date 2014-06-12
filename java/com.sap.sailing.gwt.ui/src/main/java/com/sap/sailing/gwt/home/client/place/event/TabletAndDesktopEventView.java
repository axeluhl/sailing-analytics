package com.sap.sailing.gwt.home.client.place.event;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.place.event.header.EventHeader;
import com.sap.sailing.gwt.home.client.shared.eventsponsors.EventSponsors;
import com.sap.sailing.gwt.ui.shared.EventDTO;

public class TabletAndDesktopEventView extends Composite implements EventView {
    private static EventViewUiBinder uiBinder = GWT.create(EventViewUiBinder.class);

    interface EventViewUiBinder extends UiBinder<Widget, TabletAndDesktopEventView> {
    }

    @UiField(provided=true) EventHeader eventHeader;
    @UiField EventSponsors eventSponsors;

    public TabletAndDesktopEventView(EventDTO event) {
        eventHeader = new EventHeader(event);
        initWidget(uiBinder.createAndBindUi(this));
    }
}
