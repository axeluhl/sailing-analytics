package com.sap.sailing.gwt.ui.adminconsole;

import com.google.gwt.activity.shared.ActivityManager;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.place.shared.PlaceHistoryHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.EventBus;
import com.sap.sailing.gwt.ui.adminconsole.places.AdminConsolePlace;
import com.sap.sailing.gwt.ui.client.AbstractSailingWriteEntryPoint;
import com.sap.sse.gwt.resources.Highcharts;

public class AdminConsoleDesktopEntryPoint extends AbstractSailingWriteEntryPoint {         
    
    @Override
    protected void doOnModuleLoad() {
        Highcharts.ensureInjectedWithMore();
        super.doOnModuleLoad();      
                
        initActivitiesAndPlaces();
    }
     
    private void initActivitiesAndPlaces() {
        final AdminConsoleClientFactory clientFactory = new AdminConsoleDesktopClientFactoryImpl(getSailingService());
        EventBus eventBus = clientFactory.getEventBus();
        PlaceController placeController = clientFactory.getPlaceController();
        
        AdminConsoleActivityMapper activityMapper = new AdminConsoleActivityMapper(clientFactory);
        ActivityManager activityManager = new ActivityManager(activityMapper, eventBus);
        activityManager.setDisplay(clientFactory.getContent());
        
        AdminConsolePlaceHistoryMapper historyMapper = GWT.create(AdminConsolePlaceHistoryMapper.class);
        PlaceHistoryHandler historyHandler = new PlaceHistoryHandler(historyMapper);
        historyHandler.register(placeController, eventBus, new AdminConsolePlace());
        
        placeWidgetOnRootPanel(clientFactory.getRoot()); 
        
        addHistoryValueChangeHandler(clientFactory, activityMapper);
        
        historyHandler.handleCurrentHistory();
    }
    
    private void addHistoryValueChangeHandler(final AdminConsoleClientFactory clientFactory, final AdminConsoleActivityMapper activityMapper) {
        History.addValueChangeHandler(new ValueChangeHandler<String>() {
            
            public void onValueChange(ValueChangeEvent<String> event) {
                final String token = event.getValue();
                
                if (token == null || token.isEmpty()) { 
                    clientFactory.getPlaceController().goTo(new AdminConsolePlace());       
                }
            }
            });
    }
    
    protected void placeWidgetOnRootPanel(Widget rootWidget) {
        if (rootWidget != null) {
            RootPanel.get().add(rootWidget);
        }
    }

}