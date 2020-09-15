package com.sap.sailing.gwt.ui.adminconsole;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.PlaceController;
import com.sap.sailing.gwt.ui.client.SailingServiceWriteAsync;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.security.ui.client.WithSecurity;

public interface AdminConsoleClientFactory extends WithSecurity{
    
    public EventBus getEventBus();
    
    public PlaceController getPlaceController();
    
    public ErrorReporter getErrorReporter();
    
    public SailingServiceWriteAsync getSailingService();
    
    // TODO sarah gehört das hierhin?
    public void setSailingService(SailingServiceWriteAsync sailingService);
    
}
