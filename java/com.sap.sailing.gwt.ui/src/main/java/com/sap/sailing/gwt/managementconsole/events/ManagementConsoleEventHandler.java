package com.sap.sailing.gwt.managementconsole.events;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

public interface ManagementConsoleEventHandler extends EventHandler {
    
    void onEvent(GwtEvent<? extends EventHandler> event);

}
