package com.sap.sailing.gwt.home.mobile;

import com.google.gwt.activity.shared.ActivityManager;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.place.shared.PlaceHistoryHandler;
import com.google.gwt.place.shared.PlaceHistoryMapper;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.SimpleEventBus;
import com.sap.sailing.gwt.home.client.place.start.StartPlace;
import com.sap.sailing.gwt.home.mobile.app.MobileActivityMapper;
import com.sap.sailing.gwt.home.mobile.app.MobileApplicationClientFactory;
import com.sap.sailing.gwt.home.mobile.app.MobileHistoryMapper;
import com.sap.sailing.gwt.home.mobile.places.MainView;
import com.sap.sailing.gwt.home.mobile.resources.SharedResources;
import com.sap.sailing.gwt.ui.client.RemoteServiceMappingConstants;
import com.sap.sse.gwt.client.EntryPointHelper;

public class MobileEntryPoint implements EntryPoint {
    @Override
    public void onModuleLoad() {

        SharedResources sRes = GWT.create(SharedResources.class);
        sRes.mediaCss().ensureInjected();
        sRes.mainCss().ensureInjected();
        EventBus eventBus = new SimpleEventBus();
        PlaceController placeController = new PlaceController(eventBus);
        MobileApplicationClientFactory appContext = new MobileApplicationClientFactory(placeController);
        EntryPointHelper.registerASyncService((ServiceDefTarget) appContext.getHomeService(),
                RemoteServiceMappingConstants.homeServiceRemotePath);
        
        ActivityMapper mobileActivityMapper = new MobileActivityMapper(appContext);
        ActivityManager activityManager = new CustomActivityManager(mobileActivityMapper, eventBus);
        MainView panel = new MainView(appContext, eventBus);
        RootPanel.get().add(panel);
        activityManager.setDisplay(panel.getContent());
        PlaceHistoryMapper mobileHistoryMapper = GWT.create(MobileHistoryMapper.class);
        PlaceHistoryHandler historyHandler = new PlaceHistoryHandler(mobileHistoryMapper);
        historyHandler.register(placeController, eventBus, new StartPlace());
        RootPanel.get().add(panel);

        historyHandler.handleCurrentHistory();
    }
    
}