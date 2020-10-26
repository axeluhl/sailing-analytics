package com.sap.sailing.gwt.ui.pwa;

import static com.sap.sse.common.HttpRequestHeaderConstants.HEADER_FORWARD_TO_MASTER;

import com.google.gwt.activity.shared.ActivityManager;
import com.google.gwt.core.client.GWT;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.place.shared.PlaceHistoryHandler;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimpleLayoutPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.EventBus;
import com.sap.sailing.gwt.ui.client.AbstractSailingWriteEntryPoint;
import com.sap.sailing.gwt.ui.client.MediaServiceWrite;
import com.sap.sailing.gwt.ui.client.MediaServiceWriteAsync;
import com.sap.sailing.gwt.ui.client.RemoteServiceMappingConstants;
import com.sap.sailing.gwt.ui.pwa.mobile.places.events.MobileEventsPlace;
import com.sap.sse.gwt.client.EntryPointHelper;
import com.sap.sse.gwt.resources.Highcharts;

public class AdminConsoleMobileEntryPoint extends AbstractSailingWriteEntryPoint {    
    
    private final MediaServiceWriteAsync mediaServiceWrite = GWT.create(MediaServiceWrite.class);
    
    private SimplePanel appWidget = new SimpleLayoutPanel();
    
    @Override
    protected void doOnModuleLoad() {
        Highcharts.ensureInjectedWithMore();
        super.doOnModuleLoad();
        EntryPointHelper.registerASyncService((ServiceDefTarget) mediaServiceWrite, RemoteServiceMappingConstants.mediaServiceRemotePath, HEADER_FORWARD_TO_MASTER);
                
        initActivitiesAndPlaces();
    }
     
    private void initActivitiesAndPlaces() {
        final PwaClientFactory clientFactory = new AdminConsoleMobileClientFactoryImpl(getSailingService());
        EventBus eventBus = clientFactory.getEventBus();
        PlaceController placeController = clientFactory.getPlaceController();
        
        AdminConsoleMobileActivityMapper activityMapper = new AdminConsoleMobileActivityMapper(clientFactory);
        ActivityManager activityManager = new ActivityManager(activityMapper, eventBus);
        activityManager.setDisplay(appWidget);
        
        PwaPlaceHistoryMapper historyMapper = GWT.create(PwaPlaceHistoryMapper.class);
        PlaceHistoryHandler historyHandler = new PlaceHistoryHandler(historyMapper);
        historyHandler.register(placeController, eventBus, new MobileEventsPlace());
        
        placeWidgetOnRootPanel(clientFactory.getRoot()); 
        
        historyHandler.handleCurrentHistory();        
    }
    
    protected void placeWidgetOnRootPanel(Widget rootWidget) {
        if (rootWidget != null) {
            RootPanel.get().add(rootWidget);
        }
    }

}