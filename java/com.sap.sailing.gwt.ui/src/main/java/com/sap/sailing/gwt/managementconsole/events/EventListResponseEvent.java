package com.sap.sailing.gwt.managementconsole.events;

import java.util.List;

import com.sap.sailing.gwt.common.communication.event.EventMetadataDTO;

public class EventListResponseEvent
        extends ListResponseEvent<EventMetadataDTO, ListResponseEvent.Handler<EventMetadataDTO>> {

    public static final Type<Handler<EventMetadataDTO>> TYPE = new Type<>();

    public EventListResponseEvent(final List<EventMetadataDTO> events) {
        super(TYPE, events);
    }

}
