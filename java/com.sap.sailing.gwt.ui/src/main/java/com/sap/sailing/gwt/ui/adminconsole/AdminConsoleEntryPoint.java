package com.sap.sailing.gwt.ui.adminconsole;

import static com.sap.sse.gwt.shared.RpcConstants.HEADER_FORWARD_TO_MASTER;

import com.google.gwt.activity.shared.ActivityManager;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.place.shared.PlaceHistoryHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.web.bindery.event.shared.EventBus;
import com.sap.sailing.gwt.ui.adminconsole.places.AdminConsolePlace;
import com.sap.sailing.gwt.ui.client.AbstractSailingWriteEntryPoint;
import com.sap.sailing.gwt.ui.client.MediaServiceWrite;
import com.sap.sailing.gwt.ui.client.MediaServiceWriteAsync;
import com.sap.sailing.gwt.ui.client.RemoteServiceMappingConstants;
import com.sap.sse.gwt.client.EntryPointHelper;
import com.sap.sse.gwt.resources.Highcharts;

public class AdminConsoleEntryPoint extends AbstractSailingWriteEntryPoint {    
    
    private final MediaServiceWriteAsync mediaServiceWrite = GWT.create(MediaServiceWrite.class);
    
    private SimplePanel appWidget = new SimplePanel();
    
    @Override
    protected void doOnModuleLoad() {
        Highcharts.ensureInjectedWithMore();
        super.doOnModuleLoad();
        EntryPointHelper.registerASyncService((ServiceDefTarget) mediaServiceWrite, RemoteServiceMappingConstants.mediaServiceRemotePath, HEADER_FORWARD_TO_MASTER);
                
        initActivitiesAndPlaces();
    }
     
    private void initActivitiesAndPlaces() {
        final AdminConsoleClientFactory clientFactory = GWT.create(AdminConsoleClientFactoryImpl.class);
        EventBus eventBus = clientFactory.getEventBus();
        PlaceController placeController = clientFactory.getPlaceController();
        
        AdminConsoleActivityMapper activityMapper = new AdminConsoleActivityMapper(clientFactory, mediaServiceWrite, getSailingService());
        ActivityManager activityManager = new ActivityManager(activityMapper, eventBus);
        activityManager.setDisplay(appWidget);
        
        AdminConsolePlaceHistoryMapper historyMapper = GWT.create(AdminConsolePlaceHistoryMapper.class);
        PlaceHistoryHandler historyHandler = new PlaceHistoryHandler(historyMapper);
        historyHandler.register(placeController, eventBus, new AdminConsolePlace());
        
        RootLayoutPanel.get().add(appWidget);      
        
        addHistoryValueChangeHandler(clientFactory, activityMapper);
        
        historyHandler.handleCurrentHistory();
    }
    
    private void addHistoryValueChangeHandler(final AdminConsoleClientFactory clientFactory, final AdminConsoleActivityMapper activityMapper) {
        History.addValueChangeHandler(new ValueChangeHandler<String>() {
            
            public void onValueChange(ValueChangeEvent<String> event) {
                handleHistoryChange(clientFactory, event);
            }
            });
    }
    
    private void handleHistoryChange(AdminConsoleClientFactory clientFactory, ValueChangeEvent<String> event) {
        final String token = event.getValue();
        
        if (token == null || token.isEmpty()) { 
            clientFactory.getPlaceController().goTo(new AdminConsolePlace());       
        }
    }

}