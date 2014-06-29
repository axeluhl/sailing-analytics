package com.sap.sailing.gwt.home.client.place.event.regattaschedule;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.place.event.EventPageNavigator;
import com.sap.sailing.gwt.ui.shared.EventDTO;

public class EventRegattaSchedule extends Composite {
    private static EventRegattaScheduleUiBinder uiBinder = GWT.create(EventRegattaScheduleUiBinder.class);

    interface EventRegattaScheduleUiBinder extends UiBinder<Widget, EventRegattaSchedule> {
    }

    @SuppressWarnings("unused")
    private final EventDTO event;
    
    public EventRegattaSchedule(EventDTO event, EventPageNavigator pageNavigator) {
        this.event = event;
        
        EventRegattaScheduleResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
    }
    
}
