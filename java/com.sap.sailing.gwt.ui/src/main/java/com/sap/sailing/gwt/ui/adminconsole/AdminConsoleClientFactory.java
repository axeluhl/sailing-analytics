package com.sap.sailing.gwt.ui.adminconsole;

import com.google.gwt.place.shared.PlaceController;
import com.google.web.bindery.event.shared.EventBus;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.security.ui.client.WithSecurity;

public interface AdminConsoleClientFactory extends WithSecurity{
    
    public EventBus getEventBus();
    
    public PlaceController getPlaceController();
    
    public ErrorReporter getErrorReporter();
    
}
