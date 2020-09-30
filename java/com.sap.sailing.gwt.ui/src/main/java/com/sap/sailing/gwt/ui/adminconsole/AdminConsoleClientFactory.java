package com.sap.sailing.gwt.ui.adminconsole;

import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.EventBus;
import com.sap.sailing.gwt.ui.client.MediaServiceWriteAsync;
import com.sap.sailing.gwt.ui.client.SailingServiceWriteAsync;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.mvp.TopLevelView;
import com.sap.sse.security.ui.client.WithSecurity;

public interface AdminConsoleClientFactory extends WithSecurity{
    
    public EventBus getEventBus();
    
    public PlaceController getPlaceController();
    
    public ErrorReporter getErrorReporter();
    
    public SailingServiceWriteAsync getSailingService();
    
    public MediaServiceWriteAsync getMediaServiceWrite();
    
    public Widget getRoot();
    
    public TopLevelView getTopLevelView();

    public AcceptsOneWidget getContent();
}
