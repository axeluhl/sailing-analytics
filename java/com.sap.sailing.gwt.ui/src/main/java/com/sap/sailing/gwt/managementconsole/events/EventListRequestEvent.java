package com.sap.sailing.gwt.managementconsole.events;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

public class EventListRequestEvent extends GwtEvent<EventListRequestEvent.Handler> {

    public interface Handler extends EventHandler {

        void onEventListResponse(EventListRequestEvent event);
    }

    public static final Type<Handler> TYPE = new Type<>();

    @Override
    public Type<Handler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(final Handler handler) {
        handler.onEventListResponse(this);
    }

}
