package com.sap.sailing.gwt.ui.adminconsole.mobile.app.places.events;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.ui.adminconsole.AdminConsoleClientFactory;
import com.sap.sailing.gwt.ui.client.MediaServiceWriteAsync;
import com.sap.sailing.gwt.ui.client.SailingServiceWriteAsync;

public class MobileEventsActivity extends AbstractActivity {
    
    private final AdminConsoleClientFactory clientFactory;
    
    private final MediaServiceWriteAsync mediaServiceWrite;
    private final SailingServiceWriteAsync sailingService;
    
    public MobileEventsActivity(final MobileEventsPlace place, final AdminConsoleClientFactory clientFactory) {
        this.clientFactory = clientFactory;
        this.mediaServiceWrite = clientFactory.getMediaServiceWrite();
        this.sailingService = clientFactory.getSailingService();
    }
    
    @Override
    public void start(AcceptsOneWidget containerWidget, EventBus eventBus) {
       
        
    }
 
}
