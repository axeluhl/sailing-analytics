package com.sap.sailing.gwt.ui.adminconsole.desktop.app.places;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.ui.adminconsole.AdminConsoleDesktopClientFactoryImpl;
import com.sap.sailing.gwt.ui.client.MediaServiceWriteAsync;
import com.sap.sailing.gwt.ui.client.SailingServiceWriteAsync;

public class DesktopEventsActivity extends AbstractActivity {

    private final String menu;
    private final String tab;
    
    private final AdminConsoleDesktopClientFactoryImpl clientFactory;
    
    private final MediaServiceWriteAsync mediaServiceWrite;
    private final SailingServiceWriteAsync sailingService;
    
    public DesktopEventsActivity(final DesktopEventsPlace place, final AdminConsoleDesktopClientFactoryImpl clientFactory) {
        this.menu = place.getMenu();
        this.tab = place.getTab();
        this.clientFactory = clientFactory;
        this.mediaServiceWrite = clientFactory.getMediaServiceWrite();
        this.sailingService = clientFactory.getSailingService();
    }
    
    @Override
    public void start(AcceptsOneWidget containerWidget, EventBus eventBus) {
       
        
    }
 
}
