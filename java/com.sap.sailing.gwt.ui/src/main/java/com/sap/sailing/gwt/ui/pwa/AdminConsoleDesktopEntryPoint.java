package com.sap.sailing.gwt.ui.pwa;

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
import com.sap.sailing.gwt.ui.client.AbstractSailingWriteEntryPoint;
import com.sap.sailing.gwt.ui.pwa.desktop.places.DesktopEventsPlace;
import com.sap.sse.gwt.resources.Highcharts;

public class AdminConsoleDesktopEntryPoint extends AbstractSailingWriteEntryPoint {         
    
    @Override
    protected void doOnModuleLoad() {
        Highcharts.ensureInjectedWithMore();
        super.doOnModuleLoad();      
                
        initActivitiesAndPlaces();
    }
     
    private void initActivitiesAndPlaces() {
        final AdminConsoleDesktopClientFactoryImpl clientFactory = new AdminConsoleDesktopClientFactoryImpl(getSailingService());
        EventBus eventBus = clientFactory.getEventBus();
        PlaceController placeController = clientFactory.getPlaceController();
        
        AdminConsoleDesktopActivityMapper activityMapper = new AdminConsoleDesktopActivityMapper(clientFactory);
        ActivityManager activityManager = new ActivityManager(activityMapper, eventBus);
        activityManager.setDisplay(clientFactory.getContent());
        
        PwaPlaceHistoryMapper historyMapper = GWT.create(PwaPlaceHistoryMapper.class);
        PlaceHistoryHandler historyHandler = new PlaceHistoryHandler(historyMapper);
        historyHandler.register(placeController, eventBus, new DesktopEventsPlace());
        
        placeWidgetOnRootPanel(clientFactory.getRoot()); 
        
        addHistoryValueChangeHandler(clientFactory, activityMapper);
        
        historyHandler.handleCurrentHistory();
    }
    
    private void addHistoryValueChangeHandler(final PwaClientFactory clientFactory, final AdminConsoleDesktopActivityMapper activityMapper) {
        History.addValueChangeHandler(new ValueChangeHandler<String>() {
            
            public void onValueChange(ValueChangeEvent<String> event) {
                final String token = event.getValue();
                
                if (token == null || token.isEmpty()) { 
                    clientFactory.getPlaceController().goTo(new DesktopEventsPlace());       
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