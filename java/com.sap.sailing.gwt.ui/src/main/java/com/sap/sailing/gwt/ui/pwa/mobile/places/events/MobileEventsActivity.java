package com.sap.sailing.gwt.ui.pwa.mobile.places.events;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.ui.pwa.PwaClientFactory;
import com.sap.sailing.gwt.ui.pwa.mobile.AdminConsoleMobileView;

public class MobileEventsActivity extends AbstractActivity {
    
    private final PwaClientFactory clientFactory;
      
    private MobileEventsPresenter mobileEventsPresenter;
    private AdminConsoleMobileView mobileView;
    
    public MobileEventsActivity(final MobileEventsPlace place, final PwaClientFactory clientFactory) {
        this.clientFactory = clientFactory;
        
        mobileView = new AdminConsoleMobileView(clientFactory.getEventBus());
        mobileEventsPresenter = new MobileEventsPresenter(mobileView);
    }
    
    @Override
    public void start(AcceptsOneWidget containerWidget, EventBus eventBus) {
        
    }
    
    private class MobileEventsPresenter {
        
        final AdminConsoleMobileView view;
        
        private MobileEventsPresenter(AdminConsoleMobileView view) {
            this.view = view;
        }
       
    }
 
}
