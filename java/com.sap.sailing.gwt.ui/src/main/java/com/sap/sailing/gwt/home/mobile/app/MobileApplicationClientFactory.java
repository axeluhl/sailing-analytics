package com.sap.sailing.gwt.home.mobile.app;

import com.google.gwt.core.client.GWT;
import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.SimpleEventBus;
import com.sap.sailing.gwt.home.client.place.start.StartPlace;
import com.sap.sailing.gwt.home.mobile.places.MainView;
import com.sap.sailing.gwt.home.shared.dispatch.DispatchSystem;
import com.sap.sailing.gwt.home.shared.dispatch.DispatchSystemImpl;
import com.sap.sailing.gwt.ui.client.HomeService;
import com.sap.sailing.gwt.ui.client.HomeServiceAsync;
import com.sap.sailing.gwt.ui.client.RemoteServiceMappingConstants;
import com.sap.sse.gwt.client.EntryPointHelper;
import com.sap.sse.gwt.client.ErrorReporter;

/**
 * 
 * @author pgtaboada
 *
 */
public class MobileApplicationClientFactory implements com.sap.sse.gwt.client.mvp.ClientFactory {
    private final HomeServiceAsync homeService;
    private final PlaceController placeController;
    private final MobilePlacesNavigator navigator;
    private final SimpleEventBus eventBus;
    private final DispatchSystem dispatch = new DispatchSystemImpl();
    private MainView mainView;

    public MobileApplicationClientFactory() {
        this(new SimpleEventBus());
    }

    public MobileApplicationClientFactory(SimpleEventBus eventBus) {
        this(eventBus, new PlaceController(eventBus));
    }

    public MobileApplicationClientFactory(SimpleEventBus eventBus, PlaceController placeController) {
        this.eventBus = eventBus;
        this.placeController = placeController;
        this.homeService = GWT.create(HomeService.class);
        this.navigator = new MobilePlacesNavigator(placeController);
        mainView = new MainView(this);
        EntryPointHelper.registerASyncService((ServiceDefTarget) homeService,
                RemoteServiceMappingConstants.homeServiceRemotePath);
    }


    public MobilePlacesNavigator getNavigator() {
        return navigator;
    }

    public HomeServiceAsync getHomeService() {
        return homeService;
    }

    public PlaceController getPlaceController() {
        return placeController;
    }

    public DispatchSystem getDispatch() {
        return dispatch;
    }

    @Override
    public ErrorReporter getErrorReporter() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public EventBus getEventBus() {
        return eventBus;
    }

    @Override
    public Widget getRoot() {
        return mainView;
    }

    @Override
    public AcceptsOneWidget getContent() {
        return mainView.getContent();
    }

    @Override
    public Place getDefaultPlace() {
        return new StartPlace();
    }
}
