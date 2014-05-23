package com.sap.sailing.gwt.home.client.app.start;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.shared.dto.EventDTO;

public class Event extends Composite {
    private static EventUiBinder uiBinder = GWT.create(EventUiBinder.class);

    private EventDTO event;

    @UiField
    Label eventName;
    @UiField
    Label eventLocation;

    interface EventUiBinder extends UiBinder<Widget, Event> {
    }

    public Event() {
        super();

        initWidget(uiBinder.createAndBindUi(this));
    }

    public EventDTO getEvent() {
        return event;
    }

    public void setEvent(EventDTO event) {
        this.event = event;
        eventName.setText(event.getName());
        eventLocation.setText(event.venue.getName());
    }
}
