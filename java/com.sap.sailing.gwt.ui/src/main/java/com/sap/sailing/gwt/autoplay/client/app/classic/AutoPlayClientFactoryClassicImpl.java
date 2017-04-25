package com.sap.sailing.gwt.autoplay.client.app.classic;

import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceController;
import com.google.web.bindery.event.shared.EventBus;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayClientFactoryBase;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayContext;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayPlaceNavigator;
import com.sap.sailing.gwt.autoplay.client.events.AutoPlayFailureEvent;
import com.sap.sailing.gwt.autoplay.client.places.startclassic.old.DesktopPlayerView;
import com.sap.sailing.gwt.autoplay.client.places.startclassic.old.PlayerView;
import com.sap.sailing.gwt.autoplay.client.places.startup.classic.ClassicConfigPlace;
import com.sap.sailing.gwt.home.communication.SailingDispatchSystem;
import com.sap.sailing.gwt.home.communication.SailingDispatchSystemImpl;
import com.sap.sse.gwt.client.mvp.ErrorView;


public class AutoPlayClientFactoryClassicImpl extends AutoPlayClientFactoryBase
        implements AutoPlayClientFactoryClassic {

    private AutoPlayContext currentContext;
    private final SailingDispatchSystem dispatch = new SailingDispatchSystemImpl();

    public AutoPlayClientFactoryClassicImpl() {
        this(new SimpleEventBus());
    }
    
    private AutoPlayClientFactoryClassicImpl(EventBus eventBus) {
        this(eventBus, new PlaceController(eventBus));
    }

    private AutoPlayClientFactoryClassicImpl(EventBus eventBus, PlaceController placeController) {
        this(eventBus, placeController, new ClassicPlaceNavigatorImpl(placeController));
    }

    private AutoPlayClientFactoryClassicImpl(EventBus eventBus, PlaceController placeController,
            AutoPlayPlaceNavigator navigator) {
        super(new AutoPlayMainViewDesktopImpl(), eventBus, placeController, navigator);
    }
    

    @Override
    public PlayerView createPlayerView() {
        return new DesktopPlayerView(getPlaceNavigator());
    }

    @Override
    public ErrorView createErrorView(String errorMessage, Throwable errorReason) {
        return null;
    }

    @Override
    public Place getDefaultPlace() {
        return new ClassicConfigPlace();
    }

    @Override
    public void setSlideContext(AutoPlayContext ctx) {
        this.currentContext = ctx;
    }

    @Override
    public AutoPlayContext getSlideCtx() {
        if (currentContext == null) {
            getEventBus().fireEvent(new AutoPlayFailureEvent("No autoplay context found"));
        }
        return currentContext;
    }

    @Override
    public SailingDispatchSystem getDispatch() {
        return dispatch;
    }
}
