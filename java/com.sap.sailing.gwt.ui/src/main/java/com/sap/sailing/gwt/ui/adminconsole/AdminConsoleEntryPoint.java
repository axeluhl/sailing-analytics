package com.sap.sailing.gwt.ui.adminconsole;

import static com.sap.sse.gwt.shared.RpcConstants.HEADER_FORWARD_TO_MASTER;

import com.google.gwt.activity.shared.ActivityManager;
import com.google.gwt.activity.shared.ActivityMapper;
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
    
    // TODO sarah
    private final MediaServiceWriteAsync mediaServiceWrite = GWT.create(MediaServiceWrite.class);
    
    private SimplePanel appWidget = new SimplePanel();
    
    @Override
    protected void doOnModuleLoad() {
        Highcharts.ensureInjectedWithMore();
        super.doOnModuleLoad();
        EntryPointHelper.registerASyncService((ServiceDefTarget) mediaServiceWrite, RemoteServiceMappingConstants.mediaServiceWriteRemotePath, HEADER_FORWARD_TO_MASTER);
        
        //getUserService().executeWithServerInfo(this::createUI);
        //getUserService().addUserStatusEventHandler((u, p) -> checkPublicServerNonPublicUserWarning());
        
        initActivitiesAndPlaces();
    }
     
    private void initActivitiesAndPlaces() {
        final AdminConsoleClientFactory clientFactory = GWT.create(AdminConsoleClientFactoryImpl.class);
        EventBus eventBus = clientFactory.getEventBus();
        clientFactory.setSailingService(getSailingService());
        PlaceController placeController = clientFactory.getPlaceController();
        
        ActivityMapper activityMapper = new AdminConsoleActivityMapper(clientFactory);
        ActivityManager activityManager = new ActivityManager(activityMapper, eventBus);
        activityManager.setDisplay(appWidget);
        
        AdminConsolePlaceHistoryMapper historyMapper = GWT.create(AdminConsolePlaceHistoryMapper.class);
        PlaceHistoryHandler historyHandler = new PlaceHistoryHandler(historyMapper);
        historyHandler.register(placeController, eventBus, new AdminConsolePlace());
        
        addHistoryToken(clientFactory);
        
        RootLayoutPanel.get().add(appWidget);
        
        historyHandler.handleCurrentHistory();
    }
    
    private void addHistoryToken(final AdminConsoleClientFactory clientFactory) {
        History.addValueChangeHandler(new ValueChangeHandler<String>() {
            
            public void onValueChange(ValueChangeEvent<String> event) {
                final String token = event.getValue();
                
                AdminConsolePlace place;
                
                if (token != null && token.contains(":")) { 
                    final String[] tabAndMenu = token.split(":");
                    place = new AdminConsolePlace(tabAndMenu[1], tabAndMenu[2]);
                } else {
                    place = new AdminConsolePlace();
                }
 
                clientFactory.getPlaceController().goTo(place);
              }
            });
    }

}