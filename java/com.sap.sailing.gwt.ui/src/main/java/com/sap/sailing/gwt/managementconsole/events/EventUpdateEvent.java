package com.sap.sailing.gwt.managementconsole.events;

import com.google.gwt.event.shared.GwtEvent;

public class EventUpdateEvent extends GwtEvent<ManagementConsoleEventHandler> {
    
    public static Type<ManagementConsoleEventHandler> TYPE = new Type<>();

    @Override
    public Type<ManagementConsoleEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(ManagementConsoleEventHandler handler) {
        handler.onEvent(this);
    }

}
