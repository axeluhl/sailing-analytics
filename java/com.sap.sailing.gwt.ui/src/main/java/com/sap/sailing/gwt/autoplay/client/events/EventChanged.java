package com.sap.sailing.gwt.autoplay.client.events;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.sap.sailing.gwt.ui.shared.EventDTO;

/**
 * Sample custom event class for copy & paste
 */
public class EventChanged extends GwtEvent<EventChanged.Handler> {
    public static final Type<Handler> TYPE = new Type<Handler>();

    private final EventDTO event;

    /**
     * Event handler interface
     */
    public interface Handler extends EventHandler {
        void onEventChanged(EventChanged e);
    }

    public EventChanged(EventDTO event) {
        this.event = event;
    }

    @Override
    public com.google.gwt.event.shared.GwtEvent.Type<Handler> getAssociatedType() {
        return TYPE;
    }

    public EventDTO getEvent() {
        return event;
    }

    @Override
    protected void dispatch(Handler handler) {
        handler.onEventChanged(this);
    }
}
