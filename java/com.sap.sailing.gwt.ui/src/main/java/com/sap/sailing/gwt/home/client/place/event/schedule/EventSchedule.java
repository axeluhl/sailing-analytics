package com.sap.sailing.gwt.home.client.place.event.schedule;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.shared.EventDTO;

public class EventSchedule extends Composite {
    private static EventScheduleUiBinder uiBinder = GWT.create(EventScheduleUiBinder.class);

    interface EventScheduleUiBinder extends UiBinder<Widget, EventSchedule> {
    }

    @SuppressWarnings("unused")
    private final EventDTO event;
    
    public EventSchedule(EventDTO event) {
        this.event = event;
        
        EventScheduleResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
    }
    
}
