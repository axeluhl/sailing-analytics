package com.sap.sailing.gwt.home.mobile.app;

import com.google.gwt.core.client.GWT;
import com.google.gwt.place.shared.PlaceController;
import com.sap.sailing.gwt.home.shared.dispatch.DispatchSystem;
import com.sap.sailing.gwt.home.shared.dispatch.DispatchSystemImpl;
import com.sap.sailing.gwt.ui.client.HomeService;
import com.sap.sailing.gwt.ui.client.HomeServiceAsync;

/**
 * 
 * @author pgtaboada
 *
 */
public class MobileApplicationClientFactory {
    private final HomeServiceAsync homeService;
    private final PlaceController placeController;
    private final MobilePlacesNavigator navigator;

    private final DispatchSystem dispatch = new DispatchSystemImpl();

    public MobileApplicationClientFactory(PlaceController placeController) {
        this.navigator = new MobilePlacesNavigator(placeController);
        this.placeController = placeController;
        this.homeService = GWT.create(HomeService.class);
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

}
