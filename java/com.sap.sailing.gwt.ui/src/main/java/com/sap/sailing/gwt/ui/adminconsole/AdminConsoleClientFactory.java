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

public interface AdminConsoleClientFactory extends WithSecurity {
    EventBus getEventBus();
    
    PlaceController getPlaceController();
    
    ErrorReporter getErrorReporter();
    
    SailingServiceWriteAsync getSailingService();
    
    MediaServiceWriteAsync getMediaServiceWrite();
    
    Widget getRoot();
    
    TopLevelView getTopLevelView();

    AcceptsOneWidget getContent();
}
