package com.sap.sailing.gwt.ui.pwa.desktop.places;

import java.util.logging.Logger;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.ui.client.MediaServiceWriteAsync;
import com.sap.sailing.gwt.ui.client.SailingServiceWriteAsync;
import com.sap.sailing.gwt.ui.pwa.AdminConsoleDesktopClientFactoryImpl;
import com.sap.sailing.gwt.ui.pwa.desktop.AdminConsoleDesktopView;

public class DesktopEventsActivity extends AbstractActivity {
    
    private static Logger LOG = Logger.getLogger(DesktopEventsActivity.class.getName());
    
    private final AdminConsoleDesktopClientFactoryImpl clientFactory;
    
    private final MediaServiceWriteAsync mediaServiceWrite;
    private final SailingServiceWriteAsync sailingService;
    
    private AdminConsoleDesktopView view;
    
    public DesktopEventsActivity(final DesktopEventsPlace place, final AdminConsoleDesktopClientFactoryImpl clientFactory) {
        LOG.info("DesktopEventsActivity");
        this.clientFactory = clientFactory;
        this.mediaServiceWrite = clientFactory.getMediaServiceWrite();
        this.sailingService = clientFactory.getSailingService();
        
        view = new AdminConsoleDesktopView(clientFactory.getEventBus(), clientFactory.getUserService(), sailingService);
    }
    
    @Override
    public void start(AcceptsOneWidget containerWidget, EventBus eventBus) {
        
    }
 
}
