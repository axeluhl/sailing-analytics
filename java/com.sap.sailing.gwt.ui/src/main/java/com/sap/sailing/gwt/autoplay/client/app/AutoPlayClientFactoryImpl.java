package com.sap.sailing.gwt.autoplay.client.app;

import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceController;
import com.google.web.bindery.event.shared.EventBus;
import com.sap.sailing.gwt.autoplay.client.events.AutoPlayFailureEvent;
import com.sap.sailing.gwt.autoplay.client.places.autoplaystart.AutoPlayStartPlace;
import com.sap.sailing.gwt.home.communication.SailingDispatchSystem;
import com.sap.sailing.gwt.home.communication.SailingDispatchSystemImpl;
import com.sap.sse.gwt.client.mvp.ErrorView;


public class AutoPlayClientFactoryImpl extends AutoPlayClientFactoryBase {

    private AutoPlayContext currentContext;
    private final SailingDispatchSystem dispatch = new SailingDispatchSystemImpl();

    public AutoPlayClientFactoryImpl() {
        this(new SimpleEventBus());
    }
    
    private AutoPlayClientFactoryImpl(EventBus eventBus) {
        this(eventBus, new PlaceController(eventBus));
    }

    private AutoPlayClientFactoryImpl(EventBus eventBus, PlaceController placeController) {
        this(eventBus, placeController, new AutoplayNavigatorImpl(placeController));
    }

    private AutoPlayClientFactoryImpl(EventBus eventBus, PlaceController placeController,
            AutoPlayPlaceNavigator navigator) {
        super(new AutoPlayMainViewImpl(eventBus), eventBus, placeController, navigator);
    }
    

    @Override
    public ErrorView createErrorView(String errorMessage, Throwable errorReason) {
        return null;
    }

    @Override
    public Place getDefaultPlace() {
        return new AutoPlayStartPlace();
    }

    @Override
    public void setAutoPlayContext(AutoPlayContext ctx) {
        this.currentContext = ctx;
    }

    @Override
    public AutoPlayContext getAutoPlayCtxSignalError() {
        if (currentContext == null) {
            getEventBus().fireEvent(new AutoPlayFailureEvent("No autoplay context found"));
        }
        return currentContext;
    }

    @Override
    public SailingDispatchSystem getDispatch() {
        return dispatch;
    }

    @Override
    /**
     * The context can be uninitialized, if a direct place url is used. In this case a round trip via the startview that
     * can parse the url parameter configuration is required.
     */
    public boolean isConfigured() {
        return currentContext != null && currentContext.getEvent() != null;
    }

}
