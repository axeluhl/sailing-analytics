package com.sap.sailing.gwt.autoplay.client.app;

import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceController;
import com.google.web.bindery.event.shared.EventBus;
import com.sap.sailing.gwt.autoplay.client.place.player.DesktopPlayerView;
import com.sap.sailing.gwt.autoplay.client.place.player.PlayerView;
import com.sap.sailing.gwt.autoplay.client.place.start.DesktopStartView;
import com.sap.sailing.gwt.autoplay.client.place.start.StartPlace;
import com.sap.sailing.gwt.autoplay.client.place.start.StartView;
import com.sap.sse.gwt.client.mvp.ErrorView;


public class AutoPlayClientFactoryDesktopImpl extends AutoPlayClientFactoryBase implements AutoPlayClientFactory {
    public AutoPlayClientFactoryDesktopImpl() {
        this(new SimpleEventBus());
    }
    
    private AutoPlayClientFactoryDesktopImpl(EventBus eventBus) {
        this(eventBus, new PlaceController(eventBus));
    }

    private AutoPlayClientFactoryDesktopImpl(EventBus eventBus, PlaceController placeController) {
        this(eventBus, placeController, new PlaceNavigatorImpl(placeController));
    }

    private AutoPlayClientFactoryDesktopImpl(EventBus eventBus, PlaceController placeController, PlaceNavigator navigator) {
        super(new AutoPlayMainViewDesktopImpl(), eventBus, placeController, navigator);
    }
    
    @Override
    public StartView createStartView() {
        return new DesktopStartView(getPlaceNavigator(), getEventBus(), getUserService());
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
        return new StartPlace();
    }

}
