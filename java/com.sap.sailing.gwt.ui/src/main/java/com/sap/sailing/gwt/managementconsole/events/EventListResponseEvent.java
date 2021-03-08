package com.sap.sailing.gwt.managementconsole.events;

import java.util.List;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.sap.sailing.gwt.ui.shared.EventDTO;

public class EventListResponseEvent extends GwtEvent<EventListResponseEvent.Handler> {

    public interface Handler extends EventHandler {

        void onEventsRefreshed(EventListResponseEvent event);
    }

    public static final Type<EventListResponseEvent.Handler> TYPE = new Type<>();

    private final List<EventDTO> events;

    public EventListResponseEvent(final List<EventDTO> events) {
        this.events = events;
    }

    @Override
    public Type<EventListResponseEvent.Handler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(final EventListResponseEvent.Handler handler) {
        handler.onEventsRefreshed(this);
    }

    public List<EventDTO> getEvents() {
        return events;
    }

}
