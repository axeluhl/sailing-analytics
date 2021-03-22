package com.sap.sailing.gwt.managementconsole.events;

import java.util.List;

import com.sap.sailing.gwt.common.communication.event.EventSeriesMetadataDTO;

public class EventSeriesListResponseEvent
        extends ListResponseEvent<EventSeriesMetadataDTO, ListResponseEvent.Handler<EventSeriesMetadataDTO>> {

    public static final Type<Handler<EventSeriesMetadataDTO>> TYPE = new Type<>();

    public EventSeriesListResponseEvent(final List<EventSeriesMetadataDTO> eventSeries) {
        super(TYPE, eventSeries);
    }

}
