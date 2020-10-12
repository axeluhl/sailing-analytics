package com.sap.sailing.gwt.ui.adminconsole.mobile.app.places.events;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.ui.adminconsole.AdminConsoleClientFactory;

public class MobileEventsActivity extends AbstractActivity {
    
    private final AdminConsoleClientFactory clientFactory;
      
    private MobileEventsPresenter mobileEventsPresenter;
    
    public MobileEventsActivity(final MobileEventsPlace place, final AdminConsoleClientFactory clientFactory) {
        this.clientFactory = clientFactory;
    }
    
    @Override
    public void start(AcceptsOneWidget containerWidget, EventBus eventBus) {
        mobileEventsPresenter = new MobileEventsPresenter();
        containerWidget.setWidget(mobileEventsPresenter.view.asWidget());
    }
    
    private class MobileEventsPresenter implements MobileEventsView.Presenter {
        
        final MobileEventsView view;
        
        private MobileEventsPresenter() {
            view = new MobileEventsViewImpl(this);
        }
       
    }
 
}
