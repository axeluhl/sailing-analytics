package com.sap.sailing.gwt.home.client.place.event.regattaschedule;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.place.event.EventPageNavigator;
import com.sap.sailing.gwt.ui.shared.EventDTO;

public class EventRegattaScheduleSeries extends Composite {
    private static EventRegattaScheduleSeriesUiBinder uiBinder = GWT.create(EventRegattaScheduleSeriesUiBinder.class);

    interface EventRegattaScheduleSeriesUiBinder extends UiBinder<Widget, EventRegattaScheduleSeries> {
    }

    @SuppressWarnings("unused")
    private final EventDTO event;
    
    public EventRegattaScheduleSeries(EventDTO event, EventPageNavigator pageNavigator) {
        this.event = event;
        
        EventRegattaScheduleResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
    }
    
}
