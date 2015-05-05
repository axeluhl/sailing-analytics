package com.sap.sailing.gwt.regattaoverview.client;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.sap.sailing.gwt.ui.shared.EventDTO;

public class EventDTOLoadedEvent extends GwtEvent<EventDTOLoadedEvent.Handler> {

    public static final Type<Handler> TYPE = new Type<EventDTOLoadedEvent.Handler>();

    public interface Handler extends EventHandler {
        void onEventDTOLoaded(EventDTOLoadedEvent e);
    }

    private final EventDTO currentEvent;

    public EventDTOLoadedEvent(EventDTO currentEvent) {
        super();
        this.currentEvent = currentEvent;
    }

    public EventDTO getCurrentEvent() {
        return currentEvent;
    }

    @Override
    public Type<Handler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(Handler handler) {
        handler.onEventDTOLoaded(this);
    }

}
