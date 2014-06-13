package com.sap.sailing.gwt.home.client.shared.eventsoverview;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class EventsOverview extends Composite {

    interface EventsOverviewUiBinder extends UiBinder<Widget, EventsOverview> {
    }
    
    private static EventsOverviewUiBinder uiBinder = GWT.create(EventsOverviewUiBinder.class);

    public EventsOverview() {
        EventsOverviewResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
    }

}
