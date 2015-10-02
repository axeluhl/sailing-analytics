package com.sap.sailing.gwt.home.mobile.app;

import com.google.gwt.core.client.GWT;
import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.SimpleEventBus;
import com.sap.sailing.gwt.home.client.place.start.StartPlace;
import com.sap.sailing.gwt.home.desktop.app.ApplicationTopLevelView;
import com.sap.sailing.gwt.home.mobile.places.MainView;
import com.sap.sailing.gwt.home.shared.dispatch.DispatchSystem;
import com.sap.sailing.gwt.home.shared.dispatch.DispatchSystemImpl;
import com.sap.sailing.gwt.ui.client.HomeService;
import com.sap.sailing.gwt.ui.client.HomeServiceAsync;
import com.sap.sailing.gwt.ui.client.RemoteServiceMappingConstants;
import com.sap.sse.gwt.client.EntryPointHelper;
import com.sap.sse.security.ui.client.SecureClientFactoryImpl;

/**
 * 
 * @author pgtaboada
 *
 */
public class MobileApplicationClientFactory extends SecureClientFactoryImpl {
    private final HomeServiceAsync homeService;
    private final MobilePlacesNavigator navigator;
    private final DispatchSystem dispatch = new DispatchSystemImpl();

    public MobileApplicationClientFactory(boolean isStandaloneServer) {
        this(new SimpleEventBus(), isStandaloneServer);
    }

    private MobileApplicationClientFactory(SimpleEventBus eventBus, boolean isStandaloneServer) {
        this(eventBus, new PlaceController(eventBus), isStandaloneServer);
    }

    private MobileApplicationClientFactory(EventBus eventBus, PlaceController placeController, boolean isStandaloneServer) {
        this(eventBus, placeController, new MobilePlacesNavigator(placeController, isStandaloneServer));
    }

    private MobileApplicationClientFactory(EventBus eventBus, PlaceController placeController, MobilePlacesNavigator navigator) {
        this(new MainView(navigator, eventBus), eventBus, placeController, navigator);
    }

    public MobileApplicationClientFactory(ApplicationTopLevelView root, EventBus eventBus,
            PlaceController placeController, final MobilePlacesNavigator navigator) {
        super(root, eventBus, placeController);
        this.navigator = navigator;
        this.homeService = GWT.create(HomeService.class);
        EntryPointHelper.registerASyncService((ServiceDefTarget) homeService, RemoteServiceMappingConstants.homeServiceRemotePath);
    }

    public MobilePlacesNavigator getNavigator() {
        return navigator;
    }

    public HomeServiceAsync getHomeService() {
        return homeService;
    }

    public DispatchSystem getDispatch() {
        return dispatch;
    }

    @Override
    public Place getDefaultPlace() {
        return new StartPlace();
    }
}
