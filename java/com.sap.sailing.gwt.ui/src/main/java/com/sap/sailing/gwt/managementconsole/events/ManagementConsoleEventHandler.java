package com.sap.sailing.gwt.managementconsole.events;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

public interface ManagementConsoleEventHandler<E extends GwtEvent<? extends EventHandler>> extends EventHandler {
    
    void onEvent(E event);

}
