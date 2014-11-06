package com.sap.sailing.gwt.home.client.place.event.schedule;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.place.event.AbstractEventComposite;
import com.sap.sailing.gwt.home.client.place.event.EventPlaceNavigator;
import com.sap.sailing.gwt.ui.shared.EventDTO;

public class EventSchedule extends AbstractEventComposite {
    private static EventScheduleUiBinder uiBinder = GWT.create(EventScheduleUiBinder.class);

    interface EventScheduleUiBinder extends UiBinder<Widget, EventSchedule> {
    }
    
    public EventSchedule(EventDTO event, EventPlaceNavigator placeNavigator) {
        super(event, placeNavigator);
        
        EventScheduleResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
    }
    
}
