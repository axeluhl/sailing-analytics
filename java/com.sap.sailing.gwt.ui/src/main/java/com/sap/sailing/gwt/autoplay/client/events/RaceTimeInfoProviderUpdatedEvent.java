package com.sap.sailing.gwt.autoplay.client.events;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

/**
 * Sample custom event class for copy & paste
 */
public class RaceTimeInfoProviderUpdatedEvent extends GwtEvent<RaceTimeInfoProviderUpdatedEvent.Handler> {

    public static final Type<Handler> TYPE = new Type<Handler>();

    /**
     * Event handler interface
     */
    public interface Handler extends EventHandler {
        void handleNoOpEvent(RaceTimeInfoProviderUpdatedEvent e);
    }

    @Override
    public com.google.gwt.event.shared.GwtEvent.Type<Handler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(Handler handler) {
        handler.handleNoOpEvent(this);
    }
}
