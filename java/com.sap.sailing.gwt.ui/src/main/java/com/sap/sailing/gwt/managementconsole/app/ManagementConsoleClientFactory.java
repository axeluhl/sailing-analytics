package com.sap.sailing.gwt.managementconsole.app;

import com.google.gwt.place.shared.PlaceController;
import com.sap.sailing.gwt.managementconsole.mvp.ViewFactory;
import com.sap.sailing.gwt.managementconsole.services.EventService;
import com.sap.sailing.gwt.managementconsole.services.RegattaService;
import com.sap.sailing.gwt.ui.client.MediaServiceWriteAsync;
import com.sap.sailing.gwt.ui.client.SailingServiceWriteAsync;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.security.ui.authentication.WithAuthenticationManager;
import com.sap.sse.security.ui.client.WithSecurity;

public interface ManagementConsoleClientFactory extends WithSecurity, WithAuthenticationManager {

    public PlaceController getPlaceController();

    public ErrorReporter getErrorReporter();

    public SailingServiceWriteAsync getSailingService();

    public MediaServiceWriteAsync getMediaServiceWrite();

    public EventService getEventService();
    
    public RegattaService getRegattaService();

    public ViewFactory getViewFactory();

}
