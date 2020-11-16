package com.sap.sailing.gwt.managementconsole.events;

import java.util.List;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.sap.sailing.gwt.ui.shared.EventDTO;

public class EventListUpdateEvent extends GwtEvent<EventListUpdateEvent.Handler> {

    public interface Handler extends EventHandler {

        void onEventUpdate(EventListUpdateEvent event);

    }

    public static Type<Handler> TYPE = new Type<>();

    private final List<EventDTO> events;

    public EventListUpdateEvent(List<EventDTO> events) {
        this.events = events;
    }

    @Override
    public Type<Handler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(Handler handler) {
        handler.onEventUpdate(this);
    }

    public List<EventDTO> getEvents() {
        return events;
    }

}
